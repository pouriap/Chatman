/*
 * Copyright (c) 2020. Pouria Pirhadi
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.pouria.chatman;

import com.pouria.chatman.connection.ChatmanClient;
import com.pouria.chatman.gui.ChatFrame;
import com.pouria.chatman.messages.CMMessage;
import com.pouria.chatman.messages.DisplayableMessage;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 *
 * @author pouriap
 */
public class CMSendQueue {

	//we only send diaplayable messages with sendQueue
	private final ConcurrentLinkedQueue<DisplayableMessage> queue = new ConcurrentLinkedQueue<>();
	private QueueProcessorThread processThread = new QueueProcessorThread();
	private final int CONNECT_COOLDOWN = 1000 * 30;	//30 sec
	private long lastConnectTime = 0;
	private final ChatmanClient client;

	public CMSendQueue(ChatmanClient client){
		this.client = client;
	}

	private boolean connectWithCooldown(){
		long time = System.currentTimeMillis();
		if(time - lastConnectTime > CONNECT_COOLDOWN){
			boolean success = client.connect();
			lastConnectTime = time;
			return success;
		}
		return false;
	}
	
	public void add(DisplayableMessage object){
		queue.add(object);
	}
	
	public DisplayableMessage peek(){
		return queue.peek();
	}
	
	public DisplayableMessage poll(){
		return queue.poll();
	}
	
	public boolean isEmpty(){
		return queue.isEmpty();
	}
	
	public void process(){
		if(!processThread.isAlive()){
			processThread = new QueueProcessorThread();
			processThread.start();
		}
	}

	private class QueueProcessorThread extends Thread{

		public QueueProcessorThread(){
			super("CM-SendQeue-Processor");
		}

		@Override
		public void run() {
			//ta zamani ke chizi dar queue has edame bede
			while(!queue.isEmpty()){
				//avvalin message ra befrest
				DisplayableMessage firstMessage = queue.peek();
				OutgoingMsgHandler sender = new OutgoingMsgHandler(firstMessage);
				sender.send();
				//agar ferestade shod az saf dar biar va boro baadi
				if(firstMessage.getStatus() == CMMessage.Status.SENT){
					queue.poll();
					continue;
				}

				//agar nashod connect sho
				else{
					boolean connectFail = !connectWithCooldown();
					if(connectFail){
						/*
						fake a failed send if connect fails because server is not connected
						and actually sending them is useless
					    */
						for(DisplayableMessage message : queue){
							//don't do it again for the ones already done
							if(message.getStatus() == CMMessage.Status.NOTSENT) {
								message.onSend(false);
							}
						}
						return;
					}
					else{
						//agar connect shod dobare az avval befrest
						//khodesh ettefagh miofte chon tooye while() hastim
						//nokte: har vagh serveri peida mishavad CMSendQueue() call mishavad
						//vali chon ma khodeman alan inja hastim oon call barmigarde va hamin edame peida mikone
					}
				}
			}
		}
	}
	
}

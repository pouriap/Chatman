/*
 * Copyright (C) 2020 pouriap
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.pouria.chatman;

import com.pouria.chatman.classes.ChatmanClient;
import com.pouria.chatman.classes.ChatmanServer;
import com.pouria.chatman.connection.HttpClient;
import com.pouria.chatman.connection.HttpServer;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author pouriap
 */
public class Chatman {
	
	private final ChatmanServer server;
	private final ChatmanClient client;
	private final CMHistory history;
	private final BgTasksManager bgTasksMngr;
	
	private final int HISTORY_SAVE_INTERVAL = 1000 * 100; //100 sec
	private final int HEARTBEAT_INTERVAL = 1000 * 60; //60 sec
	private final long OLD_MSG_TIMEDIFF = 1000*60*60;	//1hour
	
	private final String horizontalLineHtml = "<div style='text-align:center;font-size:8px;font-color:#606060'>older messages<br>________________________________________________________________<br></div>";;
	private final ArrayList<CMMessage> allConversationMessages = new ArrayList<CMMessage>();
	private final CMSendQueue sendQueue = new CMSendQueue();
	
	public Chatman(){
		server = new HttpServer();
		client = new HttpClient();
		history = new CMHistory();
		bgTasksMngr = new BgTasksManager();
		client.addListener(bgTasksMngr);
		bgTasksMngr.start();
	}
		
	public void sendMessage(CMMessage m){
		sendQueue.add(m);
		sendQueue.process();
	}
	
    public void saveHistory(){
		history.save();
    }
	
	//anything that accesses Lists should be synchronized
	public synchronized void addToAllMessages(CMMessage message){
		//don't add duplicates (when resending failed messages)
		if(!allConversationMessages.contains(message)){
			allConversationMessages.add(message);
		}
	}
	
	public ChatmanServer getServer(){
		return server;
	}
	
	public ChatmanClient getClient(){
		return client;
	}
	
	public long getLastMessageTime(){
		if(allConversationMessages.isEmpty()){
			return System.currentTimeMillis();
		}
		CMMessage lastMessage = allConversationMessages.get(allConversationMessages.size()-1);
		return lastMessage.getTime();
	}
	
	public synchronized CMMessage[] getAllMessages(){
		CMMessage[] messages = new CMMessage[allConversationMessages.size()];
		allConversationMessages.toArray(messages);
		return messages;
	}

	//all access to lists should be synchronized
	public synchronized String getAllMessagesText(){
		
		//this takes ~20ms with a shitload of messages
		String conversationTextAll = "";
		long prevMessageTime = System.currentTimeMillis();
		for(CMMessage message: allConversationMessages){
			//put a line after older messages
			long messageTime = message.getTime();
			if(messageTime - prevMessageTime > OLD_MSG_TIMEDIFF){
				conversationTextAll += horizontalLineHtml;
			}
			prevMessageTime = messageTime;
			
			conversationTextAll += message.getDisplayableContent();
		}
		
		return conversationTextAll;
	}
	
	
	/**
	 * A class that manages background tasks that happen in threads
	 */
	private class BgTasksManager implements Observer{
		
		//starts threads/timers that should be running in the background
		public void start(){
			
			//connect for the first time and send a first ping letting them know we're up
			Runnable r = () -> {
				client.connect();
				CMMessage firstPing = new CMMessage(CMMessage.TYPE_PING, "", "");
				client.send(firstPing);
			};
			(new Thread(r, "CM-Initial-Connect")).start();
			
			//save history
			Timer historyTimer = new Timer("CM-History-Saver");
			TimerTask historyTask = new TimerTask() {
				@Override
				public void run() {
					saveHistory();
					CMHelper.getInstance().log("history saved from timer");
				}
			};
			historyTimer.scheduleAtFixedRate(historyTask, HISTORY_SAVE_INTERVAL, HISTORY_SAVE_INTERVAL);
				
			//send heartbeat
			Timer heartBeatTimer = new Timer("CM-Heartbeat-Sender");
			TimerTask heartBeatTask = new TimerTask() {
				@Override
				public void run() {
					CMMessage m = new CMMessage(CMMessage.TYPE_PING, "", "");
					boolean connected = client.send(m);
					if(!connected && !sendQueue.isEmpty()){
						client.connect();
					}
				}
			};
			heartBeatTimer.scheduleAtFixedRate(heartBeatTask, HEARTBEAT_INTERVAL, HEARTBEAT_INTERVAL);
			
		}
		
		/**
		 * is called from client when a server is found
		 * @param o
		 * @param arg 
		 */
		@Override
		public synchronized void update(Observable o, Object arg) {
			sendQueue.process();
		}

	}
	
}

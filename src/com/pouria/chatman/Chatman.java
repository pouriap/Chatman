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
import com.pouria.chatman.classes.CommandInvokeLater;
import com.pouria.chatman.classes.CommandUpdateChatHistory;
import com.pouria.chatman.connection.HttpClient;
import com.pouria.chatman.connection.HttpServer;
import com.pouria.chatman.gui.ChatFrame;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author pouriap
 */
public class Chatman {
	
	private final ChatmanServer server;
	private final ChatmanClient client;
	private final ChatmanHistory history;
	private final BgTasksManager bgTasksMngr;
	
	private final int CONNECT_COOLDOWN = 1000 * 30;	//30 sec
	private final int HISTORY_SAVE_INTERVAL = 1000 * 100; //100 sec
	private final int CONFIG_SAVE_INTERVAL = 1000 * 100; //100 sec
	private final int HEARTBEAT_INTERVAL = 1000 * 60; //60 sec
	
	private long lastConnectTime = 0;
	
	Lock unsavedLock = new ReentrantLock();
	Lock unsentLock = new ReentrantLock();
	
	private final ArrayList<ChatmanMessage> unsentMessages = new ArrayList<ChatmanMessage>();
	private final ArrayList<ChatmanMessage> allConversationMessages = new ArrayList<ChatmanMessage>();
	private final ConcurrentLinkedQueue<ChatmanMessage> sendQueue = new ConcurrentLinkedQueue<>();
	
	public Chatman(){
		server = new HttpServer();
		client = new HttpClient();
		history = new ChatmanHistory();
		bgTasksMngr = new BgTasksManager();
		client.addListener(bgTasksMngr);
		bgTasksMngr.start();
	}
	
	public void sendMessage(ChatmanMessage m){

		//message ro ghabl az thread sakhtan tooye saf bezar ke tartibe message he be ham nakhore
		//bad az inke message tooye thread ferestade shod be tartib message ha ro be conversationpane ezafe mikonim
		sendQueue.add(m);
		
		final ChatmanMessage thisMessage = m;
		
		Runnable r = () -> {
			//wait for unsent messages to be sent in case it is in progress
			bgTasksMngr.waitForUnsentMessagesToBeSent();
			while(true){
				ChatmanMessage firstMessage = sendQueue.peek();
				//agar in message avvalin message dar saf ast anra befrest
				if(firstMessage.equals(thisMessage)){
					boolean success = client.send(thisMessage);
					(new CommandInvokeLater(new CommandUpdateChatHistory(thisMessage))).execute();
					if(success){
						history.addToUnsavedMessages(m);
					}
					else{
						addToUnsentMessages(thisMessage);
						connectWithCooldown();
					}
					//in message ro az queue bekesh biroon ke nafare baadi ferestade beshe
					sendQueue.poll();
					break;
				}
				//agar nist sabr kon ta nobate in thisMessage beshe dar queue
				try{
					Thread.sleep(10);
				}catch(Exception e){}
			}

		};
		
		Thread th = new Thread(r);
		th.start();

	}
	
	public void connectWithCooldown(){
		long time = System.currentTimeMillis();
		if(time - lastConnectTime > CONNECT_COOLDOWN){
			client.connect();
			lastConnectTime = time;
		}
	}
	
    public void saveHistory(){
		history.save();
    }
	
	private void addToUnsentMessages(ChatmanMessage message){
		//accessing thread must have this lock
		synchronized(unsentMessages){
			this.unsentMessages.add(message);
		}
	}
	
	//anything that accesses Lists should be synchronized
	public synchronized void addToAllMessages(ChatmanMessage message){
		allConversationMessages.add(message);
	}
	
	public ChatmanServer getServer(){
		return server;
	}
	
	public ChatmanClient getClient(){
		return client;
	}
	
	
	/**
	 * A class that manages background tasks that happen in threads
	 */
	private class BgTasksManager implements Observer{
		
		private Thread unsentSenderThread;
		
		//starts threads/timers that should be running in the background
		public void start(){
			
			//connect for the first time
			Runnable r = () -> {
				client.connect();
			};
			(new Thread(r)).start();
			
			//save history
			Timer historyTimer = new Timer("history saver");
			TimerTask historyTask = new TimerTask() {
				@Override
				public void run() {
					saveHistory();
				}
			};
			historyTimer.scheduleAtFixedRate(historyTask, 0, HISTORY_SAVE_INTERVAL);
			
			//save config
			Timer configTimer = new Timer("config saver");
			TimerTask configTask = new TimerTask() {
				@Override
				public void run() {
					ChatmanConfig.getInstance().save();
				}
			};
			configTimer.scheduleAtFixedRate(configTask, 0, CONFIG_SAVE_INTERVAL);	
			
			//send heartbeat
			Timer heartBeatTimer = new Timer("heartbeat timer");
			TimerTask heartBeatTask = new TimerTask() {
				@Override
				public void run() {
					ChatmanMessage m = new ChatmanMessage(ChatmanMessage.TYPE_PING, "", "");
					boolean connected = client.send(m);
					if(!connected && !unsentMessages.isEmpty()){
						client.connect();
					}
				}
			};
			heartBeatTimer.scheduleAtFixedRate(heartBeatTask, 0, HEARTBEAT_INTERVAL);
			
		}
		
		/**
		 * is called from client when a server is found
		 * @param o
		 * @param arg 
		 */
		@Override
		public synchronized void update(Observable o, Object arg) {
			Runnable r = () -> {
				sendUnsentMessages();
			};
			unsentSenderThread = new Thread(r);
			unsentSenderThread.start();
		}
		
		//is only called from update() which is only called when a server is set
		private void sendUnsentMessages(){
			synchronized(unsentMessages){

				if(unsentMessages.isEmpty()){
					return;
				}

				ChatmanMessage unsents[] = new ChatmanMessage[unsentMessages.size()];
				unsentMessages.toArray(unsents);
				for(int i=0; i<unsents.length; i++){
					ChatmanMessage unsentMessage = unsents[i];
					boolean success = client.send(unsentMessage);
					if(success){
						unsentMessages.remove(unsentMessage);
						//remove the unsent one and add the sent one to the end
						allConversationMessages.remove(unsentMessage);
						allConversationMessages.add(unsentMessage);
					}
				}

				String conversationTextAll = "";
				for(ChatmanMessage message: allConversationMessages){
					conversationTextAll += message.getDisplayableContent();
				}
				ChatFrame.getInstance().updateConversationTextAll(conversationTextAll);

			}
		}

		public void waitForUnsentMessagesToBeSent(){
			try{
				if(unsentSenderThread != null){
					unsentSenderThread.join();
				}
			}catch(InterruptedException e){}
		}

	}
	
}

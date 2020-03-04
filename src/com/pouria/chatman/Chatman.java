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
	private final ChatmanHistory history;
	private final BgTasksManager bgTasksMngr;
	
	private final int HISTORY_SAVE_INTERVAL = 1000 * 100; //100 sec
	private final int CONFIG_SAVE_INTERVAL = 1000 * 100; //100 sec
	private final int HEARTBEAT_INTERVAL = 1000 * 60; //60 sec
	
	private final ArrayList<ChatmanMessage> allConversationMessages = new ArrayList<ChatmanMessage>();
	private final SendQueue sendQueue = new SendQueue();
	
	public Chatman(){
		server = new HttpServer();
		client = new HttpClient();
		history = new ChatmanHistory();
		bgTasksMngr = new BgTasksManager();
		client.addListener(bgTasksMngr);
		bgTasksMngr.start();
	}
	
	//TODO: add names to threads
	
	public void sendMessage(ChatmanMessage m){
		//TODO: add sendQueue as static to Outgoingmessagehandler 
		sendQueue.add(m);
		sendQueue.process();
	}
	
	//TODO: this doesn't belong here
	public void addToUnsavedMessages(ChatmanMessage message){
		history.addToUnsavedMessages(message);
	}
	
    public void saveHistory(){
		history.save();
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
	
	public long getLastMessageTime(){
		if(allConversationMessages.isEmpty()){
			return System.currentTimeMillis();
		}
		ChatmanMessage lastMessage = allConversationMessages.get(allConversationMessages.size()-1);
		return lastMessage.getTime();
	}
	
	//synchronized to be safe
	public synchronized String getConversationTextAll(){
		String conversationTextAll = "";
		for(ChatmanMessage message: allConversationMessages){
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
				ChatmanMessage m = new ChatmanMessage(ChatmanMessage.TYPE_PING, "", "");
				client.send(m);
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
			historyTimer.scheduleAtFixedRate(historyTask, HISTORY_SAVE_INTERVAL, HISTORY_SAVE_INTERVAL);
			
			//save config
			Timer configTimer = new Timer("config saver");
			TimerTask configTask = new TimerTask() {
				@Override
				public void run() {
					ChatmanConfig.getInstance().save();
				}
			};
			configTimer.scheduleAtFixedRate(configTask, CONFIG_SAVE_INTERVAL, CONFIG_SAVE_INTERVAL);	
			
			//send heartbeat
			Timer heartBeatTimer = new Timer("heartbeat timer");
			TimerTask heartBeatTask = new TimerTask() {
				@Override
				public void run() {
					ChatmanMessage m = new ChatmanMessage(ChatmanMessage.TYPE_PING, "", "");
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

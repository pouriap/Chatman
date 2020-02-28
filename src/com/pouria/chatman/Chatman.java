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
import com.pouria.chatman.classes.CommandFatalErrorExit;
import com.pouria.chatman.classes.CommandInvokeLater;
import com.pouria.chatman.classes.CommandUpdateChatHistory;
import com.pouria.chatman.connection.HttpClient;
import com.pouria.chatman.connection.HttpServer;
import com.pouria.chatman.gui.ChatFrame;
import java.util.ArrayList;

/**
 *
 * @author pouriap
 */
public class Chatman {
	
	ChatmanServer server;
	ChatmanClient client;
	ChatmanHistory history;
	
	ArrayList<ChatmanMessage> unsavedMessages = new ArrayList<ChatmanMessage>();
	ArrayList<ChatmanMessage> unsentMessages = new ArrayList<ChatmanMessage>();
	ArrayList<ChatmanMessage> allConversationMessages = new ArrayList<ChatmanMessage>();
	
	public Chatman(){
		server = new HttpServer();
		client = new HttpClient();
		history = new ChatmanHistory();
	}
	
	public void startUnsentWatcher(){
		//constantly try to sendMessage unsent messages
		Runnable r = new Runnable() {
			@Override
			public void run() {
				while(true){
					try{
						Thread.sleep(1000*30);
					}catch(Exception e){
						String error = "unsent messages thread cannot sleep" + e.getMessage();
						(new CommandInvokeLater(new CommandFatalErrorExit(error))).execute();
					}
					System.out.println("sending unsent messages from thread");
					if(!client.isServerConnected()){
						client.connect();
					}
					sendUnsentMessages();
				}
			}
		};
		Thread th = new Thread(r);
		th.start();
	}
	
	public void sendMessage(ChatmanMessage message){
		
		final ChatmanMessage m = message;
		
		//TODO: change 'chathistory' to 'conversationpane'
		Runnable r = new Runnable() {
			@Override
			public void run() {
				//this is a guard to preserve order
				if(client.isServerConnected()){
					//if we are sending a message and there are unsent messages sendMessage them first to preserve order
					//fek nemikonam hich vagh ettefagh biofte, jahatte etminane
					if(!unsentMessages.isEmpty()){
						sendUnsentMessages();
					}
					//server connect has vali momkene dagighan dar hami lahze dis shode bashe pas check mikonim hatman rafte bashe
					boolean success = client.send(m);
					if(!success){
						addToUnsentMessages(m);
					}
					(new CommandInvokeLater(new CommandUpdateChatHistory(m))).execute();
				}
				//if server is not connected
				else{
					//agar vasl nistim bezar too unsent
					addToUnsentMessages(m);
					(new CommandInvokeLater(new CommandUpdateChatHistory(m))).execute();
					//agar dar hale vasl shodan nistim vasl sho
					if(!((HttpClient)client).isConnectInProgress()){	//in kar tooye connect() khodash anjam mishavad inja baraye vozooh gozashtam
						boolean connected = client.connect();
						//agar vasl shodi unsent hara befrest
						if(connected){
							sendUnsentMessages();
						}
					}
				}
			}
		};
		
		Thread th = new Thread(r);
		th.start();

	}
	
	public ChatmanServer getServer(){
		return server;
	}
	
	public ChatmanClient getClient(){
		return client;
	}
	
	public void addToUnsavedMessages(ChatmanMessage message){
		unsavedMessages.add(message);
	}
	
	//not thread safe
    public void saveHistory(){
		history.save(unsavedMessages);
    }
	
	public synchronized void addToUnsentMessages(ChatmanMessage message){
		this.unsentMessages.add(message);
	}
	
	public synchronized void sendUnsentMessages(){
		System.out.println("sending from thread: " + Thread.currentThread().getName());
		//age az avval server nadashte bashim momkene vasate 'for' server vasl beshe
		//va message ha bedoone tartib beran
		if(!client.isServerConnected()){
			return;
		}
		
		ChatmanMessage unsents[] = new ChatmanMessage[unsentMessages.size()];
		unsentMessages.toArray(unsents);
		for(int i=0; i<unsents.length; i++){
			ChatmanMessage unsentMessage = unsents[i];
			boolean success = client.send(unsentMessage);
			if(success){
				this.unsentMessages.remove(unsentMessage);
				//remove the unsent one and add the sent one to the end
				this.allConversationMessages.remove(unsentMessage);
				this.allConversationMessages.add(unsentMessage);
			}
		}
		
		String conversationTextAll = "";
		for(ChatmanMessage message: allConversationMessages){
			conversationTextAll += message.getDisplayableContent();
		}
		ChatFrame.getInstance().updateConversationTextAll(conversationTextAll);
	}
	
	public void addToAllMessages(ChatmanMessage message){
		allConversationMessages.add(message);
	}
	
}

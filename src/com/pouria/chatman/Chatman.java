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
import com.pouria.chatman.classes.SendCallback;
import com.pouria.chatman.connection.HttpClient;
import com.pouria.chatman.connection.HttpServer;
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
	
	public Chatman(){
		server = new HttpServer();
		client = new HttpClient();
		history = new ChatmanHistory();
	}
	
	public void send(ChatmanMessage message, SendCallback callback){
		//TODO: tell user we are reconnecting (useful for file drop)
		//TODO: baraye special ha mesle shutdown yekari konim karbar befahme vaghti narafte
		
		if(callback == null){
			callback = new SendCallback() {
				@Override
				public void call(boolean success, String reason) {
					//nothing
				}
			};
		}
		
		final ChatmanMessage _message = message;
		final SendCallback _callback = callback;
	
		//perform send in a thread
		Runnable r = new Runnable() {
			@Override
			public void run() {
				client.send(_message, _callback);
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
	
	public void addToUnsentMessages(ChatmanMessage message){
		unsentMessages.add(message);
	}
	
	//not thread safe
    public void saveHistory(){
		history.save(unsavedMessages);
    }
	
}

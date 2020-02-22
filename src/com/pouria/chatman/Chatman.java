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
import com.pouria.chatman.classes.PeerNotFoundException;
import com.pouria.chatman.connection.HttpClient;
import com.pouria.chatman.connection.HttpServer;

/**
 *
 * @author pouriap
 */
public class Chatman {
	
	ChatmanServer server;
	ChatmanClient client;
	
	public Chatman(){
		server = new HttpServer();
		client = new HttpClient();
	}
	
	public boolean send(ChatmanMessage message){
		try{
			client.send(message);
			return true;
		}catch(PeerNotFoundException e){
			client.connect();
			//TODO: baraye special ha mesle shutdown yekari konim karbar befahme vaghti narafte
			return false;
		}
	}
	
	public ChatmanServer getServer(){
		return server;
	}
	
	public ChatmanClient getClient(){
		return client;
	}
	
}

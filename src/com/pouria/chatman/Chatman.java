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
import com.pouria.chatman.gui.ChatFrame;
import java.util.ArrayList;

/**
 *
 * @author pouriap
 */
public class Chatman {
	
	ChatmanServer server;
	ChatmanClient client;
	
	ArrayList<ChatmanMessage> unsavedMessages = new ArrayList<ChatmanMessage>();
	ArrayList<ChatmanMessage> unsentMessages = new ArrayList<ChatmanMessage>();
	
	public Chatman(){
		server = new HttpServer();
		client = new HttpClient();
	}
	
	public boolean send(ChatmanMessage message){
		try{
			client.send(message);
			return true;
		}catch(PeerNotFoundException e){
			//TODO: tell user we are reconnecting (useful for file drop)
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
	
	public void addToUnsavedMessages(ChatmanMessage message){
		unsavedMessages.add(message);
	}
	
	public void addToUnsentMessages(ChatmanMessage message){
		unsentMessages.add(message);
	}
	
	//not thread safe
    public void saveHistory(){
		
		ChatFrame.getInstance().message("not supported");
		
		//we don't want to save empty stuff
//        if(unsavedMessages.isEmpty())
//            return;
//        
//        Connection c = null;
//        PreparedStatement stmt = null;
//        Date date = new Date();
//
//        try {
//			
//			Class.forName("org.sqlite.JDBC");
//			
//            c = DriverManager.getConnection("jdbc:sqlite:history.sqlite");
//            c.setAutoCommit(false);            
//
//            stmt = c.prepareStatement("INSERT INTO chat_sessions (date, text) Values(? , ?)");
//            stmt.setString(1, String.valueOf(date.getTime()));
//            stmt.setString(2, incomingTextAll);
//            stmt.executeUpdate();
//            c.commit();
//            
//            stmt.close();
//            c.close();
//					
//        } catch ( Exception e ) {
//            message(Helper.getInstance().getStr("history_save_fail") + e.getMessage());
//        }
    }
	
}

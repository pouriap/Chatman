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

import com.pouria.chatman.gui.ChatFrame;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;

/**
 *
 * @author pouriap
 */
public class ChatmanHistory {
	
	private static String previousChatHistory = "";
	private final ArrayList<ChatmanMessage> unsavedMessages = new ArrayList<ChatmanMessage>();
	
	public synchronized void save(){

		//we don't want to save empty stuff
        if(unsavedMessages.isEmpty())
            return;
        
        Connection con;
        PreparedStatement stmt;
		
        try {
			
			Class.forName("org.sqlite.JDBC");		
            con = DriverManager.getConnection("jdbc:sqlite:history.sqlite");
            con.setAutoCommit(false);
			
			ChatmanMessage unsaveds[] = new ChatmanMessage[unsavedMessages.size()];
			unsavedMessages.toArray(unsaveds);
			for(int i=0; i<unsaveds.length; i++){
				
				ChatmanMessage message = unsaveds[i];
				long messageTime = message.getTime();
				//set message date to 00:00:00 of the day the message was sent
				Calendar messageDate = Calendar.getInstance();
				messageDate.setTimeInMillis(messageTime);
				messageDate.set(Calendar.HOUR_OF_DAY, 0);
				messageDate.set(Calendar.MINUTE, 0);
				messageDate.set(Calendar.SECOND, 0);
				messageDate.set(Calendar.MILLISECOND, 0);
				long messageDay = messageDate.getTimeInMillis();
				stmt = con.prepareStatement("SELECT * FROM chat_sessions WHERE date=?");
				stmt.setLong(1, messageDay);
				ResultSet rs = stmt.executeQuery();
				//if there is a result it means that day already exists in table
				if(rs.next()){
					//we append
					String content = message.getDisplayableContent();
					stmt = con.prepareStatement("UPDATE chat_sessions SET text=text||? WHERE date=?");
					stmt.setString(1, content);
					stmt.setLong(2, messageDay);
					stmt.executeUpdate();
					con.commit();	
					stmt.close();
				}
				//there is no result so we have to create that day in the table
				else{
					String content = message.getDisplayableContent();
					stmt = con.prepareStatement("INSERT INTO chat_sessions (date, text) Values(? , ?)");
					stmt.setLong(1, messageDay);
					stmt.setString(2, content);
					stmt.executeUpdate();
					con.commit();	
					stmt.close();
				}
				//if success
				unsavedMessages.remove(message);

			}
			
			con.close();
					
        } catch ( Exception e ) {
            ChatFrame.getInstance().message(Helper.getInstance().getStr("history_save_fail") + e.getMessage());
        }
				
	}
	
	public synchronized void addToUnsavedMessages(ChatmanMessage message){
		unsavedMessages.add(message);
	}
	
	public static void storeCurrentHistory(String history){
		previousChatHistory = history;
	}
	
	public static String getStoredHistory(){
		return previousChatHistory;
	}
}

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

import com.pouria.chatman.classes.AbstractSQLPagination;
import com.pouria.chatman.classes.HistoryTablePagination;
import com.pouria.chatman.messages.CMMessage;
import com.pouria.chatman.messages.DisplayableMessage;

import javax.swing.*;
import java.io.File;
import java.sql.*;
import java.util.*;


/**
 *
 * @author pouriap
 */
public class CMHistory {

	private static String dbFileName = "";
	private String connectionURL;

	public CMHistory(String dbFileName){
		CMHistory.dbFileName = dbFileName;
		connectionURL = "jdbc:sqlite:" + dbFileName;
	}
	
	public synchronized void save(List<DisplayableMessage> messagesToSave){

		CMDBData data = new CMDBData();

        for(DisplayableMessage message : messagesToSave){
	        //don't save saved or failed messages
	        if(message.isSaved() || message.getStatus() == CMMessage.Status.SENDFAIL){
		        continue;
	        }
	        data.put(message);
        }

		Connection con = null;
		PreparedStatement stmtQuery = null;
		PreparedStatement stmtUpdate = null;

		try{

			con = DriverManager.getConnection(connectionURL);
			con.setAutoCommit(false);

			for(String day: data.messagesOfDays.keySet()){

				long dayLong = Long.parseLong(day);
				String textOfDayString = data.messagesOfDays.get(day).toString();

				stmtQuery = con.prepareStatement("SELECT * FROM chat_sessions WHERE date=?");
				stmtQuery.setLong(1, dayLong);
				ResultSet rs = stmtQuery.executeQuery();

				//if there is a result it means that day already exists in table
				if(rs.next()){
					//we append
					stmtUpdate = con.prepareStatement("UPDATE chat_sessions SET text=text||? WHERE date=?");
					stmtUpdate.setString(1, textOfDayString);
					stmtUpdate.setLong(2, dayLong);
				}
				//there is no result so we have to create that day in the table
				else{
					stmtUpdate = con.prepareStatement("INSERT INTO chat_sessions (date, text) Values(? , ?)");
					stmtUpdate.setLong(1, dayLong);
					stmtUpdate.setString(2, textOfDayString);
				}
				stmtUpdate.executeUpdate();

				rs.close();
				stmtQuery.close();
				stmtUpdate.close();

			}

			con.commit();
			con.close();

			//if success
			data.setIsSaved();

		}catch(Exception e){
			CMHelper.getInstance().log("failed to save history: " + e.getMessage());
			//roll back changes if any exception occured
			if(con != null){
				try{
					con.rollback();
				}catch(Exception ee){
					//java is stupid
				}
			}
		}finally{
			//close everything
			try{
				if(con != null){
					con.close();
				}
				if(stmtQuery != null){
					stmtQuery.close();
				}
				if(stmtUpdate != null){
					stmtUpdate.close();
				}
			}catch(Exception e){
				//java is stupid
			}
		}
	}

	/**
	 * creates the database file if it doesn't exist
	 * @throws Exception
	 */
	public void createDBIfNotExist() throws Exception{

		Class.forName("org.sqlite.JDBC");

		File dbFile = new File(dbFileName);
		if(dbFile.isFile()){
			return;
		}

		try(
			Connection con = DriverManager.getConnection(connectionURL);
			Statement stmt = con.createStatement()
		){
			CMHelper.getInstance().log("history database doesn't exist. creating it");
			dbFile.createNewFile();
			CMHelper.getInstance().log("history database created successfully");

			CMHelper.getInstance().log("creating database tables");
			String query = "CREATE TABLE IF NOT EXISTS chat_sessions (id INTEGER PRIMARY KEY ASC AUTOINCREMENT UNIQUE NOT NULL, date INTEGER UNIQUE NOT NULL, text VARCHAR NOT NULL)";
			stmt.execute(query);

			CMHelper.getInstance().log("tables created succesffully");

		}catch (Exception e){
			throw new Exception("history database cannot be created", e);
		}

	}

	public static AbstractSQLPagination getPagination(JTable tableHistory){
		return new HistoryTablePagination(5, tableHistory, dbFileName);
	}

	private static class CMDBData{

		Map<String, StringBuilder> messagesOfDays = new HashMap<>();
		List<DisplayableMessage> allMessages = new ArrayList<>();

		public void put(DisplayableMessage message){

			allMessages.add(message);

			long messageTime = message.getTime();
			//set message date to 00:00:00 of the day the message was sent
			Calendar messageDate = Calendar.getInstance();
			messageDate.setTimeInMillis(messageTime);
			messageDate.set(Calendar.HOUR_OF_DAY, 0);
			messageDate.set(Calendar.MINUTE, 0);
			messageDate.set(Calendar.SECOND, 0);
			messageDate.set(Calendar.MILLISECOND, 0);
			String day = String.valueOf(messageDate.getTimeInMillis());
			String messageHTML = message.getAsHTMLString();

			if(messagesOfDays.containsKey(day)){
				messagesOfDays.get(day).append(messageHTML);
			}
			else{
				StringBuilder builder = new StringBuilder(messageHTML);
				messagesOfDays.put(day, builder);
			}
		}

		public void setIsSaved(){
			for(DisplayableMessage message: allMessages){
				message.setIsSaved(true);
			}
		}

	}

}

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

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.pouria.chatman.classes.CommandInvokeLater;
import com.pouria.chatman.classes.CommandShowMessage;
import com.pouria.chatman.classes.CommandUpdateIncomingText;
import com.pouria.chatman.gui.ChatFrame;
import java.io.IOException;
import java.util.Deque;
import java.util.Map;

/**
 *
 * @author pouriap
 */
public class ChatmanMessageHandler {
	
	private String rawMessage = "";
	private ChatmanMessage message;
	
	public ChatmanMessageHandler(Map<String,Deque<String>> httpQueryParameters){
		try{
			//throws exception is "message" not present
			rawMessage = httpQueryParameters.get("message").pop();
			Gson gson = new Gson();
			message = gson.fromJson(rawMessage, ChatmanMessage.class);
			
		}catch(JsonSyntaxException e){
			//create a 'bad message' json as our message because the original one is lost
			message = new ChatmanMessage(ChatmanMessage.TYPE_BADMESSAGE, "bad json syntax", "unknown", 0);
			
		}catch(Exception e){
			//create a 'bad message' json as our message because the original one is lost
			message = new ChatmanMessage(ChatmanMessage.TYPE_BADMESSAGE, "bad message", "unknown", 0);
		}
	}
	
	public void handle(){
		
		int messageType = message.getType();
		
		switch(messageType){
			
			case ChatmanMessage.TYPE_BADMESSAGE:
				processBadMessage();
				break;
				
			case ChatmanMessage.TYPE_TEXT:
				processTextMessage();
				break;
				
			case ChatmanMessage.TYPE_FILE:
				processFileMessage();
				break;
			
			case ChatmanMessage.TYPE_SHUTDOWN:
				processShutdown();
				break;
				
			case ChatmanMessage.TYPE_ABORT_SHUTDOWN:
				processAbortShutdown();
				break;
				
			case ChatmanMessage.TYPE_SHOWGUI:
				processShowGUI();
				break;
					
			default:
				break;
		}
	}
	
	public void processBadMessage(){
		System.out.println(message.getContent());
	}
	
	public void processTextMessage(){
		(new CommandInvokeLater(new CommandUpdateIncomingText(message))).execute();
	}
	
	public void processFileMessage(){
//		//get file
//		String f = reader.readLine();
//		final String fileName = new String(BaseEncoding.base64().decode(f), Charsets.UTF_8);
//		String fileData = reader.readLine();
//		final String location = (new JFileChooser()).getFileSystemView().getDefaultDirectory().toString() + "\\Chatman Downloads\\";
//
//		//save file
//		try{
//			File saveDir = new File(location);
//			if(!saveDir.isDirectory())
//				saveDir.mkdir();
//			Files.write(BaseEncoding.base64().decode(fileData), new File(location + fileName));
//
//			(new CommandInvokeLater(new CommandUpdateIncomingText(Helper.getInstance().getStr("file_recieved") + fileName + " - " + Helper.getInstance().getStr("saved_in") + "file://" + location + fileName))).execute();
//
//		}catch(IOException e){
//			(new CommandInvokeLater(new CommandMessage(Helper.getInstance().getStr("file_save_fail") + e.getMessage()))).execute();
//		}
	}
	
	public void processShutdown(){
//		//show cancell dialog
//		(new CommandInvokeLater(new CommandConfirmDialog(new Command() {
//			@Override
//			public void execute() {
//				try{
//					Helper.getInstance().abortLocalShutdown();
//				}catch(IOException e){
//					gui.message("couldn't stop the shutdown :(");
//				}
//			}
//		}, Helper.getInstance().getStr("local_shutdown_message"), Helper.getInstance().getStr("local_shutdown_title")))).execute();
//
//		//start shutdown process
//		Helper.getInstance().localShutdown();
	}
	
	public void processAbortShutdown(){
		try{
			Helper.getInstance().abortLocalShutdown();
		}catch(IOException e){
			(new CommandInvokeLater(new CommandShowMessage("Abort Failed"))).execute();
		}
	}
	
	public void processShowGUI(){
		ChatFrame.getInstance().showWindow();
	}
	
}

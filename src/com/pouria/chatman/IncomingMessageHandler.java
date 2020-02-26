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

import com.google.common.io.Files;
import com.pouria.chatman.classes.Command;
import com.pouria.chatman.classes.CommandConfirmDialog;
import com.pouria.chatman.classes.CommandInvokeLater;
import com.pouria.chatman.classes.CommandShowError;
import com.pouria.chatman.classes.CommandUpdateChatHistory;
import com.pouria.chatman.gui.ChatFrame;
import java.io.File;
import java.io.IOException;
import javax.swing.JFileChooser;

/**
 *
 * @author pouriap
 */
public class IncomingMessageHandler {
	
	private final ChatmanMessage message;
	
	public IncomingMessageHandler(ChatmanMessage message){
		this.message = message;
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
		(new CommandInvokeLater(new CommandUpdateChatHistory(message))).execute();
	}
	
	public void processTextMessage(){
		(new CommandInvokeLater(new CommandUpdateChatHistory(message))).execute();
	}
	
	public void processFileMessage(){
		String tmpFilePath = message.getContent().split("\\*\\*")[0];
		String fileName = message.getContent().split("\\*\\*")[1];
		//filename is saved in 'sender' field hehe
		String dlDirectory = (new JFileChooser()).getFileSystemView().getDefaultDirectory().toString() + "\\Chatman Downloads\\";
		File srcFile = new File(tmpFilePath);
		File dstFile = new File(dlDirectory+fileName);
		//save file
		try{
			File saveDir = new File(dlDirectory);
			if(!saveDir.isDirectory()){
				saveDir.mkdir();
			}
			Files.copy(srcFile, dstFile);
			//set file path (=content) to the one saved in Chatman Downloads
			message.setContent(dstFile.getAbsolutePath());
			(new CommandInvokeLater(new CommandUpdateChatHistory(message))).execute();
			
		}catch(IOException e){
			ChatmanMessage displayedMessage = new ChatmanMessage(ChatmanMessage.TYPE_TEXT, "File receive failed", "ERROR: ", message.getTime());
			(new CommandInvokeLater(new CommandUpdateChatHistory(displayedMessage))).execute();
		}
	}
	
	public void processShutdown(){

		//start shutdown process
		try{
			
			Helper.getInstance().localShutdown();
			
			//show cancell dialog
			(new CommandInvokeLater(new CommandConfirmDialog(new Command() {
				@Override
				public void execute() {
					try{
						//if user chooses cancel shutdown
						Helper.getInstance().abortLocalShutdown();
						//tell the other computer we have aborted
						String info = "[INFO: REMOTE SHUTDOWN ABORTED BY USER]";
						String sender = ChatFrame.getInstance().getUserName();
						ChatmanMessage message = new ChatmanMessage(ChatmanMessage.TYPE_TEXT, info, sender);
						ChatFrame.getInstance().getChatmanInstance().send(message, null);
					}catch(IOException e){
						// we don't need invokelater because we're already in invokelater
						(new CommandShowError("could not abort shutdown")).execute();
					}
				}
			}, Helper.getInstance().getStr("local_shutdown_message"), Helper.getInstance().getStr("local_shutdown_title")))).execute();

		}catch(Exception e){
			//tell the other computer our shutdown has failed
			String error = "[ERROR: SHUTDOWN FAILED]";
			String sender = ChatFrame.getInstance().getUserName();
			ChatmanMessage msg = new ChatmanMessage(ChatmanMessage.TYPE_TEXT, error, sender);
			ChatFrame.getInstance().getChatmanInstance().send(msg, null);
		}
		
		(new CommandInvokeLater(new CommandUpdateChatHistory(message))).execute();

	}
	
	public void processAbortShutdown(){
		String msgText;
		String sender = ChatFrame.getInstance().getUserName();
		try{
			Helper.getInstance().abortLocalShutdown();
			msgText = "[SHUTDOWN ABORTED SUCCESSFULLY]";
		}catch(IOException e){
			msgText = "[ERROR: COULD NOT ABORT THE SHUTDOWN]";
		}
		//tell the other computer what happened
		ChatmanMessage msg = new ChatmanMessage(ChatmanMessage.TYPE_TEXT, msgText, sender);
		ChatFrame.getInstance().getChatmanInstance().send(msg, null);
		
		(new CommandInvokeLater(new CommandUpdateChatHistory(message))).execute();
	}
	
	public void processShowGUI(){
		ChatFrame.getInstance().showWindow();
	}
	
}

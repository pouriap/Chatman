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
import com.pouria.chatman.classes.CommandInvokeLater;
import com.pouria.chatman.classes.CommandShowMessage;
import com.pouria.chatman.classes.CommandUpdateIncomingText;
import com.pouria.chatman.gui.ChatFrame;
import java.io.File;
import java.io.IOException;
import javax.swing.JFileChooser;

/**
 *
 * @author pouriap
 */
public class ChatmanMessageHandler {
	
	private final ChatmanMessage message;
	
	public ChatmanMessageHandler(ChatmanMessage message){
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
		(new CommandInvokeLater(new CommandUpdateIncomingText(message))).execute();
	}
	
	public void processTextMessage(){
		(new CommandInvokeLater(new CommandUpdateIncomingText(message))).execute();
	}
	
	public void processFileMessage(){
		String tmpFilePath = message.getContent();
		//filename is saved in 'sender' field hehe
		String fileName = message.getSender();
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
			String content = "file://"+dstFile.getAbsolutePath();
			String sender = Helper.getInstance().getStr("file_recieved");
			ChatmanMessage displayedMessage = new ChatmanMessage(ChatmanMessage.TYPE_TEXT, content, sender, message.getTime());
			(new CommandInvokeLater(new CommandUpdateIncomingText(displayedMessage))).execute();
		}catch(IOException e){
			ChatmanMessage displayedMessage = new ChatmanMessage(ChatmanMessage.TYPE_TEXT, "File receive failed", "ERROR: ", message.getTime());
			(new CommandInvokeLater(new CommandUpdateIncomingText(displayedMessage))).execute();
		}
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

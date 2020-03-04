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
		Helper.getInstance().log("bad message received");
	}
	
	public void processTextMessage(){
	}
	
	public void processFileMessage(){
		Helper.getInstance().log("file message received");
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
				Helper.getInstance().log("download dir doesn't exist. creating download dir");
				saveDir.mkdir();
				Helper.getInstance().log("download dir created successfully");
			}
			Helper.getInstance().log("copying received file from " + srcFile.getAbsolutePath() + " to " + dstFile.getAbsolutePath());
			Files.copy(srcFile, dstFile);
			Helper.getInstance().log("file copied");
			//set file path (=content) to the one saved in Chatman Downloads
			message.setContent(dstFile.getAbsolutePath());
			
		}catch(IOException e){
			Helper.getInstance().log("copying file from tmp folder to download direcoty failed");
			String content = Helper.getInstance().getStr("file-receive-failed");
			message.setContent("ERROR: " + content);
		}
	}
	
	public void processShutdown(){

		//start shutdown process
		try{
			
			Helper.getInstance().log("remote shutdown message received");
			Helper.getInstance().localShutdown();
			
			//show cancell dialog
			(new CommandInvokeLater(new CommandConfirmDialog(new Command() {
				@Override
				public void execute() {
					try{
						//if user chooses cancel shutdown
						Helper.getInstance().log("abort shutdown requested by user");
						Helper.getInstance().abortLocalShutdown();
						//tell the user abort was successfull
						Helper.getInstance().log("shutdown aborted successfully");
						ChatFrame.getInstance().message(Helper.getInstance().getStr("shutdown-abort-success"));	// we don't need invokelater because we're already in invokelater
						//tell the other computer we have aborted
						String info = "[INFO: REMOTE SHUTDOWN ABORTED BY USER]";
						String sender = ChatFrame.getInstance().getUserName();
						ChatmanMessage message = new ChatmanMessage(ChatmanMessage.TYPE_TEXT, info, sender);
						ChatFrame.getInstance().getChatmanInstance().sendMessage(message);
					}catch(IOException e){
						Helper.getInstance().log("failed to abort local shutdown");
						//tell the user abort failed. we don't tell the other computer because it's not necessary
						(new CommandShowError(Helper.getInstance().getStr("shutdown-abort-fail"))).execute();  // we don't need invokelater because we're already in invokelater
					}
				}
			}, Helper.getInstance().getStr("local_shutdown_message"), Helper.getInstance().getStr("local_shutdown_title")))).execute();

		}catch(Exception e){
			Helper.getInstance().log("shutdown failed");
			//tell the user shutdown has failed
			(new CommandInvokeLater(new CommandShowError(Helper.getInstance().getStr("shutdown-fail")))).execute();
			//tell the other computer our shutdown has failed
			String error = "[ERROR: SHUTDOWN FAILED]";
			String sender = ChatFrame.getInstance().getUserName();
			ChatmanMessage msg = new ChatmanMessage(ChatmanMessage.TYPE_TEXT, error, sender);
			ChatFrame.getInstance().getChatmanInstance().sendMessage(msg);
		}

	}
	
	public void processAbortShutdown(){
		
		String msgText;
		String sender = ChatFrame.getInstance().getUserName();
		Helper.getInstance().log("remote-abort-shutdown received");
		try{
			Helper.getInstance().abortLocalShutdown();
			Helper.getInstance().log("shutdown aborted successfully");
			msgText = "[SHUTDOWN ABORTED SUCCESSFULLY]";
		}catch(IOException e){
			Helper.getInstance().log("abort failed");
			msgText = "[ERROR: COULD NOT ABORT THE SHUTDOWN]";
		}
		//tell the other computer if abort was successfull
		ChatmanMessage msg = new ChatmanMessage(ChatmanMessage.TYPE_TEXT, msgText, sender);
		ChatFrame.getInstance().getChatmanInstance().sendMessage(msg);
		
	}
	
	public void processShowGUI(){
		ChatFrame.getInstance().showWindow();
	}
	
}

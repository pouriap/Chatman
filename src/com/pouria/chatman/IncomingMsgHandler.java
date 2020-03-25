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

import com.pouria.chatman.commands.CmdConfirmDialog;
import com.pouria.chatman.commands.CmdInvokeLater;
import com.pouria.chatman.commands.CmdShowError;
import com.pouria.chatman.enums.CMType;
import com.pouria.chatman.gui.CMTheme;
import com.pouria.chatman.gui.ChatFrame;
import com.pouria.chatman.messages.*;
import com.pouria.chatman.messages.BadMessage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Base64;


/**
 *
 * @author pouriap
 */
public class IncomingMsgHandler {
	
	private CMMessage message;
	
	public IncomingMsgHandler(CMMessage message){
		this.message = message;
	}
	
	public void receive(){
		
		CMType messageType = message.getType();

		switch(messageType){
			
			case BADMESSAGE:
				processBadMessage();
				break;
				
			case TEXT:
				processTextMessage();
				break;
				
			case FILE:
				processFileMessage();
				break;
			
			case SHUTDOWN:
				processShutdown();
				break;
				
			case ABORT_SHUTDOWN:
				processAbortShutdown();
				break;
				
			case PING:
				processPing();
				break;
				
			case SHOWGUI:
				processShowGUI();
				break;
			
			case THEME_FILE:
				processThemeFile();
				break;
				
			case REQUEST_THEME_FILE:
				processRequestThemeFile();
				break; 
				
			default:
				break;
		}

	}
	
	private void processBadMessage(){
		String content = ((BadMessage)message).getContent();
		CMHelper.getInstance().log("bad message received: " + content);
	}
	
	private void processTextMessage(){
	}
	
	private void processFileMessage(){
		
		CMHelper.getInstance().log("file message received");

		CMMessage.Direction direction = ((FileMessage)message).getDirection();
		String sender = ((FileMessage)message).getSender();
		String senderTheme = ((FileMessage)message).getSenderTheme();
		long time = ((FileMessage)message).getTime();
		String fileName = ((FileMessage)message).getFileName();
		File tempFile = ((FileMessage)message).getFile();

		String dlDirectory = CMHelper.getInstance().getCMDownloadsDir();
		File savedFile = new File(dlDirectory+fileName);
		
		//copy temp file to chatman dl directory
		try{
			
			File saveDir = new File(dlDirectory);
			if(!saveDir.isDirectory()){
				CMHelper.getInstance().log("download dir doesn't exist. creating download dir");
				saveDir.mkdirs();
				CMHelper.getInstance().log("download dir created successfully");
			}
			
			CMHelper.getInstance().log("copying received file from " + tempFile.getAbsolutePath() + " to " + savedFile.getAbsolutePath());
			Files.copy(tempFile.toPath(), savedFile.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
			CMHelper.getInstance().log("file copied");
			
			message = FileMessage.getNew(direction, sender, fileName, savedFile, senderTheme, time);
			
		}catch(IOException e){
			CMHelper.getInstance().log("copying file from tmp folder to download direcoty failed");
			message = FileMessage.getNew(direction, "Error", "File receive failed", new File(""), senderTheme, time);
		}
		
	}
	
	private void processShutdown(){

		//start shutdown process
		try{
			
			CMHelper.getInstance().log("remote shutdown message received");
			CMHelper.getInstance().localShutdown();
			
			//show cancell dialog
			(new CmdInvokeLater(new CmdConfirmDialog(() -> {
				try {
					//if user chooses cancel shutdown
					CMHelper.getInstance().log("abort shutdown requested by user");
					CMHelper.getInstance().abortLocalShutdown();
					//tell the user abort was successfull
					CMHelper.getInstance().log("shutdown aborted successfully");
					ChatFrame.getInstance().message(CMHelper.getInstance().getStr("shutdown-abort-success"));	// we don't need invokelater because we're already in invokelater
					//tell the other computer we have aborted
					String info = "[INFO: REMOTE SHUTDOWN ABORTED BY USER]";
					TextMessage msg = TextMessage.getNewOutgoing(info);
					ChatFrame.getInstance().getChatmanInstance().sendMessage(msg);
				}catch(IOException e){
					CMHelper.getInstance().log("failed to abort local shutdown");
					//tell the user abort failed. we don't tell the other computer because it's not necessary
					(new CmdShowError(CMHelper.getInstance().getStr("shutdown-abort-fail"))).execute();  // we don't need invokelater because we're already in invokelater
				}
			}, CMHelper.getInstance().getStr("local_shutdown_message"), CMHelper.getInstance().getStr("local_shutdown_title")))).execute();

		}catch(Exception e){
			CMHelper.getInstance().log("shutdown failed");
			//tell the user shutdown has failed
			(new CmdInvokeLater(new CmdShowError(CMHelper.getInstance().getStr("shutdown-fail")))).execute();
			//tell the other computer our shutdown has failed
			String error = "[ERROR: SHUTDOWN FAILED]";
			TextMessage msg = TextMessage.getNewOutgoing(error);
			ChatFrame.getInstance().getChatmanInstance().sendMessage(msg);
		}

	}
	
	private void processAbortShutdown(){
		
		String info;
		CMHelper.getInstance().log("remote-abort-shutdown received");
		try{
			CMHelper.getInstance().abortLocalShutdown();
			CMHelper.getInstance().log("shutdown aborted successfully");
			info = "[SHUTDOWN ABORTED SUCCESSFULLY]";
		}catch(IOException e){
			CMHelper.getInstance().log("abort failed");
			info = "[ERROR: COULD NOT ABORT THE SHUTDOWN]";
		}
		//tell the other computer if abort was successfull
		TextMessage msg = TextMessage.getNewOutgoing(info);
		ChatFrame.getInstance().getChatmanInstance().sendMessage(msg);
		
	}
	
	private void processPing(){
		CMHelper.getInstance().log("ping received");
	}
	
	private void processShowGUI(){
		ChatFrame.getInstance().showWindow();
	}
	
	private void processThemeFile(){
		
		try{
			
			String themeDataBase64 = ((ThemeFileMessage)message).getThemeDataBase64();
			String themeName = ((ThemeFileMessage)message).getThemeName();
			File themeFileToSave = new File(
				CMConfig.getInstance().get("themes-dir", CMConfig.DEFAULT_THEMES_DIR) + "\\" + themeName
			);

			byte[] themeFileData = Base64.getDecoder().decode(themeDataBase64);
			CMHelper.getInstance().createFile(themeFileToSave, themeFileData);

			CMTheme peerTheme = new CMTheme(themeFileToSave.getAbsolutePath());
			ChatFrame.getInstance().setPeerTheme(peerTheme);
			ChatFrame.getInstance().showNewMessagePopup();
			
		}catch(Exception e){
			CMHelper.getInstance().log("failed to receive peer theme: " + e.getMessage());
			ChatFrame.getInstance().setPeerTheme(CMTheme.getDefaultTheme());
		}
	}
	
	private void processRequestThemeFile(){
		String themeDataBase64 = ChatFrame.getInstance().getCurrentTheme().getDataBase64();
		String themeName =ChatFrame.getInstance().getCurrentTheme().getFileName();
		ThemeFileMessage message = ThemeFileMessage.getNewOutgoing(themeName, themeDataBase64);
		OutgoingMsgHandler handler = new OutgoingMsgHandler(message);
		handler.send();
	}
	
}

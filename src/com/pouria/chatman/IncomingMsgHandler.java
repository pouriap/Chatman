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

import com.pouria.chatman.classes.CmdConfirmDialog;
import com.pouria.chatman.classes.CmdInvokeLater;
import com.pouria.chatman.classes.CmdShowError;
import com.pouria.chatman.gui.CMTheme;
import com.pouria.chatman.gui.ChatFrame;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Base64;
import javax.swing.JFileChooser;


/**
 *
 * @author pouriap
 */
public class IncomingMsgHandler {
	
	private final CMMessage message;
	
	public IncomingMsgHandler(CMMessage message){
		this.message = message;
	}
	
	public void handle(){
		
		int messageType = message.getType();
		switch(messageType){
			
			case CMMessage.TYPE_BADMESSAGE:
				processBadMessage();
				break;
				
			case CMMessage.TYPE_TEXT:
				processTextMessage();
				break;
				
			case CMMessage.TYPE_FILE:
				processFileMessage();
				break;
			
			case CMMessage.TYPE_SHUTDOWN:
				processShutdown();
				break;
				
			case CMMessage.TYPE_ABORT_SHUTDOWN:
				processAbortShutdown();
				break;
				
			case CMMessage.TYPE_PING:
				processPing();
				break;
				
			case CMMessage.TYPE_SHOWGUI:
				processShowGUI();
				break;
			
			case CMMessage.TYPE_PEER_THEME_FILE:
				processThemeFile();
				break;
				
			case CMMessage.TYPE_REQUEST_THEME_FILE:
				processRequestThemeFile();
				break; 
				
			default:
				break;
		}
		
	}
	
	private void processBadMessage(){
		CMHelper.getInstance().log("bad message received: " + message.getContent());
	}
	
	private void processTextMessage(){
	}
	
	private void processFileMessage(){
		CMHelper.getInstance().log("file message received");
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
				CMHelper.getInstance().log("download dir doesn't exist. creating download dir");
				saveDir.mkdir();
				CMHelper.getInstance().log("download dir created successfully");
			}
			CMHelper.getInstance().log("copying received file from " + srcFile.getAbsolutePath() + " to " + dstFile.getAbsolutePath());
			Files.copy(srcFile.toPath(), dstFile.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
			CMHelper.getInstance().log("file copied");
			//set file path (=content) to the one saved in Chatman Downloads
			message.setContent(dstFile.getAbsolutePath());
		}catch(IOException e){
			CMHelper.getInstance().log("copying file from tmp folder to download direcoty failed");
			String content = CMHelper.getInstance().getStr("file-receive-failed");
			message.setContent("ERROR: " + content);
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
					CMMessage message1 = new CMMessage(CMMessage.TYPE_TEXT, info);
					ChatFrame.getInstance().getChatmanInstance().sendMessage(message1);
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
			CMMessage msg = new CMMessage(CMMessage.TYPE_TEXT, error);
			ChatFrame.getInstance().getChatmanInstance().sendMessage(msg);
		}

	}
	
	private void processAbortShutdown(){
		
		String msgText;
		CMHelper.getInstance().log("remote-abort-shutdown received");
		try{
			CMHelper.getInstance().abortLocalShutdown();
			CMHelper.getInstance().log("shutdown aborted successfully");
			msgText = "[SHUTDOWN ABORTED SUCCESSFULLY]";
		}catch(IOException e){
			CMHelper.getInstance().log("abort failed");
			msgText = "[ERROR: COULD NOT ABORT THE SHUTDOWN]";
		}
		//tell the other computer if abort was successfull
		CMMessage msg = new CMMessage(CMMessage.TYPE_TEXT, msgText);
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
			String base64ThemeFile = message.getContent();
			String themeName = message.getSenderTheme();
			File themeFileToSave = new File(
				CMConfig.getInstance().get("themes-dir", CMConfig.DEFAULT_THEMES_DIR) + "\\" + themeName
			);

			byte[] themeFileData = Base64.getDecoder().decode(base64ThemeFile);
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
		String themeData = ChatFrame.getInstance().getCurrentTheme().getDataBase64();
		CMMessage themeMessage = new CMMessage(CMMessage.TYPE_PEER_THEME_FILE, themeData);
		OutgoingMsgHandler handler = new OutgoingMsgHandler(themeMessage);
		handler.handle();
	}
	
}

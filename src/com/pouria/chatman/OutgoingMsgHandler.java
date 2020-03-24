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

import com.pouria.chatman.connection.ChatmanClient;
import com.pouria.chatman.enums.CMType;
import com.pouria.chatman.gui.ChatFrame;
import com.pouria.chatman.messages.*;

import java.io.File;

/**
 *
 * @author pouriap
 */
public class OutgoingMsgHandler {
	
	private final CMMessage message;
	private final ChatmanClient client = ChatFrame.getInstance().getChatmanInstance().getClient();
	
	public OutgoingMsgHandler(CMMessage message){
		this.message = message;
	}
	
	public void handle(){

		boolean success;
		CMType messageType = message.getType();
		
		switch(messageType){
			
			case FILE:
				success = sendFileMessage();
				break;
				
			case SHOWGUI:
				success = sendShowGUIMessage();
				break;
				
			default:
				success = sendTextMessage();
				break;
				
		}

		CMMessage.Status status = (success)? CMMessage.Status.SENT : CMMessage.Status.SENDFAIL;
		message.setStatus(status);
		
		if(message.getType() == CMType.PING){
			if(success){
				CMHelper.getInstance().log("ping sent successfully");
			}
			else{
				CMHelper.getInstance().log("ping send failed");
			}
		}
		
	}

	private boolean sendTextMessage(){
		String text = message.getAsJSONString();
		return client.sendText(text);
	}
	
	private boolean sendFileMessage(){
		File file = ((FileMessage)message).getFile();
		String metadata = message.getAsJSONString();
		return client.sendFile(file, metadata);
	}
	
	private boolean sendShowGUIMessage(){
		client.setServer("127.0.0.1");
		String text = message.getAsJSONString();
		return client.sendText(text);
	}

	public static TextMessage buildTextMessage(String content){
		CMMessage.Direction direction = CMMessage.Direction.OUT;
		String sender = ChatFrame.getInstance().getCurrentTheme().getUsername();
		String senderTheme = ChatFrame.getInstance().getCurrentTheme().getFileName();
		long time = System.currentTimeMillis();
		return new TextMessage(direction, sender, content, senderTheme, time);
	}

	public static FileMessage buildFileMessage(File file){
		CMMessage.Direction direction = CMMessage.Direction.OUT;
		String sender = ChatFrame.getInstance().getCurrentTheme().getUsername();
		String fileName = file.getName();
		String senderTheme = ChatFrame.getInstance().getCurrentTheme().getFileName();
		long time = System.currentTimeMillis();
		return new FileMessage(direction, sender, fileName, file, senderTheme, time);
	}

	public static PingMessage buildPingMessage(){
		String senderTheme = ChatFrame.getInstance().getCurrentTheme().getFileName();
		return new PingMessage(senderTheme);
	}

	public static ShutdownMessage buildShutDownMessage(){
		CMMessage.Direction direction = CMMessage.Direction.OUT;
		String sender = ChatFrame.getInstance().getCurrentTheme().getUsername();
		String senderTheme = ChatFrame.getInstance().getCurrentTheme().getFileName();
		long time = System.currentTimeMillis();
		return new ShutdownMessage(direction, sender, senderTheme, time);
	}

	public static AbortShutdownMessage buildAbortShutdownMessage(){
		CMMessage.Direction direction = CMMessage.Direction.OUT;
		String sender = ChatFrame.getInstance().getCurrentTheme().getUsername();
		String senderTheme = ChatFrame.getInstance().getCurrentTheme().getFileName();
		long time = System.currentTimeMillis();
		return new AbortShutdownMessage(direction, sender, senderTheme, time);
	}

}

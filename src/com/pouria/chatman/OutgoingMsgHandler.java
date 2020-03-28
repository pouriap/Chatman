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
import com.pouria.chatman.messages.CMMessage;
import com.pouria.chatman.messages.FileMessage;

import java.io.File;
import java.util.Objects;


/**
 * takes a {@link CMMessage} as argument and sends it
 * @author pouriap
 */
public class OutgoingMsgHandler {
	
	private final CMMessage message;
	private final ChatmanClient client;
	
	public OutgoingMsgHandler(CMMessage message){
		this.message = message;
		this.client = ChatFrame.getInstance().getChatmanInstance().getClient();
		Objects.requireNonNull(client);
	}

	public OutgoingMsgHandler(CMMessage message, ChatmanClient client){
		this.message = message;
		this.client = client;
		Objects.requireNonNull(client);
	}

	/**
	 * sends the message synchonously<br>
	 * when the message is sent, the message's {@link CMMessage#onSend(boolean)} method is invoked<br>
	 * this function is blocking
	 */
	public void send(){

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

		message.onSend(success);

	}

	/**
	 * Sends the message asynchronously
	 */
	public void sendAsync(){
		(new Thread(this::send)).start();
	}

	/**
	 * sends a text message using the underlying ChatmanClient
	 * @return whether send was successful
	 */
	private boolean sendTextMessage(){
		String text = message.getAsJSONString();
		return client.sendText(text);
	}

	/**
	 * sends a file message using the underlying ChatmanClient
	 * @return whether send was successful
	 */
	private boolean sendFileMessage(){
		File file = ((FileMessage)message).getFile();
		String metadata = message.getAsJSONString();
		return client.sendFile(file, metadata);
	}

	/**
	 * sends a showGUI message using the underlying ChatmanClient
	 * @return whether send was successful
	 */
	private boolean sendShowGUIMessage(){
		client.setServer("127.0.0.1");
		String text = message.getAsJSONString();
		return client.sendText(text);
	}

}

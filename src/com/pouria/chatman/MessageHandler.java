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

import com.pouria.chatman.classes.CommandAddToConversation;
import com.pouria.chatman.classes.CommandInvokeLater;
import com.pouria.chatman.gui.ChatFrame;

/**
 *
 * @author pouriap
 */
public class MessageHandler {
	
	private final int direction;
	
	public MessageHandler(int direction) {
		this.direction = direction;
	}
	
	public void handle(ChatmanMessage message){
		
		if(direction == ChatmanMessage.DIR_IN){
			IncomingMessageHandler handler = new IncomingMessageHandler(message);
			handler.handle();
		}
		else{
			OutgoingMessageHandler handler = new OutgoingMessageHandler(message);
			handler.handle();
		}
		
		//add to history 
		if(
				message.getType() != ChatmanMessage.TYPE_PING
				&& message.getType() != ChatmanMessage.TYPE_SHOWGUI
				&& message.getStatus()== ChatmanMessage.STATUS_SENT
		){
			//TODO: can we get instance better?
			ChatFrame.getInstance().getChatmanInstance().addToAllMessages(message);
		}
		
		//add to gui
		//synchronous bashe baraye inke isDisplayed dar akhar set mishe va dar
		//sendQueue az isDisplayed estefasde mikonim
		(new CommandInvokeLater(new CommandAddToConversation(message), true)).execute();

		
	}
	
}

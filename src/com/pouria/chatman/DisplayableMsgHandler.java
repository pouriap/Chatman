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

import com.pouria.chatman.classes.CmdInvokeLater;
import com.pouria.chatman.classes.CmdShowMessage;
import com.pouria.chatman.gui.ChatFrame;

/**
 *
 * @author pouriap
 */
public class DisplayableMsgHandler {
	
	private final CMMessage.Direction direction;
	
	public DisplayableMsgHandler(CMMessage.Direction direction) {
		this.direction = direction;
	}
	
	public void handle(CMMessage message){
		
		if(direction == CMMessage.Direction.IN){
			IncomingMsgHandler handler = new IncomingMsgHandler(message);
			handler.handle();
		}
		else{
			OutgoingMsgHandler handler = new OutgoingMsgHandler(message);
			handler.handle();
		}
		
		//make sure only messages with displayable content get displayed
		if(message.getDisplayableContent().isEmpty()){
			return;
		}
		
		//add to all messages
		ChatFrame.getInstance().getChatmanInstance().addToAllMessages(message);
		
		//add to gui
		(new CmdInvokeLater(new CmdShowMessage(message))).execute();

	}
	
}

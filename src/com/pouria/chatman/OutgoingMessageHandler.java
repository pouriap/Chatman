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

import com.pouria.chatman.classes.ChatmanClient;
import com.pouria.chatman.gui.ChatFrame;

/**
 *
 * @author pouriap
 */
public class OutgoingMessageHandler {
	
	private final CMMessage message;
	private final ChatmanClient client = ChatFrame.getInstance().getChatmanInstance().getClient();
	
	public OutgoingMessageHandler(CMMessage message){
		this.message = message;
	}
	
	public void handle(){
		boolean success = client.send(message);
		message.setIsOurMessage(true);
		int status = (success)? CMMessage.STATUS_SENT : CMMessage.STATUS_SENDFAIL;
		message.setStatus(status);
	}
	
}

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

import com.pouria.chatman.commands.CmdInvokeLater;
import com.pouria.chatman.commands.CmdShowMessage;
import com.pouria.chatman.gui.ChatFrame;
import com.pouria.chatman.messages.DisplayableMessage;

/**
 *
 * @author pouriap
 */
public class messageDisplayer {
	
	private final DisplayableMessage message;
	
	public messageDisplayer(DisplayableMessage message) {
		this.message = message;
	}
	
	public void display(){

		//add to all messages
		ChatFrame.getInstance().getChatmanInstance().addToAllDisplayableMessages(message);
		
		//add to gui
		(new CmdInvokeLater(new CmdShowMessage(message))).execute();

	}
	
}

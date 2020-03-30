/*
 * Copyright (c) 2020. Pouria Pirhadi
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.pouria.chatman;

import com.pouria.chatman.messages.CMMessage;


/**
 *
 * @author pouriap
 */
public class IncomingMsgHandler {
	
	private final CMMessage message;
	
	public IncomingMsgHandler(CMMessage message){
		this.message = message;
	}
	
	public void receive(){
		message.onReceive();
	}

}

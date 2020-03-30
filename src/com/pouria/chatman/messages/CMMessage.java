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

package com.pouria.chatman.messages;

import com.pouria.chatman.enums.CMType;

public abstract class CMMessage {

	private Status status = Status.NOTSENT;
	private final Direction direction;

	public enum Status{
		NOTSENT, SENT, SENDFAIL;
	}

	public enum Direction{
		IN, OUT, UKNOWN;
	}

	public CMMessage(Direction direction){
		this.direction = direction;
	}

	abstract public CMType getType();
	abstract public String getAsJSONString();
	abstract protected void doOnReceive();
	abstract protected void doOnSend();

	public void onReceive(){
		//things to do for all messages
		doOnReceive();
	}

	public void onSend(boolean success){
		//things to do for all messages
		this.status = (success)? CMMessage.Status.SENT : CMMessage.Status.SENDFAIL;
		doOnSend();
	}

	public Status getStatus(){
		return this.status;
	}

	public Direction getDirection() {
		return direction;
	}
}

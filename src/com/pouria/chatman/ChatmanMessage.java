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

import com.google.gson.Gson;

/**
 *
 * @author pouriap
 */
public class ChatmanMessage {

	private int type;
	private String content;
	private String sender;
	private int time;
	
	public static final int TYPE_BADMESSAGE = 0;
	public static final int TYPE_TEXT = 1;
	public static final int TYPE_SHUTDOWN = 2;
	public static final int TYPE_ABORT_SHUTDOWN = 3;
	public static final int TYPE_FILE = 4;
	public static final int TYPE_SHOWGUI = 5;
	
	public ChatmanMessage(int type, String content, String sender, int time){
		this.type = type;
		this.content = content;
		this.sender = sender;
		this.time = time;
	}
	
	public ChatmanMessage(int type, String content, String sender){
		this.type = type;
		this.content = content;
		this.sender = sender;
		this.time = Helper.getInstance().getTime();
	}
	
	public String getAsJsonString(){
		Gson g = new Gson();
		return g.toJson(this);
	}
	
	public int getType(){
		return this.type;
	}
	
	public String getContent(){
		return this.content;
	}
	
	public String getSender(){
		return this.sender;
	}
	
	public int getTime(){
		return this.time;
	}
	
}

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

import com.pouria.chatman.gui.ChatFrame;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import org.json.JSONObject;

/**
 *
 * @author pouriap
 */
public class CMMessage {

	private final int type;
	private final String content;
	private final String sender;
	private final String senderTheme;
	private final long time;
	private final HashMap<String, String> miscData  = new HashMap<>();
	private int status = STATUS_NOTSENT;
	private int direction = DIR_UNKNOWN;
	private boolean isSaved = false;
	
	public static final String COLOR_FAILED = "#f73900";

	public static final int TYPE_BADMESSAGE = 0;
	public static final int TYPE_TEXT = 1;
	public static final int TYPE_SHUTDOWN = 2;
	public static final int TYPE_ABORT_SHUTDOWN = 3;
	public static final int TYPE_FILE = 4;
	public static final int TYPE_SHOWGUI = 5;
	public static final int TYPE_PING = 6;
	public static final int TYPE_PEER_THEME_FILE = 7;
	public static final int TYPE_REQUEST_THEME_FILE = 8;
	
	public static final int DIR_UNKNOWN = -1;
	public static final int DIR_IN = 0;
	public static final int DIR_OUT = 1;
	
	public static final int STATUS_NOTSENT = 0;
	public static final int STATUS_SENT = 1;
	public static final int STATUS_SENDFAIL = 2;
	
	//this is for outgoing messsages
	public CMMessage(int type, String content){
		this.type = type;
		this.content = content;
		this.sender = ChatFrame.getInstance().getUserName();
		this.senderTheme = ChatFrame.getInstance().getCurrentTheme().getFileName();
		this.time = CMHelper.getInstance().getTime();
	}

	public CMMessage(int type, String content, String sender, String senderTheme, long time){
		this.type = type;
		this.content = content;
		this.sender = sender;
		this.senderTheme = senderTheme;
		this.time = time;
	}
		
	public String getAsJsonString(){
		JSONObject json = new JSONObject();
		json.put("type", this.type);
		json.put("content", this.content);
		json.put("sender", this.sender);
		json.put("sender_theme", this.senderTheme);
		json.put("time", this.time);
		return json.toString();
	}
	
	public int getType(){
		return this.type;
	}
	
	public String getContent(){
		return this.content;
	}
	
	//formats content for being displayed in textAreaIncoming
	public String getDisplayableContent(){
		
		String t = content;
		Date d = new Date(time);
		SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
        String timeTxt = dateFormat.format(d);
		
		if(this.type == TYPE_TEXT){
			//parse web links 
			t = t.replaceAll("((http|https)://[^\\s]*)\\s?", "<a style='color:#dee3e9;font-weight:bold;' href='$1'><u>$1</u></a> ");
			//parse emoticons
			String url = getClass().getResource("/resources/emoticons_large/").toString();
			t = t.replaceAll("src=\"[^\"]*emoticons_large\\/([^\"]*\\.gif)\"", "src=\"" + url + "$1\"");
		}
		else if(this.type == TYPE_FILE){
			File file = new File(this.getMiscData("file_path"));
			String path = file.getAbsolutePath();
			String name = file.getName();
			//add file link
			t = "<a style='color:#dee3e9;font-weight:bold;' href='file://"+path+"'><u>"+name+"</u></a>";
		}
		else if(this.type == TYPE_SHUTDOWN){
			t = "[SHUTDOWN COMMAND]";
		}
		else if(this.type == TYPE_BADMESSAGE){
			t = "BAD MESSAGE: " + content;
		}
		else if(this.type == TYPE_ABORT_SHUTDOWN){
			t = "[ABORT SHUTDOWN COMMAND]";
		}
		else{
			//other message types don't have a display
			return "";
		}

		String textAlign = "left";
		String you = "You";
		String color = (this.status==STATUS_SENDFAIL)? COLOR_FAILED : ChatFrame.getInstance().getTextColor();
		String senderName = (isOurMessage())? you : sender;
		if(this.type == TYPE_FILE){
			senderName = (isOurMessage())? "File Sent: " : "File Received: ";
		}
		
		//each message is a div
		t = "<div style='text-align:"+textAlign+";padding:5px;'><span class='time'>["+timeTxt+"]  |  </span><b style='font-size:14px;color:"+color+"'>" + senderName + ":</b> " + t + "</div>";
		
        return t;
		
	}

	public void setDirection(int dir){
		this.direction = dir;
	}
	
	public boolean isOurMessage(){
		return this.direction == DIR_OUT;
	}
	
	public long getTime(){
		return this.time;
	}
	
	public int getStatus(){
		return this.status;
	}
	
	public void setStatus(int status){
		this.status = status;
	}
	
	public boolean isSaved() {
		return isSaved;
	}

	public void setIsSaved(boolean isSaved) {
		this.isSaved = isSaved;
	}

	public String getSenderTheme() {
		return senderTheme;
	}
	
	public void putMiscData(String key, String value){
		miscData.put(key, value);
	}
	
	public String getMiscData(String key){
		String value = miscData.get(key);
		return (value != null)? value : "";
	}
	
}

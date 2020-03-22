/*
 * Copyright (C) 2020 Pouria
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
package com.pouria.chatman.connection;

import com.pouria.chatman.CMHelper;
import com.pouria.chatman.CMMessage;
import static com.pouria.chatman.CMMessage.TYPE_BADMESSAGE;
import com.pouria.chatman.gui.ChatFrame;
import io.undertow.server.handlers.form.FormData;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Pouria
 */
public class CMFormDataParser {
	
	private final FormData formData;
	
	public CMFormDataParser(FormData formData) throws Exception{
		this.formData = formData;
	}
	
	public CMMessage parseAsCMMessage(){
		
		int type;
		String content;
		String sender;
		String senderTheme;
		long time;
		
		try{
			//if normal message
			if(formData.contains("message")){
				FormData.FormValue messageValue = formData.get("message").getFirst();
				String message = messageValue.getValue();
				//throws exception if JSON is curropt
				JSONObject json = new JSONObject(message);
				type = json.getInt("type");
				content = json.getString("content");
				sender = json.getString("sender");
				//for backwards compatibility if the message doesn't have a sender_theme
				//we set our own theme as sender theme 
				try{
					senderTheme = json.getString("sender_theme");
				}catch(Exception e){
					senderTheme = ChatFrame.getInstance().getCurrentTheme().getFileName();
				}
				time = json.getLong("time");
			}
			//if file message
			else if(formData.contains("file")){
				FormData.FormValue formFile = formData.get("file").getFirst();
				FormData.FormValue formMetadata = formData.get("metadata").getFirst();
				if(formFile.isFileItem()){
					String tmpFilePath = formFile.getFileItem().getFile().toAbsolutePath().toString();
					String message = formMetadata.getValue();
					//throws exception if JSON is curropt
					JSONObject json = new JSONObject(message);
					String fileName = json.getString("content");
					type = json.getInt("type");
					sender = json.getString("sender");
					//for backwards compatibility if the message doesn't have a sender_theme
					//we set our own theme as sender theme 
					try{
						senderTheme = json.getString("sender_theme");
					}catch(Exception e){
						senderTheme = ChatFrame.getInstance().getCurrentTheme().getFileName();
					}
					time = json.getLong("time");
					//we have to store file name somewhere
					content = tmpFilePath + "**" + fileName;
				}
				else{
					throw new Exception("bad file message");
				}
			}
			//if bad message
			else{
				throw new Exception();
			}

		}catch(JSONException e){
			//create a 'bad message' instance as our message because the original one is lost
			type = TYPE_BADMESSAGE;
			content = "bad json syntax";
			sender = "unknown";
			senderTheme = "default";
			time = CMHelper.getInstance().getTime();
			
		}catch(Exception e){
			//create a 'bad message' instance as our message because the original one is lost
			type = TYPE_BADMESSAGE;
			content = "bad message";
			sender = "unknown";
			senderTheme = "default";
			time = CMHelper.getInstance().getTime();	
		}
		
		CMMessage message = new CMMessage(type, content, sender, senderTheme, time);
		return message;
		
	}
	
	
}

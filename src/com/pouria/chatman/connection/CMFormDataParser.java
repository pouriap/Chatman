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
import com.pouria.chatman.enums.CMType;
import com.pouria.chatman.messages.*;
import io.undertow.server.handlers.form.FormData;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.function.Consumer;

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
		
		CMMessage message;
		
		try{
			//if normal message
			if(formData.contains("message")){

				FormData.FormValue messageValue = formData.getFirst("message");

				String jsonString = messageValue.getValue();
				JSONObject json = new JSONObject(jsonString);
				CMType type = CMType.valueOf(json.getString("type"));

				switch(type){
					case TEXT:
						message = getTextMessage(json);
						break;
					case SHUTDOWN:
						message = getShutdownMessage(json);
						break;
					case ABORT_SHUTDOWN:
						message = getAbortShutdownMessage(json);
						break;
					case PING:
						message = getPingMessage(json);
						break;
					case SHOWGUI:
						message = getShowguiMessage(json);
						break;
					case REQUEST_THEME_FILE:
						message = getRequestThemeMessage(json);
						break;
					case THEME_FILE:
						message = getThemeFileMessage(json);
						break;
					default: 
						message = new BadMessage("unknown message type");
						break;
				}

			}
			//if file message
			else if(formData.contains("file")){
				FormData.FormValue formFile = formData.getFirst("file");
				FormData.FormValue formMetadata = formData.getFirst("metadata");
				if(formFile.isFileItem()){
					File tempFile = formFile.getFileItem().getFile().toFile();
					String jsonString = formMetadata.getValue();
					JSONObject json = new JSONObject(jsonString);
					String sender = json.getString("sender");
					String fileName = json.getString("file_name");
					String senderTheme = json.getString("sender_theme");
					long time = json.getLong("time");
					message = new FileMessage(CMMessage.Direction.IN, sender, fileName, tempFile, senderTheme, time);
				}
				else{
					throw new Exception("bad file item");
				}
			}
			//if bad message
			else{
				throw new Exception("bad POST parameters");
			}
		}catch(Exception e){
			
			StringBuilder b = new StringBuilder();
			formData.iterator().forEachRemaining(new Consumer<String>() {
				@Override
				public void accept(String t) {
					//don't get file data
					if(formData.getFirst(t).isFileItem()){
						return;
					}
					b.append(t);
					b.append(" : ");
					b.append(formData.getFirst(t).getValue());
				}
			});
			String rawData = b.toString();
			
			CMHelper.getInstance().log("bad message recived:\n");
			CMHelper.getInstance().log("exception message: " + e.getMessage() + "\nraw data: " + rawData);
			
			message = new BadMessage("bad message");
			
		}

		return message;
		
	}

	private TextMessage getTextMessage(JSONObject json) throws JSONException{
		String content = json.getString("content");
		String sender = json.getString("sender");
		String senderTheme = json.getString("sender_theme");
		long time = json.getLong("time");
		return new TextMessage(CMMessage.Direction.IN, sender, content, senderTheme, time);
	}

	private ShutdownMessage getShutdownMessage(JSONObject json) throws JSONException{
		String sender = json.getString("sender");
		String senderTheme = json.getString("sender_theme");
		long time = json.getLong("time");
		return new ShutdownMessage(CMMessage.Direction.IN, sender, senderTheme, time);
	}

	private AbortShutdownMessage getAbortShutdownMessage(JSONObject json) throws JSONException{
		String sender = json.getString("sender");
		String senderTheme = json.getString("sender_theme");
		long time = json.getLong("time");
		return new AbortShutdownMessage(CMMessage.Direction.IN, sender, senderTheme, time);
	}

	private PingMessage getPingMessage(JSONObject json) throws JSONException{
		String senderTheme = json.getString("sender_theme");
		return new PingMessage(senderTheme);
	}

	private ShowGUIMessage getShowguiMessage(JSONObject json) throws JSONException{
		return new ShowGUIMessage();
	}

	private RequestThemeMessage getRequestThemeMessage(JSONObject json) throws JSONException{
		String themeName = json.getString("theme_name");
		return new RequestThemeMessage(themeName);
	}

	private ThemeFileMessage getThemeFileMessage(JSONObject json) throws JSONException{
		String themeName = json.getString("theme_name");
		String themeDataBase64 = json.getString("theme_data_base64");
		return new ThemeFileMessage(themeName, themeDataBase64);
	}
	
	
}

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
package com.pouria.chatman.connection;

import com.pouria.chatman.CMHelper;
import com.pouria.chatman.enums.CMType;
import com.pouria.chatman.messages.*;
import io.undertow.server.handlers.form.FormData;
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
						message = TextMessage.getNewIncoming(json);
						break;
					case SHUTDOWN:
						message = ShutdownMessage.getNewIncoming(json);
						break;
					case ABORT_SHUTDOWN:
						message = AbortShutdownMessage.getNewIncoming(json);
						break;
					case PING:
						message = PingMessage.getNewIncoming(json);
						break;
					case SHOWGUI:
						message = ShowGUIMessage.getNewIncoming();
						break;
					case REQUEST_THEME_FILE:
						message = RequestThemeMessage.getNewIncoming(json);
						break;
					case THEME_FILE:
						message = ThemeFileMessage.getNewIncoming(json);
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
					message = FileMessage.getNewIncoming(tempFile, json);
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

}

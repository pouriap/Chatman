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

import com.pouria.chatman.CMHelper;
import com.pouria.chatman.MessageDisplayer;
import com.pouria.chatman.enums.CMType;
import com.pouria.chatman.gui.ChatFrame;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class FileMessage extends DisplayableMessage {

	private final String fileName;
	private final File file;
	private final String sender;

	private FileMessage(Direction direction, String sender, String fileName, File file, String senderTheme, long time) {
		super(direction, sender, "", senderTheme, time);
		this.fileName = fileName;
		this.file = file;
		this.sender = sender;
	}

	public static FileMessage getNew(Direction direction, String sender, String fileName, File file, String senderTheme, long time) {
		return new FileMessage(direction, sender, fileName, file, senderTheme, time);
	}

	public static FileMessage getNewOutgoing(File file){
		CMMessage.Direction direction = CMMessage.Direction.OUT;
		String sender = ChatFrame.getInstance().getCurrentTheme().getUsername();
		String fileName = file.getName();
		String senderTheme = ChatFrame.getInstance().getCurrentTheme().getFileName();
		long time = System.currentTimeMillis();
		return new FileMessage(direction, sender, fileName, file, senderTheme, time);
	}

	public static FileMessage getNewIncoming(File file, JSONObject json) throws JSONException{
		String sender = json.getString("sender");
		String fileName = json.getString("file_name");
		String senderTheme = json.getString("sender_theme");
		long time = json.getLong("time");
		return new FileMessage(CMMessage.Direction.IN, sender, fileName, file, senderTheme, time);
	}

	@Override
	public CMType getType() {
		return CMType.FILE;
	}

	@Override
	public String getSender(){
		return (getDirection() == Direction.IN)? "File Received" : "File Sent";
	}

	@Override
	public String getDisplayableContent(){
		String path = "";
		String name = "no such file";
		if(file!=null && file.isFile()) {
			path = file.getAbsolutePath();
			name = fileName;
		}
		//add file link
		return "<a style='font-weight:bold;' href='file://" + path + "'><u>" + name + "</u></a>";
	}

	@Override
	public String getAsJSONString() {
		JSONObject json = new JSONObject();
		json.put("type", CMType.FILE);
		json.put("sender", sender);
		json.put("file_name", fileName);
		json.put("sender_theme", getSenderTheme());
		json.put("time", getTime());
		return json.toString();
	}

	public File getFile() {
		return file;
	}

	public String getFileName(){
		return fileName;
	}

	@Override
	public void doOnReceive(){

		CMHelper.getInstance().log("file message received");

		FileMessage messageToDisplay;
		String dlDirectory = CMHelper.getInstance().getCMDownloadsDir();
		File savedFile = new File(dlDirectory + fileName);

		//copy temp file to chatman dl directory
		try{

			File saveDir = new File(dlDirectory);
			if(!saveDir.isDirectory()){
				CMHelper.getInstance().log("download dir doesn't exist. creating download dir");
				saveDir.mkdirs();
				CMHelper.getInstance().log("download dir created successfully");
			}

			CMHelper.getInstance().log("copying received file from " + file.getAbsolutePath() + " to " + savedFile.getAbsolutePath());
			Files.copy(getFile().toPath(), savedFile.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
			CMHelper.getInstance().log("file copied");

			messageToDisplay = FileMessage.getNew(getDirection(), sender, fileName, savedFile, getSenderTheme(), getTime());

		}catch(IOException e){
			CMHelper.getInstance().log("copying file from tmp folder to download direcoty failed");
			messageToDisplay = FileMessage.getNew(getDirection(), "Error", "File receive failed", new File(""), getSenderTheme(), getTime());
		}

		MessageDisplayer displayer = new MessageDisplayer(messageToDisplay);
		displayer.display();

	}

}

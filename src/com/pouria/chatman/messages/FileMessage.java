package com.pouria.chatman.messages;

import com.pouria.chatman.enums.CMType;
import org.json.JSONObject;

import java.io.File;

public class FileMessage extends DisplayableMessage {

	private final String fileName;
	private final File file;
	private final String sender;

	public FileMessage(Direction direction, String sender, String fileName, File file, String senderTheme, long time) {
		super(direction, sender, "", senderTheme, time);
		this.fileName = fileName;
		this.file = file;
		this.sender = sender;
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

	public String getFileName() {
		return fileName;
	}

	public File getFile() {
		return file;
	}
}

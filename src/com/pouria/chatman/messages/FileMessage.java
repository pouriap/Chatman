package com.pouria.chatman.messages;

import com.pouria.chatman.enums.CMType;
import com.pouria.chatman.gui.ChatFrame;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

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

	public String getFileName() {
		return fileName;
	}

	public File getFile() {
		return file;
	}
}

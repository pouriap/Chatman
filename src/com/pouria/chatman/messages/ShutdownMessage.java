package com.pouria.chatman.messages;

import com.pouria.chatman.enums.CMType;
import com.pouria.chatman.gui.ChatFrame;
import org.json.JSONException;
import org.json.JSONObject;

public class ShutdownMessage extends DisplayableMessage {

	private final static String content = "[REMOTE SHUTDOWN]";

	private ShutdownMessage(Direction direction, String sender, String senderTheme, long time) {
		super(direction, sender, content, senderTheme, time);
	}

	public static ShutdownMessage getNew(Direction direction, String sender, String senderTheme, long time) {
		return new ShutdownMessage(direction, sender, senderTheme, time);
	}

	public static ShutdownMessage getNewOutgoing(){
		CMMessage.Direction direction = CMMessage.Direction.OUT;
		String sender = ChatFrame.getInstance().getCurrentTheme().getUsername();
		String senderTheme = ChatFrame.getInstance().getCurrentTheme().getFileName();
		long time = System.currentTimeMillis();
		return new ShutdownMessage(direction, sender, senderTheme, time);
	}

	public static ShutdownMessage getNewIncoming(JSONObject json) throws JSONException {
		String sender = json.getString("sender");
		String senderTheme = json.getString("sender_theme");
		long time = json.getLong("time");
		return new ShutdownMessage(CMMessage.Direction.IN, sender, senderTheme, time);
	}

	@Override
	public CMType getType() {
		return CMType.SHUTDOWN;
	}

	@Override
	public String getDisplayableContent() {
		return getContent();
	}

}

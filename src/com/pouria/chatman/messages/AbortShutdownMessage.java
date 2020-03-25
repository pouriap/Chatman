package com.pouria.chatman.messages;

import com.pouria.chatman.enums.CMType;
import com.pouria.chatman.gui.ChatFrame;
import org.json.JSONException;
import org.json.JSONObject;

public class AbortShutdownMessage extends DisplayableMessage {

	private final static String content = "[ABORT SHUTDOWN]";

	private AbortShutdownMessage(Direction direction, String sender, String senderTheme, long time) {
		super(direction, sender, content, senderTheme, time);
	}

	public static AbortShutdownMessage getNew(Direction direction, String sender, String senderTheme, long time) {
		return new AbortShutdownMessage(direction, sender, senderTheme, time);
	}

	public static AbortShutdownMessage getNewOutgoing(){
		CMMessage.Direction direction = CMMessage.Direction.OUT;
		String sender = ChatFrame.getInstance().getCurrentTheme().getUsername();
		String senderTheme = ChatFrame.getInstance().getCurrentTheme().getFileName();
		long time = System.currentTimeMillis();
		return new AbortShutdownMessage(direction, sender, senderTheme, time);
	}

	public static AbortShutdownMessage getNewIncoming(JSONObject json) throws JSONException{
		String sender = json.getString("sender");
		String senderTheme = json.getString("sender_theme");
		long time = json.getLong("time");
		return new AbortShutdownMessage(CMMessage.Direction.IN, sender, senderTheme, time);
	}

	@Override
	public CMType getType() {
		return CMType.ABORT_SHUTDOWN;
	}

	@Override
	public String getDisplayableContent() {
		return getContent();
	}
}

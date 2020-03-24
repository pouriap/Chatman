package com.pouria.chatman.messages;

import com.pouria.chatman.enums.CMType;

public class ShutdownMessage extends DisplayableMessage {

	private final static String content = "[REMOTE SHUTDOWN]";

	public ShutdownMessage(Direction direction, String sender, String senderTheme, long time) {
		super(direction, sender, content, senderTheme, time);
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

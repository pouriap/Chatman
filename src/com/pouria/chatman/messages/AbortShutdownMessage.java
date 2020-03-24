package com.pouria.chatman.messages;

import com.pouria.chatman.enums.CMType;

public class AbortShutdownMessage extends DisplayableMessage {

	private final static String content = "[ABORT SHUTDOWN]";

	public AbortShutdownMessage(Direction direction, String sender, String senderTheme, long time) {
		super(direction, sender, content, senderTheme, time);
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

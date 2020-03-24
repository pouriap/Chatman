package com.pouria.chatman.messages;

import com.pouria.chatman.CMConfig;
import com.pouria.chatman.enums.CMType;

public class BadMessage extends DisplayableMessage {

	public BadMessage(String content) {
		super(Direction.UKNOWN, "Bad Message", content, CMConfig.DEFAULT_THEME, System.currentTimeMillis());
	}

	@Override
	public CMType getType() {
		return CMType.BADMESSAGE;
	}

	@Override
	public String getDisplayableContent() {
		return getContent();
	}
}

package com.pouria.chatman.gui;

public enum CMCSS{

	RED("#f73900"), WHITE("#e0e0e0"), BLACK("#2b2b2b"),
	MESSAGE_DIV_PADDING("5px"),
	USERNAME_FONT_SIZE("14px"),
	CONVERSATION_FONT_FAMILY("Tahoma"),
	CONVERSATION_FONT_SIZE("12px"),
	MESSAGE_TIME_FONT_SIZE("11px"),
	;

	public final String val;

	CMCSS(String s) {
		this.val = s;
	}

}

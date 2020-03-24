package com.pouria.chatman.messages;

public abstract class HiddenMessage extends CMMessage {

	@Override
	public boolean isDisplayable(){
		return false;
	}

}

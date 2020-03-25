package com.pouria.chatman.messages;

public abstract class HiddenMessage extends CMMessage {


	public HiddenMessage(Direction direction) {
		super(direction);
	}

	@Override
	public boolean isDisplayable(){
		return false;
	}

}

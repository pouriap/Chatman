package com.pouria.chatman.messages;

import com.pouria.chatman.enums.CMType;

public abstract class CMMessage {

	private Status status = Status.NOTSENT;
	private final Direction direction;

	public enum Status{
		NOTSENT, SENT, SENDFAIL;
	}

	public enum Direction{
		IN, OUT, UKNOWN;
	}

	public CMMessage(Direction direction){
		this.direction = direction;
	}

	abstract public CMType getType();
	abstract public String getAsJSONString();
	abstract protected void doOnReceive();
	abstract protected void doOnSend();

	public void onReceive(){
		//things to do for all messages
		doOnReceive();
	}

	public void onSend(boolean success){
		//things to do for all messages
		this.status = (success)? CMMessage.Status.SENT : CMMessage.Status.SENDFAIL;
		doOnSend();
	}

	public Status getStatus(){
		return this.status;
	}

	public Direction getDirection() {
		return direction;
	}
}

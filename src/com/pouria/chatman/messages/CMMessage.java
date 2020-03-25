package com.pouria.chatman.messages;

import com.pouria.chatman.enums.CMType;

public abstract class CMMessage {

	private Status status = Status.NOTSENT;
	private Direction direction = Direction.UKNOWN;

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
	abstract public boolean isDisplayable();

	public Status getStatus(){
		return this.status;
	}

	public void setStatus(Status status){
		this.status = status;
	}

	public Direction getDirection() {
		return direction;
	}
}

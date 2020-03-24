package com.pouria.chatman.messages;

import com.pouria.chatman.enums.CMType;

public abstract class CMMessage {

	private Status status = Status.NOTSENT;

	public enum Status{
		NOTSENT, SENT, SENDFAIL;
	}

	public enum Direction{
		IN, OUT, UKNOWN;
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

}

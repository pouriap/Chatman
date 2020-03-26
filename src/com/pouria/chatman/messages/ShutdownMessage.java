package com.pouria.chatman.messages;

import com.pouria.chatman.CMHelper;
import com.pouria.chatman.commands.CmdConfirmDialog;
import com.pouria.chatman.commands.CmdInvokeLater;
import com.pouria.chatman.commands.CmdShowError;
import com.pouria.chatman.enums.CMType;
import com.pouria.chatman.gui.ChatFrame;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class ShutdownMessage extends DisplayableMessage {

	private final static String content = "[REMOTE SHUTDOWN]";

	private ShutdownMessage(Direction direction, String sender, String senderTheme, long time) {
		super(direction, sender, content, senderTheme, time);
	}

	public static ShutdownMessage getNew(Direction direction, String sender, String senderTheme, long time) {
		return new ShutdownMessage(direction, sender, senderTheme, time);
	}

	public static ShutdownMessage getNewOutgoing(){
		CMMessage.Direction direction = CMMessage.Direction.OUT;
		String sender = ChatFrame.getInstance().getCurrentTheme().getUsername();
		String senderTheme = ChatFrame.getInstance().getCurrentTheme().getFileName();
		long time = System.currentTimeMillis();
		return new ShutdownMessage(direction, sender, senderTheme, time);
	}

	public static ShutdownMessage getNewIncoming(JSONObject json) throws JSONException {
		String sender = json.getString("sender");
		String senderTheme = json.getString("sender_theme");
		long time = json.getLong("time");
		return new ShutdownMessage(CMMessage.Direction.IN, sender, senderTheme, time);
	}

	@Override
	public CMType getType() {
		return CMType.SHUTDOWN;
	}

	@Override
	public String getDisplayableContent() {
		return getContent();
	}

	@Override
	public void doOnReceive(){

		//start shutdown process
		try{

			CMHelper.getInstance().log("remote shutdown message received");
			CMHelper.getInstance().localShutdown();

			//show cancell dialog
			(new CmdInvokeLater(new CmdConfirmDialog(() -> {
				try {
					//if user chooses cancel shutdown
					CMHelper.getInstance().log("abort shutdown requested by user");
					CMHelper.getInstance().abortLocalShutdown();
					//tell the user abort was successfull
					CMHelper.getInstance().log("shutdown aborted successfully");
					ChatFrame.getInstance().message(CMHelper.getInstance().getStr("shutdown-abort-success"));	// we don't need invokelater because we're already in invokelater
					//tell the other computer we have aborted
					String info = "[INFO: REMOTE SHUTDOWN ABORTED BY USER]";
					TextMessage msg = TextMessage.getNewOutgoing(info);
					ChatFrame.getInstance().getChatmanInstance().sendMessage(msg);
				}catch(IOException e){
					CMHelper.getInstance().log("failed to abort local shutdown");
					//tell the user abort failed. we don't tell the other computer because it's not necessary
					(new CmdShowError(CMHelper.getInstance().getStr("shutdown-abort-fail"))).execute();  // we don't need invokelater because we're already in invokelater
				}
			}, CMHelper.getInstance().getStr("local_shutdown_message"), CMHelper.getInstance().getStr("local_shutdown_title")))).execute();

		}catch(Exception e){
			CMHelper.getInstance().log("shutdown failed");
			//tell the user shutdown has failed
			(new CmdInvokeLater(new CmdShowError(CMHelper.getInstance().getStr("shutdown-fail")))).execute();
			//tell the other computer our shutdown has failed
			String error = "[ERROR: SHUTDOWN FAILED]";
			TextMessage msg = TextMessage.getNewOutgoing(error);
			ChatFrame.getInstance().getChatmanInstance().sendMessage(msg);
		}

		super.doOnReceive();

	}

}

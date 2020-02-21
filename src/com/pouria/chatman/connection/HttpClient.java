/*
 * Copyright (C) 2020 pouriap
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.pouria.chatman.connection;

import com.github.kevinsawicki.HttpRequest;
import com.pouria.chatman.ChatmanMessage;
import com.pouria.chatman.Helper;
import com.pouria.chatman.IpConnector;
import com.pouria.chatman.IpScanner;
import com.pouria.chatman.classes.ChatmanClient;
import com.pouria.chatman.classes.PeerNotFoundException;
import com.pouria.chatman.gui.ChatFrame;
import com.pouria.chatman.gui.ChatmanConfig;

/**
 *
 * @author pouriap
 */
public class HttpClient implements ChatmanClient{
	
	private String serverIP = null;
	private boolean connectInProgress = false;

	@Override
	public void send(String message) throws PeerNotFoundException{
		
		if(!isServerFound()){
			throw new PeerNotFoundException("server not found");
		}
		
		int code = 0;
		try{
			String serverPort = ChatmanConfig.getInstance().get("server-port");
			String serverAddress = "http://" + serverIP + ":" + serverPort;
			HttpRequest req = new HttpRequest(serverAddress, "GET");
			req.getConnection().setConnectTimeout(300);
			code = req.get(serverAddress, true, "message", message).code();
			
		}catch(Exception e){
			e.printStackTrace();
			throw new PeerNotFoundException("request could not be sent: " + e.getMessage());
		}
		
		if(code != 200){
			throw new PeerNotFoundException("http request returned code: " + code);
		}
		
	}

	@Override
	public void send(ChatmanMessage message) throws PeerNotFoundException{
		send(message.getAsJsonString());
	}

	@Override
	public void connect() {
		
		if(connectInProgress){
			return;
		}
		
        ChatFrame.getInstance().setLabelStatus(Helper.getInstance().getStr("searching_network"));

		setConnectInProgress(true);		
		this.serverIP = null;
        int serverPort = Integer.valueOf(ChatmanConfig.getInstance().get("server-port"));
		Thread scanner;
        
        //if we have server's ip we don't scan the network
        if(ChatmanConfig.getInstance().isSet("server-ip")){
            String serverIp = ChatmanConfig.getInstance().get("server-ip");
            scanner = new Thread(new IpConnector(serverIp, serverPort, true));
            scanner.start();
        }
        else{
            String subnet = ChatmanConfig.getInstance().get("subnet-mask");
            scanner = new Thread(new IpScanner(subnet, serverPort));
            scanner.start();
        }	
	}

	@Override
	public synchronized void setServer(Object server) {
		this.serverIP = (String) server;
	}

	@Override
	public boolean isServerFound() {
		return (this.serverIP != null);
	}
	
	public void setConnectInProgress(boolean b){
		this.connectInProgress = b;
		if(connectInProgress){
			ChatFrame.getInstance().disableTextOutgoing();
		}
		else{
			ChatFrame.getInstance().enableTextOutgoing();
		}
	}
	
	public boolean isConnectInProgress(){
		return this.connectInProgress;
	}

}

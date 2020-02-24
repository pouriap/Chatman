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

import com.github.kevinsawicki.http.HttpRequest;
import com.pouria.chatman.ChatmanMessage;
import com.pouria.chatman.Helper;
import com.pouria.chatman.classes.ChatmanClient;
import com.pouria.chatman.classes.CommandClientConnect;
import com.pouria.chatman.classes.CommandConfirmDialog;
import com.pouria.chatman.classes.CommandInvokeLater;
import com.pouria.chatman.classes.CommandSetLabelStatus;
import com.pouria.chatman.classes.IpScannerCallback;
import com.pouria.chatman.classes.PeerNotFoundException;
import com.pouria.chatman.gui.ChatFrame;
import com.pouria.chatman.gui.ChatmanConfig;
import java.io.File;
import java.util.ArrayList;

/**
 *
 * @author pouriap
 */
public class HttpClient implements ChatmanClient{
	
	private String serverIP = null;
	private boolean connectInProgress = false;

	@Override
	public void send(ChatmanMessage message) throws PeerNotFoundException{
		
		if(serverIP == null){
			throw new PeerNotFoundException("server not found");
		}
		
		int messageType = message.getType();
		
		switch(messageType){
			case ChatmanMessage.TYPE_TEXT:
				sendTextMessage(message.getAsJsonString());
				break;
			case ChatmanMessage.TYPE_FILE:
				sendFileMessage(message);
				break;
			default:
				break;
		}
		
	}
	
	private void sendTextMessage(String message) throws PeerNotFoundException{

		int code = 0;
		try{
			String remotePort = ChatmanConfig.getInstance().get("server-port");
			String remoteAddress = "http://" + serverIP + ":" + remotePort;
			HttpRequest req = new HttpRequest(remoteAddress, "GET");
			code = req.get(remoteAddress, true, "message", message).connectTimeout(200).code();
		}catch(Exception e){
			throw new PeerNotFoundException("request could not be sent: " + e.getMessage());
		}
		
		if(code != 200){
			throw new PeerNotFoundException("http request returned code: " + code);
		}
		
	}
	
	private void sendFileMessage(ChatmanMessage message) throws PeerNotFoundException{
		//TODO: so much in common with above function
		int code = 0;
		try{
			String remotePort = ChatmanConfig.getInstance().get("server-port");
			//String remoteAddress = "http://" + serverIP + ":" + remotePort;
			String remoteAddress = "http://192.168.1.20:2222";
			String fileName = message.getSender();
			File file = new File(fileName);
			HttpRequest req = HttpRequest.post(remoteAddress);
			req.part("data", file);
			code = req.code();
		}catch(Exception e){
			throw new PeerNotFoundException("file could not be sent: " + e.getMessage());
		}
		
		if(code != 200){
			throw new PeerNotFoundException("http file upload returned code: " + code);
		}
	}

	@Override
	public void connect(){
		
		if(connectInProgress){
			return;
		}
		
        ChatFrame.getInstance().setLabelStatus(Helper.getInstance().getStr("searching_network"));

		setConnectInProgress(true);
		this.serverIP = null;
        int serverPort = Integer.valueOf(ChatmanConfig.getInstance().get("server-port"));
		String[] ipsToScan = getIpsToScan();
		Thread scanner;
		
		IpScannerCallback callback = new IpScannerCallback() {
			@Override
			public void call(ArrayList<String> foundIps) {
				//if server found
				if(!foundIps.isEmpty()){
					String ip = foundIps.get(0);
					setServer(ip);
					(new CommandInvokeLater(new CommandSetLabelStatus(Helper.getInstance().getStr("connection_with") + ip + Helper.getInstance().getStr("stablished")))).execute();
				}
				else{
					(new CommandInvokeLater(new CommandConfirmDialog(
							new CommandClientConnect(),
							Helper.getInstance().getStr("server_retry_confirm"),
							Helper.getInstance().getStr("server_not_found")
					))).execute();
				}
				//in either case
				setConnectInProgress(false);
			}
		};
		
		scanner = new Thread(new IpScanner(ipsToScan, serverPort, callback));
		scanner.start();
	}
	
	private String[] getIpsToScan(){
		String[] ipsToScan;
		//if we have server's ip we don't scan the network
        if(ChatmanConfig.getInstance().isSet("server-ip")){
            String serverIp = ChatmanConfig.getInstance().get("server-ip");
			ipsToScan = new String[]{serverIp};
        }
        else{
            String subnet = ChatmanConfig.getInstance().get("subnet-mask");
			int numHostsToScan = Integer.valueOf(ChatmanConfig.getInstance().get("num-hosts-to-scan"));
			ipsToScan = new String[numHostsToScan];
			for(int i=0; i<numHostsToScan; i++){
				String ip = subnet.replace("*", String.valueOf(i));
				ipsToScan[i] = ip;
			}
        }
		
		return ipsToScan;
	}

	@Override
	public synchronized void setServer(Object server) {
		this.serverIP = (String) server;
	}

	public void setConnectInProgress(boolean b){
		//TODO: textarea becomes enables even after search didn't find a server
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
	
	public boolean isServerFound(){
		return (serverIP != null);
	}

}

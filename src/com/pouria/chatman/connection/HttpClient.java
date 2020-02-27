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

import com.pouria.chatman.ChatmanMessage;
import com.pouria.chatman.Helper;
import com.pouria.chatman.classes.ChatmanClient;
import com.pouria.chatman.classes.CommandClientConnect;
import com.pouria.chatman.classes.CommandConfirmDialog;
import com.pouria.chatman.classes.CommandInvokeLater;
import com.pouria.chatman.classes.CommandSetLabelStatus;
import com.pouria.chatman.classes.IpScannerCallback;
import com.pouria.chatman.classes.SendCallback;
import com.pouria.chatman.gui.ChatFrame;
import com.pouria.chatman.ChatmanConfig;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicNameValuePair;

/**
 *
 * @author pouriap
 */
public class HttpClient implements ChatmanClient{
	
	private String serverIP = null;
	private boolean connectInProgress = false;
	private final RequestConfig configTimeout;
	private final int timeoutMillis = 300;
	
	public HttpClient(){
		configTimeout = RequestConfig.custom().setConnectTimeout(timeoutMillis, TimeUnit.MILLISECONDS)
			.setResponseTimeout(timeoutMillis, TimeUnit.MILLISECONDS) //TODO: file gives error because response takes long
			.setConnectionRequestTimeout(timeoutMillis, TimeUnit.MILLISECONDS)
			.build();
	}

	@Override
	public void send(ChatmanMessage message, SendCallback callback){
		
		if(serverIP == null){
			callback.call(false, "server not set, reconnecting...");
			connect();
			return;
		}
		
		int messageType = message.getType();
		
		switch(messageType){
			case ChatmanMessage.TYPE_TEXT:
				sendTextMessage(message, callback);
				break;
			case ChatmanMessage.TYPE_SHUTDOWN:
				sendTextMessage(message, callback);
				break;
			case ChatmanMessage.TYPE_ABORT_SHUTDOWN:
				sendTextMessage(message, callback);
				break;
			case ChatmanMessage.TYPE_FILE:
				sendFileMessage(message, callback);
				break;

			default:
				break;
		}
		
	}
	
	private void sendTextMessage(ChatmanMessage message, SendCallback callback){
		
		boolean success = false;
		String reason = "";
		String messageText = message.getAsJsonString();
		
		try{
			String remotePort = ChatmanConfig.getInstance().get("server-port", ChatmanConfig.DEFAULT_SERVER_PORT);
			String remoteAddress = "http://" + serverIP + ":" + remotePort;
			List<NameValuePair> urlParameters = new ArrayList<>();
			urlParameters.add(new BasicNameValuePair("message", messageText));
			
			CloseableHttpClient httpClient = HttpClientBuilder.create().build();
			UrlEncodedFormEntity data = new UrlEncodedFormEntity(urlParameters);
			HttpPost post = new HttpPost(remoteAddress);
			post.setConfig(configTimeout);
			post.setEntity(data);
			CloseableHttpResponse response = httpClient.execute(post);
			int code = response.getCode();
			EntityUtils.consume(response.getEntity());
			response.close();
			httpClient.close();
			
			if(code == 200){
				success = true;
			}
			else{
				reason = "http request returned code: " + code;
				success = false;
			}
			
		}catch(Exception e){
			//TODO: vaghti avval server boode va connect shodim baad disconnect mishe hamash in erroro migirim mogheye send kardan
			reason = "request could not be sent: " + e.getMessage();
			success = false;
		}
		
		callback.call(success, reason);

	}
	
	private void sendFileMessage(ChatmanMessage message, SendCallback callback){

		boolean success = false;
		String reason = "";
		
		try{
			String remotePort = ChatmanConfig.getInstance().get("server-port", ChatmanConfig.DEFAULT_SERVER_PORT);
			String remoteAddress = "http://" + serverIP + ":" + remotePort;
			String filePath = message.getContent();
			File file = new File(filePath);

			CloseableHttpClient httpClient = HttpClientBuilder.create().build();
			HttpEntity requestEntity = MultipartEntityBuilder.create().addBinaryBody("data", file).build();
			HttpPost post = new HttpPost(remoteAddress);
			post.setConfig(configTimeout);
			post.setEntity(requestEntity);
			CloseableHttpResponse response = httpClient.execute(post);
			int code = response.getCode();
			EntityUtils.consume(response.getEntity());
			response.close();
			httpClient.close();
			
			if(code == 200){
				success = true;
			}
			else{
				reason = "http request returned code: " + code;
				success = false;
			}

		}catch(Exception e){
			reason = "request could not be sent: " + e.getMessage();
			success = false;
		}
		
		callback.call(success, reason);

	}

	@Override
	public void connect(){
		
		if(connectInProgress){
			return;
		}
		
        ChatFrame.getInstance().setLabelStatus(Helper.getInstance().getStr("searching_network"));

		setConnectInProgress(true);
		this.serverIP = null;
        int serverPort = Integer.valueOf(ChatmanConfig.getInstance().get("server-port", ChatmanConfig.DEFAULT_SERVER_PORT));
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
            String serverIp = ChatmanConfig.getInstance().get("server-ip", "");
			ipsToScan = new String[]{serverIp};
        }
        else{
            String subnet = ChatmanConfig.getInstance().get("subnet-mask", ChatmanConfig.DEFAULT_SUBNET);
			int numHostsToScan = Integer.valueOf(ChatmanConfig.getInstance().get("num-hosts-to-scan", ChatmanConfig.DEFAULT_HOSTS_SCAN));
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

		this.connectInProgress = b;
		if(connectInProgress){
			ChatFrame.getInstance().disableInputTextArea();
		}
		else{
			ChatFrame.getInstance().enableInputTextArea();
		}
	}
	
	public boolean isConnectInProgress(){
		return this.connectInProgress;
	}
	
	public boolean isServerFound(){
		return (serverIP != null);
	}

}

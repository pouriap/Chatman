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

import com.pouria.chatman.CMMessage;
import com.pouria.chatman.CMHelper;
import com.pouria.chatman.classes.ChatmanClient;
import com.pouria.chatman.classes.CmdInvokeLater;
import com.pouria.chatman.classes.CmdSetLabelStatus;
import com.pouria.chatman.CMConfig;
import com.pouria.chatman.classes.CmdShowLoading;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
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
public class HttpClient extends Observable implements ChatmanClient{
	
	private String serverIP = null;
	private boolean connectInProgress = false;
	private final RequestConfig configTimeoutText;
	private final RequestConfig configTimeoutFile;
	private final int timeoutMillis = 300;	//millis
	private final int timeoutMillisFile = 5;	//mins

	
	public HttpClient(){
		configTimeoutText = RequestConfig.custom()
			.setConnectTimeout(timeoutMillis, TimeUnit.MILLISECONDS)
			.setResponseTimeout(timeoutMillis, TimeUnit.MILLISECONDS)
			.setConnectionRequestTimeout(timeoutMillis, TimeUnit.MILLISECONDS)
			.build();
		//file sending takes much longer so maximum is 5 minutes
		configTimeoutFile = RequestConfig.custom().setConnectTimeout(timeoutMillis, TimeUnit.MILLISECONDS)
			.setResponseTimeout(timeoutMillisFile, TimeUnit.MINUTES)
			.setConnectionRequestTimeout(timeoutMillis, TimeUnit.MILLISECONDS)
			.build();
	}

	//this function is blocking!
	//TODO: create sendTextMessage and sendFileMessage and add all this shit to outoginghandler
	@Override
	public synchronized boolean send(CMMessage message){
		
		if(serverIP == null){
			return false;
		}

		boolean success;

		if(message.getType() == CMMessage.TYPE_FILE){
			success = sendFileMessage(message);
		}
		else{
			success = sendTextMessage(message);
		}

		if(!success){
			removeServer();
		}
		
		return success;

	}
	
	private boolean sendTextMessage(CMMessage message){

		boolean success = false;
		String reason = "";
		String messageText = message.getAsJsonString();
				
		try{
			String remotePort = CMConfig.getInstance().get("server-port", CMConfig.DEFAULT_SERVER_PORT);
			String remoteAddress = "http://" + serverIP + ":" + remotePort;
			List<NameValuePair> urlParameters = new ArrayList<>();
			urlParameters.add(new BasicNameValuePair("message", messageText));
			
			CloseableHttpClient httpClient = HttpClientBuilder.create().build();
			UrlEncodedFormEntity data = new UrlEncodedFormEntity(urlParameters);
			HttpPost post = new HttpPost(remoteAddress);
			post.setConfig(configTimeoutText);
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
				//don'g log the pings
				if(message.getType() != CMMessage.TYPE_PING){
					CMHelper.getInstance().log("sending text message failed. reason: " + reason);
				}
			}
			
		}catch(Exception e){
			reason = "http request could not be sent: " + e.getMessage();
			success = false;
			//don't log the pings
			if(message.getType() != CMMessage.TYPE_PING){
				CMHelper.getInstance().log("sending text message failed. reason: " + reason);
			}
		}
		
		return success;

	}
	
	private boolean sendFileMessage(CMMessage message){

		boolean success = false;
		String reason = "";
		
		try{
			String remotePort = CMConfig.getInstance().get("server-port", CMConfig.DEFAULT_SERVER_PORT);
			String remoteAddress = "http://" + serverIP + ":" + remotePort;
			String filePath = message.getContent();
			File file = new File(filePath);

			CloseableHttpClient httpClient = HttpClientBuilder.create().build();
			HttpEntity requestEntity = MultipartEntityBuilder.create().addBinaryBody("data", file).build();
			HttpPost post = new HttpPost(remoteAddress);
			post.setConfig(configTimeoutFile);
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
				CMHelper.getInstance().log("sending file message failed. reason: " + reason);
			}

		}catch(Exception e){
			reason = "request could not be sent: " + e.getMessage();
			success = false;
			CMHelper.getInstance().log("sending file message failed. reason: " + reason);
		}
		
		return success;
	}

	@Override
	//this is blocking!
	public synchronized boolean connect(){
		
		if(connectInProgress){
			return false;
		}
		
		connectInProgress = true;
		removeServer();

		(new CmdInvokeLater(new CmdSetLabelStatus(CMHelper.getInstance().getStr("searching_network")))).execute();
		(new CmdInvokeLater(new CmdShowLoading(true))).execute();
		
        int serverPort = Integer.valueOf(CMConfig.getInstance().get("server-port", CMConfig.DEFAULT_SERVER_PORT));
		String[] ipsToScan = getIpsToScan();
		
		IpScanner scanner = new IpScanner(ipsToScan, serverPort);
		ArrayList<String> foundIps = scanner.scan();
		boolean success = !foundIps.isEmpty();
		
		if(success){
			String ip = foundIps.get(0);
			setServer(ip);
		}
		else{
			removeServer();
		}
		
		connectInProgress = false;
		(new CmdInvokeLater(new CmdShowLoading(false))).execute();
		
		return success;
		
	}
	
	private String[] getIpsToScan(){
		
		String[] ipsToScan;
		//if we have server's ip we don't scan the network
        if(CMConfig.getInstance().isSet("server-ip")){
            String serverIp = CMConfig.getInstance().get("server-ip", "");
			ipsToScan = new String[]{serverIp};
        }
        else{
            String subnet = CMConfig.getInstance().get("subnet-mask", CMConfig.DEFAULT_SUBNET);
			int numHostsToScan = Integer.valueOf(CMConfig.getInstance().get("num-hosts-to-scan", CMConfig.DEFAULT_HOSTS_SCAN));
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
		if(server == null){
			removeServer();
		}
		else{
			this.serverIP = (String) server;
			(new CmdInvokeLater(new CmdSetLabelStatus(CMHelper.getInstance().getStr("connection_with") + this.serverIP + CMHelper.getInstance().getStr("stablished")))).execute();
			setChanged();
			notifyObservers();
		}
	}
	
	private synchronized void removeServer(){
		this.serverIP = null;
		(new CmdInvokeLater(new CmdSetLabelStatus(CMHelper.getInstance().getStr("server_not_found")))).execute();
	}

	@Override
	public void addListener(Observer o){
		addObserver(o);
	}
	
}

/*
 * Copyright (c) 2020. Pouria Pirhadi
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.pouria.chatman.connection;

import com.pouria.chatman.CMConfig;
import com.pouria.chatman.CMHelper;
import com.pouria.chatman.commands.CmdInvokeLater;
import com.pouria.chatman.commands.CmdUpdateProgressbar;
import javafx.util.Pair;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.entity.mime.StringBody;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicNameValuePair;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author pouriap
 */
public class HttpClient extends Observable implements ChatmanClient{
	
	private String serverIP = null;
	private boolean connectInProgress = false;
	private final RequestConfig configTimeoutText;
	private final RequestConfig configTimeoutFile;
	private final int connectTimeout = 100;	//millis  - this is for socket connection
	private final int responseTimeout = 500; //millis - in moddatye ke handler dar server tool mikeshe return kone
	private final int responeTimeoutFile = 5;	//mins - baraye file handler bayad sabr kone ta file biad va baad copy ham bokone pas bishtare

	
	public HttpClient(){
		configTimeoutText = RequestConfig.custom()
			.setConnectTimeout(connectTimeout, TimeUnit.MILLISECONDS)
			.setConnectionRequestTimeout(connectTimeout, TimeUnit.MILLISECONDS)
			.setResponseTimeout(responseTimeout, TimeUnit.MILLISECONDS)
			.build();
		//file sending takes much longer so maximum is 5 minutes
		configTimeoutFile = RequestConfig.custom()
			.setConnectTimeout(connectTimeout, TimeUnit.MILLISECONDS)
			.setConnectionRequestTimeout(connectTimeout, TimeUnit.MILLISECONDS)
			.setResponseTimeout(responeTimeoutFile, TimeUnit.MINUTES)
			.build();
	}


	@Override
	public boolean sendText(String text){
		
		List<NameValuePair> postParams = new ArrayList<>();
		postParams.add(new BasicNameValuePair("message", text));
		HttpEntity postData = new UrlEncodedFormEntity(postParams, StandardCharsets.UTF_8);

		return sendPOSTRequest(postData, configTimeoutText);

	}
	
	@Override
	public boolean sendFile(File file, String metadata){

		FileBodyWithProgress fileBody = new FileBodyWithProgress(file, ContentType.APPLICATION_OCTET_STREAM.withCharset("UTF-8"));
		fileBody.setProgressCallback((int percent) -> {
			(new CmdInvokeLater(new CmdUpdateProgressbar(percent))).execute();
		}, 200);
		StringBody stringBodyMetadata = new StringBody(metadata, ContentType.TEXT_PLAIN.withCharset("UTF-8"));
		
		HttpEntity postData = MultipartEntityBuilder.create()
				.addPart("file", fileBody)
				.addPart("metadata", stringBodyMetadata)
				.setCharset(StandardCharsets.UTF_8)
				.build();
		
		return sendPOSTRequest(postData, configTimeoutFile);
		
	}
	
	private boolean sendPOSTRequest(HttpEntity postData, RequestConfig config){

		if(serverIP == null){
			return false;
		}

		boolean success = false;
		String reason = "";
				
		try{
			String remotePort = CMConfig.getInstance().get("server-port", CMConfig.DEFAULT_SERVER_PORT);
			String remoteAddress = "http://" + serverIP + ":" + remotePort;
			
			HttpPost post = new HttpPost(remoteAddress);
			post.setConfig(config);
			post.setEntity(postData);

			CloseableHttpClient httpClient = HttpClientBuilder.create().build();			
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
				CMHelper.getInstance().log("send failed. reason: " + reason);
			}
			
		}catch(Exception e){
			reason = "http request could not be sent: " + e.getMessage();
			success = false;
			CMHelper.getInstance().log("send failed. reason: " + reason);
		}
		
		if(!success){
			removeServer();
		}
		
		return success;
	}

	@Override
	//this is blocking!
	public synchronized boolean connect(){
		
		CMHelper.getInstance().log("trying to connect");
		
		if(connectInProgress){
			CMHelper.getInstance().log("connection already in progress. stopping connect attempt");
			return false;
		}
		
		connectInProgress = true;
		removeServer();
		notifyListeners(ConnectionStatus.CONNETING);

        int serverPort = Integer.parseInt(CMConfig.getInstance().get("server-port", CMConfig.DEFAULT_SERVER_PORT));
		String[] ipsToScan = getIpsToScan();
		
		IpScanner scanner = new IpScanner(ipsToScan, serverPort);
		ArrayList<String> foundIps = scanner.scan();
		boolean success = !foundIps.isEmpty();
		
		if(success){
			String ip = foundIps.get(0);
			CMHelper.getInstance().log("connection sucessfully established with " + ip);
			setServer(ip);
		}
		else{
			CMHelper.getInstance().log("connection failed. no servers found");
			removeServer();
		}
		
		connectInProgress = false;
		
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
			int numHostsToScan = Integer.parseInt(CMConfig.getInstance().get("num-hosts-to-scan", CMConfig.DEFAULT_HOSTS_SCAN));
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
			return;
		}
					
		String ip = (String) server;

		//if there is a server-ip set in the config don't let any other server to be set (useful when ping sets server)
		if(CMConfig.getInstance().isSet("server-ip") && !ip.equals(CMConfig.getInstance().get("server-ip", "")) && !ip.equals("127.0.0.1")){
			removeServer();
			return;
		}

		this.serverIP = ip;
		notifyListeners(ConnectionStatus.CONNECTED);

	}
	
	private synchronized void removeServer(){
		this.serverIP = null;
		notifyListeners(ConnectionStatus.DISCONNECTED);
	}

	private void notifyListeners(ConnectionStatus status){
		Pair<ConnectionStatus, String> result = new Pair<>(status, this.serverIP);
		setChanged();
		notifyObservers(result);
	}

	@Override
	public void addServerStateChangedListener(Observer o){
		addObserver(o);
	}
	
}

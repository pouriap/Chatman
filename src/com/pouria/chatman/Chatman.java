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
package com.pouria.chatman;

import com.pouria.chatman.classes.AbstractPOSTHander;
import com.pouria.chatman.commands.CmdChangeStatusIcon;
import com.pouria.chatman.commands.CmdInvokeLater;
import com.pouria.chatman.commands.CmdSetLabelStatus;
import com.pouria.chatman.connection.*;
import com.pouria.chatman.enums.CMType;
import com.pouria.chatman.messages.CMMessage;
import com.pouria.chatman.messages.DisplayableMessage;
import com.pouria.chatman.messages.PingMessage;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.form.FormData;
import javafx.util.Pair;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author pouriap
 */
public class Chatman {
	
	private final ChatmanServer server;
	private final ChatmanClient client;
	private final CMHistory history;
	private final BgTasksManager bgTasksMngr;
	
	private final long HISTORY_SAVE_INTERVAL = TimeUnit.MINUTES.toMillis(5);
	private final long HEARTBEAT_INTERVAL = TimeUnit.MINUTES.toMillis(1);
	private final long OLD_MSG_TIMEDIFF = TimeUnit.HOURS.toMillis(1);
	
	private final String horizontalLineHtml = "<div style='margin-top:20px;margin-bottom:20px;text-align:center;font-size:8px;font-color:#606060'>____________________ older messages ____________________<br></div>";
	private final ArrayList<DisplayableMessage> allDisplayableMessages = new ArrayList<>();
	private final CMSendQueue sendQueue;
	
	public Chatman(){
		int port = Integer.parseInt(CMConfig.getInstance().get("server-port", CMConfig.DEFAULT_SERVER_PORT));
		client = new HttpClient(port, getIpsToScan(), CMHelper.getInstance()::log);
		server = new HttpServer(port, new ChatmanHandler());
		sendQueue = new CMSendQueue();
		bgTasksMngr = new BgTasksManager();
		history = new CMHistory();
	}
	
	public ChatmanClient getClient(){
		return this.client;
	}
	
	public void start() throws Exception{
		server.start();
		client.addServerStateChangedListener(bgTasksMngr);
		bgTasksMngr.start();
	}

	// we only send displayable messages with sentQueue
	public void sendMessage(DisplayableMessage m){
		sendQueue.add(m);
		sendQueue.process();
	}
	
	public void saveHistory(){
		history.save();
	}
	
	//anything that accesses Lists should be synchronized
	public synchronized void addToAllDisplayableMessages(DisplayableMessage message){
		//don't add duplicates (when resending failed messages)
		if(!allDisplayableMessages.contains(message)){
			allDisplayableMessages.add(message);
		}
	}

	public synchronized DisplayableMessage[] getAllDisplayableMessages(){
		DisplayableMessage[] messages = new DisplayableMessage[allDisplayableMessages.size()];
		allDisplayableMessages.toArray(messages);
		return messages;
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

	//all access to lists should be synchronized
	public synchronized String getAllMessagesText(){
		
		//this takes ~20ms with a shitload of messages
		StringBuilder conversationTextAll = new StringBuilder();
		long prevMessageTime = System.currentTimeMillis();
		for(DisplayableMessage message: allDisplayableMessages){
			//put a line after older messages
			long messageTime = message.getTime();
			if(messageTime - prevMessageTime > OLD_MSG_TIMEDIFF){
				conversationTextAll.append(horizontalLineHtml);
			}
			prevMessageTime = messageTime;

			conversationTextAll.append(message.getAsHTMLString());
		}
		
		return conversationTextAll.toString();
	}
	
	
	/**
	 * A class that manages background tasks that happen in threads
	 */
	private class BgTasksManager implements Observer{
		
		//starts threads/timers that should be running in the background
		public void start(){
			
			//connect for the first time and send a first ping letting them know we're up
			Runnable r = () -> {
				client.connect();
				PingMessage firstPing = PingMessage.getNewOutgoing();
				OutgoingMsgHandler handler = new OutgoingMsgHandler(firstPing);
				handler.send();
			};
			(new Thread(r, "CM-Initial-Connect")).start();
			
			//save history
			Timer historyTimer = new Timer("CM-History-Saver");
			TimerTask historyTask = new TimerTask() {
				@Override
				public void run() {
					saveHistory();
				}
			};
			historyTimer.scheduleAtFixedRate(historyTask, HISTORY_SAVE_INTERVAL, HISTORY_SAVE_INTERVAL);
				
			//send heartbeat
			Timer heartBeatTimer = new Timer("CM-Heartbeat-Sender");
			TimerTask heartBeatTask = new TimerTask() {
				@Override
				public void run() {
					PingMessage pingMessage = PingMessage.getNewOutgoing();
					OutgoingMsgHandler handler = new OutgoingMsgHandler(pingMessage);
					handler.send();
					boolean connected = (pingMessage.getStatus() == CMMessage.Status.SENT);
					if(!connected){
						client.connect();
					}
				}
			};
			heartBeatTimer.scheduleAtFixedRate(heartBeatTask, HEARTBEAT_INTERVAL, HEARTBEAT_INTERVAL);
			
		}
		
		/**
		 * is called from client when a server is found
		 * @param o
		 * @param arg a Pair<ChatmanClient.ConnectionStatus, String><br>
		 *            String is IP if found
		 */
		@Override
		public synchronized void update(Observable o, Object arg) {

			Pair<ChatmanClient.ConnectionStatus, String> result = (Pair)arg;
			ChatmanClient.ConnectionStatus status = result.getKey();
			String ip = result.getValue();

			switch(status){
				case CONNECTED:
					(new CmdInvokeLater(new CmdSetLabelStatus(CMHelper.getInstance().getStr("connection_with") + ip + CMHelper.getInstance().getStr("stablished")))).execute();
					(new CmdInvokeLater(new CmdChangeStatusIcon("connected.png"))).execute();
					sendQueue.process();
					break;

				case CONNETING:
					(new CmdInvokeLater(new CmdSetLabelStatus(CMHelper.getInstance().getStr("searching_network")))).execute();
					(new CmdInvokeLater(new CmdChangeStatusIcon("connecting.gif"))).execute();
					break;

				case DISCONNECTED:
					(new CmdInvokeLater(new CmdSetLabelStatus(CMHelper.getInstance().getStr("server_not_found")))).execute();
					(new CmdInvokeLater(new CmdChangeStatusIcon("disconnected.png"))).execute();
			}

		}

	}


	private class ChatmanHandler extends AbstractPOSTHander {

		@Override
		public void handle(HttpServerExchange exchange, FormData formData) throws Exception{

			long start = System.currentTimeMillis();

			String localIp = exchange.getDestinationAddress().getAddress().getHostAddress();
			String peerIp = exchange.getSourceAddress().getAddress().getHostAddress();

			//some guards
			if(!"127.0.0.1".equals(peerIp)){
				//we don't want to receive our own messages unless it's a showGUI
				if(peerIp.equals(localIp)){
					CMHelper.getInstance().log("rejecting self-to-self message with IP: " + peerIp);
					exchange.setStatusCode(400);
					return;
				}
				//don't receive message from anyone else when server is set
				if(CMConfig.getInstance().isSet("server-ip")){
					String configServerIP = CMConfig.getInstance().get("server-ip", "");
					if(!peerIp.equals(configServerIP)){
						CMHelper.getInstance().log("rejecting message from IP: " + peerIp);
						exchange.setStatusCode(400);
						return;
					}
				}
			}

			//form data is stored here
			CMFormDataParser parser = new CMFormDataParser(formData);
			CMMessage message = parser.parseAsCMMessage();
			IncomingMsgHandler handler = new IncomingMsgHandler(message);
			handler.receive();

			//don't set server to localhost
			if("127.0.0.1".equals(peerIp)){
				return;
			}

			//set server everytime we recieve a message to avoid unnecessary searches
			notifyServerIsUp(peerIp);

			long time = System.currentTimeMillis() - start;
			CMHelper.getInstance().log("request handling took: " + time + " millis");

		}

		//we do this in a thread because if we block request takes too long and times out
		private void notifyServerIsUp(String serverIP){
			Runnable r = () -> {
				client.setServer(serverIP);
			};
			(new Thread(r, "CM-Server-Notify")).start();
		}

	}
	
}

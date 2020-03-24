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

import com.pouria.chatman.CMConfig;
import com.pouria.chatman.CMHelper;
import com.pouria.chatman.DisplayableMsgHandler;
import com.pouria.chatman.IncomingMsgHandler;
import com.pouria.chatman.enums.CMType;
import com.pouria.chatman.gui.ChatFrame;
import com.pouria.chatman.messages.CMMessage;
import com.pouria.chatman.messages.DisplayableMessage;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.form.FormData;
import io.undertow.server.handlers.form.FormDataParser;
import io.undertow.server.handlers.form.FormParserFactory;

import java.io.IOException;

/**
 *
 * @author pouriap
 */
public class HttpServer implements ChatmanServer{
	
	@Override
	public void start() throws IOException{
		
		int serverPort = Integer.valueOf(CMConfig.getInstance().get("server-port", CMConfig.DEFAULT_SERVER_PORT));
		final ChatmanHandler handler = new ChatmanHandler();
		
		Undertow server = Undertow.builder()
			.addHttpListener(serverPort, "0.0.0.0")
			.setHandler((HttpServerExchange exchange) -> {
				//parses POST form data and passes it to a handler
				FormDataParser parser = FormParserFactory.builder().build().createParser(exchange);
				//send Bad Request if there is no post data
				if(parser == null){
					exchange.setStatusCode(400);
					return;
				}
				parser.parse(handler);
		}).build();
		
		server.start();

	}


	private class ChatmanHandler implements HttpHandler{
		
		@Override
		public void handleRequest(HttpServerExchange exchange) throws Exception {
			
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
					if(!peerIp.equals(configServerIP))
					return;
				}
			}

			//form data is stored here
			FormData formData = exchange.getAttachment(FormDataParser.FORM_DATA);
			CMFormDataParser parser = new CMFormDataParser(formData);
			CMMessage message = parser.parseAsCMMessage();
			IncomingMsgHandler handler = new IncomingMsgHandler(message);
			handler.handle();
			if(message.isDisplayable()){
				DisplayableMsgHandler displayer = new DisplayableMsgHandler((DisplayableMessage)message);
				displayer.handle();
			}
			
			//don't set server for showgui messages
			if(message.getType() == CMType.SHOWGUI){
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
				ChatFrame.getInstance().getChatmanInstance().getClient().setServer(serverIP);
			};
			(new Thread(r, "CM-Server-Notify")).start();
		}
		
	}
	
}

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
import com.pouria.chatman.classes.ChatmanServer;
import com.pouria.chatman.gui.ChatFrame;
import com.pouria.chatman.CMConfig;
import com.pouria.chatman.MessageHandler;
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
			.setHandler(new HttpHandler() {
				@Override
				public void handleRequest(HttpServerExchange exchange) throws Exception {
					//parses POST form data and passes it to a handler
					FormDataParser parser = FormParserFactory.builder().build().createParser(exchange);
					parser.parse(handler);
				}
			}).build();
		
		server.start();

	}


	private class ChatmanHandler implements HttpHandler{
		@Override
		public void handleRequest(HttpServerExchange exchange) throws Exception {
			//form data is stored here
			FormData formData = exchange.getAttachment(FormDataParser.FORM_DATA);
			CMMessage message = new CMMessage(formData);
			MessageHandler handler = new MessageHandler(CMMessage.DIR_IN);
			handler.handle(message);
			//set server everytime we recieve a message to avoid unnecessary searches
			String ourIP = exchange.getDestinationAddress().getAddress().getHostAddress();
			String peerIP = exchange.getSourceAddress().getAddress().getHostAddress();
			//to avoid setting server as our own IP when we sendMessage showGUI messages from our own PC
			if(!peerIP.equals(ourIP) && !peerIP.equals("127.0.0.1")){
				notifyServerIsUp(peerIP);
			}
		}
		//we do this in a thread because if we block request takes too long and times out
		private void notifyServerIsUp(String serverIP){
			final String ip = serverIP;
			Runnable r = () -> {
				ChatFrame.getInstance().getChatmanInstance().getClient().setServer(ip);
			};
			(new Thread(r, "CM-Server-Notify")).start();
		}
	}
	
}

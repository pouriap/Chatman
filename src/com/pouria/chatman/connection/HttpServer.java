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

import com.pouria.chatman.ChatmanMessageHandler;
import com.pouria.chatman.classes.ChatmanServer;
import com.pouria.chatman.classes.CommandFatalErrorExit;
import com.pouria.chatman.classes.CommandInvokeLater;
import com.pouria.chatman.gui.ChatmanConfig;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

/**
 *
 * @author pouriap
 */
public class HttpServer implements ChatmanServer{
	
	@Override
	public void start(){
		
		int serverPort = Integer.valueOf(ChatmanConfig.getInstance().get("server-port"));
		
		Undertow server = Undertow.builder()
				.addHttpListener(serverPort, "0.0.0.0")
				.setHandler(new HttpHandler() {
					@Override
					public void handleRequest(final HttpServerExchange exchange) throws Exception {
						ChatmanMessageHandler MsgHandler = 
								new ChatmanMessageHandler(exchange.getQueryParameters());
						MsgHandler.handle();
					}
				}).build();
		
		
		try{
			server.start();
			
		}catch(Exception e){
			String error = "Could not start server: " + e.getMessage();
			(new CommandInvokeLater(new CommandFatalErrorExit(error))).execute();
		}
		
	}


}

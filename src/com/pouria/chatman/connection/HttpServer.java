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

import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.form.FormDataParser;
import io.undertow.server.handlers.form.FormParserFactory;

import java.io.IOException;

/**
 *
 * @author pouriap
 */
public class HttpServer implements ChatmanServer{

	private final int port;
	private final HttpHandler requestHandler;
	private Undertow server;

	public HttpServer(int port, HttpHandler requestHandler){
		this.port = port;
		this.requestHandler = requestHandler;
	}

	@Override
	public void start() throws IOException{

		server = Undertow.builder()
			.addHttpListener(this.port, "0.0.0.0")
			.setHandler((HttpServerExchange exchange) -> {
				//parses POST form data and passes it to a handler
				FormDataParser parser = FormParserFactory.builder().build().createParser(exchange);
				//send Bad Request if there is no post data
				if(parser == null){
					exchange.setStatusCode(400);
					return;
				}
				parser.parse(requestHandler);
		}).build();
		
		server.start();

	}

	//for stupid fucking tests
	public void stop(){
		server.stop();
	}

}

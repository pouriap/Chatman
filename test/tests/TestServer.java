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

package tests;

import com.pouria.chatman.connection.ChatmanServer;
import com.pouria.chatman.connection.HttpClient;
import com.pouria.chatman.connection.HttpServer;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;


public class TestServer {

	static final int port = 8571;
	final Undertow server;
	String receivedHTTPData = "";

	public TestServer(){

		server = Undertow.builder()
			.addHttpListener(port, "0.0.0.0")
			.setHandler(new HttpHandler() {
				@Override
				public void handleRequest(HttpServerExchange exchange) throws Exception{
					if(exchange.isInIoThread()){
						exchange.dispatch(this);
						return;
					}
					exchange.startBlocking();
					InputStream in = exchange.getInputStream();
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					byte[] buf = new byte[512];
					int len;
					while(( len = in.read(buf) ) != -1){
						out.write(buf, 0, len);
					}
					receivedHTTPData = URLDecoder.decode(out.toString(), "UTF-8");
				}
			}).build();

		server.start();

	}

	public void stop(){
		server.stop();
	}

}

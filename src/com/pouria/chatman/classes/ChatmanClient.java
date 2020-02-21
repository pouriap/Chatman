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
package com.pouria.chatman.classes;

import com.pouria.chatman.ChatmanMessage;

/**
 *
 * @author pouriap
 */
public interface ChatmanClient {
	/**
	 * sends a string to the other person we are talking to
	 * @param message 
	 * @throws com.pouria.chatman.classes.PeerNotFoundException 
	 */
	public void send(String message) throws PeerNotFoundException;

	/**
	 * sends a ChatmanMessage to the other person we are talking to
	 * @param message 
	 * @throws com.pouria.chatman.classes.PeerNotFoundException 
	 */	
	public void send(ChatmanMessage message) throws PeerNotFoundException;
	
	/**
	 * connects to the person we want to talk to if their IP is specified <br>
	 * should search network if their IP is not specified <br>
	 * the connection/search process takes place in a thread <br>
	 * the setServer() method is called from that thread in order to specify the 
	 * server the client should send() messages to
	 */
	public void connect();
	
	/**
	 * sets the server this client is supposed to connect to <br>
	 * the server Object can be anything depending on the implementation<br>
	 * is called when server is found in search/connect threads
	 * @param server 
	 */
	public void setServer(Object server);
	
	/**
	 * when searching for servers in the network we use many threads<br>
	 * we use this to check in that threads if server is found
	 * @return 
	 */
	public boolean isServerFound();
	
}

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

import java.io.File;
import java.util.Observer;

/**
 * every method in this class should be blocking!!!
 *
 * @author pouriap
 */
public interface ChatmanClient{

	/**
	 * sends a String to the server we're connected to<br>
	 * this function should be blocking!
	 * @param text text to send
	 * @return  the result of send operation
	 */	
	public boolean sendText(String text);

	/**
	 * sends a File to the server we're connected to<br>
	 * this function should be blocking!
	 * @param file file to send
	 * @return  the result of send operation
	 */	
	public boolean sendFile(File file);
	
	/**
	 * connects to the person we want to talk to if their IP is specified <br>
	 * should search network if their IP is not specified <br>
	 * the connection/search process takes place in a thread <br>
	 * the setServer() method is called from that thread in order to specify the 
	 * server the client should send() messages to<br>
	 * this function should be blocking!
	 * @return whether connection with server was successful
	 */
	public boolean connect();
	
	/**
	 * sets the server this client is supposed to connect to <br>
	 * the server Object can be anything depending on the implementation<br>
	 * is called when server is found in search/connect threads<br>
	 * this function should be blocking!
	 * @param server 
	 */
	public void setServer(Object server);
	
	
	public void addListener(Observer o);
	
}

/*
 * Copyright (C) 2016 Pouria Pirhadi
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
package com.pouria.chatman;

import com.pouria.chatman.classes.ChatmanClient;
import com.pouria.chatman.classes.CommandClientConnect;
import com.pouria.chatman.classes.CommandConfirmDialog;
import com.pouria.chatman.classes.CommandInvokeLater;
import com.pouria.chatman.classes.CommandSetLabelStatus;
import com.pouria.chatman.connection.HttpClient;
import com.pouria.chatman.gui.ChatFrame;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 *
 * @author pouriap
 * 
 * is only used when we are client
 * takes an ip and port and connects to it
 * is only called when we have the ip of the live server
 * which we acquire either by scanning or from the config server-ip
 */

public class IpConnector implements Runnable{

    private final ChatFrame gui;
	private final ChatmanClient client;
    private final String ip;
    private final int port;
	private final boolean askRetry;
    
    public IpConnector(String host, int port, boolean retry){
        this.gui = ChatFrame.getInstance();
		this.client = gui.getChatmanInstance().getClient();
        this.port = port;
        this.ip = host;
		this.askRetry = retry;
    }

    @Override
    public void run() {
        //connect to a specific ip
        try{
			
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(ip, port), 3000);
			socket.close();
			client.setServer(ip);
			((HttpClient) client).setConnectInProgress(false);
	        (new CommandInvokeLater(new CommandSetLabelStatus(Helper.getInstance().getStr("connection_with") + ip + Helper.getInstance().getStr("stablished")))).execute();
		 
        }catch(IOException ex){
			if(askRetry){
				//we show a confirm dialog asking retry?
				(new CommandInvokeLater(new CommandConfirmDialog(
						new CommandClientConnect(),
						Helper.getInstance().getStr("server_retry_confirm"),
						Helper.getInstance().getStr("server_not_found")
				))).execute();
			}
        }
        
    }
    
}

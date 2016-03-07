/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pouria.chatman;

import com.pouria.chatman.classes.CommandClientConnect;
import com.pouria.chatman.classes.CommandConfirmDialog;
import com.pouria.chatman.classes.CommandInvokeLater;
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
    private final String ip;
    private final int port;
    private final boolean isSignle;
    
    IpConnector(String h, int p, boolean r){
        this.gui = ChatFrame.getInstance();
        this.port = p;
        this.ip = h;
        this.isSignle = r;
    }

    @Override
    public void run() {
        //connect to a specific ip
        try{
            Socket socket = new Socket();           
            socket.connect(new InetSocketAddress(ip, port), 3000);
            
            //isSingle = we have the server ip and we are connecting to it
            //there is no scanning and adding to list
            if(isSignle){
                ((ChatmanClient)gui.getChatmanInstance()).setServerSocket(socket);
                ((ChatmanClient)gui.getChatmanInstance()).start();
            }
            //we are scanning
            //so we just add the live server to list
            //when scanning is finished we decide what to do in IpScanner
            else{
                ((ChatmanClient)gui.getChatmanInstance()).addLiveServer(socket);
            }
            
        }catch(IOException ex){ 
            //is single is false when we are scanning network for servers and don't want a "not found" message for each failed connect
            //we show a confirm dialog asking retry?
            if(isSignle){
                (new CommandInvokeLater(new CommandConfirmDialog(
                        new CommandClientConnect(),
                        gui.l.getString("server_retry_confirm"),
                        gui.l.getString("server_not_found")
                ))).execute();
            }
        }
        
    }
    
}

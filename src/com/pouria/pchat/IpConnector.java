/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pouria.pchat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 *
 * @author SH
 */
public class IpConnector implements Runnable{

    ChatFrame gui;
    String ip;
    int port;
    boolean verbose;
    private static String exceptionMessage;
    
    IpConnector(ChatFrame g, String h, int p, boolean r){
        this.gui = g;
        this.port = p;
        this.ip = h;
        this.verbose = r;
        exceptionMessage = "";
    }

    @Override
    public void run() {
        //connect to a specific ip
        try{
            Socket socket = new Socket();           
            socket.connect(new InetSocketAddress(ip, port), 3000);
            gui.getChatmanInstance().setServerSocket(socket);
            gui.getChatmanInstance().start();
        }catch(IOException ex){ 
            //verbose is false when we are scanning network for servers and don't want a "not found"
            //message for each failed connect
            if(verbose){
                exceptionMessage = ex.getMessage();
                gui.getChatmanInstance().connect(true);
            }
        }catch(IllegalArgumentException e){
            //server socket already set
            //we have this exception when we are scanning the network and more than one live
            //server is found
        }
    }
    
    public static String getExceptionMessage(){
        return exceptionMessage;
    } 
}

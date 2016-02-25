/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pouria.pchat;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import javax.swing.JOptionPane;

/**
 *
 * @author SH
 */
public class ChatmanClient extends Chatman{
    
    private Socket serverSocket;
    private Thread scanner;
    
    ChatmanClient(ChatFrame gui){
        super(gui);
        this.mode = MOD_CLIENT;
    }
    
    @Override
    public void start(){
        //Output: Socket.OutputStream
        //Input:  Socket.inputStream
        //stablishes the input and output streams as a client
        //is only called from IpConnector when it finds an alive server
        try{
            writer = new PrintWriter(new OutputStreamWriter(serverSocket.getOutputStream()),true);
            gui.setLabelStatus("اتصال با " + serverSocket.getInetAddress().getHostAddress() + " برقرار شد");

            inputReaderThread = new Thread(new InputReaderTh(gui, "client", serverSocket));
            inputReaderThread.start();
        }catch(UnknownHostException e){
            gui.message("could not find host"+e.getMessage());
            gui.exit();
        }catch(IOException e){
            gui.message("could not open stream"+e.getMessage());
            gui.exit();
        }
    }
    
    @Override
    public void connect(boolean retry){
        //connects to server. if server-ip is specified in config then connects directly
        //else it scans the subnet-mask for live servers
        //if retry is true, shows a dialog asking the user whether to retry connection
        
        if(retry)
            if(JOptionPane.showConfirmDialog(null, "سروری در شبکه پیدا نشد. تلاش دوباره؟", "سرور پیدا نشد", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION)
                gui.exit();

        gui.setLabelStatus("در حال جستجوی شبکه");

        int serverPort = Integer.valueOf(gui.getConfig("server-port"));
        //if we have server's ip we don't scan the network
        if(gui.isConfigAvailable("server-ip")){
            String serverIp = gui.getConfig("server-ip");
            scanner = new Thread(new IpConnector(gui, serverIp, serverPort, true));
            scanner.start();
        }
        else{
            String subnet = gui.getConfig("subnet-mask");
            scanner = new Thread(new IpScanner(gui, subnet, serverPort));
            scanner.start();
        }

    }
    
    @Override
    public void setServerSocket(Socket s){
        if(this.serverSocket != null)
            throw new IllegalArgumentException();
        else
            this.serverSocket = s;
    }
    
    @Override
    public boolean isServerSocketSet(){
        return this.serverSocket != null;
    }
}

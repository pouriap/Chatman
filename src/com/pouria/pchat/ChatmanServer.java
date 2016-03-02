/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pouria.pchat;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

/**
 *
 * @author SH
 */
//Output: STDOUT
//Input:  STDIN
public class ChatmanServer extends Chatman {
    
    ChatmanServer(ChatFrame gui){
        super(gui, MOD_SERVER);
    }
    
    @Override
    public void start(){
        //establishes the input and output streams as a server
        writer = new PrintWriter(new OutputStreamWriter(System.out), true);
        gui.setLabelStatus("سرور در حال اجرا");

        inputReaderThread = new Thread(new InputReaderTh(gui));
        inputReaderThread.start();
    }
    
    @Override
    public void stop(){
        writer = null;
        if(inputReaderThread.isAlive())
            inputReaderThread.interrupt();
    }
    
    @Override
    public void connect(boolean retry){
    }
    
    @Override
    public void setServerSocket(Socket s){
    }
    
    @Override
    public boolean isServerSocketSet(){
        return false;
    }
}

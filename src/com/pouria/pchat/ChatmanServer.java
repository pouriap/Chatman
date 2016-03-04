/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pouria.pchat;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/**
 *
 * @author SH
 */
//Output: STDOUT
//Input:  STDIN
public class ChatmanServer extends Chatman {
    
    ChatmanServer(){
        super(MOD_SERVER);
    }
    
    @Override
    public void start(){
        //establishes the input and output streams as a server
        writer = new PrintWriter(new OutputStreamWriter(System.out), true);
        gui.setLabelStatus("سرور در حال اجرا");

        th = new InputReaderTh();
        inputReaderThread = new Thread(th);
        inputReaderThread.start();
    }


}

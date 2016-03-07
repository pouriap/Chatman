/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pouria.chatman.classes;

import com.pouria.chatman.ChatmanClient;
import com.pouria.chatman.gui.ChatFrame;

/**
 *
 * @author pouriap
 * 
 * it may seem that this Command doesn't do any GUI-related thing. but some GUI-related things happen in Chatman.connect()
 */
public class CommandClientConnect implements Command{
    
    public CommandClientConnect(){
    }
    
    @Override
    public void execute(){
        ChatFrame gui = ChatFrame.getInstance();
        ((ChatmanClient)gui.getChatmanInstance()).connect();
    };
}

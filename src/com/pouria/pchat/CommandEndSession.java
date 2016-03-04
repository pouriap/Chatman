/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pouria.pchat;

/**
 *
 * @author SH
 */
public class CommandEndSession implements Command{
    ChatFrame gui;
    String message; 
    
    public CommandEndSession(String message){
        this.gui = ChatFrame.getInstance();
        this.message = message;
    }
    
    @Override
    public void execute(){
        gui.endSession(message);
    }
}

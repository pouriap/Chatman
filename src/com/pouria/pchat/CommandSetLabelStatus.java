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
public class CommandSetLabelStatus implements Command{
    ChatFrame gui;
    String text;
    
    public CommandSetLabelStatus(ChatFrame gui, String text){
        this.gui = gui;
        this.text = text;
    }
    
    @Override
    public void execute(){
        gui.setLabelStatus(text);
    }
}
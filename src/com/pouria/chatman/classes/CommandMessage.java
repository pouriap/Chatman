/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pouria.chatman.classes;

import javax.swing.JOptionPane;

/**
 *
 * @author SH
 */
public class CommandMessage implements Command{
    String message;
    
    public CommandMessage(String message){
        this.message = message;
    }
    
    @Override
    public void execute(){
        JOptionPane.showMessageDialog(null, message);
    }
    
}

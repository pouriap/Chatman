/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pouria.chatman.classes;

import javax.swing.SwingUtilities;

/**
 *
 * @author SH
 */
public class CommandInvokeLater implements Command{
    Command[] innerCommands;
    
    public CommandInvokeLater(Command c){
        innerCommands = new Command[]{c};
    }
    
    public CommandInvokeLater(Command[] c){
        innerCommands = c;
    }
    
    @Override
    public void execute(){
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                for(Command command: innerCommands)
                    command.execute();
            }
        });
    }
}

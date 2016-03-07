/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pouria.chatman.classes;

/**
 *
 * @author pouriap
 * 
 * i could as well use the Runnable() interface instead of creating this
 * but i was learning design patterns and was excited :P
 * all classes implementing this interface do a GUI-related command
 * all classes implementing this interface will get wrapped in a CommandInvokeLater object (they don't have to but that's the whole purpose of this interface)
 * the purpose was to reduce code redundancy
 */
public interface Command {
    public void execute();
}

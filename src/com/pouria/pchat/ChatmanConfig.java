/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pouria.pchat;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author SH
 */
public class ChatmanConfig {
    
    private ChatFrame gui;
    private final String configFile = "config.conf";
    private List<String> defaultConfigs, configs = new ArrayList();
    
    private ChatmanConfig(){
        this.gui = ChatFrame.getInstance();
        this.defaultConfigs = Arrays.asList(new String[]{
            "background-image", "batman_1.jpg",
            "server-port", "9988",
            "subnet-mask", "192.168.1.*",
            "num-hosts-to-scan", "10"
        });
        
        try{
            String config;
            File f = new File(configFile);
            BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
            
            while((config = r.readLine()) != null){
                // '/' is comment character
                if(config.charAt(0) != '/')
                    configs.add(config);
            }
            
            r.close();
            
        }catch(IOException e){
            gui.message("could not read configuration file: " + e.getMessage());
            gui.exit();
        }
        
    }


    public String get(String confName) {      
        if(configs.contains(confName)){
            //confige value is one line after config name in config file
            return configs.get(configs.indexOf(confName)+1).trim();
        }
        //do we have a default config for this?
        else if(defaultConfigs.contains(confName)){
            return defaultConfigs.get(defaultConfigs.indexOf(confName)+1).trim();
        }
        //no? then it's fatal
        else{
            gui.message("configuration " + confName + " doesn't exist in config.conf");
            gui.exit();
            return "";
        }
    }
    
    public void set(String confName, String confValue){
        configs.set(configs.indexOf(confName)+1, confValue);
        String configAll = "";
        try {
            for(String line: configs){
                configAll = configAll + line + "\r\n";
            }
            Files.write(configAll , new File(configFile), Charsets.UTF_8);
            
        } catch (IOException e) {
            gui.message("could not write configuration to file: " + e.getMessage());
        }
    }
    
    public boolean isSet(String confName){
        return configs.contains(confName);
    }
    
    
    //Singleton stuff
    public static ChatmanConfig getInstance() {
        return ChatmanConfigHolder.INSTANCE;
    }
    
    
    private static class ChatmanConfigHolder {

        private static final ChatmanConfig INSTANCE = new ChatmanConfig();
    }
    
    
}

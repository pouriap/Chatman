/*
 * Copyright (C) 2016 Pouria Pirhadi
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.pouria.chatman.gui;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.pouria.chatman.Helper;
import com.pouria.chatman.classes.CommandFatalErrorExit;
import com.pouria.chatman.classes.CommandInvokeLater;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 *
 * @author pouriap
 * 
 * a class for setting and getting configurations
 * it's a singleton
 */
public class ChatmanConfig {
    
    private ChatFrame gui;
    private final String configPath = "config.conf";
    private final File configFile = new File(configPath);
    private List<String> defaultConfigs, configs = new ArrayList();
    private final char commentCharacter = '#';
    private boolean hasChanged = false;
    
    private ChatmanConfig(){
		
        this.gui = ChatFrame.getInstance();
        
        //default configs for the occasion that a config was missing
        this.defaultConfigs = Arrays.asList(new String[]{
            "background-image", "batman_1.jpg",
            "server-port", "9988",
            "subnet-mask", "192.168.1.*",
            "num-hosts-to-scan", "100",
            "max-file-size", "20",
            "locale", "fa_IR"
        });
        
		//TODO: create default config file if config file doesn't exist
        //read config file
        //configs are stored line by line. each config value must be the line after config name. otherwise we're screwed.
        try{
            String configLine; 
            BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(configFile), "UTF-8"));
            
            while((configLine = r.readLine()) != null){
                
                //ignore empty lines
                if(configLine.isEmpty())
                    continue;
                
                //ignore comments
                if(configLine.charAt(0) == commentCharacter)
                    continue;

                configs.add(configLine);
                
            }
            
            r.close();
                        
        //we are doomed if we can't read configs
        }catch(Exception e){
            String error = "Failed to read config file. " + e.getMessage();
            (new CommandInvokeLater(new CommandFatalErrorExit(error))).execute();
        }
        
    }

    //gets a config value
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
            String error = "Configuration " + confName + " not in config file";
            (new CommandInvokeLater(new CommandFatalErrorExit(error))).execute();
            return "";
        }
    }
    
    //set a config. when we set a config we also save the config file.
    public void set(String confName, String confValue) {
        
        //if we have this name, set it's value
        if(configs.contains(confName)){
            configs.set(configs.indexOf(confName) + 1, confValue);
        }
        else{
            configs.add(confName);
            configs.add(confValue);
        }
        
        hasChanged = true;
    }
    
    //save to config.conf file
    public void save(){
        
        if(!hasChanged)
            return;
        
        try {
            List<String> configFileLines = Files.readLines(configFile, Charsets.UTF_8);
            
            for(int i=0; i<configs.size(); i++){
                String configName = configs.get(i);
                String configValue = configs.get(i+1);
                i++;
                   
                //if we have this name, sets it's value
                if(configFileLines.contains(configName)){
                    configFileLines.set(configFileLines.indexOf(configName) + 1, configValue);
                }
                else{
                    configFileLines.add(configName);
                    configFileLines.add(configValue);
                } 
            }
            
            //because Files.write() only accepts a String as parameter
            String toSave = "";
            for(String line: configFileLines){
                toSave += line + "\r\n";
            }
            
            Files.write(toSave, configFile, Charsets.UTF_8);
            
        } catch (IOException e) {
            gui.message(Helper.getInstance().getStr("config_write_fail") + e.getMessage());
        }
        
        hasChanged = false;
    }
    
    //do we have this config?
    public boolean isSet(String confName){
        return configs.contains(confName);
    }
    
    //a special treatment for locale
    public Locale getLocale(){
        String[] l = get("locale").split("_");
        return new Locale(l[0], l[1]);
    }
    
    
    //Singleton stuff
    public static ChatmanConfig getInstance() {
        return ChatmanConfigHolder.INSTANCE;
    }
    
    
    private static class ChatmanConfigHolder {

        private static final ChatmanConfig INSTANCE = new ChatmanConfig();
    }
    
    
}

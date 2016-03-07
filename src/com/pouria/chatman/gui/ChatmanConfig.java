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
    
    private ChatmanConfig(){
        this.gui = ChatFrame.getInstance();
        
        //default configs for the occasion that a config was missing
        this.defaultConfigs = Arrays.asList(new String[]{
            "background-image", "none",
            "server-port", "9988",
            "subnet-mask", "192.168.1.*",
            "num-hosts-to-scan", "10",
            "max-file-size", "20",
            "locale", "en_US",
            //"user-id", "Chatman User"
        });
        
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
            gui.message(gui.l.getString("config_read_fail") + e.getMessage());
            gui.exit();
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
            gui.message(gui.l.getString("configuration") + confName + gui.l.getString("is_not_in_config"));
            gui.exit();
            return "";
        }
    }
    
    //set a config. when we set a config we also save the config file.
    public void set(String confName, String confValue) {
        configs.set(configs.indexOf(confName) + 1, confValue);
        String s = "";
        
        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(configFile), "UTF-8"));
            String line;
            
            //read the config file line by line and replace it whenever it differs from out runtime 'configs' array
            //we store all config in 's' then save s to the config file because it's easier
            while ((line = r.readLine()) != null) {

                s += line + "\r\n";

                if (line.isEmpty())
                    continue;
                
                if (line.charAt(0) == commentCharacter)
                    continue;

                //line is not empty and not comment so it is a config name
                //if we have this name in config, change it's value to the one we have in config
                int index = configs.indexOf(line);
                if (index != -1) {
                    //add the value of this config to s
                    s += configs.get(index + 1) + "\r\n";
                    //skip the line in the current config file
                    r.readLine();
                }

            }
            r.close();
            
            Files.write(s, configFile, Charsets.UTF_8);
            
            
        } catch (IOException e) {
            gui.message(gui.l.getString("config_write_fail") + e.getMessage());
        }
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

/*
 * Copyright (C) 2020 pouriap
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
package com.pouria.chatman;

import com.pouria.chatman.commands.CmdFatalErrorExit;
import com.pouria.chatman.commands.CmdInvokeLater;
import com.puria.PoConfig;

import java.io.File;

/**
 *
 * @author pouriap
 */
public class CMConfig {
	
	private final PoConfig config;
	private final File configFile;
	private final String CONFIG_FILE_PATH = "config.conf";
	
	public static final String DEFAULT_BG = "bane_1.jpg";
	public static final String DEFAULT_SERVER_PORT = "20759";
	public static final String DEFAULT_SUBNET = "192.168.1.*";
	public static final String DEFAULT_HOSTS_SCAN = "50";
	public static final String DEFAULT_FILEDROP_SIZEWARNING = "300";
	public static final String DEFAULT_LOCALE = "fa_IR";
	public static final String DEFAULT_SHOWTRAY = "yes";
	public static final String DEFAULT_THEME = "default.cmtheme";
	public static final String DEFAULT_THEMES_DIR = "themes";
	
	
	private CMConfig() {
		
		configFile = new File(CONFIG_FILE_PATH);
		config = new PoConfig(configFile);
		
		try{
			//if there is a config file try to load it
			if(configFile.isFile()){
				config.load();
			}
			//if there isn't a config file try to create it
			else{
				CMHelper.getInstance().log("config file doesn't exist. creating it");
				CMHelper.getInstance().copyFromResources("default_config.conf", new File("config.conf"));
				CMHelper.getInstance().log("default config file created successfully");
				config.load();
			}
		//if load/create fails it's a fatal error
		}catch(Exception e){
			String error = "Failed to read or create config file. " + e.getMessage();
			(new CmdInvokeLater(new CmdFatalErrorExit(error, e))).execute();
		}
		
	}
	
	public static CMConfig getInstance() {
		return ChatmanConfigHolder.INSTANCE;
	}
	
	private static class ChatmanConfigHolder {

		private static final CMConfig INSTANCE = new CMConfig();
	}
	
	public String get(String key, String defaultValue){
		return config.getString(key, defaultValue);
	}
	
	public void set(String key, String value){
		config.set(key, value);
	}
	
	public final void save(){
		try{
			config.save();
		}catch(Exception e){
			e.printStackTrace();
			CMHelper.getInstance().log("failed to save config file");
		}
	}
	
	public boolean isSet(String key){
		return config.isSet(key);
	}
	
}

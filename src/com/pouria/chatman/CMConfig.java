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

import com.pouria.chatman.classes.CmdFatalErrorExit;
import com.pouria.chatman.classes.CmdInvokeLater;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Locale;
import java.util.Properties;

/**
 *
 * @author pouriap
 */
public class CMConfig {
	
	private final Properties config;
	private final File configFile;
	private final String CONFIG_FILE_NAME = "config.conf";
	
	public static final String DEFAULT_BG = "bane_1.jpg";
	public static final String DEFAULT_SERVER_PORT = "8645";
	public static final String DEFAULT_SUBNET = "192.168.1.*";
	public static final String DEFAULT_HOSTS_SCAN = "100";
	public static final String DEFAULT_FILEDROP_SIZEWARNING = "50";
	public static final String DEFAULT_LOCALE = "fa_IR";
	public static final String DEFAULT_BUTTONSTHEME = "dark";
	public static final String DEFAULT_TEXTAREASTHEME = "dark";
	
	private final String comments = "rahnama:\n"
			+ "subnet-mask: subneti ke bayad donbale server jostojoo shavad. bayad adade akhare an setare (*) bashad\n"
			+ "num-hosts-to-scan: chand ip scan shavad donbale server\n"
			+ "file-dorp-size-warning: filehaye az in bozorg tar ra bekhaim befrestim warning midahad\n"
			+ "buttons-theme: range dokme ha - 'dark' ya 'light'\n"
			+ "textareas-theme: range poshte neveshte ha - 'dark' ya 'light'"
			+ "\n"
			+ "\n";
	
	
	private CMConfig() {
		
		config = new Properties();
		configFile = new File(CONFIG_FILE_NAME);
		
		try{
			//if there is a config file try to load it
			if(configFile.isFile()){
				config.load(new FileInputStream(configFile));
			}
			//if there isn't a config file try to create it
			else{
				CMHelper.getInstance().log("config file doesn't exist. creating it");
				setAsDefault();
				configFile.createNewFile();
				save();
				CMHelper.getInstance().log("default config file created successfully");
			}
		//if load/create fails it's a fatal error
		}catch(Exception e){
			final Exception ex = e;
			String error = "Failed to read or create config file. " + e.getMessage();
			(new CmdInvokeLater(new CmdFatalErrorExit(error, ex))).execute();
		}
		
	}
	
	public static CMConfig getInstance() {
		return ChatmanConfigHolder.INSTANCE;
	}
	
	private static class ChatmanConfigHolder {

		private static final CMConfig INSTANCE = new CMConfig();
	}
	
	private void setAsDefault(){
		config.setProperty("background-image", DEFAULT_BG);
		config.setProperty("server-port", DEFAULT_SERVER_PORT);
		config.setProperty("subnet-mask", DEFAULT_SUBNET);
		config.setProperty("num-hosts-to-scan", DEFAULT_HOSTS_SCAN);
		config.setProperty("file-drop-size-warning", DEFAULT_FILEDROP_SIZEWARNING);		
		config.setProperty("buttons-theme", DEFAULT_BUTTONSTHEME);
		config.setProperty("textareas-theme", DEFAULT_TEXTAREASTHEME);
	}
	
	public String get(String key, String defaultValue){
		return config.getProperty(key, defaultValue);
	}
	
	public void set(String key, String value){
		config.setProperty(key, value);
	}
	
	public final void save(){
		try{
			config.store((new FileOutputStream(configFile)), comments);
		}catch(Exception e){
			CMHelper.getInstance().log("failed to save config file");
		}
	}
	
	public boolean isSet(String key){
		return config.stringPropertyNames().contains(key);
	}
	
	public Locale getLocale(){
		String[] l = get("locale", "fa_IR").split("_");
        return new Locale(l[0], l[1]);
    }
}

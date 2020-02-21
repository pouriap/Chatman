/*
 * Copyright (C) 2019 pouriap
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

import com.pouria.chatman.classes.ResourceBundleWrapper;
import java.io.IOException;
import java.util.Locale;

/**
 *
 * @author pouriap
 */
public class Helper {
	
	private ResourceBundleWrapper l;
	
	public final int OS_WIN = 0, OS_LIN = 1;
	
	private Helper() {
	}
	
	public static Helper getInstance() {
		return HelperHolder.INSTANCE;
	}
	
	private static class HelperHolder {

		private static final Helper INSTANCE = new Helper();
	}
	
	public void setLocale(Locale locale) throws Exception{
        l = new ResourceBundleWrapper("resources.locale.locale", locale);
    }
	
	public String getStr(String str){
		return l.getString(str);
	}
	
	public void localShutdown() throws IOException{
		if(getOS() == OS_WIN){
			Runtime.getRuntime().exec("shutdown /s /f /t 100");
		}
		else{
			Runtime.getRuntime().exec("shutdown +1");
		}
	}
	
	public void abortLocalShutdown() throws IOException{
		if(getOS() == OS_WIN){
			Runtime.getRuntime().exec("shutdown /a");
		}
		else{
			Runtime.getRuntime().exec("shutdown -c");
		}
	}
	
	public void sendWakeOnLan(String remoteIp) throws IOException{
		throw new UnsupportedOperationException("not supported yet");
		
		//Usage: wolcmd [mac address] [ipaddress] [subnet mask] [port number]
		//Runtime.getRuntime().exec("wolcmd 9C5C8E719827 192.168.2.21 255.255.255.0");
	}
	
	public int getOS(){
		String os = System.getProperty("os.name");
		os = os.toLowerCase();
		if(os.contains("windows")){
			return OS_WIN;
		}
		else{
			return OS_LIN;
		}
	}
	
	public int getTime(){
		return (int) (System.currentTimeMillis() / 1000L);
	}
}

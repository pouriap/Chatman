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

import com.pouria.chatman.classes.CmdFatalErrorExit;
import com.pouria.chatman.classes.CmdInvokeLater;
import com.pouria.chatman.classes.CmdShowError;
import com.pouria.chatman.classes.ResourceBundleWrapper;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Locale;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 *
 * @author pouriap
 */
public class CMHelper {
	
	private ResourceBundleWrapper l;
	private Logger logger;
	
	public final int OS_WIN = 0, OS_LIN = 1;
	
	private CMHelper() {
	}
	
	public static CMHelper getInstance() {
		return HelperHolder.INSTANCE;
	}
	
	private static class HelperHolder {
		private static final CMHelper INSTANCE = new CMHelper();
	}
	
	public void setLocale(Locale locale){
		try{
			l = new ResourceBundleWrapper("resources.locale.locale", locale);
		}catch(Exception e){
			final Exception ex = e;
			String error = "Could not get locale";
			(new CmdInvokeLater(new CmdFatalErrorExit(error, ex))).execute();
		}
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
	
	public long getTime(){
		return System.currentTimeMillis();
	}
	        
	/**
	 * Finds local ip addresses
	 * @return list of all ip addressed assigned to this machine
	 */
	public ArrayList<String> getLocalIps(){
		
        final ArrayList<String> localIps = new ArrayList<String>();

		try{
			Enumeration<NetworkInterface> n = NetworkInterface.getNetworkInterfaces();
			while(n.hasMoreElements())
			{
				NetworkInterface e = n.nextElement();
				Enumeration<InetAddress> a = e.getInetAddresses();
				while(a.hasMoreElements())
				{
					InetAddress addr = a.nextElement();
					String ip = addr.getHostAddress();
					localIps.add(ip);
				}
			}
		}catch(Exception e){
			final Exception ex = e;
			String error = "Could not get local IPs";
			(new CmdInvokeLater(new CmdFatalErrorExit(error, ex))).execute();
		}

		if(!localIps.contains("192.168.1.20") && CMConfig.getInstance().isSet("puria-debug")){
			String ips = "";
			for(String ip: localIps){
				ips += ip+"\n";
			}
			log("local IPs doesn't include 192.168.1.20");
			log("local IPs: \n" + ips);
			(new CmdInvokeLater(new CmdShowError("ips doesn't include 1.20"))).execute();
		}
		
		return localIps;
	}
	
	public void checkDatabaseFile() throws Exception{
		
		File dbFile = new File("history.sqlite");
		if(dbFile.isFile()){
			return;
		}

		log("history.sqlite doesn't exist. creating it");
		dbFile.createNewFile();
		log("history.sqlite created successfully");
		
		log("creating database tables");
		Connection con = DriverManager.getConnection("jdbc:sqlite:history.sqlite");
		Statement stmt = con.createStatement();
		String query = "CREATE TABLE IF NOT EXISTS chat_sessions (id INTEGER PRIMARY KEY ASC AUTOINCREMENT UNIQUE NOT NULL, date INTEGER UNIQUE NOT NULL, text VARCHAR NOT NULL)";
		stmt.execute(query);
		
		stmt.close();
		con.close();
		log("tables created succesffully");

	}
	
	public synchronized void log(String msg){

		if(logger == null){
			logger = Logger.getLogger("ChatmanLog");
			FileHandler fh;
			try {
				int logSizeLimit = 1000 * 1000 * 1;	//1MB
				fh = new FileHandler("log_%g.txt", logSizeLimit, 2, true);
				logger.addHandler(fh);
				SimpleFormatter formatter = new SimpleFormatter();
				fh.setFormatter(formatter);
			}catch(Exception e){
				//if logger doesn't work then fuck it
				e.printStackTrace();
			}
		}
		
		logger.info(msg);
	}
	
}

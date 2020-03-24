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

import com.pouria.chatman.commands.CmdFatalErrorExit;
import com.pouria.chatman.commands.CmdInvokeLater;
import com.pouria.chatman.enums.CMOS;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.ResourceBundle;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 *
 * @author pouriap
 */
public class CMHelper {
	
	private Logger logger;
	private ResourceBundle bundle = ResourceBundle.getBundle("com.pouria.chatman.gui.locale");

	private CMHelper() {
	}
	
	public static CMHelper getInstance() {
		return HelperHolder.INSTANCE;
	}
	
	private static class HelperHolder {
		private static final CMHelper INSTANCE = new CMHelper();
	}

	public String getStr(String key){
		try{
			return bundle.getString(key);
		}catch(Exception e){
			CMHelper.getInstance().log("locale key " + key + " not defined: " + e.getMessage());
			return "locale key \"" + key + "\" not defined";
		}
	}
	
	public void localShutdown() throws IOException{
		if(getOS() == CMOS.WINDOWS){
			Runtime.getRuntime().exec("shutdown /s /f /t 100");
		}
		else{
			Runtime.getRuntime().exec("shutdown +1");
		}
	}
	
	public void abortLocalShutdown() throws IOException{
		if(getOS() == CMOS.WINDOWS){
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
	
	public CMOS getOS(){
		String os = System.getProperty("os.name");
		os = os.toLowerCase();
		if(os.contains("windows")){
			return CMOS.WINDOWS;
		}
		else{
			return CMOS.LINUX;
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
	
	public void createFile(File file) throws Exception{
		createFile(file, null);
	}
	
	public void createFile(File file, byte[] data) throws Exception{
		Path parent = file.toPath().getParent();
		if(parent != null){
			File parentDir = file.toPath().getParent().toFile();
			if(!parentDir.isDirectory()){
				parentDir.mkdirs();
			}
		}
		file.createNewFile();
		if(data != null){
			Files.write(file.toPath(), data, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
		}
	}

	public void copyFromResources(String resourceName, File dstFile) throws Exception{
		CMHelper.getInstance().log("copying file from jar: " + resourceName);
		InputStream in = getClass().getResourceAsStream("/resources/" + resourceName);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buff = new byte[1024];
		int len = 0;
		while((len = in.read(buff)) > 0){
			out.write(buff, 0, len);
		}
		in.close();
		createFile(dstFile, out.toByteArray());
		CMHelper.getInstance().log("file copied successfully ");
	}
	
	public static byte[] readStreamAsByteArray(InputStream in) throws IOException{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int len = 0;
		byte[] buff = new byte[2048];
		while((len = in.read(buff)) > 0){
			out.write(buff, 0, len);
		}
		return out.toByteArray();
	}

	public String getCMDownloadsDir(){
		return (new JFileChooser()).getFileSystemView().getDefaultDirectory().toString() + "\\Chatman Downloads\\";
	}
	
	public ImageIcon applyOpacity(ImageIcon img, float opacity){
		
		try{
			
			int width = img.getIconWidth();
			int height = img.getIconHeight();
			BufferedImage transparentImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = (Graphics2D) transparentImage.createGraphics();
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
			g.drawImage(img.getImage(), 0, 0, width, height, null);
			g.dispose();
		
			return new ImageIcon(transparentImage);
			
		}catch(Exception e){
			CMHelper.getInstance().log("failed to apply transparency to image: " + img.toString());
			return img;
		}
		
	}

}

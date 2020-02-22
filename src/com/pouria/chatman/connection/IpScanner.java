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
package com.pouria.chatman.connection;

import com.pouria.chatman.Helper;
import com.pouria.chatman.classes.CommandInvokeLater;
import com.pouria.chatman.classes.CommandFatalErrorExit;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import com.pouria.chatman.classes.IpScannerCallback;

/**
 *
 * @author pouriap
 * 
 * is only used when we are client
 * takes a subnet mask and scans that subnet
 * num-hosts-to-scan in the config file determines how many hosts in the subnet should we scan
 * when scanning is finished we decide what to do based on number of servers found
 */

public class IpScanner implements Runnable {

    private final String[] ipsToScan;
    private final int port;
	private final IpScannerCallback callback;
	private final ArrayList<String> foundIps;
	   
    public IpScanner(String[] ipsToScan, int port, IpScannerCallback callback){
        this.port = port;
		this.ipsToScan = ipsToScan;
		this.callback = callback;
		foundIps = new ArrayList<String>();
    }

	//start scanning the network
	//we will spawn threads that each one will try to connect to an ip
	//when a live server is detected we call ChatmanClient.setServer() and break operation
    @Override
    public void run() {
		
			int numHosts = ipsToScan.length;
            Thread[] scanners = new Thread[numHosts];
			String localIp = Helper.getInstance().getLocalIp();
			
            for(int i=0; i<numHosts; i++){
                String addr = ipsToScan[i];
                //don't scan local ip
                if(addr.equals(localIp))
                    continue;
                scanners[i] = new Thread(new portScanner(addr, port));
                scanners[i].start();
            }
            
            //wait until the scanning is finished
            boolean c;
            do{
                c = false;
				
                try{
                    Thread.sleep(100);
                }catch(InterruptedException e){
					String error = Helper.getInstance().getStr("thread_sleep_fail");
                    (new CommandInvokeLater(new CommandFatalErrorExit(error))).execute();
                }
				
                //break if all threads are finished
                for(Thread scanner: scanners){
                    if(scanner != null){
						//ooni ke IP local bood va continue dade budim null ast
						c = c || scanner.isAlive();
					}
                }
				
				//break if server is found
				if(!foundIps.isEmpty()){
					c = false;
				}
				
            }while(c);  
            
            //now we have finished scanning the network for live servers
			callback.call(foundIps);
			
    }
	
	private class portScanner implements Runnable{

		private final String ip;
		private final int port;

		public portScanner(String host, int port){
			this.port = port;
			this.ip = host;
		}

		@Override
		public void run() {
			//connect to a specific ip
			try{
				Socket socket = new Socket();
				socket.connect(new InetSocketAddress(ip, port), 2000);
				socket.close();
				foundIps.add(ip);
			}catch(IOException ex){
				//ignore closed ports
			}
		}
	}

	
}

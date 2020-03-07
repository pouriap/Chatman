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

import com.pouria.chatman.CMHelper;
import com.pouria.chatman.classes.CmdInvokeLater;
import com.pouria.chatman.classes.CmdFatalErrorExit;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;

/**
 *
 * @author pouriap
 * 
 * is only used when we are client
 * takes a subnet mask and scans that subnet
 * num-hosts-to-scan in the config file determines how many hosts in the subnet should we scan
 * when scanning is finished we decide what to do based on number of servers found
 */

public class IpScanner {

    private final String[] ipsToScan;
    private final int port;
	private final ArrayList<String> foundIps;
	   
    public IpScanner(String[] ipsToScan, int port){
        this.port = port;
		this.ipsToScan = ipsToScan;
		foundIps = new ArrayList<String>();
    }

	//start scanning the network
	//we will spawn threads that each one will try to connect to an ip
	//when a live server is detected we call ChatmanClient.setServer() and break operation
	//this method is blocking!
    public ArrayList<String> scan() {
		
			int numHosts = ipsToScan.length;
            Thread[] scanners = new Thread[numHosts];
			ArrayList<String> localIps = CMHelper.getInstance().getLocalIps();
			
            for(int i=0; i<numHosts; i++){
                String addr = ipsToScan[i];
                //don't scan local ips
                if(localIps.contains(addr))
                    continue;
                scanners[i] = new Thread(new portScanner(addr, port), "CM-Port-Scanner-"+String.valueOf(i));
                scanners[i].start();
            }
            
            //wait until the scanning is finished
            boolean c;
            do{
                c = false;
				
                try{
                    Thread.sleep(100);
                }catch(InterruptedException e){
					final Exception ex = e;
					String error = CMHelper.getInstance().getStr("thread_sleep_fail");
                    (new CmdInvokeLater(new CmdFatalErrorExit(error, ex))).execute();
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
            
			return foundIps;
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

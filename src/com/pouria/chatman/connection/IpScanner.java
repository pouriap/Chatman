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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author pouriap
 * 
 * takes a range of IPs and scans them
 * when scanning is finished we return found IPs
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

	/**
	 * scan the provided IPs<br>
	 * we will spawn threads that each one will try to connect to an IP<br>
	 * this method should be blocking!!
	 * @return 
	 */
    public ArrayList<String> scan() {
		
			int numHosts = ipsToScan.length;
			ArrayList<String> localIps = CMHelper.getInstance().getLocalIps();
			
			//create a pool of 10 threads
			ExecutorService executor = Executors.newFixedThreadPool(10, new ThreadFactory() {
				//this shit is only for giving the threads a name hehehe
				private final AtomicInteger counter = new AtomicInteger(0);
				@Override
				public Thread newThread(Runnable r) {
					Thread th = new Thread(r, "IP-Scanner-" + counter.incrementAndGet());
					return th;
				}
			});

			//add PortScanner jobs to the executor
		    for (String addr : ipsToScan) {
			    //don't scan local ips
			    if (localIps.contains(addr))
				    continue;
			    Runnable scanner = new PortScanner(addr, port);
			    executor.execute(scanner);
		    }
			
			executor.shutdown();
			//keep waiting until either all hosts are scanned or a server is found
			while(!executor.isTerminated() && foundIps.isEmpty()){
				try{
					Thread.sleep(100);
				}catch(InterruptedException e){
					String error = CMHelper.getInstance().getStr("thread_sleep_fail");
						(new CmdInvokeLater(new CmdFatalErrorExit(error, e))).execute();
				}
			}
			//forced shutdown in for when loop stops because ip was found
			executor.shutdownNow();
            
			return foundIps;
    }
	
	private synchronized void addToFoundIps(String ip){
		foundIps.add(ip);
	}
	
	
	
	private class PortScanner implements Runnable{

		private final String ip;
		private final int port;

		public PortScanner(String host, int port){
			this.port = port;
			this.ip = host;
		}
		
		@Override
		public String toString(){
			return ip;
		}

		@Override
		public void run() {
			//connect to a specific ip
			try{
				Socket socket = new Socket();
				socket.connect(new InetSocketAddress(ip, port), 100);
				String localIP = socket.getLocalAddress().getHostAddress();
				socket.close();
				//don't add ourself as server
				if(!ip.equals(localIP)){
					addToFoundIps(ip);
				}
				else{
					CMHelper.getInstance().log("rejecting self-found server with IP: " + ip);
				}
			}catch(IOException ex){
				//ignore closed ports
			}
		}
	}

	
}

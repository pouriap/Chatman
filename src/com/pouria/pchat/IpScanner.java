/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pouria.pchat;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 *
 * @author PouriaP
 */
public class IpScanner implements Runnable {

    ChatFrame gui;
    String subnet;
    int port;
    
    IpScanner(ChatFrame g, String s, int p){
        this.gui = g;
        this.port = p;
        this.subnet = s;
    }

    @Override
    public void run() {
        //find local computer's IP and scan the subnet except the local IP
        //find local ip address
        String sub = subnet.replace(".*","");
        String localIp = null;
        int numHosts = Integer.parseInt(gui.getConfig("num-hosts-to-scan"));
        try{
            Enumeration<NetworkInterface> n = NetworkInterface.getNetworkInterfaces();
            while(n.hasMoreElements())
            {
                NetworkInterface e = n.nextElement();
                Enumeration<InetAddress> a = e.getInetAddresses();
                while(a.hasMoreElements())
                {
                    InetAddress addr = a.nextElement();
                    if(addr.getHostAddress().contains(sub)){
                        localIp = addr.getHostAddress();
                        break;
                    }
                }
            }
        }catch(SocketException e){
            gui.message("could not find local computer's ip");
            gui.exit();
        }
        if(localIp != null){
            //start scanning the network
            Thread[] scanners = new Thread[numHosts];
            for(int i=1, j=0;i<numHosts+1;i++,j++){
                String addr = subnet.replace("*", String.valueOf(i));
                //don't scan local ip
                if(addr.equals(localIp))
                    continue;

                scanners[j] = new Thread(new IpConnector(gui, addr, port, false));
                scanners[j].start();
            }
            //wait until the scanning is finished
            boolean c;
            do{
                c = false;
                try{
                    Thread.sleep(3000);
                }catch(InterruptedException e){
                    gui.message("could not wait for threads");
                }
                for(Thread scanner: scanners){
                    //ooni ke IP local bood va continue dade budim null ast
                    if(scanner != null)
                        c = c || scanner.isAlive();
                }
            }while(c);  
            //if serversocket is not set after all threads are finished means we have not found a server
            if(!gui.isServerSocketSet())
                gui.connect(true);
        }
        else{
            gui.message("could not find local computer's ip");
            gui.exit();
        }
    }

}

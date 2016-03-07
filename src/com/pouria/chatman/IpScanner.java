/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pouria.chatman;

import com.pouria.chatman.classes.CommandClientConnect;
import com.pouria.chatman.classes.CommandConfirmDialog;
import com.pouria.chatman.classes.CommandInvokeLater;
import com.pouria.chatman.classes.CommandMessage;
import com.pouria.chatman.classes.CommandSetLabelStatus;
import com.pouria.chatman.classes.CommandShowServerList;
import com.pouria.chatman.gui.ChatmanConfig;
import com.pouria.chatman.gui.ChatFrame;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

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

    private final ChatFrame gui;
    private final String subnet;
    private final int port;
    
    IpScanner(String s, int p){
        this.gui = ChatFrame.getInstance();
        this.port = p;
        this.subnet = s;
    }

    @Override
    public void run() {
        //find local computer's IP and scan the subnet except the local IP
        //find local ip address
        String sub = subnet.replace(".*","");
        String localIp = null;
        int numHosts = Integer.parseInt(ChatmanConfig.getInstance().get("num-hosts-to-scan"));
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
            (new CommandInvokeLater(new CommandMessage(gui.l.getString("local_ip_fail")))).execute();
            gui.exit();
        }
        if(localIp != null){
            //start scanning the network
            //we will spawn threads that each one will try to connect to an ip
            //when a live server is detected it is added to liveserverlist
            //when we are done it is decided what to do
            Thread[] scanners = new Thread[numHosts];
            for(int i=1, j=0;i<numHosts+1;i++,j++){
                String addr = subnet.replace("*", String.valueOf(i));
                
                //don't scan local ip
                if(addr.equals(localIp))
                    continue;

                scanners[j] = new Thread(new IpConnector(addr, port, false));
                scanners[j].start();
            }
            
            //wait until the scanning is finished
            boolean c;
            do{
                c = false;
                try{
                    Thread.sleep(3000);
                }catch(InterruptedException e){
                    (new CommandInvokeLater(new CommandMessage(gui.l.getString("thread_sleep_fail")))).execute();
                }
                //keep sleeping if we have live threads
                for(Thread scanner: scanners){
                    //ooni ke IP local bood va continue dade budim null ast
                    if(scanner != null)
                        c = c || scanner.isAlive();
                }
            }while(c);  
            
            //now we have finished scanning the network for live servers
            int foundServers = ((ChatmanClient)gui.getChatmanInstance()).numServersFound();
            (new CommandInvokeLater(new CommandSetLabelStatus(foundServers + gui.l.getString("servers_found")))).execute();
            
            if(foundServers == 0){
                (new CommandInvokeLater(new CommandConfirmDialog(
                        new CommandClientConnect(),
                        gui.l.getString("server_retry_confirm"),
                        gui.l.getString("server_not_found")
                ))).execute();

            }
            else if (foundServers == 1){
                //if only one server is found don't show the list, connect to it
                ((ChatmanClient)gui.getChatmanInstance()).setServerSocket(0);
                ((ChatmanClient)gui.getChatmanInstance()).start();
            }
            else{
                (new CommandInvokeLater(new CommandShowServerList())).execute();

            }

        }
        else{
            (new CommandInvokeLater(new CommandMessage(gui.l.getString("local_ip_fail")))).execute();
            gui.exit();
        }
    }

}

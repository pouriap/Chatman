/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pouria.chatman.gui;

import com.google.common.io.Files;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author pouriap
 */
public class Background {
    
    ArrayList<File> backgrounds = new ArrayList<File>();
    private File current;
    private final String bgFolder = "backgrounds";
    
    private Background(){
        File bgsPath;
        File[] _backgrounds;
        
        try{
            bgsPath = new File(bgFolder);
            
            //first add from background folder if exists
            if(bgsPath.exists()){
                _backgrounds = bgsPath.listFiles(new JpegFileFilter());
                if(_backgrounds.length > 0){
                    backgrounds.addAll(Arrays.asList(_backgrounds));
                }
            }

            //then add from resources
            bgsPath = new File(getClass().getResource("/resources/bg").toURI());                
            _backgrounds = bgsPath.listFiles();
            backgrounds.addAll(Arrays.asList(_backgrounds));
            
            //then initialize current bg
            current = new File(ChatmanConfig.getInstance().get("background-image"));
            
        }catch(Exception e){
            //i know. i just don't care
        }

    }
    
    public void next(){      
        int nextIndex = backgrounds.indexOf(current) + 1;
        current = (nextIndex < backgrounds.size())? backgrounds.get(nextIndex) : backgrounds.get(0);
        ChatmanConfig.getInstance().set("background-image", current.getPath());
    }
    
    public String getCurrent(){
        return current.getPath();
    }

    
    //Singleton stuff
    public static Background getInstance() {
        return BackgroundHolder.INSTANCE;
    }
    
    
    private static class BackgroundHolder {

        private static final Background INSTANCE = new Background();
    }
    
    //only accepts jpeg files
    private class JpegFileFilter implements FilenameFilter{
        
        @Override
        public boolean accept(File dir, String fileName){
            String ext = Files.getFileExtension(fileName);
            if(ext.equals("jpg") || ext.equals("png"))
                return true;
            
            return false;
        }
    }
    
}

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
package com.pouria.chatman.gui;

import com.pouria.chatman.CMConfig;
import com.google.common.io.Files;
import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author pouriap
 * 
 * this class is responsible for loading and changing backgrounds
 * it's a singleton
 */
public class Background {
    //TODO: new backgrounds aren't added until we restart the app
    ArrayList<String> backgrounds = new ArrayList<String>();
    private String current;
    private final String bgFolder = "backgrounds";
    private final String defaultBg = "batman.jpg";
    
    private Background(){
        File bgsPath;
        String [] _backgrounds;
        
        try{
            bgsPath = new File(bgFolder);
            //first add from background folder
            if(bgsPath.exists()){
                _backgrounds = bgsPath.list(new imgFilenameFilter());
                if(_backgrounds.length > 0){
                    backgrounds.addAll(Arrays.asList(_backgrounds));
                }
            }
            //then initialize current bg
            current = CMConfig.getInstance().get("background-image", CMConfig.DEFAULT_BG);

        }catch(Exception e){
            //i know. i just don't care
        }

    }
    
    //sets 'current' to next background in the list
    //changes the config accordingly
    public void next(){     
        if(backgrounds.isEmpty())
            return;
        
        int nextIndex = backgrounds.indexOf(current) + 1;
        current = (nextIndex < backgrounds.size())? backgrounds.get(nextIndex) : backgrounds.get(0);
        CMConfig.getInstance().set("background-image", current);
		CMConfig.getInstance().save();
    }
	
	public void prev(){
        if(backgrounds.isEmpty())
            return;
        
        int prevIndex = backgrounds.indexOf(current) - 1;
        current = (prevIndex < 0)? backgrounds.get(backgrounds.size()-1) : backgrounds.get(prevIndex);
        CMConfig.getInstance().set("background-image", current);
		CMConfig.getInstance().save();
	}

    //gets the filename of the current background
    public String getCurrent(){
        File currentBgFile = new File(bgFolder + "/" + current);
        if(!currentBgFile.exists())
            current = defaultBg;
        
        return current;
    }

    //gets the URL of the current background
    public URL getCurrentURL(){
        File currentBgFile = new File(bgFolder + "/" + current);
        if(!currentBgFile.exists()){
            current = defaultBg;
            return getClass().getResource("/resources/bg/" + defaultBg);
        }
        
        try{
            return currentBgFile.toURI().toURL();
        }catch(MalformedURLException e){
            return getClass().getResource("/resources/bg/" + defaultBg);
        }
    }
    
    
    
    //Singleton stuff
    public static Background getInstance() {
        return BackgroundHolder.INSTANCE;
    }
    
    
    private static class BackgroundHolder {

        private static final Background INSTANCE = new Background();
    }
    
    
    //only accepts jpeg and png files
    private class imgFilenameFilter implements FilenameFilter{
        
        @Override
        public boolean accept(File dir, String fileName){
            String ext = Files.getFileExtension(fileName);
            if(ext.equals("jpg") || ext.equals("png"))
                return true;
            
            return false;
        }
    }
    
}

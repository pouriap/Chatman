/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pouria.pchat;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author SH
 */
public class Background {
    
    ArrayList<String> backgrounds;
    private String current;
    private final String bgFolder = "/resources/bg";
    
    private Background(){
        File bgsPath;
        
        try{
            bgsPath = new File(getClass().getResource(bgFolder).toURI());
            backgrounds = new ArrayList(Arrays.asList(bgsPath.list()));
            current = ChatmanConfig.getInstance().get("background-image");
        }catch(Exception e){
            //don't worry
        }

    }
    
    public URL next(){
        int nextIndex = backgrounds.indexOf(current) + 1;
        current = (nextIndex < backgrounds.size())? backgrounds.get(nextIndex) : backgrounds.get(0);
        ChatmanConfig.getInstance().set("background-image", current);
        return getCurrent();
    }
    
    public URL getCurrent(){
        return getClass().getResource(bgFolder + "/" + current);
    }
    
    
    //Singleton stuff
    public static Background getInstance() {
        return BackgroundHolder.INSTANCE;
    }
    
    
    private static class BackgroundHolder {

        private static final Background INSTANCE = new Background();
    }
}

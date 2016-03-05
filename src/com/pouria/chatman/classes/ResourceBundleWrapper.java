/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pouria.chatman.classes;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 *
 * @author pouriap
 * This class wraps ResourceBundle and Properties classes
 * Lets the user specify her own locale file
 * If locale is not found in the application/locale directory then locale is loaded from JAR
 * If no such locale is available en_US is loaded
 */
public class ResourceBundleWrapper {
    
    ResourceBundle bundle = null;
    Properties props = null;
    
    public ResourceBundleWrapper(String path, Locale locale) throws Exception{

        //first look in files
        //it gives the user ability to overwrite our locale
        try{
            props = new Properties();
            InputStreamReader r;

            File f = new File("locale/locale_" + locale.toString() + ".properties");
            r = new InputStreamReader(new FileInputStream(f), "UTF-8");
            props.load(r);

        }catch(Exception e){
            props = null; 
        }
        
        //not such file? look in jar
        if(props == null){
            try{
                //loads english if no such locale is found
                //throws exception if path is wrong
                bundle = ResourceBundle.getBundle(path, locale);
            }catch(Exception e){
                bundle = null;
                throw new Exception("No such locale in 'locale' folder or in application jar file");
            }
        }
    
    }
        
   
    public String getString(String key){
        if(bundle != null)
            return bundle.getString(key);
        
        return props.getProperty(key);
    }
}

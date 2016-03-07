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
 * 
 * this class wraps ResourceBundle and Properties classes
 * lets the user specify her own locale file
 * if locale is not found in the application/locale directory then locale is loaded from JAR
 * if no such locale is available en_US is loaded
 */
public class ResourceBundleWrapper {
    
    private ResourceBundle bundle = null;
    private Properties props = null;
    
    private final String localeFolder = "locale";
    
    
    public ResourceBundleWrapper(String path, Locale locale) throws Exception{

        //first look in files
        //it gives the user ability to overwrite our locale
        try{
            props = new Properties();
            InputStreamReader r;

            File f = new File(localeFolder + "/locale_" + locale.toString() + ".properties");
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

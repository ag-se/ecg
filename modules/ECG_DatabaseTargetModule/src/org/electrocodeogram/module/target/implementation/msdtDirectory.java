package org.electrocodeogram.module.target.implementation;
import java.io.File;

/**
 * 
 * @author jule
 * @version 1.0
 */
public class msdtDirectory {

    public static String[] getSchemes (File f){
    
       if (f.isDirectory())
       {  
          // der Ordnerinhalt als Array von Strings 
          String[] msdtSchemes = f.list();
          return msdtSchemes;
       }
       else
       {  
          return null;
       }
    }
}



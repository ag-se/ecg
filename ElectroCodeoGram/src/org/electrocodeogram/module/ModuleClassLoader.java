package org.electrocodeogram.module;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/*
 * Created on 11.03.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

/**
 * @author 7oas7er
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ModuleClassLoader extends java.lang.ClassLoader
{
    public ModuleClassLoader(ClassLoader cl)
    {
        super(cl);
    }
    
//    public Class loadClass(String name) throws ClassNotFoundException
//    {
//        return loadClass(name,true);
//    }
    
    protected Class findClass(String name) throws ClassNotFoundException
    {
        File classFile = new File(name);
        if(classFile.exists() && classFile.isFile())
        {
            FileInputStream fis;
            try {
                fis = new FileInputStream(classFile);
            }
            catch (FileNotFoundException e1) {
                throw new ClassNotFoundException();
            }
          
            byte[] data = new byte[(int)classFile.length()];
            
            try {
                fis.read(data);
                Class toReturn = defineClass(null,data,0,data.length);
                return toReturn;
                
            }
            catch (IOException e) {
                throw new ClassNotFoundException();
            }
            
        }
        else
        {
            throw new ClassNotFoundException();
        }
    }
}

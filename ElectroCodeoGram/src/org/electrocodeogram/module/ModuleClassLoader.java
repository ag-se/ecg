package org.electrocodeogram.module;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.logging.Level;

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
   
    private Logger logger = Logger.getLogger(this.getClass().getName());
    
    public ModuleClassLoader(ClassLoader cl)
    {
        super(cl);
    }
    
    protected Class findClass(String name) throws ClassNotFoundException
    {
        Class toReturn = null;
        
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
                
                toReturn = defineClass(null,data,0,data.length);
                
                logger.log(Level.INFO,"Succesfully loaded module class: " + classFile.getName());
            }
            catch (IOException e) {
                throw new ClassNotFoundException();
            }
            
            File moduleDir = classFile.getParentFile();
            
            assert(moduleDir.exists());
            
            assert(moduleDir.isDirectory());
            
            FilenameFilter filter = new FilenameFilter() {
                public boolean accept(File dir, String name)
                {
                    if(name.endsWith(".class"))
                    {
                        return true;
                    }
                    else
                    {
                        return false;
                    }
                }
            };
            
            String[] files = moduleDir.list(filter);
            
            assert(files != null);
            
            for(int i=0;i<files.length;i++)
            {
                File file = new File(moduleDir + File.separator + files[i]);
                
                if(!file.equals(classFile))
                {
                    try {
                        fis = new FileInputStream(file);
                    }
                    catch (FileNotFoundException e1) {
                        throw new ClassNotFoundException();
                    }
                  
                    data = new byte[(int)file.length()];
                    
                    try {
                        fis.read(data);
                        
                        defineClass(null,data,0,data.length);
                        
                        logger.log(Level.INFO,"Succesfully loaded additional class: " + file.getName() + " required by module " + classFile.getName());
                    }
                    catch (IOException e) {
                        throw new ClassNotFoundException();
                    }
                }
            }
                        
            return toReturn;            
        }
        else
        {
            throw new ClassNotFoundException();
        }
    }
}

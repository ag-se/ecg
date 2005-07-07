package org.electrocodeogram.module;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * The ModuleClassLoader is able to load classes directly from a
 * given location in the file system, which should be the
 * module directory.
 * So the ModuleClassLoader makes it possible to have modules loaded
 * from locations that are not in the classpath.
 *
 */
public class ModuleClassLoader extends java.lang.ClassLoader
{
   
    private Logger logger = null;
    
    /**
     * This creates theModuleClassLoader and sets the given ClassLoader to be the parent
     * ClassLoader oh the ModulClassLoader in the ClassLoader hierarchy.
     * @param cl
     */
    public ModuleClassLoader(ClassLoader cl)
    {
        super(cl);
        
        this.logger = Logger.getLogger(this.getClass().getName());
    }
    
    @Override
    protected Class<?> findClass(String classFilePath) throws ClassNotFoundException
    {
        Class<?> toReturn = null;
        
        File classFile = new File(classFilePath);
        
        assert(classFile.exists() && classFile.isFile());
        
        FileInputStream fis = null;
            
        try {
        
            fis = new FileInputStream(classFile);
        
        }
        
        catch (FileNotFoundException e1) {
        
            throw new ClassNotFoundException();
            
        }
          
           
        byte[] data = new byte[(int)classFile.length()];
            
     
            try {
                fis.read(data);
            }
            catch (IOException e2) {

                this.logger.log(Level.INFO,"IOException while reading module class: " + classFile.getName());
                
                return null;
            }
                
            
            toReturn = this.defineClass(null,data,0,data.length);
                
            
            this.logger.log(Level.INFO,"Succesfully loaded module class: " + classFile.getName());
            
            File moduleDirectory = classFile.getParentFile();
            
            assert(moduleDirectory.exists());
            
            assert(moduleDirectory.isDirectory());
            
            FilenameFilter filter = new FilenameFilter() {
                
                public boolean accept(@SuppressWarnings("unused") File dir, String name)
                {
                    if(name.endsWith(".class"))
                    {
                        return true;
                    }
                   
                    return false;
                   
                }
            };
            
            String[] files = moduleDirectory.list(filter);
            
            for(int i=0;i<files.length;i++)
            {
                File file = new File(moduleDirectory + File.separator + files[i]);
                
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
                        
                        this.logger.log(Level.INFO,"Succesfully loaded additional class: " + file.getName() + " required by module " + classFile.getName());
                    }
                    catch (IOException e) {
                        throw new ClassNotFoundException();
                    }
                }
            }
                        
            return toReturn;            
        
        
    }
}

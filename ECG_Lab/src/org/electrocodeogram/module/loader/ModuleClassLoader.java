package org.electrocodeogram.module.loader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;
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

    private ArrayList<String> $moduleClassPaths;

    /**
     * This creates the ModuleClassLoader and sets the given ClassLoader to be the parent
     * ClassLoader oh the ModuleClassLoader in the ClassLoader hierarchy.
     * @param cl Is the parent ClassLoader
     */
    public ModuleClassLoader(ClassLoader cl)
    {
        super(cl);

        this.logger = Logger.getLogger(this.getClass().getName());
    }

    /**
     * @see java.lang.ClassLoader#findClass(java.lang.String)
     * 
     * This method loads classes from the given path to the file system.
     */
    @Override
    protected Class<?> findClass(String className)
    {
        Class<?> toReturn = null;

        if (this.$moduleClassPaths == null) {
            this.logger.log(Level.SEVERE, "No module class paths defined.");

            return null;
        }

        String normalizedClassName = getNormalizedClassName(className);

        if (normalizedClassName == null) {
            this.logger.log(Level.SEVERE, "Class path is invalid: " + className);

            return null;
        }

        for (String moduleClassPath : this.$moduleClassPaths) {

            String pathToModuleClass = moduleClassPath + normalizedClassName + ".class";

            File classFile = new File(pathToModuleClass);

            if (!classFile.exists()) {
                
                continue;
            }

            if (!classFile.isFile()) {
                
                continue;
            }

            FileInputStream fis = null;

            try {
                fis = new FileInputStream(classFile);

                byte[] data = new byte[(int) classFile.length()];

                fis.read(data);

                try {
                    toReturn = this.defineClass(null, data, 0, data.length);

                    break;

                }
                catch (LinkageError e) {
                    this.logger.log(Level.INFO, "Linkage error: " + e.getMessage());

                }

                this.logger.log(Level.INFO, "Successfully loaded module class: " + classFile.getName());

            }
            catch (IOException e) {

                this.logger.log(Level.WARNING, "Error while loading module class: " + className);

            }
        }

        if(toReturn == null)
        {
            this.logger.log(Level.WARNING, "The desired class could not be found: " + className);
        }
        
        return toReturn;
    }

    /**
     * @param className
     * @return
     */
    private String getNormalizedClassName(String className)
    {
        StringTokenizer stringTokenizer = new StringTokenizer(className, ".");

        String normalizedClassName = "";

        while (stringTokenizer.hasMoreTokens()) {
            normalizedClassName += File.separator + stringTokenizer.nextToken();

        }

        return normalizedClassName;
    }

    /**
     * @param currentModuleDirectoryPath
     */
    public void addModuleClassPath(String moduleClassPath)
    {
        if (this.$moduleClassPaths == null) {
            this.$moduleClassPaths = new ArrayList<String>();
        }

        this.$moduleClassPaths.add(moduleClassPath);

    }
}

/*
 * Class: ModuleClassLoader
 * Version: 1.0
 * Date: 18.10.2005
 * By: Frank@Schlesinger.com
 */

package org.electrocodeogram.module.classloader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.electrocodeogram.logging.LogHelper;

/**
 * The <em>ModuleClassLoader</em> is able to load <code>Class</code>
 * files directly from a given location in the file system, which is
 * be the module directory.
 */
public class ModuleClassLoader extends java.lang.ClassLoader {

    /**
     * Thisis the logger.
     */
    private static Logger logger = LogHelper
        .createLogger(ModuleClassLoader.class.getName());

    /**
     * This is a list of known <em>ModulePackage</em> pathes.
     */
    private static ArrayList<String> moduleClassPaths;

    /**
     * A refernce to the instance.
     */
    private static ModuleClassLoader theInstance;

    /**
     * This creates the <em>ModuleClassLoader</em> and sets the
     * given <code>ClassLoader</code> to be its parent.
     * @param loader
     *            Is the parent <code>ClassLoader</code>
     */
    public ModuleClassLoader(final ClassLoader loader) {
        super(loader);

        logger.entering(this.getClass().getName(), "ModuleClassLoader",
            new Object[] {loader});

        logger.exiting(this.getClass().getName(), "ModuleClassLoader");
    }

    /**
     * @see java.lang.ClassLoader#findClass(java.lang.String)
     */
    @Override
    protected final Class<?> findClass(final String className) {
        logger.entering(this.getClass().getName(), "findClass",
            new Object[] {className});

        if (className == null) {
            logger.log(Level.WARNING, "The parameter \"className\" is null");

            logger.exiting(this.getClass().getName(), "findClass", null);

            return null;
        }

        Class<?> toReturn = null;

        if (moduleClassPaths == null) {
            logger.log(Level.SEVERE, "No module class paths are defined.");

            logger.exiting(this.getClass().getName(), "findClass", null);

            return null;
        }

        String normalizedClassName = getNormalizedClassName(className);

        if (normalizedClassName == null) {
            logger.log(Level.SEVERE, "Class path is invalid: " + className);
            logger.exiting(this.getClass().getName(), "findClass", null);
            return null;
        }

        File classFile = null;

        for (String moduleClassPath : moduleClassPaths) {

            String pathToModuleClass = moduleClassPath + normalizedClassName
                                       + ".class";

            classFile = new File(pathToModuleClass);

            if (classFile.exists() && classFile.isFile()) {
                logger.log(Level.FINE, "Class is found here: "
                                       + classFile.getAbsolutePath());

                break;
            }
        }

        if (classFile == null) {
            logger.log(Level.SEVERE, "The class could not be found: "
                                     + classFile);
            logger.exiting(this.getClass().getName(), "findClass", null);
            return null;
        }

        FileInputStream fis = null;

        try {
            fis = new FileInputStream(classFile);

            byte[] data = new byte[(int) classFile.length()];

            fis.read(data);

            try {
                toReturn = this.defineClass(null, data, 0, data.length);

            } catch (Error e) {
                logger.log(Level.SEVERE, "Error while loading class: "
                                         + className);

                logger.log(Level.SEVERE, e.getMessage());

            }

            logger.log(Level.INFO, "Successfully loaded module class: "
                                   + classFile.getName());

        } catch (IOException e) {

            logger.log(Level.SEVERE, "Error while loading module class: "
                                     + className);

            logger.log(Level.SEVERE, e.getMessage());

        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error while closing the stream.");
            }
        }

        if (toReturn == null) {
            logger.log(Level.SEVERE, "The class could not be found: "
                                     + className);
        }

        logger.exiting(this.getClass().getName(), "findClass", toReturn);

        return toReturn;
    }

    /**
     * Converts the full qualified class name into a file system path.
     * @param className
     *            The full qualified class name
     * @return A file system path
     */
    private String getNormalizedClassName(final String className) {
        logger.entering(this.getClass().getName(), "getNormalizedClassName",
            new Object[] {className});

        if (className == null) {
            logger.log(Level.WARNING, "The parameter \"className\" is null.");

            logger.exiting(this.getClass().getName(), "getNormalizedClassName");

            return null;
        }

        StringTokenizer stringTokenizer = new StringTokenizer(className, ".");

        String normalizedClassName = "";

        while (stringTokenizer.hasMoreTokens()) {
            normalizedClassName += File.separator + stringTokenizer.nextToken();

        }

        logger.exiting(this.getClass().getName(), "getNormalizedClassName");

        return normalizedClassName;
    }

    /**
     * This method is used by the
     * {@link org.electrocodeogram.module.registry.ModuleRegistry} to
     * add another <em>ModulePackage</em> path to the list.
     * @param moduleClassPath
     *            Is the <em>ModulePackage</em> path to add
     */
    public static final void addClassPath(final String moduleClassPath) {
        logger.entering(ModuleClassLoader.class.getName(),
            "addModuleClassPath", new Object[] {moduleClassPath});

        if (moduleClassPath == null) {
            logger.log(Level.WARNING,
                "The parameter \"moduleClassPath\" is null.");
            logger.exiting(ModuleClassLoader.class.getName(),
                "addModuleClassPath");
            return;
        }

        if (moduleClassPaths == null) {
            moduleClassPaths = new ArrayList<String>();
        }

        moduleClassPaths.add(moduleClassPath);

        logger.exiting(ModuleClassLoader.class.getName(), "addModuleClassPath");

    }

    /**
     * This method is used to get an instance of the
     * <em>ModuleClassLoader</em>.
     * @return An instance of this class
     */
    public static ModuleClassLoader getInstance() {
        logger.entering(ModuleClassLoader.class.getName(), "getInstance");

        if (theInstance == null) {
            Class clazz = org.electrocodeogram.system.System.getInstance()
                .getClass();

            ClassLoader currentClassLoader = clazz.getClassLoader();

            theInstance = new ModuleClassLoader(currentClassLoader);
        }

        logger.exiting(ModuleClassLoader.class.getName(), "getInstance",
            theInstance);

        return theInstance;
    }

}

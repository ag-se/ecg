/*
 * Class: ModuleClassLoader
 * Version: 1.0
 * Date: 18.10.2005
 * By: Frank@Schlesinger.com
 */

package org.electrocodeogram.modulepackage.classloader;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;

import org.electrocodeogram.logging.LogHelper;

/**
 * The <code>ModuleClassLoader</code> is able to load
 * <code>Class</code> files directly from a given location in the
 * file system, which is the module directory. It has support for
 * loading resources too. Resources and classes can be either located
 * in plain files inside the <em>ModulePackages</em> or in
 * jar-libraries directly present in the "lib/runtime/" folder of a
 * <em>ModulePackage</em>.
 */
public class ModuleClassLoader extends java.lang.ClassLoader {

    /**
     * This is the logger.
     */
    private static Logger logger = LogHelper
        .createLogger(ModuleClassLoader.class.getName());

    /**
     * This is a list of known <em>ModulePackage</em> pathes.
     */
    private static ArrayList < String > modulePackages;

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
     * Locates a file with the given name inside the jar-libraries of
     * the known <em>ModulePackage</em> directories. The filename is
     * expected to be fully-qualified and the corresponding file is a
     * resource or a class.
     * @param name
     *            The fully-qualified name of the resource or class
     * @return The corresponding file or <code>null</code> if it is
     *         not found
     */
    private JarURLConnection getJarConnection(final String name) {

        logger.entering(this.getClass().getName(), "getJarConnection",
            new Object[] {name});

        JarURLConnection conn = null;

        String suffix = "";

        String realName = "";

        if (!name.endsWith(".properties")) {
            suffix = ".class";

            realName = getUrlClassName(name);

            realName += suffix;

        } else {
            realName = "/" + name;
        }

        // locate jar-libs in the known ModulePackages
        for (String modulePackage : modulePackages) {

            File libdir = new File(modulePackage, "lib" + File.separator
                                                  + "runtime");

            if (!libdir.exists() || libdir.isFile()) {

                // No lib here so go ahead
                continue;
            }

            // A lib-dir is found. Read the jars inside.
            String[] libs = libdir.list(new FilenameFilter() {

                // Only jar files please...
                public boolean accept(@SuppressWarnings("unused")
                final File dir, final String str) {
                    if (str.endsWith(".jar")) {
                        return true;
                    }

                    return false;
                }
            });

            // Look in each found jar-lib of this ModulePackage
            for (String libname : libs) {

                String path = libdir.getAbsolutePath() + File.separator
                              + libname + "!" + realName;

                URL url = null;

                // try to open a connection
                try {

                    url = new URL("jar:file:" + path);

                    conn = (JarURLConnection) url.openConnection();

                    conn.connect();

                    logger.log(Level.FINE, "Found the file " + name
                                           + " in the library: "
                                           + conn.getJarFile().getName());

                    logger.exiting(this.getClass().getName(),
                        "getJarConnection", conn);

                    // Ok its there now return it
                    return conn;

                } catch (MalformedURLException e) {

                    // No try another
                    continue;

                } catch (IOException e) {
                    // No try another
                    continue;
                }

            }

        }

        logger.log(Level.FINE, "Unable to find the file in the libraries. "
                               + name);

        logger.exiting(this.getClass().getName(), "getJarConnection", null);

        // The file is not found in any jar-libs
        return null;
    }

    /**
     * Looks for the requested file in the known
     * <em>ModulePackage</em> directories. The given name is
     * expected to be fully-qualified and the file is either a class
     * or a resource.
     * @param name
     *            Is the fully-qualified name of the class or resource
     *            to locate
     * @return The corresponding file or <code>null</code> if it is
     *         not found
     */
    private File getFile(final String name) {

        logger.entering(this.getClass().getName(), "getFile",
            new Object[] {name});
        // check the parameter
        if (name == null) {
            logger.log(Level.WARNING, "The parameter \"name\" is null");

            logger.exiting(this.getClass().getName(), "getFile", null);

            return null;
        }

        // Is there any known ModulePackage path? If not return...
        if (modulePackages == null) {
            logger.log(Level.SEVERE, "No modulepackage paths are defined.");

            logger.exiting(this.getClass().getName(), "getFile", null);

            return null;
        }

        // replace '.' with File.separator(s)
        String normalizedClassName = getNormalizedClassName(name);

        if (normalizedClassName == null) {
            logger.log(Level.SEVERE, "Path is invalid: " + name);

            logger.exiting(this.getClass().getName(), "getFile", null);

            return null;
        }

        File file = null;

        String suffix = "";

        // are we going for a resource or a class?
        if (!name.endsWith(".properties")) {
            // a class!
            suffix = ".class";
        }

        // Look in every ModulePackage for the file
        for (String moduleClassPath : modulePackages) {

            String path = moduleClassPath + normalizedClassName + suffix;

            file = new File(path);

            if (file.exists() && file.isFile()) {
                logger.log(Level.FINE, "File is found here: "
                                       + file.getAbsolutePath());

                break;
            }

            file = null;

        }

        logger.exiting(this.getClass().getName(), "getFile", file);

        return file;
    }

    // private InputStream getInputStream(File file) throws
    // FileNotFoundException {
    // if (file != null) {
    // return new FileInputStream(file);
    // } else {
    // return null;
    // }
    // }

    // private InputStream getInputStream(JarURLConnection conn,
    // JarEntry jarEntry)
    // throws IOException {
    // JarFile jarFile = conn.getJarFile();
    //
    // return jarFile.getInputStream(jarEntry);
    // }

    /**
     * This is reading a <code>Class</code> file from the given
     * stream. It makes use of the native method
     * {@link java.lang.ClassLoader#defineClass(java.lang.String, byte[], int, int)}.
     * @param is
     *            The stream to read from. It is either a
     *            {@link FileInputStream} if the class is a plain file
     *            or it is the
     *            {@link JarFile#getInputStream(java.util.zip.ZipEntry)}
     *            if the class is contained in a jar-library.
     * @param length
     *            The length of the class
     * @return The class file
     * @throws IOException
     *             If any exception occures while reading from the
     *             stream
     */
    private Class readClass(final InputStream is, final int length)
        throws IOException {

        byte[] data = new byte[length];

        is.read(data);

        return this.defineClass(null, data, 0, data.length);
    }

    /**
     * @see java.lang.ClassLoader#getResourceAsStream(java.lang.String)
     *      Implements the ability for ECG modules, to load
     *      {@link java.util.ResourceBundle} at runtime.
     */
    @Override
    public final InputStream getResourceAsStream(final String name) {

        logger.entering(this.getClass().getName(), "getResourceAsStream",
            new Object[] {name});

        Locale locale = Locale.getDefault();

        String loc = locale.toString();

        System.out.println(loc);

        logger.log(Level.FINE, "Trying to load the resource: " + name);

        // The resource might be located in a jar-lib
        JarURLConnection conn;

        JarEntry jarEntry;

        // or it is a plain file
        File file = getFile(name);

        if (file == null) {

            logger.log(Level.FINE, "The resource is not a plain file.");
            // it's no file so we try to get a connection to the
            // jar-lib
            conn = getJarConnection(name);

            if (conn == null) {
                logger.log(Level.WARNING, "Unable to locate the resource: "
                                          + name);

                logger.exiting(this.getClass().getName(),
                    "getResourceAsStream", null);
                // The resource is neither a file nor is it inside a
                // jar-lib

                return null;
            }

            try {
                jarEntry = conn.getJarEntry();

                JarFile jarFile = conn.getJarFile();

                ZipEntry entry = jarFile.getEntry(jarEntry.getName());

                InputStream is = new BufferedInputStream(jarFile
                    .getInputStream(entry));

                logger.log(Level.FINE, "The resource is found in:"
                                       + jarFile.getName());

                logger.exiting(this.getClass().getName(),
                    "getResourceAsStream", is);
                // The resource is found in the jar-lib and we return
                // the desired stream
                return is;
            } catch (IOException e1) {

                logger.log(Level.WARNING, "Unable to locate the resource: "
                                          + name);

                logger.exiting(this.getClass().getName(),
                    "getResourceAsStream", null);
                // The resource is neither a file nor is it inside a
                // jar-lib
                return null;
            }
        }
        try {

            logger.log(Level.FINE, "The resource is a plain file.");
            // We hav a file that seems to be the resource
            InputStream is = new FileInputStream(file);

            logger
                .log(Level.FINE, "The resource is found in:" + file.getName());

            logger
                .exiting(this.getClass().getName(), "getResourceAsStream", is);

            return is;

        } catch (FileNotFoundException e) {

            logger
                .log(Level.FINE,
                    "But it is not existing. Trying to locate it inside a library.");
            // But it was not... So we are trying to get the resource
            // from a jar-lib here
            conn = getJarConnection(name);

            try {
                jarEntry = conn.getJarEntry();

                JarFile jarFile = conn.getJarFile();

                ZipEntry entry = jarFile.getEntry(jarEntry.getName());

                InputStream is = new BufferedInputStream(jarFile
                    .getInputStream(entry));

                logger.log(Level.FINE, "The resource is found in:"
                                       + jarFile.getName());

                logger.exiting(this.getClass().getName(),
                    "getResourceAsStream", is);

                return is;

            } catch (IOException e1) {

                logger.log(Level.WARNING, "Unable to locate the resource: "
                                          + name);

                logger.exiting(this.getClass().getName(),
                    "getResourceAsStream", null);
                // sorry again
                return null;
            }
        }
    }

    /**
     * @see java.lang.ClassLoader#findClass(java.lang.String) Looks
     *      for class files in the known <em>ModulePackages</em>.
     */
    @Override
    protected final Class < ? > findClass(final String name) {
        logger.entering(this.getClass().getName(), "findClass",
            new Object[] {name});

        // look for the class as a plain file
        File file = getFile(name);

        Class < ? > toReturn = null;

        if (file != null) {
            FileInputStream fis = null;

            try {

                // open a stream to the class file and read it
                fis = new FileInputStream(file);

                toReturn = readClass(fis, (int) file.length());

                logger.log(Level.INFO, "Successfully loaded module class: "
                                       + file.getName());

            } catch (IOException e) {

                logger.log(Level.SEVERE, "Error while loading module class: "
                                         + name);

                logger.log(Level.SEVERE, e.getMessage());

            } catch (Error e) {
                logger.log(Level.SEVERE, "Error while loading class: " + name);

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
                                         + name);
            }

            logger.exiting(this.getClass().getName(), "findClass", toReturn);

            return toReturn;
        }

        // ok the class was not in a plain file... So we go for
        // libraries
        JarURLConnection conn = getJarConnection(name);

        // Get the jar entry
        JarEntry jarEntry;

        InputStream is = null;

        try {
            if (conn == null) {

                logger.log(Level.SEVERE, "The class could not be found: "
                                         + name);

                logger.exiting(this.getClass().getName(), "findClass", null);

                return null;
            }

            jarEntry = conn.getJarEntry();

            JarFile jarFile = conn.getJarFile();

            ZipEntry entry = jarFile.getEntry(jarEntry.getName());

            is = new BufferedInputStream(jarFile.getInputStream(entry));

            toReturn = readClass(is, (int) entry.getSize());

            logger.log(Level.INFO, "Successfully loaded module class: "
                                   + entry.getName());
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error while loading module class: "
                                     + name);

            logger.log(Level.SEVERE, e.getMessage());

        } catch (Error e) {
            logger.log(Level.SEVERE, "Error while loading class: ");

            logger.log(Level.SEVERE, e.getMessage());

        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error while closing the stream.");
            }
        }

        if (toReturn == null) {
            logger.log(Level.SEVERE, "The class could not be found: " + name);
            logger.exiting(this.getClass().getName(), "findClass", null);

        }

        return toReturn;
    }

    /**
     * Converts the full qualified class name into a file system path.
     * org.foo.bar -> org\\foo\\bar for example.
     * @param name
     *            The full qualified class name
     * @return A file system path
     */
    private String getNormalizedClassName(final String name) {
        logger.entering(this.getClass().getName(), "getNormalizedClassName",
            new Object[] {name});

        if (name == null) {
            logger.log(Level.WARNING, "The parameter \"className\" is null.");

            logger.exiting(this.getClass().getName(), "getNormalizedClassName");

            return null;
        }

        StringTokenizer stringTokenizer = new StringTokenizer(name, ".");

        String normalizedClassName = "";

        while (stringTokenizer.hasMoreTokens()) {
            normalizedClassName += File.separator + stringTokenizer.nextToken();

        }

        logger.exiting(this.getClass().getName(), "getNormalizedClassName");

        return normalizedClassName;
    }

    /**
     * Converts the full qualified class name into a URL-path. *
     * org.foo.bar -> org/foo/bar for example.
     * @param name
     *            The full qualified class name
     * @return A URL-path
     */
    private String getUrlClassName(final String name) {
        logger.entering(this.getClass().getName(), "getUrlClassName",
            new Object[] {name});

        if (name == null) {
            logger.log(Level.WARNING, "The parameter \"className\" is null.");

            logger.exiting(this.getClass().getName(), "getNormalizedClassName");

            return null;
        }

        StringTokenizer stringTokenizer = new StringTokenizer(name, ".");

        String normalizedClassName = "";

        while (stringTokenizer.hasMoreTokens()) {
            normalizedClassName += "/" + stringTokenizer.nextToken();

        }

        logger.exiting(this.getClass().getName(), "getUrlClassName");

        return normalizedClassName;
    }

    /**
     * This method is used by the
     * {@link org.electrocodeogram.module.registry.ModuleRegistry} to
     * add another <em>ModulePackage</em> path to the list.
     * @param path
     *            Is the <em>ModulePackage</em> path to add
     */
    public static final void addPath(final String path) {
        logger.entering(ModuleClassLoader.class.getName(),
            "addModuleClassPath", new Object[] {path});

        if (path == null) {
            logger.log(Level.WARNING,
                "The parameter \"moduleClassPath\" is null.");
            logger.exiting(ModuleClassLoader.class.getName(),
                "addModuleClassPath");
            return;
        }

        if (modulePackages == null) {
            modulePackages = new ArrayList < String >();
        }

        modulePackages.add(path);

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
            Class clazz = ModuleClassLoader.class;

            ClassLoader currentClassLoader = clazz.getClassLoader();

            theInstance = new ModuleClassLoader(currentClassLoader);
        }

        logger.exiting(ModuleClassLoader.class.getName(), "getInstance",
            theInstance);

        return theInstance;
    }

}

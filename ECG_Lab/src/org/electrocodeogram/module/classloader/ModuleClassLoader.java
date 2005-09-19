package org.electrocodeogram.module.classloader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.electrocodeogram.system.SystemRoot;

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

	private static ArrayList<String> _moduleClassPaths;

	private static ModuleClassLoader _theInstance;
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

		if (this._moduleClassPaths == null)
		{
			this.logger.log(Level.SEVERE, "No module class paths defined.");

			return null;
		}

		String normalizedClassName = getNormalizedClassName(className);

		if (normalizedClassName == null)
		{
			this.logger.log(Level.SEVERE, "Class path is invalid: " + className);

			return null;
		}

		for (String moduleClassPath : this._moduleClassPaths)
		{

			String pathToModuleClass = moduleClassPath + normalizedClassName + ".class";

			File classFile = new File(pathToModuleClass);

			if (!classFile.exists())
			{

				continue;
			}

			if (!classFile.isFile())
			{

				continue;
			}

			FileInputStream fis = null;

			try
			{
				fis = new FileInputStream(classFile);

				byte[] data = new byte[(int) classFile.length()];

				fis.read(data);

				try
				{
					toReturn = this.defineClass(null, data, 0, data.length);

					break;

				}
				catch (LinkageError e)
				{
					this.logger.log(Level.INFO, "Linkage error: " + e.getMessage());

				}

				this.logger.log(Level.INFO, "Successfully loaded module class: " + classFile.getName());

			}
			catch (IOException e)
			{

				this.logger.log(Level.WARNING, "Error while loading module class: " + className);

			}
		}

		if (toReturn == null)
		{
			this.logger.log(Level.WARNING, "The desired class could not be found: " + className);
		}

		return toReturn;
	}

	private String getNormalizedClassName(String className)
	{
		StringTokenizer stringTokenizer = new StringTokenizer(className, ".");

		String normalizedClassName = "";

		while (stringTokenizer.hasMoreTokens())
		{
			normalizedClassName += File.separator + stringTokenizer.nextToken();

		}

		return normalizedClassName;
	}

	/**
	 * This method is used by the ModuleRegistry to add another module path
	 * to the list of knon module paths.
	 * @param moduleClassPath Is the new module path
	 */
	public static void addModuleClassPath(String moduleClassPath)
	{
		if (_moduleClassPaths == null)
		{
			_moduleClassPaths = new ArrayList<String>();
		}

		_moduleClassPaths.add(moduleClassPath);

	}
	
	public static ModuleClassLoader getInstance()
	{
		if(_theInstance == null)
		{
			Class clazz = SystemRoot.getSystemInstance().getClass();

			ClassLoader currentClassLoader = clazz.getClassLoader();
			
			_theInstance = new ModuleClassLoader(currentClassLoader);
		}
		
		return _theInstance;
	}
	
}

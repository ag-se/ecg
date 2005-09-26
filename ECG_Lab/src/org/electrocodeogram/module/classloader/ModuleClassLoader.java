package org.electrocodeogram.module.classloader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.electrocodeogram.logging.LogHelper;
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

	private static Logger _logger = LogHelper.createLogger(ModuleClassLoader.class.getName());

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

		_logger.entering(this.getClass().getName(), "ModuleClassLoader");

		_logger.exiting(this.getClass().getName(), "ModuleClassLoader");
	}

	/**
	 * @see java.lang.ClassLoader#findClass(java.lang.String)
	 * 
	 * This method loads classes from the given path to the file system.
	 */
	@Override
	protected Class<?> findClass(String className)
	{
		_logger.entering(this.getClass().getName(), "findClass");

		if (className == null)
		{
			_logger.log(Level.WARNING, "className is null");

			return null;
		}

		Class<?> toReturn = null;

		if (_moduleClassPaths == null)
		{
			_logger.log(Level.SEVERE, "No module class paths are defined.");

			return null;
		}

		String normalizedClassName = getNormalizedClassName(className);

		if (normalizedClassName == null)
		{
			_logger.log(Level.SEVERE, "Class path is invalid: " + className);

			return null;
		}

		for (String moduleClassPath : _moduleClassPaths)
		{

			String pathToModuleClass = moduleClassPath + normalizedClassName + ".class";

			File classFile = new File(pathToModuleClass);

			if (!classFile.exists())
			{
				_logger.log(Level.WARNING, "Class is not found here: " + classFile.getAbsolutePath());

				continue;
			}

			if (!classFile.isFile())
			{
				_logger.log(Level.WARNING, "Class is not found here: " + classFile.getAbsolutePath());

				continue;
			}

			FileInputStream fis = null;

			_logger.log(Level.INFO, "Class is found here: " + classFile.getAbsolutePath());

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
				catch (Error e)
				{
					_logger.log(Level.SEVERE, "Error while loading class: " + className);

					_logger.log(Level.FINEST, e.getMessage());

				}

				_logger.log(Level.INFO, "Successfully loaded module class: " + classFile.getName());

			}
			catch (IOException e)
			{

				_logger.log(Level.WARNING, "Error while loading module class: " + className);
				
				_logger.log(Level.FINEST, e.getMessage());

			}
		}

		if (toReturn == null)
		{
			_logger.log(Level.WARNING, "The class could not be found: " + className);
		}

		_logger.exiting(this.getClass().getName(), "findClass");

		return toReturn;
	}

	private String getNormalizedClassName(String className)
	{
		_logger.entering(this.getClass().getName(), "getNormalizedClassName");

		if (className == null)
		{
			_logger.log(Level.WARNING, "className is null");

			return null;
		}

		StringTokenizer stringTokenizer = new StringTokenizer(className, ".");

		String normalizedClassName = "";

		while (stringTokenizer.hasMoreTokens())
		{
			normalizedClassName += File.separator + stringTokenizer.nextToken();

		}

		_logger.exiting(this.getClass().getName(), "getNormalizedClassName");

		return normalizedClassName;
	}

	/**
	 * This method is used by the ModuleRegistry to add another module path
	 * to the list of knon module paths.
	 * @param moduleClassPath Is the new module path
	 */
	public static void addModuleClassPath(String moduleClassPath)
	{
		_logger.entering(ModuleClassLoader.class.getName(), "addModuleClassPath");

		if (moduleClassPath == null)
		{
			_logger.log(Level.WARNING, "moduleClassPath is null");

			return;
		}

		if (_moduleClassPaths == null)
		{
			_moduleClassPaths = new ArrayList<String>();
		}

		_moduleClassPaths.add(moduleClassPath);

		_logger.exiting(ModuleClassLoader.class.getName(), "addModuleClassPath");

	}

	public static ModuleClassLoader getInstance()
	{
		_logger.entering(ModuleClassLoader.class.getName(), "getInstance");

		if (_theInstance == null)
		{
			Class clazz = SystemRoot.getSystemInstance().getClass();

			ClassLoader currentClassLoader = clazz.getClassLoader();

			_theInstance = new ModuleClassLoader(currentClassLoader);
		}

		_logger.exiting(ModuleClassLoader.class.getName(), "getInstance");

		return _theInstance;
	}

}

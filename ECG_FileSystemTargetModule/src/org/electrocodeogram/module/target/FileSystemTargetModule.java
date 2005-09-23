package org.electrocodeogram.module.target;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;

import org.electrocodeogram.event.TypedValidEventPacket;
import org.electrocodeogram.module.ModuleProperty;
import org.electrocodeogram.module.ModulePropertyException;

/**
 *
 */
public class FileSystemTargetModule extends TargetModule
{

	private String outputFileName;

	private File outputFile;

	private PrintWriter writer;

	private static final String DEFAULT_FILENAME_PREFIX = "out";

	private static final String DEFAULT_FILENAME_SUFFIX = ".log";

	private static final String LOG_SUBDIR = "ecg_log";

	private int count = 0;

	private int fileSize = 1024 * 1024 * 10;

	private boolean splitFiles = true;

	private String homeDir;

	private File logDir;

	/**
	 * @param arg0
	 * @param arg1
	 */
	public FileSystemTargetModule(String arg0, String arg1)
	{
		super(arg0, arg1);

		this.getLogger().exiting(this.getClass().getName(), "FileSystemTargetModule");

	}

	/**
	 * @see org.electrocodeogram.module.target.TargetModule#write(org.electrocodeogram.event.TypedValidEventPacket)
	 */
	@Override
	public void write(TypedValidEventPacket arg0)
	{

		try
		{
			this.getLogger().entering(this.getClass().getName(), "write");

			this.writer.println(arg0.toString());

			this.writer.flush();

			this.getLogger().log(Level.INFO, "Event packet written to " + this.outputFile.getAbsolutePath());

			if (this.outputFile.length() >= this.fileSize && this.splitFiles)
			{
				this.writer.close();

				this.outputFile = new File(
						this.logDir.getAbsoluteFile() + File.separator + ++this.count + "_" + this.outputFileName);

				this.writer = new PrintWriter(new FileWriter(this.outputFile));
				
				this.getLogger().log(Level.INFO, "New logfile created: " + this.outputFile.getAbsolutePath());
			}

			this.getLogger().exiting(this.getClass().getName(), "write");
		}
		catch (IOException e)
		{
			this.getLogger().log(Level.SEVERE, "Error while writing to logfile: " + this.outputFile.getAbsolutePath() + "\nThe disk might be full.");
		}
	}

	/**
	 * @param propertyName 
	 * @param propertyValue 
	 * @throws ModulePropertyException 
	 * 
	 */
	@Override
	public void setProperty(String propertyName, String propertyValue) throws ModulePropertyException
	{
		if (propertyName.equals("Output File"))
		{

			File propertyValueFile = new File(propertyValue);

			this.outputFile = propertyValueFile;

			this.writer.close();

			try
			{
				this.writer = new PrintWriter(new FileWriter(this.outputFile));
			}
			catch (IOException e)
			{

				System.out.println("C");
				throw new ModulePropertyException(
						"The file could not be opened for writing.");
			}

			for (ModuleProperty property : this.runtimeProperties)
			{
				if (property.getName().equals(propertyName))
				{
					property.setValue(propertyValue);
				}
			}

		}
		else if(propertyName.equals("Split Files"))
		{
			if(propertyValue.equals("true"))
			{
				this.splitFiles = true;
				
			}
			else if(propertyValue.equals("false"))
			{
				this.splitFiles = false;
			}
			else
			{
				throw new ModulePropertyException(
						"The module does not support a property value of " + propertyValue + " with the given name: " + propertyName);
			}
			
		
		}
		else if(propertyName.equals("File Size"))
		{
			try
			{
				Integer intObj = Integer.parseInt(propertyValue);
				
				this.fileSize = intObj.intValue();
			}
			catch(NumberFormatException e)
			{
				throw new ModulePropertyException(
						"The module does not support a property value of " + propertyValue + " with the given name: " + propertyName);
			}
		}
		else
		{
			throw new ModulePropertyException(
					"The module does not support a property with the given name: " + propertyName);

		}
		
		this.getLogger().log(Level.INFO,"The " + propertyName + " property has been set to " + propertyValue);
	}

	public void analyseCoreNotification()
	{

	}

	/**
	 * @see org.electrocodeogram.module.Module#initialize()
	 */
	@Override
	public void initialize()
	{
		this.getLogger().entering(this.getClass().getName(), "initialize");

		this.homeDir = System.getProperty("user.home");

		if (this.homeDir == null)
		{
			this.homeDir = ".";
		}

		this.logDir = new File(this.homeDir + File.separator + LOG_SUBDIR);

		if (!logDir.exists())
		{
			logDir.mkdir();
		}

		this.outputFileName = DEFAULT_FILENAME_PREFIX + DEFAULT_FILENAME_SUFFIX;

		this.outputFile = new File(
				logDir.getAbsolutePath() + File.separator + this.outputFileName);

		try
		{
			this.writer = new PrintWriter(new BufferedWriter(new FileWriter(
					this.outputFile, true)));

		}
		catch (IOException e)
		{
			this.getLogger().log(Level.SEVERE, "Error while opening the output file.");
		}

		this.getLogger().exiting(this.getClass().getName(), "initialize");
	}

	/**
	 * @see org.electrocodeogram.module.Module#getProperty(java.lang.String)
	 */
	@Override
	public String getProperty(String propertyName)
	{
		this.getLogger().entering(this.getClass().getName(), "getProperty");

		this.getLogger().exiting(this.getClass().getName(), "getProperty");

		return null;
	}
}

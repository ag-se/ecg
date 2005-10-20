package org.electrocodeogram.module.target;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.electrocodeogram.event.ValidEventPacket;
import org.electrocodeogram.logging.LogHelper;
import org.electrocodeogram.module.ModuleProperty;
import org.electrocodeogram.module.ModulePropertyException;

/**
 * This class is an ECG module used to write ECG events into the file system.
 *
 */
public class FileSystemTargetModule extends TargetModule
{

	private static Logger _logger = LogHelper.createLogger(FileSystemTargetModule.class.getName());

	private File _outputFile;

	private PrintWriter _writer;

	private static final String DEFAULT_FILENAME_PREFIX = "out";

	private static final String DEFAULT_FILENAME_SUFFIX = ".log";

	private static final String LOG_SUBDIR = "ecg_log";

	private static final int DEFAULT_FILE_SIZE = 1024 * 1024 * 10;

	private int _count = 0;

	private int _fileSize = DEFAULT_FILE_SIZE;

	private boolean _splitFiles = false;

	private String _homeDir;

	private File _logDir;

	/**
	 * The constructor creates the module instance.
	 * It is not to be called by developers, instead it is called
	 * from the ECG ModuleRegistry when the user requested
	 * a new instance of this module.
	 * @param id This is the unique String id of the module  
	 * @param name This is the name which is given to the module instance
	 */
	public FileSystemTargetModule(String id, String name)
	{
		super(id, name);

		_logger.entering(this.getClass().getName(), "FileSystemTargetModule");

		_logger.exiting(this.getClass().getName(), "FileSystemTargetModule");

	}

	/**
	 * @see org.electrocodeogram.module.target.TargetModule#write(org.electrocodeogram.event.TypedValidEventPacket)
	 */
	@Override
	public void write(ValidEventPacket packet)
	{

		_logger.entering(this.getClass().getName(), "write");
		
		try
		{
			this._writer.println(packet.toString());

			this._writer.flush();

			_logger.log(Level.INFO, "An event has been written to the file " + this._outputFile.getAbsolutePath() + " by the module " + this.getName());

			if (this._outputFile.length() >= this._fileSize && this._splitFiles)
			{
				_logger.log(Level.INFO, "The log-file has reached the maximum file size of " + this._fileSize);

				this._writer.close();

				this._outputFile = new File(
						this._logDir.getAbsoluteFile() + File.separator + ++this._count + "_" + this._outputFile.getAbsolutePath());

				this._writer = new PrintWriter(new FileWriter(this._outputFile));

				_logger.log(Level.INFO, "A new log-file has been created: " + this._outputFile.getAbsolutePath());
			}

			_logger.exiting(this.getClass().getName(), "write");
		}
		catch (IOException e)
		{
			_logger.log(Level.SEVERE, "Error while writing to logfile: " + this._outputFile.getAbsolutePath() + "\nThe disk might be full.");
		}
	}

	
	@Override
	public void propertyChanged(ModuleProperty moduleProperty) throws ModulePropertyException
	{
	 
		if (moduleProperty.getName().equals("Output File"))
		{

			_logger.log(Level.INFO, "Request to set the property: " + moduleProperty.getName());

			if (moduleProperty.getValue() == null)
			{
				_logger.log(Level.WARNING, "The property value is null for: " + moduleProperty.getName());

				throw new ModulePropertyException(
						"The property value is null for: " + moduleProperty.getName());
			}

			File propertyValueFile = new File(moduleProperty.getValue());

			this._outputFile = propertyValueFile;

			this._writer.close();

			try
			{
				this._writer = new PrintWriter(new FileWriter(this._outputFile));

				_logger.log(Level.INFO, "Set the property: " + moduleProperty.getName() + " to " + this._outputFile.getAbsolutePath());
			}
			catch (IOException e)
			{

				_logger.log(Level.SEVERE, "The file could not be opened for writing: " + moduleProperty.getValue());

				throw new ModulePropertyException(
						"The file could not be opened for writing: " + moduleProperty.getValue());
			}
        

		}
		else if (moduleProperty.getName().equals("Split Files"))
		{
			_logger.log(Level.INFO, "Request to set the property: " + moduleProperty.getName());

			if (moduleProperty.getValue().equals("true"))
			{
				this._splitFiles = true;

				_logger.log(Level.INFO, "Set the property: " + moduleProperty.getName() + " to true");
			}
			else if (moduleProperty.getValue().equals("false"))
			{
				this._splitFiles = false;

				_logger.log(Level.INFO, "Set the property: " + moduleProperty.getName() + " to false");
			}
			else
			{
				_logger.log(Level.WARNING, "The module does not support a property value of " + moduleProperty.getValue() + " with the given name: " + moduleProperty.getName());

				throw new ModulePropertyException(
						"The module does not support a property value of " + moduleProperty.getValue() + " with the given name: " + moduleProperty.getName());
			}

		
		}
		else if (moduleProperty.getName().equals("File Size"))
		{
			_logger.log(Level.INFO, "Request to set the property: " + moduleProperty.getName());

			try
			{
				this._fileSize = Integer.parseInt(moduleProperty.getValue());

				_logger.log(Level.INFO, "Set the property: " + moduleProperty.getName() + " to " + this._fileSize);

			}
			catch (NumberFormatException e)
			{
				_logger.log(Level.WARNING, "The module does not support a property value of " + moduleProperty.getValue() + " with the given name: " + moduleProperty.getName());

				throw new ModulePropertyException(
						"The module does not support a property value of " + moduleProperty.getValue() + " with the given name: " + moduleProperty.getName());
			}

		
		}
		else
		{
			_logger.log(Level.WARNING, "The module does not support a property with the given name: " + moduleProperty.getName());

			throw new ModulePropertyException(
					"The module does not support a property with the given name: " + moduleProperty.getName());

		}

		_logger.exiting(this.getClass().getName(), "setProperty");
	}

	/**
	 *  @see org.electrocodeogram.module.Module#analyseCoreNotification()
	 *  This method is not implemented in this module.
	 */
	@Override
	public void analyseCoreNotification()
	{
		_logger.entering(this.getClass().getName(), "analyseCoreNotification");

		_logger.exiting(this.getClass().getName(), "analyseCoreNotification");

		// not implemented
	}

	/**
	 * @see org.electrocodeogram.module.Module#initialize()
	 * The method creates the default output file and a PrintWriter and FileWriter object to write to it. 
	 */
	@Override
	public void initialize()
	{
		_logger.entering(this.getClass().getName(), "initialize");

		this._homeDir = System.getProperty("user.home");

		if (this._homeDir == null)
		{
			this._homeDir = ".";
		}

		this._logDir = new File(this._homeDir + File.separator + LOG_SUBDIR);

		if (!this._logDir.exists())
		{
			this._logDir.mkdir();
		}

		String outputFileName = DEFAULT_FILENAME_PREFIX + DEFAULT_FILENAME_SUFFIX;

		this._outputFile = new File(
				this._logDir.getAbsolutePath() + File.separator + outputFileName);

		try
		{
			this._writer = new PrintWriter(new BufferedWriter(new FileWriter(
					this._outputFile, true)));

		}
		catch (IOException e)
		{
			_logger.log(Level.SEVERE, "Error while opening the output file: " + this._outputFile.getAbsolutePath());

			_logger.log(Level.FINEST, e.getMessage());
		}

		_logger.exiting(this.getClass().getName(), "initialize");
	}

	/**
	 * @see org.electrocodeogram.module.target.TargetModule#startWriter()
	 *  This method is not implemented in this module.
	 */
	@SuppressWarnings("unused")
	@Override
	public void startWriter() throws TargetModuleException
	{
		_logger.entering(this.getClass().getName(), "startWriter");

		// not implemented

		_logger.exiting(this.getClass().getName(), "startWriter");
	}

	/**
	 * @see org.electrocodeogram.module.target.TargetModule#stopWriter()
	 *  This method is not implemented in this module.
	 */
	@Override
	public void stopWriter()
	{
		_logger.entering(this.getClass().getName(), "stopWriter");

		// not implemented

		_logger.exiting(this.getClass().getName(), "stopWriter");

	}
}

package org.electrocodeogram.module.source;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.electrocodeogram.event.IllegalEventParameterException;
import org.electrocodeogram.event.ValidEventPacket;
import org.electrocodeogram.logging.LogHelper;
import org.electrocodeogram.module.source.FileSystemSourceModule.ReadMode;

/**
 * This Thread is used by the FileSystemSourceModule to read in
 * events from a file asynchroneously.
 *
 */
public class FileReaderThread extends Thread
{

	private Logger _logger;

	private SourceModule _sourceModule;

	private File _inputFile;

	private BufferedReader _reader;

	private ReadMode _readMode;

	private boolean _run;

	private static int TIME_SPAN = 100;

	/**
	 * This constrcutor creates the FileReaderThread.
	 * @param sourceModule Is the SourceModule to which the events shall be passed
	 * @param inputFile Is the File from which too read the events
	 * @param readMode Tells the FileReaderThread to run in either "BURST" or "REALTIME" mode
	 */
	public FileReaderThread(SourceModule sourceModule, File inputFile, ReadMode readMode)
	{
		this._logger = LogHelper.createLogger(this.getClass().getName());

		this._readMode = readMode;

		this._sourceModule = sourceModule;

		this._inputFile = inputFile;

		this._run = true;

	}

	/**
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run()
	{

		// Stores the timestamp of the last read event
		Date dateOfLastEvent = null;

		// Stores the Date when the last event has been read in
		Date relativeDate = null;

		try
		{

			// create the Reader
			this._reader = new BufferedReader(new FileReader(this._inputFile));

			// count the linenumbers for eventual error messages
			int lineNumber = 0;

			String line = null;

			/*
			 *  The first level Tokenizer used to disassemble the line into
			 *  the timestamp, the SensorDataType and the argList String.
			 */
			StringTokenizer eventTokenizer = null;

			//  As long as there are more lines...
			while ((line = this._reader.readLine()) != null && this._run)
			{

				lineNumber++;

				// Get a new Tokenizer.
				eventTokenizer = new StringTokenizer(line,
						ValidEventPacket.EVENT_SEPARATOR);

				// Check if the line is well formed.
				if (eventTokenizer.countTokens() != 3)
				{
					this._logger.log(Level.WARNING, "Error while reading line " + lineNumber + ":");

					this._logger.log(Level.WARNING, "This line does not contain valid event data.");

					continue;
				}

				// Get the timestamp String.
				String timeStampString = eventTokenizer.nextToken();

				// Check if the timestamp String is well formed.
				if (timeStampString == null || timeStampString.equals(""))
				{
					this._logger.log(Level.WARNING, "Error while reading timeStamp in line " + lineNumber + ":");

					this._logger.log(Level.WARNING, "The timeStamp is empty.");

					continue;
				}

				// Get the SensorDataType String.
				String sensorDataTypeString = eventTokenizer.nextToken();

				// Check if the SensorDataType String is well formed.
				if (sensorDataTypeString == null || sensorDataTypeString.equals(""))
				{
					this._logger.log(Level.WARNING, "Error while reading SensorDataType in line " + lineNumber + ":");

					this._logger.log(Level.WARNING, "The SensorDataType is empty.");

					continue;
				}

				// Get the argList String.
				String argListString = eventTokenizer.nextToken();

				// Check if the argList String is well formed.
				if (argListString == null || argListString.equals(""))
				{
					this._logger.log(Level.WARNING, "Error while reading argList in line " + lineNumber + ":");

					this._logger.log(Level.WARNING, "The argList is empty.");

					continue;
				}

				// Try to parse the timestamp String into a Date object.
				Date timeStamp = null;

				try
				{
					timeStamp = new SimpleDateFormat(
							ValidEventPacket.DATE_FORMAT_PATTERN).parse(timeStampString);
				}
				catch (ParseException e)
				{

					this._logger.log(Level.WARNING, "Error while reading timeStamp in line " + lineNumber + ":");

					this._logger.log(Level.WARNING, "The timeStamp is invalid.");

					this._logger.log(Level.WARNING, e.getMessage());

					continue;
				}

				// This second level Tokenizer is used to dissasemble the argList.
				StringTokenizer argListTokenizer = new StringTokenizer(
						argListString, ValidEventPacket.ARGLIST_SEPARATOR);

				// The Array is used to temprarilly store the argList entries.
				String[] argListStringArray = new String[argListTokenizer.countTokens()];

				int i = 0;

				while (argListTokenizer.hasMoreTokens())
				{
					argListStringArray[i++] = argListTokenizer.nextToken();
				}

				// Create a List object from the Array now containing the argList String entries.
				List argList = Arrays.asList(argListStringArray);

				// Try to create a ValidEventPacket object from the line's data.
				ValidEventPacket eventPacket = null;

				try
				{
					eventPacket = new ValidEventPacket(0, timeStamp,
							sensorDataTypeString, argList);
				}
				catch (IllegalEventParameterException e)
				{

					this._logger.log(Level.WARNING, "Error while generating eventz from line " + lineNumber + ":");

					this._logger.log(Level.WARNING, e.getMessage());

					continue;
				}

				/*
				 * When this module is in "BURST" mode we can continue to parse the next line.
				 * If not we need to analyse the timestamp of the next line.
				 */
				if (this._readMode != ReadMode.BURST)
				{
					// If this was the first line, then no previous timestamp has been stored.
					if (dateOfLastEvent != null)
					{
						// Get the time delta of the last event and the current event.
						long eventDelta = eventPacket.getTimeStamp().getTime() - dateOfLastEvent.getTime();

						// Get the current Date
						Date currentDate = new Date();

						// Get the delta of the time when the last event was parsed and now
						long realDelta = currentDate.getTime() - relativeDate.getTime();

						// Get the delta in real realtime and compare it to the event time delta.
						while (realDelta < eventDelta && this._readMode != ReadMode.BURST && this._run)
						{
							try
							{
								// Wait some time and retry until the time has ellapsed.
								Thread.sleep(TIME_SPAN);

								currentDate = new Date();

								realDelta = currentDate.getTime() - relativeDate.getTime();
							}
							catch (InterruptedException e)
							{
								// No problem if and interruption occures...
							}
						}

					}

				}

				// Store the timestamp of the current event for the next loop.
				dateOfLastEvent = eventPacket.getTimeStamp();

				// Store the timestamp for the next loop.
				relativeDate = new Date();

				// Now pass the event to the SourceModule and into the ECG Lab.
				this._sourceModule.append(eventPacket);

			}

			this._reader.close();

			this._sourceModule.deactivate();
		}
		catch (IOException e)
		{
			this._logger.log(Level.SEVERE, "Error while reading the file: " + this._inputFile.getAbsolutePath());

			this._logger.log(Level.FINEST, e.getMessage());
		}
		finally
		{
			if (this._reader != null)
			{
				try
				{
					this._reader.close();
				}
				catch (IOException e)
				{
					this._logger.log(Level.SEVERE, "Error while closing the file: " + this._inputFile.getAbsolutePath());

					this._logger.log(Level.FINEST, e.getMessage());
				}
			}
		}
	}

	/**
	 * This method sets the ReadMode for the FileReaderThread.
	 * @param readMode Is the ReadMode. Either "BURST" or "REALTIME".
	 */
	public void setMode(ReadMode readMode)
	{
		this._readMode = readMode;
	}

	/**
	 * This stops the FileReaderThread.
	 */
	public void stopReader()
	{
		this._run = false;

	}

}

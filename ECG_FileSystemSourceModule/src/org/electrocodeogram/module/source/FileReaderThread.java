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


public class FileReaderThread extends Thread
{

	private Logger _logger;
	
	
	private SourceModule _sourceModule;
	
	private File _inputFile;
	
    private BufferedReader _reader;
    
    private ReadMode _readMode;

	private boolean _run;
    
	public FileReaderThread(SourceModule sourceModule,File inputFile,ReadMode readMode)
	{
		this._logger = LogHelper.createLogger(this);
	
		this._readMode = readMode;
		
		this._sourceModule = sourceModule;
		
		this._inputFile = inputFile;
		
		this._run = true;
		
	}
	
	public void run()
	{

    	
    	Date dateOfLastEvent = null;
    	
    	Date relativeDate = null;
    	
        try {

        	this._reader = new BufferedReader(new FileReader(this._inputFile));
        	
            int lineNumber = 0;
            
            String line = null;

            StringTokenizer eventTokenizer = null;
            
            while ((line = this._reader.readLine()) != null && this._run)
            {
                lineNumber++;
                
                eventTokenizer = new StringTokenizer(line,ValidEventPacket.EVENT_SEPARATOR);
                
                if(eventTokenizer.countTokens() != 3)
                {
                    this._logger.log(Level.WARNING,"Error while reading line " + lineNumber + ":");
                    
                    this._logger.log(Level.WARNING,"This line does not contain valid event data.");
                    
                    continue;
                }
                
                String timeStampString = eventTokenizer.nextToken();
                
                if(timeStampString == null || timeStampString.equals(""))
                {
                    this._logger.log(Level.WARNING,"Error while reading timeStamp in line " + lineNumber + ":");
                    
                    this._logger.log(Level.WARNING,"The timeStamp is empty.");
                    
                    continue;
                }
                
                String sensorDataTypeString = eventTokenizer.nextToken();
                
                if(sensorDataTypeString == null || sensorDataTypeString.equals(""))
                {
                    this._logger.log(Level.WARNING,"Error while reading SensorDataType in line " + lineNumber + ":");
                    
                    this._logger.log(Level.WARNING,"The SensorDataType is empty.");
                    
                    continue;
                }
                
                String argListString = eventTokenizer.nextToken();
                
                if(argListString == null || argListString.equals(""))
                {
                    this._logger.log(Level.WARNING,"Error while reading argList in line " + lineNumber + ":");
                    
                    this._logger.log(Level.WARNING,"The argList is empty.");
                    
                    continue;
                }
                
                Date timeStamp = null;
                
                try {
                    timeStamp = new SimpleDateFormat(ValidEventPacket.DATE_FORMAT_PATTERN).parse(timeStampString);
                }
                catch (ParseException e) {
                    
                    this._logger.log(Level.WARNING,"Error while reading timeStamp in line " + lineNumber + ":");
                    
                    this._logger.log(Level.WARNING,"The timeStamp is invalid.");
                    
                    this._logger.log(Level.WARNING,e.getMessage());
                    
                    continue;
                }
                
                StringTokenizer argListTokenizer = new StringTokenizer(argListString,ValidEventPacket.ARGLIST_SEPARATOR);
                
                String[] argListStringArray = new String[argListTokenizer.countTokens()];
                
                int i = 0;
                
                while(argListTokenizer.hasMoreTokens())
                {
                    argListStringArray[i++] = argListTokenizer.nextToken();
                }
                
                List argList = Arrays.asList(argListStringArray);
                
                ValidEventPacket eventPacket = null;
                
                try {
                    eventPacket = new ValidEventPacket(0,timeStamp,sensorDataTypeString,argList);
                }
                catch (IllegalEventParameterException e) {
                    
                    this._logger.log(Level.WARNING,"Error while generating eventz from line " + lineNumber + ":");
                    
                    this._logger.log(Level.WARNING,e.getMessage());
                    
                    continue;
                }
                
                if(this._readMode != ReadMode.BURST)
                {                	
	                if(dateOfLastEvent != null)
	                {
	                	long eventDateDiff = eventPacket.getTimeStamp().getTime() - dateOfLastEvent.getTime();
	                	
	                	Date currentDate = new Date();
	                	
	                	long realDateDiff = currentDate.getTime() - relativeDate.getTime();
	                	
	                	while(realDateDiff < eventDateDiff && this._readMode != ReadMode.BURST && this._run)
	                	{
	                		try
							{
								Thread.sleep(100);
								
								currentDate = new Date();
								
								realDateDiff = currentDate.getTime() - relativeDate.getTime();
							}
							catch (InterruptedException e)
							{
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
	                	}
	                	
	                }
	               
                }
                
                dateOfLastEvent = eventPacket.getTimeStamp();
                
                relativeDate = new Date();
                
                this._sourceModule.append(eventPacket);
                
                 
                
            }
            this._sourceModule.deactivate();
        }
        catch (IOException e) {
            this._logger.log(Level.WARNING,"Error while reading the file.");
            
            this._logger.log(Level.WARNING,e.getMessage());
        }
	}

	/**
	 * @param readMode
	 * @param speed
	 */
	public void setMode(ReadMode readMode)
	{
		this._readMode = readMode;
	}

	/**
	 * 
	 */
	public void stopReader()
	{
		this._run = false;
		
	}
	 
}

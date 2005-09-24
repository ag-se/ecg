package org.electrocodeogram.module.source;

import java.io.File;

import org.electrocodeogram.module.ModulePropertyException;

/**
 *
 */
public class FileSystemSourceModule extends SourceModule
{
	
	public enum ReadMode
	{
		BURST, REALTIME
	}

	private FileReaderThread _readerThread;
	
	private File _inputFile;

	private ReadMode _readMode;

	/**
     * @param arg0
     * @param arg1
     */
    public FileSystemSourceModule(String arg0, String arg1)
    {
        super(arg0, arg1);
        
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
        if (propertyName.equals("Input File"))
        {

	        File propertyValueFile = new File(propertyValue);
	
	        this._inputFile = propertyValueFile;
        
        }
        else if(propertyName.equals("Enable Realtime Mode"))
        {
        	if(propertyValue.equalsIgnoreCase("true"))
        	{
        		this._readMode = ReadMode.REALTIME;
        		
        		setMode();
        	}
        }
        else if(propertyName.equals("Enable Burst Mode"))
        {
        	if(propertyValue.equalsIgnoreCase("true"))
        	{
        		this._readMode = ReadMode.BURST;
        		
        		setMode();
        	}
        }
        else
        {
        	throw new ModulePropertyException(
                    "The module does not support a property with the given name: " + propertyName);
        }
               
    }

 

	/**
	 * 
	 */
	private void setMode()
	{
		if(this._readerThread != null)
		{
			this._readerThread.setMode(this._readMode);
		}
		
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
        
    }

    /**
     * @throws SourceModuleException 
     * @throws SourceModuleException 
     * @see org.electrocodeogram.module.source.SourceModule#startReader(org.electrocodeogram.module.source.SourceModule)
     */
    @Override
    public void startReader(SourceModule arg0) throws SourceModuleException
    {
		if(this._inputFile == null)
    	{
    		throw new SourceModuleException("No input file selected");
    	}

		this._readerThread = new FileReaderThread(this,this._inputFile,this._readMode);
		
		this._readerThread.start();
    }
    
    /**
	 * @see org.electrocodeogram.module.Module#getProperty(java.lang.String)
	 */
	@Override
	public String getProperty(String propertyName)
	{
		return null;
	}

	/* (non-Javadoc)
	 * @see org.electrocodeogram.module.source.SourceModule#stopReader()
	 */
	@Override
	public void stopReader()
	{
		this._readerThread.stopReader();
		
	}
}

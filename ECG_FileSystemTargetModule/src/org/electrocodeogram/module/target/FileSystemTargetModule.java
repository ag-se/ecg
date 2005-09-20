package org.electrocodeogram.module.target;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.electrocodeogram.event.TypedValidEventPacket;
import org.electrocodeogram.module.ModuleDescriptor;
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

    /**
     * @param arg0
     * @param arg1
     */
    public FileSystemTargetModule(String arg0, String arg1)
    {
        super(arg0, arg1);
        
        this.getLogger().exiting(this.getClass().getName(),"FileSystemTargetModule");

    }

    /**
     * @see org.electrocodeogram.module.target.TargetModule#write(org.electrocodeogram.event.ValidEventPacket)
     */
    @Override
    public void write(TypedValidEventPacket arg0)
    {

    	this.getLogger().entering(this.getClass().getName(),"write");
    	
        this.writer.println(arg0.toString());

        this.writer.flush();
        
        this.getLogger().exiting(this.getClass().getName(),"write");

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
        if (!propertyName.equals("Output File")) {
            throw new ModulePropertyException(
            "The module does not support a property with the given name: " + propertyName);
            
        }
        
        File propertyValueFile = new File(propertyValue);

        this.outputFile = propertyValueFile;

        this.writer.close();

        try {
            this.writer = new PrintWriter(new FileWriter(this.outputFile));
        }
        catch (IOException e) {

            System.out.println("C");
            throw new ModulePropertyException(
                    "The file could not be opened for writing.");
        }

        for(ModuleProperty property : this.runtimeProperties)
        {
        	if(property.getName().equals(propertyName))
        	{
        		property.setValue(propertyValue);
        	}
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
    	this.getLogger().entering(this.getClass().getName(),"initialize");
    	
        String homeDir = System.getProperty("user.home");

        if (homeDir == null) {
            homeDir = ".";
        }

        this.outputFileName = "test.txt";

        this.outputFile = new File(
                homeDir + File.separator + this.outputFileName);

        try {
            this.writer = new PrintWriter(new BufferedWriter(new FileWriter(
                    this.outputFile, true)));

        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        this.getLogger().exiting(this.getClass().getName(),"initialize");
    }
    
    /**
	 * @see org.electrocodeogram.module.Module#getProperty(java.lang.String)
	 */
	@Override
	public String getProperty(String propertyName)
	{
		this.getLogger().entering(this.getClass().getName(),"getProperty");
		
		this.getLogger().exiting(this.getClass().getName(),"getProperty");
		
		return null;
	}
}

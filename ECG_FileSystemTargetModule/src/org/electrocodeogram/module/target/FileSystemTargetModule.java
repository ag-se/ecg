package org.electrocodeogram.module.target;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.electrocodeogram.event.ValidEventPacket;
import org.electrocodeogram.module.target.TargetModule;

/**
 *
 */
public class FileSystemTargetModule extends TargetModule
{

    private String outputFileName = null;
    
    private File outputFile = null;
    
    private PrintWriter writer = null;
        
    /**
     * @param arg0
     * @param arg1
     */
    public FileSystemTargetModule(int arg0, String arg1)
    {
        super(arg0, arg1);
        
        String homeDir = System.getProperty("user.home");
        
        if(homeDir == null)
        {
            homeDir = ".";
        }
        
        this.outputFileName = "test.txt";
        
        this.outputFile = new File(homeDir + File.separator + this.outputFileName);
        
        try {
            this.writer = new PrintWriter(new BufferedWriter(new FileWriter(this.outputFile,true)));
            
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * @see org.electrocodeogram.module.target.TargetModule#write(org.electrocodeogram.event.ValidEventPacket)
     */
    @Override
    public void write(ValidEventPacket arg0)
    {
        
        this.writer.println(arg0.toString());
        
        this.writer.flush();
        
    }

    /**
     * 
     */
    public void setProperty(String propertyName, Object propertyValue)
    {
        
    }

    

}
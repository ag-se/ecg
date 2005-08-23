package org.electrocodeogram.module.target;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.electrocodeogram.event.ValidEventPacket;
import org.electrocodeogram.module.ModulePropertyException;
import org.electrocodeogram.module.target.TargetModule;

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
    public FileSystemTargetModule(int arg0, String arg1)
    {
        super(arg0, arg1);


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
     * @param propertyName 
     * @param propertyValue 
     * @throws ModulePropertyException 
     * 
     */
    @Override
    public void setProperty(String propertyName, Object propertyValue) throws ModulePropertyException
    {
        if (!propertyName.equals("Output File")) {
            throw new ModulePropertyException(
            "The module does not support a property with the given name: " + propertyName);
            
        }

        if (!(propertyValue instanceof File)) {
            throw new ModulePropertyException(
                    "The module only support a property of type: \"java.io.File\".");
        }

        File propertyValueFile = (File) propertyValue;

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
        
    }
}

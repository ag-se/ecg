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
import org.electrocodeogram.event.EventPacket;
import org.electrocodeogram.event.IllegalEventParameterException;
import org.electrocodeogram.event.ValidEventPacket;
import org.electrocodeogram.module.ModulePropertyException;

/**
 *
 */
public class FileSystemSourceModule extends SourceModule
{

    private String inputFileName;

    private File inputFile;

    private BufferedReader reader;
    
    /**
     * @param arg0
     * @param arg1
     */
    public FileSystemSourceModule(int arg0, String arg1)
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
    public void setProperty(String propertyName, Object propertyValue) throws ModulePropertyException
    {
        if (!propertyName.equals("Input File")) {
            throw new ModulePropertyException(
                    "The module does not support a property with the given name: " + propertyName);

        }

        if (!(propertyValue instanceof File)) {
            throw new ModulePropertyException(
                    "The module only support a property of type: \"java.io.File\".");
        }

        File propertyValueFile = (File) propertyValue;

        this.inputFile = propertyValueFile;

        try {
            
            if(this.reader != null)
            {
                this.reader.close();
            }

            this.reader = new BufferedReader(new FileReader(this.inputFile));
        }
        catch (IOException e) {

            System.out.println("C");
            throw new ModulePropertyException(
                    "The file could not be opened for reading.");
        }
        
        startReader(this);
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
     * @see org.electrocodeogram.module.source.SourceModule#startReader(org.electrocodeogram.module.source.SourceModule)
     */
    @Override
    public void startReader(SourceModule arg0)
    {

        try {

            int lineNumber = 0;
            
            String line = null;

            StringTokenizer eventTokenizer = null;
            
            while ((line = reader.readLine()) != null)
            {
                lineNumber++;
                
                eventTokenizer = new StringTokenizer(line,EventPacket.EVENT_SEPARATOR);
                
                if(eventTokenizer.countTokens() != 3)
                {
                    this.logger.log(Level.WARNING,"Error while reading line " + lineNumber + ":");
                    
                    this.logger.log(Level.WARNING,"This line does not contain valid event data.");
                    
                    continue;
                }
                
                String timeStampString = eventTokenizer.nextToken();
                
                if(timeStampString == null || timeStampString.equals(""))
                {
                    this.logger.log(Level.WARNING,"Error while reading timeStamp in line " + lineNumber + ":");
                    
                    this.logger.log(Level.WARNING,"The timeStamp is empty.");
                    
                    continue;
                }
                
                String sensorDataTypeString = eventTokenizer.nextToken();
                
                if(sensorDataTypeString == null || sensorDataTypeString.equals(""))
                {
                    this.logger.log(Level.WARNING,"Error while reading SensorDataType in line " + lineNumber + ":");
                    
                    this.logger.log(Level.WARNING,"The SensorDataType is empty.");
                    
                    continue;
                }
                
                String argListString = eventTokenizer.nextToken();
                
                if(argListString == null || argListString.equals(""))
                {
                    this.logger.log(Level.WARNING,"Error while reading argList in line " + lineNumber + ":");
                    
                    this.logger.log(Level.WARNING,"The argList is empty.");
                    
                    continue;
                }
                
                Date timeStamp = null;
                
                try {
                    timeStamp = new SimpleDateFormat("E M FF HH:mm:ss z yyyy").parse(timeStampString);
                }
                catch (ParseException e) {
                    
                    this.logger.log(Level.WARNING,"Error while reading timeStamp in line " + lineNumber + ":");
                    
                    this.logger.log(Level.WARNING,"The timeStamp is invalid.");
                    
                    this.logger.log(Level.WARNING,e.getMessage());
                    
                    continue;
                }
                
                StringTokenizer argListTokenizer = new StringTokenizer(argListString,EventPacket.ARGLIST_SEPARATOR);
                
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
                    
                    this.logger.log(Level.WARNING,"Error while generating eventz from line " + lineNumber + ":");
                    
                    this.logger.log(Level.WARNING,e.getMessage());
                    
                    continue;
                }
                
                arg0.append(eventPacket);
            }
        }
        catch (IOException e) {
            this.logger.log(Level.WARNING,"Error while reading the file.");
            
            this.logger.log(Level.WARNING,e.getMessage());
        }

    }
}
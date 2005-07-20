/*
 * Created on 10.03.2005
 *
 */
package org.electrocodeogram.module.annotator;

import java.util.logging.Level;

import org.electrocodeogram.core.Core;
import org.electrocodeogram.event.ValidEventPacket;
import org.electrocodeogram.module.Module;
import org.electrocodeogram.module.ModuleRegistry;

/**
 * @author 7oas7er
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public abstract class EventProcessor extends Module
{
    public static final int PRE_ANNOTATION = 0;

    public static final int POST_ANNOTATION = 1;
    
    public static final int ANNOTATOR = 0;
    
    public static final int FILTER = 1;
    
    private int processorMode = -1;

    private String separator = "";

    private int annnotationType = -1;

    /**
     * @param name
     */
    public EventProcessor(Core corePar, int processorMode, int annotationType, String separator)
    {
        super(corePar, ModuleType.INTERMEDIATE_MODULE);

        this.processorMode = processorMode;
        
        this.annnotationType = annotationType;

        this.separator = separator;
    }
    
    public EventProcessor(Core corePar, int annotationType, String separator)
    {
        super(corePar, ModuleType.INTERMEDIATE_MODULE);

        this.processorMode = EventProcessor.ANNOTATOR;
        
        this.annnotationType = annotationType;

        this.separator = separator;
    }
    
    public EventProcessor(Core corePar, int processorMode)
    {
        super(corePar, ModuleType.INTERMEDIATE_MODULE);
        
        this.processorMode = processorMode;
    }
    
    public int getAnnnotationType()
    {
        return annnotationType;
    }
    public void setAnnnotationType(int annnotationType)
    {
        this.annnotationType = annnotationType;
    }
    public int getProcessorMode()
    {
        return processorMode;
    }
    public void setProcessorMode(int processorMode)
    {
        this.processorMode = processorMode;
    }
    public String getSeparator()
    {
        return separator;
    }
    public void setSeparator(String separator)
    {
        this.separator = separator;
    }
    public void receiveEventPacket(ValidEventPacket eventPacket)
    {
        if(processorMode == -1)
        {
            processorMode = EventProcessor.ANNOTATOR;
        }
        if(processorMode == EventProcessor.ANNOTATOR)
        {
	        ValidEventPacket resultPacket = getProcessingResult(eventPacket);
	
	        if (annnotationType == EventProcessor.PRE_ANNOTATION) {
	            sendEventPacket(resultPacket);
	            sendEventPacket(eventPacket);
	
	        }
	        else {
	            sendEventPacket(eventPacket);
	            sendEventPacket(resultPacket);
	        }
        }
        else if(processorMode == EventProcessor.FILTER)
        {
            ValidEventPacket resultPacket = getProcessingResult(eventPacket);
        	
            sendEventPacket(resultPacket);
        }
    }

    /**
     * @param str
     */
    private ValidEventPacket getProcessingResult(ValidEventPacket eventPacket)
    {
        logger.log(Level.INFO, "getAnnotation");

        return annotate(eventPacket);

    }

    /**
     * @param str
     * @return
     */
    public abstract ValidEventPacket annotate(ValidEventPacket eventPacket);

}
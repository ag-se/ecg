package org.electrocodeogram.modules;
import java.util.Arrays;
import java.util.Date;

import org.electrocodeogram.EventPacket;
import org.electrocodeogram.module.annotator.EventProcessor;
/*
 * Created on 29.04.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

/**
 * @author 7oas7er
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class CodechangeDiffer extends EventProcessor
{

    /**
     * @param processorMode
     * @param annotationType
     * @param separator
     * @param name
     */
    public CodechangeDiffer()
    {
        super(EventProcessor.FILTER);
       
        start();
    }

    /* (non-Javadoc)
     * @see org.electrocodeogram.module.annotator.EventProcessor#annotate(org.electrocodeogram.EventPacket)
     */
    public EventPacket annotate(EventPacket eventPacket)
    {

        if(isPacketMatching(eventPacket,EventPacket.HS_COMMAND_PREFIX + "Activity",EventPacket.ECG_TYPE_CODECHANGE))
        {
            String[] args = {getName(),"CODECHANGE","DIFF"}; 
            
            return new EventPacket(this.getId(),new Date(),"Processed",Arrays.asList(args));
            
            // TODO : abstract returning
        }
        
        return null;
    }

    /* (non-Javadoc)
     * @see org.electrocodeogram.module.Module#setProperty(java.lang.String, java.lang.Object)
     */
    public void setProperty(String currentPropertyName, Object propertyValue)
    {
        // TODO Auto-generated method stub
        
    }

}

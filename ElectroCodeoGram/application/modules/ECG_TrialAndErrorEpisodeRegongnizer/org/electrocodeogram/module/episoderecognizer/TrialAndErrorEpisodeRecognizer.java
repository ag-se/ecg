package org.electrocodeogram.module.episoderecognizer;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Level;

import org.electrocodeogram.EventPacket;
import org.electrocodeogram.module.annotator.EventProcessor;

/*
 * Created on 07.04.2005
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
public class TrialAndErrorEpisodeRecognizer extends EventProcessor
{
    
    private int state = 1;
    
    public TrialAndErrorEpisodeRecognizer()
    {
        super(EventProcessor.ANNOTATOR);
    }

    /* (non-Javadoc)
     * @see org.electrocodeogram.module.Module#setProperty(java.lang.String, java.lang.Object)
     */
    public void setProperty(String currentPropertyName, Object propertyValue)
    {
        
    }
    
    public EventPacket annotate(EventPacket e)
    {
        
        if(state == 1)
        {
           
            logger.log(Level.INFO,"state is 1");
            
            if(isPacketMatching(e,EventPacket.HS_COMMAND_PREFIX + "Activity",EventPacket.ECG_TYPE_RUN))
            {
            
                this.eventBuffer.append(e);
            
                state = 2;
            }
            
            logger.log(Level.INFO,"returning null");
            
            return null;
        }
        else if(state == 2)
        {
            
            logger.log(Level.INFO,"state is 2");
            
            if(isPacketMatching(e,EventPacket.HS_COMMAND_PREFIX + "Activity",EventPacket.ECG_TYPE_RUN))
            {
	            Date d1 = eventBuffer.getLast().getTimeStamp();
	            
	            long l1 = d1.getTime();
	            
	            logger.log(Level.INFO,"last time is " + l1);
	            
	            Date d2 = e.getTimeStamp();
	            
	            long l2 = d2.getTime();
	            
	            logger.log(Level.INFO,"current time is " + l2);
	            
	            long diff = l2 - l1;
	            
	            assert(diff >= 0);
	            
	            logger.log(Level.INFO,"difference is " + diff);
	            
	            if(diff < 10000)
	            {
	                
	                logger.log(Level.INFO,"Episode recognized");
	                
	                String[] args = {getName(),d1.toString(),d2.toString()}; 
	                	                
	                return new EventPacket(this.getId(),new Date(),"Episode",Arrays.asList(args));
	                
	                
	            }
	            
	            this.eventBuffer.append(e);
            }
        }

        return null;
        
        
    }

    

}

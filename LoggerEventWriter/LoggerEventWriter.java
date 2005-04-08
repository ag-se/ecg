
/*
 * Created on 07.03.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


import org.electrocodeogram.module.writer.EventWriter;
import org.electrocodeogram.sensorwrapper.EventPacket;


/**
 * @author 7oas7er
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class LoggerEventWriter extends EventWriter
{
    /**
     * @param name
     */
    public LoggerEventWriter()
    {
        super("LoggerEventWriter");

    }


    Logger logger = Logger.getAnonymousLogger();

   
    /* (non-Javadoc)
     * @see net.datenfabrik.microstat.core.writer.EventWriter#write(java.lang.String)
     */
    public void write(EventPacket eventPacket)
    {
        String eventString = new String(eventPacket.getTimeStamp().toString() + " : " + eventPacket.getCommandName());
        
        List argList = eventPacket.getArglist();
        
        if (argList != null)
        {
	            
            Object[] args = eventPacket.getArglist().toArray();
        
            for(int i = 0; i < args.length; i++)
	        {
	            String str = (String) args[i];
	            
	            eventString += str;
	        }
        }
        
        eventString += "\n";
        
        logger.log(Level.INFO, eventString);
    }


    /* (non-Javadoc)
     * @see org.electrocodeogram.module.Module#setProperty(java.lang.String, java.lang.Object)
     */
    public void setProperty(String currentPropertyName, Object propertyValue)
    {
        // TODO Auto-generated method stub
        
    }

}

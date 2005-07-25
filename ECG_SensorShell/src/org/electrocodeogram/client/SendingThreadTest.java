package org.electrocodeogram.client;

import java.util.ArrayList;

import org.electrocodeogram.event.EventPacket;
import org.electrocodeogram.event.ValidEventPacket;

/**
 * This class contains methods to access the protected fields of the SendingThread
 * for testing purposes. It is used by the automated tests.
 */
public class SendingThreadTest
{

    private SendingThread sendingThread = null;
    
    /**
     * This creates a new SendingThreadTest object.
     *
     */
    public SendingThreadTest()
    {
        this.sendingThread = SendingThread.getInstance();
    }
    
    /**
     * This method returns the current size of the EventPacketQeueu in the SendingThread
     * @return The current size of the EventPacketQeueu in the SendingThread
     */
    public int getBufferSize()
    {
        ArrayList<ValidEventPacket> bufferCopy = createBufferCopy();
        
        return bufferCopy.size();
    }
    
    /**
     * This method tests whether the size of the EventPacketQueue in the SendingThread is equal to the given size. 
     * @param size The size to test against
     * @return "true", if the size is equal and "false" if not
     */
    public boolean testBufferSize(int size)
    {
        ArrayList<ValidEventPacket> bufferCopy = createBufferCopy();
        
        if(bufferCopy.size() == size)
        {
            return true;
        }
        
        return false;
        
    }
 
    /**
     * This method tests whether the state being connected of the SendingThread is equal to the state within a period of given connection trials. 
     * @param connected The state to test against
     * @param trials The number of trials to reach that state
     * @return "true", if the state is equal and "false" if not
     */
    public boolean testConnection(boolean connected, int trials)
    {
        int connectionTrialOffset = this.sendingThread.connectionTrials;
        
        int connectionTrialDelta = 0; 
        
        while(connectionTrialDelta > trials  && this.sendingThread.ping() != connected)
        {
            connectionTrialDelta = this.sendingThread.connectionTrials - connectionTrialOffset;
        }
        
        if(this.sendingThread.ping() == connected)
        {
            return true;
        }
       
        return false;
       
        
    }
    
    /**
     * This method tests whether the tail-most EventPacket of the EventPacketQueue in the SendingThread is equal to the given EventPacket.
     * @param eventPacket The EventPacket to test against
     * @return "true" if it is equal and "false" if not
     */
    public boolean testLastElement(EventPacket eventPacket)
    {
        ArrayList<ValidEventPacket> bufferCopy = createBufferCopy();
        
        ValidEventPacket lastAdded = bufferCopy.get(bufferCopy.size()-1);
        
        if(lastAdded.getSourceId() == eventPacket.getSourceId() && lastAdded.getTimeStamp().equals(eventPacket.getTimeStamp()) && lastAdded.getSensorDataType().equals(eventPacket.getSensorDataType()) && lastAdded.getArglist().equals(eventPacket.getArglist()))
        {
            return true;
        }
        
        return false;
        
    }
    
    private ArrayList<ValidEventPacket> createBufferCopy()
    {
        ArrayList<ValidEventPacket> bufferCopy = new ArrayList<ValidEventPacket>(); 
        
        for(ValidEventPacket elem : this.sendingThread.queue)
        {
            bufferCopy.add(elem);
        }
        return bufferCopy;
    }
    
    /**
     * This method returns the current value for the connection delay in the SendingThread
     * @return The current value for the connection delay in the SendingThread
     */
    public int getConnectionDelay()
    {
        return this.sendingThread.connectionDelay;
    }
    
    /**
     * This method returns the current number of connection trials in the SendingThread
     * @return The current number of connection trials in the SendingThread
     */
    public int getConnectionTrials()
    {
        return this.sendingThread.connectionTrials;
    }
}

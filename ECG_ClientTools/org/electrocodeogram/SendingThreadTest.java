package org.electrocodeogram;

import java.util.ArrayList;

public class SendingThreadTest
{

    private SendingThread sendingThread = null;
    
    public SendingThreadTest()
    {
        sendingThread = SendingThread.getInstance();
    }
    
    public int getBufferSize()
    {
        ArrayList<EventPacket> bufferCopy = createBufferCopy();
        
        return bufferCopy.size();
    }
    
    public boolean testBufferSize(int size)
    {
        ArrayList<EventPacket> bufferCopy = createBufferCopy();
        
        if(bufferCopy.size() == size)
        {
            return true;
        }
        else
        {
            return false;
        }
    }
 
    public boolean testConnection(boolean connected, int trials)
    {
        int connectionTrialOffset = sendingThread.connectionTrials;
        
        int connectionTrialDelta = 0; 
        
        while(connectionTrialDelta > trials  && sendingThread.connectedFlag != connected)
        {
            connectionTrialDelta = sendingThread.connectionTrials - connectionTrialOffset;
        }
        
        if(sendingThread.connectedFlag == connected)
        {
            return true;
        }
        else
        {
            return false;
        }
        
    }
    
    public boolean testLastElement(TestEventPacket eventPacket)
    {
        ArrayList<EventPacket> bufferCopy = createBufferCopy();
        
        EventPacket lastAdded = bufferCopy.get(bufferCopy.size()-1);
        
        if(lastAdded.getSourceId() == eventPacket.getSourceId() && lastAdded.getTimeStamp().equals(eventPacket.getTimeStamp()) && lastAdded.getHsCommandName().equals(eventPacket.getHsCommandName()) && lastAdded.getArglist().equals(eventPacket.getArglist()))
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    
    private ArrayList<EventPacket> createBufferCopy()
    {
        ArrayList<EventPacket> bufferCopy = new ArrayList<EventPacket>(); 
        
        for(EventPacket elem : sendingThread.queue)
        {
            bufferCopy.add(elem);
        }
        return bufferCopy;
    }
    
    public int getConnectionDelay()
    {
        return sendingThread.connectionDelay;
    }
    
    public int getConnectionTrials()
    {
        return sendingThread.connectionTrials;
    }
}

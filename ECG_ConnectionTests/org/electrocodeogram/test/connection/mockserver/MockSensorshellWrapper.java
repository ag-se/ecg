package org.electrocodeogram.test.connection.mockserver;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.electrocodeogram.core.SensorShellInterface;
import org.electrocodeogram.event.EventPacket;
import org.electrocodeogram.module.source.SocketServer;

/**
 * @author 7oas7er
 *
 */
public class MockSensorshellWrapper implements SensorShellInterface
{

    private EventPacket receivedEventPacket = null;
    
    private Date receivingTime = null;
    
    private int receivingCounter = 0;
    
    private SocketServer sensorServer = null;
    
    public MockSensorshellWrapper()
    {
        this.sensorServer = new SocketServer(this);
        
    }
    
    public boolean doCommand(Date timeStamp, String commandName, List argList)
    {
        this.receivingTime =  new Date();
        
        this.receivedEventPacket = new EventPacket(0,timeStamp,commandName,argList);
        
        this.receivingCounter++;
        
        return true;
        
    }
    
    public int getReceivingCount()
    {
        return this.receivingCounter;
    }
    
    public EventPacket getReceivedEventPacket()
    {
        EventPacket toReturn = this.receivedEventPacket;
        
        this.receivedEventPacket = null;
        
        return toReturn;
    }
    
    public Date getReceivingTime()
    {
        Date toReturn = this.receivingTime;
        
        this.receivingTime = null;
       
        return toReturn;
    }
}

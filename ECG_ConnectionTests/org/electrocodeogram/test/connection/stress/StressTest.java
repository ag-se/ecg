package org.electrocodeogram.test.connection.stress;

import java.io.IOException;

import org.electrocodeogram.event.IllegalEventParameterException;
import org.electrocodeogram.event.ValidEventPacket;
import org.electrocodeogram.test.EventGenerator;
import org.electrocodeogram.test.NoTestDataException;
import org.electrocodeogram.test.client.mocksensor.MockSensor;
import org.electrocodeogram.test.connection.mockserver.MockSensorshellWrapper;

public class StressTest
{

    private static StressTestResult result = null;

    private MockSensorshellWrapper mockSensorShellWrapper = null;

    private MockSensor testSensor = null;

    private EventGenerator eventGenerator = null;

    private int testCount = -1;

    public StressTest(int testCountPar) throws IOException
    {

        if (StressTest.result == null) {
            result = new StressTestResult(testCountPar);
        }

        this.testCount = testCountPar;

        this.eventGenerator = new EventGenerator();

        this.testSensor = new MockSensor();
        

        this.mockSensorShellWrapper = new MockSensorshellWrapper();

    }

    public void initialize() throws IllegalEventParameterException, NoTestDataException
    {

        ValidEventPacket eventPacket = this.eventGenerator.createPayloadEventPacket(1);
        
        int preReceivingCount = this.mockSensorShellWrapper.getReceivingCount();
        
        this.testSensor.sendEvent(eventPacket);
        
        while(!(this.mockSensorShellWrapper.getReceivingCount() == preReceivingCount + 1))
        {
            // do nothing
        }
        
    }
    
    public void test(int bytes) throws Exception
    {
        //check parameter
        if(!(bytes > 1))
        {
            throw new Exception();
        }
        
        assert(bytes > 1);
        
        
        
        long delta = 0;

        long receivedAt = 0;

        long sentAt = 0;

        //generate packet with payload of > "bytes" bytes
        ValidEventPacket eventPacket = this.eventGenerator.createPayloadEventPacket(bytes);

        int preReceivingCount = 0;
        
        for(int i=1;i<=this.testCount;i++)
        {
            preReceivingCount = this.mockSensorShellWrapper.getReceivingCount();
            
            // send the packet
            this.testSensor.sendEvent(eventPacket);
    
            // wait for receiving
            while (!(this.mockSensorShellWrapper.getReceivingCount() == preReceivingCount + 1)) {
               // do nothing
            }
    
            preReceivingCount++;
            
            // get times
            receivedAt = this.mockSensorShellWrapper.getReceivingTime().getTime();
    
            sentAt = this.testSensor.getSendingTime().getTime();
    
            // calculate
            delta = receivedAt - sentAt;
            
            // store result
            StressTest.result.addTransmissionTime(bytes, delta);
            
        }
    
    }

    public void printOut()
    {
        StressTest.result.printOut();

    }

    public static void main(String[] args)
    {
        try {
            StressTest stressTest = new StressTest(1000);

            stressTest.initialize();
            
            for(int i=1;i<=20;i++)
            {
                stressTest.test((int)Math.pow(2,i));
                
            }
            stressTest.printOut();

            System.exit(0);
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}

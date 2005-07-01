package org.electrocodeogram.test.serverside;

import org.electrocodeogram.client.TestClient;
import org.electrocodeogram.core.SensorShellWrapper;
import org.electrocodeogram.core.SensorShellWrapperTest;
import org.electrocodeogram.event.EventPacket;
import org.electrocodeogram.event.IllegalEventParameterException;
import org.electrocodeogram.event.ValidEventPacket;
import org.electrocodeogram.test.EventGenerator;
import org.electrocodeogram.test.EventGenerator.SensorDataType;

import junit.framework.TestCase;

/**
 * This class collects testcases running over the ECG serverside.
 *
 */
public class ServersideTests extends TestCase
{

    private SensorShellWrapper shell = null;
    
    private TestClient testClient = null;
    
    private EventGenerator eventGenerator = null;
 
    /**
     * This creates a testcase with the given name.
     * @param name The name of the testcase to create
     */
    public ServersideTests(String name)
    {
        super(name);
    }
    
    @Override
    protected void setUp()
    {
        this.shell = SensorShellWrapperTest.getInstance();
        
        this.testClient = new TestClient();
        
        this.eventGenerator = new EventGenerator();
    }
    
    @Override
    protected void tearDown()
    {
        this.shell = null;
        
        this.testClient = null;
        
        this.eventGenerator = null;
    }
    
    /**
     * Testcase 1 according to the document TESTPLAN Version 1.0 or higher.
     * This testcase passes a single syntactically invalid EventPacket from a TestClient
     * to the ECG SensorShellWrapper. In this case the timestamp of the EventPacket has the value "null".
     * The test is successfull if the result from the ECG SensorShell is "false", meaning the
     * EventPacket is syntactically invalid and not accepted. 
     *
     */
    public void testInvalidEventIsNotAcceptedTimeStampIsNull()
    {
        EventPacket eventPacket = this.eventGenerator.createEventPacket(false, true, true, true, 10, 10);

        boolean result = this.testClient.passEventData(this.shell,eventPacket);

        assertFalse(result);
    }
    
    /**
     * Testcase 2 according to the document TESTPLAN Version 1.0 or higher.
     * This testcase passes a single syntactically invalid EventPacket from a TestClient
     * to the ECG SensorShellWrapper. In this case the commandName of the EventPacket has the value "null".
     * The test is successfull if the result from the ECG SensorShellWrapper is "false", meaning the
     * EventPacket is syntactically invalid and not accepted. 
     *
     */
    public void testInvalidEventIsNotAcceptedCommandNameIsNull()
    {
        EventPacket eventPacket = this.eventGenerator.createEventPacket(true, false, true, true, 10, 10);

        boolean result = this.testClient.passEventData(this.shell,eventPacket);

        assertFalse(result);
    }

    /**
     * Testcase 3 according to the document TESTPLAN Version 1.0 or higher.
     * This testcase passes a single syntactically invalid EventPacket from a TestClient
     * to the ECG SensorShellWrapper. In this case the argList of the EventPacket has the value "null".
     * The test is successfull if the result from the ECG SensorShellWrapper is "false", meaning the
     * EventPacket is syntactically invalid and not accepted. 
     *
     */
    public void testInvalidEventIsNotAcceptedArgListIsNull()
    {
        EventPacket eventPacket = this.eventGenerator.createEventPacket(true, true, false, true, 10, 10);

        boolean result = this.testClient.passEventData(this.shell,eventPacket);

        assertFalse(result);
    }

    /**
     * Testcase 4 according to the document TESTPLAN Version 1.0 or higher.
     * This testcase passes a single syntactically invalid EventPacket from a TestClient
     * to the ECG SensorShellWrapper. In this case the argList is empty.
     * The test is successfull if the result from the ECG SensorShellWrapper is "false", meaning the
     * EventPacket is syntactically invalid and not accepted. 
     *
     */
    public void testInvalidEventIsNotAcceptedArgListIsEmpty()
    {
        EventPacket eventPacket = this.eventGenerator.createEventPacket(true, true, true, true, 0, 10);

        boolean result = this.testClient.passEventData(this.shell,eventPacket);

        assertFalse(result);
    }

    /**
     * Testcase 5 according to the document TESTPLAN Version 1.0 or higher.
     * This testcase passes a single syntactically invalid EventPacket from a TestClient
     * to the ECG SensorShellWrapper. In this case the argList is not of type List<String>.
     * The test is successfull if the result from the ECG SensorShellWrapper is "false", meaning the
     * EventPacket is syntactically invalid and not accepted. 
     *
     */
    public void testInvalidEventIsNotAcceptedArgListIsNotOfTypeString()
    {
        EventPacket eventPacket = this.eventGenerator.createEventPacket(true, true, true, false, 10, 10);

        boolean result = this.testClient.passEventData(this.shell,eventPacket);

        assertFalse(result);
    }
    
    /**
     * Testcase 6 according to the document TESTPLAN Version 1.0 or higher.
     * This testcase passes a single syntactically valid EventPacket from a TestClient
     * to the ECG SensorShellWrapper. But the EventPacket's commandName value is not any of the HackyStat
     * SensorDataTypes.
     * The test is successfull if the result from the ECG SensorShellWrapper is "false".
     *
     */
    public void testUnknownCommandNameIsNotAccepted()
    {
        ValidEventPacket eventPacket = null;
        
        try {
            eventPacket = this.eventGenerator.createValidEventPacket(true, true, true, true, 10, 10);
        }
        catch (IllegalEventParameterException e) {
            fail();
            
            e.printStackTrace();
        }

        boolean result = this.testClient.passEventData(this.shell,eventPacket);

        assertFalse(result);
    }
    
   /**
   * Testcase 7 according to the document TESTPLAN Version 1.0 or higher.
   * This testcase passes a HackyStat "Activity" SensorDataType event to the ECG SensorShellWrapper.
   * 
   * The test is successfull if the result from the ECG SensorShellWrapper is "true".
   *
   */
    public void testHackyStatActivityEventsAccepted()
    {
        ValidEventPacket eventPacket = this.eventGenerator.createHackyStatEventPacket(SensorDataType.ACTIVITY);
        
        boolean result = this.testClient.passEventData(this.shell,eventPacket);

        assertTrue(result);
       
    }
    
    /**
     * Testcase 8 according to the document TESTPLAN Version 1.0 or higher.
     * This testcase passes a HackyStat "Build" SensorDataType event to the ECG SensorShellWrapper.
     * 
     * The test is successfull if the result from the ECG SensorShellWrapper is "true".
     *
     */
      public void testHackyStatBuildEventsAccepted()
      {
          ValidEventPacket eventPacket = this.eventGenerator.createHackyStatEventPacket(SensorDataType.BUILD);
          
          boolean result = this.testClient.passEventData(this.shell,eventPacket);

          assertTrue(result);
         
      }
      
      /**
       * Testcase 9 according to the document TESTPLAN Version 1.0 or higher.
       * This testcase passes a HackyStat "BuffTrans" SensorDataType event to the ECG SensorShellWrapper.
       * 
       * The test is successfull if the result from the ECG SensorShellWrapper is "true".
       *
       */
        public void testHackyStatBuffTransEventsAccepted()
        {
            ValidEventPacket eventPacket = this.eventGenerator.createHackyStatEventPacket(SensorDataType.BUFFTRANS);
            
            boolean result = this.testClient.passEventData(this.shell,eventPacket);

            assertTrue(result);
           
        }
        
//        /**
//         * Testcase 10 according to the document TESTPLAN Version 1.0 or higher.
//         * This testcase passes a HackyStat "Commit" SensorDataType event to the ECG SensorShellWrapper.
//         * 
//         * The test is successfull if the result from the ECG SensorShellWrapper is "true".
//         *
//         */
//          public void testHackyStatCommitEventsAccepted()
//          {
//              ValidEventPacket eventPacket = this.testClient.createHackyStatEventPacket(SensorDataType.COMMIT);
//              
//              boolean result = this.testClient.passEventData(this.shell,eventPacket);
//
//              assertTrue(result);
//             
//          }
}

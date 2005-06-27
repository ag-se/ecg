package org.electrocodeogram.test;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * This class contains a suite of client-side tests for the ECG framework, which runs all client-side tests multiple times.
 */
public class ClientTestSuite
{
    
    /**
     * This method returns the current testsuite.
     * @return The testsuite
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite("Test for the client side of the ECG");
        
        int testACount = 100;
        
        for(int i=0;i<testACount;i++)
        {
            suite.addTest(new ClientsideTests("testValidEventIsAccepted"));
        }
        
        int testBCount = 100;
        
        for(int i=0;i<testBCount;i++)
        {
            suite.addTest(new ClientsideTests("testValidEventIsQueued"));
        }
        
        int testCCount = 100;
        
        for(int i=0;i<testCCount;i++)
        {
            suite.addTest(new ClientsideTests("testInvalidEventIsNotAcceptedTimeStampIsNull"));
        }
        
        int testDCount = 100;
        
        for(int i=0;i<testDCount;i++)
        {
            suite.addTest(new ClientsideTests("testInvalidEventIsNotAcceptedCommandNameIsNull"));
        }
        
        int testECount = 100;
        
        for(int i=0;i<testECount;i++)
        {
            suite.addTest(new ClientsideTests("testInvalidEventIsNotAcceptedArgListIsNull"));
        }
        
        int testFCount = 100;
        
        for(int i=0;i<testFCount;i++)
        {
            suite.addTest(new ClientsideTests("testInvalidEventIsNotAcceptedArgListIsEmpty"));
        }
        
        int testGCount = 100;
        
        for(int i=0;i<testGCount;i++)
        {
            suite.addTest(new ClientsideTests("testInvalidEventIsNotAcceptedArgListIsNotOfTypeString"));
        }
        
        int testHCount = 100;
        
        for(int i=0;i<testHCount;i++)
        {
            suite.addTest(new ClientsideTests("testIllegalEventParametersCauseException"));
        }
        
        return suite;
    }

}

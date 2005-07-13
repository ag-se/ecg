package org.electrocodeogram.test.client;


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
            suite.addTest(new ClientsideTests("testValidEventIsAccepted",i));
        }
        
        int testBCount = 100;
        
        for(int i=0;i<testBCount;i++)
        {
            suite.addTest(new ClientsideTests("testValidEventIsQueued",i));
        }
        
        int testCCount = 100;
        
        for(int i=0;i<testCCount;i++)
        {
            suite.addTest(new ClientsideTests("testInvalidEventIsNotAcceptedTimeStampIsNull",i));
        }
        
        int testDCount = 100;
        
        for(int i=0;i<testDCount;i++)
        {
            suite.addTest(new ClientsideTests("testInvalidEventIsNotAcceptedCommandNameIsNull",i));
        }
        
        int testECount = 100;
        
        for(int i=0;i<testECount;i++)
        {
            suite.addTest(new ClientsideTests("testInvalidEventIsNotAcceptedArgListIsNull",i));
        }
        
        int testFCount = 100;
        
        for(int i=0;i<testFCount;i++)
        {
            suite.addTest(new ClientsideTests("testInvalidEventIsNotAcceptedArgListIsEmpty",i));
        }
        
        int testGCount = 100;
        
        for(int i=0;i<testGCount;i++)
        {
            suite.addTest(new ClientsideTests("testInvalidEventIsNotAcceptedArgListIsNotOfTypeString",i));
        }
        
        int testHCount = 100;
        
        for(int i=0;i<testHCount;i++)
        {
            suite.addTest(new ClientsideTests("testIllegalEventParametersCauseException",i));
        }
        
        int testICount = 100;
        
        for(int i=0;i<testICount;i++)
        {
            suite.addTest(new ClientsideTests("testHackyStatActivityEventsAccepted",i));
        }
        
        int testJCount = 100;
        
        for(int i=0;i<testJCount;i++)
        {
            suite.addTest(new ClientsideTests("testHackyStatBuildEventsAccepted",i));
        }
        
        int testKCount = 100;
        
        for(int i=0;i<testKCount;i++)
        {
            suite.addTest(new ClientsideTests("testHackyStatBuffTransEventsAccepted",i));
        }
        
        
        int testLCount = 100;
        
        for(int i=0;i<testLCount;i++)
        {
            suite.addTest(new ClientsideTests("testHackyStatCommitEventsAccepted",i));
        }
        
        
        int testMCount = 100;
        
        for(int i=0;i<testMCount;i++)
        {
            suite.addTest(new ClientsideTests("testHackyStatFileMetricEventsAccepted",i));
        }
        
        
        int testNCount = 100;
        
        for(int i=0;i<testNCount;i++)
        {
            suite.addTest(new ClientsideTests("testHackyStatUnitTestEventsAccepted",i));
        }
        
        return suite;
    }

}

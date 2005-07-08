package org.electrocodeogram.test.serverside;


import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * This class contains a suite of client-side tests for the ECG framework, which runs all client-side tests multiple times.
 */
public class ServerTestSuite
{
    
    /**
     * This method returns the current testsuite.
     * @return The testsuite
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite("Test for the server side of the ECG");
        
        int testCCount = 100;
        
        for(int i=0;i<testCCount;i++)
        {
            suite.addTest(new ServersideTests("testInvalidEventIsNotAcceptedTimeStampIsNull",i));
        }
        
        int testDCount = 100;
        
        for(int i=0;i<testDCount;i++)
        {
            suite.addTest(new ServersideTests("testInvalidEventIsNotAcceptedCommandNameIsNull",i));
        }
        
        int testECount = 100;
        
        for(int i=0;i<testECount;i++)
        {
            suite.addTest(new ServersideTests("testInvalidEventIsNotAcceptedArgListIsNull",i));
        }
        
        int testFCount = 100;
        
        for(int i=0;i<testFCount;i++)
        {
            suite.addTest(new ServersideTests("testInvalidEventIsNotAcceptedArgListIsEmpty",i));
        }
        
        int testGCount = 100;
        
        for(int i=0;i<testGCount;i++)
        {
            suite.addTest(new ServersideTests("testInvalidEventIsNotAcceptedArgListIsNotOfTypeString",i));
        }
        
        int testHCount = 100;
        
        for(int i=0;i<testHCount;i++)
        {
            suite.addTest(new ServersideTests("testUnknownCommandNameIsNotAccepted",i));
        }
        
        int testICount = 100;
        
        for(int i=0;i<testICount;i++)
        {
            suite.addTest(new ServersideTests("testHackyStatActivityEventsAccepted",i));
        }
        
        int testJCount = 100;
        
        for(int i=0;i<testJCount;i++)
        {
            suite.addTest(new ServersideTests("testHackyStatBuildEventsAccepted",i));
        }
        
        int testKCount = 100;
        
        for(int i=0;i<testKCount;i++)
        {
            suite.addTest(new ServersideTests("testHackyStatBuffTransEventsAccepted",i));
        }
        
        int testLCount = 100;
        
        for(int i=0;i<testLCount;i++)
        {
            suite.addTest(new ServersideTests("testHackyStatCommitEventsAccepted",i));
        }
        
        int testMCount = 100;
        
        for(int i=0;i<testMCount;i++)
        {
            suite.addTest(new ServersideTests("testHackyStatFileMetricEventsAccepted",i));
        }
        
        int testNCount = 100;
        
        for(int i=0;i<testNCount;i++)
        {
            suite.addTest(new ServersideTests("testHackyStatUnitTestEventsAccepted",i));
        }
        
        return suite;
    }

}

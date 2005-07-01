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
        
        int testCCount = 1;
        
        for(int i=0;i<testCCount;i++)
        {
            suite.addTest(new ServersideTests("testInvalidEventIsNotAcceptedTimeStampIsNull"));
        }
        
        int testDCount = 1;
        
        for(int i=0;i<testDCount;i++)
        {
            suite.addTest(new ServersideTests("testInvalidEventIsNotAcceptedCommandNameIsNull"));
        }
        
        int testECount = 1;
        
        for(int i=0;i<testECount;i++)
        {
            suite.addTest(new ServersideTests("testInvalidEventIsNotAcceptedArgListIsNull"));
        }
        
        int testFCount = 1;
        
        for(int i=0;i<testFCount;i++)
        {
            suite.addTest(new ServersideTests("testInvalidEventIsNotAcceptedArgListIsEmpty"));
        }
        
        int testGCount = 1;
        
        for(int i=0;i<testGCount;i++)
        {
            suite.addTest(new ServersideTests("testInvalidEventIsNotAcceptedArgListIsNotOfTypeString"));
        }
        
        int testHCount = 1;
        
        for(int i=0;i<testHCount;i++)
        {
            suite.addTest(new ServersideTests("testUnknownCommandNameIsNotAccepted"));
        }
        
        int testICount = 1;
        
        for(int i=0;i<testICount;i++)
        {
            suite.addTest(new ServersideTests("testHackyStatActivityEventsAccepted"));
        }
        
        int testJCount = 1;
        
        for(int i=0;i<testJCount;i++)
        {
            suite.addTest(new ServersideTests("testHackyStatBuildEventsAccepted"));
        }
        
        int testKCount = 1;
        
        for(int i=0;i<testKCount;i++)
        {
            suite.addTest(new ServersideTests("testHackyStatBuffTransEventsAccepted"));
        }
        
        return suite;
    }

}

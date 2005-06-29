package org.electrocodeogram.test.connectivity;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * This class contains a suite of connection tests.
 */
public class ConnectionTestSuite
{

    /**
     * This returns the connection test suite.
     * @return The connection test suite
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite("Connection tests for the ECG");
//        
//        int testACount = 100;
//        
//        for(int i=0;i<testACount;i++)
//        {
//            suite.addTest(new ConnectionTests("testStayNotConnectedIfServerIsDown"));
//        }
//        
//        int testBCount = 5;
//        
//        for(int i=0;i<testBCount;i++)
//        {
//            suite.addTest(new ConnectionTests("testConnectionTrialsAreIncreasing"));
//        }
//        
//        int testCCount = 5;
//        
//        for(int i=0;i<testCCount;i++)
//        {
//            suite.addTest(new ConnectionTests("testDelayedSendingThreadAcceptsNewEvents"));
//        }
        
        int testDCount = 5;
        
        for(int i=0;i<testDCount;i++)
        {
            suite.addTest(new ConnectionTests("testAfterConnectionBufferIsEmptied"));
        }
        
        return suite;
        
        
    }

}

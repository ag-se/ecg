package org.electrocodeogram.test.connection.reliability;

import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * This class is a test suite for the reliability tests.
 * It calls all test multiple times with different event data.
 */
public class ReliabilityTestSuite
{

    /**
     * This returns the actual test suite.
     * @return The actual test suite
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite(
                "Reliability tests for the connection from the ECG SensorShell to the ECG Server&Lab");

        
        int testACount = 1000;
        
        for(int i=0;i<testACount;i++)
        {
            try {
                suite.addTest(new ReliabilityTests("testIfNoEventsAreLost",i%100));
            }
            catch (IOException e) {
                
                e.printStackTrace();
            }
        }
         
        int testBCount = 1000;
        
        for(int i=0;i<testBCount;i++)
        {
            try {
                suite.addTest(new ReliabilityTests("testIfEventPacketsAreNotAltered",i%100));
            }
            catch (IOException e) {
               
                e.printStackTrace();
            }
        }
        
        return suite;
    }

}

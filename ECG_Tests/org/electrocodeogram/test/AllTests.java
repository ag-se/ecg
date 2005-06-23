package org.electrocodeogram.test;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * This is a collection of all tests for the ECG framework. 
 * @author 7oas7er
 *
 */
public class AllTests
{

    /**
     * This method returns the colllection of all tests.
     * @return The colllection of all tests
     */
    public static Test suite()
    {
        TestSuite suite = new TestSuite("All test for the ECG");
        
        suite.addTest(ClientTestSuite.suite());
        
        suite.addTest(ConnectionTestSuite.suite());
        
        return suite;
    }

}

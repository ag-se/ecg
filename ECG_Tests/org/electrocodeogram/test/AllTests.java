package org.electrocodeogram.test;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests
{

    public static Test suite()
    {
        TestSuite suite = new TestSuite("All test for the ECG");
        
        suite.addTest(ClientTestSuite.suite());
        
        suite.addTest(ConnectionTestSuite.suite());
        
        return suite;
    }

}

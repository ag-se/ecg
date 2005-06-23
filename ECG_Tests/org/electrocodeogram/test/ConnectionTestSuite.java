package org.electrocodeogram.test;

import junit.framework.Test;
import junit.framework.TestSuite;

public class ConnectionTestSuite
{

    public static Test suite()
    {
        TestSuite suite = new TestSuite("Connection tests for the ECG");
        
        int testACount = 100;
        
        for(int i=0;i<testACount;i++)
        {
            suite.addTest(new ConnectionTests("testA"));
        }
        
        int testBCount = 5;
        
        for(int i=0;i<testBCount;i++)
        {
            suite.addTest(new ConnectionTests("testB"));
        }
        
        int testCCount = 5;
        
        for(int i=0;i<testCCount;i++)
        {
            suite.addTest(new ConnectionTests("testC"));
        }
        
        return suite;
        
        
    }

}

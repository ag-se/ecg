package org.electrocodeogram.test;

import junit.framework.Test;
import junit.framework.TestSuite;

public class ClientTestSuite
{
    
    public static Test suite()
    {
        TestSuite suite = new TestSuite("Test for the client side of the ECG");
        
        int testACount = 100;
        
        for(int i=0;i<testACount;i++)
        {
            suite.addTest(new ClientsideTests("testA"));
        }
        
        int testBCount = 100;
        
        for(int i=0;i<testBCount;i++)
        {
            suite.addTest(new ClientsideTests("testB"));
        }
        
        int testCCount = 100;
        
        for(int i=0;i<testCCount;i++)
        {
            suite.addTest(new ClientsideTests("testC"));
        }
        
        int testDCount = 100;
        
        for(int i=0;i<testDCount;i++)
        {
            suite.addTest(new ClientsideTests("testD"));
        }
        
        int testECount = 100;
        
        for(int i=0;i<testECount;i++)
        {
            suite.addTest(new ClientsideTests("testE"));
        }
        
        int testFCount = 100;
        
        for(int i=0;i<testFCount;i++)
        {
            suite.addTest(new ClientsideTests("testF"));
        }
        
        int testGCount = 100;
        
        for(int i=0;i<testGCount;i++)
        {
            suite.addTest(new ClientsideTests("testG"));
        }
        
        return suite;
    }

}

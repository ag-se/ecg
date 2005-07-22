package org.electrocodeogram.test.connection.stress;

import utmj.threaded.RetriedAssert;

public class RetriedAssertEqualsForValue extends RetriedAssert
{

    private int value = 0;
    
   
    
    protected RetriedAssertEqualsForValue(int timeOutMs, int intervalMs, int valuePar)
    {
        super(timeOutMs, intervalMs);
        
        this.value = valuePar;
    }

    @Override
    public void run() throws Exception
    {
        // TODO Auto-generated method stub
        
    }
    
    public int getValue()
    {
        return this.value;
    }

}

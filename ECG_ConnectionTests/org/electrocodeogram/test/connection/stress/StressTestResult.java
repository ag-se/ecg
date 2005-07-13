package org.electrocodeogram.test.connection.stress;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import sun.misc.Compare;
import sun.misc.Sort;

public class StressTestResult
{

    private Date dateOfTest = null;
    
    private int testCount = -1;
    
    private HashMap<Integer,ArrayList> byteValues = null;
    
    private ArrayList<Long> transmissionTimeValues = null;
    
    public StressTestResult(int testCountPar)
    {
        this.dateOfTest = new Date();
        
        this.testCount = testCountPar;
        
        this.byteValues = new HashMap<Integer,ArrayList>();
        
    }
    
    public void addTransmissionTime(int bytes, Long transmissionTime) throws AllreadyAddedException
    {
        if (!(this.byteValues.containsKey(bytes)))
        {
            ArrayList<Long> transmissionTimes = new ArrayList<Long>();
            
            transmissionTimes.add(transmissionTime);
            
            this.byteValues.put(bytes,transmissionTimes);
            
        }
        else
        {
            ArrayList<Long> transmissionTimes = this.byteValues.get(bytes);
            
            transmissionTimes.add(transmissionTime);
        }
        
    }

    /**
     * 
     */
    public void printOut()
    {
        System.out.println("Results for transmission times over payload size in Bytes");
        
        Integer[] byteValueArray = this.byteValues.keySet().toArray(new Integer[0]);
        
        Compare comp = new Compare(){

            public int doCompare(Object arg0, Object arg1)
            {
                Integer intA = (Integer) arg0;
                
                Integer intB = (Integer) arg1;
                
                if(intA == intB)
                {
                    return 0;
                }
                else if(intA > intB)
                {
                    return 1;
                }
                else
                {
                    return -1;
                }
                    
            }};
        
        Sort.quicksort(byteValueArray,comp);
        
        for(int i=0;i<byteValueArray.length;i++)
        {
            System.out.print("Payload in Bytes >= " + byteValueArray[i] + ": ");
            
            ArrayList<Long> transmissionTimes = this.byteValues.get(byteValueArray[i]);
            
            long sumOfTransmissionTimes = 0;
            
            for (int j=0;j<transmissionTimes.size();j++)
            {
//                if(j == 0)
//                {
//                    System.out.print(transmissionTimes.get(j));
//                }
//                else
//                {
//                    System.out.print("," + transmissionTimes.get(j));
//                }
                
                sumOfTransmissionTimes += transmissionTimes.get(j);
                
            }
            
            long averageTransmissionTime = sumOfTransmissionTimes / transmissionTimes.size();
            
            System.out.println(averageTransmissionTime);
        }
        
    }
    
}

package org.electrocodeogram.module.source;

import org.electrocodeogram.module.Module;
import org.electrocodeogram.sensorwrapper.EventPacket;
    /**
     * @author Frank Schlesinger
     * 
     * This nested class represents an event collector. It extends the class Module
     * ans is in fact the first module in the module hierarchy or the root module in
     * the module tree of the ECG framework.
     *
     */
    public class SensorSource extends Module
    {
        
        
        public SensorSource()
        {
            super(Module.SOURCE_MODULE, "Sensor Data Source");
        }
        
        public void append(EventPacket eventPacket)
        {
            assert(eventBuffer != null);
            
            eventBuffer.append(eventPacket);
                       
            processEventPacket(eventPacket);
            
            // TODO : make own processing mechanism
        }

       

        
    }
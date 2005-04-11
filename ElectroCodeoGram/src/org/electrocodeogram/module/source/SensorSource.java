package org.electrocodeogram.module.source;

import org.electrocodeogram.module.Module;
import org.electrocodeogram.EventPacket;
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
                       
            sendEventPacket(eventPacket);
        }

        /* (non-Javadoc)
         * @see org.electrocodeogram.module.Module#setProperty(java.lang.String, java.lang.Object)
         */
        public void setProperty(String currentPropertyName, Object propertyValue)
        {
            
        }

        /* (non-Javadoc)
         * @see org.electrocodeogram.module.Module#receiveEventPacket(org.electrocodeogram.EventPacket)
         */
        public void receiveEventPacket(EventPacket eventPacket)
        {
            // TODO Auto-generated method stub
            
        }

       

        
    }
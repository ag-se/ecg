package org.electrocodeogram.module.source;

import org.electrocodeogram.event.ValidEventPacket;
import org.electrocodeogram.module.Module;
    /**
     * @author Frank Schlesinger
     * 
     * This nested class represents an event collector. It extends the class Module
     * ans is in fact the first module in the module hierarchy or the root module in
     * the module tree of the ECG framework.
     *
     */
    public class SourceModule extends Module
    {
        
        
        public SourceModule()
        {
            super(Module.SOURCE_MODULE);
        }
        
        public void append(ValidEventPacket eventPacket)
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
        public void receiveEventPacket(ValidEventPacket eventPacket)
        {
            // TODO Auto-generated method stub
            
        }

       

        
    }
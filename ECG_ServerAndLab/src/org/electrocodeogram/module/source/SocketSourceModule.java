package org.electrocodeogram.module.source;

import org.electrocodeogram.core.Core;
import org.electrocodeogram.event.ValidEventPacket;
import org.electrocodeogram.module.Module;
import org.electrocodeogram.module.ModuleRegistry;
    /**
     * @author Frank Schlesinger
     * 
     * This nested class represents an event collector. It extends the class Module
     * ans is in fact the first module in the module hierarchy or the root module in
     * the module tree of the ECG framework.
     *
     */
    public class SocketSourceModule extends Module
    {
    	private SocketServer sensorServer = null;

    	
        public SocketSourceModule(Core corePar)
        {
            super(corePar.getModuleRegistry(),ModuleType.SOURCE_MODULE);
            
//        	start the ECG server to listen for incoming events
            this.sensorServer = new SocketServer(corePar.getSensorShellWrapper());
        }
        
        public void append(ValidEventPacket eventPacket)
        {
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
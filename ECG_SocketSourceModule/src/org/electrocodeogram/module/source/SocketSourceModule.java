package org.electrocodeogram.module.source;

import org.electrocodeogram.core.Core;
import org.electrocodeogram.event.ValidEventPacket;
import org.electrocodeogram.module.Module;
import org.electrocodeogram.msdt.EventValidator;

    /**
     * 
     * This nested class represents an event collector. It extends the class Module
     * and is in fact the first module in the module hierarchy or the root module in
     * the module tree of the ECG framework.
     *
     */
    public class SocketSourceModule extends Module implements ISourceModule
    {
    	private EventValidator eventValidator = null;
        
        /**
         * This creates the module.
         *
         */
        public SocketSourceModule()
        {
            super(ModuleType.SOURCE_MODULE);
            
            this.eventValidator = new EventValidator(Core.getInstance().getMsdtManager());
            
            // start the ECG server to listen for incoming events
            new SocketServer(this);
        }
        
        
        /**
         * @see org.electrocodeogram.module.source.ISourceModule#append(org.electrocodeogram.event.ValidEventPacket)
         */
        public void append(ValidEventPacket eventPacket)
        {
            if(this.eventValidator.validate(eventPacket))
            {
                sendEventPacket(eventPacket);
            }
        }

        /**
         * @see org.electrocodeogram.module.Module#setProperty(java.lang.String, java.lang.Object)
         */
        @Override
        public void setProperty(@SuppressWarnings("unused") String currentPropertyName, @SuppressWarnings("unused") Object propertyValue)
        {
            // not used
        }

       
        /**
         * @see org.electrocodeogram.module.Module#receiveEventPacket(org.electrocodeogram.event.ValidEventPacket)
         */
        @Override
        public void receiveEventPacket(@SuppressWarnings("unused") ValidEventPacket eventPacket)
        {
            // not used
            
        }

       

        
    }
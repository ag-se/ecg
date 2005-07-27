package org.electrocodeogram.module.source;
    /**
     * This module receives event data from multiple client sensors.
     * The communication is done over Sockets. Each new incoming
     * communication request starts a new SocketServerThread which
     * than receives the event data from the new client.
     *
     */
    public class SocketSourceModule extends SourceModule
    {

        /**
         * @see org.electrocodeogram.module.source.SourceModule#startReader(org.electrocodeogram.module.source.SourceModule)
         */
        @Override
        public void startReader(SourceModule sourceModule)
        {
            SocketServer socketServer = new SocketServer(sourceModule);
            
            socketServer.start();
            
        }

       
        /**
         * @see org.electrocodeogram.module.Module#setProperty(java.lang.String, java.lang.Object)
         */
        @Override
        public void setProperty(@SuppressWarnings("unused") String currentPropertyName, @SuppressWarnings("unused") Object propertyValue)
        {
            // TODO Auto-generated method stub
            
        }
    	
        
    }
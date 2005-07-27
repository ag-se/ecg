package org.electrocodeogram.module.source;
    /**
     * 
     * This module receives events in a SocketSourceModule fashion.
     * But instead of processing the received events into the ECG Lab
     * thy are simply echoed back to the sender.
     * So this module is build for testing the connection.
     */
    public class SocketEchoSourceModule extends SocketSourceModule
    {

        /**
         * @see org.electrocodeogram.module.source.SourceModule#startReader(org.electrocodeogram.module.source.SourceModule)
         */
        @Override
        public void startReader(SourceModule sourceModule)
        {
            SocketEchoServer socketServer = new SocketEchoServer(sourceModule);
            
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
package org.electrocodeogram.module.source;

import java.util.Observable;

import org.electrocodeogram.module.ModulePropertyException;
    /**
     * This module receives event data from multiple client sensors.
     * The communication is done over Sockets. Each new incoming
     * communication request starts a new SocketServerThread which
     * than receives the event data from the new client.
     *
     */
    public class SocketSourceModule extends SourceModule
    {

        private int port;
        
        private SocketServer socketServer;
        
        /**
         * @param moduleClassId
         * @param name
         */
        public SocketSourceModule(String moduleClassId, String name)
        {
            super(moduleClassId, name);
            
        }


        /**
         * @see org.electrocodeogram.module.source.SourceModule#startReader(org.electrocodeogram.module.source.SourceModule)
         */
        @Override
        public void startReader(SourceModule sourceModule)
        {
            this.socketServer = new SocketServer(sourceModule,this.port);
            
            this.socketServer.start();
            
        }

       
        /**
         * @throws ModulePropertyException 
         * @see org.electrocodeogram.module.Module#setProperty(java.lang.String, java.lang.Object)
         */
        @Override
        public void setProperty(String propertyName, Object propertyValue) throws ModulePropertyException
        {
            if(propertyName.equals("port"))
            {
                if(propertyValue instanceof String)
                {
                    String propertyValueString = (String) propertyValue;
                    
                    try
                    {
                        int portValue = Integer.parseInt(propertyValueString);
                        
                        if(portValue > 1024 && portValue < 65536)
                        {
                            this.port = portValue;
                            
                            this.socketServer.shutDown();
                            
                            this.startReader(this);
                            
                        }
                        
                        throw new ModulePropertyException("The value for the port property must be a number greater than 1024 and less then 65536.");
                         
                    }
                    catch(NumberFormatException e)
                    {
                        throw new ModulePropertyException("The value for the port property must be a number greater than 1024 and less then 65536.");
                    }
                }
                
                throw new ModulePropertyException("The value for the port property must be a number greater than 1024 and less then 65536.");
            }
        }


       public void analyseCoreNotification()
       {
           
       }


    /**
     * @see org.electrocodeogram.module.Module#initialize()
     */
    @Override
    public void initialize()
    {
        this.port = 22222;
        
        this.startReader(this);
    }
    	
        
    }
package org.electrocodeogram.module.source;
    /**
     * 
     * This nested class represents an event collector. It extends the class Module
     * and is in fact the first module in the module hierarchy or the root module in
     * the module tree of the ECG framework.
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
            new SocketServer(sourceModule);
            
        }

       
        /**
         * @see org.electrocodeogram.module.Module#setProperty(java.lang.String, java.lang.Object)
         */
        @Override
        public void setProperty(String currentPropertyName, Object propertyValue)
        {
            // TODO Auto-generated method stub
            
        }
    	
        
    }
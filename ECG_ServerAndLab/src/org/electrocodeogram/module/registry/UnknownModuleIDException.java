
package org.electrocodeogram.module.registry;


/**
 * This Exception is thrown when a request is made to a unknown module id.
 *
 */public class UnknownModuleIDException extends Exception
{

     /**
      * This creates the Exception with the given message. 
      * @param message
      */
    public UnknownModuleIDException(String message)
    {
        super(message);
    }

    /**
     * This creates the Exception. 
     */
    public UnknownModuleIDException()
    {
        super();
    }

    private static final long serialVersionUID = 5814851080758469529L;



    
}

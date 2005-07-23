package org.electrocodeogram.client;

/**
 * If the "host" or "port" value are malformed when being passed to the
 * SendingThread for connection to the ECG server, this exception is thrown.
 */
public class IllegalHostOrPortException extends Exception
{

    private static final long serialVersionUID = 1L;

}

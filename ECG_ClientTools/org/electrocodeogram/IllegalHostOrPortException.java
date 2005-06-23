package org.electrocodeogram;

/**
 * If the "host" or "port" value are malformed when beeing passed to the SendingThread for connection to the
 * ECG server, this exception is thrown.
 */
public class IllegalHostOrPortException extends Exception
{

    private static final long serialVersionUID = 1L;

}

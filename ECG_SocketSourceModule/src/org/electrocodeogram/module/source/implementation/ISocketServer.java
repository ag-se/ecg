
package org.electrocodeogram.module.source.implementation;

/**
 * This Interface is provided to avoid circular dependencies
 * between the SocketServer and the SocketServerThread class.
 */
public interface ISocketServer
{

    /**
     * This method removes a single ServerThread from the threadpool
     * in the case the ServerThread is not needed anymore.
     * @param id The unique ID of the ServerThread to remove. 
     */
    void removeSensorThread(int id);

}
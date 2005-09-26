package org.electrocodeogram.client;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.electrocodeogram.event.ValidEventPacket;
import org.electrocodeogram.logging.LogHelper;

/**
 * The class SendingThread provides asynchronous transfer of event data to the
 * ECG Server & Lab.
 * 
 * The SendingThread has an EventPacket buffer, which is filled continously by
 * the ECG sensor that uses this ECG SensorShell component. The EventBuffer is
 * implemented as a Monitor making the SendingThread wait if the buffersize is 0
 * and notifies the SendingThread when new EventPackets are added. So the
 * SendingThread only works when it has work to do.
 * 
 * If new EventPackets are added and a connection to the ECG server is
 * established they are sended to the ECG server via serialisation over sockets.
 * 
 * If the connection is not established a new connection approach is initiated
 * after a delay.
 */
public class SendingThread extends Thread
{

	/**
	 * 
	 */
	private static final int STREAM_RESET_COUNT = 100;

	/**
	 * 
	 */
	private static final int MIN_PORT = 1024;

	/**
	 * 
	 */
	private static final int MAX_PORT = 65565;

	/**
	 * This is the singleton instance of the SendingThread.
	 */
	protected static SendingThread theInstance;

	static Logger _logger = LogHelper.createLogger(SendingThread.class.getName());

	/**
	 * The FIFO EventPacket buffer to store event data temporarily. It is
	 * implemented as an ArrayList<EventPacket> Monitor.
	 */
	protected EventPacketQueue queue;

	/**
	 * This tells how often did the SendingThread try to connect since its
	 * creation.
	 */
	protected int connectionTrials = 0;

	private ObjectOutputStream _oos;

	/**
	 * This is the reference to the communication socket.
	 */
	protected Socket socketToServer;

	private InetAddress _host;

	private int _port = -1;

	/**
	 * If a connection attempt fails this tells, when the next connection
	 * attempt shall occur.
	 */
	protected int connectionDelay = 5000;

	private boolean run = false;

	/**
	 * This creates the instance of the SendingThread.
	 * 
	 * @param host
	 *            The InetAddress object giving the IP-Address/Hostname of the
	 *            ECG server
	 * @param port
	 *            The TCP port of the ECG server
	 * @throws IllegalHostOrPortException,
	 *             when the parameter values are illegal e.g. Host is "null" or
	 *             port is "-1". This exception is not thrown every time the
	 *             parameter values are illegal, but only when the parameter
	 *             values are used for the SendingThread because the
	 *             SendingThread's "host" and "port" values have not been set
	 *             yet.
	 */

	public SendingThread(InetAddress host, int port) throws IllegalHostOrPortException
	{

		_logger.entering(this.getClass().getName(), "SendingThread");

		// assert parameter value are legal
		if (host == null || port < MIN_PORT || port > MAX_PORT)
		{
			_logger.log(Level.INFO, "Host or Port values are illegal.");

			_logger.exiting(this.getClass().getName(), "SendingThread");

			throw new IllegalHostOrPortException();
		}

		this.queue = new EventPacketQueue();

		this._host = host;

		this._port = port;

		this.setDaemon(true);

		this.setPriority(Thread.MIN_PRIORITY);

		theInstance = this;

		this.start();

		_logger.exiting(this.getClass().getName(), "SendingThread");
	}

	/**
	 * This method is used to pass a new EventPacket to the SendingThread for
	 * transmission to the ECG server. The EventPacket is passed to the
	 * EventPacket buffer.
	 * 
	 * @param packet
	 *            This is the EventPacket to transmit.
	 * @return "true" if adding the EventPacket was successful and "false"
	 *         otherwise
	 */
	public boolean addEventPacket(ValidEventPacket packet)
	{
		_logger.entering(this.getClass().getName(), "addEventPacket");

		if (packet == null)
		{
			_logger.log(Level.FINEST, "packet is null");

			_logger.exiting(this.getClass().getName(), "addEventPacket");

			return false;
		}

		boolean result = this.queue.addToTail(packet);

		_logger.exiting(this.getClass().getName(), "addEventPacket");

		return result;

	}

	/*
	 * This private method tries to establish a Socket connection to the ECG
	 * server. If it fails it throws an IOException.
	 */
	private void connect()
	{
		_logger.entering(this.getClass().getName(), "connect");

		this.connectionTrials++;

		try
		{

			// open a new socket
			this.socketToServer = new Socket(this._host, this._port);

			_logger.log(Level.INFO, "Socket connection to " + this._host.toString() + ":" + this._port + " established");

			// create a stream upon the socket
			this._oos = new ObjectOutputStream(
					this.socketToServer.getOutputStream());

			_logger.log(Level.INFO, "Stream opened");

			_logger.log(Level.INFO, "Connected to the ECG Server at " + this._host.toString() + ":" + this._port);
		}
		catch (IOException e)
		{

			_logger.log(Level.WARNING, "Unable to connect to the ECG Server at " + this._host.toString() + ":" + this._port + " \nNext attempt in " + this.connectionDelay / 1000 + " seconds.");

			try
			{
				// delay for another attempt
				Thread.sleep(this.connectionDelay);

				connect();
			}
			catch (InterruptedException e1)
			{
				// this is not a problem
			}

		}

		_logger.exiting(this.getClass().getName(), "connect");

	}

	/**
	 * The run-method is doing the actual transmission to the ECG server. If any
	 * new EventPackets are in the buffer this Thread is notified and if a
	 * connection is established, sending EventPackets in the buffer is done.
	 * 
	 * Sent EventPackets are then removed from the buffer. If the connection is
	 * lost reconnection is initiated. After the last EventPacket has left the
	 * buffer this Thread blocks until new EventPackets are added.
	 */
	@Override
	public void run()
	{
		_logger.entering(this.getClass().getName(), "run");
		
		this.run = true;

		ValidEventPacket packet;

		// first attempt to connect to server
		connect();

		int count = 0;

		// is the SendingThread running?
		while (this.run)
		{

			/*
			 * Any EventPackets to transmit? This Thread blocks here
			 * if the buffer size is 0. If the size is greater than
			 * 0 the Thread sends one EventPacket after another and
			 * removes it from their buffer until the buffer size is
			 * 0 again and the Thread blocks again.
			 */
			while (this.queue.getSize() > 0)
			{

				// Are we still connected?
				if (this.socketToServer == null || !(this.socketToServer.isConnected()))
				{
					_logger.log(Level.INFO,"Connection is not established, reconnecting.");
					
					// reconnect
					connect();
				}
				try
				{
					// remove the oldest packet
					packet = this.queue.removeFromHead();

					// send packet serialized over socket
					this._oos.writeObject(packet);

					this._oos.flush();
					
					_logger.log(Level.FINEST,"Event written to Socket.");

					count++;

					if (count >= STREAM_RESET_COUNT)
					{
						this._oos.reset();

						count = 0;

					}

				}
				catch (EventPacketQueueUnderflowException e)
				{
					// this is checked in the while-loop condition
				}
				catch (IOException e)
				{

					_logger.log(Level.WARNING, "Lost connection to the ECG Lab.");

					this.socketToServer = null;
				}
			}
		}

		theInstance = null;
		
		_logger.exiting(this.getClass().getName(), "run");
	}

	/**
	 * This method tells whether the SendingThread is running or not.
	 * 
	 * @return "true" if the SendingThread is running and "false" otherwise
	 */
	public boolean isRunning()
	{
		_logger.entering(this.getClass().getName(), "isRunning");
		
		_logger.exiting(this.getClass().getName(), "isRunning");
		
		return this.run;
	}

	/**
	 * This class represents a queue with FIFO characteristic for buffering
	 * incoming EventPackets. It is also a Monitor for the SendingThread causing
	 * it to wait if the buffer is empty and notifying it if new EventPackets
	 * are added.
	 */
	private static class EventPacketQueue extends ArrayList<ValidEventPacket>
	{
		private static final long serialVersionUID = -7457045862890074109L;

		/**
		 * This method add a single EventPacket to the tail of the queue and
		 * notifies the SendingThread.
		 * 
		 * @param packet
		 *            The EventPacket to queue
		 * @return "true if cuing succeeded and "false" otherwise
		 */
		public synchronized boolean addToTail(ValidEventPacket packet)
		{
			_logger.entering(this.getClass().getName(), "addToTail");
			
			boolean result = this.add(packet);

			this.notifyAll();

			_logger.exiting(this.getClass().getName(), "addToTail");
			
			return result;
		}

		/**
		 * This method returns and removes the head-most EventPacket of the
		 * queue.
		 * 
		 * @return The head EventPacket
		 * @throws EventPacketQueueUnderflowException
		 *             If the queue is empty already
		 */
		public synchronized ValidEventPacket removeFromHead() throws EventPacketQueueUnderflowException
		{
			
			_logger.entering(this.getClass().getName(), "removeFromHead");
			
			int sizeBefore;

			if ((sizeBefore = this.size()) > 0)
			{

				ValidEventPacket packet = this.get(0);

				this.remove(0);

				assert (this.size() == sizeBefore - 1);

				_logger.exiting(this.getClass().getName(), "removeFromHead");
				
				return packet;

			}

			_logger.log(Level.SEVERE,"An EventPacketQueueUnderflowException has occured.");
			
			throw new EventPacketQueueUnderflowException();

			
		}

		/**
		 * This method returns the number of EventPackets in the queue and
		 * causes the SendingThread to wait if the size is 0.
		 * 
		 * @return The number of EventPackets
		 */
		public synchronized int getSize()
		{
			_logger.entering(this.getClass().getName(), "getSize");
			
			int size = this.size();

			if (size == 0)
			{
				try
				{
					this.wait();
				}
				catch (InterruptedException e)
				{
					_logger.log(Level.WARNING,"SendingThread was interrupted hile waiting for EventQueue to fill.");
				}
			}

			_logger.exiting(this.getClass().getName(), "getSize");
			
			return this.size();
		}

	}

	/**
	 * This returns the singleton instance of the SendingThread.
	 * @return The singleton instance of the SendingThread
	 */
	protected static SendingThread getInstance()
	{
		_logger.entering(SendingThread.class.getName(), "getInstance");
		
		_logger.exiting(SendingThread.class.getName(), "getInstance");
		
		return theInstance;
	}

	  /**
     * This method tries to send one byte of data to the ECG server.
     * @return "true" if sending was successful and "false" otherwise
     */
    protected boolean ping()
    {
    	_logger.entering(this.getClass().getName(), "ping");
    	
        try {
            this.socketToServer.sendUrgentData(0);

            _logger.exiting(this.getClass().getName(), "ping");
            
            return true;
        }
        catch (Exception e)
        {
        	_logger.exiting(this.getClass().getName(), "ping");
        	
            return false;
        }
    }

	
	/**
	 * This Exception shall be thrown, when the EventPacketQueue is empty but an
	 * EventPacket is to be removed.
	 * 
	 */
	private static class EventPacketQueueUnderflowException extends Exception
	{
		private static final long serialVersionUID = 870916601241806158L;

	}
}

package org.electrocodeogram.module.source;

import java.net.InetAddress;

public interface ISocketServerThread {

	/**
	 * This method returns the name of the currently connected ECG sensor.
	 * @return The name of the currently connected ECG sensor
	 */
	public abstract String getSensorName();

	/**
	 * This method returns the IP address of the currently connected ECG sensor.
	 * @return The IP address of the currently connected ECG sensor
	 */
	public abstract InetAddress getSensorAddress();

	/**
	 * This method returns the unique ID of the ServerThread.
	 * @return The unique ID of the ServerThread
	 */
	public abstract int getServerThreadId();

	/**
	 * This method stops this ServerThread and closes the socket and stream.
	 *
	 */
	public abstract void stopSensorThread();

	public abstract void start();



}
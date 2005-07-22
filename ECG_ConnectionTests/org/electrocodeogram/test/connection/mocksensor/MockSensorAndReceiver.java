package org.electrocodeogram.test.connection.mocksensor;

import org.electrocodeogram.module.source.SocketServer;
import org.electrocodeogram.test.client.mocksensor.MockSensor;
import org.electrocodeogram.test.connection.mockserver.MockSensorshellWrapper;



public class MockSensorAndReceiver extends MockSensor
{
	
	private SocketServer socketServer = null;
	
	private MockSensorshellWrapper mockSensorShellWrapper = null;
	
	public MockSensorAndReceiver()
	{
		
		this.mockSensorShellWrapper = new MockSensorshellWrapper();
		
		this.socketServer = new SocketServer(this.mockSensorShellWrapper);
		
	}
	
	
}

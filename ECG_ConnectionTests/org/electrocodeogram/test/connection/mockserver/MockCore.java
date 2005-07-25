package org.electrocodeogram.test.connection.mockserver;

import org.electrocodeogram.core.ICore;
import org.electrocodeogram.module.registry.ModuleRegistry;
import org.electrocodeogram.msdt.IEventValidator;
import org.electrocodeogram.msdt.MsdtManager;
import org.electrocodeogram.ui.Gui;
import org.electrocodeogram.ui.messages.GuiEventWriter;

public class MockCore implements ICore
{

	private MockSocketServer mockSocketServer = null;
	
	private MockSensorshellWrapper mockSensorshellWrapper = null;
	
	public MockCore()
	{
		this.mockSensorshellWrapper = new MockSensorshellWrapper();
		
		this.mockSocketServer = new MockSocketServer(this.mockSensorshellWrapper);
	}
	
	public MsdtManager getMsdtManager() {
		// TODO Auto-generated method stub
		return null;
	}

	public ModuleRegistry getModuleRegistry() {
		// TODO Auto-generated method stub
		return null;
	}

	public IEventValidator getSensorShellWrapper() {
		// TODO Auto-generated method stub
		return null;
	}

	public Gui getConfigurator() {
		// TODO Auto-generated method stub
		return null;
	}

	public GuiEventWriter getGuiEventWriter() {
		// TODO Auto-generated method stub
		return null;
	}

}

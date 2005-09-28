package org.electrocodeogram.test.server.modules;

import org.electrocodeogram.event.TypedValidEventPacket;
import org.electrocodeogram.module.ModulePropertyException;
import org.electrocodeogram.module.target.TargetModule;
import org.electrocodeogram.module.target.TargetModuleException;

/**
 *
 */
public class MockTargetModule extends TargetModule
{

	private ModuleTestHelper _moduleTestHelper;

	public MockTargetModule(ModuleTestHelper helper)
	{
		super("org.electrocodeogram.test.server.modules.TestTargetModule", "TestTargetModule");
		
		this._moduleTestHelper = helper;
	}

	/**
	 * @see org.electrocodeogram.module.target.TargetModule#write(org.electrocodeogram.event.ValidEventPacket)
	 */
	@Override
	public void write(TypedValidEventPacket eventPacket)
	{
		this._moduleTestHelper.comparePackets(eventPacket);
		
	}

	/**
	 * @see org.electrocodeogram.module.target.TargetModule#initialize()
	 */
	@Override
	public void initialize()
	{
		// not implemented
		
	}

	/**
	 * @see org.electrocodeogram.module.Module#analyseCoreNotification()
	 */
	@Override
	public void analyseCoreNotification()
	{
		// not implemented
		
	}

	/**
	 * @see org.electrocodeogram.module.Module#setProperty(java.lang.String, java.lang.Object)
	 */
	@Override
	public void setProperty(String currentPropertyName, String propertyValue) throws ModulePropertyException
	{
		// not implemented
		
	}

	
	/**
	 * @see org.electrocodeogram.module.target.TargetModule#startWriter()
	 */
	@Override
	public void startWriter() throws TargetModuleException
	{
		// not implemented
		
	}

	/**
	 * @see org.electrocodeogram.module.target.TargetModule#stopWriter()
	 */
	@Override
	public void stopWriter()
	{
		// not implemented
		
	}

}

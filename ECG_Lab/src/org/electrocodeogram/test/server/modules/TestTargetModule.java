/**
 * 
 */
package org.electrocodeogram.test.server.modules;

import org.electrocodeogram.event.TypedValidEventPacket;
import org.electrocodeogram.event.ValidEventPacket;
import org.electrocodeogram.module.ModulePropertyException;
import org.electrocodeogram.module.target.TargetModule;

/**
 *
 */
public class TestTargetModule extends TargetModule
{

	private ModuleTestHelper helper;

	public TestTargetModule(ModuleTestHelper helper)
	{
		super("org.electrocodeogram.test.server.modules.TestTargetModule", "TestTargetModule");
		
		this.helper = helper;
	}

	/* (non-Javadoc)
	 * @see org.electrocodeogram.module.target.TargetModule#write(org.electrocodeogram.event.ValidEventPacket)
	 */
	@Override
	public void write(TypedValidEventPacket eventPacket)
	{
		helper.comparePackets(eventPacket);
		
	}

	/* (non-Javadoc)
	 * @see org.electrocodeogram.module.target.TargetModule#initialize()
	 */
	@Override
	public void initialize()
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.electrocodeogram.module.Module#analyseCoreNotification()
	 */
	@Override
	public void analyseCoreNotification()
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.electrocodeogram.module.Module#setProperty(java.lang.String, java.lang.Object)
	 */
	@Override
	public void setProperty(String currentPropertyName, String propertyValue) throws ModulePropertyException
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.electrocodeogram.module.Module#getProperty(java.lang.String)
	 */
	@Override
	public String getProperty(String currentPropertyName)
	{
		// TODO Auto-generated method stub
		return null;
	}

}

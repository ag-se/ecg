
package org.electrocodeogram.module.setup;

import java.util.ArrayList;

/**
 *
 */
public class ModuleSetup
{

	private ArrayList<ModuleConfiguration> _moduleSetup;
	
	public ModuleSetup()
	{
		this._moduleSetup = new ArrayList<ModuleConfiguration>();
	}
	
	public void addModuleConfiguration(ModuleConfiguration moduleConfiguration)
	{
		this._moduleSetup.add(moduleConfiguration);
	}
	
	public ModuleConfiguration[] getModuleConfigurations()
	{
		return this._moduleSetup.toArray(new ModuleConfiguration[this._moduleSetup.size()]);
	}
}

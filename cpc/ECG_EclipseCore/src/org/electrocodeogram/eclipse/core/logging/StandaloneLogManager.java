package org.electrocodeogram.eclipse.core.logging;


import java.util.Properties;

import org.apache.log4j.Hierarchy;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.spi.RootLogger;
import org.electrocodeogram.eclipse.core.ECGEclipseCorePlugin;


public class StandaloneLogManager implements ILogManager
{
	private Hierarchy hierarchy;

	public StandaloneLogManager(Properties properties)
	{
		this.hierarchy = new Hierarchy(new RootLogger(Level.DEBUG));
		//this.hierarchy.addHierarchyEventListener(new PluginEventListener());
		new PropertyConfigurator().doConfigure(properties, this.hierarchy);
		ECGEclipseCorePlugin.getDefault().getPluginLogRegistry().addLogManager(this);
	}

	@Override
	public Logger getLogger(String name)
	{
		return this.hierarchy.getLogger(name);
	}

	@Override
	public JDKStyleLogger getJDKStyleLogger(String name)
	{
		return new JDKStyleLogger(this.hierarchy.getLogger(name));
	}

	@Override
	public void shutdown()
	{
	}

}

package org.electrocodeogram.core;

import org.electrocodeogram.module.ModuleRegistry;
import org.electrocodeogram.msdt.MsdtManager;
import org.electrocodeogram.ui.Configurator;
import org.electrocodeogram.ui.messages.GuiEventWriter;

public interface ICore {

	/**
	 * This method returns a reference to the MicroSensorDataTypeManager (MsdtManager) component,
	 * which is a registry for legal types of event data.
	 * @return A reference to the MicroSensorDataTypeManager
	 */
	public abstract MsdtManager getMsdtManager();

	/**
	 * This method returns a reference to the ModuleRegistry component,
	 * which is managing installed and running modules.
	 * @return A reference to the ModuleRegistry
	 */
	public abstract ModuleRegistry getModuleRegistry();

	/**
	 * This is returning a reference to the SensorShellWrapper, which validates all incoming event data.
	 * @return A reference to the SensorShellWrapper
	 */
	public abstract SensorShellWrapper getSensorShellWrapper();

	/**
	 * The main GUI component is accessible through this method.
	 * @return The Configurator, beeing the main GUI component
	 */
	public abstract Configurator getConfigurator();

	
	public GuiEventWriter getGuiEventWriter();
}
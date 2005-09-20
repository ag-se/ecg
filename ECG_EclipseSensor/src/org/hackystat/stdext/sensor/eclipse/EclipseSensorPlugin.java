package org.hackystat.stdext.sensor.eclipse;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.electrocodeogram.sensor.eclipse.ECGEclipseSensor;

/**
 * This is the ECG EclipseSensor plug-in. At Eclipse startup this
 * class creates the actual ECG EclipseSensor.
 *
 */
public class EclipseSensorPlugin extends AbstractUIPlugin implements IStartup {
  
  private static EclipseSensorPlugin plugin;
  
 /**
 * This creates the plug-in.
 * @param descriptor Is the plug-in descriptor. 
 */
  @SuppressWarnings({"deprecation","deprecation"})
public EclipseSensorPlugin(IPluginDescriptor descriptor) {
    super(descriptor);
    EclipseSensorPlugin.plugin = this;
  }
  
 
  
 /**
  * This method is called from the Eclipse runtime during startup.
  */
  public void earlyStartup() {
         
      ECGEclipseSensor.getInstance();
  }

  /**
   * This returns the singleton instance of the ECG EclipseSensor plug-in.
   * @return The singleton instance of the ECG EclipseSensor plug-in
   */
  public static EclipseSensorPlugin getInstance() {
    return plugin;
  }
  
  /**
   * This returns the current workspace.
   * @return The current workspace
   */
  public static IWorkspace getWorkspace() {
      return ResourcesPlugin.getWorkspace();
    }
  
  public void log(Exception e) {
	    IStatus status =
	      new Status(IStatus.ERROR, this.getDescriptor().getUniqueIdentifier(), 0, e.getMessage(), e);
	    plugin.getLog().log(status);
	  }

}

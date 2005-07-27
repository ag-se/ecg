package org.hackystat.stdext.sensor.eclipse;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.electrocodeogram.sensor.eclipse.ECGEclipseSensor;


public class EclipseSensorPlugin extends AbstractUIPlugin implements IStartup {
  
  private static EclipseSensorPlugin plugin;


  public EclipseSensorPlugin(IPluginDescriptor descriptor) {
    super(descriptor);
    EclipseSensorPlugin.plugin = this;
  }
  
 
  
 
  public void earlyStartup() {
         
      ECGEclipseSensor.getInstance();
  }

  
  public static EclipseSensorPlugin getInstance() {
    return plugin;
  }
  
  public static IWorkspace getWorkspace() {
      return ResourcesPlugin.getWorkspace();
    }
 
}

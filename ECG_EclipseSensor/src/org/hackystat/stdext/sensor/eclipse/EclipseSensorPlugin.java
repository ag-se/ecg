package org.hackystat.stdext.sensor.eclipse;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * Provides a main plug-in functionality for Eclipse, namely instantiation of the
 * <code>org.hackystat.stdext.sensor.eclipse.EclipseSensor</code> class to start gathering
 * necessary data.
 * <p>
 * Since <code>earlyStartup()</code> method was called when Eclipse runs, there is a major
 * instantiation in the method such as instantiation of EclipseSensor.
 *
 * @author Takuya Yamashita
 * @version $Id: EclipseSensorPlugin.java,v 1.11 2004/07/22 09:54:39 takuyay Exp $
 */
public class EclipseSensorPlugin extends AbstractUIPlugin implements IStartup {
  /** The shared instance. */
  private static EclipseSensorPlugin plugin;

  /**
   * Creates an Hackystat sensor plug-in runtime object for the given plug-in descriptor.
   * <p>Note that instances of plug-in runtime classes are automatically created by
   * the platform in the course of plug-in activation.
   *
   * @param descriptor the plug-in descriptor
   */
  public EclipseSensorPlugin(IPluginDescriptor descriptor) {
    super(descriptor);
    EclipseSensorPlugin.plugin = this;
  }
  
  /**
   * Gets the path to the sensorshell.jar.
   * @return the path to the sensorshell.jar.
   */
  public String getSensorShellPath() {
    URL pluginUrl = EclipseSensorPlugin.getInstance().getDescriptor().getInstallURL();
    try {
      return Platform.asLocalURL(new URL(pluginUrl, "sensorshell.jar")).getFile();
    }
    catch (Exception e) {
      return null;
    }
  }

  /**
   * Instantiates EclipseSensor class so that the collection for necessary data is ready. Note that
   * this is called when workbench starts up. This method must be overridden due to IStartup
   * interface. Because of this method, this class is instantiated on startup.
   *
   * @see IStartup
   */
  public void earlyStartup() {
    // Instantiates EclipseSensor instance.
    EclipseSensor.getInstance();
  }

  /**
   * Returns the shared instance.
   *
   * @return The this plug-in instance.
   */
  public static EclipseSensorPlugin getInstance() {
    return plugin;
  }
  
  /**
   * Creates a image descriptor form the given path. The path should be the relative path from a
   * project root.
   *
   * @param path the relative path from a project root.
   *
   * @return the newly created image descriptor.
   */
  public static ImageDescriptor createImageDescriptor(String path) {
    try {
      URL url = getInstance().getDescriptor().getInstallURL();
      return ImageDescriptor.createFromURL(new URL(url, path));
    }
    catch (MalformedURLException e) {
      return ImageDescriptor.getMissingImageDescriptor();
    }
  }

  /**
   * Returns the workspace instance. This method might be overridden due to AbstractUIPlugin
   * abstract class although it is not necessary to be overridden.
   *
   * @return The IWorkspace instance.
   *
   * @see AbstractUIPlugin
   */
  public static IWorkspace getWorkspace() {
    return ResourcesPlugin.getWorkspace();
  }

  /**
   * Initializes a preference store with default preference values for this plug-in. This method
   * could be overridden due to AbstractUIPlugin abstract class. It's because it is empty
   * protected method in the AbstractUIPlugin.
   *
   * @param store the preference store to fill
   *
   * @see AbstractUIPlugin
   */
  protected void initializeDefaultPreferences(IPreferenceStore store) {
    store.setDefault("formatOnSave", true);
    
    // Stores the preference page default values.
//    store.setDefault(PreferenceConstants.ENABLE_ECLIPSE_SENSOR, false);
//    store.setDefault(PreferenceConstants.HACKYSTAT_KEY, "ChangeThisToYourPersonalKey");
//    store.setDefault(PreferenceConstants.HACKYSTAT_HOST, "http://hackystat.ics.hawaii.edu/");
//    store.setDefault(PreferenceConstants.HACKYSTAT_AUTOSEND_INTERVAL, "10");
//    store.setDefault(PreferenceConstants.HACKYSTAT_STATE_CHANGE_INTERVAL, "30");
//    store.setDefault(PreferenceConstants.ECLIPSE_UPDATE_URL, 
//                     "http://hackystat.ics.hawaii.edu/hackystat/download/eclipse/site.xml");
//    store.setDefault(PreferenceConstants.HACKYSTAT_BUFFTRANS_INTERVAL, "2");
//    store.setDefault(PreferenceConstants.ENABLE_ECLIPSE_MONITOR_SENSOR, true);
//    store.setDefault(PreferenceConstants.ENABLE_ECLIPSE_UPDATE_SENSOR, true); 
//    store.setDefault(PreferenceConstants.ENABLE_ECLIPSE_BUFFTRANS_SENSOR, false);  
    
  }

  /**
   * Logs out the exception or error message for Eclispe sensor plug-in.
   * 
   * @param e Exception. 
   */
  public void log(Exception e) {
    IStatus status =
      new Status(IStatus.ERROR, this.getDescriptor().getUniqueIdentifier(), 0, e.getMessage(), e);
    plugin.getLog().log(status);
  }
}

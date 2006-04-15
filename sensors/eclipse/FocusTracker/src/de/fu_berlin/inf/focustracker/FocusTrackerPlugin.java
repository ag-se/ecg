package de.fu_berlin.inf.focustracker;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;


/**
 * The main plugin class to be used in the desktop.
 */
public class FocusTrackerPlugin extends AbstractUIPlugin implements IStartup {

	//The shared instance.
	private static FocusTrackerPlugin plugin;
	
	/**
	 * The constructor.
	 */
	public FocusTrackerPlugin() {
		plugin = this;
	}
	
	/**
	 * Used to start plugin on startup -> entry in plugin.xml to invoke this
	 */
	public void earlyStartup() {
		if(PlatformUI.isWorkbenchRunning()) {
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {
					EventDispatcher.getInstance();
				}
			});
		}
		System.err.println("earlyStartup!");
	}	
	
	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
//		EventDispatcher.getInstance();
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
	}

	/**
	 * Returns the shared instance.
	 *
	 * @return the shared instance.
	 */
	public static FocusTrackerPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path.
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return AbstractUIPlugin.imageDescriptorFromPlugin("FocusTracker", path);
	}
}

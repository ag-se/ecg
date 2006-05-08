package de.fu_berlin.inf.focustracker;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Preferences.IPropertyChangeListener;
import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import de.fu_berlin.inf.focustracker.ui.FocusTrackerDecorator;
import de.fu_berlin.inf.focustracker.ui.preferences.PreferenceConstants;


/**
 * The main plugin class to be used in the desktop.
 */
public class FocusTrackerPlugin extends AbstractUIPlugin implements IStartup, IPropertyChangeListener {

	public static final String ID = "de.fu_berlin.inf.focustracker";
	
	//The shared instance.
	private static FocusTrackerPlugin plugin;
	
	/**
	 * The constructor.
	 */
	public FocusTrackerPlugin() {
		plugin = this;
		// listen to changes of the preferences
	}
	
	/**
	 * Used to start plugin on startup -> entry in plugin.xml to invoke this
	 */
	public void earlyStartup() {
//		System.err.println("earlyStartup!");
		if(PlatformUI.isWorkbenchRunning()) {
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {
					EventDispatcher.getInstance();
					FocusTrackerPlugin.getDefault().getPluginPreferences().addPropertyChangeListener(FocusTrackerPlugin.this);
				}
			});
		}
	}	
	
	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		FocusTrackerPlugin.getDefault().getWorkbench().getDecoratorManager().setEnabled(FocusTrackerDecorator.ID, FocusTrackerDecorator.isDecoratorActivated());		
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		EventDispatcher.getInstance().shutdown();
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
	
	public void propertyChange(PropertyChangeEvent aEvent) {
		if(PreferenceConstants.P_DECORATOR_ACTIVATED.equals(aEvent.getProperty())) {
			try {
				FocusTrackerPlugin.getDefault().getWorkbench().getDecoratorManager().setEnabled(ID, FocusTrackerDecorator.isDecoratorActivated());
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
				//LightweightDecoratorManager.
		}
	}
	
	
}

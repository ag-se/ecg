/*
 * Class: EclipseSensorPlugin
 * Version: 1.0
 * Date: 16.10.2005
 * By: Frank@Schlesinger.com
 */

package org.hackystat.stdext.sensor.eclipse;

import java.io.IOException;
import java.net.URL;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.plugin.AbstractUIPlugin;
//import org.electrocodeogram.logging.LogHelper;
import org.electrocodeogram.sensor.eclipse.ECGEclipseSensor;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * This is an <em>Eclipse plugin</em>, that is started when
 * <em>Eclipse</em> is started. This plugin is a
 * <em>ElectroCodeoGram</em> sensor, recording
 * <em>MicorActivityEvents</em> in <em>eclipse</em> and sending
 * them to the ECG Lab, where thay arae analysed and made persisitent.<br>
 * This plugin is implemented with the <em>Singleton</em> design
 * pattern, so that the information form the plugin'senvironment are
 * globally accessible throughout all plugin's classes.
 */
public class EclipseSensorPlugin extends AbstractUIPlugin implements IStartup {

    /**
     * The <em>Singleton</em> instance.
     */
    private static EclipseSensorPlugin plugin;

    /**
     * This is the path to this plugin in the file system.
     */
    private String sensorPath;

    /**
     * The constructor creates the plugin instance. It is not to be
     * used by developers, instead it is called from the
     * <em>Eclipse</em> runtime at startup.
     * @param descriptor
     *            Is the plugin descriptor.
     */
    @SuppressWarnings( {"deprecation", "deprecation"})
    public EclipseSensorPlugin(final IPluginDescriptor descriptor) {
        super(descriptor);

        EclipseSensorPlugin.plugin = this;

    }

    /**
     * Returns the path to this plugin in the file system.
     * @return The path to this plugin
     */
    public final String getSensorPath() {
        return this.sensorPath;
    }

    /**
     * If an <em>Eclipse</em> plugin must be started when
     * <em>Eclipse</em> starts, it has to implement the
     * <em>IStartup</em> <code>Interface</code> by implementing
     * this method.
     */
    @SuppressWarnings("deprecation")
    public final void earlyStartup() {

        Bundle bundle = Platform.getBundle(this.getDescriptor()
            .getUniqueIdentifier());

        Path path = new Path(".");

        URL fileURL = Platform.find(bundle, path);

        URL url = null;

        try {
            url = Platform.resolve(fileURL);

            this.sensorPath = url.getFile();
        } catch (IOException e) {

            log(e);

        }

        // Create the ECG EclipseSensor
        ECGEclipseSensor.getInstance();

    }

    /**
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public final void stop(final BundleContext context) throws Exception {

        super.stop(context);
    }

    /**
     * This returns the <em>Singleton</em> instance of the
     * <em>ECG EclipseSensor</em> plugin.
     * @return The <em>Singleton</em> instance of the
     *         <em>ECG EclipseSensor</em> plugin
     */
    public static EclipseSensorPlugin getInstance() {

        return plugin;
    }

    /**
     * This returns the current <em>Eclipse Workspace</em>.
     * @return The current <em>Eclipse Workspace</em>
     */
    public static IWorkspace getWorkspace() {

        return ResourcesPlugin.getWorkspace();
    }

    /**
     * Gets the path to the sensorshell.jar.
     * @return the path to the sensorshell.jar.
     */
    public String getSensorShellPath() {
        URL pluginUrl = EclipseSensorPlugin.getInstance().getDescriptor()
            .getInstallURL();
        try {
            return Platform.asLocalURL(new URL(pluginUrl, "sensorshell.jar"))
                .getFile();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * This method can be used to log an Exception into the
     * <em>Eclipse</em> "Error Log".
     * @param e
     *            Is an <code>Excpetion</code> to log
     */
    @SuppressWarnings("deprecation")
    public final void log(final Exception e) {
        if (e == null) {
            return;
        }

        IStatus status = new Status(IStatus.ERROR, this.getDescriptor()
            .getUniqueIdentifier(), 0, e.getMessage(), e);
        plugin.getLog().log(status);
    }

}

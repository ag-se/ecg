/*
 * Class: EclipseSensorPlugin
 * Version: 1.0
 * Date: 16.10.2005
 * By: Frank@Schlesinger.com
 */

package org.hackystat.stdext.sensor.eclipse;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.electrocodeogram.logging.LogHelper;
import org.electrocodeogram.sensor.eclipse.ECGEclipseSensor;
import org.hackystat.kernel.shell.SensorShell;
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
     * This is the logger.
     */
    private static Logger logger = LogHelper
        .createLogger(EclipseSensorPlugin.class.getName());

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
    public EclipseSensorPlugin(final IPluginDescriptor descriptor) {
        super(descriptor);

        logger.entering(this.getClass().getName(), "EclipseSensorPlugin",
            new Object[] {descriptor});

        EclipseSensorPlugin.plugin = this;

        logger.exiting(this.getClass().getName(), "EclipseSensorPlugin");
    }

    /**
     * Returns the path to this plugin in the file system.
     * @return The path to this plugin
     */
    public final String getSensorPath() {
        logger.entering(this.getClass().getName(), "getSensorPath");

        logger.exiting(this.getClass().getName(), "getSensorPath",
            this.sensorPath);

        return this.sensorPath;
    }

    /**
     * If an <em>Eclipse</em> plugin must be started when
     * <em>Eclipse</em> starts, it has to implement the
     * <em>IStartup</em> <code>Interface</code> by implementing
     * this method.
     */
    public final void earlyStartup() {

        logger.entering(this.getClass().getName(), "earlyStartup");

        Bundle bundle = Platform.getBundle(this.getDescriptor()
            .getUniqueIdentifier());

        Path path = new Path(".");

        URL fileURL = Platform.find(bundle, path);

        URL url = null;

        try {
            url = Platform.resolve(fileURL);

            this.sensorPath = url.getFile();
        } catch (IOException e) {

            logger
                .log(Level.SEVERE,
                    "An error occured while determining the path to the Eclipse plugin.");

            logger.log(Level.SEVERE, "Inlineserver mode is not available.");

        }

        // Create the ECG EclipseSensor
        ECGEclipseSensor.getInstance();

        logger.exiting(this.getClass().getName(), "earlyStartup");
    }

    /**
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
     */
    public final void stop(final BundleContext context) throws Exception {

        logger.entering(this.getClass().getName(), "stop",
            new Object[] {context});

        logger.log(Level.INFO, "ECG EclipsePlugin is stopping...");

        SensorShell.stop();

        logger.exiting(this.getClass().getName(), "stop");

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
     * This method can be used to log an Exception into the
     * <em>Eclipse</em> "Error Log".
     * @param e
     *            Is an <code>Excpetion</code> to log
     */
    public final void log(final Exception e) {
        if (e == null) {
            return;
        }

        IStatus status = new Status(IStatus.ERROR, this.getDescriptor()
            .getUniqueIdentifier(), 0, e.getMessage(), e);
        plugin.getLog().log(status);
    }

}

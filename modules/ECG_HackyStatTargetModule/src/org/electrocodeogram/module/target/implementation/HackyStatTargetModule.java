/*
 * Class: HackyStatTargetModule
 * Version: 1.0
 * Date: 16.10.2005
 * By: Frank@Schlesinger.com
 */

package org.electrocodeogram.module.target.implementation;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.electrocodeogram.event.ValidEventPacket;
import org.electrocodeogram.logging.LogHelper;
import org.electrocodeogram.modulepackage.ModuleProperty;
import org.electrocodeogram.module.target.TargetModule;
import org.electrocodeogram.module.target.TargetModuleException;
import org.hackystat.kernel.admin.SensorProperties;
import org.hackystat.kernel.shell.SensorShell;

/**
 * This class is an ECG module used to send ECG events to a <em>HackyStat
 * Server</em>.
 */
public class HackyStatTargetModule extends TargetModule {

    /**
     * This is the logger.
     */
    private static Logger logger = LogHelper
        .createLogger(HackyStatTargetModule.class.getName());

    /**
     * The <em>HackyStat SensorShell</em>, which is a component that is used to
     * send events to the <em>HackyStat Server</em>.
     */
    private SensorShell shell;

    /**
     * A reference to a <em>HackyStat SensorProperties</em> object needed
     * to create the <em>HackyStat SensorShell</em>.
     */
    private SensorProperties properties;

    /**
     * The IP-Address/Hostname of the <em>HackyStat Server</em>.
     */
    private String host;

    /**
     * The <em>Admin-</em> or <em>User-Key</em> to access the <em>HackyStat Server</em>.
     */
    private String key;

    /**
     * This creates the module instance. It is not to be
     * called by developers, instead it is called from the <em>ECG
     * ModuleRegistry</em> subsystem, when the user requested a new instance of this
     * module.
     * @param id
     *            This is the unique <code>String</code> id of the module
     * @param name
     *            This is the name which is assigned to the module
     *            instance
     */
    public HackyStatTargetModule(final String id, final String name) {
        super(id, name);

        logger.entering(this.getClass().getName(), "HackyStatTargetModule",
            new Object[] {id, name});

        logger.exiting(this.getClass().getName(), "HackyStatTargetModule");

    }

    /**
     * @see org.electrocodeogram.module.target.TargetModule#write(org.electrocodeogram.event.ValidEventPacket)
     */
    @Override
    public final void write(final ValidEventPacket arg0) {

        logger
            .entering(this.getClass().getName(), "write", new Object[] {arg0});

        if (this.shell == null) {

            logger.log(Level.WARNING,
                "The SensorShell is null. The event is ignored.");

            logger.exiting(this.getClass().getName(), "write");

            return;
        }

        this.shell.doCommand(arg0.getTimeStamp(), arg0.getSensorDataType(),
            arg0.getArgList());

        this.shell.send();

        logger.exiting(this.getClass().getName(), "write");

    }

    /**
     * @see org.electrocodeogram.module.Module#propertyChanged(org.electrocodeogram.modulepackage.ModuleProperty)
     */
    @Override
    public final void propertyChanged(final ModuleProperty moduleProperty) {

        logger.entering(this.getClass().getName(), "write",
            new Object[] {moduleProperty});

        if (moduleProperty.getName().equals("HackyStat Host")) {
            this.host = moduleProperty.getValue();
        } else if (moduleProperty.getName().equals("HackyStat Key")) {
            this.key = moduleProperty.getValue();
        }

        logger.exiting(this.getClass().getName(), "propertyChanged");
    }

    /**
     * @see org.electrocodeogram.module.Module#update()
     *      This The method is not implemented in this module.
     */
    @Override
    public void update() {
    // not implemented
    }

    /**
     * @see org.electrocodeogram.module.Module#initialize()
     *  This The method is not implemented in this module.
     */
    @Override
    public void initialize() {
    // not implemented
    }

    /**
     * @see org.electrocodeogram.module.target.TargetModule#startWriter()
     *      This method is not implemented in this module.
     */
    @Override
    public final void startWriter() throws TargetModuleException {

        logger.entering(this.getClass().getName(), "startWriter");

        if (this.host == null) {

            logger.exiting(this.getClass().getName(), "startWriter");

            throw new TargetModuleException(
                "The HackyStat host property is not set yet.", this.getName());
        }

        if (this.key == null) {

            logger.exiting(this.getClass().getName(), "startWriter");

            throw new TargetModuleException(
                "The HackyStat admin key property is not set yet.", this
                    .getName());
        }

        this.properties = new SensorProperties(this.host, this.key);

        logger.log(Level.FINE, "HackyStat SensorProperties created.");

        this.shell = new SensorShell(this.properties, false,
            "ElectroCodeoGram", false);

        logger.log(Level.FINE, "HackyStat SensorShell created.");

        logger.exiting(this.getClass().getName(), "startWriter");

    }

    /**
     * @see org.electrocodeogram.module.target.TargetModule#stopWriter() This
     *      method is not implemented in this module.
     */
    @Override
    public void stopWriter() {
    // not implemented
    }
}

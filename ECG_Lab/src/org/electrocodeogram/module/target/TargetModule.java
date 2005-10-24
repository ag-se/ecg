/*
 * Class: TargetModule
 * Version: 1.0
 * Date: 18.10.2005
 * By: Frank@Schlesinger.com
 */

package org.electrocodeogram.module.target;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.electrocodeogram.event.ValidEventPacket;
import org.electrocodeogram.logging.LogHelper;
import org.electrocodeogram.logging.LogHelper.ECGLevel;
import org.electrocodeogram.module.Module;

/**
 * This abstract class must be subclassed by all
 * <em>TargetModules</em> that are intended to write out the event
 * they receive.
 */
public abstract class TargetModule extends Module implements ITargetModule {

    /**
     * This is the logger.
     */
    private static Logger logger = LogHelper.createLogger(TargetModule.class
        .getName());

    /**
     * This creates the module.
     * @param id
     *            Is the id of this <em>ModulePackage</em>
     * @param name
     *            Is the name to be assigned to this module
     */
    public TargetModule(final String id, final String name) {
        super(ModuleType.TARGET_MODULE, id, name);

        logger.entering(this.getClass().getName(), "TargetModule",
            new Object[] {id, name});

        initialize();

        logger.exiting(this.getClass().getName(), "TargetModule");
    }

    /**
     * @see org.electrocodeogram.module.Module#receiveEventPacket(org.electrocodeogram.event.ValidEventPacket)
     *      In addition to its superclass method this method writes
     *      out every event it receives, by calling the module's write
     *      method.
     */
    @Override
    public final void receiveEventPacket(final ValidEventPacket eventPacket) {
        logger.entering(this.getClass().getName(), "receiveEventPacket",
            new Object[] {eventPacket});

        if (eventPacket != null) {
            logger.log(Level.INFO,
                "An event has been received by the TargetModule: "
                                + this.getName());

            logger.log(ECGLevel.PACKET, eventPacket.toString());

            write(eventPacket);

            logger.log(Level.INFO,
                "The event has been writen by the TargetModule: "
                                + this.getName());
        } else {
            logger.log(Level.WARNING, "Parameter \"eventPacket\" is null.");

        }

        logger.exiting(this.getClass().getName(), "receiveEventPacket");
    }

    /**
     * This method is to be implemented to do the actual writing of
     * incoming events.
     * @param eventPacket
     *            Is the incoming event that is to be written out
     */
    public abstract void write(ValidEventPacket eventPacket);

    /**
     * @see org.electrocodeogram.module.Module#initialize()
     */
    @Override
    public abstract void initialize();

    /**
     * @see org.electrocodeogram.module.target.ITargetModule#startWriter()
     */
    public abstract void startWriter() throws TargetModuleException;

    /**
     * @see org.electrocodeogram.module.target.ITargetModule#stopWriter()
     */
    public abstract void stopWriter();

}

/*
 * Class: ManualAnnotatorSourceModule
 * Version: 1.0
 * Date: 16.10.2005
 * By: Frank@Schlesinger.com
 */

package org.electrocodeogram.module.source.implementation;

import java.util.logging.Logger;

import org.electrocodeogram.logging.LogHelper;
import org.electrocodeogram.modulepackage.ModuleProperty;
import org.electrocodeogram.modulepackage.ModulePropertyException;
import org.electrocodeogram.module.source.EventReader;
import org.electrocodeogram.module.source.SourceModule;

/**
 * This module is used to manually add events into the event stream.
 * For sure there are many kind of relevant events that are hard to
 * be recorded programmtically from a sensor.<br>
 * For example: It might be very interessting to notice, when
 * the programmer is interrupted from his work, but this of course
 * is hard to figure aut from inside a programm.
 * <br>
 * Those events can be defined in this module's <em>ModuleDescription</em>.
 * For every event that is specified there, the user will get a button
 * in a dialog window in the ECG Lab's GUI.
 * <br>
 * So at every time the user observes an interesting event he or she
 * simply pushes the button and the module is sending out the event.  
 */
public class ManualAnnotatorSourceModule extends SourceModule {

    /**
     * A reference to the <em>EventReader</em>.
     */
    private ManualReader eventReader;

    /**
     * This is the logger.
     */
    private static Logger logger = LogHelper
        .createLogger(ManualAnnotatorSourceModule.class.getName());

    /**
     * The creates the module instance. It is not to be
     * called by developers, instead it is called from the {@link ModuleRegistry}
     * when the user requested a new instance of this
     * module.
     * @param id
     *            This is the unique <code>String</code> id of the module
     * @param name
     *            This is the name which is assigned to the module
     *            instance
     */
    public ManualAnnotatorSourceModule(String id, String name) {
        super(id, name);

        logger.entering(this.getClass().getName(),
            "ManualAnnotatorSourceModule", new Object[] {id, name});

        logger
            .exiting(this.getClass().getName(), "ManualAnnotatorSourceModule");

    }

    /**
     * @see org.electrocodeogram.module.Module#propertyChanged(org.electrocodeogram.module.ModuleProperty)
     */
    public void propertyChanged(ModuleProperty moduleProperty)
        throws ModulePropertyException {

        logger.entering(this.getClass().getName(), "propertyChanged",
            new Object[] {moduleProperty});

        if (eventReader != null) {
            if (moduleProperty.getName().equals("Events")) {
                this.eventReader.setEvents(moduleProperty.getValue());
            }
            if (moduleProperty.getName().equals("Episodes")) {
                this.eventReader.setEpisodes(moduleProperty.getValue());
            }
        }
        
        logger.exiting(this.getClass().getName(), "propertyChanged");
    }

    /**
     * @see org.electrocodeogram.module.Module#update()
     *      This method is not implemented in this module, as
     *      this module does not need to be informed about
     *      ECG Lab subsystem's state changes.
     */
    public void update() {
        // not implemented
    }

    /**
     * @see org.electrocodeogram.module.Module#initialize() This
     *      method is not implemented in this module.
     */
    public void initialize() {
    // not implemented
    }

    /**
     * @see org.electrocodeogram.module.source.SourceModule#getEventReader()
     */
    public EventReader[] getEventReader() {

        logger.entering(this.getClass().getName(), "getEventReader");

        if (this.eventReader == null) {
            this.eventReader = new ManualReader(this);
        }

        logger.exiting(this.getClass().getName(), "getEventReader",
            new EventReader[] {this.eventReader});

        return new EventReader[] {this.eventReader};
    }

    /**
     * @see org.electrocodeogram.module.source.SourceModule#preStart()
     * This
     *      method is not implemented in this module.
     */
    public void preStart() {
    // not implemented
    }

    /**
     * @see org.electrocodeogram.module.source.SourceModule#postStop()
     */
    public void postStop() {

        logger.entering(this.getClass().getName(), "postStop");

        this.eventReader.hideDialog();

        this.eventReader = null;

        logger.exiting(this.getClass().getName(), "postStop");

    }
}

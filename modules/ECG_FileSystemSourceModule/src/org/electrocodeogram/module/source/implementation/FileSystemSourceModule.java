/*
 * FU Berlin, 2006
 */

package org.electrocodeogram.module.source.implementation;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.electrocodeogram.logging.LogHelper;
import org.electrocodeogram.module.ModuleActivationException;
import org.electrocodeogram.modulepackage.ModuleProperty;
import org.electrocodeogram.modulepackage.ModulePropertyException;
import org.electrocodeogram.module.source.EventReader;
import org.electrocodeogram.module.source.SourceModule;
import org.electrocodeogram.module.source.SourceModuleException;

/**
 * This class is an ECG module used to read events from a file into
 * the ECG Lab.
 */
public class FileSystemSourceModule extends SourceModule {

    /**
     * This is the logger.
     */
    private static Logger logger = LogHelper
        .createLogger(FileSystemSourceModule.class.getName());

    /**
     * This module can operate in two different modes. It can either
     * read in events as fast as possible in "BURST" mode, or it can
     * read in the stored events in the same time intervalls in which
     * they where stored. This "REALTIME" mode is very usefull for
     * playing back the recorded events along with a video record for
     * example.
     */
    public enum ReadMode {
        /**
         * In this mode the module will read in the stored events as
         * fast as possible.
         */
        BURST,
        /**
         * In this mode the module will read in the events in the time
         * intervalls in which they were stored.
         */
        REALTIME
    }

    /**
     * This is a reference to this module's <em>EventReader</em>,
     * which is implementing the read method.
     */
    private FileReaderThread readerThread;

    /**
     * A reference to the input file.
     */
    private File inputFile;

    /**
     * This <em>EventReader</em> can either read events as fast as
     * possible, or it can read in the events in the same time as they
     * were written into the file.
     */
    private ReadMode readMode;

    /**
     * If true, a special msdt.system event to denote the end of a 
     * file is sent. Other modules may use this event type to
     * invoke finalization code. The lab in nogui mode will
     * terminate after processing this special event 
     */
    private boolean sendEndEvent = false;

    
    /**
     * A string containing full msdt names which should be ignored by the file reader
     * See module's properties description on "Ignored Event Types"
     */
    private String ignoredMsdts;

    /**
     * The creates the module instance. It is not to be called by
     * developers, instead it is called from the
     * <code>ModuleRegistry</code> when the user requested a new instance
     * of this module.
     * @param id
     *            This is the unique string id of the
     *            module
     * @param name
     *            This is the name which is assigned to the module
     *            instance
     */
    public FileSystemSourceModule(final String id, final String name) {
        super(id, name);

        logger.entering(this.getClass().getName(), "FileSystemSourceModule",
            new Object[] {id, name});

        logger.exiting(this.getClass().getName(), "FileSystemSourceModule");

    }

    /**
     * @see org.electrocodeogram.module.Module#propertyChanged(org.electrocodeogram.modulepackage.ModuleProperty)
     */
    @Override
    public final void propertyChanged(final ModuleProperty moduleProperty)
        throws ModulePropertyException {

        logger.entering(this.getClass().getName(), "propertyChanged",
            new Object[] {moduleProperty});

        if (moduleProperty.getName().equals("Input File")) {

            File propertyValueFile = new File(moduleProperty.getValue());
            
            this.inputFile = propertyValueFile;
            
            if (isActive()) {
                deactivate();

                try {
                    activate();
                } catch (ModuleActivationException e) {

                    throw new ModulePropertyException(e.getMessage(), this
                        .getName(), this.getId(), moduleProperty.getName(),
                        moduleProperty.getValue());
                }
            }
        
        } else if (moduleProperty.getName().equals("Enable Realtime Mode")) {
            if (moduleProperty.getValue().equalsIgnoreCase("true")) {
                this.readMode = ReadMode.REALTIME;

                setMode();
            }
        
        } else if (moduleProperty.getName().equals("Enable Burst Mode")) {
            if (moduleProperty.getValue().equalsIgnoreCase("true")) {
                this.readMode = ReadMode.BURST;

                setMode();
            }
        
        } else if (moduleProperty.getName().equals("Send End Event")) {
            if (moduleProperty.getValue().equalsIgnoreCase("true")) {
                this.sendEndEvent = true;

                setMode();
            }
        } else if (moduleProperty.getName().equals("Ignored Event Types")) {
            
            this.ignoredMsdts = moduleProperty.getValue();
            
        } else {

            logger.exiting(this.getClass().getName(), "propertyChanged");

            throw new ModulePropertyException(
                "The module does not support this property.", this.getName(),
                this.getId(), moduleProperty.getName(), moduleProperty
                    .getValue());
        }

        logger.exiting(this.getClass().getName(), "propertyChanged");
    }

    /**
     * This method sets the <em>ReadMode</em> and end event status for the
     * <em>FileReaderThread</em>.
     */
    private void setMode() {

        logger.entering(this.getClass().getName(), "setMode");

        if (this.readerThread != null) {
            this.readerThread.setMode(this.readMode);
            this.readerThread.setSendEndEvent(this.sendEndEvent);
        }

        logger.exiting(this.getClass().getName(), "setMode");

    }

    /**
     * @see org.electrocodeogram.module.Module#update() This method is
     *      not implemented in this module, as this module does not
     *      need to be informed about ECG Lab subsystem's state
     *      changes.
     */
    @Override
    public void update() {
    // not implemented
    }

    /**
     * @see org.electrocodeogram.module.Module#initialize() This
     *      method is not implemented in this module.
     */
    @Override
    public void initialize() {
    // not implemented
    }

    /**
     * @see org.electrocodeogram.module.source.SourceModule#getEventReader()
     */
    @Override
    public final EventReader[] getEventReader() {

        logger.entering(this.getClass().getName(), "getEventReader");

        logger.exiting(this.getClass().getName(), "getEventReader",
            this.readerThread);

        return new EventReader[] {this.readerThread};
    }

    /**
     * @throws SourceModuleException
     * @see org.electrocodeogram.module.source.SourceModule#preStart()
     */
    @Override
    public final void preStart() throws SourceModuleException {

        logger.entering(this.getClass().getName(), "preStart");

        if (this.inputFile == null) {

            logger.log(Level.WARNING, "There is no input file selected.");

            logger.exiting(this.getClass().getName(), "preStart");

            throw new SourceModuleException("There is no input file selected.",
                this.getName());

        }

        this.readerThread = new FileReaderThread(this, this.readMode, this.sendEndEvent);

        logger.log(Level.FINE, "FileReader created.");

        try {
            this.readerThread.setInputFile(this.inputFile);
            this.readerThread.setIgnorePattern(this.ignoredMsdts);

            logger.log(Level.FINE, "input file set.");
        } catch (IOException e) {

            logger.log(Level.WARNING,
                "The FileReader was unable to open the input file: " + this.inputFile.getAbsolutePath());

            throw new SourceModuleException(
            		"The FileReader was unable to open the input file: " + this.inputFile.getAbsolutePath(), this
                    .getName());
        }

        logger.exiting(this.getClass().getName(), "preStart");
    }

    /**
     * @see org.electrocodeogram.module.source.SourceModule#postStop()
     *      This method is not implemented in this module.
     */
    public final void postStop() {
    // not implemented
    }

    /**
     * @return true, if this module will send a termination event after 
     * reading the last event from the file
     */
    public boolean isSendEndEvent() {
        return sendEndEvent;
    }
}

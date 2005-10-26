/*
 * Class: FileSystemSourceModule
 * Version: 1.0
 * Date: 16.10.2005
 * By: Frank@Schlesinger.com
 */

package org.electrocodeogram.module.source;

import java.io.File;
import java.util.logging.Logger;

import org.electrocodeogram.logging.LogHelper;
import org.electrocodeogram.module.ModuleProperty;
import org.electrocodeogram.module.ModulePropertyException;

/**
 * This class is an ECG module used to read events from a file into the ECG Lab.
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
     * This <em>EventReader</em> can either read events as fast as possible,
     * or it can read in the events in the same time as they were written into the file.
     */
    private ReadMode readMode;

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
    public FileSystemSourceModule(final String id, final String name) {
        super(id, name);

        logger.entering(this.getClass().getName(), "FileSystemSourceModule",
            new Object[] {id, name});

        logger.exiting(this.getClass().getName(), "FileSystemSourceModule");

    }

    /**
     * @see org.electrocodeogram.module.Module#propertyChanged(org.electrocodeogram.module.ModuleProperty)
     */
    @Override
    public final void propertyChanged(final ModuleProperty moduleProperty)
        throws ModulePropertyException {

        logger.entering(this.getClass().getName(), "FileSystemSourceModule",
            new Object[] {moduleProperty});

        if (moduleProperty.getName().equals("Input File")) {

            File propertyValueFile = new File(moduleProperty.getValue());

            this.inputFile = propertyValueFile;

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
        } else {

            logger.exiting(this.getClass().getName(), "FileSystemSourceModule");

            throw new ModulePropertyException(
                "The module does not support this property.", this.getName(),
                this.getId(), moduleProperty.getName(), moduleProperty
                    .getValue());
        }

        logger.exiting(this.getClass().getName(), "FileSystemSourceModule");
    }

    /**
     * This method sets the <em>ReadMode</em> for the <em>FileReaderThread</em>.
     */
    private void setMode() {

        logger.entering(this.getClass().getName(), "setMode");

        if (this.readerThread != null) {
            this.readerThread.setMode(this.readMode);
        }

        logger.exiting(this.getClass().getName(), "setMode");

    }

    /**
     * @see org.electrocodeogram.module.Module#update()
     *      This method is not implemented in this module, as
     *      this module does not need to be informed about
     *      ECG Lab subsystem's state changes.
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
     * @see org.electrocodeogram.module.source.SourceModule#preStart()
     */
    @Override
    public final void preStart() {

        logger.entering(this.getClass().getName(), "preStart");

        if (this.inputFile == null) {

            logger.exiting(this.getClass().getName(), "preStart");

            return;
        }

        this.readerThread = new FileReaderThread(this, this.inputFile,
            this.readMode);

        logger.exiting(this.getClass().getName(), "preStart");
    }

    /**
     * @see org.electrocodeogram.module.source.SourceModule#postStop()
     * This method is not implemented in this module.
     */
    @Override
    public final void postStop() {
    // not implemented
    }
}

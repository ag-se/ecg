package org.electrocodeogram.module.source;

import java.io.File;

import org.electrocodeogram.module.ModuleProperty;
import org.electrocodeogram.module.ModulePropertyException;

/**
 * This class is an ECG module used to read back events from a file.
 */
public class FileSystemSourceModule extends SourceModule {

    /**
     * The module can operate in two different modes. It can either
     * read in events as fast as possible in "BURST" mode, or it can
     * read in the stored events in the same time intervalls in which
     * they where stored. This "REALTIME" mode is very neccessary for
     * playing back the recorded events along with a video record for
     * example. In that case one can use the
     * ManualAnnotatorSourceModule to manually annotate the evtn
     * stream with additional events. The "BURST" mode is usefull for
     * simply (re)-analysis of the event stream.
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

    private FileReaderThread _readerThread;

    private File _inputFile;

    private ReadMode _readMode;

    /**
     * The constructor creates the module instance. It is not to be
     * called by developers, instead it is called from the ECG
     * ModuleRegistry when the user requested a new instance of this
     * module.
     * @param id
     *            This is the unique String id of the module
     * @param name
     *            This is the name which is given to the module
     *            instance
     */
    public FileSystemSourceModule(String id, String name) {
        super(id, name);

    }

    /**
     * @see org.electrocodeogram.module.Module#propertyChanged(org.electrocodeogram.module.ModuleProperty)
     */
    @Override
    public void propertyChanged(ModuleProperty moduleProperty)
        throws ModulePropertyException {

        if (moduleProperty.getName().equals("Input File")) {

            File propertyValueFile = new File(moduleProperty.getValue());

            this._inputFile = propertyValueFile;

        } else if (moduleProperty.getName().equals("Enable Realtime Mode")) {
            if (moduleProperty.getValue().equalsIgnoreCase("true")) {
                this._readMode = ReadMode.REALTIME;

                setMode();
            }
        } else if (moduleProperty.getName().equals("Enable Burst Mode")) {
            if (moduleProperty.getValue().equalsIgnoreCase("true")) {
                this._readMode = ReadMode.BURST;

                setMode();
            }
        } else {
            throw new ModulePropertyException(
                "The module does not support this property.", this.getName(), this.getId(), moduleProperty.getName(), moduleProperty.getValue());
        }
    }

    private void setMode() {
        if (this._readerThread != null) {
            this._readerThread.setMode(this._readMode);
        }

    }

    /**
     * @see org.electrocodeogram.module.Module#analyseCoreNotification()
     *      This method is not implemented in this module.
     */
    @Override
    public void analyseCoreNotification() {
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

    /*
     * (non-Javadoc)
     * @see org.electrocodeogram.module.source.SourceModule#getEventReader()
     */
    @Override
    public EventReader[] getEventReader() {
       
        return new EventReader[] {this._readerThread};
    }

    /*
     * (non-Javadoc)
     * @see org.electrocodeogram.module.source.SourceModule#preStart()
     */
    @Override
    public void preStart() {
        if (this._inputFile == null) {
            return;
        }

        this._readerThread = new FileReaderThread(this,
            this._inputFile, this._readMode);

    }

    /*
     * (non-Javadoc)
     * @see org.electrocodeogram.module.source.SourceModule#postStop()
     */
    @Override
    public void postStop() {
    

    }

    // /**
    // * @see
    // org.electrocodeogram.module.source.SourceModule#startReader(org.electrocodeogram.module.source.SourceModule)
    // */
    // @Override
    // public void startReader(SourceModule sourceModule)
    // throws SourceModuleException {
    // if (this._inputFile == null) {
    // throw new SourceModuleException("No input file selected");
    // }
    //
    // this._readerThread = new FileReaderThread(sourceModule,
    // this._inputFile, this._readMode);
    //
    // this._readerThread.start();
    // }
    //
    // /**
    // * @see
    // org.electrocodeogram.module.source.SourceModule#stopReader()
    // */
    // @Override
    // public void stopReader() {
    //        this._readerThread.stopReader();
    //
    //    }
}

package org.electrocodeogram.module.source;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.electrocodeogram.event.WellFormedEventPacket;
import org.electrocodeogram.logging.LogHelper;
import org.electrocodeogram.module.ModuleProperty;
import org.electrocodeogram.module.ModulePropertyException;
import org.electrocodeogram.system.ModuleSystem;

/**
 * This class is an ECG nodule that reads in ECG events form standard
 * input. It is primarilly used by the ECG EclipseSenor if it runs in
 * InlineServer mode. In that case the ECG Lab is started with this
 * module and the ECG EclipseSensor writes recorded events to the
 * standard input of this module.
 */
public class IOSourceModule extends SourceModule {

    static Logger _logger = LogHelper.createLogger(IOSourceModule.class
        .getName());

    private Console _console;

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
    public IOSourceModule(String id, String name) {
        super(id, name);

        _logger.entering(this.getClass().getName(), "IOSOurceModule");

        _logger.exiting(this.getClass().getName(), "IOSOurceModule");
    }

    /**
     * @see org.electrocodeogram.module.Module#propertyChanged(org.electrocodeogram.module.ModuleProperty)
     */
    @SuppressWarnings("unused")
    @Override
    public void propertyChanged(@SuppressWarnings("unused")
    ModuleProperty moduleProperty) throws ModulePropertyException {
    // not implemented
    }

    /**
     * @see org.electrocodeogram.module.Module#analyseCoreNotification()
     *      This The method is not implemented in this module.
     */
    @Override
    public void analyseCoreNotification() {
    // not implemented
    }

    /**
     * @see org.electrocodeogram.module.Module#initialize() The method
     *      is not implemented in this module.
     */
    @Override
    public void initialize() {
    // not implemented
    }

    // /**
    // * @see
    // org.electrocodeogram.module.source.SourceModule#startReader(org.electrocodeogram.module.source.SourceModule)
    // */
    // @Override
    // public void startReader(SourceModule sourceModule) throws
    // SourceModuleException
    // {
    // _logger.entering(this.getClass().getName(), "startReader");
    //
    // this._console = new Console(sourceModule);
    //		
    // System.out.println("console created");
    //
    // this._console.start();
    //		
    // System.out.println("console started");
    //
    // _logger.exiting(this.getClass().getName(), "startReader");
    // }

    // /**
    // * @see
    // org.electrocodeogram.module.source.SourceModule#stopReader()
    // */
    // @Override
    // public void stopReader()
    // {
    // this._console.shutDown();
    //
    // this._console = null;
    // }

    private class Console extends EventReader {

        private ObjectInputStream _ois = null;

        private SourceModule _sourceModule;

        private boolean _run = true;

        /**
         * This creates the Console Thread to continously read in
         * events from standard input.
         * @param sourceModule
         *            Is the SourceModule to which events are beeing
         *            passed
         * @throws IOException
         */
        public Console(SourceModule sourceModule) {

            super(sourceModule);

            _logger.entering(this.getClass().getName(), "Console");

            this._sourceModule = sourceModule;

            try {
                this._ois = new ObjectInputStream(System.in);
            } catch (IOException e1) {
                _logger.log(Level.SEVERE,
                    "An error occured while starting the SourceModule: "
                                    + this.getName());

                _logger.exiting(this.getClass().getName(), "startReader");

                this._sourceModule.deactivate();

                return;
            }
            
            _logger.exiting(this.getClass().getName(), "Console");
        }

        /**
         * This stops the Console Thread.
         */
        public void shutDown() {
            this._run = false;
        }

//        /**
//         * @see java.lang.Thread#run()
//         */
//        @Override
//        public void run() {
//            _logger.entering(this.getClass().getName(), "run");
//
//            try {
//                this._ois = new ObjectInputStream(System.in);
//            } catch (IOException e1) {
//                _logger.log(Level.SEVERE,
//                    "An error occured while starting the SourceModule: "
//                                    + this.getName());
//
//                _logger.exiting(this.getClass().getName(), "startReader");
//
//                this._sourceModule.deactivate();
//
//                return;
//            }
//
//            while (this._run) {
//
//                System.out.println("run");
//
//                System.out.println(this._sourceModule.getName() + " >>");
//
//                Object inputObject = null;
//
//                try {
//                    System.out.println("before read");
//
//                    inputObject = this._ois.readObject();
//
//                    System.out.println("after read");
//
//                    _logger.log(Level.INFO, "Read a line...");
//                } catch (Exception e) {
//
//                    _logger.log(Level.SEVERE,
//                        "An error occurred while receiving data.");
//
//                    _logger.log(Level.SEVERE, e.getMessage());
//
//                    this._sourceModule.deactivate();
//
//                    return;
//                }
//
//                if (inputObject instanceof WellFormedEventPacket) {
//
//                    _logger.log(Level.INFO, "Event received");
//
//                    WellFormedEventPacket packet = (WellFormedEventPacket) inputObject;
//
//                    this._sourceModule.append(packet);
//
//                } else if (inputObject instanceof String) {
//
//                    _logger.log(Level.INFO, "String received");
//
//                    String string = (String) inputObject;
//
//                    _logger.log(Level.INFO, string);
//
//                    if (string.equals("quit")) {
//                        ModuleSystem.getInstance().quit();
//                    }
//                }
//
//            }
//
//            _logger.exiting(this.getClass().getName(), "run");
//        }

        /*
         * (non-Javadoc)
         * @see org.electrocodeogram.module.source.EventReader#read()
         */
        @Override
        public WellFormedEventPacket read() throws EventReaderException {

            Object inputObject = null;
            
            try {
                inputObject = this._ois.readObject();
                
                if (inputObject instanceof WellFormedEventPacket) {

                    _logger.log(Level.INFO, "Event received");

                    WellFormedEventPacket packet = (WellFormedEventPacket) inputObject;

                    return packet;

                } else if (inputObject instanceof String) {

                    _logger.log(Level.INFO, "String received");

                    String string = (String) inputObject;

                    _logger.log(Level.INFO, string);

                    if (string.equals("quit")) {
                        ModuleSystem.getInstance().quit();
                    }
                    
                    return null;
                }
                
                
            } catch (Exception e) {
             throw new EventReaderException("");
            }
            
            return null;
        }

    }

    /*
     * (non-Javadoc)
     * @see org.electrocodeogram.module.source.SourceModule#getEventReader()
     */
    @Override
    public EventReader[] getEventReader() {

        return new EventReader[] {this._console};
    }

    /* (non-Javadoc)
     * @see org.electrocodeogram.module.source.SourceModule#preStart()
     */
    @Override
    public void preStart() {
        _logger.entering(this.getClass().getName(), "preStart");

        this._console = new Console(this);

        this._console.start();

        _logger.exiting(this.getClass().getName(), "preStart");

    }

    /* (non-Javadoc)
     * @see org.electrocodeogram.module.source.SourceModule#postStop()
     */
    @Override
    public void postStop() {

    }
}

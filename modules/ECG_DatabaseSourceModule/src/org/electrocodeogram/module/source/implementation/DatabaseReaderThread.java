package org.electrocodeogram.module.source.implementation;

import java.util.logging.Logger;
import org.electrocodeogram.event.WellFormedEventPacket;
import org.electrocodeogram.logging.LogHelper;
import org.electrocodeogram.module.source.EventReader;
import org.electrocodeogram.module.source.EventReaderException;
import org.electrocodeogram.module.source.SourceModule;
import org.electrocodeogram.module.target.implementation.DBCommunicator;

/**
 * This {@link org.electrocodeogram.module.source.EventReader} is used by the
 * {@link DatabaseSourceModule}. It reads events from the Database.
 */
public class DatabaseReaderThread extends EventReader {
    /**
     * This is the logger.
     */
    private static Logger logger = LogHelper
            .createLogger(DatabaseReaderThread.class.getName());

    /**
     * This is the <em>SourceModule</em> to which read events are passed. It
     * is the {@link DatabaseSourceModule} in this case.
     */
    private SourceModule module;

    /**
     * The state of this <code>Thread</code>.
     */
    private boolean run;

    /**
     * the DBCommunicator to fetch the Events from the Database
     */
    private DBCommunicator dbCommunicator;

    private String eventID;

    /**
     * the Array which holds the linkid values of all events which have to be
     * created by the EventReader
     */
    private Object[] IDs;

    /**
     * the integer j counts the position in the IDs Array. So the EventReader
     * knows which Events from the Array have already been recreated.
     */
    private int j = 0;

    /**
     * This creates the <em>DatabaseReaderThread</em>.
     * 
     * @param sourceModule
     *            Is the SourceModule to which the events shall be passed
     * @param dbCom
     *            the DBCommunicator to communicate with the Database
     */
    public DatabaseReaderThread(final SourceModule sourceModule,
            DBCommunicator dbCom) {
        super(sourceModule);
        this.dbCommunicator = dbCom;
        this.module = sourceModule;
        this.run = true;
    }

    /**
     * This Methode allows to set the linkid values of the Events for which to
     * create the ValidEventPackets. After setting the linkid values the
     * EventReader is automatically started
     * 
     * @param ids
     *            the linkid values of the Events to create
     */
    public final void setEventIDs(Object[] ids) {
        this.IDs = ids;
        this.startReader();
    }

    /**
     * @see org.electrocodeogram.module.source.EventReader#read()
     */
    public final WellFormedEventPacket read() throws EventReaderException {
        WellFormedEventPacket eventPacket = null;
        // if there are some events to create
        if (IDs != null) {
            while (j < IDs.length) {
                logger.info("going to create Event " + eventID);
                // get the ID of each event
                eventID = IDs[j].toString();
                // create the EventPacket from the Event with the given ID
                eventPacket = CreateEventFromDB.createEvent(eventID,
                        dbCommunicator);
                logger.info("created Event with id " + eventID);
                j++;
                return eventPacket;
            }
        }
        j = 0;
        logger.info("going to stop Reader");
        this.stopReader();
        return eventPacket;
    }
}

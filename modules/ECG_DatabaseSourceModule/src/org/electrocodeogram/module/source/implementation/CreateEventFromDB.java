package org.electrocodeogram.module.source.implementation;

import java.sql.Timestamp;
import java.util.Date;
import java.util.logging.Logger;
import org.electrocodeogram.event.ValidEventPacket;
import org.electrocodeogram.logging.LogHelper;
import org.electrocodeogram.misc.xml.ECGWriter;
import org.electrocodeogram.module.target.implementation.DBCommunicator;
import org.electrocodeogram.module.target.implementation.DatabaseTargetModule;
import org.w3c.dom.Document;

/**
 * This class is responsible for creating a ValidEventPacket from the records of
 * an Event which is stored in the Database. Only the Primary Key of the Event
 * in the commondata table is needed.
 * 
 * @author jule
 * @version 1.0
 * 
 */
public class CreateEventFromDB {
    
    /**
     * This is the logger.
     */
    private static Logger logger = LogHelper
            .createLogger(CreateEventFromDB.class.getName());

    
    /**
     * This Method gets an Event from the database an creates a ValidEventPacket
     * from the event stored in the database.
     * 
     * @param eventData
     *            the event's data
     * @return true if a ValidEventPAcket could be created, otherwise false
     */
    public static boolean createEventPacket(Event event, DBCommunicator dbCom) {
        
        // the logger logs entering this method
        logger.entering(
                "org.electrocodeogram.module.source.implementation."
                        + "CreateEventFromDB", "createEventPacket()");
        
       
        /**
         * build up a tree model of the Schema
         */
        SchemaTree tree = new SchemaTree(event, dbCom);
        /**
         * get the comomn data attributes which are stored separately in the
         * event
         */
        String msdt = event.getMSDT();
        Timestamp timestamp = event.getTimestamp();
        Date date = new Date(timestamp.getTime());
        /**
         * build up the xml document which is contained in the event with the
         * data given by the Event object
         */
        Document eventXmlPart = tree.getSchemaElements();
        /**
         * create the ValidEventPAcket with the attributes and the xml Document
         * for the ValidEventPacket
         */
        ValidEventPacket packet = ECGWriter.createValidEventPacket(msdt, date,
                eventXmlPart);
        logger.info("created ValidEventPacket: "+packet.toString());
        logger.exiting(
                "org.electrocodeogram.module.source.implementation."
                        + "CreateEventFromDB", "createEventPacket()");
        return true;
    }

    /**
     * This method gets all the events data from the tables the records are
     * stored in. For this the getEventByID method from the DBQueries class is
     * used and it returns the event as an Event object which then is processed
     * further by the createEventPacket function.
     * 
     * @param eventID
     *            the primary key of the event in the commondata table
     * @param dbCom
     *            the DBCommunicator which communicaes with the database
     */
    public static void createEvent(String eventID, DBCommunicator dbCom) {
        Event event = DBQueries.getEventByID(eventID, dbCom);
        createEventPacket(event, dbCom);
    }
}

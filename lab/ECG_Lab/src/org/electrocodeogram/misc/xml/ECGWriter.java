package org.electrocodeogram.misc.xml;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.electrocodeogram.event.IllegalEventParameterException;
import org.electrocodeogram.event.ValidEventPacket;
import org.electrocodeogram.event.WellFormedEventPacket;
import org.electrocodeogram.logging.LogHelper;
import org.w3c.dom.Document;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

public class ECGWriter {

    /**
     * This is the logger.
     */
    private static Logger logger = LogHelper.createLogger(ECGParser.class.getName());

    /**
     * A serializer to output XML DOM structures 
     */
    private static LSSerializer xmlDocumentSerializer = null; 

    /**
     * TODO: change to general static format 
     */
    public static final DateFormat dateFormat = DateFormat.getDateTimeInstance(
            DateFormat.MEDIUM, DateFormat.MEDIUM);
        

    /**
     * Creates a valid event packet from eventtype ("msdt....xsd"), time stamp, and
     * XML Document.
     * 
     * @param eventtype
     * @param timestamp
     * @param eventdoc
     * @return
     */
    public static ValidEventPacket createValidEventPacket(String eventtype, Date timestamp, Document eventdoc) {

        if (xmlDocumentSerializer == null)
            initWriter();
        
        ValidEventPacket event = null;
        String docstring = xmlDocumentSerializer.writeToString(eventdoc);
        
        return createValidEventPacket(eventtype, timestamp, docstring);
    }

    /**
     * Creates a valid event packet from eventtype ("msdt....xsd"), time stamp, and
     * XML Document.
     * 
     * @param eventtype
     * @param timestamp
     * @param eventdoc
     * @return
     */
    public static ValidEventPacket createValidEventPacket(String eventtype, Date timestamp, String docstring) {

        ValidEventPacket event = null;
        String[] args = {WellFormedEventPacket.HACKYSTAT_ADD_COMMAND, eventtype, docstring};
        
        try {
            event = new ValidEventPacket(timestamp,
                WellFormedEventPacket.HACKYSTAT_ACTIVITY_STRING, Arrays.asList(args));

        } catch (IllegalEventParameterException e) {
            logger.log(Level.SEVERE, "Could not instantiate the DOM Implementation.");
            logger.log(Level.FINE, e.getMessage());
        }
        
        return event;
        
    }

    /**
     * Initialzation of DOM API
     */
    private static void initWriter() {
        try {
            // get DOM Implementation using DOM Registry
            // TODO Besser auf Xerxes setzen. Dies ist auﬂerdem wohl nur in JDK 5.0
            System.setProperty(DOMImplementationRegistry.PROPERTY, "com.sun.org.apache.xerces.internal.dom.DOMXSImplementationSourceImpl");
            DOMImplementationRegistry registry;
            registry = DOMImplementationRegistry.newInstance();
            // Retrieve load/save features
            DOMImplementationLS impl = 
                (DOMImplementationLS)registry.getDOMImplementation("LS");
            // create DOMWriter
            xmlDocumentSerializer = impl.createLSSerializer();   
            xmlDocumentSerializer.getDomConfig().setParameter("xml-declaration", Boolean.FALSE);
        } catch (Exception e) { // TODO Ok, that's bad...
            logger.log(Level.SEVERE, "Could not instantiate DOM Implementation in ECGWriter.");
            logger.log(Level.FINE, e.getMessage());
        }        
    }

    /**
     * @param end
     * @return
     */
    public static String formatDate(Date end) {
        return dateFormat.format(end);
    }

}

package org.electrocodeogram.module.target.implementation;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import org.electrocodeogram.event.ValidEventPacket;
import org.electrocodeogram.logging.LogHelper;
import org.electrocodeogram.misc.xml.ECGParser;
import org.electrocodeogram.misc.xml.NodeException;
import org.w3c.dom.Document;

/**
 * This class provides some kind of proxy for a ValidEventPacket. 
 * It offers better access to the attributes of an Event.
 * @author jule
 * @version 1.0
 * 
 */
public class ValidEventPacketProxy {
    /**
     * The Event which is given with the contructor and for which the Proxy has
     * to be created.
     */
    private ValidEventPacket event;

    /**
     * The constructor.
     * 
     * @param vPacket
     *            the validEventPacket
     */
    public ValidEventPacketProxy(final ValidEventPacket vPacket) {
        this.event = vPacket;
    }

    /**
     * labels the username tag in the XML Document
     */
    private static final String ATTR_USER = "username";

    /**
     * labels the projectname tag in the XML Dokument
     */
    private static final String ATTR_PROJECT = "projectname";

    /**
     * This is the logger.
     */
    private static Logger logger = LogHelper
            .createLogger(ValidEventPacketProxy.class.getName());

    /**
     * Get the projectname from the event. If the projectname cannot be found
     * null is returned
     * 
     * @return the Value for the Username Node
     */
    public String getProjectname() {
        return getElementValue(ATTR_PROJECT);
    }

    /**
     * returns the Timestamp of the event
     * 
     * @return the timestamp of the event
     */
    public Date getTimestamp() {
        return this.event.getTimeStamp();
    }

    /**
     * Get the username from the event If the Username can not be found null is
     * returned
     * 
     * @return the username of this event
     */
    public String getUsername() {
        return getElementValue(ATTR_USER);
    }

    /**
     * Get the XML Document from the Event
     * 
     * @return the XML Document included in the Event
     */
    public Document getXMLDoc() {
        return this.event.getDocument();
    }

    /**
     * @return the msdt of the event
     */
    public String getMsdt() {
        return this.event.getMicroSensorDataType().getName();
    }

    public File getMSDTDefFile() {
        return this.event.getMicroSensorDataType().getDefFile();
    }

    /**
     * this method returns core the Name of a MSDT Type, without "msdt" before
     * and "xsd" after the name;
     * 
     * @return the core name of the msdt
     */
    public String getMSDTName() {
        String temp = this.event.getMicroSensorDataType().getName();
        System.out.println(temp);
        StringTokenizer tokenizer = new StringTokenizer(temp, ".");
        while (tokenizer.hasMoreTokens()) {
            String current = tokenizer.nextToken();
            if (!((current.equalsIgnoreCase("msdt") || (current.equals("xsd"))))) {
                return current;
            }
        }
        return null;
    }

    /**
     * Returns the Value of the given Element Name from the XML Document If
     * there is no Node with the given elementName the Method returns null
     * 
     * @param elementName
     * @return the value in the xml document for the given element
     */
    public String getElementValue(String elementName) {
        String elementValue = null;
        try {
            elementValue = ECGParser.getSingleNodeValue(elementName, this.event
                    .getDocument());
        }
        catch (NodeException e) {
            logger.warning("There is no such Node '" + elementName
                    + "' in the event: \n" + this.event);
            return null;
        }
        return elementValue;
    }

    /**
     * the ArgList which is included in the event
     * 
     * @return the event's ArgList
     */
    public List getArgList() {
        return event.getArgList();
    }
}

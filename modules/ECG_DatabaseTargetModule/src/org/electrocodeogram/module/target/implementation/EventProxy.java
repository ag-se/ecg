package org.electrocodeogram.module.target.implementation;

import java.util.Date;
import java.util.List;

import javax.xml.validation.Schema;

import org.electrocodeogram.event.ValidEventPacket;
import org.electrocodeogram.misc.xml.ECGParser;
import org.electrocodeogram.misc.xml.NodeException;
import org.electrocodeogram.msdt.MicroSensorDataType;
import org.w3c.dom.Document;

/**
 * 
 * @author jule
 * @version 1.0
 */
public class EventProxy {

    /**
     * the xml document containing in the event
     */
    private Document xmlDoc = null;

    /**
     * the eventPacket for which the EventProxy is created
     */
    private ValidEventPacket eventPacket;

    
    private  static final String USERNAME = "username";

    private static final String PROJECTNAME = "projectname";

    /**
     * This is the Constructor.
     * @param event the event for which to create the document Proxy
     */
    public EventProxy(ValidEventPacket event) {
        this.eventPacket = event;
        this.xmlDoc = event.getDocument();
    }

    /**
     * returns the event's Timestamp which is part of the common data
     * @return the timestamp 
     */
    public Date getTimestamp() {
        return eventPacket.getTimeStamp();
    }

    /**
     * returns the events username which is part of the common data
     * @return the username
     */
    public String getUsername() {
        String tmpUsername = "";
        /*
         * get username from commonData in XML Document using the ECGParser
         */
        try {

            tmpUsername = ECGParser.getSingleNodeValue(USERNAME, xmlDoc);

        } catch (NodeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return tmpUsername;
    }

    /**
     * returns the events Projectname which is also part of the comon data
     * @return the Projectname
     */
    public String getProjectname() {
        String tmpProjectname = "";
        try {

            tmpProjectname = ECGParser.getSingleNodeValue(PROJECTNAME, xmlDoc);

        } catch (NodeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return tmpProjectname;
    }

    /**
     * This Method returns the String Value of a Node with the given Name
     * 
     * @param NodeName
     * @return the String value of the given node
     */
    public String getAnyNodevalueByName(String NodeName) {
        String tmp = "";
        try {

            tmp = ECGParser.getSingleNodeValue(NodeName, xmlDoc);

        } catch (NodeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return tmp;
    }

    /**
     * returns the event's ArgList which has three Elements
     * @return the ArgList of the Event
     */
    public List getArgList() {
        return this.eventPacket.getArgList();
    }

    /**
     * 
     * @return the the eschema corresponding to this event
     */
    public Schema getMSDTSchema() {
        return this.eventPacket.getMicroSensorDataType().getSchema();

    }

    /**
     * 
     * @return the msdt type of the event
     */
    public MicroSensorDataType getMSDT() {
        return this.eventPacket.getMicroSensorDataType();
    }

   
    /**
     * 
     * @return the name of the MicroSensorDataType
     */
    public String getEventType() {
        return this.eventPacket.getMicroSensorDataType().getName();
    }

}

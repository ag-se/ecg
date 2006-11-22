package org.electrocodeogram.module.source.implementation;

import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.Vector;

/**
 * The Event Class represents an Event which is fetched from the database and
 * then can be used for further processing. It holds the event's attributes and
 * provides methods for simple access to the events data. The Attributes are
 * hold as a vector of EventDataEntry Objects.
 * 
 * @author jule
 * @version 1.0
 */
public class Event {
    /**
     * The Vector containing the event's attributes
     */
    private Vector<EventDataEntry> eventFields = new Vector<EventDataEntry>();

    /**
     * the empty contructor
     */
    public Event() {
    }

    /**
     * Here you can add an attribute to the event
     * 
     * @param entry
     *            the EventDataEntry Object to add
     */
    public void addEntry(EventDataEntry entry) {
        eventFields.add(entry);
    }

    /**
     * Get the Timestamp of the Event
     * 
     * @return the event's timestamp
     */
    public Timestamp getTimestamp() {
        for (int i = 0; i < eventFields.size(); i++) {
            EventDataEntry currentEntry = eventFields.get(i);
            if (currentEntry.getColumnName().equalsIgnoreCase("timestamp")) {
                return (Timestamp) currentEntry.getValues().getFirst();
            }
        }
        return null;
    }

    /**
     * Get the msdt of the Event
     * 
     * @return the event's msdt as a string
     */
    public String getMSDT() {
        for (int i = 0; i < eventFields.size(); i++) {
            EventDataEntry currentEntry = eventFields.get(i);
            if (currentEntry.getColumnName().equalsIgnoreCase("msdt")) {
                return currentEntry.getValues().getFirst().toString();
            }
        }
        return null;
    }

    /**
     * Get the value of the event's Primary Key.
     * 
     * @return the primary key value
     */
    public int getPrimaryKey() {
        for (int i = 0; i < eventFields.size(); i++) {
            EventDataEntry currentEntry = eventFields.get(i);
            if (currentEntry.getColumnName().equalsIgnoreCase("linkid")) {
                Integer it = (Integer) currentEntry.getValues().getFirst();
                return it.intValue();
            }
        }
        return -1;
    }

    /**
     * This Metho allows to check if the Event contains an element with the
     * given name
     * 
     * @param elementName
     *            the searched name
     * @return true if the event contains the element, false otherwise
     */
    public boolean containsElement(String elementName) {
        for (int i = 0; i < eventFields.size(); i++) {
            EventDataEntry currentEntry = eventFields.get(i);
            if (currentEntry.getColumnName().equalsIgnoreCase(elementName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get an Attribute by its name
     * 
     * @param name
     *            the name of the attribute
     * @return the attribute as an eventDataEntry Object
     */
    public EventDataEntry getEntryByName(String name) {
        for (int i = 0; i < eventFields.size(); i++) {
            EventDataEntry currentEntry = eventFields.get(i);
            if (currentEntry.getColumnName().equalsIgnoreCase(name)) {
                return currentEntry;
            }
        }
        return null;
    }

    /**
     * Get all values for a given attribute. This is necessary because an
     * element in a XML document can occur more than once, and then for one
     * event attribute, which is an element in the xml document and a column in
     * a database table, there can be more than one value.
     * 
     * @param entryName
     *            the name of the attribute
     * @return all values for this attribute
     */
    public LinkedList getEntryValues(String entryName) {
        for (int i = 0; i < eventFields.size(); i++) {
            EventDataEntry currentEntry = eventFields.get(i);
            if (currentEntry.getColumnName().equalsIgnoreCase(entryName)) {
                return currentEntry.getValues();
            }
        }
        return null;
    }

    
    public String getAttributeValue(String attributeName) {
        for (int i = 0; i < eventFields.size(); i++) {
            EventDataEntry currentEntry = eventFields.get(i);
            if (currentEntry.getElementValue()
                    .equalsIgnoreCase(attributeName)) {
                return currentEntry.getElementValue();
            }
        }
        return null;
    }

    public String eventDataToString() {
        String eventString = "****************** \n";
        for (int i = 0; i < eventFields.size(); i++) {
            EventDataEntry currentEntry = eventFields.get(i);
            eventString = eventString + "- " + currentEntry.getColumnName();
            eventString = eventString + " : "
                    + currentEntry.getValues().toString();
            eventString = eventString + "\n";
        }
        eventString = eventString + "********************";
        return eventString;
    }

    public void printEventToConsole() {
        System.out.println("- Event -");
        for (int i = 0; i < eventFields.size(); i++) {
            EventDataEntry currentEntry = eventFields.get(i);
            System.out.println("* " + currentEntry.getColumnName() + " *");
            System.out.println(currentEntry.getValues().toString());
        }
        System.out.println("---------");
    }

    /**
     * @return Returns the eventFields.
     * @uml.property name="eventFields"
     */
    public Vector<EventDataEntry> getEventFields() {
        return eventFields;
    }

    /**
     * @param eventFields
     *            The eventFields to set.
     * @uml.property name="eventFields"
     */
    public void setEventFields(Vector<EventDataEntry> eventFields) {
        this.eventFields = eventFields;
    }
}

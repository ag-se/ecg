package org.electrocodeogram.module.source.implementation;

import java.util.Collection;
import java.util.LinkedList;

/**
 * The EventDataEntry class is used to represent a xml Element or xml Attribute
 * which is stored in the database with its values. A simple Element or
 * Attribute is represented in the database as a column in a table and the
 * corresponding values are the rows in the table. The element or attribute name
 * and the name of the column representing the element or attribute are equal.
 * 
 * Some Elements in a XML document can occur more than once. Then the element is
 * stored in a separate table with a column which has the elements name and a
 * linkid column which has a foreign key relation to the primary key column
 * linkid in the commondata table. When an element in a xml document occurs more
 * than once, then the value of each such element is stored as a row in the
 * element's table with the same linkid value.
 * 
 * 
 * @author jule
 * @version 1.0
 */
public class EventDataEntry {
    /**
     * Constructor
     * 
     * @param name
     *            the name of the element or attribute
     */
    public EventDataEntry(final String name) {
        this.columnName = name;
    }

    /**
     * The name of the column in the database table.
     */
    private String columnName = "";

    /**
     * The values for the column for the event.
     */
    private LinkedList<String> values = new LinkedList<String>();

    /**
     * Get the name of the Element or attribute which is equal to the name of
     * the column in the database which represents the element or attribute.
     * 
     * @return Returns the columnName which is equal to the element or attribute
     *         name.
     * 
     */
    public String getColumnName() {
        return columnName;
    }

    /**
     * Returns the values for the element or attribute. For an element with
     * maxOccurs>1 in the xml document there can be more than one value in the
     * LinkedList. The number of values for a element depends on the number of
     * occurences of the element in the xml document.
     * 
     * @return the list with the values for the element
     * 
     */
    public LinkedList getValues() {
        return values;
    }

    /**
     * Some elements like the commondata elements can have only one value, so
     * this value is returned directly.
     * 
     * @return the value for the element
     */
    public final String getElementValue() {
        return values.getFirst();
    }

    /**
     * Set LinkedList containing the values for the element or attribute.
     * 
     * @param values
     *            The values to set.
     * 
     */
    public void setValues(final LinkedList<String> values) {
        this.values = values;
    }

    /**
     * Fill the List which contains the values with a collection of values.
     * 
     * @param c
     *            the collection containing the values
     */
    public void fillValues(final Collection<String> c) {
        values.addAll(c);
    }
}

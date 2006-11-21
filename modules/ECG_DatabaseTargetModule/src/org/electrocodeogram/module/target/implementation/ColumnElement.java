package org.electrocodeogram.module.target.implementation;

import java.util.logging.Logger;

import org.electrocodeogram.logging.LogHelper;

/**
 * @author jule
 * @version 1.0 This class represents a Column in a Database table which holds
 *          the name of the column (=name of the element in the database), the
 *          xml type of the element in the xml Document and the corresponding
 *          sql datatype for the column in the table
 */
public class ColumnElement {

    /**
     * The name of the element in the xml-Document whic his equal to the name of
     * the column in the database.
     */
    private String name = null;

    /**
     * The SQL Datatype for the row in the database table.
     */
    private String sqlType = null;

    /**
     * The XML type of the element in the XML Document.
     */
    private String xmlType = null;

    /**
     * Denotes that the names of two ColumnElement instances are equal.
     */
    public static final int NAME_EQUAL = 1;

    /**
     * Denotes that no attributes of two ColumnElement instances are equal.
     */
    public static final int NOT_EQUAL = 0;

    /**
     * This is the logger.
     */
    private static Logger logger = LogHelper.createLogger(ColumnElement.class
            .getName());

    /**
     * The Constructor for a ColumnElement, where the sqlDT and the xmlDT are
     * optional.
     * 
     * @param name
     *            the name of the element
     * @param sqlDT
     *            the sql datatype for the element which may be null
     * @param xmlDT
     *            the xml type for the element which may be null
     */
    public ColumnElement(final String name, final String sqlDT,
            final String xmlDT) {
        if (name != null) {
            this.name = name;
        }
        if (sqlDT != null) {
            this.sqlType = sqlDT;
        }
        if (xmlDT != null) {
            this.xmlType = xmlDT;
        }
    }

    /**
     * This Method helps to decide whether an Element in a xml schema is equal
     * to a column in a database table or wheter the element in the schema and
     * the column in the database table have different types
     * @param e1
     *            the one ColumnElement instance
     * @param e2
     *            the other ColumnElement instance
     * @return an int which represents the equality status
     */
    public static int compareWithDbColumn(final ColumnElement e1,
            final ColumnElement e2) {
        if (equalName(e1, e2)) {
            logger.info("The Names are Equal: " + e1.getName());
            return NAME_EQUAL;
        }
        else
            logger.info("Names " + e1.getName() + " and " + e2.getName()
                    + " are not equal");
        return NOT_EQUAL;
    }

    /**
     * This Method checks whether the names of two ColumnElements are equal.
     * 
     * @param e1
     *            the one ColumnElement instance
     * @param e2
     *            the other ColumnElement instance
     * @return true if the names are equal, fals otherwise
     */
    private static boolean equalName(final ColumnElement e1,
            final ColumnElement e2) {
        return e1.getName().equalsIgnoreCase(e2.getName());
    }

    /**
     * Get the name of the ColumnElement.
     * 
     * @return Returns the name.
     */
    public final String getName() {
        return this.name;
    }

    /**
     * Set the name for the ColumnElement.
     * 
     * @param nameToSet
     *            The name to set.
     */
    public final void setName(final String nameToSet) {
        this.name = nameToSet;
    }

    /**
     * Get the sql datatype of this ColumnElement.
     * 
     * @return Returns the sqlType
     */
    public final String getSqlType() {
        return sqlType;
    }

    /**
     * Set the sql datatype of this ColumnElement.
     * 
     * @param sqlTypeToSet
     *            The sqlType to set
     */
    public final void setSqlType(final String sqlTypeToSet) {
        this.sqlType = sqlTypeToSet;
    }

    /**
     * Get the xml type of this ColumnElement.
     * 
     * @return the xmlType
     */
    public final String getXmlType() {
        return xmlType;
    }

    /**
     * Set the xml type of this ColumnElement.
     * 
     * @param xmlTypeToSet
     *            The xmlType to set
     */
    public final void setXmlType(final String xmlTypeToSet) {
        this.xmlType = xmlTypeToSet;
    }

}

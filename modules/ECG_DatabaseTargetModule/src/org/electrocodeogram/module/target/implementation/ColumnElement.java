/**
 * 
 */
package org.electrocodeogram.module.target.implementation;

import java.util.logging.Logger;

import org.electrocodeogram.logging.LogHelper;

/**
 * @author jule
 * @version 1.0
 */
public class ColumnElement {

    private String name = null;

    private String sqlType = null;

    private String xmlType = null;

    /**
     * denotes that name, sqlType and xmlType of two ColumnElement instances are equal
     */
    public static final int allEqual = 0;

    /**
     * denotes that sqlType and xmlType of two ColumnElement instances are equal
     */
    public static final int xmlAndSqlEqual = 1;

    /**
     * denotes that name and sqlType of two ColumnElement instances are equal
     */
    public static final int nameAndSqlEqual = 2;

    /**
     * denotes that name and xmlType of two ColumnElement instances are equal
     */
    public static final int nameAndXmlEqual = 3;

    /**
     * denotes that only the names of two ColumnElement instances are equal
     */
    public static final int nameEqual = 4;

    /**
     * denotes that only the sqlTypes of two ColumnElement instances are equal
     */
    public static final int sqlEqual = 5;

    /**
     * denotes that only the xmlTypes of two ColumnElement instances are equal
     */
    public static final int xmlEqual = 6;

    /**
     * denotes that no attributes of two ColumnElement instances are equal
     */
    public static final int nothingEqual = 7;

    /**
     * This is the logger.
     */
    private static Logger logger = LogHelper
            .createLogger(ColumnElement.class.getName());
    /**
     * 
     * @param name
     * @param sqlDT
     * @param xmlDT
     */
    public ColumnElement(String name, String sqlDT, String xmlDT) {
        
        if (name != null) this.name = name;
        if (sqlDT != null) this.sqlType = sqlDT;
        if (xmlDT != null) {
            this.xmlType = xmlDT;
        }
    }

    /**
     * With this Method you can compare two ColumnElements
     * 
     * @param e1 one ColumnElement to compare
     *            
     * @param e2 the other ColumnElement to compare
     * @return an integer which represents which values of this and the given
     *         ColumnElement are equal
     */
    public int whichAreEqual(ColumnElement e1, ColumnElement e2) {
        logger.entering(ColumnElement.class.getName(), "whichAreEqual");
        if (equalsAll(e1,e2)) return allEqual;
        if (equalsXmlAndSqlType(e1,e2)) return xmlAndSqlEqual;
        if (equalsNameAndSqlType(e1,e2)) return nameAndSqlEqual;
        if (equalsNameAndXmlType(e1,e2)) return nameAndXmlEqual;
        if (equalName(e1,e2)) return nameEqual;
        if (equalSqlType(e1,e2)) return sqlEqual;
        if (equalXmlType(e1,e2)) return xmlEqual;
        
        else
            return nothingEqual;
    }

    /**
     * This Method helps to decide whether an Element in a xml schema is equal
     * to a column in a database table or wheter the element in the schema and
     * the column in the database table have different types
     * 
     * @param e1
     * @param e2
     * @return an int which represents the equality status
     */
    public static int compareWithDbColumn(ColumnElement e1, ColumnElement e2) {
        if (equalName(e1,e2)){
            return nameEqual;
        }
        else
            return nothingEqual;

    }

    /**
     * This Method compares two Elements and returnes true if all Properties of
     * the two elements are equal, otherwise false
     * 
     * @param e1
     * @param e2
     * @return wether this and the given Element are equal
     */
    private boolean equalsAll(ColumnElement e1, ColumnElement e2) {
        boolean nameEqal = equalName(e1, e2);
        boolean sqlEqual = equalSqlType(e1, e2);
        boolean xmlEqual = equalXmlType(e1,e2);
        return (nameEqal && sqlEqual && xmlEqual);
    }

    private boolean equalsNameAndSqlType(ColumnElement e1, ColumnElement e2) {
        boolean nameEqal = equalName(e1,e2);
        boolean sqlEqual = equalSqlType(e1,e2);
        return (nameEqal && sqlEqual);
    }

    private boolean equalsNameAndXmlType(ColumnElement e1, ColumnElement e2) {
        boolean nameEqal = equalName(e1, e2);
        boolean xmlEqual = equalXmlType(e1, e2);
        return (nameEqal && xmlEqual);
    }

    private static boolean equalName(ColumnElement e1, ColumnElement e2) {
        
        return e1.getName().equalsIgnoreCase(e2.getName());
    }

    private boolean equalSqlType(ColumnElement e1, ColumnElement e2) {
        return e1.getSqlType().equalsIgnoreCase(e2.getSqlType());
    }

    private boolean equalXmlType(ColumnElement e1, ColumnElement e2) {
        return e1.getXmlType().equalsIgnoreCase(e2.getXmlType());
    }

    private boolean equalsXmlAndSqlType(ColumnElement e1, ColumnElement e2) {
        boolean xmlType = e1.getXmlType().equalsIgnoreCase(e2.getXmlType());
        boolean sqlType = e1.getSqlType().equalsIgnoreCase(e2.getSqlType());
        return (xmlType && sqlType);

    }

    /**
     * @return Returns the name.
     */
    public String getName() {
        return this.name;
    }

    /**
     * @param name
     *            The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return Returns the sqlType.
     */
    public String getSqlType() {
        return sqlType;
    }

    /**
     * @param sqlType
     *            The sqlType to set.
     */
    public void setSqlType(String sqlType) {
        this.sqlType = sqlType;
    }

    /**
     * @return the xmlType.
     */
    public String getXmlType() {
        return xmlType;
    }

    /**
     * @param xmlType
     *            The xmlType to set.
     */
    public void setXmlType(String xmlType) {
        this.xmlType = xmlType;
    }

}

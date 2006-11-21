package org.electrocodeogram.module.target.implementation;

import java.util.Iterator;
import java.util.Vector;

/**
 * This class represents a Table which has to be mapped to the database. It
 * holds the information about the name and the columns of the table
 * 
 * @author jule
 * @version 1.0
 */
public class Table {
    private static final long serialVersionUID = 1L;

    /**
     * The Name of the Table
     */
    private String tableName;

    /**
     * A Vector containing elements which represent the columns of the Table
     */
    private Vector<ColumnElement> elements;

    /**
     * 
     * @param name
     *            the Name of the Table
     * @param v
     *            the Vector which holds the Elements of the Table
     */
    public Table(String name, Vector<ColumnElement> v) {
        this.tableName = name;
        this.elements = v;
    }

    /**
     * 
     * @param e
     */
    public void addVectorElement(ColumnElement e) {
        this.elements.add(e);
    }

    /**
     * Get the Vector containing the Columns of the Table
     * @return Returns the elements.
     */
    public Vector getElements() {
        return elements;
    }

    /**
     * Get the Name of the Table
     * 
     * @return Returns the tableName.
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * 
     * @return the names of the Columns in the Table
     */
    public Vector getColumnNames() {
        Vector<String> columnNames = new Vector<String>();
        for (Iterator iter = this.elements.iterator(); iter.hasNext();) {
            ColumnElement element = (ColumnElement) iter.next();
            String name = element.getName();
            columnNames.add(name);
        }
        return columnNames;
    }
}

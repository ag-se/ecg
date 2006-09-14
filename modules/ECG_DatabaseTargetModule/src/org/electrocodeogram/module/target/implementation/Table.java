package org.electrocodeogram.module.target.implementation;

import java.util.Iterator;
import java.util.Vector;

/**
 * this class holds information for a Table in the Database
 * @author jule
 * @version 1.0
 */
public class Table {
    
    private String tableName;
    private Vector<ColumnElement> elements;
    
    
    /**
     * 
     * @param name the Name of the Table
     * @param v the Vector which holds the Elements of the Table
     */
    public Table (String name, Vector<ColumnElement>v){
        this.tableName = name;
        this.elements = v;
    }
    
    /**
     * 
     * @param e
     */
    public void addVectorElement(ColumnElement e){
        this.elements.add(e);
    }

    /**
     * @return Returns the elements.
     */
    public Vector getElements() {
        return elements;
    }

    /**
     * @return Returns the tableName.
     */
    public String getTableName() {
        return tableName;
    }
    
    /**
     * 
     * @return the names of the Columns in the Table
     */
    public Vector getColumnNames(){
        Vector <String>columnNames = new Vector<String>();
        for (Iterator iter = this.elements.iterator(); iter.hasNext();) {
            ColumnElement element = (ColumnElement)iter.next();
            String name = element.getName();
            columnNames.add(name);
        }
        return columnNames;
    }
    
}

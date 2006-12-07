package org.electrocodeogram.module.source.implementation;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import javax.sql.rowset.CachedRowSet;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

/**
 * This class is a modified version of the example ResultSetTableModel.java from
 * the article 'Making SQL Queries with JDBC and Displaying Results with Swing'
 * [http://www.oreillynet.com/pub/a/oreilly/java/news/javaex_1000.html] 
 * This class takes a JDBC ResultSet object and implements the TableModel interface
 * in terms of it so that a Swing JTable component can display the contents of
 * the ResultSet. Note that it requires a scrollable JDBC 2.0 ResultSet. Also
 * note that it provides read-only access to the results
 */
public class TableModelFromResultSet implements TableModel {
    CachedRowSet results; // The ResultSet to interpret

    ResultSetMetaData metadata; // Additional information about the results

    int numcols;

    int numrows;

    /**
     * This constructor creates a TableModel from a ResultSet. 
     */
    public TableModelFromResultSet(CachedRowSet results) throws SQLException {
        this.results = results; // Save the results
        metadata = results.getMetaData(); // Get metadata on them
        numcols = metadata.getColumnCount(); // How many columns?
        results.last(); // Move to last row
        numrows = results.getRow(); // How many rows?
    }

    // These two TableModel methods return the size of the table
    public int getColumnCount() {
        return numcols;
    }

    public int getRowCount() {
        return numrows;
    }

    // This TableModel method returns columns names from the ResultSetMetaData
    public String getColumnName(int column) {
        try {
            return metadata.getColumnLabel(column + 1);
        }
        catch (SQLException e) {
            return e.toString();
        }
    }

    // This TableModel method specifies the data type for each column.
    // We could map SQL types to Java types, but for this example, we'll just
    // convert all the returned data to strings.
    public Class getColumnClass(int column) {
        return String.class;
    }

    /**
     * This is the key method of TableModel: it returns the value at each cell
     * of the table. We use strings in this case. If anything goes wrong, we
     * return the exception as a string, so it will be displayed in the table.
     * Note that SQL row and column numbers start at 1, but TableModel column
     * numbers start at 0.
     */
    public Object getValueAt(int row, int column) {
        try {
            results.absolute(row + 1); // Go to the specified row
            Object o = results.getObject(column + 1); // Get value of the
            // column
            if (o == null)
                return null;
            else
                return o.toString(); // Convert it to a string
        }
        catch (SQLException e) {
            return e.toString();
        }
    }

    // Our table isn't editable
    public boolean isCellEditable(int row, int column) {
        return false;
    }

    // Since its not editable, we don't need to implement these methods
    public void setValueAt(Object value, int row, int column) {
    }

    public void addTableModelListener(TableModelListener l) {
    }

    public void removeTableModelListener(TableModelListener l) {
    }
}

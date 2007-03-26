package org.electrocodeogram.module.target.implementation;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

/**
 * @author jule
 * @version 1.0
 * 
 * This Class holds the information for each msdt type which tables in the
 * database are involved to store the information of this msdt type in the
 * database
 * 
 * this class has to be a singelton class to ensure that only one Instance is
 * created
 */
public class TableInformation {
    /**
     * The HashMap where the key of each element is the name of a msdt and the
     * value is a Vector containing the names of the tables which are involved
     * in storing events of this msdt type
     */
    private HashMap<String, Vector> msdtTables;

    /**
     * the singleton instance
     */
    private static TableInformation uniqueInstance = null;

    /**
     * The constructor
     */
    private TableInformation() {
        msdtTables = new HashMap<String, Vector>();
    }

    /**
     * 
     * @return the Singleton instance
     */
    public static TableInformation getInstance() {
        if (uniqueInstance == null) {
            uniqueInstance = new TableInformation();
        }
        return uniqueInstance;
    }

    /**
     * Add the information of about a msdt and its table names to the HashMap
     * which hold the information about all msdts
     * 
     * @param msdtType
     *            the msdt
     * @param v
     *            the Names of the tables
     */
    public void addTableInformation(String msdtType, Vector v) {
        Vector<String> tables = new Vector<String>();
        for (Iterator iter = v.iterator(); iter.hasNext();) {
            Table table = (Table) iter.next();
            String tableName = table.getTableName();
            tables.add(tableName);
        }
        msdtTables.put(msdtType, tables);
    }

    /**
     * Get the names of the tables which are involved in storing the given msdt
     * 
     * @param msdtType
     * @return the HashSet with the Table Names
     */
    public Vector getTableNamesForMSDT(String msdtType) {
        if (msdtTables.containsKey(msdtType)) {
            return msdtTables.get(msdtType);
        }
        else {
            return null;
        }
    }
}

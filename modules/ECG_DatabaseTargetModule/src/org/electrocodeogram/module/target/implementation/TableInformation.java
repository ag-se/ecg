/**
 * 
 */
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

    private static HashMap<String, Vector> msdtTables;

    private static TableInformation uniqueInstance = null;

    private TableInformation() {
        msdtTables = new HashMap<String, Vector>();
    }

    /**
     * 
     * @return the Singleton instance
     */
    public static TableInformation Instance() {
        if (uniqueInstance == null) {
            uniqueInstance = new TableInformation();
        }
        return uniqueInstance;
    }

    /**
     * 
     * @param msdtType
     * @param v
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
            /**
             * @TODO Fehlermeldung wenn nicht vorhanden
             */
        }
    }

}

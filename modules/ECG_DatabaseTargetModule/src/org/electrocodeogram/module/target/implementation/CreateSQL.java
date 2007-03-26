package org.electrocodeogram.module.target.implementation;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Vector;
import java.util.logging.Logger;

import org.electrocodeogram.logging.LogHelper;

/**
 * this class proviedes several methods to generate Strings which are SQL
 * Statements for the Database which then can be executed by the DBCommunicator
 * 
 * @author jule
 * @version 1.0
 * 
 */
public class CreateSQL {

    /**
     * This is the logger.
     */
    private static Logger logger = LogHelper
            .createLogger(DatabaseTargetModule.class.getName());

    /**
     * empty Constructor
     */
    public CreateSQL() {
    }

    /**
     * this method generates the string which is executed by the DBCommunicator
     * to insert the common data Part of an event
     * 
     * it extracts the data from the event with the help of an event Proxy of
     * the event whose data has to be inserted in the database
     * 
     * the common data part always contains the event's msdt, the timestamp of
     * the event, the username and the projectname of the event
     * 
     * the common data table in the database contains this four columns and an
     * extra column linkid which is the table's primary key. because the primary
     * key is auto incremented by the dbms for each inserted event the common
     * data insert String contains a NULL value for this first column
     * 
     * @param eventProxy
     *            the proxy for the event whose data has to be inserted in the
     *            database
     * @return the string which represents the insert Statement for the database
     */
    public static String createCommonDataInsertStmt(
            ValidEventPacketProxy eventProxy, DBCommunicator dbCommunicator) throws SQLException {

        // the logger entering the method
        logger.entering(CreateSQL.class.getName(),
                "createCommonDataInsertStmt", new Object[] { eventProxy });

        /**
         * get the event's msdt which is part of the event, but not part of the
         * xml document in the event
         */
        String msdt = eventProxy.getMsdt();

        // the String which has to hold the insert Statement for the common Data
        String insertCommonDataString = "";

        /**
         * the event's timestamp, part of the event, but not part of the xml
         * document in the event
         */
        Date timestamp = eventProxy.getTimestamp();
        // generate SQL Timestamp from java Date
        Timestamp sqlTimestamp = new Timestamp(timestamp.getTime());
        /**
         * get the column names and their datatypes of the common data table
         * from the database
         */

        // the columns of the common data table in the database
        Vector commonColumns = DBTablesMetadataPool.getInstance()
                .getMetadataVector("commondata", dbCommunicator);

        insertCommonDataString = "INSERT INTO commondata VALUES (";

        // the null value for the auto generated linkid
        insertCommonDataString = insertCommonDataString + "NULL,";

        // the event's timestamp
        insertCommonDataString = insertCommonDataString + "'" + sqlTimestamp
                + "',";
        // the event's msdt
        insertCommonDataString = insertCommonDataString + "'" + msdt + "',";

        /**
         * insert the other common data from the event which is placed in the
         * event'sxml document
         */
        for (int i = 3; i < commonColumns.size(); i++) {
            ColumnElement tmpEntity = (ColumnElement) commonColumns.get(i);
            String elementName = tmpEntity.getName();

            /**
             * if in this event no data exists for the table column, i.e. the
             * element in the xml document is optional, then insert a NULL value
             * in the table
             */
            if (eventProxy.getElementValue(elementName) == null) {
                if (i < commonColumns.size() - 1) {
                    insertCommonDataString = insertCommonDataString + " NULL,";
                }
                else {
                    insertCommonDataString = insertCommonDataString + " NULL);";
                }
            }
            /**
             * else insert the data from the event for this column
             */
            else {
                String documentValueForElement = eventProxy
                        .getElementValue(elementName);
                if (i < commonColumns.size() - 1) {
                    insertCommonDataString = insertCommonDataString + "'"
                            + documentValueForElement + "',";
                }
                else {
                    insertCommonDataString = insertCommonDataString + "'"
                            + documentValueForElement + "');";
                }
            }
        }
        // the logger exiting the method
        logger.exiting(CreateSQL.class.getName(), "createCommonDataInsertStmt",
                new Object[] { eventProxy });
        return insertCommonDataString;
    }

    /**
     * this method generates the strings which are executed by the
     * DBCommunicator to insert the msdt data of an event
     * 
     * it extracts the data from the event with the help of an event Proxy of
     * the event whose data has to be inserted in the database
     * 
     * for an event's data there can be several tables in the database to hold
     * these data
     * 
     * each table for msdt data in the database contains an linkId column which
     * is a foreign key referencing the primary key of the common data table the
     * value for the foreign key column is given as an argument of the method
     * 
     * the algorithm first fetches the names of all tables which are concerned
     * in storing the data of the event
     * 
     * then it fetches for each of these tables the Metadata (column names and
     * datatypes) from the database
     * 
     * and for each column of a table it trys to get the corresponding data from
     * the event to add it to the insert String. This works because each column
     * has the same name as the corresponding element in the xml document
     * 
     * if the event does not contain any data for a column (element) then the
     * NULL value is added to the insert string
     * 
     * @param eventProxy
     *            the Proxy for the event from which to store the data
     * @param linkId
     *            the linkid value for the foreign key column in each table
     * 
     * @return insertStmtsForAllTables the vector which holds the insert String
     *         for each table which is incorporated in storing the data of the
     *         event
     */
    public static Vector createMSDTInsertStmts(
            final ValidEventPacketProxy eventProxy, final int linkId,
            final DBCommunicator dbCommunicator) throws SQLException {

        // the logger entering the method
        logger.entering(CreateSQL.class.getName(), "createMSDTInsertStmt",
                new Object[] { eventProxy, linkId });

        /**
         * Vector which holds the insert statement for all Tables which are
         * incorporated in storing the events data
         */
        Vector<String> insertStmtsForAllTables = new Vector<String>();

        /**
         * the names of the Tables in the Database which are incorporated in
         * holding the Information of this msdt Type in the Database
         */

        Vector tableNamesInDatabase = TableInformation.getInstance()
                .getTableNamesForMSDT(eventProxy.getMsdt());
        if(tableNamesInDatabase.size()<= 0){
            logger.warning("No Table Information found for msdt Type "+eventProxy.getMsdt());
        }

        /**
         * for each table which is involved in storing the msdt data
         */
        for (int k = 0; k < tableNamesInDatabase.size(); k++) {

            String currentTable = (String) tableNamesInDatabase.get(k);

            /**
             * get the column names and their datatypes from the database
             */
            Vector currentTableColumns = DBTablesMetadataPool.getInstance()
                    .getMetadataVector(currentTable, dbCommunicator);

            String insertDataString = "INSERT INTO " + currentTable;
            insertDataString = insertDataString + " VALUES (";
            insertDataString = insertDataString + "'" + linkId + "', ";

            /**
             * for each column in the current table try to get the data from the
             * event
             */
            for (int i = 0; i < currentTableColumns.size(); i++) {

                ColumnElement tmpEntity = (ColumnElement) currentTableColumns
                        .get(i);
                // get Name of the current column
                String currentColumn = tmpEntity.getName();

                if (currentColumn.equalsIgnoreCase("linkid")) {
                    continue;
                }

                String documentValueForElement = eventProxy
                        .getElementValue(currentColumn);

                // if the event does not contain any data for the column
                // insert the NULL value
                if (eventProxy.getElementValue(currentColumn) == null) {
                    if (i < currentTableColumns.size() - 1) {
                        insertDataString = insertDataString + " NULL,";
                    }
                    else {
                        insertDataString = insertDataString + " NULL);";
                    }
                    continue;
                }
                // otherwise add the value of the corresponding element to the
                // string
                else {
                    if (i == currentTableColumns.size() - 1) {
                        insertDataString = insertDataString + "'"
                                + documentValueForElement + "');";
                    }

                    else {
                        insertDataString = insertDataString + "'"
                                + documentValueForElement + "', ";
                    }

                }
            }
            logger.info("Insert Statement: " + insertDataString);
            insertStmtsForAllTables.add(insertDataString);
        }
        // the logger exiting the method
        logger.exiting(CreateSQL.class.getName(), "createMSDTInsertStmt",
                new Object[] { eventProxy, linkId });
        return insertStmtsForAllTables;
    }

    /**
     * This method returns a String which represents the CREATE TABLE Statement
     * for a given Table which then can be executed by the DBCommunicator
     * 
     * @see DBCommunicator
     * 
     * @param tableToCreate
     *            the Table instance which holds the Information for the
     *            Database Table which has to be created
     * 
     * @return the String with the "CREATE TABLE ..." Statement
     */
    public static String createTableStmt(final Table tableToCreate) {

        // the logger entering the method
        logger.entering(CreateSQL.class.getName(), "createTableStmt",
                new Object[] { tableToCreate });
        logger.info("CREATE TABLE " + tableToCreate.getTableName());
        // set the sql types or the given xml types in the table's elements
        SqlDatatypes sqlDT = new SqlDatatypes();
        sqlDT.setSqlTypes4Elements(tableToCreate.getElements());

        String createTable = "CREATE TABLE ";
        createTable = createTable + tableToCreate.getTableName() + " (";

        Vector columns = tableToCreate.getElements();
        if (columns.size() == 0) {
            createTable = createTable + "linkid INTEGER);";
        }
        else {
            createTable = createTable + "linkid INTEGER, ";
        }

        for (int i = 0; i < columns.size(); i++) {
            ColumnElement temp = (ColumnElement) columns.get(i);
            String columnName = temp.getName();
            String SqlDatatype = temp.getSqlType();
            createTable = createTable + columnName + " ";
            if (i < columns.size() - 1) {
                createTable = createTable + SqlDatatype + ", ";
            }
            else
                createTable = createTable
                        + SqlDatatype
                        + ", FOREIGN KEY (linkid) REFERENCES commonData(linkid) ON DELETE CASCADE);";
        }
        // the logger exiting the method
        logger.exiting(CreateSQL.class.getName(), "createTableStmt",
                new Object[] { tableToCreate });
        return createTable;
    }

    /**
     * creates the commonData Table
     * 
     * 
     * @return the String which represents the createCommonData Table Statement
     */
    public static String createCommonDataTable(final Table commondataTable) {
        // the logger entering the method
        logger.entering(CreateSQL.class.getName(), "createCommonDataTable",
                new Object[] { commondataTable });

        logger.info("CREATE TABLE " + commondataTable.getTableName());
        // set the sql types or the given xml types in the table's elements

        SqlDatatypes sqlDT = new SqlDatatypes();
        sqlDT.setSqlTypes4Elements(commondataTable.getElements());
        Vector columns = commondataTable.getElements();

        String createCommonData = "CREATE TABLE commondata (";
        createCommonData = createCommonData
                + "linkid INTEGER NOT NULL AUTO_INCREMENT,";
        createCommonData = createCommonData + "timestamp TIMESTAMP NOT NULL,";
        createCommonData = createCommonData + "msdt VARCHAR(30),";

        for (int i = 0; i < columns.size(); i++) {
            ColumnElement temp = (ColumnElement) columns.get(i);
            String columnName = temp.getName();
            String SqlDatatype = temp.getSqlType();
            createCommonData = createCommonData + columnName + " ";
            if (i < columns.size() - 1) {
                createCommonData = createCommonData + SqlDatatype + ", ";
            }
            else
                createCommonData = createCommonData + SqlDatatype
                        + ", PRIMARY KEY(linkid))ENGINE=INNODB; ";
        }
        return createCommonData;
    }

    /**
     * When a Schema contains a column which does not exist in the corresponding
     * table in the database then with this Method you can create a String which
     * represents the ALTER TABLE Statement for adding this column to the table
     * in the database.
     * 
     * @param schemaTable
     *            the Table to which the column has to be added
     * @param columnToAdd
     *            the column which has to be added to the table
     * @return the string which represents the ALTER TABLE ADD COLUMN statement
     */
    public static String alterTableNewColumn(final Table schemaTable,
            final ColumnElement columnToAdd) {

        // the logger entering the method
        logger.entering(CreateSQL.class.getName(), "alterTableNewColumn",
                new Object[] { schemaTable, columnToAdd });
        logger.info("ALTER TABLE " + schemaTable.getTableName()
                + " ADD COLUMN " + columnToAdd.getName());
        // Name of column to add
        String newColumnName = columnToAdd.getName();
        // type of the column to add
        String newColumnType = columnToAdd.getSqlType();

        // the string representing the ALTER TABLE ADD COLUMN ... Statement
        String alterTableStmt = "ALTER TABLE " + schemaTable.getTableName()
                + " ADD COLUMN ";
        alterTableStmt = alterTableStmt + newColumnName + " " + newColumnType
                + ";";

        // the logger exiting the method
        logger.exiting(CreateSQL.class.getName(), "alterTableNewColumn",
                new Object[] { schemaTable, columnToAdd });
        return alterTableStmt;
    }

    /**
     * When a Schema contains a column whose Type does is not the same as the
     * type of the corresponding column in the database then with this Method
     * you can create a String which represents the ALTER TABLE Statement for
     * modifing the type of the column in the database table.
     * 
     * @param schemaTable
     *            the table containg the column with the type to which the
     *            column in the table has to be changed
     * @param columnToChange
     *            the column with the type to change
     * @return the string which represents the ALTER TABLE MODIFY... statement
     */
    public static String alterTableSqlType(final Table schemaTable,
            final ColumnElement columnToChange) {

        // the logger entering the method
        logger.entering(CreateSQL.class.getName(), "alterTableSqlType",
                new Object[] { schemaTable, columnToChange });
        logger.info("ALTER TABLE " + schemaTable.getTableName() + " MODIFY "
                + columnToChange.getName() + " " + columnToChange.getSqlType());
        String modifyColumnType = "ALTER TABLE " + schemaTable.getTableName()
                + " MODIFY ";
        // the name of the column whose type has to be changed
        modifyColumnType = modifyColumnType + columnToChange.getName() + " ";
        // the type to which the column in the database table has to be changed
        modifyColumnType = modifyColumnType + columnToChange.getSqlType() + ";";

        // the logger exiting the method
        logger.exiting(CreateSQL.class.getName(), "alterTableSqlType",
                new Object[] { schemaTable, columnToChange });

        return modifyColumnType;
    }

}

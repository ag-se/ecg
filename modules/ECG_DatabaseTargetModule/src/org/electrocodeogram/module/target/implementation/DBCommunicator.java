/**
 * 
 */
package org.electrocodeogram.module.target.implementation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Logger;

import org.electrocodeogram.event.ValidEventPacket;
import org.electrocodeogram.logging.LogHelper;
import org.electrocodeogram.msdt.MicroSensorDataType;
import org.electrocodeogram.system.ModuleSystem;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.DatabaseMetaData;
import com.mysql.jdbc.ResultSet;
import com.mysql.jdbc.ResultSetMetaData;
import com.mysql.jdbc.Statement;

/**
 * @author jule
 * @version 1.0
 */
public class DBCommunicator {

    /**
     * this is the logger.
     */
    private static Logger logger = LogHelper.createLogger(DBCommunicator.class
            .getName());

    private Connection connection;
    
    private String db_user;
    
    private String db_pwd;
    
    private String db_URL;
    
    private String db_driver;
    
    public DBCommunicator(String user, String pwd, String dbURL, String jdbcDriver){
    	this.db_user=user;
    	this.db_pwd = pwd;
    	this.db_URL = dbURL;
    	this.db_driver = jdbcDriver;
    }

    /**
     * this method connects the Database and returns a Connection object which
     * represents the open connection to the database
     * 
     * @return conn the Connection to the Database or null if an exeption was
     *         thrown
     * @pre Connection conn is not opened
     * @post an open Database Connection is returned by this Method if no
     *       exception occures
     * @post null is returned if an exeption occured
     * @post Connection conn is still closed if an exception occured
     */
    public Connection getDBConnection() {

        if (connection == null || connection.isClosed()) {
            // the logger logs entering this method
            logger.entering(
                    "org.electrocodeogram.module.target.implementation."
                            + "DBCommunicator", "getDBConnection()");

            // the database connection which has to be returned
            try {
                // register driver
                Class.forName(db_driver).newInstance();
            }
            catch (InstantiationException e) {
                logger.severe("database driver could not be instantiated");
                e.printStackTrace();
                return null;
            }
            catch (IllegalAccessException e) {
                logger.severe("could not connect the Database in cause of an "
                        + "illegal access");
                e.printStackTrace();
                return null;
            }
            catch (ClassNotFoundException e) {
                logger.severe("database driver class not found");
                e.printStackTrace();
                return null;
            }

            try {
                logger.info("want to connect the Database with URL " + db_URL
                        + " and User " + db_user);
                // Connect the Database with user and password
                connection = (Connection) DriverManager.getConnection(db_URL,
                        db_user, db_pwd);
                logger.info("Database connected with Connection: "
                        + connection.toString());
            }
            catch (SQLException sqle) {
                logger.severe("Tried to get a connection but a SQL "
                        + "Exception was thrown");
                sqle.printStackTrace();
                return null;
            }
            // the logger logs leaving the method
            logger.exiting("org.electrocodeogram.module.target.implementation."
                    + "DBCommunicator", "getDBConnection()", connection);
        }
        return connection;

    }

    /**
     * close the database connection
     * 
     * @pre connection is opened
     * @post connection is closed
     */
    public void closeDBConnection() {
        // the logger logs entering this method
        logger.entering("org.electrocodeogram.module.target.implementation."
                + "DBCommunicator", "closeDBConnection()");
        if (connection.isClosed()) {
            // the logger logs entering this method
            logger.exiting("org.electrocodeogram.module.target.implementation."
                    + "DBCommunicator", "closeDBConnection()");
            return;
        }

        try {
            // close the connection
            connection.close();
        }
        catch (SQLException e) {
            logger.warning("SQL Exception was thrown, could not "
                    + "close the given connection");
            e.printStackTrace();
        }
    }

    /**
     * This method return true if an database connection is established, false
     * otherwise
     * 
     * @return true if the database is connected
     */
    public boolean isDbConnected() {
        if (connection.isClosed()) return false;
        else
            return true;
    }

    /**
     * This Method connects the Database, gets the Metadata from a given table
     * as a ResultSet and closes the database connection. It returns null if the
     * Query could not be executed.
     * 
     * @param table
     *            the table from which to get the Metadata
     * @return a ResultSet containing the Metadata or null if an exception
     *         occured, suppose for example the table with the given name does
     *         not exist in the database
     */
    private ResultSetMetaData getMetadata(String table) {

        // the logger logs entering this method
        logger.entering("org.electrocodeogram.module.target.implementation."
                + "DBCommunicator", "getMetadata()");
        if (table.length() <= 0) {
            throw new IllegalArgumentException("empty table name is not valid");
        }

        /**
         * get DB connection
         * 
         * @see org.electrocodeogram.module.target.implementation.
         *      DBCommunicator#getDBConnection() getDBConnection
         */
        Connection c = getDBConnection();

        // the Statement which has to execute the Query
        Statement stmt = null;

        // the ResultSet which has to hold the Metadata of the given table
        ResultSetMetaData rsmd = null;

        try {
            // create Statement Object
            stmt = (Statement) c.createStatement();

            // get the ResultSet from the executed Query
            ResultSet rs = (ResultSet) stmt.executeQuery("SELECT * FROM "+ table);

            // get the Metadata of the table from the ResultSet
            rsmd = (ResultSetMetaData) rs.getMetaData();

        }
        catch (SQLException e) {
            logger
                    .severe("An SQL Exception occured while getting the Metadata "
                            + "for the table " + table);
            e.printStackTrace();
            return null;
        }

        // the logger logs the leaving of the method
        logger.exiting("org.electrocodeogram.module.target.implementation."
                + "DBCommunicator", "closeDBConnection()", rsmd);

        // return the result set containing the tables's Metadata
        return rsmd;
    }

    /**
     * With this Method you can execute an arbitrary Statement to the Database
     * which is just a Statement and so does not deliver a Result Set
     * 
     * the Method gets an open connection and executes the statement
     * 
     * @param sqlString
     *            the String which contains the SQL Statement
     * @return true if the Statement could be executed or false if not or if a
     *         SQLException occured
     */
    public boolean executeStmt(String sqlString) {

        // the logger logs entering this method
        logger.entering("org.electrocodeogram.module.target.implementation."
                + "DBCommunicator", "insertStatement(...)");
        /**
         * get DB connection
         * 
         * @see org.electrocodeogram.module.target.implementation.
         *      DBCommunicator#getDBConnection() getDBConnection
         */
        Connection conn = getDBConnection();

        // Statement which executes the SQL statement
        Statement stmt = null;
        try {
            stmt = (Statement) conn.createStatement();
            stmt.execute(sqlString);

        }
        catch (SQLException e) {
            logger.info("The statement could not be executed on the Database");
            System.out.println("Statement: "+sqlString);
            e.printStackTrace();

            return false;
        }
        // the logger logs the leaving the method
        logger
                .exiting(
                        "org.electrocodeogram.module.target.implementation.DBCommunicator",
                        "insertStatement(...)", true);
        return true;

    }

    /**
     * This method allows you to query the database and returns the result of
     * the given query as a ResultSet.
     * 
     * @param sqlString
     * @return ResultSet containg the data satifys the query or null if an
     *         Exception occures
     * @pre the ResultSet rs is null
     * @post the ResultSet rs is empty if there is no data in the database which
     *       satisfys the query or if there is data in the db which satisfys the
     *       query the returned ResultSet rs contains this data
     * @post the ResultSet rs is null if an exception occured
     */
    public ResultSet executeQuery(String sqlString) {
        // the logger logs entering this method
        logger.entering("org.electrocodeogram.module.target.implementation."
                + "DBCommunicator", "queryDB(...)");
        /**
         * get DB connection
         * 
         * @see org.electrocodeogram.module.target.implementation.
         *      DBCommunicator#getDBConnection() getDBConnection
         */
        Connection conn = getDBConnection();
        /**
         * get Statement from connection
         * 
         * @see com.mysql.jdbc.Statement
         */
        Statement stmt = null;

        /**
         * the ResultSet which has to hold the data which is returned as a
         * result from the query
         * 
         * @see com.mysql.jdbc.ResultSet
         */
        ResultSet rs = null;
        try {
            stmt = (Statement) conn.createStatement();
            rs = (ResultSet) stmt.executeQuery(sqlString);

        }
        catch (SQLException e) {
            logger.info("The statement could not be executed on the Database");
            System.out.println("Statement: "+sqlString);
            e.printStackTrace();

            return null;
        }
        logger
                .exiting(
                        "org.electrocodeogram.module.target.implementation.DBCommunicator",
                        "queryDB(...)", rs);
        // return the resultSet containing the data
        return rs;
    }

    /**
     * Insert an Event into the database requires in the main three steps
     * 
     * 1st insert the data which is common to all events in the 'common' table
     * 
     * 2nd get the auto-generated key from this insert operation to link the
     * events data in the other tables for the msdt data
     * 
     * 3rd insert the msdt specific data in the corresponding tables with the
     * key value from the first insert operation as variable for the foreign key
     * column of the linked tables
     * 
     * to ensure the integrity of the database data these tree steps have to be a
     * transaction.
     * 
     * @pre the event is a ValidEventPacket
     * @pre the event's data is not in the database
     * @post the event's whole data is in the database
     * @post if an exception occured nothing was written in the db
     * 
     * @param vPacket
     *            the Event which has to be inserted in the db
     * @return true if the event's data was written in the db, otherwise false
     */
    public boolean insertEvent(final ValidEventPacket vPacket) {

        // the logger logs entering this method
        logger.entering("org.electrocodeogram.module.target.implementation."
                + "DBCommunicator", "insertEvent(...)");
        /**
         * get Proxy for the Event for better access to the Events data
         * 
         * @see org.electrocodeogramm.module.target.implementation.
         *      ValidEventPacketProxy
         */
        ValidEventPacketProxy proxy = new ValidEventPacketProxy(vPacket);

        /**
         * get DB connection
         * 
         * @see org.electrocodeogram.module.target.implementation.
         *      DBCommunicator#getDBConnection() getDBConnection
         */
        Connection conn = getDBConnection();

        // the statement which executes the SQL
        Statement stmt = null;

        try {
            /**
             * Disable auto-commit because the following steps are considered to
             * be a transaction. with auto-commit off the statement does not
             * commit after every executed statement
             */
            conn.setAutoCommit(false);
            stmt = (Statement) conn.createStatement();

            
            /**
             * 1st execute Statement to insert the event's common data
             */
            String s = CreateSQL.createCommonDataInsertStmt(proxy, this);
            System.out.println(s);
            stmt.execute(s,
                    Statement.RETURN_GENERATED_KEYS);

            /**
             * 2nd get the auto-generated linkid value from the last insert
             * operation in the commondata table
             */
            ResultSet idKey = (ResultSet) stmt.getGeneratedKeys();
            int idRow = 0;
            while (idKey.next()) {
                // get the generated value
                idRow = idKey.getInt(1);
                logger.info("Id last inserted row " + idRow);
            }

            /**
             * 3rd insert the msdt special Data in the corresponding tables with
             * id value as a link to the event's common data
             */
            Vector insertStms = CreateSQL.createMSDTInsertStmts(proxy, idRow, this);
            for (Iterator iter = insertStms.iterator(); iter.hasNext();) {
                String insertStatement = (String) iter.next();
                stmt.execute(insertStatement);
            }

            // if no exception occured commit the last steps
            conn.commit();

        }
        catch (SQLException e) {
            try {
                // rollback if a SQLException was thrown
                conn.rollback();
            }
            catch (SQLException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

            logger.info("The Event could not be inserted into the Database");
            e.printStackTrace();

            return false;

        }
        catch (Exception e) {
            logger.info("The Event was not inserted into the Database");
            try {
                // if any Exception occured rollback the last operations
                conn.rollback();
                e.printStackTrace();
            }
            catch (SQLException e1) {
                logger.info("The Event was not inserted into the Database");
                e1.printStackTrace();
            }

            return false;

        }
        logger.exiting("org.electrocodeogram.module.target.implementation."
                + "DBCommunicator", "insertEvent(...)", true);
        return true;
    }

    /**
     * This method returns the Metadata of a given table (Coloumn names and
     * Datatypes)in the same order (from left to right) they occur in the table.
     * 
     * @pre the table from which to retrieve the Metadata exists in the db
     * @post the returned vector holds informations about name and datatype of
     *       each row in the given table
     * @post the order of the Entity elements in the vector corresponds to the
     *       order of the rows in the table
     * @param table
     *            the table from which the metadata is required
     * @return the Vector containing the Metadata as Elements
     * 
     */
    public Vector getMetadataInColumnOrder(String table) {

        logger.entering("org.electrocodeogramm.module.target.implementation."
                + "DBCommunicator", "getMetadataInColumnOrder(...)");

        // Vector to hold the Metadata in column order
        Vector<ColumnElement> v = new Vector<ColumnElement>();

        // get MetadataResultSet for the given table
        ResultSetMetaData rsmd = getMetadata(table);

        int i = 1;
        try {
            // write Metadata from MetadataResultSet in Vector
            while (i <= rsmd.getColumnCount()) {
                // create for each column name and column datatype an Entity
                // Object
                ColumnElement tmp = new ColumnElement(rsmd.getColumnName(i), rsmd
                        .getColumnTypeName(i), null);
                // add the Entity Object to the Vector
                v.add(tmp);
                i++;
            }
        }
        catch (SQLException e) {
            logger.severe("while creating the MetadataVector from the "
                    + "MetadataResultSet a SQL Exception occured ");
            e.printStackTrace();
            return null;
        }
        // the logger logs the leaving of the method
        logger.exiting("org.electrocodeogramm.module.target.implementation."
                + "DBCommunicator", "getMetadataInColumnOrder(...)");
        return v;
    }

    /**
     * This method querys the database for all tablenames in the database.
     * 
     * @post there is no table in the database whose name is not in the returned
     *       vector
     * @return a Vector containing the names of all tables in the db
     */
    public Vector getTableNames() {
        // the logger logs entering this method
        logger.entering("org.electrocodeogramm.module.target.implementation."
                + "DBCommunicator", "getTableNames()");
        /**
         * get DB connection
         * 
         * @see org.electrocodeogram.module.target.implementation.
         *      DBCommunicator#getDBConnection() getDBConnection
         */
        Connection c = getDBConnection();
        Statement stmt;
        // the RsultSet which holds the returned tablenames
        ResultSet tableNames = null;

        // the Vector in which the tablenames from the ResultSet are written
        Vector<String> v = new Vector<String>();
        try {
            stmt = (Statement) c.createStatement();
            // select the right database from which to retrieve the tablenames
            stmt.execute("USE ecg_test;"); // TODO

            // get Metadata from the selected database
            DatabaseMetaData dbmd = (DatabaseMetaData) c.getMetaData();
            String[] names = { "TABLE" };
            // extract the Tablenames from the Metadata
            tableNames = (ResultSet) dbmd.getTables(null, null, "%", names);

            // write tablenames from ResultSet in the Vector
            while (tableNames.next()) {
                v.add(tableNames.getString("TABLE_NAME"));
            }

        }
        catch (SQLException e) {
            logger
                    .warning("While getting the tablenames a SQLException occured");
            e.printStackTrace();

        }

        logger.exiting("org.electrocodeogramm.module.target.implementation."
                + "DBCommunicator", "getTableNames", v);
        return v;
    }

    /**
     * if the table exists true is returned, false otherwise
     * 
     * @param tableName
     * @return true if the given table with the given table name exists
     */
    public boolean tableExists(String tableName) {
        Connection c = getDBConnection();
        Statement stmt = null;
        try {
            stmt = (Statement) c.createStatement();
        }
        catch (SQLException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        try {
            stmt.execute("DESCRIBE " + tableName + ";");
            return true;
        }
        catch (SQLException e) {
            return false;
        }

    }
    
    public void getInformationAndSyncTables(){
    	
    	MicroSensorDataType[] msdts = ModuleSystem.getInstance().getMicroSensorDataTypes();

    	/**
         * For each schema: 
         * 1. create Proxy 
         * 2. getSchemaProperties(String) --> Vector with Table instances 
         * 3. for each table of the schema --> synchronizeTableToDatabase
         */

        File msdtFolder = new File(DBTargetModuleConstants.MSDT_FOLDER);
        if (msdtFolder.exists()) {
            logger.info("Folder msdt exists");
        }
        
        File commondataSchema = new File(DBTargetModuleConstants.MSDT_FOLDER+"msdt.common.xsd");
        XMLSchemaProxy commonProxy = new XMLSchemaProxy (commondataSchema, this);
  
        if (!(tableExists("commondata"))){
        	Table commondataTable = commonProxy.getCommonProperties();
        	String createCommondataTable  = CreateSQL.createCommonDataTable(commondataTable);
        	logger.info("must create commondata table in database");
        	executeStmt(createCommondataTable);
        	logger.info("commondata table successfully created");
        	
        }
        else{
        	commonProxy.synchronizeCommonSchemaToDatabase();
        	logger.info("synchronized commondata schema to database");
        }
        
        logger.info("Array with MSDTs has Length : "+msdts.length);

        for (int i = 0; i < msdts.length; i++) {
            	logger.info("found MSDT: "+msdts[i].getName());
                XMLSchemaProxy schemaProxy = new XMLSchemaProxy(msdts[i].getDefFile(), this);
                schemaProxy.synchronizeSchemaToDatabase();
                logger.info("synchronized "+msdts[i].getName()+" schema to database");
        }        
    }
    
    

}

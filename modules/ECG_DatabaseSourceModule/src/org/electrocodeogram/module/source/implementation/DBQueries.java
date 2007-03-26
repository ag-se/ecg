package org.electrocodeogram.module.source.implementation;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Vector;
import java.util.logging.Logger;
import javax.sql.rowset.CachedRowSet;
import org.electrocodeogram.logging.LogHelper;
import org.electrocodeogram.module.target.implementation.DBCommunicator;
import org.electrocodeogram.module.target.implementation.TableInformation;
import org.electrocodeogram.module.source.implementation.Event;
import com.mysql.jdbc.ResultSet;
import com.sun.rowset.CachedRowSetImpl;

public class DBQueries {
    /**
     * This is the logger.
     */
    private static Logger logger = LogHelper
            .createLogger(DatabaseSourceModule.class.getName());

    /**
     * This method fetches a complete event from the database
     * 
     * @param id
     *            the primary key of the event in the commondata table
     * @return the event data turned into an Event object
     */
    public static Event getEventByID(final String id, final DBCommunicator dbCom) throws SQLException {
        // get the commondata record from the event with the given primary key
        String query = SqlQueryStrings.getDataByID(id, "commondata");
        CachedRowSet commonData = returnCachedResult(query, dbCom);
        // fetch the rest of the event data from the tables which hold the
        // records for this event
        return getRestOfEvent(commonData, dbCom);
    }

    /**
     * 
     * @param username
     *            the username for the event
     * @param date
     *            the date of the event
     * @param dbCom
     *            the DBCommunicator for accessing the database
     * @return a CachedRowSet with the commondata attributes of the events which
     *         satisfy the given attribute values
     */
    public static CachedRowSet getEventsWithUsernameAndDate(String username,
            String date, DBCommunicator dbCom) throws SQLException {
        return returnCachedResult(SqlQueryStrings.queryWithUsernameAndDate(
                username, date), dbCom);
    }

    /**
     * 
     * @return a Hash Map where each entry is a Pair of a String describing the
     *         two runs events and a Vector containing HashMaps which hold the
     *         events between the two run Events
     */
    public static CachedRowSet getRunsWithTimediff(DBCommunicator dbCom) throws SQLException {
        // the resultSet contains the four columns run1, run1_TS (timestamp of
        // run1), run2 and runs2_TS (timestamp of run2)
        // each row in the result set represents two run events
        return returnCachedResult(SqlQueryStrings.queryRunsWithTimediff(),
                dbCom);
    }

    /**
     * Return all events between two given timestamps
     * 
     * @param run1_TS
     *            the first timestamp
     * @param run2_TS
     *            the second timestamp
     * @param dbCom
     * @return a CachedRowSet which holds the commondata information of all
     *         events between the two given timestamps
     */
    public static CachedRowSet getEventsBetweenTimestamps(String run1_TS,
            String run2_TS, DBCommunicator dbCom) throws SQLException {
        return returnCachedResult(SqlQueryStrings.queryEventsBetweenTimestamps(
                run1_TS, run2_TS), dbCom);
    }

    /**
     * Select and return all events between two other events given by their
     * primary key
     * 
     * @param firstID
     *            the value of the primary key for the first event
     * @param secondID
     *            the value of the primary key for the second event
     * @param dbCom
     * @return all events with primary key values greater than the primary key
     *         value of the first given event and smaller than the primary key
     *         value of the second given event
     */
    public static CachedRowSet getEventsBetweenTwoEvents(String firstID,
            String secondID, DBCommunicator dbCom) throws SQLException {
        return returnCachedResult(SqlQueryStrings.queryEventsBetweenTwoEvents(
                firstID, secondID), dbCom);
    }

    /**
     * This method executes a sql Query and transforms the returned ResultSet
     * from the Query in a CachedRowSet which then can be used for further
     * processing without an open database connection
     * 
     * @param query
     *            the given SQL Query
     * @param dbCom
     *            the DBCommunicator whic executes to the database
     * @return a CachedRowSet containing the result of the Query
     */
    private static CachedRowSet returnCachedResult(final String query,
            final DBCommunicator dbCom) throws SQLException {
        ResultSet currentEventSet = dbCom.executeQuery(query);
        CachedRowSet currentCommonData = null;
        try {
            currentCommonData = new CachedRowSetImpl();
            currentCommonData.populate(currentEventSet);
        }
        catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return currentCommonData;
    }

    /**
     * This method delivers for the given commondata Attributes of an event the
     * remaining data for the event
     * 
     * possibly the comonData resultSet contains data for several events
     * 
     * @param commonData
     *            the commondata Attributes of the event
     * @return the whole data records for an event as an Event object
     */
    public static Event getRestOfEvent(final CachedRowSet commonData,
            final DBCommunicator dbCom) {
        Event event = new Event();
        String msdt = "";
        String eventID = "-1";
        Vector columnNames = getColumnNamesForCachedRowSet(commonData, dbCom);
        try {
            // put the commonData to the Event
            for (int j = 0; j < columnNames.size(); j++) {
                EventDataEntry currentEntry = new EventDataEntry(columnNames
                        .get(j).toString());
                currentEntry.fillValues((Collection<String>) commonData
                        .toCollection(columnNames.get(j).toString()));
                event.addEntry(currentEntry);
            }
            msdt = event.getMSDT();
            eventID = "" + event.getPrimaryKey();
            Vector tableNames = TableInformation.getInstance()
                    .getTableNamesForMSDT(msdt);
            if (tableNames == null) {
                logger.severe("tableNames == NULL");
            }
            // get the data from each table which is involved storing the
            // event's data
            for (int i = 0; i < tableNames.size(); i++) {
                String tableName = (String) tableNames.get(i);
                String getData = SqlQueryStrings
                        .getDataByID(eventID, tableName);
                ResultSet rs = dbCom.executeQuery(getData);
                CachedRowSet data = null;
                try {
                    data = new CachedRowSetImpl();
                    data.populate(rs);
                }
                catch (SQLException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                event = addEventData(data, event);
            }
        }
        catch (SQLException e2) {
            // TODO Auto-generated catch block
            e2.printStackTrace();
        }
        // return the Vector containing the data for all events
        return event;
    }

    /**
     * This Mthod allows to query the table in the database and return the
     * result as a CachedRowSet
     * 
     * @param query
     *            the query to execute
     * @param dbCom
     *            the dbCommunicator which has direct access to the database
     * @return a CachesRowSet containing the result of the query
     */
    public static CachedRowSet executeUserQuery(final String query,
            final DBCommunicator dbCom) throws SQLException {
        ResultSet rs = dbCom.executeQuery(query);
        CachedRowSet data = null;
        try {
            data = new CachedRowSetImpl();
            data.populate(rs);
        }
        catch (SQLException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        return data;
    }

    /**
     * This Method gets a CachedRowSet an put the containing data into an Event
     * Object which the is returned. Because of the Event Object as a method
     * parameter it you can put the Information of several CachedRowSets into
     * one Event
     * 
     * @param rowset
     *            the CachedRowSet which holds the data to put in the Event
     * @param event
     *            the Event to put the data in
     * @return the Event containing the data from the given CachedRowSet
     */
    public static Event addEventData(final CachedRowSet rowset, Event event) {
        ResultSetMetaData rsmd = null;
        
        try {
            rsmd = rowset.getMetaData();
            for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                EventDataEntry currentEntry = new EventDataEntry(rsmd
                        .getColumnName(i));
                currentEntry.fillValues((Collection<String>) rowset
                        .toCollection(i));
                event.addEntry(currentEntry);
            }// END for
        }
        catch (SQLException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        return event;
    }

    /**
     * this method returns the names of all columns in a CachedRowSet as a
     * Vector of Strings
     * 
     * @param set
     *            the CachedRowSet with the columns
     * @param dbCom
     *            the DBCommunicator which has direct access to the database
     * @return the Vector containing the column names
     */
    private static Vector getColumnNamesForCachedRowSet(CachedRowSet set,
            DBCommunicator dbCom) {
        ResultSetMetaData rsmd;
        // Vector holding the column names
        Vector<String> columnNames = new Vector<String>();
        try {
            rsmd = set.getMetaData();
            // get all the column names of the commonData ResultSet
            for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                columnNames.add(rsmd.getColumnLabel(i));
            }
        }
        catch (SQLException e) {
            
            e.printStackTrace();
        }
        return columnNames;
    }
}

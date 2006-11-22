package org.electrocodeogram.module.source.implementation;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.rowset.CachedRowSet;
import org.electrocodeogram.logging.LogHelper;
import org.electrocodeogram.module.source.EventReader;
import org.electrocodeogram.module.source.SourceModule;
import org.electrocodeogram.module.source.SourceModuleException;
import org.electrocodeogram.module.target.implementation.DBCommunicator;
import org.electrocodeogram.module.target.implementation.DBTargetModuleConstants;
import org.electrocodeogram.modulepackage.ModuleProperty;
import org.electrocodeogram.modulepackage.ModulePropertyException;

/**
 * @author Lilly Cool
 */
public class DatabaseSourceModule extends SourceModule {
    /**
     * This is the logger.
     */
    private static Logger logger = LogHelper
            .createLogger(DatabaseSourceModule.class.getName());

    /**
     * The JDBC database connection string of the form
     * jdbc:dbms://host:port/database.
     */
    private String jdbcConnection;

    /**
     * The user name for the database.
     */
    private String username;

    /**
     * The password for the user.
     */
    private String password;

    /**
     * The driver for the database.
     */
    private String jdbcDriver;

    /**
     * The sql Query which may be given by the SQL Query Module Property.
     */
    private String SQLQuery;

    /**
     * The DBCommunicator which communicates with the database.
     */
    private DBCommunicator dbCommunicator;

    /**
     * This creates the module instance. It is called by the ECG
     * <em>ModuleRegistry</em> subsystem, when the user requested a new
     * instance of this module.
     * 
     * @param id
     *            This is the unique <code>String</code> id of the module
     * @param name
     *            This is the name which is assigned to the module instance
     */
    public DatabaseSourceModule(final String id, final String name) {
        super(id, name);
        logger.entering(this.getClass().getName(), "DatabaseTargetModule",
                new Object[] { id, name });
        /**
         * @TODO remove this
         */
        this.username = DBTargetModuleConstants.DB_USER;
        this.password = DBTargetModuleConstants.DB_PWD;
        this.jdbcConnection = DBTargetModuleConstants.DB_URL;
        this.jdbcDriver = DBTargetModuleConstants.DB_DRIVER;
        try {
            this.SQLQuery = this.getModuleProperty("SQL Query").getValue();
        }
        catch (ModulePropertyException e) {
            e.printStackTrace();
        }
        /**
         * TODO remove the block comment
         */
        /*
         * try { this.username = this.getModuleProperty("Username").getValue();
         * this.password = this.getModuleProperty("Password").getValue();
         * this.jdbcConnection = this.getModuleProperty("JDBC
         * Connection").getValue(); this.jdbcDriver =
         * this.getModuleProperty("JDBC Driver").getValue(); } catch
         * (ModulePropertyException e) { e.printStackTrace(); }
         * 
         */
        this.dbCommunicator = new DBCommunicator(username, password,
                jdbcConnection, jdbcDriver);
        logger.exiting(this.getClass().getName(), "DatabaseTargetModule");
    }

    /**
     * @see org.electrocodeogram.module.Module#initialize()
     */
    @Override
    public void initialize() {
    }

    /**
     * This is to be implemented by all actual <code>SourceModule</code>
     * implementations. It returns the module's {@link EventReader} as an array.
     * 
     * @return The module's {@link EventReader}
     */
    public EventReader[] getEventReader() {
        return null;
    }

    /**
     * This is to be implemented by all actual <code>SourceModule</code>
     * implementations. It is called during module activation before its
     * {@link EventReader} are started.
     * 
     * @throws SourceModuleException
     *             If an exception occurs while starting the module
     */
    public void preStart() throws SourceModuleException {
        CachedRowSet commonData;
        dbCommunicator.getInformationAndSyncTables();
        /**
         * if the SQL Query Property String is given, then execute the statement
         * and create ValidEventPackets from the Events in the ResultSet
         * 
         * There has to be the column linkid for the Events in the Result Set,
         * otherwise the ValidEventPackets cannot be created
         */
        if (this.SQLQuery != null) {
            commonData = DBQueries.executeUserQuery(this.SQLQuery,
                    dbCommunicator);
            Collection eventIDs = null;
            
            try {
                /**
                 * select all the linkid valuies for the events
                 */
                eventIDs = commonData.toCollection("linkID");
            }
            catch (SQLException e) {
               logger
                        .severe("was not able to fetch the linkid values " +
                                        "for the events");
                e.printStackTrace();
            }
            /**
             * create the ValidEventPacket for each given linkid value
             */
            for (Iterator iter = eventIDs.iterator(); iter.hasNext();) {
                String eventID = iter.next().toString();
                CreateEventFromDB.createEvent(eventID, dbCommunicator);
            }
        }
        new QueriesWindow(dbCommunicator);
    }

    /**
     * This is to be implemented by all actual <code>SourceModule</code>
     * implemenations. It is called during module deactivation after the
     * {@link EventReader} are stopped.
     */
    public void postStop() {
    }

    /**
     * @see org.electrocodeogram.module.Module#propertyChanged(org.electrocodeogram.modulepackage.ModuleProperty)
     */
    @Override
    public final void propertyChanged(final ModuleProperty moduleProperty)
            throws ModulePropertyException {
        logger.entering(this.getClass().getName(), "propertyChanged",
                new Object[] { moduleProperty });
        if (moduleProperty.getName().equals("SQL Query")) {
            logger.log(Level.INFO, "Request to set the property: "
                    + moduleProperty.getName());
            if (moduleProperty.getValue() == null) {
                logger.log(Level.WARNING, "The property value is null for: "
                        + moduleProperty.getName());
                logger.exiting(this.getClass().getName(), "propertyChanged");
                // ToDo
            }
            this.SQLQuery = moduleProperty.getValue();
        }
        else if (moduleProperty.getName().equals("JDBC Connection")) {
            logger.log(Level.INFO, "Request to set the property: "
                    + moduleProperty.getName());
            if (moduleProperty.getValue() == null) {
                logger.log(Level.WARNING, "The property value is null for: "
                        + moduleProperty.getName());
                logger.exiting(this.getClass().getName(), "propertyChanged");
                throw new ModulePropertyException(
                        "The property value is null.", this.getName(), this
                                .getId(), moduleProperty.getName(),
                        moduleProperty.getValue());
            }
            this.jdbcConnection = moduleProperty.getValue();
            // reconnect to database here
            dbCommunicator.closeDBConnection();
            dbCommunicator = new DBCommunicator(username, password,
                    jdbcConnection, jdbcDriver);
            logger.log(Level.INFO, "Set the property: "
                    + moduleProperty.getName() + " to " + this.jdbcConnection);
        }
        else if (moduleProperty.getName().equals("Username")) {
            logger.log(Level.INFO, "Request to set the property: "
                    + moduleProperty.getName());
            if (moduleProperty.getValue() == null) {
                logger.log(Level.WARNING, "The property value is null for: "
                        + moduleProperty.getName());
                logger.exiting(this.getClass().getName(), "propertyChanged");
                throw new ModulePropertyException(
                        "The property value is null.", this.getName(), this
                                .getId(), moduleProperty.getName(),
                        moduleProperty.getValue());
            }
            // reconnect to database here
            dbCommunicator.closeDBConnection();
            dbCommunicator = new DBCommunicator(username, password,
                    jdbcConnection, jdbcDriver);
            this.username = moduleProperty.getValue();
        }
        else if (moduleProperty.getName().equals("Password")) {
            logger.log(Level.INFO, "Request to set the property: "
                    + moduleProperty.getName());
            if (moduleProperty.getValue() == null) {
                logger.log(Level.WARNING, "The property value is null for: "
                        + moduleProperty.getName());
                logger.exiting(this.getClass().getName(), "propertyChanged");
                throw new ModulePropertyException(
                        "The property value is null.", this.getName(), this
                                .getId(), moduleProperty.getName(),
                        moduleProperty.getValue());
            }
            // reconnect to database here
            dbCommunicator.closeDBConnection();
            dbCommunicator = new DBCommunicator(username, password,
                    jdbcConnection, jdbcDriver);
            this.password = moduleProperty.getValue();
        }
        else if (moduleProperty.getName().equals("JDBC Driver")) {
            logger.log(Level.INFO, "Request to set the property: "
                    + moduleProperty.getName());
            if (moduleProperty.getValue() == null) {
                logger.log(Level.WARNING, "The property value is null for: "
                        + moduleProperty.getName());
                logger.exiting(this.getClass().getName(), "propertyChanged");
                throw new ModulePropertyException(
                        "The property value is null.", this.getName(), this
                                .getId(), moduleProperty.getName(),
                        moduleProperty.getValue());
            }
            // reconnect to database here
            dbCommunicator.closeDBConnection();
            dbCommunicator = new DBCommunicator(username, password,
                    jdbcConnection, jdbcDriver);
            this.jdbcDriver = moduleProperty.getValue();
        }
        else {
            logger.log(Level.WARNING,
                    "The module does not support a property with the given name: "
                            + moduleProperty.getName());
            logger.exiting(this.getClass().getName(), "propertyChanged");
            throw new ModulePropertyException(
                    "The module does not support this property.", this
                            .getName(), this.getId(), moduleProperty.getName(),
                    moduleProperty.getValue());
        }
        logger.exiting(this.getClass().getName(), "propertyChanged");
    }

    /**
     * @see org.electrocodeogram.module.Module#update() This method is not
     *      implemented in this module, as this module does not need to be
     *      informed about ECG Lab subsystem's state changes.
     */
    public void update() {
        // not implemented
    }
}

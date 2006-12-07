package org.electrocodeogram.module.source.implementation;

import java.sql.SQLException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.rowset.CachedRowSet;
import javax.swing.JPanel;
import org.electrocodeogram.logging.LogHelper;
import org.electrocodeogram.module.UIModule;
import org.electrocodeogram.module.source.EventReader;
import org.electrocodeogram.module.source.SourceModule;
import org.electrocodeogram.module.source.SourceModuleException;
import org.electrocodeogram.module.target.implementation.DBCommunicator;
import org.electrocodeogram.modulepackage.ModuleProperty;
import org.electrocodeogram.modulepackage.ModulePropertyException;

/**
 * @author jule
 * @TODO exception handling
 * The functions of the DatabaseSourceModule are implemented rudimentarily.
 */
public class DatabaseSourceModule extends SourceModule implements UIModule {
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
     * The DBCommunicator to communicate with the database.
     */
    private DBCommunicator dbCommunicator;

    /**
     * the window to query the database
     */
    private QueriesWindow GUI;

    /**
     * This is a reference to this module's <em>EventReader</em>, which is
     * implementing the read method.
     */
    private DatabaseReaderThread readerThread;

    /**
     * This is a reference to this module's <em>EventReader</em>, which reads
     * the Events from the database which are in the ResultSet of the SQL Query
     * giben in the Property SQL Query.
     */
    private DatabaseReaderThread readerThreadPropChanged;

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
        logger.exiting(this.getClass().getName(), "DatabaseTargetModule");
    }

    /**
     * @see org.electrocodeogram.module.Module#initialize()
     */
    @Override
    public void initialize() {
    }

    /**
     * @see org.electrocodeogram.module.source.SourceModule#getEventReader()
     */
    @Override
    public final EventReader[] getEventReader() {
        logger.entering(this.getClass().getName(), "getEventReader");
        logger.exiting(this.getClass().getName(), "getEventReader",
                this.readerThread);
        return new EventReader[] {this.readerThread};
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
        this.dbCommunicator = new DBCommunicator(username, password,
                jdbcConnection, jdbcDriver);
        dbCommunicator.getInformationAndSyncTables(); 
        readerThread = new DatabaseReaderThread(this, this.dbCommunicator);
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
        /**
         * if the SQL Query Property String is given, then execute the statement
         * and create ValidEventPackets from the Events in the ResultSet
         * 
         * There has to be the column linkid for the Events in the Result Set,
         * otherwise the ValidEventPackets cannot be created
         */
        if (moduleProperty.getName().equals("SQL Query")) {
            logger.log(Level.INFO, "Request to set the property: "
                    + moduleProperty.getName());
            if (moduleProperty.getValue() == null) {
                logger.log(Level.WARNING, "The property value is null for: "
                        + moduleProperty.getName());
                logger.exiting(this.getClass().getName(), "propertyChanged");
                // ToDo
            }
            System.out.println("vor set Property");
            this.SQLQuery = moduleProperty.getValue();
            CachedRowSet commonData;
            commonData = DBQueries.executeUserQuery(this.SQLQuery,
                    dbCommunicator);
            Collection eventIDs = null;
            try {
                /**
                 * select all the linkid values for the events
                 */
                eventIDs = commonData.toCollection("linkid");
            }
            catch (SQLException e) {
                logger.severe("was not able to fetch the linkid values "
                        + "for the events");
                e.printStackTrace();
            }
            /**
             * create the ValidEventPacket for each given linkid value
             */
            Object[] eventIDArray = eventIDs.toArray();
            readerThreadPropChanged = new DatabaseReaderThread(this,
                    this.dbCommunicator);
            readerThreadPropChanged.setEventIDs(eventIDArray);
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
            // dbCommunicator.closeDBConnection();
            // dbCommunicator = new DBCommunicator(username, password,
            // jdbcConnection, jdbcDriver);
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
            // dbCommunicator.closeDBConnection();
            // dbCommunicator = new DBCommunicator(username, password,
            // jdbcConnection, jdbcDriver);
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
            // dbCommunicator.closeDBConnection();
            // dbCommunicator = new DBCommunicator(username, password,
            // jdbcConnection, jdbcDriver);
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
            // dbCommunicator.closeDBConnection();
            // dbCommunicator = new DBCommunicator(username, password,
            // jdbcConnection, jdbcDriver);
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

    /*
     * (non-Javadoc)
     * 
     * @see org.electrocodeogram.module.UIModule#getPanel()
     */
    public JPanel getPanel() {
        if (GUI == null) {
            GUI = new QueriesWindow(dbCommunicator, this);
        }
        return GUI;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.electrocodeogram.module.UIModule#getPanelName()
     */
    public String getPanelName() {
        if (GUI == null) {
            GUI = new QueriesWindow(dbCommunicator, this);
        }
        return GUI.getName();
    }
}

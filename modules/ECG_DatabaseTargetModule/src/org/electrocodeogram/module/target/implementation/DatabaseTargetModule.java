package org.electrocodeogram.module.target.implementation;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.electrocodeogram.event.ValidEventPacket;
import org.electrocodeogram.logging.LogHelper;
import org.electrocodeogram.module.target.TargetModule;
import org.electrocodeogram.module.target.TargetModuleException;
import org.electrocodeogram.modulepackage.ModuleProperty;
import org.electrocodeogram.modulepackage.ModulePropertyException;

/**
 * This class is an ECG module used to write ECG events into a file in the file
 * system.
 */
public class DatabaseTargetModule extends TargetModule {

    /**
     * This is the logger.
     */
    private static Logger logger = LogHelper
            .createLogger(DatabaseTargetModule.class.getName());

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
     * The database communicaotr instance for this module.
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
    public DatabaseTargetModule(final String id, final String name) {
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

        /**
         * TODO remove the block comment
         */
        /*
         * try { this.username = this.getModuleProperty("Username").getValue();
         * this.password = this.getModuleProperty("Password").getValue();
         * this.jdbcConnection = this.getModuleProperty("JDBC
         * Connection").getValue(); this.jdbcDriver =
         * this.getModuleProperty("JDBC Driver").getValue(); } catch
         * (ModulePropertyException e) { // TODO Auto-generated catch block
         * e.printStackTrace(); }
         * 
         */

        this.dbCommunicator = new DBCommunicator(username, password,
                jdbcConnection, jdbcDriver);

        logger.exiting(this.getClass().getName(), "DatabaseTargetModule");

    }

    /**
     * @see org.electrocodeogram.module.target.TargetModule#write(org.electrocodeogram.event.ValidEventPacket)
     */
    @Override
    public final void write(final ValidEventPacket packet) {

        logger.entering(this.getClass().getName(), "write",
                new Object[] { packet });

        dbCommunicator.insertEvent(packet);

        logger.log(Level.INFO, "An event has been written to the database "
                + this.jdbcConnection + " by the module " + this.getName());

        logger.exiting(this.getClass().getName(), "write");

    }

    /**
     * @see org.electrocodeogram.module.Module#propertyChanged(org.electrocodeogram.modulepackage.ModuleProperty)
     */
    @Override
    public final void propertyChanged(final ModuleProperty moduleProperty)
            throws ModulePropertyException {

        logger.entering(this.getClass().getName(), "propertyChanged",
                new Object[] { moduleProperty });

        if (moduleProperty.getName().equals("JDBC Connection")) {

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

    /**
     * @see org.electrocodeogram.module.Module#initialize()
     */
    public final void initialize() {
        // not implemented
    }

    /**
     * @see org.electrocodeogram.module.target.TargetModule#startWriter()
     */
    public final void startWriter() throws TargetModuleException {

        logger.entering(this.getClass().getName(), "startWriter");

        // connect to database here

        syncWithDatabase();

        logger.exiting(this.getClass().getName(), "startWriter");

    }

    /**
     * @see org.electrocodeogram.module.target.TargetModule#stopWriter() This
     *      method is not implemented in this module.
     */
    @Override
    public void stopWriter() {

        logger.entering(this.getClass().getName(), "stopWriter");

        // disconnect to database here

        disconnectDatabase();

        logger.exiting(this.getClass().getName(), "stopWriter");

    }

    /**
     * Sychronize the xml schemes with the database tables
     */
    private void syncWithDatabase() {
        dbCommunicator.getInformationAndSyncTables();
    }

    /**
     * disconnect from the database
     */
    private void disconnectDatabase() {
        dbCommunicator.closeDBConnection();

    }

    /**
     * get the DBCommunicator for this Module Instance
     * 
     * @return the dbCommunicator
     */
    public DBCommunicator getDbCommunicator() {
        return dbCommunicator;
    }

}

package org.electrocodeogram.msdt;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The MicroSensorDataType registry is a database for MicroSensorDataTypes.
 * Every ECG module is able to bring in its own MicroSensorDataTypes which are
 * the types of events that the actual module is sending. During hte module
 * registration process the module's MicroSensorDataType definitions are
 * registered with this MsdtRegistry. When a module is removed from workspace
 * and dregestering takes place, the module's MicroSensorDataTypes are also
 * deregestered. A core set of MicroSensorDataTypes are provided by the core
 * modules which are built into the ECG.
 */
public class MsdtRegistry
{

    private HashMap<String, MicroSensorDataType> registeredMsdt = null;

    private Logger logger = null;

    /**
     * This creates the MsdtRegistry object.
     */
    public MsdtRegistry()
    {

        this.logger = Logger.getLogger("MstdManager");

        this.registeredMsdt = new HashMap<String, MicroSensorDataType>();

    }

    /**
     * This method returns an Array of all currently registered
     * MicroSensorDataType XMLSchemas.
     * 
     * @return An Array of all currently registered MicroSensorDataType
     *         XMLSchemas
     */
    public MicroSensorDataType[] getMicroSensorDataTypes()
    {
        return this.registeredMsdt.values().toArray(new MicroSensorDataType[0]);
    }

    /**
     * This method is used by modules to register their MicroSensorDataTypes
     * with this MsdtRegistry.
     * 
     * @param msdt
     *            Is the actual MicroSensorDataType to register
     * @throws MicroSensorDataTypeRegisterException
     *             Is thrown by this method, if the provided MicroSensorDataType
     *             is "null" or if a MicroSensorDataType with the same name is
     *             allready registered with the registry. In this case the
     *             registration of the given MicroSensorDataType is canceled.
     */
    public void registerMsdt(MicroSensorDataType msdt) throws MicroSensorDataTypeRegisterException
    {
        if (msdt == null) {
            throw new MicroSensorDataTypeRegisterException(
                    "The given MicroSensorDataType is \"null\".");
        }

        if (this.registeredMsdt.containsKey(msdt.getName())) {
            throw new MicroSensorDataTypeRegisterException(
                    "A MicroSensorDataType with the name " + msdt.getName() + " is allready registered.");
        }

        this.registeredMsdt.put(msdt.getName(), msdt);

        this.logger.log(Level.INFO, "Registered new MicroSensorDatyType " + msdt.getName());
    }

    /**
     * This method is used by modules to deregister their MicroSensorDataTypes.
     * 
     * @param msdt
     *            Is the actual MicroSensorDataType to deregister
     * @throws MicroSensorDataTypeRegisterException
     *             Is thrown by this method, if the provided MicroSensorDataType
     *             is "null" or if the given MicroSensorDataType is not
     *             registered with the registry.
     */
    public void deregisterMsdt(MicroSensorDataType msdt) throws MicroSensorDataTypeRegisterException
    {
        if (msdt == null) {
            throw new MicroSensorDataTypeRegisterException(
                    "The given MicroSensorDataType is \"null\".");
        }

        if (!this.registeredMsdt.containsKey(msdt.getName())) {
            throw new MicroSensorDataTypeRegisterException(
                    "A MicroSensorDataType with the name " + msdt.getName() + " is not registered.");
        }

        this.registeredMsdt.remove(msdt);

        this.logger.log(Level.INFO, "Deregistered MicroSensorDatyType " + msdt.getName());
    }
}

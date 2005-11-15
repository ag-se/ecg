/*
 * Class: IMsdtRegistry
 * Version: 1.0
 * Date: 18.10.2005
 * By: Frank@Schlesinger.com
 */

package org.electrocodeogram.msdt.registry;

import org.electrocodeogram.msdt.MicroSensorDataType;

/**
 * This interface declares methods that are used by ECG
 * subsystems to access the
 * {@link org.electrocodeogram.msdt.registry.MsdtRegistry}.
 */
public interface IMsdtRegistry {

    /**
     * This method returns an array of all currently
     * registered {@link MicroSensorDataType}.
     * @return An array of all currently registered
     *         {@link MicroSensorDataType}
     */
    MicroSensorDataType[] getMicroSensorDataTypes();

    /**
     * This method is used by modules to deregister their
     * {@link MicroSensorDataType} when the modules are removed.
     * @param msdt
     *            Is the actual <code>MicroSensorDataType</code> to
     *            deregister
     * @throws MicroSensorDataTypeRegistrationException
     *             Is thrown by this method, if the provided MSDT is
     *             <code>null</code> or the module is
     *             <code>null</code>.
     */
    void deregisterMsdt(MicroSensorDataType msdt)
        throws MicroSensorDataTypeRegistrationException;

}

/*
 * Class: IMsdtRegistry
 * Version: 1.0
 * Date: 18.10.2005
 * By: Frank@Schlesinger.com
 */

package org.electrocodeogram.msdt.registry;

import java.io.File;

import org.electrocodeogram.msdt.MicroSensorDataType;
import org.electrocodeogram.msdt.MicroSensorDataTypeException;

/**
 * This <code>Interface</code> declares methods that are used by ECG
 * subsystems to access the
 * {@link org.electrocodeogram.msdt.registry.MsdtRegistry}.
 */
public interface IMsdtRegistry {

    /**
     * This method returns an <code>Array</code> of all currently
     * registered {@link MicroSensorDataType}.
     * @return An <code>Array</code> of all currently registered
     *         {@link MicroSensorDataType}
     */
    MicroSensorDataType[] getMicroSensorDataTypes();

    /**
     * This method is used by modules to deregister their
     * {@link MicroSensorDataType} when the modules are removed.
     * @param msdt
     *            Is the actual <em>MicroSensorDataType</em> to
     *            deregister
     * @throws MicroSensorDataTypeRegistrationException
     *             Is thrown by this method, if the provided MSDT is
     *             <code>null</code> or the module is
     *             <code>null</code>.
     */
    void deregisterMsdt(MicroSensorDataType msdt)
        throws MicroSensorDataTypeRegistrationException;

    /**
     * This method is used to parse a given XML schema file and create
     * a {@link MicroSensorDataType} from it.
     * @param msdtFile
     *            Is the file containing the XML schema
     * @return The <em>MicroSensorDataType</em> defined by the file
     * @throws MicroSensorDataTypeException
     *             If the file does not contain a valid XML schema or
     *             if any other error occures during parsing
     */
    MicroSensorDataType parseMicroSensorDataType(File msdtFile)
        throws MicroSensorDataTypeException;

}

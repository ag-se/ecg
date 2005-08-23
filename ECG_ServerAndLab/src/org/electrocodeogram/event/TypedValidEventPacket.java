/**
 * 
 */
package org.electrocodeogram.event;

import java.util.Date;
import java.util.List;

import org.electrocodeogram.msdt.MicroSensorDataType;

/**
 *
 */
public class TypedValidEventPacket extends ValidEventPacket
{
    
   
    private static final long serialVersionUID = -2907957495470756557L;
   
    private MicroSensorDataType $msdt = null;

    /**
     * @param id
     * @param timeStamp
     * @param sensorDataType
     * @param argList
     * @throws IllegalEventParameterException
     */
    public TypedValidEventPacket(int id, Date timeStamp, String sensorDataType, List argList, MicroSensorDataType msdt) throws IllegalEventParameterException
    {
        super(id, timeStamp, sensorDataType, argList);
     
        this.$msdt = msdt;
    }

    public MicroSensorDataType getMicroSensorDataType()
    {
        return this.$msdt;
    }
    
}

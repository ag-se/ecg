package org.electrocodeogram.client;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.hackystat.kernel.admin.SensorProperties;

public class ECG_SensorProperties extends SensorProperties
{
    
    public ECG_SensorProperties()
    {
        super("ElectroCodeoGram");
    }

    public InetAddress getECGServerAddress() throws UnknownHostException
    {
        String ECGServerAddressKey = "ECG_SERVER_ADDRESS";
        String str = this.getProperty(ECGServerAddressKey).trim();
        return InetAddress.getByName(str);
      
    }

    public int getECGServerPort()
    {
        String ECGServerPortKey = "ECG_SERVER_PORT";
        String str = this.getProperty(ECGServerPortKey).trim();
        
        return Integer.parseInt(str);
    }
    
}

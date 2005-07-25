package org.electrocodeogram.msdt;

import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.electrocodeogram.event.ValidEventPacket;
import org.hackystat.kernel.admin.SensorProperties;
import org.hackystat.kernel.shell.SensorShell;

/**
 * This class provides the functionality to verify that syntactically valid
 * ValidEventPacket objects are according to HackyStat SensorDataTypes and to
 * ECG MicroSensorDataTypes.
 * 
 */
public class EventValidator
{
    private Logger logger = null;

    private int processingID = 0;

    private MsdtManager $mSdtManager = null;

    private SensorShell shell;

    /**
     * This creates a EventValidator object.
     * 
     * @param mSdtManager
     *            Is the MicroSensorDataType-Manager (MsdtManager) object that
     *            maintains the MicroSensorDataType definitions.
     */
    public EventValidator(MsdtManager mSdtManager)
    {
        this.shell = new SensorShell(new SensorProperties("", ""), false,
                "ElectroCodeoGram");

        this.$mSdtManager = mSdtManager;

        this.logger = Logger.getLogger("ECG Server");

    }

    /**
     * This method checks if a given ValidEventPacket object complies to
     * HackyStat and ECG standards.
     * 
     * @param packet
     *            Is the ValidEventPacket object to check
     * @return "true" if the event data is according to a HackyStat
     *         SensorDataType and an ECG MicroSensorDataType
     */
    public boolean validate(ValidEventPacket packet)
    {
        this.processingID++;

        this.logger.log(Level.INFO, this.processingID + ": Begin to process new event data at " + new Date().toString());

        // validate the incoming event data by using the HackyStat framework
        boolean result = this.shell.doCommand(packet.getTimeStamp(), packet.getSensorDataType(), packet.getArglist());

        if (result) {

            this.logger.log(Level.INFO, this.processingID + ": Event data is conforming to a HackyStat SensorDataType and is processed.");

            this.logger.log(Level.INFO, this.processingID + " : " + packet.toString());

            // List<String> newArgList = new ArrayList<String>(argList.size());

            // Object[] entries = argList.toArray();
            //
            // for (int i = 0; i < entries.length; i++) {
            // String entryString = (String) entries[i];
            //
            // if (commandName.equals("Activity") && i == 0) {
            // entryString = "" + entryString;
            //                     
            // }
            //
            // newArgList.add(entryString);
            // }
            //
            // isMsdt(newArgList);

            return true;
        }

        this.logger.log(Level.INFO, this.processingID + ": Event data is not conforming to a HackyStat SensorDataType and is discarded.");

        this.logger.log(Level.INFO, this.processingID + ":" + packet.toString());

        return false;
    }

    private void isMsdt(List<String> argList)
    {

        String mSdtName = argList.get(1);

        if (mSdtName == null || mSdtName == "") {
            this.logger.log(Level.WARNING, this.processingID + ": Event data is not a known ECG MicroSensorDataType: " + mSdtName);

            return;
        }

        MicroSensorDataType microSensorDataType = null;

        try {

            if (this.$mSdtManager == null) {
                this.logger.log(Level.WARNING, this.processingID + ": Event data is not conforming to a ECG MicroSensorDataType. " + mSdtName);

                return;
            }

            microSensorDataType = this.$mSdtManager.getMicroSensorDataType(mSdtName);

        }
        catch (MicroSensorDataTypeNotFoundException e) {

            this.logger.log(Level.WARNING, this.processingID + ": Event data is not conforming to a ECG MicroSensorDataType. " + mSdtName);

            return;
        }

        String[] entryAttriuteNames = microSensorDataType.getEntryAttributeNames();

        String data = argList.get(2);

        if (entryAttriuteNames == null && data != null) {
            this.logger.log(Level.WARNING, this.processingID + ": Event data is not conforming to a ECG MicroSensorDataType. " + mSdtName);

            return;
        }

        if (entryAttriuteNames == null && data == null) {
            this.logger.log(Level.WARNING, this.processingID + ": Event data is not conforming to a ECG MicroSensorDataType. " + mSdtName);

            return;
        }

        StringTokenizer tokenizer = new StringTokenizer(data, "#");

        if(tokenizer == null)
        {
            this.logger.log(Level.WARNING, this.processingID + ": Event data is not conforming to a ECG MicroSensorDataType. " + mSdtName);

            return;
        }
        
        if (tokenizer.countTokens() != entryAttriuteNames.length) {
            this.logger.log(Level.WARNING, this.processingID + ": Event data is not conforming to a ECG MicroSensorDataType. " + mSdtName);

            return;
        }

        this.logger.log(Level.WARNING, this.processingID + ": Event data is conforming to the ECG MicroSensorDataType: " + microSensorDataType.getName());

        return;

    }

}
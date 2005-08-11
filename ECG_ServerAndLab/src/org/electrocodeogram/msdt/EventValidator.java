package org.electrocodeogram.msdt;

import java.io.IOException;
import java.io.StringReader;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.transform.sax.SAXSource;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;

import org.electrocodeogram.event.ValidEventPacket;
import org.hackystat.kernel.admin.SensorProperties;
import org.hackystat.kernel.shell.SensorShell;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

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
    
    private boolean allowNonHackyStatSDTConformEvents = false;
    
    private boolean allowNonECGmSDTConformEvents = false;

    /**
     * This creates a EventValidator object.
     * 
     * @param mSdtManager
     *            Is the MicroSensorDataType-Manager (MsdtManager) object that
     *            maintains the MicroSensorDataType XML schema definitions which are used
     *            to validate the MicroActivities against.
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
     * HackyStat and ECG standards. Checking is done in a sequence from
     * the weakest condition to the strongest.
     * First the event data is checked by the HackyStat SensorShell component
     * for comliance to a HackyStat SensorDataType.
     * If positive the event data is checked to be a HackyStat "Activity"
     * event.
     * If positive the event data is chekced to be an ECG "MicroActivity"
     * event.
     * At last the event data is checked for compliance to a ECG MicroSensorDataType.
     * Only if the last stage checking is positive this method returns "true".
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

        if(this.allowNonHackyStatSDTConformEvents) return true;
            
        /*
         * Is the incoming event according to a HackyStat SensorDataType?
         */
        boolean isHackyStatSensorDataTypeConform = this.shell.doCommand(packet.getTimeStamp(), packet.getSensorDataType(), packet.getArglist());

        if (isHackyStatSensorDataTypeConform) {

            this.logger.log(Level.INFO, this.processingID + ": Event data is conforming to a HackyStat SensorDataType and is processed.");

            this.logger.log(Level.INFO, this.processingID + " : " + packet.toString());

            if(this.allowNonECGmSDTConformEvents) return true;
            
            if(isActivityEvent(packet))
            {
                if(isMicroActivityEvent(packet))
                {
                    if(isMicroSensorDataType(packet))
                    {
                        return true;
                    }
                }
            }
          
        }

     
        return false;
    }

    /**
     * This method checks if an ValidEventPacket is containing a ECG "MicroActivity" event.
     * @param packet Is the ValidEventPacket to check
     * @return "true" if the packet is a "MicroActivity" event and "false" if not
     */
    private boolean isMicroActivityEvent(ValidEventPacket packet)
    {
        if (packet == null)
            return false;

        if (packet.getArglist().get(1).equals("MicroActivity")) {
            this.logger.log(Level.INFO, this.processingID + ": The event is a ECG \"MicroActivity\" event.");
            return true;
        }

        this.logger.log(Level.INFO, this.processingID + ": The event is not a ECG \"MicroActivity\" event.");
        return false;
    }

    /**
     * This method checks if an ValidEventPacket is containing a HackyStat "Activity" event.
     * @param packet Is the ValidEventPacket to check
     * @return "true" if the packet is an "Activity" event and "false" if not
     */
    private boolean isActivityEvent(ValidEventPacket packet)
    {
        
        if (packet == null) {
            return false;
        }

        if (packet.getSensorDataType().equals("Activity")) {
            this.logger.log(Level.INFO, this.processingID + ": The event is a HackyStat \"Activity\" event.");
            return true;
        }

        this.logger.log(Level.INFO, this.processingID + ": The event is not a HackyStat \"Activity\" event.");
        return false;
    }

    private boolean isMicroSensorDataType(ValidEventPacket packet)
    {

        List argList = packet.getArglist();
        
        String microActivityString = (String) argList.get(2);

        if (microActivityString == null || microActivityString.equals("")) {

            this.logger.log(Level.INFO, this.processingID + ": No MicroActivity data found.");

            this.logger.log(Level.INFO, this.processingID + ": Event data is not conforming to a HackyStat SensorDataType and is discarded.");

            this.logger.log(Level.INFO, this.processingID + ":" + packet.toString());

            
            return false;
        }

        String[] schemaNames = this.$mSdtManager.getMstdSchemaNames();

        if (schemaNames.length == 0) {

            this.logger.log(Level.INFO, this.processingID + ": No MicroSensorDataTypes are found.");

            this.logger.log(Level.INFO, this.processingID + ": Event data is not conforming to a HackyStat SensorDataType and is discarded.");

            this.logger.log(Level.INFO, this.processingID + ":" + packet.toString());

            
            return false;
        }

        for (int i = 0; i < schemaNames.length; i++) {
        
            SAXSource saxSource = new SAXSource(new InputSource(new StringReader(
                    microActivityString)));
            
            Schema schema = this.$mSdtManager.getSchemaForName(schemaNames[i]);

            Validator validator = schema.newValidator();

            try {

                this.logger.log(Level.INFO, "Validating MicroActivity against " + schemaNames[i] + " XML schema.");

                validator.validate(saxSource);

                this.logger.log(Level.INFO, "The MicroActivity is a valid " + schemaNames[i] + " event.");

                return true;
            }
            catch (SAXException e) {

                this.logger.log(Level.INFO, "The MicroActivity event is not a valid " + schemaNames[i] + " event.");

                this.logger.log(Level.INFO, e.getMessage());

            }
            catch (IOException e) {

                this.logger.log(Level.INFO, "The MicroActivity event could not been read.");

            }

        }

        return false;
    }

    
    /**
     * This method tells wheter event data that does not conform to
     * a ECG MicroSensorDataType is allowed to pass validation or not.
     * @return "true" if event data that does not conform to
     * a ECG MicroSensorDataType is allowed and "false" if not
     */
    public boolean areNonECGmSDTConformEventsAllowed()
    {
        return this.allowNonECGmSDTConformEvents;
    }
    
    /**
     * This method is used to decalare whether event data that does not conform to
     * a ECG MicroSensorDataType is allowed to pass validation.
     * A value of "false" is ignored if the value for allowing non HackyStat conform
     * event data is set to "true".
     * @param allowNonECGmSDTConformEvents Is "true" if event data that does not conform to
     * a ECG MicroSensorDataType is allowed and "false" if not
     */

    public void setAllowNonECGmSDTConformEvents(boolean allowNonECGmSDTConformEvents)
    {
        this.allowNonECGmSDTConformEvents = allowNonECGmSDTConformEvents;
    }
    
    /**
     * This method tells wheter event data that does not conform to
     * a HackyStat SensorDataType is allowed to pass validation or not.
     * @return "true" if event data that does not conform to
     * a HackyStat SensorDataType is allowed and "false" if not
     */

    public boolean areNonHackyStatSDTConformEventsAllowed()
    {
        return this.allowNonHackyStatSDTConformEvents;
    }
    
    /**
     * This method is used to declare whether event data that does not conform to
     * a HackyStat SensorDataType is allowed to pass validation.
     * @param allowNonHackyStatSDTConformEvents Is "true" if event data that does not conform to
     * a HackyStat SensorDataType is allowed and "false" if not
     */

    public void setAllowNonHackyStatSDTConformEvents(boolean allowNonHackyStatSDTConformEvents)
    {
        this.allowNonHackyStatSDTConformEvents = allowNonHackyStatSDTConformEvents;
    }

}
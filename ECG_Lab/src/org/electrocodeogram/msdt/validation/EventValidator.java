package org.electrocodeogram.msdt.validation;

import java.io.IOException;
import java.io.StringReader;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.transform.sax.SAXSource;
import javax.xml.validation.Validator;

import org.electrocodeogram.event.IllegalEventParameterException;
import org.electrocodeogram.event.TypedValidEventPacket;
import org.electrocodeogram.event.ValidEventPacket;
import org.electrocodeogram.msdt.MicroSensorDataType;
import org.electrocodeogram.msdt.registry.ISystemMsdtRegistry;
import org.hackystat.kernel.admin.SensorProperties;
import org.hackystat.kernel.shell.OfflineManagerClearer;
import org.hackystat.kernel.shell.SensorShell;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * This class provides the functionality to verify that syntactically valid
 * ValidEventPacket objects are according to HackyStat SensorDataTypes and to
 * ECG MicroSensorDataTypes.
 */
public class EventValidator
{
	private Logger logger = null;

	private int processingID = 0;

	private ISystemMsdtRegistry $mSdtManager = null;

	private SensorShell shell;

	private boolean $allowNonHackyStatSDTConformEvents = false;

	private boolean $allowNonECGmSDTConformEvents = false;
	
	private int count = 0;

	/**
	 * This creates a EventValidator object.
	 * 
	 * @param mSdtManager
	 *            Is the MicroSensorDataType-Manager (MsdtManager) object that
	 *            keeps the MicroSensorDataType XML schema definitions which are used
	 *            to validate the MicroActivities against.
	 */
	public EventValidator(ISystemMsdtRegistry mSdtManager)
	{
		this.shell = new SensorShell(new SensorProperties("", ""), false,
				"ElectroCodeoGram",false);

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
	public TypedValidEventPacket validate(ValidEventPacket packet)
	{
		//this.processingID++;

		//this.logger.log(Level.INFO, this.processingID + ": Begin to process new event data at " + new Date().toString());

		if (this.$allowNonHackyStatSDTConformEvents)
		{
			try
			{
				return new TypedValidEventPacket(-1, packet.getTimeStamp(),
						packet.getSensorDataType(), packet.getArglist(), null);
			}
			catch (IllegalEventParameterException e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		/*
		 * Is the incoming event according to a HackyStat SensorDataType?
		 */
		boolean isHackyStatSensorDataTypeConform = this.shell.doCommand(packet.getTimeStamp(), packet.getSensorDataType(), packet.getArglist());
		
		this.count++;
		
		if(this.count == 100)
		{
			this.shell = new SensorShell(new SensorProperties("", ""), false,
					"ElectroCodeoGram",false);
			
			OfflineManagerClearer.clearOfflineManager();
			
			System.gc();
			
			this.count = 0;
		}
		
		//boolean isHackyStatSensorDataTypeConform = true;

		if (isHackyStatSensorDataTypeConform)
		{

			this.logger.log(Level.INFO, this.processingID + ": Event data is conforming to a HackyStat SensorDataType and is processed.");

			this.logger.log(Level.INFO, this.processingID + " : " + packet.toString());

			if (this.$allowNonECGmSDTConformEvents)
			{
				try
				{
					return new TypedValidEventPacket(0, packet.getTimeStamp(),
							packet.getSensorDataType(), packet.getArglist(),
							null);
				}
				catch (IllegalEventParameterException e)
				{
					return null;
				}
			}

			if (isActivityEvent(packet))
			{
				if (isMicroActivityEvent(packet))
				{
					return isMicroSensorDataType(packet);
				}
			}
			
		}
		
		return null;
	}

	/**
	 * This method checks if an ValidEventPacket is containing a ECG "MicroActivity" event.
	 * @param packet Is the ValidEventPacket to check
	 * @return "true" if the packet is a "MicroActivity" event and "false" if not
	 */
	private boolean isMicroActivityEvent(ValidEventPacket packet)
	{
		if (packet == null) return false;

		if (packet.getArglist().get(1).equals("MicroActivity"))
		{
			this.logger.log(Level.INFO, this.processingID + ": The event is an ECG \"MicroActivity\" event.");
			return true;
		}

		this.logger.log(Level.INFO, this.processingID + ": The event is not an ECG \"MicroActivity\" event.");
		return false;
	}

	/**
	 * This method checks if an ValidEventPacket is containing a HackyStat "Activity" event.
	 * @param packet Is the ValidEventPacket to check
	 * @return "true" if the packet is an "Activity" event and "false" if not
	 */
	private boolean isActivityEvent(ValidEventPacket packet)
	{

		if (packet == null)
		{
			return false;
		}

		if (packet.getSensorDataType().equals("Activity"))
		{
			this.logger.log(Level.INFO, this.processingID + ": The event is a HackyStat \"Activity\" event.");
			return true;
		}

		this.logger.log(Level.INFO, this.processingID + ": The event is not a HackyStat \"Activity\" event.");
		return false;
	}

	private TypedValidEventPacket isMicroSensorDataType(ValidEventPacket packet)
	{

		List argList = packet.getArglist();

		String microActivityString = (String) argList.get(2);

		if (microActivityString == null || microActivityString.equals(""))
		{

			this.logger.log(Level.INFO, this.processingID + ": No MicroActivity data found.");

			this.logger.log(Level.INFO, this.processingID + ": Event data is not conforming to a HackyStat SensorDataType and is discarded.");

			this.logger.log(Level.INFO, this.processingID + ":" + packet.toString());

			return null;
		}

		MicroSensorDataType[] microSensorDataTypes = this.$mSdtManager.getMicroSensorDataTypes();

		if (microSensorDataTypes.length == 0)
		{

			this.logger.log(Level.INFO, this.processingID + ": No MicroSensorDataTypes are found.");

			this.logger.log(Level.INFO, this.processingID + ": Event data is not conforming to a HackyStat SensorDataType and is discarded.");

			this.logger.log(Level.INFO, this.processingID + ":" + packet.toString());

			return null;
		}

		for (int i = 0; i < microSensorDataTypes.length; i++)
		{

			SAXSource saxSource = new SAXSource(new InputSource(
					new StringReader(microActivityString)));

			Validator validator = microSensorDataTypes[i].getSchema().newValidator();

			try
			{

				//this.logger.log(Level.INFO, "Validating MicroActivity against " + microSensorDataTypes[i].getName() + " XML schema.");

				validator.validate(saxSource);

				this.logger.log(Level.INFO, "The MicroActivity is a valid " + microSensorDataTypes[i].getName() + " event.");

				TypedValidEventPacket typedValidEventPacket = null;

				try
				{
					typedValidEventPacket = new TypedValidEventPacket(0,
							packet.getTimeStamp(), packet.getSensorDataType(),
							packet.getArglist(), microSensorDataTypes[i]);
				}
				catch (IllegalEventParameterException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				return typedValidEventPacket;
			}
			catch (SAXException e)
			{

				// ignore this

			}
			catch (IOException e)
			{

				this.logger.log(Level.INFO, "The MicroActivity event could not been read.");

			}

		}

		this.logger.log(Level.INFO, "The MicroActivity is not conforming to a known MicroSensorDataType.");

		return null;
	}

	/**
	 * This method tells wheter event data that does not conform to
	 * a ECG MicroSensorDataType is allowed to pass validation or not.
	 * @return "true" if event data that does not conform to
	 * a ECG MicroSensorDataType is allowed and "false" if not
	 */
	public boolean areNonECGmSDTConformEventsAllowed()
	{
		return this.$allowNonECGmSDTConformEvents;
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
		this.$allowNonECGmSDTConformEvents = allowNonECGmSDTConformEvents;
	}

	/**
	 * This method tells wheter event data that does not conform to
	 * a HackyStat SensorDataType is allowed to pass validation or not.
	 * @return "true" if event data that does not conform to
	 * a HackyStat SensorDataType is allowed and "false" if not
	 */

	public boolean areNonHackyStatSDTConformEventsAllowed()
	{
		return this.$allowNonHackyStatSDTConformEvents;
	}

	/**
	 * This method is used to declare whether event data that does not conform to
	 * a HackyStat SensorDataType is allowed to pass validation.
	 * @param allowNonHackyStatSDTConformEvents Is "true" if event data that does not conform to
	 * a HackyStat SensorDataType is allowed and "false" if not
	 */

	public void setAllowNonHackyStatSDTConformEvents(boolean allowNonHackyStatSDTConformEvents)
	{
		this.$allowNonHackyStatSDTConformEvents = allowNonHackyStatSDTConformEvents;
	}

}
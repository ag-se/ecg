package org.electrocodeogram.module.intermediate.implementation.recognizers;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.electrocodeogram.event.IllegalEventParameterException;
import org.electrocodeogram.event.ValidEventPacket;
import org.electrocodeogram.event.WellFormedEventPacket;
import org.electrocodeogram.logging.LogHelper;
import org.electrocodeogram.misc.xml.ECGParser;
import org.electrocodeogram.misc.xml.NodeException;
import org.electrocodeogram.module.intermediate.implementation.EpisodeRecognizer;
import org.electrocodeogram.module.intermediate.implementation.EpisodeRecognizerIntermediateModule;
import org.w3c.dom.Document;

public class PartActiveEpisodeRecognizer implements EpisodeRecognizer {

    private enum PartActiveEpisodeState {START, PARTACTIVE, PARTHOLD, STOP}
	
	// Eigentlich ist dies ein kleiner Unterautomat. Die Zustandsmenge oben müsste man verdoppeln!
	private boolean hasBeenChanged = false;
	
	private static Logger logger = LogHelper
    .createLogger(EpisodeRecognizerIntermediateModule.class.getName());

    private static String[] generalParts = new String[] {
            "Hierarchy",
            "Search",
            "Console",
            "Ant",
            "Outline",
            "Navigator",
            "Package Explorer",
            "Problems"
            };
    
    private PartActiveEpisodeState state;
	
	private String activePartName = null;
	
	private Date startDate = null;
	
	public static DateFormat dateFormat = DateFormat.getDateTimeInstance(
	        DateFormat.MEDIUM, DateFormat.MEDIUM);
		
	public PartActiveEpisodeRecognizer() {
		state = PartActiveEpisodeState.START;
	}
	
	public boolean isInInitialState() {
		return state == PartActiveEpisodeState.START;
	}
	
	public boolean isInFinalState() {
		return state == PartActiveEpisodeState.STOP;
	}
	
    public ValidEventPacket analyse(ValidEventPacket packet, int id, long minDuration) {

		ValidEventPacket event = null;

		String msdt = packet.getMicroSensorDataType().getName();

		try {

			if (msdt.equals("msdt.part.xsd") ||
                msdt.equals("msdt.window.xsd")) {

				Document document = packet.getDocument();
				Date timestamp = packet.getTimeStamp();
				
                if (msdt.equals("msdt.part.xsd")) {
    				String activity = ECGParser.getSingleNodeValue("activity", document);
    				String partname = ECGParser.getSingleNodeValue("partname", document);
    				
    				if (state == PartActiveEpisodeState.START && activity.equals("activated")) {
    					state = PartActiveEpisodeState.PARTACTIVE;
    					activePartName = partname;
    					startDate = timestamp;
    				}
    				else if (state == PartActiveEpisodeState.PARTACTIVE && 
    						activity.equals("deactivated") && 
    						this.isSamePart(partname, activePartName)) {
                            event = generateEpisode(id, minDuration, "msdt.partactive.xsd", 
    							ECGParser.getSingleNodeValue("username", document),
    							ECGParser.getSingleNodeValue("projectname", document),
    							timestamp,
    							startDate,
    							activePartName);
    					activePartName = null;
    					startDate = null;
    					state = PartActiveEpisodeState.STOP;
    				}
                    
                } else if (msdt.equals("msdt.window.xsd")) {
    
                    String activity = ECGParser.getSingleNodeValue("activity", document);
                    
                    if (state == PartActiveEpisodeState.PARTACTIVE && activity.equals("deactivated")) {
                        state = PartActiveEpisodeState.PARTHOLD;
                        event = generateEpisode(id, minDuration, "msdt.partactive.xsd", 
                                ECGParser.getSingleNodeValue("username", document),
                                ECGParser.getSingleNodeValue("projectname", document),
                                timestamp,
                                startDate,
                                activePartName);
                        startDate = null;
                    }
                    else if (state == PartActiveEpisodeState.PARTHOLD && activity.equals("activated")) {
                        state = PartActiveEpisodeState.PARTACTIVE;
                        startDate = timestamp; 
                    }
                }
			}

		} catch (NodeException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

        return event;

    }
	
	private boolean isSamePart(String partname1, String partname2) {
        // TODO Auto-generated method stub

        for (int i = 0; i < generalParts.length; i++) {
            if (partname1.startsWith(generalParts[i]))
                return partname2.startsWith(generalParts[i]);            
        }
        return partname1.equals(partname2);
    }

    public String toString() {
		return activePartName + " in state " + state;
	}

	private ValidEventPacket generateEpisode(int id, long minDur, String msdt, 
				String username, String projectname, Date end, 
				Date begin, String partname) {

		ValidEventPacket event = null;
		long duration = end.getTime() - begin.getTime();
		
		if (duration < minDur)
			return null;
/*		
        for (int i = 0; i < generalParts.length; i++) {
            if (partname.startsWith(generalParts[i])) {
                partname = generalParts[i];
                break;
            }
        }
*/        
        String data = "<?xml version=\"1.0\"?><microActivity>";
		
		data += "<username>"  + username  + "</username>";

		data += "<projectname>" + projectname + "</projectname>";

		data += "<endtime>" + this.dateFormat.format(end) + "</endtime>";

		data += "<duration>" + duration + "</duration>";

		data += "<resourcename>" + partname + "</resourcename>";

		data += "</microActivity>";

        logger.log(Level.FINE, "PARTACTIVE event created");
        logger.log(Level.FINE, data);

        String[] args = {WellFormedEventPacket.HACKYSTAT_ADD_COMMAND, msdt,
            data};

        try {
            event = new ValidEventPacket(id, begin,
                WellFormedEventPacket.HACKYSTAT_ACTIVITY_STRING, Arrays
                    .asList(args));

        } catch (IllegalEventParameterException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
		
		return event;

	}
	

}

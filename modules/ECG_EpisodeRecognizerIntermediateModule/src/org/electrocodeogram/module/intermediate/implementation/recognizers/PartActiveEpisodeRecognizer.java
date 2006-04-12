/*
 * Freie Universität Berlin, 2006
 *
 */
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

/**
 * Recognizes episodes of individual activity durations (start, end) of a view
 */
public class PartActiveEpisodeRecognizer implements EpisodeRecognizer {

    /**
     * State types for this episode recognizer
     *
     */
    private enum PartActiveEpisodeState {
        /**
         * Initial State
         */
        START, 
        /**
         * State when view has been activated
         */
        PARTACTIVE, 
        /**
         * State when view has been deactivated because the window has been deactivated
         */
        PARTHOLD, 
        /**
         * Final state, taken after deactivation
         */
        STOP
    }
	
    /**
     * TODO: change to general static format 
     */
    public static DateFormat dateFormat = DateFormat.getDateTimeInstance(
            DateFormat.MEDIUM, DateFormat.MEDIUM);
        
	/**
	 * General Logger 
	 */
	private static Logger logger = LogHelper
    .createLogger(EpisodeRecognizerIntermediateModule.class.getName());

    /**
     * Set of String which serve as prefix for view titles. Each view which start with one
     * of these strings are considered equal to others with the same prefix
     */
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
    
    /**
     * Current state of episode recognizer
     */
    private PartActiveEpisodeState state;
	
	/**
	 * Current name of view instate active
	 */
	private String activePartName = null;
	
	/**
	 * Time stamp of when recognizer got in state partactive the first time
	 */
	private Date startDate = null;
	
	/**
	 * Constructor to start recognizer in initial state 
	 */
	public PartActiveEpisodeRecognizer() {
		state = PartActiveEpisodeState.START;
	}
	
	/* (non-Javadoc)
	 * @see org.electrocodeogram.module.intermediate.implementation.EpisodeRecognizer#isInInitialState()
	 */
	public boolean isInInitialState() {
		return state == PartActiveEpisodeState.START;
	}
	
	/* (non-Javadoc)
	 * @see org.electrocodeogram.module.intermediate.implementation.EpisodeRecognizer#isInFinalState()
	 */
	public boolean isInFinalState() {
		return state == PartActiveEpisodeState.STOP;
	}
	
    /* (non-Javadoc)
     * @see org.electrocodeogram.module.intermediate.implementation.EpisodeRecognizer#analyse(org.electrocodeogram.event.ValidEventPacket, int, long)
     */
    public ValidEventPacket analyse(ValidEventPacket packet, int id, long minDuration) {

		ValidEventPacket event = null;

		String msdt = packet.getMicroSensorDataType().getName();

		try {

			if (msdt.equals("msdt.part.xsd") ||
                msdt.equals("msdt.editor.xsd") ||
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
                            // normal deactivate of this view
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
                    else if (state == PartActiveEpisodeState.PARTACTIVE && 
                                (activity.equals("activated") || activity.equals("opened")) && 
                                !this.isSamePart(partname, activePartName)) {
                            // activation of another view = more reliable to tell that this one is deactivated
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
                    else if ((state == PartActiveEpisodeState.PARTHOLD) && 
                            activity.equals("deactivated") && 
                            this.isSamePart(partname, activePartName)) {
                            // View has been silently deactivated, discard it
                            activePartName = null;
                            startDate = null;
                            state = PartActiveEpisodeState.STOP;
                        }

                } else if (msdt.equals("msdt.editor.xsd")) {

                    String activity = ECGParser.getSingleNodeValue("activity", document);
                    
                    if (state == PartActiveEpisodeState.PARTACTIVE &&
                            (activity.equals("activated") || activity.equals("opened"))) {
                        // activation of editor means deactivation of this part
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
	
	/**
	 * @param partname1
	 * @param partname2
	 * @return
	 */
	private boolean isSamePart(String partname1, String partname2) {
        // TODO Auto-generated method stub

        for (int i = 0; i < generalParts.length; i++) {
            if (partname1.startsWith(generalParts[i]))
                return partname2.startsWith(generalParts[i]);            
        }
        return partname1.equals(partname2);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
		return activePartName + " in state " + state;
	}

	/**
     * Helper method to generate XML string for episode event
     * 
	 * @param id
	 * @param minDur
	 * @param msdt
	 * @param username
	 * @param projectname
	 * @param end
	 * @param begin
	 * @param partname
	 * @return
	 */
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

		data += "<endtime>" + dateFormat.format(end) + "</endtime>";

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
    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        // TODO Auto-generated method stub
        if((obj == null) || (obj.getClass() != this.getClass())) return false;
        if(obj == this) return true;
        PartActiveEpisodeRecognizer partActiveRecog = (PartActiveEpisodeRecognizer)obj;
//        if (partActiveRecog.getState().equals(this.state))
            if (partActiveRecog.getPartName() != null && this.activePartName != null &&
                isSamePart(partActiveRecog.getPartName(), this.activePartName))
                return true;
        return false;
    }

    /**
     * @return name of current activated view
     */
    private String getPartName() {
        // TODO Auto-generated method stub
        return activePartName;
    }

    /**
     * @return state of episode recognizer
     */
    private PartActiveEpisodeState getState() {
        // TODO Auto-generated method stub
        return state;
    }

	

}

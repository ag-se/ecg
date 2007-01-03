/*
 * Freie Universität Berlin, 2006
 *
 */
package org.electrocodeogram.module.intermediate.implementation.recognizers;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.electrocodeogram.event.ValidEventPacket;
import org.electrocodeogram.logging.LogHelper;
import org.electrocodeogram.misc.xml.ECGParser;
import org.electrocodeogram.misc.xml.ECGWriter;
import org.electrocodeogram.misc.xml.NodeException;
import org.electrocodeogram.module.intermediate.implementation.AbstractSingleEpisodeRecognizer;
import org.electrocodeogram.module.intermediate.implementation.EpisodeRecognizerIntermediateModule;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Recognizes episodes of individual activity durations (start, end) of a view
 */
public class PartActiveEpisodeRecognizer  extends AbstractSingleEpisodeRecognizer {

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
	
    // XML Document and Elements
    private static Document msdt_partactive_doc = null;
    private static Element partactive_username = null;
    private static Element partactive_projectname = null;
    private static Element partactive_endtime = null;
    private static Element partactive_duration = null;
    private static Element partactive_resourcename = null;

    /**
	 * Constructor to start recognizer in initial state 
	 */
	public PartActiveEpisodeRecognizer() {

        // Start automata in start state
        state = PartActiveEpisodeState.START;

        // initialize static DOM skeleton for msdt.editor.xsd
        if (msdt_partactive_doc == null)
            try {
                msdt_partactive_doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
                Element partactive_microactivity = msdt_partactive_doc.createElement("microActivity");                
                partactive_username = msdt_partactive_doc.createElement("username");
                partactive_projectname = msdt_partactive_doc.createElement("projectname");
                partactive_endtime = msdt_partactive_doc.createElement("endtime");
                partactive_duration = msdt_partactive_doc.createElement("duration");
                partactive_resourcename = msdt_partactive_doc.createElement("resourcename");
        
                msdt_partactive_doc.appendChild(partactive_microactivity);
                partactive_microactivity.appendChild(partactive_username);
                partactive_microactivity.appendChild(partactive_projectname);
                partactive_microactivity.appendChild(partactive_endtime);
                partactive_microactivity.appendChild(partactive_duration);
                partactive_microactivity.appendChild(partactive_resourcename);
            } catch (ParserConfigurationException e) {
                logger.log(Level.SEVERE, "Could not instantiate the DOM Document in PartActiveEpisodeRecognizer.");
                logger.log(Level.FINE, e.getMessage());
            }

	}
	
	/**
	 * @see org.electrocodeogram.module.intermediate.implementation.EpisodeRecognizer#isInInitialState()
	 */
	public boolean isInInitialState() {
		return state == PartActiveEpisodeState.START;
	}
	
	/**
	 * @see org.electrocodeogram.module.intermediate.implementation.EpisodeRecognizer#isInFinalState()
	 */
	public boolean isInFinalState() {
		return state == PartActiveEpisodeState.STOP;
	}
	
    /**
     * @see org.electrocodeogram.module.intermediate.implementation.EpisodeRecognizer#analyse(org.electrocodeogram.event.ValidEventPacket, int, long)
     */
    public ValidEventPacket analyseSingle(ValidEventPacket packet, long minDuration) {

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
//System.out.println(timestamp + " / " + activity + " " + partname + " / " + packet.toString()); 
    				
    				if (state == PartActiveEpisodeState.START && activity.equals("activated")) {
    					state = PartActiveEpisodeState.PARTACTIVE;
                        activePartName = partname;
    					startDate = timestamp;
    				}
    				else if (state == PartActiveEpisodeState.PARTACTIVE && 
        						activity.equals("deactivated") && 
        						this.isSamePart(partname, activePartName)) {
                            // normal deactivate of this view
                            event = generateEpisode(minDuration, "msdt.partactive.xsd", 
    							ECGParser.getSingleNodeValue("username", document),
    							ECGParser.getSingleNodeValueIfAvailable("projectname", document),
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
                            event = generateEpisode(minDuration, "msdt.partactive.xsd", 
                                ECGParser.getSingleNodeValue("username", document),
                                ECGParser.getSingleNodeValueIfAvailable("projectname", document),
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
                        event = generateEpisode(minDuration, "msdt.partactive.xsd", 
                                ECGParser.getSingleNodeValue("username", document),
                                ECGParser.getSingleNodeValueIfAvailable("projectname", document),
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
                        event = generateEpisode(minDuration, "msdt.partactive.xsd", 
                                ECGParser.getSingleNodeValue("username", document),
                                ECGParser.getSingleNodeValueIfAvailable("projectname", document),
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

		} catch (NodeException e) {
            logger.log(Level.SEVERE, "Could not read XML string in PartActiveEpisodeRecognizer.");
            logger.log(Level.FINE, e.getMessage());
		}

        return event;

    }
	
	/**
	 * @param partname1
	 * @param partname2
	 * @return
	 */
	private boolean isSamePart(String partname1, String partname2) {

        for (int i = 0; i < generalParts.length; i++) {
            if (partname1.startsWith(generalParts[i]))
                return partname2.startsWith(generalParts[i]);            
        }
        return partname1.equals(partname2);
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
		return activePartName + " in state " + state;
	}

	/**
     * Helper method to generate ValidEventPacket (including XML string) for episode event
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
	private ValidEventPacket generateEpisode(long minDur, String msdt, 
				String username, String projectname, Date end, 
				Date begin, String partname) {

		ValidEventPacket event = null;
		long duration = end.getTime() - begin.getTime();
		
		if (duration < minDur)
			return null;
        
        /* This is for generalizing the part names
        for (int i = 0; i < generalParts.length; i++) {
            if (partname.startsWith(generalParts[i])) {
                partname = generalParts[i];
                break;
            }
        }
        */

        partactive_projectname.setTextContent(projectname);
        partactive_username.setTextContent(username);
        partactive_duration.setTextContent(String.valueOf(duration));
        partactive_endtime.setTextContent(ECGWriter.formatDate(end));
        partactive_resourcename.setTextContent(partname);

        event = ECGWriter.createValidEventPacket("msdt.partactive.xsd", begin, msdt_partactive_doc);

        /*
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
            event = new ValidEventPacket(begin,
                WellFormedEventPacket.HACKYSTAT_ACTIVITY_STRING, Arrays
                    .asList(args));

        } catch (IllegalEventParameterException e) {
            e.printStackTrace();
        }
         */
		
		return event;

	}
    
    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {

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
        return activePartName;
    }


}

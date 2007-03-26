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
 * Recognizes episodes of individual activity durations (start, end) of the main window
 */
public class WindowActiveEpisodeRecognizer  extends AbstractSingleEpisodeRecognizer {

    /**
     * State types for this recognizer 
     */
    public enum WindowActiveEpisodeState {
        /**
         * Ínitial state, no window recognized yet
         */
        START, 
        
        /**
         * Window has been activated 
         */
        WINDOWACTIVE, 
        
        /**
         * Final state after window deactivate
         */
        STOP
    }
	
	/**
	 * General logger for this class 
	 */
	private static Logger logger = LogHelper
    .createLogger(EpisodeRecognizerIntermediateModule.class.getName());

    // XML Document and Elements
    private Document msdt_windowactive_doc = null;
    private Element windowactive_username = null;
    private Element windowactive_projectname = null;
    private Element windowactive_endtime = null;
    private Element windowactive_duration = null;
    private Element windowactive_resourcename = null;

    /**
	 * Current state of episode recognizer
	 */
	private WindowActiveEpisodeState state;
	
	/**
	 * current active
	 */
	private String activeWindow = null;
	
	/**
	 * Time stamp of getting into APPLACTIVE state 
	 */
	private Date startDate = null;
	
	/**
	 * Constructor to start recognizer in initial state
	 */
	public WindowActiveEpisodeRecognizer() {

        // Start in start state
        state = WindowActiveEpisodeState.START;
        
        // initialize static DOM skeleton for msdt.editor.xsd
        if (msdt_windowactive_doc == null)
            try {
                msdt_windowactive_doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
                Element windowactive_microactivity = msdt_windowactive_doc.createElement("microActivity");                
                windowactive_username = msdt_windowactive_doc.createElement("username");
                windowactive_projectname = msdt_windowactive_doc.createElement("projectname");
                windowactive_endtime = msdt_windowactive_doc.createElement("endtime");
                windowactive_duration = msdt_windowactive_doc.createElement("duration");
                windowactive_resourcename = msdt_windowactive_doc.createElement("resourcename");
        
                msdt_windowactive_doc.appendChild(windowactive_microactivity);
                windowactive_microactivity.appendChild(windowactive_username);
                windowactive_microactivity.appendChild(windowactive_projectname);
                windowactive_microactivity.appendChild(windowactive_endtime);
                windowactive_microactivity.appendChild(windowactive_duration);
                windowactive_microactivity.appendChild(windowactive_resourcename);
            } catch (ParserConfigurationException e) {
                logger.log(Level.SEVERE, "Could not instantiate the DOM Document in WindowActiveEpisodeRecognizer.");
                logger.log(Level.FINE, e.getMessage());
            }
	}
	
	/**
	 * @see org.electrocodeogram.module.intermediate.implementation.EpisodeRecognizer#isInInitialState()
	 */
	public boolean isInInitialState() {
		return state == WindowActiveEpisodeState.START;
	}
	
	/**
	 * @see org.electrocodeogram.module.intermediate.implementation.EpisodeRecognizer#isInFinalState()
	 */
	public boolean isInFinalState() {
		return state == WindowActiveEpisodeState.STOP;
	}
	
    /**
     * @see org.electrocodeogram.module.intermediate.implementation.EpisodeRecognizer#analyse(org.electrocodeogram.event.ValidEventPacket, int, long)
     */
    public ValidEventPacket analyseSingle(ValidEventPacket packet, long minDuration) {

		ValidEventPacket event = null;

		String msdt = packet.getMicroSensorDataType().getName();

		try {

			if (msdt.equals("msdt.window.xsd") ||
                msdt.equals("msdt.part.xsd") ||
                msdt.equals("msdt.dialog.xsd") ||
                msdt.equals("msdt.editor.xsd")) {

				Document document = packet.getDocument();
				Date timestamp = packet.getTimeStamp();

				String activity = ECGParser.getSingleNodeValue("activity", document);
				String windowname = "";

				if (msdt.equals("msdt.window.xsd"))
                    windowname = ECGParser.getSingleNodeValue("windowname", document);
                if (msdt.equals("msdt.dialog.xsd"))
                    windowname = ECGParser.getSingleNodeValue("dialogname", document);
				
				if (state == WindowActiveEpisodeState.START && 
                    (activity.equals("activated") || activity.equals("opened"))) {
					state = WindowActiveEpisodeState.WINDOWACTIVE;
					activeWindow = windowname;
					startDate = timestamp;
				}
                /* doesn't seem be happen: window activate/open after part activate/open
                else if (msdt.equals("msdt.window.xsd") && 
                         state == ApplicationActiveEpisodeState.windowactive && 
                         (activity.equals("activated") || activity.equals("opened"))) {
                    // Preserve windowname
                    if (activeWindow == null) {
                        activeWindow = windowname;
                    }
                }
                */
				else if (msdt.equals("msdt.window.xsd") &&
                         state == WindowActiveEpisodeState.WINDOWACTIVE && 
						(activity.equals("deactivated") || activity.equals("closed")) 
                        /*&& windowname.equals(activeWindow)*/ ) {
                    if (activeWindow == null) {
                        activeWindow = windowname;
                    }
					event = generateEpisode("msdt.windowactive.xsd", minDuration,
							ECGParser.getSingleNodeValue("username", document),
							ECGParser.getSingleNodeValueIfAvailable("projectname", document),
							timestamp,
							startDate,
							activeWindow);
					activeWindow = null;
					startDate = null;
					state = WindowActiveEpisodeState.STOP;
				}

			}

		} catch (NodeException e) {
            logger.log(Level.SEVERE, "Could not read XML string in WindowActiveEpisodeRecognizer.");
            logger.log(Level.FINE, e.getMessage());
		}

        return event;

    }
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return activeWindow + " in state " + state;
	}

	/**
     * Helper method for generating XML for episode event
     * 
	 * @param id
	 * @param msdt
	 * @param minDur
	 * @param username
	 * @param projectname
	 * @param end
	 * @param begin
	 * @param filename
	 * @return
	 */
	private ValidEventPacket generateEpisode(String msdt, long minDur,
			String username, String projectname, Date end, 
			Date begin, String filename) {

		ValidEventPacket event = null;
		long duration = end.getTime() - begin.getTime();
		
		if (duration < minDur)
			return null;

        
        windowactive_projectname.setTextContent(projectname);
        windowactive_username.setTextContent(username);
        windowactive_endtime.setTextContent(ECGWriter.formatDate(end));
        windowactive_duration.setTextContent(String.valueOf(duration));
        windowactive_resourcename.setTextContent(filename);

        event = ECGWriter.createValidEventPacket("msdt.windowactive.xsd", begin, msdt_windowactive_doc);
        
        /*
        String data = "<?xml version=\"1.0\"?><microActivity>";		
		data += "<username>"  + username  + "</username>";
		data += "<projectname>" + projectname + "</projectname>";
		data += "<endtime>" + dateFormat.format(end) + "</endtime>";
		data += "<duration>" + duration + "</duration>";
		data += "<resourcename>" + filename + "</resourcename>";
		data += "</microActivity>";

        logger.log(Level.FINE, "windowactive event created");
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

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + ((activeWindow == null) ? 0 : activeWindow.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final WindowActiveEpisodeRecognizer other = (WindowActiveEpisodeRecognizer) obj;
        if (activeWindow == null) {
            if (other.activeWindow != null)
                return false;
        } else if (!activeWindow.equals(other.activeWindow))
            return false;
        return true;
    }


}

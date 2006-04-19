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
 * Recognizes episodes of individual activity durations (start, end) of the main window
 */
public class WindowActiveEpisodeRecognizer implements EpisodeRecognizer {

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

	/**
	 * Current state of episode recognizer
	 */
	private WindowActiveEpisodeState state;
	
	/**
	 * current active
	 */
	private String activeWindow = null;
	
	/**
	 * Time stamp of getting into WINDOWACTIVE state 
	 */
	private Date startDate = null;
	
	/**
	 * TODO: should be global
	 */
	public static DateFormat dateFormat = DateFormat.getDateTimeInstance(
	        DateFormat.MEDIUM, DateFormat.MEDIUM);
		
	/**
	 * Constructor to start recognizer in initial state
	 */
	public WindowActiveEpisodeRecognizer() {
		state = WindowActiveEpisodeState.START;
	}
	
	/* (non-Javadoc)
	 * @see org.electrocodeogram.module.intermediate.implementation.EpisodeRecognizer#isInInitialState()
	 */
	public boolean isInInitialState() {
		return state == WindowActiveEpisodeState.START;
	}
	
	/* (non-Javadoc)
	 * @see org.electrocodeogram.module.intermediate.implementation.EpisodeRecognizer#isInFinalState()
	 */
	public boolean isInFinalState() {
		return state == WindowActiveEpisodeState.STOP;
	}
	
    /* (non-Javadoc)
     * @see org.electrocodeogram.module.intermediate.implementation.EpisodeRecognizer#analyse(org.electrocodeogram.event.ValidEventPacket, int, long)
     */
    public ValidEventPacket analyse(ValidEventPacket packet, long minDuration) {

		ValidEventPacket event = null;

		String msdt = packet.getMicroSensorDataType().getName();

		try {

			if (msdt.equals("msdt.window.xsd") ||
                msdt.equals("msdt.part.xsd") ||
                msdt.equals("msdt.editor.xsd")) {

				Document document = packet.getDocument();
				Date timestamp = packet.getTimeStamp();

				String activity = ECGParser.getSingleNodeValue("activity", document);
				String windowname = "";
                if (msdt.equals("msdt.window.xsd"))
                    windowname = ECGParser.getSingleNodeValue("windowname", document);
				
				if (state == WindowActiveEpisodeState.START && 
                    (activity.equals("activated") || activity.equals("opened"))) {
					state = WindowActiveEpisodeState.WINDOWACTIVE;
					activeWindow = windowname;
					startDate = timestamp;
				}
                /* doesn't seem be happen: window activate/open after part activate/open
                else if (msdt.equals("msdt.window.xsd") && 
                         state == WindowActiveEpisodeState.windowactive && 
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
							ECGParser.getSingleNodeValue("projectname", document),
							timestamp,
							startDate,
							activeWindow);
					activeWindow = null;
					startDate = null;
					state = WindowActiveEpisodeState.STOP;
				}

			}

		} catch (NodeException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
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
        // TODO currently only allow ONE SINGLE recognizer of this type!
        return true;
    }
	

}

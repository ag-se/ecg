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
public class ApplicationActiveEpisodeRecognizer  extends AbstractSingleEpisodeRecognizer {

    /**
     * State types for this recognizer 
     */
    public enum ApplicationActiveEpisodeState {
        /**
         * Ínitial state, no window recognized yet
         */
        START, 
        
        /**
         * Window has been activated 
         */
        APPLACTIVE, 
        
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
    private static Document msdt_applicationactive_doc = null;
    private static Element applicationactive_username = null;
    private static Element applicationactive_projectname = null;
    private static Element applicationactive_endtime = null;
    private static Element applicationactive_duration = null;
    private static Element applicationactive_windowtitle = null;
    private static Element applicationactive_windowhandle = null;
    private static Element applicationactive_processname = null;

    /**
	 * Current state of episode recognizer
	 */
	private ApplicationActiveEpisodeState state;
	
	/**
	 * current active window id and the latest title
	 */
	private String activeWindow, windowTitle = null;
	
	/**
	 * Time stamp of getting into APPLACTIVE state 
	 */
	private Date startDate = null;
	
	/**
	 * Constructor to start recognizer in initial state
	 */
	public ApplicationActiveEpisodeRecognizer() {

        // Start in start state
        state = ApplicationActiveEpisodeState.START;
        
        // initialize static DOM skeleton for msdt.editor.xsd
        if (msdt_applicationactive_doc == null)
            try {
                msdt_applicationactive_doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
                Element applicationactive_microactivity = msdt_applicationactive_doc.createElement("microActivity");                
                applicationactive_username = msdt_applicationactive_doc.createElement("username");
                applicationactive_projectname = msdt_applicationactive_doc.createElement("projectname");
                applicationactive_endtime = msdt_applicationactive_doc.createElement("endtime");
                applicationactive_duration = msdt_applicationactive_doc.createElement("duration");
                applicationactive_windowtitle = msdt_applicationactive_doc.createElement("windowtitle");
                applicationactive_windowhandle = msdt_applicationactive_doc.createElement("windowhandle");
                applicationactive_processname = msdt_applicationactive_doc.createElement("processname");
        
                msdt_applicationactive_doc.appendChild(applicationactive_microactivity);
                applicationactive_microactivity.appendChild(applicationactive_username);
                applicationactive_microactivity.appendChild(applicationactive_projectname);
                applicationactive_microactivity.appendChild(applicationactive_endtime);
                applicationactive_microactivity.appendChild(applicationactive_duration);
                applicationactive_microactivity.appendChild(applicationactive_windowtitle);
                applicationactive_microactivity.appendChild(applicationactive_windowhandle);
                applicationactive_microactivity.appendChild(applicationactive_processname);
            } catch (ParserConfigurationException e) {
                logger.log(Level.SEVERE, "Could not instantiate the DOM Document in ApplicationActiveEpisodeRecognizer.");
                logger.log(Level.FINE, e.getMessage());
            }
	}
	
	/**
	 * @see org.electrocodeogram.module.intermediate.implementation.EpisodeRecognizer#isInInitialState()
	 */
	public boolean isInInitialState() {
		return state == ApplicationActiveEpisodeState.START;
	}
	
	/**
	 * @see org.electrocodeogram.module.intermediate.implementation.EpisodeRecognizer#isInFinalState()
	 */
	public boolean isInFinalState() {
		return state == ApplicationActiveEpisodeState.STOP;
	}
	
    /**
     * @see org.electrocodeogram.module.intermediate.implementation.EpisodeRecognizer#analyse(org.electrocodeogram.event.ValidEventPacket, int, long)
     */
    public ValidEventPacket analyseSingle(ValidEventPacket packet, long minDuration) {

		ValidEventPacket event = null;

		String msdt = packet.getMicroSensorDataType().getName();

		try {

			if (msdt.equals("msdt.application.xsd")) {

				Document document = packet.getDocument();
				Date timestamp = packet.getTimeStamp();

				String activity = getValue(document, "activity", "type"); // For V2
				String handle = "";

				if (msdt.equals("msdt.application.xsd"))
                    handle = getValue(document, "windowhandle", "windowHandle");
				
				if (state == ApplicationActiveEpisodeState.START && 
                    (activity.equals("activated") || activity.equals("renamed") || activity.equals("ACTIVATE") || activity.equals("RENAME"))) {
					state = ApplicationActiveEpisodeState.APPLACTIVE;
					activeWindow = handle;
                    windowTitle = getValue(document, "windowtitle", "windowTitle");
					startDate = timestamp;
				}
				else if (msdt.equals("msdt.application.xsd") &&
                         state == ApplicationActiveEpisodeState.APPLACTIVE && 
                         (activity.equals("deactivated") || activity.equals("renamed") || activity.equals("DEACTIVATE") || activity.equals("RENAME")) 
                        && handle.equals(activeWindow)) {
                    if (activeWindow == null) {
                        activeWindow = handle;
                    }
					event = generateEpisode("msdt.applicationactive.xsd", minDuration,
							ECGParser.getSingleNodeValue("username", document),
							ECGParser.getSingleNodeValueIfAvailable("projectname", document),
							timestamp,
							startDate,
                            windowTitle,
                            activeWindow,
                            ECGParser.getSingleNodeValue("processName", document)
					);
					activeWindow = null;
					startDate = null;
					state = ApplicationActiveEpisodeState.STOP;				}

			}

		} catch (NodeException e) {
            logger.log(Level.SEVERE, "Could not read XML string in ApplicationActiveEpisodeRecognizer.");
		}

        return event;

    }
	
	private String getValue(Document document, String row1, String row2) {
	    
        String result = null;
        try {
            result = ECGParser.getSingleNodeValue(row1, document);
        } catch (NodeException e) {
            // try next
        }
        if (result == null || result.length() == 0) {
            try {
                result = ECGParser.getSingleNodeValue(row2, document);
            } catch (NodeException e) {
                logger.log(Level.SEVERE, "Could'nt read value of neither '" + row1 + "' nor '" + row2 + " in XML document \n  " + document.getTextContent());
                return "";
            }            
        }
        return result;
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
			Date begin, String windowname, String windowhandle,
            String processname) {

		ValidEventPacket event = null;
		long duration = end.getTime() - begin.getTime();
		
		if (duration < minDur)
			return null;

        applicationactive_projectname.setTextContent(projectname);
        applicationactive_username.setTextContent(username);
        applicationactive_endtime.setTextContent(ECGWriter.formatDate(end));
        applicationactive_duration.setTextContent(String.valueOf(duration));
        applicationactive_windowtitle.setTextContent(windowname);
        applicationactive_windowhandle.setTextContent(windowhandle);
        applicationactive_processname.setTextContent(processname);

        event = ECGWriter.createValidEventPacket("msdt.applicationactive.xsd", begin, msdt_applicationactive_doc);
        
		return event;

	}

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if((obj == null) || (obj.getClass() != this.getClass())) return false;
        if(obj == this) return true;
        ApplicationActiveEpisodeRecognizer runActiveRecog = (ApplicationActiveEpisodeRecognizer)obj;
        if (runActiveRecog.getId() != null && this.activeWindow != null &&
                runActiveRecog.getId().equals(this.activeWindow))
            return true;
        return false;
    }

    private Object getId() {
        return this.activeWindow;
    }

}

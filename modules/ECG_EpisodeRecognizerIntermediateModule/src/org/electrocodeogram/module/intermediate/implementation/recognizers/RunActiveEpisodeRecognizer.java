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
public class RunActiveEpisodeRecognizer extends AbstractSingleEpisodeRecognizer {

    /**
     * State types for this recognizer 
     */
    public enum RunActiveEpisodeState {
        /**
         * Ínitial state, no window recognized yet
         */
        START, 
        
        /**
         * Window has been activated 
         */
        RUNACTIVE, 
        
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
    private static Document msdt_runactive_doc = null;
    private static Element runactive_username = null;
    private static Element runactive_projectname = null;
    private static Element runactive_endtime = null;
    private static Element runactive_duration = null;
    private static Element runactive_launchname = null;
    private static Element runactive_launchtype = null;

    /**
	 * Current state of episode recognizer
	 */
	private RunActiveEpisodeState state;
	
	/**
	 * current active launch name
	 */
	private String activeRun = null;
	
    /**
     * current active launch id
     */
    private String activeRunId = null;
    
    /**
     * current active launch mode
     */
    private String activeMode = null;
    
	/**
	 * Time stamp of getting into ANTACTIVE state 
	 */
	private Date startDate = null;
	
	/**
	 * Constructor to start recognizer in initial state
	 */
	public RunActiveEpisodeRecognizer() {

        // Start in start state
        state = RunActiveEpisodeState.START;
        
        // initialize static DOM skeleton for msdt.editor.xsd
        if (msdt_runactive_doc == null)
            try {
                msdt_runactive_doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
                Element runactive_microactivity = msdt_runactive_doc.createElement("microActivity");                
                runactive_username = msdt_runactive_doc.createElement("username");
                runactive_projectname = msdt_runactive_doc.createElement("projectname");
                runactive_endtime = msdt_runactive_doc.createElement("endtime");
                runactive_duration = msdt_runactive_doc.createElement("duration");
                runactive_launchname = msdt_runactive_doc.createElement("launch");
                runactive_launchtype = msdt_runactive_doc.createElement("mode");
        
                msdt_runactive_doc.appendChild(runactive_microactivity);
                runactive_microactivity.appendChild(runactive_username);
                runactive_microactivity.appendChild(runactive_projectname);
                runactive_microactivity.appendChild(runactive_endtime);
                runactive_microactivity.appendChild(runactive_duration);
                runactive_microactivity.appendChild(runactive_launchname);
                runactive_microactivity.appendChild(runactive_launchtype);
            } catch (ParserConfigurationException e) {
                logger.log(Level.SEVERE, "Could not instantiate the DOM Document in RunActiveEpisodeRecognizer.");
                logger.log(Level.FINE, e.getMessage());
            }
	}
	
	/**
	 * @see org.electrocodeogram.module.intermediate.implementation.EpisodeRecognizer#isInInitialState()
	 */
	public boolean isInInitialState() {
		return state == RunActiveEpisodeState.START;
	}
	
	/**
	 * @see org.electrocodeogram.module.intermediate.implementation.EpisodeRecognizer#isInFinalState()
	 */
	public boolean isInFinalState() {
		return state == RunActiveEpisodeState.STOP;
	}
	
    /**
     * @see org.electrocodeogram.module.intermediate.implementation.EpisodeRecognizer#analyse(org.electrocodeogram.event.ValidEventPacket, int, long)
     */
    public ValidEventPacket analyseSingle(ValidEventPacket packet, long minDuration) {

		ValidEventPacket event = null;

		String msdt = packet.getMicroSensorDataType().getName();

		try {

			if (msdt.equals("msdt.run.xsd")) {

				Document document = packet.getDocument();
				Date timestamp = packet.getTimeStamp();

				String mode = ECGParser.getSingleNodeValue("mode", document);
				String launchname = ECGParser.getSingleNodeValue("launch", document);
				String id = ECGParser.getSingleNodeValue("id", document);
                
				if (state == RunActiveEpisodeState.START && !mode.equals("termination")) {

                    state = RunActiveEpisodeState.RUNACTIVE;
					activeRun = launchname;
                    activeRunId = id;
                    activeMode = mode;
					startDate = timestamp;

                } else if (state == RunActiveEpisodeState.RUNACTIVE 
                        && mode.equals("termination")
                        && id.equals(activeRunId)) {

                    if (activeRun == null) {
                        activeRun = launchname;
                    }
					event = generateEpisode("msdt.runactive.xsd", minDuration,
							ECGParser.getSingleNodeValue("username", document),
							ECGParser.getSingleNodeValueIfAvailable("projectname", document),
							timestamp,
							startDate,
							activeRun,
                            activeMode);
					activeRun = null;
                    activeRunId = null;
                    activeMode = null;
					startDate = null;
					state = RunActiveEpisodeState.STOP;
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
		return activeRun + " in state " + state;
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
			Date begin, String launchname, String launchtype) {

		ValidEventPacket event = null;
		long duration = end.getTime() - begin.getTime();
		
		if (duration < minDur)
			return null;
        
        runactive_projectname.setTextContent(projectname);
        runactive_username.setTextContent(username);
        runactive_endtime.setTextContent(ECGWriter.formatDate(end));
        runactive_duration.setTextContent(String.valueOf(duration));
        runactive_launchname.setTextContent(launchname);
        runactive_launchtype.setTextContent(launchtype);

        event = ECGWriter.createValidEventPacket("msdt.runactive.xsd", begin, msdt_runactive_doc);
        
		return event;

	}

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if((obj == null) || (obj.getClass() != this.getClass())) return false;
        if(obj == this) return true;
        RunActiveEpisodeRecognizer runActiveRecog = (RunActiveEpisodeRecognizer)obj;
        if (runActiveRecog.getId() != null && this.activeRunId != null &&
                runActiveRecog.getId().equals(this.activeRunId))
            return true;
        return false;
    }

    public Object getId() {
        return this.activeRunId;
    }
	
}

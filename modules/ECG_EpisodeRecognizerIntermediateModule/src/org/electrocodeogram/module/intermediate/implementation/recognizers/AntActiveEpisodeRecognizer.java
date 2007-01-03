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
 * Recognizes episodes of individual start/termination durations (start, end) of ant tasks
 */
public class AntActiveEpisodeRecognizer extends AbstractSingleEpisodeRecognizer {

    /**
     * State types for this recognizer 
     */
    public enum AntActiveEpisodeState {
        /**
         * Ínitial state, no window recognized yet
         */
        START, 
        
        /**
         * Ant has been started 
         */
        ANTACTIVE, 
        
        /**
         * Final state after ant terminated
         */
        STOP
    }
	
	/**
	 * General logger for this class 
	 */
	private static Logger logger = LogHelper
        .createLogger(EpisodeRecognizerIntermediateModule.class.getName());

    // XML Document and Elements
    private static Document msdt_antactive_doc = null;
    private static Element antactive_username = null;
    private static Element antactive_projectname = null;
    private static Element antactive_endtime = null;
    private static Element antactive_duration = null;
    private static Element antactive_buildfile = null;
    private static Element antactive_target = null;

    /**
	 * Current state of episode recognizer
	 */
	private AntActiveEpisodeState state;
	
    /**
     * current active id of Ant task
     */
    private String activeAntId = null;
    
    /**
     * current active Ant buildfile
     */
    private String activeBuildfile = null;
    
    /**
     * current active Ant target
     */
    private String activeTarget = null;
    
	/**
	 * Time stamp of getting into ANTACTIVE state 
	 */
	private Date startDate = null;
	
	/**
	 * Constructor to start recognizer in initial state
	 */
	public AntActiveEpisodeRecognizer() {

        // Start in start state
        state = AntActiveEpisodeState.START;
        
        // initialize static DOM skeleton for msdt.antactivity.xsd
        if (msdt_antactive_doc == null)
            try {
                msdt_antactive_doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
                Element runactive_microactivity = msdt_antactive_doc.createElement("microActivity");                
                antactive_username = msdt_antactive_doc.createElement("username");
                antactive_projectname = msdt_antactive_doc.createElement("projectname");
                antactive_endtime = msdt_antactive_doc.createElement("endtime");
                antactive_duration = msdt_antactive_doc.createElement("duration");
                antactive_buildfile = msdt_antactive_doc.createElement("buildfile");
                antactive_target = msdt_antactive_doc.createElement("target");
        
                msdt_antactive_doc.appendChild(runactive_microactivity);
                runactive_microactivity.appendChild(antactive_username);
                runactive_microactivity.appendChild(antactive_projectname);
                runactive_microactivity.appendChild(antactive_endtime);
                runactive_microactivity.appendChild(antactive_duration);
                runactive_microactivity.appendChild(antactive_buildfile);
                runactive_microactivity.appendChild(antactive_target);
            } catch (ParserConfigurationException e) {
                logger.log(Level.SEVERE, "Could not instantiate the DOM Document in AntActiveEpisodeRecognizer.");
                logger.log(Level.FINE, e.getMessage());
            }
	}
	
	/**
	 * @see org.electrocodeogram.module.intermediate.implementation.EpisodeRecognizer#isInInitialState()
	 */
	public boolean isInInitialState() {
		return state == AntActiveEpisodeState.START;
	}
	
	/**
	 * @see org.electrocodeogram.module.intermediate.implementation.EpisodeRecognizer#isInFinalState()
	 */
	public boolean isInFinalState() {
		return state == AntActiveEpisodeState.STOP;
	}
	
    /**
     * @see org.electrocodeogram.module.intermediate.implementation.EpisodeRecognizer#analyse(org.electrocodeogram.event.ValidEventPacket, int, long)
     */
    public ValidEventPacket analyseSingle(ValidEventPacket packet, long minDuration) {

		ValidEventPacket event = null;

		String msdt = packet.getMicroSensorDataType().getName();

		try {

			if (msdt.equals("msdt.antrun.xsd")) {

				Document document = packet.getDocument();
				Date timestamp = packet.getTimeStamp();

				String mode = ECGParser.getSingleNodeValue("mode", document);
				String buildfile = ECGParser.getSingleNodeValue("buildfile", document);
                String target = ECGParser.getSingleNodeValue("target", document);
				String id = ECGParser.getSingleNodeValue("id", document);
                
				if (state == AntActiveEpisodeState.START && !mode.equals("termination")) {

                    state = AntActiveEpisodeState.ANTACTIVE;
                    activeTarget = target;
					activeBuildfile = buildfile;
                    activeAntId = id;
					startDate = timestamp;

                } else if (state == AntActiveEpisodeState.ANTACTIVE 
                        && mode.equals("termination")
                        && id.equals(activeAntId)) {

                    if (activeBuildfile == null)
                        activeBuildfile = buildfile;
                    if (activeTarget == null)
                        activeTarget = buildfile;

					event = generateEpisode("msdt.runactive.xsd", minDuration,
							ECGParser.getSingleNodeValue("username", document),
							ECGParser.getSingleNodeValueIfAvailable("projectname", document),
							timestamp,
							startDate,
                            activeBuildfile,
                            activeTarget);
					activeAntId = null;
                    activeBuildfile = null;
                    activeTarget = null;
					startDate = null;
					state = AntActiveEpisodeState.STOP;
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
		return activeTarget + " in state " + state;
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
			Date begin, String buildfile, String target) {

		ValidEventPacket event = null;
		long duration = end.getTime() - begin.getTime();
		
		if (duration < minDur)
			return null;
        
        antactive_projectname.setTextContent(projectname);
        antactive_username.setTextContent(username);
        antactive_endtime.setTextContent(ECGWriter.formatDate(end));
        antactive_duration.setTextContent(String.valueOf(duration));
        antactive_buildfile.setTextContent(buildfile);
        antactive_target.setTextContent(target);

        event = ECGWriter.createValidEventPacket("msdt.antactive.xsd", begin, msdt_antactive_doc);
        
		return event;

	}

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        // TODO currently only allow ONE SINGLE recognizer of this type!
        return true;
    }
	

}

/*
 * Freie Universität Berlin, 2006
 *
 */
package org.electrocodeogram.module.intermediate.implementation.recognizers;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.electrocodeogram.event.ValidEventPacket;
import org.electrocodeogram.logging.LogHelper;
import org.electrocodeogram.misc.xml.ECGParser;
import org.electrocodeogram.misc.xml.NodeException;
import org.electrocodeogram.module.intermediate.implementation.AbstractSingleEpisodeRecognizer;
import org.electrocodeogram.module.intermediate.implementation.EpisodeRecognizerIntermediateModule;
import org.w3c.dom.Document;

/**
 * Recognizes episodes of individual activity durations (start, end) of the main window
 */
public class BettermentEpisodeRecognizer  extends AbstractSingleEpisodeRecognizer {

    /**
     * State types for this recognizer 
     */
    public enum BettermentEpisodeState {
        /**
         * Ínitial state, no betterment recognized yet
         */
        START, 
        
        /**
         * Betterment hypothised has been activated 
         */
        BM_ACTIVE1, 
        BM_ACTIVE2, 
        BM_ACTIVE3, 
        
        /**
         * Final state after Betterment recognized
         */
        STOP
    }
	
	/**
	 * General logger for this class 
	 */
	private static Logger logger = LogHelper
        .createLogger(EpisodeRecognizerIntermediateModule.class.getName());

    // XML Document and Elements
    /*
    private static Document msdt_runactive_doc = null;
    private static Element runactive_username = null;
    private static Element runactive_projectname = null;
    private static Element runactive_endtime = null;
    private static Element runactive_duration = null;
    private static Element runactive_launchname = null;
    private static Element runactive_launchtype = null;
     */
    
    /**
	 * Current state of episode recognizer
	 */
	private BettermentEpisodeState state;
	
	/**
	 * Time stamp of getting into BM_ACTIVE state 
	 */
	private Date startDate = null;
	private String loc1 = null;
    private String loc2 = null;
    private String loc3 = null;
    private Date locd1 = null;
    private Date locd2 = null;
    private Date locd3 = null;

    /**
	 * Constructor to start recognizer in initial state
	 */
	public BettermentEpisodeRecognizer() {

        // Start in start state
        state = BettermentEpisodeState.START;
        
        // initialize static DOM skeleton for msdt.editor.xsd
        /*
        if (msdt_runactive_doc == null) {
            try {
                msdt_runactive_doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
                Element runactive_microactivity = msdt_runactive_doc.createElement("microActivity");                
                runactive_username = msdt_runactive_doc.createElement("username");
                runactive_projectname = msdt_runactive_doc.createElement("projectname");
                runactive_endtime = msdt_runactive_doc.createElement("endtime");
                runactive_duration = msdt_runactive_doc.createElement("duration");
                runactive_launchname = msdt_runactive_doc.createElement("launchname");
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
        */
	}
	
	/**
	 * @see org.electrocodeogram.module.intermediate.implementation.EpisodeRecognizer#isInInitialState()
	 */
	public boolean isInInitialState() {
		return state == BettermentEpisodeState.START;
	}
	
	/**
	 * @see org.electrocodeogram.module.intermediate.implementation.EpisodeRecognizer#isInFinalState()
	 */
	public boolean isInFinalState() {
		return state == BettermentEpisodeState.STOP;
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
                
				if (state == BettermentEpisodeState.START && mode.equals("termination")) {
                    state = BettermentEpisodeState.BM_ACTIVE1;
					startDate = timestamp;
                } else if (state.compareTo(BettermentEpisodeState.START) > 0  && !mode.equals("termination")) {
                    printBetterment(timestamp);
                }
                
			} else if (msdt.equals("msdt.codelocation.xsd")) {

                Document document = packet.getDocument();
                Date timestamp = packet.getTimeStamp();

                if (state == BettermentEpisodeState.BM_ACTIVE1) {
                    state = BettermentEpisodeState.BM_ACTIVE2;
                    loc1 = ECGParser.getSingleNodeValue("locid", document);
                    locd1 = timestamp;
                } else if (state == BettermentEpisodeState.BM_ACTIVE2) {
                    state = BettermentEpisodeState.BM_ACTIVE3;
                    loc2 = ECGParser.getSingleNodeValue("locid", document);
                    locd2 = timestamp;
                } else if (state == BettermentEpisodeState.BM_ACTIVE3) {
                    state = BettermentEpisodeState.STOP;
                    loc3 = ECGParser.getSingleNodeValue("locid", document);
                    locd3 = timestamp;
                    printBetterment(timestamp);
                }
            }
		} catch (NodeException e) {
            logger.log(Level.SEVERE, "Could not read XML string in BettermentActiveEpisodeRecognizer.");
            logger.log(Level.SEVERE, e.getMessage());
		}

        return event;

    }
	
	private void printBetterment(Date timestamp) {
        System.out.println("Run termination at " + this.startDate + " followed by \n" + 
            printLoc(loc1, locd1) + 
            printLoc(loc2, locd2) + 
            printLoc(loc3, locd3) 
        );
        
    }

    /* (non-Javadoc)
	 * @see java.lang.Object#toString()
	public String toString() {
		return activeRun + " in state " + state;
	}
     */

	private String printLoc(String loc, Date locd) {
        String res = "";
        if (loc != null) {
            res += "Location " + loc + " after " + timediff(this.startDate, locd) + " seconds"; 
            res += " (at " + locd + ")\n";
        }
        return res; 
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
     */

    private String timediff(Date start, Date end) {
        return String.valueOf((end.getTime() - start.getTime()) / 1000);
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

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
 * Recognizes episodes of individual activity durations (start, end) of a text file
 */
public class FileActiveEpisodeRecognizer implements EpisodeRecognizer {

    /**
     * Types of states for this episode recognize
     */
    private enum FileActiveEpisodeState {
        /**
         * Initial state, i.e. no file attached 
         */
        START, 

        /**
         * File has been activated
         */
        FILEACTIVE, 

        /**
         * File is not active because the window is not active
         */
        FILEHOLD, 

        /**
         * File is not active, because it has been deactivated
         */
        STOP
    }
	
    /**
     * Used data format
     * TODO: change this to a publicly accessable static 
     */
    public static DateFormat dateFormat = DateFormat.getDateTimeInstance(
            DateFormat.MEDIUM, DateFormat.MEDIUM);

    /**
     * General logger 
     */
    private static Logger logger = LogHelper
    .createLogger(EpisodeRecognizerIntermediateModule.class.getName());

    /**
     * Stores time stamp of last change event. Resolves a bug in redundant code change events 
     */
    private Date lastChangeDate;
        
	/**
	 * Counts the number of Code Changes during fileactive time 
	 */
	private int changeCount = 0;
	
	/**
	 * Holds the current state of episode automata
	 */
	private FileActiveEpisodeState state;
	
	/**
	 * Stores the name of the file in fileactive or filehold state
	 */
	private String activeFileName = null;
	
	/**
	 * Stores the time stamp when file has been activated
	 */
	private Date startDate = null;
	
	/**
	 * Constructor to start the recognizer in initial state
	 */
	public FileActiveEpisodeRecognizer() {
		state = FileActiveEpisodeState.START;
	}
	
    /* (non-Javadoc)
     * @see org.electrocodeogram.module.intermediate.implementation.EpisodeRecognizer#isInInitialState()
     */
    public boolean isInInitialState() {
		return state == FileActiveEpisodeState.START;
	}
	
	/* (non-Javadoc)
	 * @see org.electrocodeogram.module.intermediate.implementation.EpisodeRecognizer#isInFinalState()
	 */
	public boolean isInFinalState() {
		return state == FileActiveEpisodeState.STOP;
	}
	
    /* (non-Javadoc)
     * @see org.electrocodeogram.module.intermediate.implementation.EpisodeRecognizer#analyse(org.electrocodeogram.event.ValidEventPacket, int, long)
     */
    public ValidEventPacket analyse(ValidEventPacket packet, int id, long minDuration) {

		ValidEventPacket event = null;

		String msdt = packet.getMicroSensorDataType().getName();

		try {

			if (msdt.equals("msdt.editor.xsd") || 
				msdt.equals("msdt.window.xsd") ||
                msdt.equals("msdt.part.xsd") ||
				msdt.equals("msdt.codechange.xsd")) {

				Document document = packet.getDocument();
				Date timestamp = packet.getTimeStamp();
				
				if (msdt.equals("msdt.editor.xsd")) {

					String activity = ECGParser.getSingleNodeValue("activity", document);
					String editorname = ECGParser.getSingleNodeValue("editorname", document);
					
					if (state == FileActiveEpisodeState.START && activity.equals("activated")) {
						state = FileActiveEpisodeState.FILEACTIVE;
						activeFileName = editorname;
						startDate = timestamp;
                        changeCount = 0;
					}
                    else if (state == FileActiveEpisodeState.START && activity.equals("opened")) {
                            // after Eclipse was deactivated, activating it again results in a single opened event
                            // for the former active editor - no activated event. In cases of newly opened editors,
                            // a code change follows to init the delta computation in the same second. 
                            // This is *not* a change, so we wont count it: this is done via setting the
                            // lastChangeDate but not counting it.
                            // Problems occur with TabGroups, though. Could be possible to compare Window names
                            // (TabGroups have "null") but since the name may change, it looks like being unreliable
                            state = FileActiveEpisodeState.FILEACTIVE;
                            activeFileName = editorname;
                            startDate = timestamp;
                            lastChangeDate = timestamp;
                            changeCount = 0;
                        }
					else if ((state == FileActiveEpisodeState.FILEACTIVE) && 
    							activity.equals("deactivated") && 
    							editorname.equals(activeFileName)) {
    						event = generateEpisode(id, minDuration, "msdt.fileactive.xsd", 
    								ECGParser.getSingleNodeValue("username", document),
    								ECGParser.getSingleNodeValue("projectname", document),
    								timestamp,
    								startDate,
    								changeCount,
    								activeFileName);
    						activeFileName = null;
    						startDate = null;
    						state = FileActiveEpisodeState.STOP;
    					}
                    else if ((state == FileActiveEpisodeState.FILEHOLD) && 
                                activity.equals("deactivated") && 
                                editorname.equals(activeFileName)) {
                            // File has been silently deactivated, discard it
                            activeFileName = null;
                            startDate = null;
                            state = FileActiveEpisodeState.STOP;
                        }
                    else if (state == FileActiveEpisodeState.FILEACTIVE && 
                                activity.equals("activated") && 
                                !editorname.equals(activeFileName)) {
                            // Another editor has been activated => this one will be deactivated 
                            event = generateEpisode(id, minDuration, "msdt.fileactive.xsd", 
                                    ECGParser.getSingleNodeValue("username", document),
                                    ECGParser.getSingleNodeValue("projectname", document),
                                    timestamp,
                                    startDate,
                                    changeCount,
                                    activeFileName);
                            activeFileName = null;
                            startDate = null;
                            state = FileActiveEpisodeState.STOP;
                        }
                    
				} else if (msdt.equals("msdt.window.xsd")) {

					String activity = ECGParser.getSingleNodeValue("activity", document);
					
					if (state == FileActiveEpisodeState.FILEACTIVE && 
                            (activity.equals("deactivated") || activity.equals("closed"))) {
    						state = FileActiveEpisodeState.FILEHOLD;
    						event = generateEpisode(id, minDuration, "msdt.fileactive.xsd", 
    								ECGParser.getSingleNodeValue("username", document),
    								ECGParser.getSingleNodeValue("projectname", document),
    								timestamp,
    								startDate,
                                    changeCount,
    								activeFileName);
    						startDate = null;
    					}
					else if (state == FileActiveEpisodeState.FILEHOLD && activity.equals("activated")) {
    						state = FileActiveEpisodeState.FILEACTIVE;
    						startDate = timestamp;
                            changeCount = 0;
    					}

                } else if (msdt.equals("msdt.part.xsd")) {

                    String activity = ECGParser.getSingleNodeValue("activity", document);

                    if (state == FileActiveEpisodeState.FILEACTIVE && 
                            activity.equals("activated")) {
                        // Another part has been activated => this one will be deactivated 
                        event = generateEpisode(id, minDuration, "msdt.fileactive.xsd", 
                                ECGParser.getSingleNodeValue("username", document),
                                ECGParser.getSingleNodeValue("projectname", document),
                                timestamp,
                                startDate,
                                changeCount,
                                activeFileName);
                        activeFileName = null;
                        startDate = null;
                        state = FileActiveEpisodeState.STOP;
                    }

                } else if (msdt.equals("msdt.codechange.xsd")) {

					String documentname = ECGParser.getSingleNodeValue("documentname", document);
                    
                    if (state == FileActiveEpisodeState.START) {
                        // Codechange on non-active editor means at least editor activation
                        state = FileActiveEpisodeState.FILEACTIVE;
                        activeFileName = documentname;
                        startDate = timestamp;
                        changeCount = 0;
                    }
                    else if (state != FileActiveEpisodeState.START && activeFileName.equals(documentname)) {
                        if (lastChangeDate == null || !lastChangeDate.equals(timestamp)) {
                            // for some reason, code change event are snt more than once at 
                            // exactly the same time. Don't count these.
                            lastChangeDate = timestamp;
                            changeCount++;
                        }
                    }

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
		return activeFileName + " in state " + state;
	}

	/**
     * Helper method to create a new episode event
     * 
	 * @param id Id of module
	 * @param minDur Configured minimal duration of episode which should be reported
	 * @param msdt Type of event
	 * @param username Name of user
	 * @param projectname Name of project
	 * @param end End time stamp
	 * @param begin Start time stamp
	 * @param count Editor code change count
	 * @param filename Name of editor
	 * @return
	 */
	private ValidEventPacket generateEpisode(int id, long minDur, String msdt, 
				String username, String projectname, Date end, 
				Date begin, int count, String filename) {

		ValidEventPacket event = null;
		long duration = end.getTime() - begin.getTime();
		
		if (duration < minDur)
			return null;
		
        String data = "<?xml version=\"1.0\"?><microActivity>";
		
		data += "<username>"  + username  + "</username>";

		data += "<projectname>" + projectname + "</projectname>";

		data += "<endtime>" + dateFormat.format(end) + "</endtime>";

		data += "<duration>" + duration + "</duration>";

		data += "<changed>" + count + "</changed>";

		data += "<resourcename>" + filename + "</resourcename>";

		data += "</microActivity>";

        logger.log(Level.FINE, "fileactive event created");
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
        FileActiveEpisodeRecognizer fileActiveRecog = (FileActiveEpisodeRecognizer)obj;
//        if (fileActiveRecog.getState().equals(this.state))
            if (fileActiveRecog.getFileName() != null && this.activeFileName != null &&
                fileActiveRecog.getFileName().equals(this.activeFileName))
                return true;
        return false;
    }

    /**
     * Returns current file name, if available 
     * 
     * @return file name, or null if in initial or final state
     */
    private String getFileName() {
        return activeFileName;
    }

    /**
     * @return State of episode recognizer
     */
    private FileActiveEpisodeState getState() {
        // TODO Auto-generated method stub
        return state;
    }


}

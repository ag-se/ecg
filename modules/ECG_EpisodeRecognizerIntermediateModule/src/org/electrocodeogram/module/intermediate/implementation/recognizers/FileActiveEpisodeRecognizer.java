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

public class FileActiveEpisodeRecognizer implements EpisodeRecognizer {

    private enum FileActiveEpisodeState {start, fileactive, filehold, stop}
	
	// Eigentlich ist dies ein kleiner Unterautomat. Die Zustandsmenge oben müsste man verdoppeln!
	private boolean hasBeenChanged = false;
	
	private static Logger logger = LogHelper
    .createLogger(EpisodeRecognizerIntermediateModule.class.getName());

	private FileActiveEpisodeState state;
	
	private String activeFile = null;
	
	private Date startDate = null;
	
	public static DateFormat dateFormat = DateFormat.getDateTimeInstance(
	        DateFormat.MEDIUM, DateFormat.MEDIUM);
		
	public FileActiveEpisodeRecognizer() {
		state = FileActiveEpisodeState.start;
	}
	
    public boolean isInInitialState() {
		return state == FileActiveEpisodeState.start;
	}
	
	public boolean isInFinalState() {
		return state == FileActiveEpisodeState.stop;
	}
	
    public ValidEventPacket analyse(ValidEventPacket packet, int id, long minDuration) {

		ValidEventPacket event = null;

		String msdt = packet.getMicroSensorDataType().getName();

		try {

			if (msdt.equals("msdt.editor.xsd") || 
				msdt.equals("msdt.window.xsd") ||
				msdt.equals("msdt.codechange.xsd")) {

				Document document = packet.getDocument();
				Date timestamp = packet.getTimeStamp();
				
				if (msdt.equals("msdt.editor.xsd")) {

					String activity = ECGParser.getSingleNodeValue("activity", document);
					String editorname = ECGParser.getSingleNodeValue("editorname", document);
					
					if (state == FileActiveEpisodeState.start && activity.equals("activated")) {
						state = FileActiveEpisodeState.fileactive;
						activeFile = editorname;
						startDate = timestamp;
					}
					else if (state == FileActiveEpisodeState.fileactive && 
							activity.equals("deactivated") && 
							editorname.equals(activeFile)) {
						event = generateEpisode(id, minDuration, "msdt.fileactive.xsd", 
								ECGParser.getSingleNodeValue("username", document),
								ECGParser.getSingleNodeValue("projectname", document),
								timestamp,
								startDate,
								hasBeenChanged,
								activeFile);
						activeFile = null;
						startDate = null;
						state = FileActiveEpisodeState.stop;
					}
				} else if (msdt.equals("msdt.window.xsd")) {

					String activity = ECGParser.getSingleNodeValue("activity", document);
					
					if (state == FileActiveEpisodeState.fileactive && activity.equals("deactivated")) {
						state = FileActiveEpisodeState.filehold;
						event = generateEpisode(id, minDuration, "msdt.fileactive.xsd", 
								ECGParser.getSingleNodeValue("username", document),
								ECGParser.getSingleNodeValue("projectname", document),
								timestamp,
								startDate,
								hasBeenChanged,
								activeFile);
						startDate = null;
					}
					else if (state == FileActiveEpisodeState.filehold && activity.equals("activated")) {
						state = FileActiveEpisodeState.fileactive;
						startDate = timestamp; 
					}

				} else if (msdt.equals("msdt.codechange.xsd")) {

					String documentname = ECGParser.getSingleNodeValue("documentname", document);
					
					if (state != FileActiveEpisodeState.start && activeFile.equals(documentname)) 
						hasBeenChanged = true;

				}

			}

		} catch (NodeException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

        return event;

    }
	
	public String toString() {
		return activeFile + " in state " + state;
	}

	private ValidEventPacket generateEpisode(int id, long minDur, String msdt, 
				String username, String projectname, Date end, 
				Date begin, boolean changed, String filename) {

		ValidEventPacket event = null;
		long duration = end.getTime() - begin.getTime();
		
		if (duration < minDur)
			return null;
		
        String data = "<?xml version=\"1.0\"?><microActivity>";
		
		data += "<username>"  + username  + "</username>";

		data += "<projectname>" + projectname + "</projectname>";

		data += "<starttime>" + this.dateFormat.format(begin) + "</starttime>";

		data += "<duration>" + duration + "</duration>";

		data += "<changed>" + (changed ? "true" : "false") + "</changed>";

		data += "<resourcename>" + filename + "</resourcename>";

		data += "</microActivity>";

        logger.log(Level.FINE, "fileactive event created");
        logger.log(Level.FINE, data);

        String[] args = {WellFormedEventPacket.HACKYSTAT_ADD_COMMAND, msdt,
            data};

        try {
            event = new ValidEventPacket(id, end,
                WellFormedEventPacket.HACKYSTAT_ACTIVITY_STRING, Arrays
                    .asList(args));

        } catch (IllegalEventParameterException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
		
		return event;

	}

    @Override
    public boolean equals(Object obj) {
        // TODO Auto-generated method stub
        if((obj == null) || (obj.getClass() != this.getClass())) return false;
        if(obj == this) return true;
        FileActiveEpisodeRecognizer fileActiveRecog = (FileActiveEpisodeRecognizer)obj;
        if (fileActiveRecog.getState().equals(this.state))
            if (fileActiveRecog.getFileName() != null &&
                fileActiveRecog.getFileName().equals(this.activeFile))
                return true;
        return false;
    }

    private String getFileName() {
        // TODO Auto-generated method stub
        return activeFile;
    }

    private FileActiveEpisodeState getState() {
        // TODO Auto-generated method stub
        return state;
    }


}

package org.electrocodeogram.module.intermediate.implementation.recognizers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.awt.List;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;


import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.electrocodeogram.event.IllegalEventParameterException;
import org.electrocodeogram.event.ValidEventPacket;
import org.electrocodeogram.logging.LogHelper;
import org.electrocodeogram.misc.xml.ECGParser;
import org.electrocodeogram.misc.xml.ECGWriter;
import org.electrocodeogram.misc.xml.NodeException;
import org.electrocodeogram.module.intermediate.implementation.EpisodeRecognizer;
import org.electrocodeogram.module.intermediate.implementation.EpisodeRecognizerIntermediateModule;
import org.electrocodeogram.module.intermediate.implementation.location.change.BlockChange;
import org.electrocodeogram.module.intermediate.implementation.location.change.LineChange;
import org.electrocodeogram.module.intermediate.implementation.location.change.BlockChange.BlockChangeType;
import org.electrocodeogram.module.intermediate.implementation.location.state.*;
import org.electrocodeogram.module.source.EventReaderException;
import org.electrocodeogram.msdt.MicroSensorDataType;
//import org.electrocodeogram.module.intermediate.implementation.recognizers.FileActiveEpisodeRecognizer.FileActiveEpisodeState;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Generelles Modell:
 * 1. Alle Aktionen beim Codieren werden mit zwei Konzepten beschrieben.
 * 		a) Generelle Textoperationen: CUT, COPY, PASTE
 * 		b) Tatsächliche Zeilenoperationen: linediffs
 * 		   linediffs repräsentieren eine Change-Aktion eines beliebigen Codeabschnitts.
 * 2. Ein Recognizer muss prüfen, ob eine generelle Textoperation ausgewertet werden muss.
 * 3. Ein Recognizer muss prüfen, ob die Änderung eines beliebigen Codeabschnitts ihn btrifft.
 */

public class CopyPasteChangeEpisodeRecognizer implements EpisodeRecognizer {
	// STATIC for all Recognizers
	private static Integer ConsCounter = 0;
	private static final double STRING_MEASURE = 100.0;
	// NOT STATIC for each Recognizer
	private enum CopyPasteChangeEpisodeState {
        /**
         * Ínitial state, no textoperation recognized yet
         */
        START, /* 0 */
        /**
         * 
         */
        COPY, /* 1 */
        /**
         * 
         */
        CUT, /* 2 */
        /**
         * 
         */
        CUTPASTE, /* 3 */
        /**
         * 
         */
        PASTE, /* 4 */
        /**
         * 
         */
        CHANGE, /* 5 */
        /**
         * 
         */
        STOP /* 6 */
    }
	
	private CopyPasteChangeEpisodeState STATE;
	private Vector<Clone> CloneFamilyVector;
	public ArrayList<ValidEventPacket> validEventPacketList; 
	private int nr = 0;
	private String user = null;
	private String project = null;
	private String docuname = null;
	//private Date paste_timestamp = null;
	
	private static Logger logger = LogHelper
    .createLogger(EpisodeRecognizerIntermediateModule.class.getName());

    // XML Document and Elements
    private static Document msdt_cpcwarning_doc = null;
    private static Element cpcwarning_username = null;
    private static Element cpcwarning_projectname = null;
    private static Element cpcwarning_documentnames = null;
    private static Element cpcwarning_version = null;
    private static Element cpcwarning_creator = null;
    private static Element cpcwarning_startline = null;
    private static Element cpcwarning_endline = null;
	
	public CopyPasteChangeEpisodeRecognizer() {
		//Start in start STATE
		STATE = CopyPasteChangeEpisodeState.START;
		synchronized (ConsCounter) {
        	ConsCounter++;
        	this.nr = ConsCounter.intValue();
        }
        CloneFamilyVector = new Vector();
        
        // initialize static DOM skeleton for msdt.cpcwarning.xsd
        if (msdt_cpcwarning_doc == null)
             try {
                 msdt_cpcwarning_doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
                 Element cpcwarning_microactivity = msdt_cpcwarning_doc.createElement("microActivity");                
                 cpcwarning_projectname = msdt_cpcwarning_doc.createElement("projectname");
                 cpcwarning_username = msdt_cpcwarning_doc.createElement("username");
                 cpcwarning_documentnames = msdt_cpcwarning_doc.createElement("documentname");
                 cpcwarning_startline = msdt_cpcwarning_doc.createElement("startline");
                 cpcwarning_endline = msdt_cpcwarning_doc.createElement("endline");
                 
                 msdt_cpcwarning_doc.appendChild(cpcwarning_microactivity);
                 cpcwarning_microactivity.appendChild(cpcwarning_username);
                 cpcwarning_microactivity.appendChild(cpcwarning_projectname);
                 cpcwarning_microactivity.appendChild(cpcwarning_documentnames);
                 cpcwarning_microactivity.appendChild(cpcwarning_startline);
                 cpcwarning_microactivity.appendChild(cpcwarning_endline);
             } 
             catch (ParserConfigurationException e) {
                 logger.log(Level.SEVERE, "Could not instantiate the DOM Document in CopyPasteChangeEpisodeRecognizer.");
                 logger.log(Level.FINE, e.getMessage());
             }
	}
	
	public boolean isInInitialState() {
		return (STATE == CopyPasteChangeEpisodeState.START);
	}
	public boolean isInFinalState() {
		return (STATE == CopyPasteChangeEpisodeState.STOP);
	}
	
	public void evaluateStringDiff(Clone event){
		double percentResult = 0.0;
		ListIterator<Clone> diffevallist = CloneFamilyVector.listIterator();
		while(diffevallist.hasNext()){
			Clone diffevalevent = diffevallist.next();
			if(!event.equals(diffevalevent) && diffevalevent.isValid()){
				String eventCode = event.getEventCode();
				System.out.println("eventCode:" + eventCode);
				String diffevaleventCode = diffevalevent.getEventCode();
				System.out.println("diffevaleventCode: " + diffevaleventCode);
				StringDifferMeasurement stringdiff = new StringDifferMeasurement(event.getEventCode(), diffevalevent.getEventCode()); 
				percentResult = stringdiff.LevenshteinDistance(stringdiff.firstString, stringdiff.secondString);
				System.out.println("verschiedenheit: " + percentResult);
			}
			if((event.isValid() == true) && (percentResult >= STRING_MEASURE)){
				event.setValid(false);
			}
			if((event.isValid() == false) && (percentResult < STRING_MEASURE)){
				event.setValid(true);
			}
		}
	}
	
	public boolean countDeletedEvents(){
		boolean stopRecognizer = false;
		int deletedCounter = 0;
		int vectorSize = CloneFamilyVector.size();
		for(int i = 0; i < vectorSize; i++){
			if(CloneFamilyVector.get(i).isDeleted()){
				deletedCounter++;
			}	
		}
		if(vectorSize - deletedCounter <= 1){
			stopRecognizer = true;
			return stopRecognizer;
		}
		else return stopRecognizer;
	}
		
    public Collection<ValidEventPacket> analyse(ValidEventPacket packet, long minDuration) {
    	/*if(packet.getMicroSensorDataType().getName().equalsIgnoreCase("msdt.linediff.xsd")) 
    		System.out.println(packet);*/
    	ValidEventPacket validEvent = null;
    	boolean event_inside_change = false;
    	Clone event = null;
    	Document document = null;
    	Date timestamp = null;
    	String msdt = packet.getMicroSensorDataType().getName();
    	if(msdt.equals("msdt.textoperation.xsd") || msdt.equals("msdt.linediff.xsd")){
    		try {
    			document = packet.getDocument();
    			timestamp = packet.getTimeStamp();
                if(msdt.equals("msdt.textoperation.xsd")){
        			System.out.println("event is a textoperation");
        		}
        		if(msdt.equals("msdt.linediff.xsd")){
        			docuname = ECGParser.getSingleNodeValue("documentname", document);
        			System.out.println("event is a linediff");
        		}
    	    	try{
	    			user = ECGParser.getSingleNodeValue("username", document);
	    			project = ECGParser.getSingleNodeValue("projectname", document);
	    			
    			} catch (NodeException e) {
    				logger.log(Level.SEVERE, "Could not read XML string in CopyPasteChangeEpisodeRecognizer.");
    				logger.log(Level.SEVERE, e.getMessage());

    			}
    			System.out.println("**************************  packet-Infos  ************************************************");
				System.out.println("nr: " + this.nr);
				System.out.println("zustand: " + this.STATE);
				System.out.println("clonefamilyvector vor bearbeitung: " + CloneFamilyVector);
				System.out.println("msdt-typ: " + packet.getMicroSensorDataType().getName());
				if(packet.getMicroSensorDataType().getName().equalsIgnoreCase("msdt.linediff.xsd")){
					System.out.println("pakettyp: linediff");
					System.out.println("linedifftyp: " + ECGParser.getSingleNodeValue("type", packet.getDocument()));
					System.out.println("datei: " + ECGParser.getSingleNodeValue("documentname", packet.getDocument()));
					System.out.println("zeitstempel: " + packet.getTimeStamp());
				}
				if(packet.getMicroSensorDataType().getName().equalsIgnoreCase("msdt.textoperation.xsd")){
					System.out.println("pakettyp: textoperation");
					System.out.println("aktivität: " + ECGParser.getSingleNodeValue("activity",packet.getDocument()));
					System.out.println("datei: " + ECGParser.getSingleNodeValue("editorname", packet.getDocument()));
					if(ECGParser.getSingleNodeValue("activity",packet.getDocument()).equalsIgnoreCase("cut") || ECGParser.getSingleNodeValue("activity",packet.getDocument()).equalsIgnoreCase("copy")){
						System.out.println("code: " + ECGParser.getSingleNodeValue("selection", packet.getDocument()));
					}
					if(ECGParser.getSingleNodeValue("activity",packet.getDocument()).equalsIgnoreCase("paste")){
						System.out.println("code: " + ECGParser.getSingleNodeValue("clipboard", packet.getDocument()));
					}
					System.out.println("startzeile: " + Integer.parseInt(ECGParser.getSingleNodeValue("startline",packet.getDocument())));
					System.out.println("endzeile: " + Integer.parseInt(ECGParser.getSingleNodeValue("endline",packet.getDocument())));
					System.out.println("zeitstempel: " + packet.getTimeStamp());
				}
				switch (STATE.ordinal()) {
			    	//if (STATE == CopyPasteChangeEpisodeState.START) {
					case 0:
			    		if (packet.getMicroSensorDataType().getName().equalsIgnoreCase("msdt.linediff.xsd")) STATE = CopyPasteChangeEpisodeState.STOP;
			    		if (packet.getMicroSensorDataType().getName().equalsIgnoreCase("msdt.textoperation.xsd")) {
			    			if (ECGParser.getSingleNodeValue("activity",packet.getDocument()).equalsIgnoreCase("paste")) {
			    				STATE = CopyPasteChangeEpisodeState.STOP;
			    			}
			    			if (ECGParser.getSingleNodeValue("activity",packet.getDocument()).equalsIgnoreCase("cut")) {
			    				String selection = ECGParser.getSingleNodeValue("selection",packet.getDocument());
			    				if(selection != null){
			    					if(selection.length() > 50){
					    				CloneFamilyVector.add(new Clone(false,true,false,false,ECGParser.getSingleNodeValue("editorname", packet.getDocument()), 
					    						Integer.parseInt(ECGParser.getSingleNodeValue("startline",packet.getDocument())), 
					    						Integer.parseInt(ECGParser.getSingleNodeValue("endline",packet.getDocument())), 
					    						ECGParser.getSingleNodeValue("selection",packet.getDocument()),timestamp));
					    				STATE = CopyPasteChangeEpisodeState.CUT;
			    					}
			    					else STATE = CopyPasteChangeEpisodeState.STOP;
			    				}
			    				else STATE = CopyPasteChangeEpisodeState.STOP;
			    			}
			    			if (ECGParser.getSingleNodeValue("activity",packet.getDocument()).equalsIgnoreCase("copy")) {
			    				String selection = ECGParser.getSingleNodeValue("selection",packet.getDocument());
			    				if(selection != null){
			    					if(selection.length() > 50){
					    				CloneFamilyVector.add(new Clone(true,false,false,false,ECGParser.getSingleNodeValue("editorname", packet.getDocument()), 
					    						Integer.parseInt(ECGParser.getSingleNodeValue("startline",packet.getDocument())), 
					    						Integer.parseInt(ECGParser.getSingleNodeValue("endline",packet.getDocument())), 
					    						ECGParser.getSingleNodeValue("selection",packet.getDocument()),timestamp));
					    				STATE = CopyPasteChangeEpisodeState.COPY;
			    					}
			    					else STATE = CopyPasteChangeEpisodeState.STOP;
			    				}
			    				else STATE = CopyPasteChangeEpisodeState.STOP;
			    			}
			    		}
			    		break;
			    	//}
			    	//if (STATE == CopyPasteChangeEpisodeState.COPY) {
					case 1:
			    		if (packet.getMicroSensorDataType().getName().equalsIgnoreCase("msdt.textoperation.xsd")) {
			    			if (ECGParser.getSingleNodeValue("activity",packet.getDocument()).equalsIgnoreCase("paste")) {
			    				if (CloneFamilyVector.firstElement().getEventCode().equalsIgnoreCase(ECGParser.getSingleNodeValue("clipboard",packet.getDocument()))) {
			    					CloneFamilyVector.add(new Clone(false,false,false,true,ECGParser.getSingleNodeValue("editorname", packet.getDocument()), 
			    							Integer.parseInt(ECGParser.getSingleNodeValue("startline",packet.getDocument())), 
			    							Integer.parseInt(ECGParser.getSingleNodeValue("endline",packet.getDocument())), 
			    							ECGParser.getSingleNodeValue("clipboard",packet.getDocument()),timestamp));
			    					//paste_timestamp = packet.getTimeStamp();
			    					STATE = CopyPasteChangeEpisodeState.PASTE;
			    				}
			    			}
			    			if (ECGParser.getSingleNodeValue("activity",packet.getDocument()).equalsIgnoreCase("cut")) {
			    				STATE = CopyPasteChangeEpisodeState.STOP;
			    			}
			    			if (ECGParser.getSingleNodeValue("activity",packet.getDocument()).equalsIgnoreCase("copy")) {
			    				STATE = CopyPasteChangeEpisodeState.STOP;
			    			}
			    		}
			    		if (packet.getMicroSensorDataType().getName().equalsIgnoreCase("msdt.linediff.xsd")) {
			    			/*
			    			 * Wir müssen für jedes Clone im CloneFamilyVector immer den Dateinamen mit dem Dateinamen des Microsensortyps linediff vergleichen.
			    			 * Im COPY Zustand ex. nur ein Clone. Daher muss die Eventliste im CloneFamilyVector nicht iteriert werden.
			    			 */
			    			if (ECGParser.getSingleNodeValue("documentname", packet.getDocument()).equalsIgnoreCase(CloneFamilyVector.firstElement().getEventFilename())) {
			    				java.util.List<BlockChange> blockChanges = BlockChange.parseLineDiffsEvent(new DummyText(),packet);
								int blockchanges = 0;
								int linenumber = 0;
								boolean inside = false;
								for (BlockChange bc: blockChanges) {
									blockchanges++;
									linenumber = bc.getBlockStart();
									for (LineChange lc: bc.getLineChanges()) {
										if (!inside) {
											if (lc.isChange()) {inside = CloneFamilyVector.firstElement().changeLine(linenumber,lc.getContents());}
											if (lc.isDeletion()) {inside = CloneFamilyVector.firstElement().deleteLine(linenumber);}
											if (lc.isInsertion()) {inside = CloneFamilyVector.firstElement().insertLine(linenumber,lc.getContents());}
											linenumber++;
										}
					    			}
								}
								CloneFamilyVector.firstElement().clearDeletedLines();
								if(CloneFamilyVector.firstElement().getEventCodeStartline() > CloneFamilyVector.firstElement().getEventCodeEndline()){
									System.out.println("#############  filename: " + CloneFamilyVector.firstElement().getEventFilename());
									System.out.println("#############  code: " + CloneFamilyVector.firstElement().getEventCode() + " ###########");
									System.out.println();
									CloneFamilyVector.firstElement().setDeleted(true);
									STATE = CopyPasteChangeEpisodeState.STOP;
								}
								if (inside) STATE = CopyPasteChangeEpisodeState.STOP;
			    			}
			    		}
			    		break;
			    	//}
			    	//if (STATE == CopyPasteChangeEpisodeState.CUT) {
					case 2:
			    		/*
			    		 * ACHTUNG: Ist ein Recognizer im CUT-Zustand, ex. der Textblock nicht mehr in der zugehörigen Datei, sondern
			    		 * nur noch im Clipboard. Folglich muss ein Recognizer im CUT-Zustand keine linediffs beachten.
			    		 * Alle anderen Recognizer müssen linediffs beachten, da deren beobachtete Textblöcke noch in Datein vorhanden sind.
			    		 */
			    		if (packet.getMicroSensorDataType().getName().equalsIgnoreCase("msdt.textoperation.xsd")) {
			    			if (ECGParser.getSingleNodeValue("activity",packet.getDocument()).equalsIgnoreCase("paste")) {
			    				if (CloneFamilyVector.firstElement().getEventCode().equalsIgnoreCase(ECGParser.getSingleNodeValue("clipboard",packet.getDocument()))) {
			    					CloneFamilyVector.setElementAt(new Clone(false,false,false,true,ECGParser.getSingleNodeValue("editorname", packet.getDocument()), 
			    							Integer.parseInt(ECGParser.getSingleNodeValue("startline",packet.getDocument())), 
			    							Integer.parseInt(ECGParser.getSingleNodeValue("endline",packet.getDocument())), 
			    							ECGParser.getSingleNodeValue("clipboard",packet.getDocument()),timestamp),0);
			    					//paste_timestamp = packet.getTimeStamp();
			    					STATE = CopyPasteChangeEpisodeState.CUTPASTE;
			    				}
			    			}
			    			if (ECGParser.getSingleNodeValue("activity",packet.getDocument()).equalsIgnoreCase("cut")) {
			    				STATE = CopyPasteChangeEpisodeState.STOP;
			    			}
			    			if (ECGParser.getSingleNodeValue("activity",packet.getDocument()).equalsIgnoreCase("copy")) {
			    				STATE = CopyPasteChangeEpisodeState.STOP;
			    			}
			    		}
			    		break;
			    	//}
			    	//if (STATE == CopyPasteChangeEpisodeState.CUTPASTE) {
					case 3:
			    		/*
			    		 * ACHTUNG lindediffs:
			    		 * 0) Das direkt auf den Pastevorgang folgende linediff entspricht diesem Pastevorgang und drückt ihn lediglich in Zeileninserts aus.
			    		 *    Das einzige Clone im CloneFamilyVector ist in diesem Zustand das CUTPASTE-Clone.
			    		 * 1) Wir wecheln also lediglich in den nächsten Zustand bei Erhalt des ersten linediffs, um dieses hier nicht abzuarbeiten.
			    		 * 2) Für das Clone im CloneFamilyVector die Datei vergleichen
			    		*/
			    		if (packet.getMicroSensorDataType().getName().equalsIgnoreCase("msdt.textoperation.xsd")) {
			    			if (ECGParser.getSingleNodeValue("activity",packet.getDocument()).equalsIgnoreCase("paste")) {
			    				if (CloneFamilyVector.firstElement().getEventCode().equalsIgnoreCase(ECGParser.getSingleNodeValue("clipboard",packet.getDocument()))) {
			    					CloneFamilyVector.add(new Clone(false,false,false,true,ECGParser.getSingleNodeValue("editorname", packet.getDocument()), 
			    							Integer.parseInt(ECGParser.getSingleNodeValue("startline",packet.getDocument())), 
			    							Integer.parseInt(ECGParser.getSingleNodeValue("endline",packet.getDocument())), 
			    							ECGParser.getSingleNodeValue("clipboard",packet.getDocument()),timestamp));
			    					//paste_timestamp = packet.getTimeStamp();
			    					STATE = CopyPasteChangeEpisodeState.PASTE;
			    				}
			    			}
			    			if (ECGParser.getSingleNodeValue("activity",packet.getDocument()).equalsIgnoreCase("cut")) {
			    				STATE = CopyPasteChangeEpisodeState.STOP;
			    			}
			    			if (ECGParser.getSingleNodeValue("activity",packet.getDocument()).equalsIgnoreCase("copy")) {
			    				STATE = CopyPasteChangeEpisodeState.STOP;
			    			}
			    		}
			    		// We get a linediff
						if (packet.getMicroSensorDataType().getName().equalsIgnoreCase("msdt.linediff.xsd")) {
			    			/*
			    			 * Wir müssen für jedes Clone im CloneFamilyVector immer den Dateinamen mit dem Dateinamen des Microsensortyps linediff vergleichen.
			    			 * Im SECOND_LINEDIFF_AFTER_CUTPASTE Zustand ex. nur ein Clone. Daher muss die Eventliste im CloneFamilyVector nicht iteriert werden.
			    			 */
			    			if (ECGParser.getSingleNodeValue("documentname", packet.getDocument()).equalsIgnoreCase(CloneFamilyVector.firstElement().getEventFilename())
			    					& !CloneFamilyVector.firstElement().getEventStartDate().equals(timestamp)) {
			    				java.util.List<BlockChange> blockChanges = BlockChange.parseLineDiffsEvent(new DummyText(),packet);
								int blockchanges = 0;
								int linenumber = 0;
								boolean inside = false;
								for (BlockChange bc: blockChanges) {
									blockchanges++;
									linenumber = bc.getBlockStart();
									for (LineChange lc: bc.getLineChanges()) {
										if (!inside) {
											if (lc.isChange()) {inside = CloneFamilyVector.firstElement().changeLine(linenumber,lc.getContents());}
											if (lc.isDeletion()) {inside = CloneFamilyVector.firstElement().deleteLine(linenumber);}
											if (lc.isInsertion()) {inside = CloneFamilyVector.firstElement().insertLine(linenumber,lc.getContents());}
											linenumber++;
										}
					    			}
								}
								CloneFamilyVector.firstElement().clearDeletedLines();
								/*if(CloneFamilyVector.firstElement().getEventCodeStartline() > CloneFamilyVector.firstElement().getEventCodeEndline()){
									System.out.println("#############  filename: " + CloneFamilyVector.firstElement().getEventFilename());
									System.out.println("#############  code: " + CloneFamilyVector.firstElement().getEventCode() + " ###########");
									System.out.println();
									CloneFamilyVector.firstElement().setDeleted(true);
									STATE = CopyPasteChangeEpisodeState.STOP;
								}*/
								if (inside) STATE = CopyPasteChangeEpisodeState.STOP;
			    			}
			    		}
			    	//}
			    	//if (STATE == CopyPasteChangeEpisodeState.PASTE) {
					case 4:
			    		//boolean event_inside_change = false;
			    		//Clone event = null;
			    		/*
		    			 * 1. Das erste linediff nach dem Paste betrifft immer genau die Paste Aktion, die diesen Automaten in diesen Paste-Zustand
		    			 * überführt hat. Dieses linediff in diesem Zustand kann alle Events bis auf das letzte Paste-Clone betreffen (Codeänderung, Zeilenverschoben).
		    			 * Wir wechseln nach dem einen linediff sofort in den SECOND_LINDEDIFF_AFTER_PASTE-Zustand, der dann für alle Events alle Signale bearbeiten darf.
		    			 * 2. textoperations können in diesem Zustand nicht auftreten, da nach der PASTE - Aktion, die in diesen Zustand führte, sofort
		    			 * das zugehörige linediff folgt.
		    			 */
						if (packet.getMicroSensorDataType().getName().equalsIgnoreCase("msdt.textoperation.xsd")) {
			    			if (ECGParser.getSingleNodeValue("activity",packet.getDocument()).equalsIgnoreCase("paste")) {
			    				if (CloneFamilyVector.firstElement().getEventCode().equalsIgnoreCase(ECGParser.getSingleNodeValue("clipboard",packet.getDocument()))) {
			    					CloneFamilyVector.add(new Clone(false,false,false,true,ECGParser.getSingleNodeValue("editorname", packet.getDocument()), 
			    							Integer.parseInt(ECGParser.getSingleNodeValue("startline",packet.getDocument())), 
			    							Integer.parseInt(ECGParser.getSingleNodeValue("endline",packet.getDocument())), 
			    							ECGParser.getSingleNodeValue("clipboard",packet.getDocument()),timestamp));
			    				}
			    			}
			    		}
			    		if (packet.getMicroSensorDataType().getName().equalsIgnoreCase("msdt.linediff.xsd")) {
			    			validEventPacketList = null;
			    			ListIterator<Clone> eventlist = CloneFamilyVector.listIterator();
			    			while (eventlist.hasNext()) {
			    				event = eventlist.next();
		    					if (!event.getEventStartDate().equals(timestamp)) {
			    					event_inside_change = false;
				    				if (ECGParser.getSingleNodeValue("documentname", packet.getDocument()).equalsIgnoreCase(event.getEventFilename())) {
					    				java.util.List<BlockChange> blockChanges = BlockChange.parseLineDiffsEvent(new DummyText(),packet);
										int blockchanges = 0;
										int linenumber = 0;
										boolean inside = false;
										for (BlockChange bc: blockChanges) {
											blockchanges++;
											linenumber = bc.getBlockStart();
											for (LineChange lc: bc.getLineChanges()) {
												if (lc.isChange()) {inside = event.changeLine(linenumber,lc.getContents());}
												if (lc.isDeletion()) {inside = event.deleteLine(linenumber);}
												if (lc.isInsertion()) {inside = event.insertLine(linenumber,lc.getContents());}
												if (inside) event_inside_change = true;
												linenumber++;
											}
										}
										event.clearDeletedLines();
										if(event.getEventCodeStartline() > event.getEventCodeEndline()){
											System.out.println("#############  filename: " + event.getEventFilename());
											System.out.println("#############  code: " + event.getEventCode() + " ###########");
											System.out.println();
											event.setDeleted(true);
										}
										/*****************************************************************
										* In den Change-Zustand wechseln und eine EPISODE schmeissen, da
										* mindestens ein Clone-Code geändert wurde !!!
										*****************************************************************/
										if(/*CloneFamilyVector.size() >= 2*/!countDeletedEvents()){
											//generateEpsiode
											if (event_inside_change) {
												evaluateStringDiff(event);
												System.out.println("aktueller event wird beobachtet: " + event.isValid());
												STATE = CopyPasteChangeEpisodeState.CHANGE;
												System.out.println("cpcwarning time: " + packet.getTimeStamp());
												if(CloneFamilyVector != null){
													validEventPacketList = new ArrayList();
													for(int i = 0; i < CloneFamilyVector.size(); i++){
														Clone episodeEvent = CloneFamilyVector.get(i);
														System.out.println("cpcwarning time: " + packet.getTimeStamp());
														if(episodeEvent != null){
															String file = "";
															int startline = 0;
															int endline = 0;
															if(user != null && project != null && timestamp != null && docuname != null){
																if(episodeEvent.isDeleted()== false && episodeEvent.isValid() == true){
																	file = episodeEvent.getEventFilename();
																	startline = episodeEvent.getEventCodeStartline()+1;
																	String startLine = Integer.toString(startline);
																	endline = episodeEvent.getEventCodeEndline()+1;
																	String endLine = Integer.toString(endline);
																	validEvent = generateEpisode("msdt.cpcwarning.xsd", user, project, docuname, timestamp, startLine,endLine);
																	validEventPacketList.add(validEvent);
																	System.out.println("event not deleted and valid");
																	System.out.println("file: " + file);
																	System.out.println("startline: " + startline + " endline: " + endline);
																}
															}
														}
													}
												}
											}
				    					}
										else STATE = CopyPasteChangeEpisodeState.STOP;
					    			}
			    				}
			    			}
			    			/* Wurden nur Zeilen verschoben, ohne einen EventCode (inside) zu verändern, wechselt der Automat in den nächsten Paste-Zustand
			    			 * 'SECOND_LINDEDIFF_AFTER_PASTE', der linediffs für alle Events beachtet. Das linediff für die letzte Paste-Aktion wurde dann
			    			 * ohne Änderung vorhandener EventCodes abgearbeitet.
			    			 * Wurde jedoch ein EventCode (inside) durch das die letzte Paste-Aktion beschreibende linediff geändert, muss der Automat
			    			 * sofort in den Change-Zustand wechseln und eine CPCWarning auslösen.
			    			 */
			    		}
			    		break;
			    	//}
			    	//if (STATE == CopyPasteChangeEpisodeState.CHANGE) {
					case 5:
			    		/**
			    		 * 1. Ein Clone kann wie in der Klasse Clone.java beschrieben seinen Zustand auf gelöscht (deleted) setzen.
			    		 * 2. Ein Clone ist gelöscht, falls sein gesamter EventCode gelöscht wurde.
			    		 * 3. Ein Automat reagiert im Change-Zustand nicht mehr auf Textoperationen (textoperation).
			    		 * 4. Ein Automat reagiert jedoch auf ein linediff, das einen Clone in seiner Liste beobachteter Codecs betrifft.
			    		 * n. Nur ein gelöschter Clone (beobachtetes Codefragment) darf vom Recognizer nicht mehr beachtet werden.
			    		 */
			    		//Clone event = null;
			    		//boolean event_inside_change = false;
			    		if (packet.getMicroSensorDataType().getName().equalsIgnoreCase("msdt.linediff.xsd")) {
			    			validEventPacketList = null;
			    			ListIterator<Clone> eventlist = CloneFamilyVector.listIterator();
			    			while (eventlist.hasNext()) {
			    				event_inside_change = false;
			    				event = eventlist.next();
			    				if (!event.isDeleted()) {
			    					if (ECGParser.getSingleNodeValue("documentname", packet.getDocument()).equalsIgnoreCase(event.getEventFilename())) {
					    				java.util.List<BlockChange> blockChanges = BlockChange.parseLineDiffsEvent(new DummyText(),packet);
										int blockchanges = 0;
										int linenumber = 0;
										boolean inside = false;
										for (BlockChange bc: blockChanges) {
											blockchanges++;
											linenumber = bc.getBlockStart();
											BlockChangeType blockType = bc.getBlockType(); 
											if(blockType == BlockChangeType.DELETED){
												System.out.println("BlockChangeType.DELETED");
											}
											if(blockType == BlockChangeType.CHANGED){
												System.out.println("BlockChangeType.CHANGED");
											}
											if(blockType == BlockChangeType.INSERTED){
												System.out.println("BlockChangeType.INSERTED");
											}
											for (LineChange lc: bc.getLineChanges()) {
												if (lc.isChange()) {inside = event.changeLine(linenumber,lc.getContents());}
												if (lc.isDeletion()) {inside = event.deleteLine(linenumber);}
												if (lc.isInsertion()) {inside = event.insertLine(linenumber,lc.getContents());}
												if (inside) event_inside_change = true;
												linenumber++;
											}
										}
										event.clearDeletedLines();
										if(event.getEventCodeStartline() > event.getEventCodeEndline()){
											System.out.println("#############  filename: " + event.getEventFilename());
											System.out.println("#############  code: " + event.getEventCode() + " ###########");
											System.out.println();
											event.setDeleted(true);
										}
										/*****************************************************************
										* In den Change-Zustand wechseln und eine EPISODE schmeissen, da
										* mindestens ein Clone-Code geändert wurde !!!
										*****************************************************************/
										//generateEpsiode
										if(/*CloneFamilyVector.size() >= 2*/!countDeletedEvents()){
											if (event_inside_change) {
												evaluateStringDiff(event);
												System.out.println("aktueller event wird beobachtet: " + event.isValid());
												STATE = CopyPasteChangeEpisodeState.CHANGE;
												if(CloneFamilyVector != null){
													validEventPacketList = new ArrayList();
													System.out.println("cpcwarning time: " + packet.getTimeStamp());
													for(int i = 0; i < CloneFamilyVector.size(); i++){
														Clone episodeEvent = CloneFamilyVector.get(i);
														if(episodeEvent != null){
															String file = "";
															int startline = 0;
															int endline = 0;
															if(user != null && project != null && timestamp != null && docuname != null){
																if(episodeEvent.isDeleted()== false && episodeEvent.isValid() == true){
																	file = episodeEvent.getEventFilename();
																	startline = episodeEvent.getEventCodeStartline()+1;
																	String startLine = Integer.toString(startline);
																	endline = episodeEvent.getEventCodeEndline()+1;
																	String endLine = Integer.toString(endline);
																	validEvent = generateEpisode("msdt.cpcwarning.xsd", user, project, docuname, timestamp,startLine,endLine);
																	validEventPacketList.add(validEvent);
																	System.out.println("event not deleted and valid");
																	System.out.println("file: " + file);
																	System.out.println("startline: " + startline + " endline: " + endline);												
																}
															}
														}
													}
												}
											}
										}
										else STATE = CopyPasteChangeEpisodeState.STOP;
					    			}
			    				}
			    			}
			    		}
			    		break;
			    	default:
			    		break;
				} // switch
		    	System.out.println("nr: " + this.nr);
				System.out.println("zustand: " + this.STATE);
				System.out.println("clonefamilyvector nach bearbeitung: " + CloneFamilyVector);
		 	} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}//END if(msdt.equals("msdt.textoperation.xsd") || msdt.equals("msdt.linediff.xsd")) 
    	return validEventPacketList;
    }
    
    /**
     * Helper method to create a new episode event
     * 
     * @param msdt type of event
     * @param version
     * @param creator
	 * @param username name of user
	 * @param projectname name of project
	 * @param documnetname name of java class
	 * @param startline startline of clone 
	 * @param endline endline of clone
	 * @param time stamp
	 * @return
	 */
	private ValidEventPacket generateEpisode(String msdt, String username, String projectname, String documentname, Date changetimestamp, 
																											String startline, String endline) {

		ValidEventPacket event = null;
		String timeStamp = changetimestamp.toString();
        cpcwarning_projectname.setTextContent(projectname);
        cpcwarning_username.setTextContent(username);
        cpcwarning_documentnames.setTextContent(documentname);
        cpcwarning_startline.setTextContent(startline);
        cpcwarning_endline.setTextContent(endline);
        event = ECGWriter.createValidEventPacket("msdt.cpcwarning.xsd", changetimestamp, msdt_cpcwarning_doc);
		return event;

	}
    
    
    class DummyText implements IText {}
    
    private class StringDifferMeasurement {

  	  private String firstString = null;
  	  private String secondString = null;
  	  private static final double STRING_DIFFER_MEASUREMENT = 100.0;
  	  
  	  
  	  public StringDifferMeasurement(String first, String second){
  	  	this.firstString = first;
  		this.secondString = second;
  	  }
  	  
  	  
  	  //****************************
  	  // Get minimum of three values
  	  //****************************

  	  private int Minimum (int a, int b, int c) {
  		  int mi;

  		  mi = a;
  		  if (b < mi) {
  	     	 mi = b;
  		  }
  		  if (c < mi) {
  	     	 mi = c;
  		  }
  		  return mi;
  	  }

  	  //*****************************
  	  // Compute Levenshtein distance
  	  //*****************************

  	  private double LevenshteinDistance (String s, String t) {
  		  int d[][]; // matrix
  		  int n; // length of s
  	  	  int m; // length of t
  	  	  int i; // iterates through s
  	  	  int j; // iterates through t
  	  	  char s_i; // ith character of s
  	  	  char t_j; // jth character of t
  	  	  int cost; // cost

  	  	  // Step 1
  	  	  n = s.length ();
  	  	  m = t.length ();
  		 System.out.println("The length of the first string is : " + n);
  		 System.out.println("--------=-------- second string is: " + m);
  	  	  	  	  
  	  	  if (n == 0) { return m; }
  	  	  if (m == 0) { return n; }
  	  	  
  	  	  d = new int[n+1][m+1];

  	  	  // Step 2
  	  	  for (i = 0; i <= n; i++) { d[i][0] = i; }

  	  	  for (j = 0; j <= m; j++) { d[0][j] = j; }

  	  	  // Step 3
  	  	  for (i = 1; i <= n; i++) { 
  	  		  s_i = s.charAt (i - 1); 
  	  		  //System.out.println("Das " + i + ". Zeichen im erten String: " + s_i);
  	  	  	// Step 4
  	    	for (j = 1; j <= m; j++) { 
  	    		t_j = t.charAt (j - 1); 
  	    		//System.out.println("Das " + i + ". Zeichen im zweiten String: " + t_j);	      
  	    		// Step 5
  	    		if (s_i == t_j) { 
  	    			cost = 0;
  	    		}
  	    		else { 
  	    			cost = 1; 
  	    		}
  	    		/**else if(Character.isWhitespace(s_i)|| Character.isWhitespace(t_j)){
  	    			cost = 0;
  	    		}*/
  				//System.out.println("cost: " + cost);
  				//System.out.println("i = " + i + "  j = " + j);
  				//System.out.println();
  				int first = d[i-1][j]+1;
  				int second = d[i][j-1]+1;
  				int third = d[i-1][j-1] + cost;
  	    		// Step 6
  	    		d[i][j] = Minimum (d[i-1][j]+1, d[i][j-1]+1, d[i-1][j-1] + cost);
  				//System.out.println("Minimum: d[i-1][j]+1: " + first + ", d[i][j-1]+1: " + second + ", d[i-1][j-1]+ cost: " + third);
  				//System.out.println("gesetzter Wert in Matrix:" + d[i][j]);
  				//System.out.println("The length of the first string is : " + n);
  				//System.out.println("--------=-------- second string is: " + m);
  	    	}
  	    }
  	  	int percentValue = d[n][m];
  	  	double myDouble  = stringDifferMeasurement(n,percentValue);
  	  	System.out.println("editieroperationen: " + percentValue);
  	  	if(myDouble > STRING_DIFFER_MEASUREMENT){
  	  		  System.out.println("STRING_DIFFER_MEASUREMENT > 100.0 %: " + myDouble);
  	    }
  	    else{
  	  		System.out.println("STRING_DIFFER_MEASUREMENT < 100.0%: " + myDouble);
  	    }
  	    // Step 7
  	    return myDouble;
  	  }

  	  private double stringDifferMeasurement(int lengthFirst, int percentVal){
  		  
  		  double percentage = 0.0;
  		  String doubleValueFirst = "" + lengthFirst;
  		  double basicPercentValue = (double)Double.valueOf(doubleValueFirst);
  		  String doubleValueSecond = "" + percentVal;
  		  double  percentValue = (double)Double.valueOf(doubleValueSecond);
  		  
  		  return percentage = ((percentValue / basicPercentValue) * 100.0);
  	  }
    }
  	  
}
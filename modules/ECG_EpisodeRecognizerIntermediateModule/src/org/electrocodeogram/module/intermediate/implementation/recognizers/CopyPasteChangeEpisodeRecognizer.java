/*
 * (c) Freie Universität Berlin - AG SoftwareEngineering - 2007
 *
 *
 *  Diplomarbeit: Verfolgen von Kodekopien in Eclipse
 *  @author Sofoklis Papadopoulos
 *
 *
 */


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
 * Generell View:
 * 1. All actions while programming are described with 2 concepts
 * 		a) textoperations: CUT, COPY, PASTE
 * 		b) program code change: linediff
 * 		   Linediffs are representing a change activitiy upon program code.
 * 2.  A Recognizer has to proof if a textoperation has to be evaluated.
 * 3.  A Recognizer has to proof if a change of program code concerns one of his clonein the CloneFamliVector.
 */

public class CopyPasteChangeEpisodeRecognizer implements EpisodeRecognizer {
	/**
	 * The measure of the code difference between 2 clones.
	 */
	private static final double STRING_MEASURE = 30;
	/**
	 * The minimum length of the clones that will be observed. 
	 */
	private static final int STRING_LENGTH = 100;
	
	/**
	 * 
	 * State types for this recognizer 
	 *
	 */
	private enum CopyPasteChangeEpisodeState {
        /**
         * Ínitial state, no textoperation recognized yet.
         */
        START, /* 0 */
        /**
         * A textoperation-copy is recognized. 
         */
        COPY, /* 1 */
        /**
         * A textoperation-cut is recognized.
         */
        CUT, /* 2 */
        /**
         * After cut and first paste. The clone of the cut-activity is replaced with the paste-clone.
         */
        CUTPASTE, /* 3 */
        /**
         * A textoperation-paste is recognized after COPY or CUTPASTE. 
         */
        PASTE, /* 4 */
        /**
         * A linediff is recognized and a clones code has been changed.
         */
        CHANGE, /* 5 */
        /**
         * Final state after !(countNotDeletedClones() > 1) or 
         */
        STOP /* 6 */
    }
	
	/**
	 * Current state of episode recognizer
	 */
	private CopyPasteChangeEpisodeState STATE;
	/**
	 * The Container for the clones
	 */
	private Vector<Clone> CloneFamilyVector; 
    /**
     * The username 
     */
	private String user = null;
	/**
	 * The project name
	 */
	private String project = null;
	/**
	 *  The filename
	 */
	private String docuname = null;
	
	private static Logger logger = LogHelper
    .createLogger(EpisodeRecognizerIntermediateModule.class.getName());

    // XML Document and Elements
    private static Document msdt_cpcwarning_doc = null;
    private static Element cpcwarning_username = null;
    private static Element cpcwarning_projectname = null;
    private static Element cpcwarning_documentnames = null;
    private static Element cpcwarning_startline = null;
    private static Element cpcwarning_endline = null;
	
	public CopyPasteChangeEpisodeRecognizer() {
		//Start in start STATE
		STATE = CopyPasteChangeEpisodeState.START;
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
	
	/**
	 * @see org.electrocodeogram.module.intermediate.implementation.EpisodeRecognizer#isInInitialState()
	 */
	public boolean isInInitialState() {
		return (STATE == CopyPasteChangeEpisodeState.START);
	}
	
	/**
	 * @see org.electrocodeogram.module.intermediate.implementation.EpisodeRecognizer#isInFinalState()
	 */
	public boolean isInFinalState() {
		return (STATE == CopyPasteChangeEpisodeState.STOP);
	}
	
	/**
	 * Computes if CloneFamilyVector.get(i) has to CloneFamilyVector.get(i) a code difference of 50%
	 * @param i index of a clone in the CloneFamilyVector
	 * @param j index of a clone
	 * @return true, if difference is 50%, otherwise false
	 */
	public boolean isOverStringMeasure(int i, int j){
		double percentResult = 0.0;
		boolean isOver = false;
		if (CloneFamilyVector != null && i >= 0 && j >= 0 && i < CloneFamilyVector.size() && j < CloneFamilyVector.size() && CloneFamilyVector.get(i) != null && CloneFamilyVector.get(j) != null) {
			String cloneCode = CloneFamilyVector.get(i).getCloneCode();
			String diffEvalCloneCode = CloneFamilyVector.get(j).getCloneCode();
			StringDifferMeasurement stringdiff = new StringDifferMeasurement(cloneCode, diffEvalCloneCode); 
			percentResult = stringdiff.LevenshteinDistance(stringdiff.firstString, stringdiff.secondString);
			if(percentResult >= STRING_MEASURE) {
				return true;
			}
		}
		return isOver;
	}
	/**
	 * Validates the clones in the CloneFamilyVector. 
	 * 
	 */
	private void validateCloneFamily() {
		boolean valid = false;
		int size = CloneFamilyVector.size();
		int j = 0;
		// iterate all clones once
		for (int i = 0; i<size; i++) {
			valid = false;
			j = 0;
			// validate all clones j except i  
			while (j<size & !valid) {
				if (j!=i) {
					if (!isOverStringMeasure(i,j)) {
						CloneFamilyVector.get(i).setValid(valid=true);
					}
				}
				j++;
			}
			if(valid==false){
				CloneFamilyVector.get(i).setValid(false);
			}
		}
	}
	/**
	 * Count not deleted clones in the CloneFamilyVector.
	 * @return The number of not deleted clones in the CloneFamilyVector.  
	 */	
	private int countNotDeletedClones() {
		int notDeletedCounter = 0;
		int vectorSize = CloneFamilyVector.size();
		for(int i = 0; i < vectorSize; i++){
			if(!CloneFamilyVector.get(i).isDeleted()){
				notDeletedCounter++;
			}	
		}
		return notDeletedCounter;
	}
	/**
	 * Count valid and not deleted clones in the CloneFamilyVector
	 * @return int
	 */
	private int countValidAndNotDeletedClones() {
		int count = 0;
		int vectorSize = CloneFamilyVector.size();
		for(int i = 0; i < vectorSize; i++){
			if(!CloneFamilyVector.get(i).isDeleted() & CloneFamilyVector.get(i).isValid()){
				count++;
			}	
		}
		return count;
	}
	
	 /**
     * Helper method to create a new episode clone
     * 
     * @param msdt type of event
	 * @param username name of user
	 * @param projectname name of project
	 * @param documnetname name of java class
	 * @param startline startline of clone 
	 * @param endline endline of clone
	 * @param time stamp
	 * @return
	 */
	private ValidEventPacket generateEpisode(String msdt, String username, String projectname, 
			                                 String documentname, Date changetimestamp,String startline, String endline) {
		ValidEventPacket event = null;
		String timeStamp = changetimestamp.toString();
		cpcwarning_username.setTextContent(username);
        cpcwarning_projectname.setTextContent(projectname);
        cpcwarning_documentnames.setTextContent(documentname);
        cpcwarning_startline.setTextContent(startline);
        cpcwarning_endline.setTextContent(endline);
        event = ECGWriter.createValidEventPacket("msdt.cpcwarning.xsd", changetimestamp, msdt_cpcwarning_doc);
		return event;
	}
	
	/**
     * @see org.electrocodeogram.module.intermediate.implementation.IntermediateModule#analyse(org.electrocodeogram.event.ValidEventPacket)
     * 
	 * @param packet
	 * @return A Collection of ValidEventPackets
	 */
	public Collection<ValidEventPacket> analyse(ValidEventPacket packet, long minDuration) {
    	ArrayList<ValidEventPacket> validEventPacketList = null;
    	ValidEventPacket validEvent = null;
    	Clone clone = null;
    	Document document = null;
    	Date timestamp = null;
    	String msdt = packet.getMicroSensorDataType().getName();
    	if(msdt.equals("msdt.textoperation.xsd") || msdt.equals("msdt.linediff.xsd")){
    		try {
    			document = packet.getDocument();
    			timestamp = packet.getTimeStamp();
        		if(msdt.equals("msdt.linediff.xsd")){
        			docuname = ECGParser.getSingleNodeValue("documentname", document);
        		}
    	    	try{
	    			user = ECGParser.getSingleNodeValue("username", document);
	    			project = ECGParser.getSingleNodeValue("projectname", document);
	    			
    			} catch (NodeException e) {
    				logger.log(Level.SEVERE, "Could not read XML string in CopyPasteChangeEpisodeRecognizer.");
    				logger.log(Level.SEVERE, e.getMessage());
    			}
				switch (STATE.ordinal()) {
			    	//(STATE == CopyPasteChangeEpisodeState.START) {
					case 0:
			    		if (packet.getMicroSensorDataType().getName().equalsIgnoreCase("msdt.linediff.xsd")) STATE = CopyPasteChangeEpisodeState.STOP;
			    		if (packet.getMicroSensorDataType().getName().equalsIgnoreCase("msdt.textoperation.xsd")) {
			    			if (ECGParser.getSingleNodeValue("activity",packet.getDocument()).equalsIgnoreCase("paste")) {
			    				STATE = CopyPasteChangeEpisodeState.STOP;
			    			}
			    			if (ECGParser.getSingleNodeValue("activity",packet.getDocument()).equalsIgnoreCase("cut")) {
			    				String selection = ECGParser.getSingleNodeValue("selection",packet.getDocument());
			    				if(selection != null){
			    					if(selection.length() > STRING_LENGTH){
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
			    					if(selection.length() > STRING_LENGTH){
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
			    	//(STATE == CopyPasteChangeEpisodeState.COPY) {
					case 1:
			    		if (packet.getMicroSensorDataType().getName().equalsIgnoreCase("msdt.textoperation.xsd")) {
			    			if (ECGParser.getSingleNodeValue("activity",packet.getDocument()).equalsIgnoreCase("paste")) {
			    				if (CloneFamilyVector.firstElement().getCloneCode().equalsIgnoreCase(ECGParser.getSingleNodeValue("clipboard",packet.getDocument()))) {
			    					CloneFamilyVector.add(new Clone(false,false,false,true,ECGParser.getSingleNodeValue("editorname", packet.getDocument()), 
			    							Integer.parseInt(ECGParser.getSingleNodeValue("startline",packet.getDocument())), 
			    							Integer.parseInt(ECGParser.getSingleNodeValue("endline",packet.getDocument())), 
			    							ECGParser.getSingleNodeValue("clipboard",packet.getDocument()),timestamp));
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
			    			//linediffs have to be considered becaus they could change the clone
			    			if (ECGParser.getSingleNodeValue("documentname", packet.getDocument()).equalsIgnoreCase(CloneFamilyVector.firstElement().getCloneFilename())) {
			    				//get blockchanges for this programcode change
			    				java.util.List<BlockChange> blockChanges = BlockChange.parseLineDiffsEvent(new DummyText(),packet);
								int blockchanges = 0;
								int linenumber = 0;
								boolean inside = false;
								for (BlockChange bc: blockChanges) {
									blockchanges++;
									linenumber = bc.getBlockStart();
									//for each linechange get the type and do the operations relevant to the type of linechange
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
								//if copied clone was changed set stop this recognizer
								if (CloneFamilyVector.firstElement().isDeleted() || inside) {
									STATE = CopyPasteChangeEpisodeState.STOP;
								}
							}
			    		}
			    		break;
			    	//(STATE == CopyPasteChangeEpisodeState.CUT) {
					case 2:
			    		/*
			    		 * if a recognizer is in CUT-STATE following linediffs after cut-activity are not of interest, because the code was cutted off.
			    		 * all other recognizers have to check the linediffs
			    		 */
			    		if (packet.getMicroSensorDataType().getName().equalsIgnoreCase("msdt.textoperation.xsd")) {
			    			if (ECGParser.getSingleNodeValue("activity",packet.getDocument()).equalsIgnoreCase("paste")) {
			    				if (CloneFamilyVector.firstElement().getCloneCode().equalsIgnoreCase(ECGParser.getSingleNodeValue("clipboard",packet.getDocument()))) {
			    					CloneFamilyVector.setElementAt(new Clone(false,false,false,true,ECGParser.getSingleNodeValue("editorname", packet.getDocument()), 
			    							Integer.parseInt(ECGParser.getSingleNodeValue("startline",packet.getDocument())), 
			    							Integer.parseInt(ECGParser.getSingleNodeValue("endline",packet.getDocument())), 
			    							ECGParser.getSingleNodeValue("clipboard",packet.getDocument()),timestamp),0);
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
			    	//(STATE == CopyPasteChangeEpisodeState.CUTPASTE) {
					case 3:
			    		if (packet.getMicroSensorDataType().getName().equalsIgnoreCase("msdt.textoperation.xsd")) {
			    			if (ECGParser.getSingleNodeValue("activity",packet.getDocument()).equalsIgnoreCase("paste")) {
			    				if (CloneFamilyVector.firstElement().getCloneCode().equalsIgnoreCase(ECGParser.getSingleNodeValue("clipboard",packet.getDocument()))) {
			    					CloneFamilyVector.add(new Clone(false,false,false,true,ECGParser.getSingleNodeValue("editorname", packet.getDocument()), 
			    							Integer.parseInt(ECGParser.getSingleNodeValue("startline",packet.getDocument())), 
			    							Integer.parseInt(ECGParser.getSingleNodeValue("endline",packet.getDocument())), 
			    							ECGParser.getSingleNodeValue("clipboard",packet.getDocument()),timestamp));
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
			    			//if the timestamp of the linediff equals the timestamp of the previous textoperation ignore linediff
			    			if (ECGParser.getSingleNodeValue("documentname", packet.getDocument()).equalsIgnoreCase(CloneFamilyVector.firstElement().getCloneFilename())
			    					& !CloneFamilyVector.firstElement().getCloneStartDate().equals(timestamp)) {
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
								//if pasted clone after cut is changed so stop recognizer
								if (CloneFamilyVector.firstElement().isDeleted() || inside) {
									STATE = CopyPasteChangeEpisodeState.STOP;
								}
							}
			    		}
						break;
			    	//(STATE == CopyPasteChangeEpisodeState.PASTE) {
					case 4:
						if (packet.getMicroSensorDataType().getName().equalsIgnoreCase("msdt.textoperation.xsd")) {
			    			if (ECGParser.getSingleNodeValue("activity",packet.getDocument()).equalsIgnoreCase("paste")) {
			    				if (CloneFamilyVector.firstElement().getCloneCode().equalsIgnoreCase(ECGParser.getSingleNodeValue("clipboard",packet.getDocument()))) {
			    					CloneFamilyVector.add(new Clone(false,false,false,true,ECGParser.getSingleNodeValue("editorname", packet.getDocument()), 
			    							Integer.parseInt(ECGParser.getSingleNodeValue("startline",packet.getDocument())), 
			    							Integer.parseInt(ECGParser.getSingleNodeValue("endline",packet.getDocument())), 
			    							ECGParser.getSingleNodeValue("clipboard",packet.getDocument()),timestamp));
			    				}
			    			}
			    		}
			    		if (packet.getMicroSensorDataType().getName().equalsIgnoreCase("msdt.linediff.xsd")) {
			    			boolean valid_clone = false;
			    			ListIterator<Clone> eventlist = CloneFamilyVector.listIterator();
			    			while (eventlist.hasNext()) {
			    				clone = eventlist.next();
			    				valid_clone = clone.isValid();
			    				//if the timestamp of the linediff equals the timestamp of the previous textoperation ignore linediff
		    					if (!clone.getCloneStartDate().equals(timestamp) & !clone.isDeleted()) {
			    					if (ECGParser.getSingleNodeValue("documentname", packet.getDocument()).equalsIgnoreCase(clone.getCloneFilename())) {
					    				java.util.List<BlockChange> blockChanges = BlockChange.parseLineDiffsEvent(new DummyText(),packet);
										int blockchanges = 0;
										int linenumber = 0;
										boolean inside = false;
										for (BlockChange bc: blockChanges) {
											blockchanges++;
											linenumber = bc.getBlockStart();
											for (LineChange lc: bc.getLineChanges()) {
												if (lc.isChange()) {inside = clone.changeLine(linenumber,lc.getContents());}
												if (lc.isDeletion()) {inside = clone.deleteLine(linenumber);}
												if (lc.isInsertion()) {inside = clone.insertLine(linenumber,lc.getContents());}
												if (inside) STATE = CopyPasteChangeEpisodeState.CHANGE;
												linenumber++;
											}
										}
										clone.clearDeletedLines();
										if (valid_clone & inside & !clone.isDeleted() & countValidAndNotDeletedClones() > 1) {
											//generateEpisode because change was inside of a clone
											if(CloneFamilyVector != null){
												validEventPacketList = new ArrayList();
												for(int i = 0; i < CloneFamilyVector.size(); i++){
													Clone iclone = CloneFamilyVector.get(i);
													if(iclone != null){
														String file = "";
														int startline = 0;
														int endline = 0;
														if(user != null && project != null && timestamp != null && docuname != null){
															if (!iclone.isDeleted() & iclone.isValid() & iclone != clone){
																file = iclone.getCloneFilename();
																startline = iclone.getCloneCodeStartline()+1;
																String startLine = Integer.toString(startline);
																endline = iclone.getCloneCodeEndline()+1;
																String endLine = Integer.toString(endline);
																validEvent = generateEpisode("msdt.cpcwarning.xsd", user, project, file, timestamp, startLine,endLine);
																validEventPacketList.add(validEvent);
															}
														}
													}
												}
											}
										}
					    			}
			    				}
			    			}
			    			// validate the clonefamily after changes
			    			validateCloneFamily();
			    			//if the number of not deleted clone is < 1 stop recognizer 
			    			if	(!(countNotDeletedClones() > 1)) STATE = CopyPasteChangeEpisodeState.STOP;
			    			//if only lines are actualized remain in PASTE-STATE
			    		}
			    		break;
			    	//if (STATE == CopyPasteChangeEpisodeState.CHANGE) {
					case 5:
						boolean valid_clone = false;
						if (packet.getMicroSensorDataType().getName().equalsIgnoreCase("msdt.linediff.xsd")) {
			    			ListIterator<Clone> eventlist = CloneFamilyVector.listIterator();
			    			while (eventlist.hasNext()) {
			    				clone = eventlist.next();
			    				valid_clone = clone.isValid();
			    				if (!clone.isDeleted()) {
			    					if (ECGParser.getSingleNodeValue("documentname", packet.getDocument()).equalsIgnoreCase(clone.getCloneFilename())) {
					    				java.util.List<BlockChange> blockChanges = BlockChange.parseLineDiffsEvent(new DummyText(),packet);
										int blockchanges = 0;
										int linenumber = 0;
										boolean inside = false;
										for (BlockChange bc: blockChanges) {
											blockchanges++;
											linenumber = bc.getBlockStart();
											BlockChangeType blockType = bc.getBlockType();
											for (LineChange lc: bc.getLineChanges()) {
												if (lc.isChange()) {inside = clone.changeLine(linenumber,lc.getContents());}
												if (lc.isDeletion()) {inside = clone.deleteLine(linenumber);}
												if (lc.isInsertion()) {inside = clone.insertLine(linenumber,lc.getContents());}
												linenumber++;
											}
										}
										clone.clearDeletedLines();
										//generateEpsiode because change was inside of a clone
										if(valid_clone & inside & !clone.isDeleted() & countValidAndNotDeletedClones() > 1){
											if(CloneFamilyVector != null){
												validEventPacketList = new ArrayList();
												for(int i = 0; i < CloneFamilyVector.size(); i++){
													Clone iclone = CloneFamilyVector.get(i);
													if(iclone != null){
														String file = "";
														int startline = 0;
														int endline = 0;
														if(user != null && project != null && timestamp != null && docuname != null){
															if (!iclone.isDeleted() & iclone.isValid() & iclone != clone){
																file = iclone.getCloneFilename();
																startline = iclone.getCloneCodeStartline()+1;
																String startLine = Integer.toString(startline);
																endline = iclone.getCloneCodeEndline()+1;
																String endLine = Integer.toString(endline);
																validEvent = generateEpisode("msdt.cpcwarning.xsd", user, project, file, timestamp, startLine,endLine);
																validEventPacketList.add(validEvent);
															}
														}
													}
												}
											}
										}
									}
			    				}
			    			}
//			    			//validation of the clones after changing activities
			    			validateCloneFamily();
			    			if	(!(countNotDeletedClones() > 1)) STATE = CopyPasteChangeEpisodeState.STOP; 
			    		}
			    		break;
			    	default:
			    		break;
				} // switch
		 	} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}//END if(msdt.equals("msdt.textoperation.xsd") || msdt.equals("msdt.linediff.xsd")) 
    	return validEventPacketList;
    }
    
       
    class DummyText implements IText {}
    
    // Class for string-differ management. Computes the percentage of the difference bewteen 2 strings.
    // First compute the edit-distance with Levenstheins edit-distance algorithm. Then compute the percentage-difference.
    private class StringDifferMeasurement {
  	  private String firstString = null;
  	  private String secondString = null;
  	  private static final double STRING_DIFFER_MEASUREMENT = STRING_MEASURE;
  
  	  public StringDifferMeasurement(String first, String second){
  	  	this.firstString = first;
  		this.secondString = second;
  	  }
  	  	  
  	  //Get minimum of three values
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

  	  //Compute Levenshtein distance
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
  	  	  if (n == 0) { return m; }
  	  	  if (m == 0) { return n; }
  	  	  d = new int[n+1][m+1];
  	  	  // Step 2
  	  	  for (i = 0; i <= n; i++) { d[i][0] = i; }
  	  	  for (j = 0; j <= m; j++) { d[0][j] = j; }
  	  	  // Step 3
  	  	  for (i = 1; i <= n; i++) { 
  	  		  s_i = s.charAt (i - 1); 
  	  		 // Step 4
  	    	for (j = 1; j <= m; j++) { 
  	    		t_j = t.charAt (j - 1); 
  	    		// Step 5
  	    		if (s_i == t_j) { 
  	    			cost = 0;
  	    		}
  	    		else { 
  	    			cost = 1; 
  	    		}
  	    		int first = d[i-1][j]+1;
  				int second = d[i][j-1]+1;
  				int third = d[i-1][j-1] + cost;
  	    		// Step 6
  	    		d[i][j] = Minimum (d[i-1][j]+1, d[i][j-1]+1, d[i-1][j-1] + cost);
  			}
  	    }
  	  	int percentValue = d[n][m];
  	  	double myDouble  = stringDifferMeasurement(n,percentValue);
  	    // Step 7
  	    return myDouble;
  	  }
  	  
      //compute percentage of the difference with the length of the first string and the edit-distance result
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
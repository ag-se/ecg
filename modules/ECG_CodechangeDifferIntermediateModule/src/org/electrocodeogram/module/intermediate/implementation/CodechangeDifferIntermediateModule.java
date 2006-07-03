package org.electrocodeogram.module.intermediate.implementation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.electrocodeogram.event.IllegalEventParameterException;
import org.electrocodeogram.event.ValidEventPacket;
import org.electrocodeogram.event.WellFormedEventPacket;
import org.electrocodeogram.logging.LogHelper;
import org.electrocodeogram.logging.LogHelper.ECGLevel;
import org.electrocodeogram.misc.xml.ECGParser;
import org.electrocodeogram.misc.xml.NodeException;
import org.electrocodeogram.module.intermediate.IntermediateModule;
import org.electrocodeogram.module.intermediate.implementation.linediff.LineDiff;
import org.electrocodeogram.modulepackage.ModuleProperty;
import org.w3c.dom.Document;

import bmsi.util.Diff;
import bmsi.util.DiffPrint.NormalPrint;

/**
 * Iterator to provide single line diffs from a diff block
 */
public class CodechangeDifferIntermediateModule extends IntermediateModule {

    /** Print a change list line by line
     */
    public class LineChangeIterator implements Iterator {

    	String[] code0, code1;
    	// for this Iterator we do it the non-lazy but easy way: Compute Collection first in O(n)
    	//   and then deliver contents in O(1). This allows for O(1) implementation of size() as well. 
    	ArrayList<LineDiff> lineDiffs = new ArrayList<LineDiff>();
    	int it = 0;
    	
		public LineChangeIterator(String[] a, String[] b, Diff.change hunk) {
		    code0 = a;
		    code1 = b;
		    compute(hunk);
		}

		private void compute(Diff.change next) {
			if (next == null)
				return;
//System.out.println("\nAnalysing hunk (" + next.line0 + "," + next.line1 + "," + next.deleted + "," + next.inserted + ")");
			if (next.inserted == 0 && next.deleted == 0)
				return;
			if (next.inserted == 0) {
				// no inserted => #deleted lines beggining at line0+1 deleted in a
				for (int i = 0; i < next.deleted; i++) {
					lineDiffs.add(new LineDiff(code0[next.line0 + i], 
							next.line0+i,
							null,
							LineDiff.ChangeType.DELETED));
//System.out.println("  " + (next.line0+i+1) + "< " + code0[next.line0 + i]);
				}
			}
			if (next.inserted != 0 && next.deleted == 0) {
				// no deleted => #inserted lines beggining at line1+1 deleted in b
				for (int i = 0; i < next.inserted; i++) {
					lineDiffs.add(new LineDiff(null,
							next.line1+i,
							code1[next.line1 + i],
							LineDiff.ChangeType.INSERTED));
//System.out.println("  " + (next.line1+i+1) + "> " + code1[next.line1 + i]);
				}        		  
			}
			if (next.inserted != 0 && next.deleted != 0) {
			    assert(next.line0 == next.line1); // TODO: Check this! Is it really o.k.? It happens from time to time 
//if (next.line0 != next.line1) System.out.println("\nProblematic hunk (" + next.line0 + "," + next.line1 + "," + next.deleted + "," + next.inserted + ")");
                    
				int affected = (next.deleted > next.inserted ? next.deleted : next.inserted);
				for (int i = 0; i < affected; i++) {
					if (i < next.inserted && i < next.deleted) {
						lineDiffs.add(new LineDiff(code0[next.line0 + i],
								next.line0+i,
								code1[next.line1 + i],
								LineDiff.ChangeType.CHANGED));
//System.out.println("  " + (next.line0+i+1) + "<> " + code0[next.line0 + i]);
//System.out.println("  " + (next.line1+i+1) + ">< " + code1[next.line1 + i]);
					} else if (i >= next.inserted && i < next.deleted) {
						lineDiffs.add(new LineDiff(code0[next.line0 + i],
								next.line0+i,
								null,
								LineDiff.ChangeType.DELETED));
//System.out.println("  " + (next.line0+i+1) + "< " + code0[next.line0 + i]);						  
					} else if (i < next.inserted && i >= next.deleted) {
						lineDiffs.add(new LineDiff(null,
								next.line1+i,
								code1[next.line1 + i],
								LineDiff.ChangeType.INSERTED));
//System.out.println("  " + (next.line1+i+1) + "> " + code1[next.line1 + i]);						  
					} else 
                        assert (false);
				}
      	  	}    	  
		}

		public boolean hasNext() {
			return this.it < lineDiffs.size();
		}

		public LineDiff next() {
			return lineDiffs.get(it++);
		}

		public void remove() {
			lineDiffs.remove(it);
		}

		public int size() {
			return lineDiffs.size();
		}
    }

    /**
     * Log member
     */
    private static Logger logger = LogHelper
        .createLogger(CodechangeDifferIntermediateModule.class.getName());

    /**
     * Stores latest values of any document reported by codechange/status events
     */
    private HashMap<String, String> codes = new HashMap<String, String>(); 

    /**
     * Standard constructor 
     */
    public CodechangeDifferIntermediateModule(String arg0, String arg1) {
        super(arg0, arg1);
    }

    /**
     * @see org.electrocodeogram.module.intermediate.implementation.IntermediateModule#analyse(org.electrocodeogram.event.ValidEventPacket)
     */
    public Collection<ValidEventPacket> analyse(ValidEventPacket packet) {
        
    	boolean isCodeStatus = false;
    	String msdt = packet.getMicroSensorDataType().getName();
   	
        if (!msdt.equals("msdt.codechange.xsd") && !msdt.equals("msdt.codestatus.xsd"))
        	return null;

    	List<ValidEventPacket> events = new ArrayList<ValidEventPacket>();

    	if (msdt.equals("msdt.codestatus.xsd")) {
            // denotes that just the initial version will be stored and no diff computation
    		isCodeStatus = true;
        }
        
    	String id = "", projectname = "", documentname = "", username = "";
    	try {
    		Document document = packet.getDocument();
			id = ECGParser.getSingleNodeValue("id", document);
			username = ECGParser.getSingleNodeValue("username", document);
			documentname = ECGParser.getSingleNodeValue("documentname", document);
			projectname = ECGParser.getSingleNodeValue("projectname", document);
		} catch (NodeException e) {
			logger.log(ECGLevel.SEVERE, "Couldn't fetch any of {id, projectname, documentname, username} " +
					"elements from event in CodechangeDifferIntermediateModule.");
		}

		if (id == null || id.length() == 0)
			id = documentname;
    	
    	String oldCode = codes.get(id);
    	String newCode = getCode(packet);

//System.out.println("Old:\n" + oldCode + "----\nNew:\n" + newCode);        
        
    	if (newCode == null)
    		newCode = "";
    	if (oldCode == null)
    		isCodeStatus = true;
    	
    	// store current version
        codes.put(id, newCode);

		if (isCodeStatus)
    		return events;
    		
    	Date timestamp = packet.getTimeStamp();    	
        String[] oldLines = getLines(oldCode);
        String[] newLines = getLines(newCode);
        Diff diff = new Diff(oldLines, newLines); 
        Diff.change script = diff.diff_2(false);  // compute diff
//(new NormalPrint(oldLines,newLines)).print_script(script);

        // For each diff section in diff analysis send single new msdt.linediff event
        for (Diff.change next = script; next != null; next = next.link)
        {
            // generates line diffs from diff
            LineChangeIterator it = new LineChangeIterator(oldLines, newLines, next);
    
            String data = "<?xml version=\"1.0\"?><microActivity><commonData>";
            data += "<username>" + username + "</username>";
            data += "<projectname>" + projectname + "</projectname>";
            data += "<id>" + id + "</id></commonData>";
            data += "<linediff><documentname>" + documentname + "</documentname><lines>\n";
    
            logger.log(ECGLevel.FINE, "Diff found " + it.size() + " differing lines:");
    
    		String diffs = "";
    		
    		while (it.hasNext()) {
    			LineDiff lc = it.next();
    			// write down line numbers with offset 1
    			if (lc.getType() == LineDiff.ChangeType.CHANGED) {
                    diffs += " <line><type>changed</type><linenumber>"
    	                      + (lc.getLinenumber()+1) + "</linenumber>"
    	                      + "<from><![CDATA[" + lc.getFrom() + "]" + "]" + "></from>"
    	                      + "<to><![CDATA[" + lc.getTo() + "]" + "]" + "></to></line>\n";
    			} else if (lc.getType() == LineDiff.ChangeType.INSERTED) {
                    diffs += " <line><type>inserted</type><linenumber>"
                              + (lc.getLinenumber()+1) + "</linenumber>"
                              + "<to><![CDATA[" + lc.getTo() + "]" + "]" + "></to></line>\n";
    			} else if (lc.getType() == LineDiff.ChangeType.DELETED) {
                    diffs += " <line><type>deleted</type><linenumber>"
                              + (lc.getLinenumber()+1) + "</linenumber>"
                              + "<from><![CDATA[" + lc.getFrom()  + "]" + "]" + "></from></line>\n";
    			} else
    				logger.log(ECGLevel.SEVERE, "Unknown line change type in CodechangeDifferIntermediateModule.");
    		}
    
            logger.log(ECGLevel.FINE, diffs);
    
            data += diffs + "</lines></linediff></microActivity>";
//System.out.println("-----\nResults in event " + data);
    		
            // send event
            String[] args = {WellFormedEventPacket.HACKYSTAT_ADD_COMMAND, "msdt.linediff.xsd", data};
    		try {
    		    events.add(new ValidEventPacket(timestamp,
    		        WellFormedEventPacket.HACKYSTAT_ACTIVITY_STRING, Arrays.asList(args)));
    		} catch (IllegalEventParameterException e) {
    			logger.log(ECGLevel.SEVERE, "Wrong event parameters in CodechangeDifferIntermediateModule.");
    		}
        }
        
//System.out.println("-----------------------------------------------");
        
		return events;
    }

	/**
     * Splits a file contents into an array of lines
     * 
     * @param contents A text file contents
     * @return The single lines (seperated by \n) in code
     */
    private String[] getLines(String contents) {
        if (contents == null) {
            return new String[0];
        }
        return contents.split("\n");
    }

    /**
     * Fetches the document contents from an event, if available.
     * 
     * @param event An event with a CDATA "document" tag
     * @return Contents of the "document" in the event, empty string otherwise
     */
    private String getCode(ValidEventPacket event) {
    	try {
        	Document document = event.getDocument();
			return ECGParser.getSingleNodeValue("document", document);

    	} catch (NodeException e) {
			logger.log(ECGLevel.SEVERE, 
                    "Could not fetch document contents in CodechangeDifferIntermediateModule on event " + event);
			return "";
		}
    }

    /**
     * @see org.electrocodeogram.module.Module#propertyChanged()
     */
    public void propertyChanged(ModuleProperty moduleProperty) {

    }

    /**
     * @see org.electrocodeogram.module.Module#analyseCoreNotification()
     */
    public void update() {

    }

    /**
     * @see org.electrocodeogram.module.intermediate.implementation.IntermediateModule#initialize()
     */
    public void initialize() {
        this.setProcessingMode(ProcessingMode.ANNOTATOR); // Module *adds* events
        this.setAnnnotationStyle(AnnotationStyle.PRE_ANNOTATION); // ... before the triggering events
    }

}

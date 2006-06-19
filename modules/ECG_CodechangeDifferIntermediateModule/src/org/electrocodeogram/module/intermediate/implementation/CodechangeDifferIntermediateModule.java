package org.electrocodeogram.module.intermediate.implementation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;

import org.electrocodeogram.event.IllegalEventParameterException;
import org.electrocodeogram.event.ValidEventPacket;
import org.electrocodeogram.event.WellFormedEventPacket;
import org.electrocodeogram.logging.LogHelper;
import org.electrocodeogram.logging.LogHelper.ECGLevel;
import org.electrocodeogram.misc.xml.ECGParser;
import org.electrocodeogram.misc.xml.NodeException;
import org.electrocodeogram.module.intermediate.IntermediateModule;
import org.electrocodeogram.modulepackage.ModuleProperty;
import org.w3c.dom.Document;

import bmsi.util.Diff;
import bmsi.util.DiffPrint.NormalPrint;

/**
 *
 */
public class CodechangeDifferIntermediateModule extends IntermediateModule {

    /** Print a change list line by line
     */
    public class LineChangeIterator implements Iterator {

    	String[] code0, code1;
    	// for this Iterator we do it the non-lazy but easy way: Compute Collection first in O(n)
    	//   and then deliver contents in O(1). This allows for O(1) implementation of size() as well. 
    	ArrayList<LineChange> lineChanges = new ArrayList<LineChange>();
    	int it = 0;
    	
		public LineChangeIterator(String[] a, String[] b, Diff.change hunk) {
		    code0 = a;
		    code1 = b;
		    compute(hunk);
		}

		private void compute(Diff.change hunk) {
			if (hunk == null)
				return;
			for (Diff.change next = hunk; next != null; next = next.link)
			{
//System.out.println("Analysing hunk (" + next.line0 + "," + next.line1 + "," + next.deleted + "," + next.inserted + ")");
				if (next.inserted == 0 && next.deleted == 0)
					continue;
				if (next.inserted == 0) {
					// no inserted => #deleted lines beggining at line0+1 deleted in a
					for (int i = 0; i < next.deleted; i++) {
						lineChanges.add(new LineChange(code0[next.line0 + i], 
								next.line0+i,
								null,
								LineChange.ChangeType.DELETED));
//System.out.println("  " + (next.line0+i+1) + "< " + code0[next.line0 + i]);
					}
				}
				if (next.inserted != 0 && next.deleted == 0) {
					// no deleted => #inserted lines beggining at line1+1 deleted in b
					for (int i = 0; i < next.inserted; i++) {
						lineChanges.add(new LineChange(null,
								next.line1+i,
								code1[next.line1 + i],
								LineChange.ChangeType.INSERTED));
//System.out.println("  " + (next.line1+i+1) + "> " + code1[next.line1 + i]);
					}        		  
				}
				if (next.inserted != 0 && next.deleted != 0) {
					assert (next.line0 == next.line1); // TODO: Check this!
					int affected = (next.deleted > next.inserted ? next.deleted : next.inserted);
					for (int i = 0; i < affected; i++) {
						if (i < next.inserted && i < next.deleted) {
							lineChanges.add(new LineChange(code0[next.line0 + i],
									next.line0+i,
									code1[next.line1 + i],
									LineChange.ChangeType.CHANGED));
//System.out.println("  " + (next.line0+i+1) + "<> " + code0[next.line0 + i]);
//System.out.println("  " + (next.line1+i+1) + ">< " + code1[next.line1 + i]);
						} else if (i > next.inserted && i <= next.deleted) {
							lineChanges.add(new LineChange(code0[next.line0 + i],
									next.line0+i,
									null,
									LineChange.ChangeType.DELETED));
//System.out.println("  " + (next.line0+i+1) + "< " + code0[next.line0 + i]);						  
						} else if (i <= next.inserted && i > next.deleted) {
							lineChanges.add(new LineChange(null,
									next.line1+i,
									code1[next.line1 + i],
									LineChange.ChangeType.INSERTED));
//System.out.println("  " + (next.line1+i+1) + "> " + code1[next.line1 + i]);						  
						}
					}
				}
      	  	}    	  
		}

		public boolean hasNext() {
			return this.it < lineChanges.size();
		}

		public LineChange next() {
			return lineChanges.get(it++);
		}

		public void remove() {
			lineChanges.remove(it);
		}

		public int size() {
			return lineChanges.size();
		}
    }

    private static Logger logger = LogHelper
        .createLogger(CodechangeDifferIntermediateModule.class.getName());

    private HashMap<String, String> codes = new HashMap<String, String>(); 

    /**
     * @param arg0
     * @param arg1
     */
    public CodechangeDifferIntermediateModule(String arg0, String arg1) {
        super(arg0, arg1);
    }

    /**
     * @see org.electrocodeogram.module.intermediate.implementation.IntermediateModule#analyse(org.electrocodeogram.event.ValidEventPacket)
     */
    @Override
    public ValidEventPacket analyse(ValidEventPacket packet) {
        
    	boolean isCodeStatus = false;
    	String msdt = packet.getMicroSensorDataType().getName();
    	
        if (!msdt.equals("msdt.codechange.xsd") && !msdt.equals("msdt.codestatus.xsd"))
        	return packet;

    	ValidEventPacket event = null;

    	if (msdt.equals("msdt.codestatus.xsd")) {
    		isCodeStatus = true;
    		event = packet;
        }
        
    	String id = "", projectname = "", documentname = "", username = "";
    	try {
    		Document document = packet.getDocument();
			id = ECGParser.getSingleNodeValue("id", document);
			username = ECGParser.getSingleNodeValue("username", document);
			documentname = ECGParser.getSingleNodeValue("documentname", document);
			projectname = ECGParser.getSingleNodeValue("projectname", document);
		} catch (NodeException e) {
			logger.log(ECGLevel.SEVERE, "Couldn't fetch any of {id, projectname, documentname} " +
					"elements from event in CodechangeDifferIntermediateModule.");
		}

		if (id == null || id.length() == 0)
			id = documentname;
    	
    	String oldCode = codes.get(id);
    	String newCode = getCode(packet);
    	
    	if (newCode == null)
    		newCode = "";
    	if (oldCode == null)
    		isCodeStatus = true;
    	
		codes.put(id, newCode);

		if (isCodeStatus)
    		return event;
    		
    	Date timestamp = packet.getTimeStamp();    	
        String[] oldLines = getLines(oldCode);
        String[] newLines = getLines(newCode);
        Diff diff = new Diff(oldLines, newLines); 
        Diff.change script = diff.diff_2(false);
//(new NormalPrint(oldLines,newLines)).print_script(script);

		LineChangeIterator it = new LineChangeIterator(oldLines, newLines, script);
        String data = "<?xml version=\"1.0\"?><microActivity><commonData>";
        data += "<username>" + username + "</username>";
        data += "<projectname>" + projectname + "</projectname>";
        data += "<id>" + id + "</id></commonData>";
        data += "<linediff><documentname>" + documentname + "</documentname><lines>\n";

        logger.log(ECGLevel.FINE, "Diff found " + it.size() + " differing lines:");

		String diffs = "";
		
		while (it.hasNext()) {
			LineChange lc = it.next();
			// write down line numbers with offset 1
			if (lc.getType() == LineChange.ChangeType.CHANGED) {
                diffs += " <line><type>changed</type><linenumber>"
	                      + (lc.getLinenumber()+1) + "</linenumber>"
	                      + "<from><![CDATA[" + lc.getFrom() + "]" + "]" + "></from>"
	                      + "<to><![CDATA[" + lc.getTo() + "]" + "]" + "></to></line>\n";
			} else if (lc.getType() == LineChange.ChangeType.INSERTED) {
                diffs += " <line><type>inserted</type><linenumber>"
                          + (lc.getLinenumber()+1) + "</linenumber>"
                          + "<to><![CDATA[" + lc.getTo() + "]" + "]" + "></to></line>\n";
			} else if (lc.getType() == LineChange.ChangeType.DELETED) {
                diffs += " <line><type>deleted</type><linenumber>"
                          + (lc.getLinenumber()+1) + "</linenumber>"
                          + "<from><![CDATA[" + lc.getFrom()  + "]" + "]" + "></from></line>\n";
			} else
				logger.log(ECGLevel.SEVERE, "Unknown line change type in CodechangeDifferIntermediateModule.");
		}

        logger.log(ECGLevel.FINE, diffs);

        data += diffs + "</lines></linediff></microActivity>";
//System.out.println(data);
		
        String[] args = {WellFormedEventPacket.HACKYSTAT_ADD_COMMAND, "msdt.linediff.xsd", data};
		try {
		    event = new ValidEventPacket(timestamp,
		        WellFormedEventPacket.HACKYSTAT_ACTIVITY_STRING, Arrays.asList(args));
		} catch (IllegalEventParameterException e) {
			logger.log(ECGLevel.SEVERE, "Wrong event parameters in CodechangeDifferIntermediateModule.");
		}

		return event;

    }

    private String getHashKey(ValidEventPacket packet) {

    	Document document = packet.getDocument();
    	String key = "unknown key";

    	try {
			key = ECGParser.getSingleNodeValue("documentname", document);
		} catch (NodeException e) {
			try {
				key = ECGParser.getSingleNodeValue("id", document);
			} catch (NodeException e1) {
				assert(false);
				return key;
			}
		}

		return key;
	}

	/**
     * @param code
     * @return
     * @throws IOException 
     */
    private String[] getLines(String code) {
        if (code == null) {
            return null;
        }
        return code.split("\n");
    }

    private String getCode(ValidEventPacket packet) {
    	try {
        	Document document = packet.getDocument();
			return ECGParser.getSingleNodeValue("document", document);

    	} catch (NodeException e) {
			logger.log(ECGLevel.SEVERE, "Could not fetch code for line diff computation in CodechangeDifferIntermediateModule.");
			return null;
		}
    }

    /**
     * @param propertyName
     * @param propertyValue
     */
    @Override
    public void propertyChanged(ModuleProperty moduleProperty) {

    }

    /**
     * @see org.electrocodeogram.module.Module#analyseCoreNotification()
     */
    @Override
    public void update() {

    }

    /**
     * @see org.electrocodeogram.module.intermediate.implementation.IntermediateModule#initialize()
     */
    @Override
    public void initialize() {
        this.setProcessingMode(ProcessingMode.FILTER);
    }

}

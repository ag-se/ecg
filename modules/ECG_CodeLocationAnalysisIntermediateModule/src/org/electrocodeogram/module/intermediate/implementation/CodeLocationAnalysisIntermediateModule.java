package org.electrocodeogram.module.intermediate.implementation;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;
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
import org.electrocodeogram.modulepackage.ModulePropertyException;
import org.w3c.dom.Document;

/**
 * Module to assemble (code) location with retained identity over time and
 * send location change events.
 */
public class CodeLocationAnalysisIntermediateModule extends IntermediateModule {

    public class Loc {
        public String id;
        public boolean alive = true;
        public Date start = null;
        public Date end = null;
        public int sizemin = 100000;
        public int sizemax = 0;
        public Map<String, Integer> types = new HashMap<String, Integer>();
        public String toString() {
            String res = id + " with " + sizemin + " <= size <= " + sizemax + " [";
            for (Map.Entry<String, Integer> entry : types.entrySet())
                res += entry.getValue() + " " + entry.getKey() + " - ";
            return res + "] " + (alive ? "is" : "was") + " alive for " + duration();
        }
        
		private String duration() {
			long ms = end.getTime() - start.getTime();
			long s = ms / 1000;
			long d = s/(24*60*60);
			s -= d*(24*60*60);
			long h = s/(60*60);
			s -= h*(60*60);
			long m = s/60;
			s -= m*60;
			return d + "d" + h + "h" + m + "m" + s + "s";
		}
    }

    private static final String CREATOR_STRING = "CodeLocationAnalysisIntermediateModule1.1.4";

    private int ccc = 0; // For Debugging purposes: A counter of the calls to analyse
    
    private Map<String, Integer> projects, users, files, types, locations;
    private Map<String, Loc> locMap;

    /**
     * The class logger
     */
    public static Logger logger = LogHelper 
        .createLogger(CodeLocationAnalysisIntermediateModule.class.getName());

	/**
     * Standard Intermediate Module constructor
     * 
	 * @param id
	 * @param name
	 */
	public CodeLocationAnalysisIntermediateModule(String id, String name) {
		super(id, name);
        projects = new HashMap<String, Integer>();
        users = new HashMap<String, Integer>();
        files = new HashMap<String, Integer>();
        types = new HashMap<String, Integer>();
        locations = new HashMap<String, Integer>();
        locMap = new HashMap<String, Loc>();
	}

	/**
	 * @see org.electrocodeogram.module.intermediate.IntermediateModule#analyse(org.electrocodeogram.event.ValidEventPacket)
	 */
	public Collection<ValidEventPacket> analyse(ValidEventPacket eventPacket) {

//System.out.println(eventPacket);
        ccc++;
//System.out.println("Event No. " + ccc + " of type " + eventPacket.getSensorDataType() + " from " + eventPacket.getTimeStamp());        

        List<ValidEventPacket> events = new ArrayList<ValidEventPacket>(); 
        
        if (eventPacket.getMicroSensorDataType().getName().equals("msdt.codelocation.xsd")) {
            // Convert code locations to exact code change events
    		
            Document doc = eventPacket.getDocument();
            String data1 = "";
            String data2 = "";
            // parse codelocation
            try {
                // String id = ECGParser.getSingleNodeValue("id", doc);
                String projectName = ECGParser.getSingleNodeValue("projectname", doc);
                String userName = ECGParser.getSingleNodeValue("username", doc);
                String type = ECGParser.getSingleNodeValue("type", doc);
                String locId = ECGParser.getSingleNodeValue("locid", doc);
                String relIds = ECGParser.getSingleNodeValue("related", doc);
                String relParts[] = relIds.split(";");
                String fId = relParts[0];
                String pId = relParts[1];
                String nId = relParts[2];
                String rId = relParts[3];
                String locString = ECGParser.getSingleNodeValue("location", doc);
                String locParts[] = locString.split(";");
                String file = locParts[0].substring(locParts[0].lastIndexOf("/")+1);
                int startline = Integer.parseInt(locParts[1]);
                // int length = Integer.parseInt(locParts[2]);
                String contents = ECGParser.getSingleNodeValue("contents", doc);
                if (contents == null)
                    contents = "";
                else
                    contents = addLineNumbers(contents, startline);
                contents = "----(Prev:" + pId + ")----\n" + 
                            contents + 
                            "----(Next:" + nId + ")----" + 
                            "\n(Parent:" + fId + ")(Related:" + rId + ")";
                String locIdFull = locId + " in " + file;
                
                count(projects, projectName);
                count(users, userName);
                count(types, type);
                count(files, file);
                Loc loc = locMap.get(locIdFull);
                if (loc == null) {
                    loc = new Loc();
                    loc.alive = true;
                    loc.id = locIdFull;
                    loc.start = eventPacket.getTimeStamp();
                    locMap.put(locIdFull, loc);
                }
                Integer tc = loc.types.get(type);
                if (tc == null) {
                    tc = 0;
                }
                tc += 1;
                loc.types.put(type, tc);
    			loc.end = eventPacket.getTimeStamp();
                if ("SHORTENED_AT_ALL,MERGED_DEL_AT_START,MERGED_DEL_AT_END".contains(type))
                    loc.alive = false;
                int size = Integer.parseInt(locParts[2]);
            	if (loc.sizemin > size)
            		loc.sizemin = size;
            	if (loc.sizemax < size)
            		loc.sizemax = size;
                
            	count(locations, locIdFull);
                
            	for (Loc l : locMap.values())
            		if (l.alive)
            			l.end = eventPacket.getTimeStamp();

                // write exactcodechange with file as path
                data1 = "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                        + userName
                        + "</username><projectname>"
                        + projectName
                        + "</projectname></commonData><exactCodeChange>"+"<path>"
                        + file+"</path>"
                        + "<change><typeOfChange>" + type + "</typeOfChange>"
                        + "<elementName>"+ String.format("%1$05d", Integer.parseInt(locId)) +"</elementName>"
                        + "<identifier><![CDATA[" + locIdFull + "]]></identifier>"
                        + "<codeOrIdentifier><![CDATA["+ contents + "]]>"
                        + "</codeOrIdentifier></change>"
                        + "</exactCodeChange></microActivity>";

                // write exactcodechange with time stamp as path
                data2 = "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                        + userName
                        + "</username><projectname>"
                        + projectName
                        + "</projectname></commonData><exactCodeChange>"+"<path>"
                        + new SimpleDateFormat("dd.MM.yy, HH:mm:ss").format(eventPacket.getTimeStamp())+"</path>"
                        + "<change><typeOfChange>" + type + "</typeOfChange>"
                        + "<elementName>"+ String.format("%1$05d", Integer.parseInt(locId)) +"</elementName>"
                        + "<identifier><![CDATA[" + locId + " at " + eventPacket.getTimeStamp() + "]]></identifier>"
                        + "<codeOrIdentifier><![CDATA["+ contents + "]]>"
                        + "</codeOrIdentifier></change>"
                        + "</exactCodeChange></microActivity>";
            } catch (NodeException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            try {
                String[] args1 = {WellFormedEventPacket.HACKYSTAT_ADD_COMMAND, 
                        "msdt.exactcodechange.xsd", 
                        data1};
                events.add(new ValidEventPacket(eventPacket.getTimeStamp(),
                    WellFormedEventPacket.HACKYSTAT_ACTIVITY_STRING, Arrays.asList(args1)));
                String[] args2 = {WellFormedEventPacket.HACKYSTAT_ADD_COMMAND, 
                        "msdt.exactcodechange.xsd", 
                        data2};
                events.add(new ValidEventPacket(eventPacket.getTimeStamp(),
                    WellFormedEventPacket.HACKYSTAT_ACTIVITY_STRING, Arrays.asList(args2)));
            } catch (IllegalEventParameterException e) {
                logger.log(ECGLevel.SEVERE, "Wrong event parameters in CodeLocationTrackerIntermediateModule.");
            }
                        
        }

        else if (eventPacket.getMicroSensorDataType().getName().equals("msdt.codechange.xsd") ||
                 eventPacket.getMicroSensorDataType().getName().equals("msdt.codestatus.xsd")) {
            
            Document doc = eventPacket.getDocument();
            String data = "";
            // parse codelocation
            try {
                // String id = ECGParser.getSingleNodeValue("id", doc);
                String projectName = ECGParser.getSingleNodeValue("projectname", doc);
                String userName = ECGParser.getSingleNodeValue("username", doc);
                String file = ECGParser.getSingleNodeValue("documentname", doc);
                String contents = ECGParser.getSingleNodeValue("document", doc);
                if (contents == null)
                    contents = "";
                else
                    contents = addLineNumbers(contents,0);

                // write exactcodechange
                data = "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                        + userName
                        + "</username><projectname>"
                        + projectName
                        + "</projectname></commonData><exactCodeChange>"+"<path>"
                        + file+"</path>"
                        + "<change><typeOfChange>CHANGED</typeOfChange>"
                        + "<elementName>complete</elementName>"
                        + "<identifier><![CDATA[" + file + " complete" + "]]></identifier>"
                        + "<codeOrIdentifier><![CDATA["+ contents + "]]>"
                        + "</codeOrIdentifier></change>"
                        + "</exactCodeChange></microActivity>";
            } catch (NodeException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            try {
                String[] args = {WellFormedEventPacket.HACKYSTAT_ADD_COMMAND, 
                        "msdt.exactcodechange.xsd", 
                        data};
                events.add(new ValidEventPacket(eventPacket.getTimeStamp(),
                    WellFormedEventPacket.HACKYSTAT_ACTIVITY_STRING, Arrays.asList(args)));
            } catch (IllegalEventParameterException e) {
                logger.log(ECGLevel.SEVERE, "Wrong event parameters in CodeLocationTrackerIntermediateModule.");
            }

        }
            // TODO just for debugging
        else if (eventPacket.getMicroSensorDataType().getName().equals("msdt.system.xsd")) {
            try {
                if (ECGParser.getSingleNodeValue("type", eventPacket.getDocument()).equals("termination")) {
                    
                    print("Projekte: ", projects, null);
                    print("User: ", users, null);
                    print("Types: ", types, null);
                    print("Files: ", files, null);
                    print("Locations: ", locations, locMap);
                    
                }
            } catch (NodeException e) {
                // TODO report this
                e.printStackTrace();
                return null;
            }
        }

        return events;
        
	}

    private String addLineNumbers(String contents, int start) {
        String res = "";
        for (String l : getLines(contents)) {
            res += (start++) + "\t" + l + "\n";
        }
        return res;
    }

    /**
     * Splits a file contents into an array of lines
     * 
     * @param contents A text file contents
     * @return The single lines (seperated by \n) in code
     */
    private Vector<String> getLines(String contents) {
        Vector<String> res=new Vector<String>();
        if (contents == null) {
            return res;
        }
        StringBuffer buffer=new StringBuffer();
        int size=contents.length();
        for (int index=0;index<size;index++) {
           char current=contents.charAt(index);
           if (current==0x0D) {
              if (index+1<size && contents.charAt(index+1)==0x0A)
                  index++;
              res.add(buffer.toString());
              buffer.setLength(0);
           } else if (current==0x0A) {
              if (index+1<size && contents.charAt(index+1)==0x0D)
                  index++;
              res.add(buffer.toString());
              buffer.setLength(0);
           } else buffer.append(current);
        }
        if (buffer.length()>0)
           res.add(buffer.toString());
        return res;
    }

    private void print(String heading, Map<String, Integer> map, Map<String, Loc> ref) {
        System.out.println(map.size() + " " + heading);
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            Object key = (ref == null ? entry.getKey() : ref.get(entry.getKey()));
            System.out.printf("  %1$05dx %2$s\n", entry.getValue().intValue(), key);
        }
        System.out.println();
    }

    private void count(Map<String, Integer> map, String id) {
        Integer cur = map.get(id);
        if (cur == null)
            cur = 0;
        cur += 1;
        map.put(id, cur);
    }

    // ----------------------------------------------
    
    public void initialize() {
		this.setProcessingMode(ProcessingMode.FILTER);
	}

	protected void propertyChanged(ModuleProperty moduleProperty)
			throws ModulePropertyException {
		// no properties
	}

	public void update() {
		// no properties
	}

}

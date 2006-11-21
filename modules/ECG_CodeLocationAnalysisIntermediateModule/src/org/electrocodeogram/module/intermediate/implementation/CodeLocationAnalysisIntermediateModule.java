package org.electrocodeogram.module.intermediate.implementation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
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
        public String file;
        public boolean alive = true;
        public Map<String, Integer> types = new HashMap<String, Integer>();
        public String toString() {
            String res = id + " (" + file + ") " + (alive ? "alive" : "dead") + " [";
            for (Map.Entry<String, Integer> entry : types.entrySet())
                res += entry.getValue() + " " + entry.getKey();
            return res + "]";
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
            String data = "";
            // parse codelocation
            try {
                String id = ECGParser.getSingleNodeValue("id", doc);
                String projectName = ECGParser.getSingleNodeValue("projectname", doc);
                String userName = ECGParser.getSingleNodeValue("username", doc);
                String type = ECGParser.getSingleNodeValue("type", doc);
                String locId = ECGParser.getNodeValue(doc.getElementsByTagName("id").item(1));
                // int relId = Integer.parseInt(ECGParser.getSingleNodeValue("related", doc));
                String locString = ECGParser.getSingleNodeValue("location", doc);
                String locParts[] = locString.split(";");
                String contents = ECGParser.getSingleNodeValue("contents", doc);
                if (contents == null)
                    contents = "";
                
                count(projects, projectName);
                count(users, userName);
                count(types, type);
                count(files, locParts[0]);
                Loc loc = locMap.get(locId);
                if (loc == null) {
                    loc = new Loc();
                    loc.alive = true;
                    loc.file = locParts[0];
                    loc.id = locId;
                    locMap.put(locId, loc);
                }
                Integer tc = loc.types.get(type);
                if (tc == null) {
                    tc = 0;
                }
                tc += 1;
                loc.types.put(type, tc);
                if ("SHORTENED_AT_ALL,MERGED_DEL_AT_START,MERGED_DEL_AT_END".contains(type))
                    loc.alive = false;
                count(locations, locId);

                // write exactcodechange
                data = "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                        + userName
                        + "</username><projectname>"
                        + projectName
                        + "</projectname></commonData><exactCodeChange>"+"<path>"
                        + locParts[0]+"</path>"
                        + "<change><typeOfChange>" + type + "</typeOfChange>"
                        + "<elementName>"+ locId +"</elementName>"
                        + "<identifier><![CDATA[" + id + "]]></identifier>"
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

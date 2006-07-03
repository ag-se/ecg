package org.electrocodeogram.module.intermediate.implementation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Logger;

import org.electrocodeogram.event.ValidEventPacket;
import org.electrocodeogram.logging.LogHelper;
import org.electrocodeogram.logging.LogHelper.ECGLevel;
import org.electrocodeogram.misc.xml.ECGParser;
import org.electrocodeogram.misc.xml.NodeException;
import org.electrocodeogram.module.intermediate.IntermediateModule;
import org.electrocodeogram.module.intermediate.implementation.location.change.BlockChange;
import org.electrocodeogram.module.intermediate.implementation.location.change.LocationChange;
import org.electrocodeogram.module.intermediate.implementation.location.change.LocationChange.LocationChangeType;
import org.electrocodeogram.module.intermediate.implementation.location.state.Line;
import org.electrocodeogram.module.intermediate.implementation.location.state.Location;
import org.electrocodeogram.module.intermediate.implementation.location.state.Text;
import org.electrocodeogram.module.intermediate.implementation.location.state.Line.Block;
import org.electrocodeogram.module.intermediate.implementation.location.state.Line.Statement;
import org.electrocodeogram.modulepackage.ModuleProperty;
import org.electrocodeogram.modulepackage.ModulePropertyException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Module to assemble (code) location with retained identity over time and
 * send location change events.
 */
public class CodeLocationTrackerIntermediateModule extends IntermediateModule {

    /**
     * The class'es logger
     */
    public static Logger logger = LogHelper 
        .createLogger(CodeLocationTrackerIntermediateModule.class.getName());

    /**
     * Holds list of texts. A Text contains the <b>actual</b> (accouring to analysed events)
     * state of a document which is its <i>Location</i>s and <i>Line</i>s. This variable maps
     * the documentname to its Text object. 
     */
    private HashMap<String, Text> texts = new HashMap<String, Text>();

    /**
     * Holds for each Location a list of LocationChanges
     * 
     * TODO Maybe not useful in later versions, because the history can be compiled 
     * from a BlockChange history
     */
    private LinkedHashMap<Location, List<LocationChange>> locationChangeHistories 
                = new LinkedHashMap<Location, List<LocationChange>>();
    
	/**
     * Standard Intermediate Module constructor
     * 
	 * @param id
	 * @param name
	 */
	public CodeLocationTrackerIntermediateModule(String id, String name) {
		super(id, name);
	}

	/**
	 * @see org.electrocodeogram.module.intermediate.IntermediateModule#analyse(org.electrocodeogram.event.ValidEventPacket)
	 */
	public Collection<ValidEventPacket> analyse(ValidEventPacket eventPacket) {

//System.out.println(eventPacket);        
		if (eventPacket.getMicroSensorDataType().getName().equals("msdt.codestatus.xsd")) {
            // On code status events compile first set of locations and initial location changes
    		
            String documentName = getDocumentName(eventPacket); 
    		if (!documentName.endsWith(".java")) // TODO currently only for .java documents
    			return null;
    		
            if (texts.get(documentName) != null) // TODO What to do here?
                System.err.println("A new codestatus for " + documentName + " at " + eventPacket.getTimeStamp());

            Text text = new Text();
            String code = getCode(eventPacket);

            String[] lines = getLines(code);
    		Line newLine = null;
    		
    		for (int lineNo = 0; lineNo < lines.length; lineNo++) {
                Line prevLine = newLine;
                newLine = new Line(lineNo, lines[lineNo]);
    			computeBasicLineProperties(newLine);
    			computeCohesionAndLevel(newLine, prevLine);
                text.getLines().add(lineNo, newLine);
    		}
            
            computeLocations(text);
/*    
System.out.println("First locations computation on file " + documentName);
System.out.println("at " + eventPacket.getTimeStamp() + ":");
System.out.println(text.printLocations());
System.out.println("---");
*/
            this.texts.put(documentName, text);
            
            assert(text.checkValidity());
            assert(text.printContents().trim().equals(code.trim()));
        }
        
        else if (eventPacket.getMicroSensorDataType().getName().equals("msdt.linediff.xsd")) {

            String documentName = getDocumentName(eventPacket); 
            if (!documentName.endsWith(".java"))
                return null;
            
            Text text = texts.get(documentName);
            assert(text != null);
            
            // TODO Better DOM usage necessary!
            Node[] linediffs = null;
            try {
                Document xmlDoc = eventPacket.getDocument();
                linediffs = ECGParser.getChildNodes( 
                        ECGParser.getChildNode(
                                ECGParser.getChildNode(
                                        ECGParser.getChildNode(xmlDoc, "microActivity"), 
                                        "linediff"),
                                "lines"),  
                        "line"); // Hier war CPC-Fehler!! Fehlendes Change (lines -> line) nach Paste
            } catch (NodeException e) {
                // TODO report this
                e.printStackTrace();
                return null;
            }
            
            // count number of inserts/deletes to later shift line numbers
            int deletesCount = 0;
            BlockChange blockChange = new BlockChange(eventPacket.getTimeStamp());

            // process each line diff
            for (int i = 0; i < linediffs.length; i++) {
                
                // parse line change
                Node linediff = linediffs[i];
                String diffType = "unknown";
                String from = null;
                String to = null;
                int linenumber = -1;
                try {
                    diffType = ECGParser.getNodeValue(ECGParser.getChildNode(linediff, "type"));
                    linenumber = Integer.parseInt(
                        ECGParser.getNodeValue(ECGParser.getChildNode(linediff, "linenumber")));
                    linenumber -= 1; // in events the line numbers start with 1
                    // the follwoing one is quite difficult to understand: shift line number due to already processed delete diffs
                    // deletes are not reflected in the diffs linenumbers but are processed in the text (i.e. the line
                    // do not exist any more). This is different to insertions which are reflected in the text and its line
                    // numbers. The shift is necessary because the line numbers are retrieved by text.lines.get(linenumber)
                    linenumber -= deletesCount;
                    if (ECGParser.hasChildNode(linediff, "from"))
                        from = ECGParser.getNodeValue(ECGParser.getChildNode(linediff, "from"));
                    if (from == null)
                        from = "";
                    if (ECGParser.hasChildNode(linediff, "to"))
                        to = ECGParser.getNodeValue(ECGParser.getChildNode(linediff, "to"));
                    if (to == null)
                        to = "";
                } catch (NodeException e) {
                    // TODO report this
                    e.printStackTrace();
                }

/*                
System.out.println("New change: '" + diffType + "' at line " + linenumber);
System.out.println("from:" + from);
System.out.println("  to:" + to);
System.out.println("at " + eventPacket.getTimeStamp() + "\n");
*/
                // first get the affected Location and its Line
                Line line = null;
                Location location = null;
                
                if (linenumber < text.getLines().size()) {
                    line = text.getLines().get(linenumber);
                    location = line.getLocation();
                } else {
                    assert(diffType.equals("inserted"));
                    // line will be computed later
                    // inserted at the end, take the last location
                    location = text.getLocations().last();
                    // linenumber must be exactly after the last line in text
                    assert(text.getLines().size() == linenumber); 
                }
                assert(location != null);
                
                LocationChange locationChange = null;
                boolean locationChangeReported = false;

                // now first perfom the linediff operation in the text
                if (diffType.equals("changed")) {
                    assert(line != null);
                    // following is not always true because previous changes in this block may have already changed contents
                    // assert(line.contents.trim().equals(from.trim()));
                    line.setContents(to);
                    this.computeBasicLineProperties(line);
                    locationChange = new LocationChange(location.getId(), LocationChangeType.CHANGED, 
                            location.printContents(), -1, blockChange);
                } else if (diffType.equals("deleted")) {
                    assert(line != null);
                    deletesCount++;
                    Collection<Location> tail = text.getLocations().tailSet(location);
                    // resize location and remove it if necessary (and register location change)
                    if (location.getLength() == 1) {
                        // location will get empty
                        text.getLocations().remove(location);
                        locationChange = new LocationChange(location.getId(), 
                                LocationChangeType.EMPTIED, "", -1, blockChange);
                    } else {
                        locationChange = new LocationChange(location.getId(), LocationChangeType.CHANGED, 
                                location.printContents(), -1, blockChange);
                    }
                    location.setLength(location.getLength() - 1);
                    // change following line numbers
                    for (Line l : text.getLines().subList(linenumber, text.getLines().size())) {
                        l.setLinenumber(l.getLinenumber() - 1);
                    }
                    // ...and startlinenumbers of the follwoing locations
                    for (Location loc : tail) {
                        if (loc != location)  // location itself is in the tail
                            loc.setStartLinenumber(loc.getStartLinenumber() - 1);
                    }
                    // finally remove line
                    text.getLines().remove(linenumber);
                } else if (diffType.equals("inserted")) {
                    // store in 'line' the inserted line
                    line = new Line(linenumber, to);
                    this.computeBasicLineProperties(line);
                    // update location link and length
                    line.setLocation(location);
                    location.setLength(location.getLength() + 1);
                    // add new line at position
                    text.getLines().add(linenumber, line);
                    // change following line numbers 
                    for (Line lin : text.getLines().subList(linenumber+1, text.getLines().size())) {
                        lin.setLinenumber(lin.getLinenumber() + 1);
                    }
                    for (Location loc : text.getLocations().tailSet(location)) {
                        if (loc != location)  // location itself is correctly positioned
                            loc.setStartLinenumber(loc.getStartLinenumber() + 1);
                    }
                    locationChange = new LocationChange(location.getId(), LocationChangeType.CHANGED, 
                            location.printContents(), -1, blockChange);
                } else
                    assert(false);

                // secondly perform recomputation of Locations at the new line neighborhoods
                if (diffType.equals("changed") || diffType.equals("inserted")) {                    
                    Line prevLine = null;
                    Line nextLine = null;
                    if (linenumber-1 >= 0)
                        prevLine = text.getLines().get(linenumber-1);
                    if (linenumber+1 < text.getLines().size())
                        nextLine = text.getLines().get(linenumber+1); 
                    boolean a = computeNewNeighborhood(prevLine, line, blockChange);
                    boolean b = computeNewNeighborhood(line, nextLine, blockChange);
                    locationChangeReported = (locationChangeReported || a || b);
                } else if (diffType.equals("deleted")) {
                    Line prevLine = null;
                    if (linenumber-1 >= 0)
                        prevLine = text.getLines().get(linenumber-1);
                    Line nextLine = text.getLines().get(linenumber); // no +1 because line has been shifted
                    boolean a = computeNewNeighborhood(prevLine, nextLine, blockChange);
                    locationChangeReported = (locationChangeReported || a);
                } 
                
                if (!locationChangeReported && locationChange != null)
                    addLocationChangeToHistory(location, locationChange);
/*
System.out.println("New locations:\n" + text.printLocations());
System.out.println("---");
*/
                assert(text.checkValidity());
                assert(text.printContents().trim().equals(text.printContentsFromLocations().trim()));

            }                    
/*
System.out.println(text);
System.out.println("---------------------------------------------------------");
*/          
        }

        // TODO just for debugging
        else if (eventPacket.getMicroSensorDataType().getName().equals("msdt.system.xsd")) {
            try {
                if (ECGParser.getSingleNodeValue("type", eventPacket.getDocument()).equals("termination")) {
                    for (Location loc : this.locationChangeHistories.keySet()) {
                        System.out.println("Location " + loc.getId() + ": ");
                        System.out.println("**********************************************");
                        Collection<LocationChange> lcs = this.locationChangeHistories.get(loc);
                        for (LocationChange lc : lcs) {
                            System.out.println("---- " + lc.toString() + " ----");
                            System.out.println(lc.getContents());
                        }
                    }
                }
            } catch (NodeException e) {
                // TODO report this
                e.printStackTrace();
                return null;
            }
        }

		// TODO just for assertions
        else if (eventPacket.getMicroSensorDataType().getName().equals("msdt.codechange.xsd")) {
            
            // Currently codechages are filtered by the CodeChangeDiffer
            String documentName = getDocumentName(eventPacket); 
            if (!documentName.endsWith(".java"))
                return null;
            
            Text text = texts.get(documentName);
            assert(text != null);

            try {
                Document xmlDoc = eventPacket.getDocument();
                String resultingCode = ECGParser.getSingleNodeValue("document", xmlDoc); 
                assert(text.printContents().trim().equals(resultingCode.trim()));
            } catch (NodeException e) {
                // TODO report this
                e.printStackTrace();
                return null;
            }
            
        }

		return null;
	}

    private void addLocationChangeToHistory(Location location, LocationChange locationChange) {
        List<LocationChange> locationChanges = this.locationChangeHistories.get(location);
        if (locationChanges == null) {
            locationChanges = new ArrayList<LocationChange>();
            this.locationChangeHistories.put(location, locationChanges);
        }
        locationChanges.add(locationChange);
        if (locationChange.getBlockChange() != null)
            locationChange.getBlockChange().getLocationChanges().add(locationChange); // TODO this is a bit strange
    }

    // simply for reuse
    // return true, if location changes have been added 
    private boolean computeNewNeighborhood(Line lineAbove, Line lineBelow, BlockChange blockChange) {
        if (lineAbove == null || lineBelow == null)
            return false; // has been at the begin/end of the text: no recompution necessary
        Location locationAbove = lineAbove.getLocation();
        Location locationBelow = lineBelow.getLocation();
        // first compute new cohesion
        this.computeCohesionAndLevel(lineBelow, lineAbove);
        // no decide whether to split, merge or retain locations
        if (lineBelow.getCohesion() >= Location.MIN_COHESION
                && locationAbove != locationBelow) {
            // different locations have high cohesion now => merge them
            if (locationAbove.getLength() > locationBelow.getLength()) {
                locationAbove.mergeLocation(locationBelow);
                addLocationChangeToHistory(locationBelow, new LocationChange(locationBelow.getId(),  
                        LocationChangeType.MERGEDAWAY, "", locationAbove.getId(), blockChange));
                addLocationChangeToHistory(locationAbove, new LocationChange(locationAbove.getId(), 
                        LocationChangeType.EXTENDEDBELOW, locationAbove.printContents(), locationBelow.getId(), blockChange));
                return true;
            } else {
                locationBelow.mergeLocation(locationAbove);                
                addLocationChangeToHistory(locationAbove, new LocationChange(locationAbove.getId(), 
                        LocationChangeType.MERGEDAWAY, "", locationBelow.getId(), blockChange));
                addLocationChangeToHistory(locationBelow, new LocationChange(locationBelow.getId(), 
                        LocationChangeType.EXTENDEDABOVE, 
                        locationBelow.printContents(), locationAbove.getId(), blockChange));
                return true;
            }
        }
        else if (lineBelow.getCohesion() < Location.MIN_COHESION
                && locationAbove == locationBelow) {
            // new low cohesion in a location => split it
            Location newLoc = locationAbove.splitLocation(lineBelow);
            LocationChangeType type = LocationChangeType.SPLITABOVE;
            if (newLoc.getStartLinenumber() > locationAbove.getStartLinenumber())
                type = LocationChangeType.SPLITBELOW;
            addLocationChangeToHistory(newLoc, new LocationChange(newLoc.getId(),
                    LocationChangeType.FORKED, newLoc.printContents(), locationAbove.getId(), blockChange));
            addLocationChangeToHistory(locationAbove, new LocationChange(locationAbove.getId(),
                    type, locationAbove.printContents(), newLoc.getId(), blockChange));
            return true;
        }
        // else no changes
        return false;
    }

    
    /**
     * TODO consider levels as well
     * 
     * @param text
     * @return
     */
    private void computeLocations(Text text) {
        Location curLoc = null;
        for (Line line : text.getLines()) {
            if (curLoc == null || line.getCohesion() < Location.MIN_COHESION) { // curLoc == null means first line
                Location newLoc = new Location(text);
                newLoc.setStartLinenumber(line.getLinenumber());
                newLoc.setLength(1);
                text.getLocations().add(newLoc);
                line.setLocation(newLoc);
                addLocationChangeToHistory(newLoc, new LocationChange(newLoc.getId(), 
                        LocationChangeType.INTIATED, newLoc.printContents(), -1, null));
                curLoc = newLoc;
/*          } else if (line.level > loc.startLine.level) {
                Location newLoc = new Location();
                this.locations.add(newLoc);
                newLoc.startLine = line;
                newLoc.length = 1;
                newLoc.parent = loc;
                loc.children.add(newLoc);
                loc.nextLocation = newLoc;
                loc = newLoc;
            } else if (line.level < loc.startLine.level) {
                loc = loc.parent;
*/          } else {
                line.setLocation(curLoc);
                curLoc.setLength(curLoc.getLength() + 1);
            }
        }
    }

	private void computeBasicLineProperties(Line line) {

        // reset properties
        line.setBlock(Block.NONE);
        line.setStatement(Statement.UNKNOWN);

        // ignore string literals, char literals, and white space
        String contents = line.getContents().trim();
    	contents = contents.replaceAll("\".*?\"", "STRINGLITERAL"); // TODO
    	contents = contents.replaceAll("'.*?'", "CHARLITERAL"); // TODO

    	// set Block
    	int thisCurlyBraceA = contents.lastIndexOf('{');
    	int thisCurlyBraceZ = contents.indexOf('}');
    	if (thisCurlyBraceA >= 0 && thisCurlyBraceZ == -1)
    		line.setBlock(Block.BEGIN);
    	else if (thisCurlyBraceA == -1 && thisCurlyBraceZ >= 0)
    		line.setBlock(Block.END);
    	else if (thisCurlyBraceA < thisCurlyBraceZ)
    		line.setBlock(Block.BEGIN_END);
    	else if (thisCurlyBraceA > thisCurlyBraceZ)
    		line.setBlock(Block.END_BEGIN);
    	
    	// set Comment Statement 1
    	int thisLineComment = contents.indexOf("//");
    	if (thisLineComment == 0) // comment for whole line
    		line.setStatement(Statement.COMMENT);
    	else if (thisLineComment > 0) // comment at the end. remove it.
    		contents = contents.substring(0, thisLineComment);

    	// set Comment Statement 2
    	int thisBlockCommentA = contents.indexOf("/*");
    	int thisBlockCommentB = contents.indexOf("*");
    	int thisBlockCommentZ = contents.indexOf("*/");
    	if (thisBlockCommentA >= 0 && thisBlockCommentZ == -1) // TODO Could be in the middle with valid code preceeding
    		line.setStatement(Statement.COMMENT);
    	else if (thisBlockCommentA == -1 && thisBlockCommentZ >= 0) // TODO Could be in the middle with valid code following
    		line.setStatement(Statement.COMMENT);
    	else if (thisBlockCommentA == -1 && thisBlockCommentZ == -1 && thisBlockCommentB == 0) {   
    		// TODO Could be a multiply operator, even at the beginning!
    		line.setStatement(Statement.COMMENT);
    	}
    	else if (thisBlockCommentA >= 0 && thisBlockCommentZ == contents.length()-1) {
    		// comment at the end. remove it.
    		contents = contents.substring(0, thisBlockCommentZ);
    	}
    	
    	// set Code Statement
    	int thisSemicolon = contents.lastIndexOf(';');
    	if (thisSemicolon == contents.length()-1) { // ; at the end
    		if (line.getStatement() == Statement.UNKNOWN) 
    			line.setStatement(Statement.FULL);
    	} else {
    		if (line.getStatement() == Statement.UNKNOWN && line.getBlock() == Block.NONE) 
    			line.setStatement(Statement.PART);    			
    	}
    	
    	// set Empty Statement
    	if (contents.length() == 0)
    		line.setStatement(Statement.EMPTY);

    }

    private void computeCohesionAndLevel(Line thisLine, Line prevLine) {

		if (thisLine.getLinenumber() == 0 || prevLine == null) {
            thisLine.setCohesion(-10); // symbolic value for first line in text
            return;
        }

		// set initial level and cohesion
        thisLine.setLevel(prevLine.getLevel());
        thisLine.setCohesion(0);
        
		// 1. test { and }
    	if (thisLine.getBlock() == Block.END) { // 1.a
    		if (thisLine.getLevel() > 0) thisLine.setLevel(thisLine.getLevel() - 1);
    		thisLine.setCohesion(thisLine.getCohesion() + 1);
    	}
    	if (prevLine.getBlock() == Block.BEGIN) { // 1.b
    		thisLine.setLevel(thisLine.getLevel() + 1);
    		thisLine.setCohesion(thisLine.getCohesion() + 1);
    	}
    	if (prevLine.getBlock() == Block.END) { // 1.c
            // Rational: Avoid multiple block ends below each other to be put together, see 1.a
    		thisLine.setCohesion(thisLine.getCohesion() - 1);
        }
    	
    	// 2. test comment 
    	if (prevLine.getStatement() == Statement.COMMENT) // comment for whole line
    		thisLine.setCohesion(thisLine.getCohesion() + 1);
    	if (thisLine.getStatement() != Statement.COMMENT && prevLine.getStatement() == Statement.COMMENT)
    		thisLine.setCohesion(thisLine.getCohesion() + 1);
    	else if (thisLine.getStatement() == Statement.COMMENT && prevLine.getStatement() != Statement.COMMENT)
    		thisLine.setCohesion(thisLine.getCohesion() - 1);
    	
    	// 3. test ;
		if (prevLine.getStatement() == Statement.PART) 
    		thisLine.setCohesion(thisLine.getCohesion() + 1);
    	
    	// 4. test empty lines
    	if (thisLine.getStatement() == Statement.EMPTY && prevLine.getStatement() != Statement.EMPTY)
    		thisLine.setCohesion(thisLine.getCohesion() - 1);
    	if (thisLine.getStatement() != Statement.EMPTY && prevLine.getStatement() == Statement.EMPTY)
    		thisLine.setCohesion(thisLine.getCohesion() - 1);
    	
    	// 5. test keywords
    	boolean thisPublic = thisLine.getContents().startsWith("public");
    	boolean thisPrivate = thisLine.getContents().startsWith("private");
    	boolean thisProtected = thisLine.getContents().startsWith("protected");
    	boolean thisStatic = thisLine.getContents().startsWith("static");
    	if (thisPublic || thisPrivate || thisProtected || thisStatic)
    		thisLine.setCohesion(thisLine.getCohesion() - 1);
    	
		return;
	}

	private String getDocumentName(ValidEventPacket packet) {
    	try {
        	Document document = packet.getDocument();
			return ECGParser.getSingleNodeValue("documentname", document);
    	} catch (NodeException e) {
			logger.log(ECGLevel.SEVERE, "Could not fetch code for line diff computation in CodeLocarionTrackerIntermediateModule.");
			return null;
		}
	}

	private String getCode(ValidEventPacket packet) {
    	try {
        	Document document = packet.getDocument();
			return ECGParser.getSingleNodeValue("document", document);
    	} catch (NodeException e) {
			logger.log(ECGLevel.SEVERE, "Could not fetch code for line diff computation in CodeLocarionTrackerIntermediateModule.");
			return null;
		}
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

	public void initialize() {
		this.setProcessingMode(ProcessingMode.ANNOTATOR);
	}

	protected void propertyChanged(ModuleProperty moduleProperty)
			throws ModulePropertyException {
		// TODO Auto-generated method stub

	}

	public void update() {
		// TODO Auto-generated method stub

	}

}

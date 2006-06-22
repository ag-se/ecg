package org.electrocodeogram.module.intermediate.implementation;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;

import org.electrocodeogram.event.ValidEventPacket;
import org.electrocodeogram.logging.LogHelper;
import org.electrocodeogram.logging.LogHelper.ECGLevel;
import org.electrocodeogram.misc.xml.ECGParser;
import org.electrocodeogram.misc.xml.NodeException;
import org.electrocodeogram.module.intermediate.IntermediateModule;
import org.electrocodeogram.modulepackage.ModuleProperty;
import org.electrocodeogram.modulepackage.ModulePropertyException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class CodeLocationTrackerIntermediateModule extends IntermediateModule {

    public enum Statement { FULL, PART, COMMENT, EMPTY, UNKNOWN }
    
    public static final int MIN_COHESION = 0; // min. level of cohesion in a location

    private static Logger logger = LogHelper
    	.createLogger(CodeLocationTrackerIntermediateModule.class.getName());

    private HashMap<String, Text> texts = new HashMap<String, Text>();
    
	public CodeLocationTrackerIntermediateModule(String id, String name) {
		super(id, name);
	}

	public Collection<ValidEventPacket> analyse(ValidEventPacket eventPacket) {

		if (eventPacket.getMicroSensorDataType().getName().equals("msdt.codestatus.xsd")) {
    		
            String documentName = getDocumentName(eventPacket); 
    		if (!documentName.endsWith(".java"))
    			return null;
    		
    		// TODO What to do here?
            if (texts.get(documentName) != null)
                System.err.println("A new codestatus for " + documentName + " at " + eventPacket.getTimeStamp());

            Text text = new Text();
            String code = getCode(eventPacket);

            String[] lines = getLines(code);
//    		Line firstLine = null;
    		Line newLine = null;
    		
    		for (int lineNo = 0; lineNo < lines.length; lineNo++) {
                Line prevLine = newLine;
                newLine = new Line(lineNo, lines[lineNo]);
    			computeBasicLineProperties(newLine);
    			computeCohesionAndLevel(newLine, prevLine);
                text.lines.add(lineNo, newLine);
    		}
            
            computeLocations(text);
    
System.out.println("First locations computation on file " + documentName);
System.out.println("at " + eventPacket.getTimeStamp() + ":");
System.out.println(text.printLocations());
System.out.println("---");

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
            int insertsCount = 0; 
            int deletesCount = 0;
//            BlockChange blockChange = new BlockChange(text);

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
                
                // create LineChange
//                LineChange lineChange = new LineChange(blockChange, diffType, linenumber, from, to);
                
System.out.println("New change: '" + diffType + "' at line " + linenumber);
System.out.println("from:" + from);
System.out.println("  to:" + to);
System.out.println("at " + eventPacket.getTimeStamp());
                // first get the affected Location and its Line
                Line line = null;
                Location location = null;
                if (linenumber < text.lines.size()) {
                    line = text.lines.get(linenumber);
                    location = line.location;
                } else {
                    assert(diffType.equals("inserted"));
                    // line will be computed later
                    // inserted at the end, take the last location
                    location = text.locations.last();
                    // linenumber must be exactly after the last line in text
                    assert(text.lines.size() == linenumber); 
                }
                assert(location != null);
System.out.println();

                // now first perfom the linediff operation in the text
                if (diffType.equals("changed")) {
                    assert(line != null);
                    // following is not always true because previous changes in this block may have already changed contents
                    // assert(line.contents.trim().equals(from.trim()));
                    line.contents = to;
                    this.computeBasicLineProperties(line);
                } else if (diffType.equals("deleted")) {
                    assert(line != null);
                    // following is not always true because previous changes in this block may have already changed contents
                    // assert(line.contents.trim().equals(from.trim())); 
                    text.lines.remove(linenumber);
                    location.length -= 1;
                    if (location.length == 0) {
                        // location empty now
                        text.locations.remove(location);
                    }
                    // change following line numbers
                    deletesCount++;
                    for (Line l : text.lines.subList(linenumber, text.lines.size())) {
                        l.linenumber--;
//                        blockChange.lineChanges.add(
//                                new LineChange(blockChange, "moved", l.linenumber, null, null));
                    }
                    for (Location loc : text.locations.tailSet(location)) {
                        if (loc != location)  // location itself is correctly positioned
                            loc.startLinenumber--;
                    }
                } else if (diffType.equals("inserted")) {
                    // store in 'line' the inserted line
                    line = new Line(linenumber, to);
                    this.computeBasicLineProperties(line);
                    // update location link and length
                    line.location = location;
                    location.length += 1;
                    // if this is inserted at the beginning of a location, set line as startLine
                    // .. obsolete
                    //if (location.startLine.linenumber == linenumber)
                    //    location.startLine = line;
                    // add new line at position
                    text.lines.add(linenumber, line);
                    // change following line numbers 
                    insertsCount++;
                    for (Line lin : text.lines.subList(linenumber+1, text.lines.size())) {
                        lin.linenumber++;
//                        blockChange.lineChanges.add(
//                                new LineChange(blockChange, "moved", l.linenumber, null, null));
                    }
                    for (Location loc : text.locations.tailSet(location)) {
                        if (loc != location)  // location itself is correctly positioned
                            loc.startLinenumber++;
                    }
                } else
                    assert(false);
                
                // secondly perform recomputation of Locations at the new line neighborhoods
                if (diffType.equals("changed") || diffType.equals("inserted")) {                    
                    Line prevLine = null;
                    Line nextLine = null;
                    if (linenumber-1 >= 0)
                        prevLine = text.lines.get(linenumber-1);
                    if (linenumber+1 < text.lines.size())
                        nextLine = text.lines.get(linenumber+1); 
                    computeNewNeighborhood(prevLine, line);
                    computeNewNeighborhood(line, nextLine);
                } else if (diffType.equals("deleted")) {
                    Line prevLine = null;
                    if (linenumber-1 >= 0)
                        prevLine = text.lines.get(linenumber-1);
                    Line nextLine = text.lines.get(linenumber); // no +1 because line has been shifted
                    computeNewNeighborhood(prevLine, nextLine);
                } 

System.out.println("New locations:\n" + text.printLocations());
System.out.println("---");

                // remember this line change
//                blockChange.lineChanges.add(lineChange);

            }                    

            assert(text.checkValidity());
            assert(text.printContents().trim().equals(text.printContentsFromLocations().trim()));

System.out.println(text);
System.out.println("---------------------------------------------------------");
            
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

    private void computeNewNeighborhood(Line lineAbove, Line lineBelow) {
        if (lineAbove == null || lineBelow == null)
            return; // has been at the begin/end of the text: no recompution necessary
        Location locationAbove = lineAbove.location;
        Location locationBelow = lineBelow.location;
        // first compute new cohesion
        this.computeCohesionAndLevel(lineBelow, lineAbove);
        // no decide whether to split, merge or retain locations
        if (lineBelow.cohesion >= MIN_COHESION
                && locationAbove != locationBelow) {
            // different locations have high cohesion now => merge them
            this.mergeLocations(locationAbove, locationBelow);
        }
        else if (lineBelow.cohesion < MIN_COHESION
                && locationAbove == locationBelow) {
            // new low cohesion in a location => split it
            this.splitLocation(locationAbove, lineBelow);
        }
        // else no changes
    }

    /**
     * @param loc1, needs to be at least of length 2
     * @param line, needs to be in loc1. The line will be the first line in the new location
     */
    private void splitLocation(Location loc1, Line line) {
        if (loc1 == null)
            return;
        int lastLineNumber = loc1.startLinenumber + (loc1.length-1);
        Text text = loc1.text;
        assert(loc1.length >= 2);
        // line must not be the first line of the location
        assert(loc1.startLinenumber < line.linenumber);
        assert(lastLineNumber >= line.linenumber);
        assert(line.location == loc1);
        Location loc2 = new Location(text);
        loc2.startLinenumber = line.linenumber;
        loc2.nextLocation = loc1.nextLocation;
        loc2.prevLocation = loc1;
        loc2.length = (lastLineNumber - line.linenumber) + 1; // +1 because line itself is included
        loc1.nextLocation = loc2;
        loc1.length = loc1.length - loc2.length;
        for (Line l : text.lines.subList(loc2.startLinenumber, 
                                            loc2.startLinenumber + loc2.length))
            l.location = loc2;
        text.locations.add(loc2);
        assert(loc1.checkValidity());
        assert(loc2.checkValidity());
    }

    /**
     * @param loc1
     * @param loc2 must be directly after loc1 in text
     */
    private void mergeLocations(Location loc1, Location loc2) {
        if (loc1 == null || loc2 == null)
            return; // TODO really?
        assert (loc1.startLinenumber < loc2.startLinenumber);
        loc1.length += loc2.length;
        loc1.nextLocation = loc2.nextLocation;
        for (Line l : loc1.text.lines.subList(loc2.startLinenumber, 
                                                loc2.startLinenumber + loc2.length))
            l.location = loc1;
        loc1.text.locations.remove(loc2);
    }

    
    /**
     * TODO consider levels as well
     * 
     * @param text
     * @return
     */
    private void computeLocations(Text text) {
        Location loc = null;
        for (Line line : text.lines) {
            if (loc == null || line.cohesion < MIN_COHESION) {
                Location newLoc = new Location(text);
                newLoc.startLinenumber = line.linenumber;
                newLoc.length = 1;
                text.locations.add(newLoc);
                line.location = newLoc;
                if (loc != null) {
                    loc.nextLocation = newLoc;
                    newLoc.prevLocation = loc;
                }
                loc = newLoc;
/*          } else if (line.level > loc.startLine.level) {
                Location newLoc = new Location();
                newLoc.startLine = line;
                newLoc.length = 1;
                newLoc.parent = loc;
                loc.children.add(newLoc);
                loc.nextLocation = newLoc;
                loc = newLoc;
            } else if (line.level < loc.startLine.level) {
                loc = loc.parent;
*/          } else {
                line.location = loc;
                loc.length++;
            }
        }
    }

	private void computeBasicLineProperties(Line line) {

        // reset properties
        line.block = Block.NONE;
        line.statement = Statement.UNKNOWN;

        // ignore string literals, char literals, and white space
        String contents = line.contents.trim();
    	contents = contents.replaceAll("\".*?\"", "STRINGLITERAL"); // TODO
    	contents = contents.replaceAll("'.*?'", "CHARLITERAL"); // TODO

    	// set Block
    	int thisCurlyBraceA = contents.lastIndexOf('{');
    	int thisCurlyBraceZ = contents.indexOf('}');
    	if (thisCurlyBraceA >= 0 && thisCurlyBraceZ == -1)
    		line.block = Block.BEGIN;
    	else if (thisCurlyBraceA == -1 && thisCurlyBraceZ >= 0)
    		line.block = Block.END;
    	else if (thisCurlyBraceA < thisCurlyBraceZ)
    		line.block = Block.BEGIN_END;
    	else if (thisCurlyBraceA > thisCurlyBraceZ)
    		line.block = Block.END_BEGIN;
    	
    	// set Comment Statement 1
    	int thisLineComment = contents.indexOf("//");
    	if (thisLineComment == 0) // comment for whole line
    		line.statement = Statement.COMMENT;
    	else if (thisLineComment > 0) // comment at the end. remove it.
    		contents = contents.substring(0, thisLineComment);

    	// set Comment Statement 2
    	int thisBlockCommentA = contents.indexOf("/*");
    	int thisBlockCommentB = contents.indexOf("*");
    	int thisBlockCommentZ = contents.indexOf("*/");
    	if (thisBlockCommentA >= 0 && thisBlockCommentZ == -1) // TODO Could be in the middle with valid code preceeding
    		line.statement = Statement.COMMENT;
    	else if (thisBlockCommentA == -1 && thisBlockCommentZ >= 0) // TODO Could be in the middle with valid code following
    		line.statement = Statement.COMMENT;
    	else if (thisBlockCommentA == -1 && thisBlockCommentZ == -1 && thisBlockCommentB == 0) {   
    		// TODO Could be a multiply operator, even at the beginning!
    		line.statement = Statement.COMMENT;
    	}
    	else if (thisBlockCommentA >= 0 && thisBlockCommentZ == contents.length()-1) {
    		// comment at the end. remove it.
    		contents = contents.substring(0, thisBlockCommentZ);
    	}
    	
    	// set Code Statement
    	int thisSemicolon = contents.lastIndexOf(';');
    	if (thisSemicolon == contents.length()-1) { // ; at the end
    		if (line.statement == Statement.UNKNOWN) 
    			line.statement = Statement.FULL;
    	} else {
    		if (line.statement == Statement.UNKNOWN && line.block == Block.NONE) 
    			line.statement = Statement.PART;    			
    	}
    	
    	// set Empty Statement
    	if (contents.length() == 0)
    		line.statement = Statement.EMPTY;

    }

    private void computeCohesionAndLevel(Line thisLine, Line prevLine) {

		if (thisLine.linenumber == 0 || prevLine == null) {
            thisLine.cohesion = -10; // symbolic value for first line in text
            return;
        }

		// set initial level and cohesion
        thisLine.level = prevLine.level;
        thisLine.cohesion = 0;
        
		// 1. test { and }
    	if (thisLine.block == Block.END) { // 1.a
    		if (thisLine.level > 0) thisLine.level--;
    		thisLine.cohesion += 1;
    	}
    	if (prevLine.block == Block.BEGIN) { // 1.b
    		thisLine.level++;
    		thisLine.cohesion += 1;
    	}
    	if (prevLine.block == Block.END) { // 1.c
            // Rational: Avoid multiple block ends below each other to be put together, see 1.a
    		thisLine.cohesion -= 1;
        }
    	
    	// 2. test comment 
    	if (prevLine.statement == Statement.COMMENT) // comment for whole line
    		thisLine.cohesion += 1;
    	if (thisLine.statement != Statement.COMMENT && prevLine.statement == Statement.COMMENT)
    		thisLine.cohesion += 1;
    	else if (thisLine.statement == Statement.COMMENT && prevLine.statement != Statement.COMMENT)
    		thisLine.cohesion -= 1;
    	
    	// 3. test ;
		if (prevLine.statement == Statement.PART) 
    		thisLine.cohesion += 1;
    	
    	// 4. test empty lines
    	if (thisLine.statement == Statement.EMPTY && prevLine.statement != Statement.EMPTY)
    		thisLine.cohesion -= 1;
    	if (thisLine.statement != Statement.EMPTY && prevLine.statement == Statement.EMPTY)
    		thisLine.cohesion -= 1;
    	
    	// 5. test keywords
    	boolean thisPublic = thisLine.contents.startsWith("public");
    	boolean thisPrivate = thisLine.contents.startsWith("private");
    	boolean thisProtected = thisLine.contents.startsWith("protected");
    	boolean thisStatic = thisLine.contents.startsWith("static");
    	if (thisPublic || thisPrivate || thisProtected || thisStatic)
    		thisLine.cohesion -= 1;
    	
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

package org.electrocodeogram.module.intermediate.implementation.location.state;

import java.util.ArrayList;
import java.util.Collection;

import org.electrocodeogram.module.intermediate.implementation.location.change.BlockChange;
import org.electrocodeogram.module.intermediate.implementation.location.change.History;
import org.electrocodeogram.module.intermediate.implementation.location.change.LocationChange;
import org.electrocodeogram.module.intermediate.implementation.location.change.LocationChange.LocationChangeType;
import org.electrocodeogram.module.intermediate.implementation.location.state.Line.Block;
import org.electrocodeogram.module.intermediate.implementation.location.state.Line.Statement;

public class LocationComputationStrategy {

    // simply for reuse
    // return true, if location changes have been added
    // TODO blockChange should be omitted as a parameter
    public void computeNewNeighborhood(History history, Line lineAbove, Line lineBelow, BlockChange blockChange) {
        if (lineAbove == null || lineBelow == null)
            return; // has been at the begin/end of the text: no recompution necessary
        Location locationAbove = lineAbove.getLocation();
        Location locationBelow = lineBelow.getLocation();
        // first compute new cohesion
        computeCohesionAndLevel(lineBelow, lineAbove);
        // no decide whether to split, merge or retain locations
        if (lineBelow.getCohesion() >= Location.MIN_COHESION
                && locationAbove != locationBelow) {
            // different locations have high cohesion now => merge them
            if (locationAbove.getLength() > locationBelow.getLength()) { // prefer the bigger one
                if (locationBelow != null)
                    blockChange.getText().removeLocation(locationBelow);
                locationAbove.mergeLocation(locationBelow);  // first remove, than merge!
                history.addLocationChange(new LocationChange(locationBelow,  
                        LocationChangeType.MERGED_DEL_AT_START, locationAbove.getId()), blockChange);
                history.addLocationChange(new LocationChange(locationAbove, 
                        LocationChangeType.MERGED_ADD_AT_END, locationBelow.getId()), blockChange);
                return;
            } else {
                if (locationAbove != null)
                    blockChange.getText().removeLocation(locationAbove);
                locationBelow.mergeLocation(locationAbove);                
                history.addLocationChange(new LocationChange(locationAbove, 
                        LocationChangeType.MERGED_DEL_AT_END, locationBelow.getId()), blockChange);
                history.addLocationChange(new LocationChange(locationBelow, 
                        LocationChangeType.MERGED_ADD_AT_START, locationAbove.getId()), blockChange);
                return;
            }
        }
        else if (lineBelow.getCohesion() < Location.MIN_COHESION
                && locationAbove == locationBelow) {
            // new low cohesion in a location => split it
            Location newLoc = locationAbove.splitLocation(lineBelow);
            // Register new location
            blockChange.getText().addLocation(newLoc);
            LocationChangeType splitType = LocationChangeType.SPLIT_DEL_AT_START;
            LocationChangeType forkType = LocationChangeType.SPLIT_ADD_AT_END;
            if (newLoc.getStart() > locationAbove.getStart()) {
                splitType = LocationChangeType.SPLIT_DEL_AT_END;
                forkType = LocationChangeType.SPLIT_ADD_AT_START;
            }
            history.addLocationChange(new LocationChange(locationAbove,
                    splitType, newLoc.getId()), blockChange);
            history.addLocationChange(new LocationChange(newLoc,
                    forkType, locationAbove.getId()), blockChange);
            return;
        }
        // else no changes
    }

    /**
     * TODO consider levels as well
     *  
     * @param text underlying text
     * @param lines collection of lines
     * @param lines curLoc beginning location right before first line
     * @return the last location
     */
    public Collection<Location> computeLocations(Collection<Line> lines) {
        Location curLoc = null;
        Collection<Location> newLocs = new ArrayList<Location>();
        for (Line line : lines) {
            if (curLoc == null || line.getCohesion() < Location.MIN_COHESION) { // curLoc == null means first line
                Location newLoc = new Location();
                newLoc.setStart(line.getLinenumber());
                newLoc.setLength(1);
                newLocs.add(newLoc);
                line.setLocation(newLoc);
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
        return newLocs;
    }

    public void computeBasicLineProperties(Line line) {
    
        // reset properties
        line.setBlock(Block.NONE);
        line.setStatement(Statement.UNKNOWN);
    
        // ignore string literals, char literals, and white space
        String contents = line.getContents().trim();
    	contents = contents.replaceAll("\".*?\"", "STRINGLITERAL");
    	contents = contents.replaceAll("'.*?'", "CHARLITERAL");
    
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

    public void computeCohesionAndLevel(Line thisLine, Line prevLine) {
    
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
    		thisLine.setCohesion(thisLine.getCohesion() - 2);
    	
    	return;
    }

}

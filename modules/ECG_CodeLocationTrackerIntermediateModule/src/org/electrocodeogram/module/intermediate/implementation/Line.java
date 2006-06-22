/**
 * 
 */
package org.electrocodeogram.module.intermediate.implementation;

import org.electrocodeogram.module.intermediate.implementation.CodeLocationTrackerIntermediateModule.Statement;

public class Line {	
    
    // TODO make getter and setter and integrate compute.. methods here, automatically invoked in setters (?)

	public int level = 0;
	public String contents = "";
	int linenumber = 0;
	int cohesion = 0;  // is always the cohesion to the line *before* this line
	Block block = Block.NONE;
	Statement statement = Statement.UNKNOWN;
    public Location location;

	public Line(int linenumber, String contents) {
        this.linenumber = linenumber;
        this.contents = contents;
    }
    
    public String toString() {
		String mod = "";
		switch (block) {
    		case BEGIN_END: mod += "{}"; break;
    		case END_BEGIN: mod += "}{"; break;
    		case BEGIN: mod += "{"; break;
    		case END: mod += "}"; break;
		}
		mod += ",";
		switch (statement) {
    		case FULL: mod += ";"; break;
    		case PART: mod += "+"; break;
            case EMPTY: mod += "0"; break;
    		case COMMENT: mod += "//"; break;
		}
		return linenumber + "(#" + level + ")[" + mod + "] " + cohesion + "\t " + contents.trim();
	}

    public boolean checkValidity() {
        Location loc = this.location;
        boolean check1 = (this.linenumber >= loc.startLinenumber);
        boolean check2 = (this.linenumber <= loc.startLinenumber + (loc.length-1));
        assert(check1);
        assert(check2);
        return check1 && check2;
    }
}
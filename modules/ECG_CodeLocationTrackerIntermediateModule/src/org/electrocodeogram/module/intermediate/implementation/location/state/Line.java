/**
 * 
 */
package org.electrocodeogram.module.intermediate.implementation.location.state;



public class Line {	
    
    public enum Statement { 
        FULL, 
        PART, 
        COMMENT, 
        EMPTY, 
        UNKNOWN 
    }

    public enum Block { 
        BEGIN_END, 
        END_BEGIN, 
        BEGIN, 
        END, 
        NONE 
    }
    
    // TODO make getter and setter and integrate compute.. methods here, automatically invoked in setters (?)

	private int level = 0;
	private String contents = "";
	private int linenumber = 0;
    private int cohesion = 0;  // is always the cohesion to the line *before* this line
    private Block block = Block.NONE;
    private Statement statement = Statement.UNKNOWN;
    private Location location;

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
		return linenumber + "@Loc" + location.getId() + "(#" + level + ")[" + mod + "] " + cohesion + "\t " + contents.trim() + "\n";
	}

    public boolean checkValidity() {
        Location loc = this.location;
        boolean check1 = loc.getText().getLocations().contains(loc);
        boolean check2 = (this.linenumber >= loc.getStartLinenumber());
        boolean check3 = (this.linenumber <= loc.getStartLinenumber() + (loc.getLength()-1));
        assert(check1);
        assert(check2);
        assert(check3);
        return check1 && check2 && check3;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public String getContents() {
        return contents;
    }

    public void setLinenumber(int linenumber) {
        this.linenumber = linenumber;
    }

    public int getLinenumber() {
        return linenumber;
    }

    public void setCohesion(int cohesion) {
        this.cohesion = cohesion;
    }

    public int getCohesion() {
        return cohesion;
    }

    /**
     * @param block the block to set
     */
    public void setBlock(Block block) {
        this.block = block;
    }

    /**
     * @return the block
     */
    public Block getBlock() {
        return block;
    }

    /**
     * @param statement the statement to set
     */
    public void setStatement(Statement statement) {
        this.statement = statement;
    }

    /**
     * @return the statement
     */
    public Statement getStatement() {
        return statement;
    }

    /**
     * @param location the location to set
     */
    public void setLocation(Location location) {
        this.location = location;
    }

    /**
     * @return the location
     */
    public Location getLocation() {
        return location;
    }

    /**
     * @param level the level to set
     */
    public void setLevel(int level) {
        this.level = level;
    }

    /**
     * @return the level
     */
    public int getLevel() {
        return level;
    }
}

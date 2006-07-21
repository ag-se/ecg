package org.electrocodeogram.module.intermediate.implementation.location.change;


/**
 * Represents a change of a line. There are three change types:
 * * New/Inserted line: contents contains initial text and linenumber initial position
 * * Changed line: contents contains new text, linenumber = -1
 * * Moved line: linenumer is new position, contents = null
 * * Deleted line: contents == null, linenumber = -1
 * locChange is a reference to the changes of the block this line was in
 */
public class LineChange {

    public enum LineChangeType {
        INSERTED,
        DELETED,
        CHANGED, 
        MOVED,
        UNKNOWN
    }
    
    static private String[] typeStrings = new String[] {
        "inserted", "deleted", "changed", "moved", "unknown"
    };
    
    private int linenumber = -1;
    private String contents = null;
    
    static public LineChangeType getLineChangeTypeFromString(String type) {
        if (type.equals(typeStrings[0])) {
            return LineChangeType.INSERTED;
        } else if (type.equals(typeStrings[1])) {
            return LineChangeType.DELETED;            
        } else if (type.equals(typeStrings[2])) {
            return LineChangeType.CHANGED;            
        } else if (type.equals(typeStrings[3])) {
            return LineChangeType.MOVED;            
        }
        return LineChangeType.UNKNOWN;
    }
    
    static public String getStringForLineChangeType(LineChangeType type) {
        return typeStrings[type.ordinal()];
    }
    
    
    public LineChange(LineChangeType type, int linenumber, String from, String to) { 
        // TODO from currently not used
        if (type == LineChangeType.INSERTED) {
            this.linenumber = linenumber;
            this.contents = to;
        } else if (type == LineChangeType.DELETED) {
            this.linenumber = -1;
            this.contents = null;
        } else if (type == LineChangeType.CHANGED) {
            this.linenumber = -1;
            this.contents = to;
        } else if (type == LineChangeType.MOVED) {
            this.linenumber = linenumber;
            this.contents = null;
        }
    }
    
    public boolean isChange() {
        return contents != null && linenumber == -1;
    }

    public boolean isInsertion() {
        return contents != null && linenumber > -1;
    }

    public boolean isMovement() {
        return contents == null && linenumber > -1;
    }

    public boolean isDeletion() {
        return contents == null && linenumber == -1;
    }
    
    public String toString() {
        String res = LineChange.typeStrings[getChangeType().ordinal()];
        if (getChangeType() == LineChangeType.CHANGED)
            res += " to '" + contents.trim() + "'";
        if (getChangeType() == LineChangeType.INSERTED)
            res += " at line " + linenumber + " with '" + contents.trim() + "'";
        if (getChangeType() == LineChangeType.MOVED)
            res += " to " + linenumber;
        if (getChangeType() == LineChangeType.DELETED)
            res += "";
        return res;
    }
    
    public LineChangeType getChangeType() {
        if (this.isChange())
            return LineChangeType.CHANGED;
        if (this.isInsertion())
            return LineChangeType.INSERTED;
        if (this.isMovement())
            return LineChangeType.MOVED;
        if (this.isDeletion())
            return LineChangeType.DELETED;
        return LineChangeType.UNKNOWN;
    }

    /**
     * @param linenumber the linenumber to set
     */
    public void setLinenumber(int linenumber) {
        this.linenumber = linenumber;
    }

    /**
     * @return the linenumber
     */
    public int getLinenumber() {
        return linenumber;
    }

    /**
     * @param contents the contents to set
     */
    public void setContents(String contents) {
        this.contents = contents;
    }

    /**
     * @return the contents
     */
    public String getContents() {
        return contents;
    }

}
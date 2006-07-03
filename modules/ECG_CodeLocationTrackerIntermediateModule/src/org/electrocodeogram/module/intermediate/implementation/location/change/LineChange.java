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
        MOVED
    }
    
    private int linenumber = -1;
    private String contents = null;
    private LocationChange locChange = null;
    
    public LineChange(LocationChange locChange) {
        this.locChange = locChange;
    }

    public LineChange(LocationChange locChange, LineChangeType type, int linenumber, String from, String to) { 
        this(locChange);
        
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
        String res = "{unkown}";
        if (this.isChange())
            res = LineChangeType.CHANGED + " to " + contents;
        if (this.isInsertion())
            res = LineChangeType.INSERTED + " at line " + linenumber + " with " + contents;
        if (this.isMovement())
            res = LineChangeType.MOVED + " to " + linenumber;
        if (this.isDeletion())
            res = LineChangeType.DELETED.toString();
        return res;
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

    /**
     * @param locChange the locChange to set
     */
    public void setLocChange(LocationChange locChange) {
        this.locChange = locChange;
    }

    /**
     * @return the locChange
     */
    public LocationChange getLocChange() {
        return locChange;
    }

}
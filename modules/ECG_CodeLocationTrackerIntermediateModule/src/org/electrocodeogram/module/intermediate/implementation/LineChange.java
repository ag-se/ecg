package org.electrocodeogram.module.intermediate.implementation;

/**
 * Represents a change of a line. There are three change types:
 * * New/Inserted line: contents contains initial text and linenumber initial position
 * * Changed line: contents contains new text, linenumber = -1
 * * Moved line: linenumer is new position, contents = null
 * * Deleted line: contents == null, linenumber = -1
 * block is a reference to the block of changes
 */
public class LineChange {
    public int linenumber = -1;
    public String contents = null;
    public BlockChange block = null;
    
    public LineChange(BlockChange block) {
        this.block = block;
    }

    public LineChange(BlockChange block, String type, int linenumber, String from, String to) { 
        this(block);
        
        if (type.equals("inserted")) {
            this.linenumber = linenumber;
            this.contents = to;
        } else if (type.equals("deleted")) {
            this.linenumber = -1;
            this.contents = null;
        } else if (type.equals("changed")) {
            this.linenumber = -1;
            this.contents = to;
        } else if (type.equals("moved")) {
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

}
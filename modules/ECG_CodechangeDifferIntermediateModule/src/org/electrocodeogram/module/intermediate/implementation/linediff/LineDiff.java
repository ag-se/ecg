/**
 * 
 */
package org.electrocodeogram.module.intermediate.implementation.linediff;

public class LineDiff {
    
    public enum ChangeType {
        CHANGED,
        INSERTED,
        DELETED,
        NOTCHANGED
    }

    public int linenumber;
    public String from;
    public String to;        
    public LineDiff.ChangeType type;
    
	public LineDiff() {
		this.from = "";
		this.linenumber = 0;
		this.to = "";
		this.type = ChangeType.NOTCHANGED;
	}

    public LineDiff(String from, int linenumber, String to, LineDiff.ChangeType type) {
        this.from = (from == null ? "" : from);
        this.linenumber = linenumber;
        this.to = (to == null ? "" : to);
        this.type = type;
    }
    
    public String getFrom() {
        return this.from;
    }
    
    public int getLinenumber() {
        return this.linenumber;
    }
    
    public String getTo() {
        return this.to;
    }
    
    public LineDiff.ChangeType getType() {
        return this.type;
    }

    public String toString() {
    	String res = "";
    	if (this.type == ChangeType.DELETED || this.type == ChangeType.CHANGED)
        	res += String.valueOf(this.linenumber) + "< " + this.from;
    	if (this.type == ChangeType.CHANGED)
    		res += "\n";
    	if (this.type == ChangeType.INSERTED || this.type == ChangeType.CHANGED)
        	res += String.valueOf(this.linenumber) + "> " + this.to;	  
    	return res;
    }
}
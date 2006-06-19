/**
 * 
 */
package org.electrocodeogram.module.intermediate.implementation;

import org.electrocodeogram.misc.xml.ECGParser;
import org.electrocodeogram.misc.xml.NodeException;
import org.w3c.dom.Node;

public class LineChange {
    
    public enum ChangeType {
        CHANGED,
        INSERTED,
        DELETED,
        NOTCHANGED
    }

    public int linenumber;
    public String from;
    public String to;        
    public LineChange.ChangeType type;
    
	public LineChange() {
		this.from = "";
		this.linenumber = 0;
		this.to = "";
		this.type = ChangeType.NOTCHANGED;
	}

    public LineChange(String from, int linenumber, String to, LineChange.ChangeType type) {
        this.from = from;
        this.linenumber = linenumber;
        this.to = to;
        this.type = type;
    }
    
    public LineChange(Node line) { 
        this();
        String typeStr;
        try {
            typeStr = ECGParser.getNodeValue(ECGParser.getChildNode(line, "type"));
            if (typeStr.equals("inserted"))
                this.type = ChangeType.INSERTED;
            else if (typeStr.equals("deleted"))
                this.type = ChangeType.DELETED;
            else if (typeStr.equals("changed"))
                this.type = ChangeType.CHANGED;                    
            this.linenumber = Integer.parseInt(
                ECGParser.getNodeValue(ECGParser.getChildNode(line, "linenumber")));
            this.linenumber -= 1; // in events the line numbers start with 1
            if (ECGParser.hasChildNode(line, "from"))
                this.from = ECGParser.getNodeValue(ECGParser.getChildNode(line, "from"));
            if (ECGParser.hasChildNode(line, "to"))
                this.to = ECGParser.getNodeValue(ECGParser.getChildNode(line, "to"));
        } catch (NodeException e) {
            // TODO report this
            e.printStackTrace();
        }
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
    
    public LineChange.ChangeType getType() {
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
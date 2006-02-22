/**
 * 
 */
package org.electrocodeogram.module.intermediate;

import org.electrocodeogram.module.intermediate.CodechangeDifferIntermediateModule.DeltaType;


/**
 *
 */
public class Delta {
    
    private int linenumber;
    
    private String from;
    
    private String to;
    
    private DeltaType type;

    /**
     * @param from
     * @param linenumber
     * @param to
     * @param type
     */
    public Delta(String from, int linenumber, String to, DeltaType type) {
        super();
        // TODO Auto-generated constructor stub
        this.from = from;
        this.linenumber = linenumber;
        this.to = to;
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

    
    public DeltaType getType() {
        return this.type;
    }
    
    

}

/**
 * 
 */
package org.electrocodeogram.module.intermediate.implementation;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class BlockChange {
    public List<LineChange> lineChanges = new ArrayList<LineChange>();
    public Date timeStamp;
    public Text text;
    
    public BlockChange(Text text) {
        this.text = text;
    }
    
}
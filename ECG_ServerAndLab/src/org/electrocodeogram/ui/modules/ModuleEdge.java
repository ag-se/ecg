/*
 * Created on 06.05.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.electrocodeogram.ui.modules;

import org.jgraph.graph.DefaultEdge;
import org.jgraph.graph.GraphConstants;

/**
 * @author 7oas7er
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ModuleEdge extends DefaultEdge
{

    private int parentId = -1;
    
    private int childId = -1;
    
    public ModuleEdge(int parentId, int childId)
    {
        super();
        
        this.parentId = parentId;
        
        this.childId = childId;
        
        GraphConstants.setLineEnd(this.getAttributes(),GraphConstants.ARROW_CLASSIC);
                        
        GraphConstants.setDisconnectable(this.getAttributes(), false);
        
        GraphConstants.setBendable(this.getAttributes(), false);
        
        
    }
    
    
    
    public int getChildId()
    {
        return childId;
    }
    public int getParentId()
    {
        return parentId;
    }
}

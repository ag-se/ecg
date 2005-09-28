package org.electrocodeogram.ui.module;

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

    private int _parentId = -1;
    
    private int _childId = -1;
    
    public ModuleEdge(int parentId, int childId)
    {
        super();
        
        this._parentId = parentId;
        
        this._childId = childId;
        
        GraphConstants.setLineEnd(this.getAttributes(),GraphConstants.ARROW_CLASSIC);
                        
        GraphConstants.setDisconnectable(this.getAttributes(), false);
        
        GraphConstants.setBendable(this.getAttributes(), false);
        
        
    }
    
    public int getChildId()
    {
        return _childId;
    }
    
    public int getParentId()
    {
        return _parentId;
    }
}

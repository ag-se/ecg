/*
 * Created on 06.05.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.electrocodeogram.ui.modules;

import org.electrocodeogram.misc.constants.UIConstants;
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
        
        GraphConstants.setLineEnd(this.getAttributes(),GraphConstants.ARROW_SIMPLE);
        
        GraphConstants.setLineWidth(this.getAttributes(),UIConstants.MED_LINE_WIDTH);
                        
        GraphConstants.setLineColor(this.getAttributes(),UIConstants.MED_LINE_COLOR);
        
        GraphConstants.setDisconnectable(this.getAttributes(), false);
        
        GraphConstants.setBendable(this.getAttributes(), false);
        
        
    }
    
    
    
    public int getChildId()
    {
        return this._childId;
    }
    public int getParentId()
    {
        return this._parentId;
    }
}

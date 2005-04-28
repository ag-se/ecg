/*
 * Created on 10.03.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.electrocodeogram.ui;

import java.awt.Color;

import javax.swing.border.LineBorder;

import org.jgraph.JGraph;
import org.jgraph.event.GraphSelectionEvent;
import org.jgraph.event.GraphSelectionListener;
import org.jgraph.graph.DefaultGraphModel;

/**
 * @author 7oas7er
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SensorGraph extends JGraph
{

    private int selectedModuleCellId = -1;
    
    public SensorGraph(){
        
        super(new DefaultGraphModel());
      
        //DefaultGraphCell[] cells = new DefaultGraphCell[2];

        addGraphSelectionListener(new GraphSelectionListener() {

            public void valueChanged(GraphSelectionEvent arg0)
            {
                if(arg0.isAddedCell() && (arg0.getCell() instanceof ModuleCell))
                {   
                    
                    selectedModuleCellId  = ((ModuleCell)(arg0.getCell())).getId();
                    
                }
                else
                {
                    selectedModuleCellId = -1;
                }
                
            }});
       
        this.setBorder(new LineBorder(Color.GRAY));
		
    }

    /**
     * 
     * @uml.property name="selectedModuleCellId"
     */
    public int getSelectedSensorCellId() {
        return selectedModuleCellId;
    }

    
    public void addSensorCell(SensorCell cell)
    {
        this.getGraphLayoutCache().insert(cell);

    }
   
   
 

    
}

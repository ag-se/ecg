/*
 * Created on 10.03.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.electrocodeogram.ui;

import org.jgraph.JGraph;
import org.jgraph.event.GraphSelectionEvent;
import org.jgraph.event.GraphSelectionListener;
import org.jgraph.graph.DefaultEdge;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.DefaultGraphModel;
import org.jgraph.graph.GraphConstants;

/**
 * @author 7oas7er
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ModuleGraph extends JGraph
{

    private int selectedModuleCellId = -1;
    
    private ModuleCell rootCell = null;

    public ModuleGraph(){
        
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
        // Create Hello Vertex
		
        //cells[0] = createVertex("Hello", 20, 20, 40, 20, null, false);

		// Create World Vertex
		//cells[1] = createVertex("World", 140, 140, 40, 20, Color.ORANGE, true);

//		 Create Edge
//		DefaultEdge edge = new DefaultEdge();
//		// Fetch the ports from the new vertices, and connect them with the edge
//		edge.setSource(cells[0].getChildAt(0));
//		edge.setTarget(cells[1].getChildAt(0));
//		cells[2] = edge;
		
		//this.getGraphLayoutCache().insert(cells);
		
    }

    /**
     * 
     * @uml.property name="selectedModuleCellId"
     */
    public int getSelectedModuleCellId() {
        return selectedModuleCellId;
    }

    
    public void addSensorCell(SensorCell cell)
    {
        this.getGraphLayoutCache().insert(cell);
        
        DefaultEdge edge = new DefaultEdge();
        
        GraphConstants.setLineEnd(edge.getAttributes(),GraphConstants.ARROW_CLASSIC);
        
        GraphConstants.setDisconnectable(edge.getAttributes(),false);
        
		edge.setSource(cell.getChildAt(0));
		
		edge.setTarget(rootCell.getChildAt(0));
		
		addEdge(edge);
    }
    
    public void addModuleCell(ModuleCell cell)
    {
        if (rootCell == null)
        {
            rootCell = cell;
        }
        this.getGraphLayoutCache().insert(cell);
    }

    /**
     * @param edge
     */
    public void addEdge(DefaultEdge edge)
    {
        this.getGraphLayoutCache().insert(edge);
        
    }
    
//    private DefaultGraphCell createVertex(String name, double x,
//			double y, double w, double h, Color bg, boolean raised) {
//
//		// Create vertex with the given name
//		DefaultGraphCell cell = new DefaultGraphCell(name);
//
//		// Set bounds
//		//GraphConstants.setBounds(cell.getAttributes(), new Rectangle2D.Double(x, y, w, h));
//
//		// Set fill color
//		if (bg != null) {
//			GraphConstants.setGradientColor(cell.getAttributes(), Color.orange);
//			GraphConstants.setOpaque(cell.getAttributes(), true);
//		}
//
//		// Set raised border
//		if (raised)
//			GraphConstants.setBorder(cell.getAttributes(), BorderFactory
//					.createRaisedBevelBorder());
//		else
//			// Set black border
//			GraphConstants.setBorderColor(cell.getAttributes(), Color.black);
//
//		// Add a Port
//		DefaultPort port1 = new DefaultPort();
//		DefaultPort port2 = new DefaultPort();
//		cell.add(port1);
//		port1.setParent(cell);
//		cell.add(port2);
//		port2.setParent(cell);
//
//		return cell;
//	}

    
}

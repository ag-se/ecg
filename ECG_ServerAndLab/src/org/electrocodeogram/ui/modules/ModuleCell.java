/*
 * Created on 08.03.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.electrocodeogram.ui.modules;

import java.awt.Color;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import javax.swing.BorderFactory;

import org.electrocodeogram.module.Module;
import org.electrocodeogram.module.Module.ModuleType;
import org.electrocodeogram.ui.Configurator;
import org.jgraph.graph.DefaultEdge;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.DefaultPort;
import org.jgraph.graph.Edge;
import org.jgraph.graph.GraphConstants;

/**
 * @author 7oas7er *  * TODO To change the template for this generated type comment go to * Window - Preferences - Java - Code Style - Code Templates
 */

public class ModuleCell extends DefaultGraphCell
{

    /**
     * 
     * @uml.property name="root"
     * @uml.associationEnd multiplicity="(0 1)"
     */
    private Configurator root = null;
    
    private int id = -1;
    
    private String name;
    
    private boolean isRunning;
    
    private ArrayList childEdges = null;
    
      /**
     * 
     * @uml.property name="id"
     */
    public int getId() {
        return id;
    }

    public ModuleCell(ModuleType moduleType, int id, String name, boolean b)
    {
        super(name);

        this.name = name;
        
        this.id = id;
        
        this.isRunning = b;
        
        childEdges = new ArrayList();
        
        GraphConstants.setBounds(this.getAttributes(), new Rectangle2D.Double(0, 0, 100, 25));
        
        GraphConstants.setAutoSize(this.getAttributes(),true);
        
        GraphConstants.setMoveable(this.getAttributes(),true);
        
        GraphConstants.setEditable(this.getAttributes(),false);
               
        switch (moduleType)
        {
        	case SOURCE_MODULE:
        	    GraphConstants.setGradientColor(this.getAttributes(), Color.GREEN);
        	break;
        	case INTERMEDIATE_MODULE:
        	    GraphConstants.setGradientColor(this.getAttributes(), Color.orange);
        	break;
        	case TARGET_MODULE:
        	    GraphConstants.setGradientColor(this.getAttributes(), Color.BLUE);
        	break;
        }
       
        GraphConstants.setOpaque(this.getAttributes(), isRunning);

        GraphConstants.setBorder(this.getAttributes(), BorderFactory.createRaisedBevelBorder());

        DefaultPort port = new DefaultPort();
        this.add(port);
        port.setParent(this);
       
    }

    public void addChildEdge(DefaultEdge edge)
    {
        childEdges.add(edge);
    }
    
    public Object[] getChildEdges()
    {
        return childEdges.toArray();
    }
    
    public String getName()
    {
        return name;
    }
    
    public Configurator getUIRoot()
    {
        return root;
    }
    
    /**
     * @return
     */
    public Point getLocation()
    {
        Rectangle2D rect = GraphConstants.getBounds(this.getAttributes());
        
        Point point = new Point((int)rect.getX(),(int)rect.getY());
        
        return point;
    }

    /**
     * @param location
     */
    public void setLocation(Point location)
    {
        
        GraphConstants.setBounds(this.getAttributes(),new Rectangle2D.Double(location.x,location.y,100,25));
        
    }

   
   }
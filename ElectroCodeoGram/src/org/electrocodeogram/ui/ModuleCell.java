/*
 * Created on 08.03.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.electrocodeogram.ui;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;

import javax.swing.BorderFactory;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.electrocodeogram.module.Module;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.DefaultPort;
import org.jgraph.graph.GraphConstants;
import org.jgraph.plaf.basic.BasicGraphUI;

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

    /**
     * 
     * @uml.property name="id"
     */
    public int getId() {
        return id;
    }

    public ModuleCell(Configurator root, int moduleType, int id, String name)
    {
        super(name);

        this.name = name;
        
        this.root = root;

        this.id = id;
        
        GraphConstants.setBounds(this.getAttributes(), new Rectangle2D.Double(root.getWidth() / 2, 20, 100, 25));
        
        GraphConstants.setAutoSize(this.getAttributes(),true);
        
        GraphConstants.setMoveable(this.getAttributes(),false);
        
        GraphConstants.setEditable(this.getAttributes(),false);
               
        switch (moduleType)
        {
        	case Module.SOURCE_MODULE:
        	    GraphConstants.setGradientColor(this.getAttributes(), Color.GREEN);
        	break;
        	case Module.INTERMEDIATE_MODULE:
        	    GraphConstants.setGradientColor(this.getAttributes(), Color.orange);
        	break;
        	case Module.TARGET_MODULE:
        	    GraphConstants.setGradientColor(this.getAttributes(), Color.BLUE);
        	break;
        }
        
        GraphConstants.setOpaque(this.getAttributes(), true);

        GraphConstants.setBorder(this.getAttributes(), BorderFactory.createRaisedBevelBorder());

        DefaultPort port = new DefaultPort();
        this.add(port);
        port.setParent(this);

    }

    public String getName()
    {
        return name;
    }
    
    public Configurator getUIRoot()
    {
        return root;
    }
}
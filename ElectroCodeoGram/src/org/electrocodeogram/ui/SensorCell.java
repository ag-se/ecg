/*
 * Created on 08.03.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.electrocodeogram.ui;

import java.awt.Color;
import java.awt.geom.Rectangle2D;

import javax.swing.BorderFactory;

import org.electrocodeogram.module.Module;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.DefaultPort;
import org.jgraph.graph.GraphConstants;

/**
 * @author 7oas7er *  * TODO To change the template for this generated type comment go to * Window - Preferences - Java - Code Style - Code Templates
 */

public class SensorCell extends DefaultGraphCell
{

    /**
     * 
     * @uml.property name="root"
     * @uml.associationEnd multiplicity="(0 1)"
     */
    private Configurator root = null;


    private int id = -1;

    /**
     * 
     * @uml.property name="id"
     */
    public int getId() {
        return id;
    }

    public SensorCell(Configurator root, String name)
    {
        super(name);

        this.root = root;
   
        GraphConstants.setBounds(this.getAttributes(), new Rectangle2D.Double(root.getWidth() / 2, 20, 100, 25));
        
        GraphConstants.setAutoSize(this.getAttributes(),true);
               
       
        GraphConstants.setGradientColor(this.getAttributes(), Color.RED);
       
        
        GraphConstants.setOpaque(this.getAttributes(), true);

        GraphConstants.setBorder(this.getAttributes(), BorderFactory.createRaisedBevelBorder());

        DefaultPort port = new DefaultPort();
        this.add(port);
        port.setParent(this);
        

    }

    public Configurator getUIRoot()
    {
        return root;
    }
}
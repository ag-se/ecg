package org.electrocodeogram.ui.module;

import java.awt.Color;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.net.URL;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.electrocodeogram.module.Module.ModuleType;
import org.electrocodeogram.ui.Gui;
import org.jgraph.graph.DefaultEdge;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.DefaultPort;
import org.jgraph.graph.GraphConstants;

/**
 * @author 7oas7er
 * 
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

public class ModuleCell extends DefaultGraphCell
{
	
	private Gui _gui;

	private int _id = -1;

	private String _name;

	private boolean _run;

	private ArrayList _edges;

	public int getId()
	{
		return _id;
	}

	public ModuleCell(ModuleType moduleType, int id, String name, boolean b)
	{
		super(name);

		this._name = name;

		this._id = id;

		this._run = b;
		
		_edges = new ArrayList();
		
		GraphConstants.setInset(this.getAttributes(),10);
		
		GraphConstants.setBounds(this.getAttributes(), new Rectangle2D.Double(0, 0, 100, 25));
		
		GraphConstants.setAutoSize(this.getAttributes(), true);

		GraphConstants.setMoveable(this.getAttributes(), true);

		GraphConstants.setEditable(this.getAttributes(), false);
		
	
		switch (moduleType)
		{
			case SOURCE_MODULE:
				
				GraphConstants.setGradientColor(this.getAttributes(), Color.GREEN);
				
				break;
				
			case INTERMEDIATE_MODULE:
				
				GraphConstants.setGradientColor(this.getAttributes(), Color.ORANGE);
				
				break;
				
			case TARGET_MODULE:
				
				GraphConstants.setGradientColor(this.getAttributes(), Color.BLUE);
				
				break;
		}

		GraphConstants.setOpaque(this.getAttributes(), true);

		GraphConstants.setBorder(this.getAttributes(), BorderFactory.createRaisedBevelBorder());

		DefaultPort port = new DefaultPort();
		
		this.add(port);
		
		port.setParent(this);

	}

	public void addEdge(DefaultEdge edge)
	{
		_edges.add(edge);
	}

	public Object[] getEdges()
	{
		return _edges.toArray();
	}

	public String getName()
	{
		return _name;
	}

	public Gui getGui()
	{
		return _gui;
	}


	public Rectangle2D getBounds()
	{
		return GraphConstants.getBounds(this.getAttributes());
	}
	
	/**
	 * @return
	 */
	public Point getLocation()
	{
		Rectangle2D rect = GraphConstants.getBounds(this.getAttributes());

		Point point = new Point((int) rect.getX(), (int) rect.getY());

		return point;
	}

	/**
	 * @param location
	 */
	public void setLocation(Point location)
	{

		GraphConstants.setBounds(this.getAttributes(), new Rectangle2D.Double(
				location.x, location.y, 100, 25));

	}

}
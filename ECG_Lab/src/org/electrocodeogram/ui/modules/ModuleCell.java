package org.electrocodeogram.ui.modules;

import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import org.electrocodeogram.misc.constants.UIConstants;
import org.electrocodeogram.modulepackage.ModuleType;
import org.electrocodeogram.ui.Gui;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.DefaultPort;
import org.jgraph.graph.GraphConstants;

public class ModuleCell extends DefaultGraphCell
{

	/**
	 * 
	 */
	private static final int DEFAULT_HEIGHT = 25;

	/**
	 * 
	 */
	private static final int DEFAULT_WIDTH = 100;

	/**
	 * 
	 */
	private static final int DEFAULT_Y_POS = 0;

	/**
	 * 
	 */
	private static final int DEFAULT_X_POS = 0;

	/**
	 * 
	 */
	private static final int DEFAULT_INSETS = 10;

	private Gui _gui;

	private int _id = -1;

	private String _name;

	private boolean _run;
	
	private ArrayList<ModuleEdge> _edgeList;

	private static final String[] ACTIVITY_SYMBOLS = { "--", "\\", "|", "/" };

	private int _count = 0;

	public ModuleCell(ModuleType moduleType, int id, String name, boolean active)
	{
		super("");

		this._name = name;

		this._id = id;

		this._run = active;

		this._edgeList = new ArrayList<ModuleEdge>();

		initialiseGraphConstants(moduleType, name);

		DefaultPort port = new DefaultPort();

		this.add(port);

		port.setParent(this);

	}

	private void initialiseGraphConstants(ModuleType moduleType, String name)
	{
		GraphConstants.setValue(this.getAttributes(), name);

		GraphConstants.setInset(this.getAttributes(), DEFAULT_INSETS);

		GraphConstants.setBounds(this.getAttributes(), new Rectangle2D.Double(
				DEFAULT_X_POS, DEFAULT_Y_POS, DEFAULT_WIDTH, DEFAULT_HEIGHT));

		GraphConstants.setAutoSize(this.getAttributes(), true);

		GraphConstants.setMoveable(this.getAttributes(), true);

		GraphConstants.setEditable(this.getAttributes(), false);

		GraphConstants.setBackground(this.getAttributes(), UIConstants.MCL_BACKGROUND_COLOR);

		switch (moduleType)
		{
			case SOURCE_MODULE:

				GraphConstants.setBorder(this.getAttributes(), new TitledBorder(
						new LineBorder(UIConstants.MCL_BORDER_COLOR,
								UIConstants.MCL_BORDER_WIDTH, true), this._name));

				break;

			case INTERMEDIATE_MODULE:

				GraphConstants.setBorder(this.getAttributes(), new TitledBorder(
						new LineBorder(UIConstants.MCL_BORDER_COLOR,
								UIConstants.MCL_BORDER_WIDTH, true), this._name));

				break;

			case TARGET_MODULE:

				GraphConstants.setBorder(this.getAttributes(), new TitledBorder(
						new LineBorder(UIConstants.MCL_BORDER_COLOR,
								UIConstants.MCL_BORDER_WIDTH, true), this._name));

				break;

			default:

				break;
		}

		GraphConstants.setOpaque(this.getAttributes(), true);
	}

	public int getId()
	{
		return this._id;
	}

	public void addEdge(ModuleEdge edge)
	{
		this._edgeList.add(edge);
	}

	public ModuleEdge[] getEdges()
	{
		return this._edgeList.toArray(new ModuleEdge[this._edgeList.size()]);
	}

	public String getName()
	{
		return this._name;
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

	private static class ActiveBorder extends TitledBorder
	{
		private static final long serialVersionUID = 6142340161190117618L;

		public ActiveBorder()
		{
			super(new LineBorder(UIConstants.MCL_ACTIVE_BORDER_TITLE_COLOR,
					UIConstants.MCL_BORDER_WIDTH, true), "active");

			this.setTitleColor(UIConstants.MCL_ACTIVE_BORDER_TITLE_COLOR);
		}

		public ActiveBorder(String str)
		{
			super(new LineBorder(UIConstants.MCL_BORDER_COLOR,
					UIConstants.MCL_BORDER_WIDTH, true), "(" + str + ") active");

			this.setTitleColor(UIConstants.MCL_ACTIVE_BORDER_TITLE_COLOR);
		}
	}

	private static class InactiveBorder extends TitledBorder
	{
		private static final long serialVersionUID = -108805078475596177L;

		public InactiveBorder()
		{
			super(new LineBorder(UIConstants.MCL_ACTIVE_BORDER_TITLE_COLOR,
					UIConstants.MCL_BORDER_WIDTH, true), "inactive");

			this.setTitleColor(UIConstants.MCL_INACTIVE_BORDER_TITLE_COLOR);
		}
	}

	/**
	 * 
	 */
	public void activate()
	{
		GraphConstants.setBorder(this.getAttributes(), new ActiveBorder());

	}

	public void select()
	{
		GraphConstants.setOpaque(this.getAttributes(),false);
	}
	
	public void deselect()
	{
		GraphConstants.setOpaque(this.getAttributes(),true);
	}
	
	/**
	 * 
	 */
	public void deactivate()
	{
		GraphConstants.setBorder(this.getAttributes(), new InactiveBorder());
	}

	public void eventReceived()
	{
//		this._count++;
//
//		GraphConstants.setBorder(this.getAttributes(), new ActiveBorder(
//				ACTIVITY_SYMBOLS[this._count % 4]));

	}
}
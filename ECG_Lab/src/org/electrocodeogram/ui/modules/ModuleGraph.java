package org.electrocodeogram.ui.modules;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.HashMap;
import java.util.Observable;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.border.LineBorder;

import org.electrocodeogram.module.Module;
import org.electrocodeogram.module.ModuleConnectionException;
import org.electrocodeogram.module.Module.ModuleType;
import org.electrocodeogram.module.registry.ModuleInstanceException;
import org.electrocodeogram.system.ISystemRoot;
import org.electrocodeogram.system.SystemRoot;
import org.electrocodeogram.ui.Gui;
import org.electrocodeogram.ui.IGui;
import org.electrocodeogram.ui.MenuManager;
import org.electrocodeogram.ui.module.ModuleCell;
import org.jgraph.JGraph;
import org.jgraph.event.GraphSelectionEvent;
import org.jgraph.event.GraphSelectionListener;
import org.jgraph.graph.DefaultGraphModel;
import org.jgraph.graph.GraphConstants;

public class ModuleGraph extends JGraph
{
	private static final int DEFAULT_X_DISTANCE = 100;

	private static final int DEFAULT_MARGIN = 10;

	protected static ImageIcon _icon = null;

	Gui _gui;

	private HashMap _cellMap;

	static int _selected = -1;

	ModuleGraphObserverDummy _observerDummy = null;

	private Icon _startIcon;

	private Icon _stopIcon;

	private int _margin;

	private int _dinstanceX;

	public ModuleGraph(Gui configurator)
	{
		super(new DefaultGraphModel());

		this._margin = DEFAULT_MARGIN;

		this._dinstanceX = DEFAULT_X_DISTANCE;

		this._gui = configurator;

		this._observerDummy = new ModuleGraphObserverDummy(configurator, this);

		this._cellMap = new HashMap();

		this.setSizeable(true);

		this.setAutoscrolls(true);

		URL stop = ModuleCell.class.getClassLoader().getResource("org/electrocodeogram/ui/modules/Stop.GIF");

		URL start = ModuleCell.class.getClassLoader().getResource("org/electrocodeogram/ui/modules/Start.GIF");

		if (start != null)
		{
			this._startIcon = new ImageIcon(start);
		}

		if (stop != null)
		{
			this._stopIcon = new ImageIcon(stop);
		}

		addGraphSelectionListener(new GraphSelectionListener()
		{

			public void valueChanged(GraphSelectionEvent arg0)
			{
				if (arg0.isAddedCell() && (arg0.getCell() instanceof ModuleCell))
				{

					ModuleGraph.this._selected = ((ModuleCell) (arg0.getCell())).getId();

					ModuleGraph.this._gui.enableModuleMenu(true);
				}
				else
				{
					ModuleGraph._selected = -1;

					ModuleGraph.this._gui.enableModuleMenu(false);
				}

			}
		});

		this.setBorder(new LineBorder(Color.GRAY));

		addMouseListener(new MouseAdapter()
		{

			public void mouseClicked(MouseEvent e)
			{
				ISystemRoot systemRoot = SystemRoot.getSystemInstance();

				IGui gui = systemRoot.getGui();

				boolean mode = gui.getModuleConnectionMode();

				if (mode)
				{
					if (e.getButton() == MouseEvent.BUTTON1)
					{
						Object o = getFirstCellForLocation(e.getPoint().x, e.getPoint().y);
						if (o != null)
						{
							if (o instanceof ModuleCell)
							{
								ModuleCell mc = (ModuleCell) o;

								_selected = mc.getId();

								if (_selected == SystemRoot.getSystemInstance().getGui().getSourceModule())
								{
									JOptionPane.showMessageDialog(getGui(), "You can not connect a module to itself.", "Connect Module", JOptionPane.ERROR_MESSAGE);
								}
								else
								{
									try
									{

										SystemRoot.getSystemInstance().getSystemModuleRegistry().getRunningModule(SystemRoot.getSystemInstance().getGui().getSourceModule()).connectReceiverModule(SystemRoot.getSystemInstance().getSystemModuleRegistry().getRunningModule(_selected));

										SystemRoot.getSystemInstance().getGui().exitModuleConnectionMode();
									}
									catch (Exception e1)
									{

										JOptionPane.showMessageDialog(getGui(), e1.getMessage(), "Connect Module", JOptionPane.ERROR_MESSAGE);
									}
								}
							}

						}
					}
					else if (e.getButton() == MouseEvent.BUTTON3)
					{
						SystemRoot.getSystemInstance().getGui().exitModuleConnectionMode();
					}

				}
				else
				{
					if (e.getButton() == MouseEvent.BUTTON3)
					{
						Object o = getFirstCellForLocation(e.getPoint().x, e.getPoint().y);
						if (o != null)
						{
							if (o instanceof ModuleCell)
							{
								ModuleCell mc = (ModuleCell) o;

								_selected = mc.getId();

								MenuManager.showModuleMenu(_selected, ModuleGraph.this, e.getPoint().x, e.getPoint().y);

							}
							else if (o instanceof ModuleEdge)
							{
								ModuleEdge edge = (ModuleEdge) o;

								MenuManager.showEdgeMenu(edge.getParentId(), edge.getChildId(), ModuleGraph.this, e.getPoint().x, e.getPoint().y);
							}

						}
					}
				}
			}

		});
	}

	private Gui getGui()
	{
		return this._gui;
	}

	private void addModuleCell(ModuleCell cell)
	{
		this._cellMap.put(new Integer(cell.getId()), cell);

		this.getGraphLayoutCache().insert(cell);
	}

	public void updateModuleCell(int id, Module module)
	{
		if (containsModuleCell(id))
		{

			ModuleCell moduleCell = (ModuleCell) this._cellMap.get(new Integer(
					id));

			if (module.isActive())
			{
				if (this._startIcon != null)
				{
					GraphConstants.setIcon(moduleCell.getAttributes(), this._startIcon);
				}
			}
			else
			{
				if (this._stopIcon != null)
				{
					GraphConstants.setIcon(moduleCell.getAttributes(), this._stopIcon);
				}
			}

			this.getGraphLayoutCache().insert(moduleCell);

			Module[] modules = module.getReceivingModules();

			Object[] edges = moduleCell.getEdges();

			if (edges.length > 0)
			{
				getGraphLayoutCache().remove(edges);
			}

			if (modules != null)
			{
				for (Module receivingModule : modules)
				{
					ModuleCell childModuleCell = (ModuleCell) this._cellMap.get(new Integer(
							receivingModule.getId()));

					ModuleEdge edge = new ModuleEdge(moduleCell.getId(),
							childModuleCell.getId());

					edge.setSource(moduleCell.getChildAt(0));

					edge.setTarget(childModuleCell.getChildAt(0));

					addChildEdge(moduleCell, edge);

				}
			}
		}
	}

	public boolean containsModuleCell(int id)
	{
		boolean flag = _cellMap.containsKey(new Integer(id));

		return flag;
	}

	public void removeModuleCell(int id)
	{
		if (containsModuleCell(id))
		{
			ModuleCell cell = (ModuleCell) _cellMap.get(new Integer(id));

			Object[] o = new Object[] { cell };

			Object[] edges = cell.getEdges();

			if (edges.length > 0)
			{
				getGraphLayoutCache().remove(edges);
			}

			getGraphLayoutCache().remove(o);

			_cellMap.remove(new Integer(id));
		}
	}

	/**
	 * @param moduleCell
	 * @param edge
	 * @param childModuleCell
	 * @param parentModuleCell
	 */
	public void addChildEdge(ModuleCell moduleCell, ModuleEdge edge)
	{

		moduleCell.addEdge(edge);

		this.getGraphLayoutCache().insert(edge);

	}

	private static class ModuleGraphObserverDummy extends Observable
	{
		private ModuleGraph parent = null;

		public ModuleGraphObserverDummy(Gui configurator, ModuleGraph parent)
		{
			super();

			this.parent = parent;

			this.addObserver(configurator);

		}

		public void notifyUI(int moduleId)
		{
			setChanged();

			notifyObservers(moduleId);

			clearChanged();
		}
	}

	/**
	 * @param moduleType
	 * @param id
	 * @param name
	 * @param b
	 */
	public void createModuleCell(ModuleType moduleType, int id, String name, boolean b)
	{
		ModuleCell cell = new ModuleCell(moduleType, id, name, b);

		int x = this._margin;

		int y = this._margin;

		switch (moduleType)
		{
			case SOURCE_MODULE:

				x = this.getX() + this._margin;

				break;

			case INTERMEDIATE_MODULE:

				x = (this.getWidth() / 2) - (int) (cell.getBounds().getWidth() / 2);

				break;

			case TARGET_MODULE:

				x = this.getWidth() - (int) (cell.getBounds().getWidth()) - this._margin;

				break;

			default:

				break;

		}

		Object o = getFirstCellForLocation(x, y);

		while (o != null)
		{
			if (o instanceof ModuleCell)
			{
				y += this._dinstanceX;
			}
			else
			{
				break;
			}

			o = getFirstCellForLocation(x, y);
		}

		cell.setLocation(new Point(x, y));

		addModuleCell(cell);

	}

	public void setMargin(int margin)
	{
		this._margin = margin;
	}

	public static int getSelectedModule()
	{
		return _selected;
	}
}

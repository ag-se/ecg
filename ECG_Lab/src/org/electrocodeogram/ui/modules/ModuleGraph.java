/*
 * Class: ModuleGraph
 * Version: 1.0
 * Date: 16.10.2005
 * By: Frank@Schlesinger.com
 */

package org.electrocodeogram.ui.modules;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Observable;

import javax.swing.JOptionPane;
import javax.swing.border.LineBorder;
import org.electrocodeogram.modulepackage.ModuleType;
import org.electrocodeogram.misc.constants.UIConstants;
import org.electrocodeogram.module.Module;
import org.electrocodeogram.system.ISystem;
import org.electrocodeogram.ui.Gui;
import org.electrocodeogram.ui.IGui;
import org.electrocodeogram.ui.MenuManager;
import org.jgraph.JGraph;
import org.jgraph.event.GraphSelectionEvent;
import org.jgraph.event.GraphSelectionListener;
import org.jgraph.graph.DefaultGraphModel;

/**
 * This is the panel where the module instances are displayed as {@link org.electrocodeogram.ui.modules.ModuleCell}.
 *
 */
public class ModuleGraph extends JGraph {

    /**
     * The <em>Serialization</em> id.
     */
    private static final long serialVersionUID = 1357439970926255409L;


    /**
     * Contains all cells displayed in this panel.
     */
    private HashMap < Integer, ModuleCell > cellMap;

    /**
     * The id of the selected cell or -1 if no cell is selected.
     */
    private static int selected = -1;


    /**
     * The margin for the cells.
     */
    private int margin;

    /**
     * The default distance for new cells.
     */
    private int dinstanceX;

    /**
     * Creates the panel.
     */
    public ModuleGraph() {
        super(new DefaultGraphModel());

        this.margin = UIConstants.MODULE_GRAPH_DEFAULT_MARGIN;

        this.dinstanceX = UIConstants.MODULE_GRAPH_DEFAULT_X_DISTANCE;

        this.cellMap = new HashMap < Integer, ModuleCell >();

        this.setSizeable(true);

        this.setAutoscrolls(true);

        this.setBackground(UIConstants.MGR_BACKGROUND_COLOR);

        this.setBorder(new LineBorder(UIConstants.MGR_BORDER_COLOR,
            UIConstants.MGR_BORDER_WIDTH, true));

        addGraphSelectionListener(new GraphSelectionListener() {

            @SuppressWarnings("synthetic-access")
            public void valueChanged(final GraphSelectionEvent arg0) {
                if (arg0.isAddedCell()
                    && (arg0.getCell() instanceof ModuleCell)) {

                    ModuleGraph.selected = ((ModuleCell) (arg0.getCell()))
                        .getId();

                    org.electrocodeogram.system.System.getInstance().getGui().enableModuleMenu(true);
                } else {

                    ModuleGraph.selected = -1;

                    org.electrocodeogram.system.System.getInstance().getGui().enableModuleMenu(false);
                }

            }
        });

        addMouseListener(new MouseAdapter() {

            @SuppressWarnings("synthetic-access")
            @Override
            public void mouseClicked(final MouseEvent e) {

                boolean mode = org.electrocodeogram.system.System.getInstance().getGui().getModuleConnectionMode();

                if (mode) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        Object o = getFirstCellForLocation(e.getPoint().x, e
                            .getPoint().y);
                        if (o != null) {
                            if (o instanceof ModuleCell) {
                                ModuleCell mc = (ModuleCell) o;

                                selected = mc.getId();

                                if (selected == org.electrocodeogram.system.System
                                    .getInstance().getGui().getSourceModule()) {
                                    JOptionPane
                                        .showMessageDialog(org.electrocodeogram.system.System.getInstance().getMainWindow(),
                                            "You can not connect a module to itself.",
                                            "Connect Module",
                                            JOptionPane.ERROR_MESSAGE);
                                } else {
                                    try {

                                        org.electrocodeogram.system.System
                                            .getInstance()
                                            .getModuleRegistry()
                                            .getModule(
                                                org.electrocodeogram.system.System
                                                    .getInstance().getGui()
                                                    .getSourceModule())
                                            .connectModule(
                                                org.electrocodeogram.system.System
                                                    .getInstance()
                                                    .getModuleRegistry()
                                                    .getModule(selected));

                                        org.electrocodeogram.system.System
                                            .getInstance().getGui()
                                            .exitModuleConnectionMode();
                                    } catch (Exception e1) {

                                        JOptionPane.showMessageDialog(org.electrocodeogram.system.System.getInstance().getMainWindow(),
                                            e1.getMessage(), "Connect Module",
                                            JOptionPane.ERROR_MESSAGE);
                                    }
                                }
                            }

                        }
                    } else if (e.getButton() == MouseEvent.BUTTON3) {
                        org.electrocodeogram.system.System.getInstance()
                            .getGui().exitModuleConnectionMode();
                    }

                } else {
                    if (e.getButton() == MouseEvent.BUTTON3) {
                        Object o = getFirstCellForLocation(e.getPoint().x, e
                            .getPoint().y);
                        if (o != null) {
                            if (o instanceof ModuleCell) {
                                ModuleCell mc = (ModuleCell) o;

                                selected = mc.getId();

                                MenuManager.showModuleMenu(selected,
                                    ModuleGraph.this, e.getPoint().x, e
                                        .getPoint().y);

                            } else if (o instanceof ModuleEdge) {
                                ModuleEdge edge = (ModuleEdge) o;

                                MenuManager.showEdgeMenu(edge.getParentId(),
                                    edge.getChildId(), ModuleGraph.this, e
                                        .getPoint().x, e.getPoint().y);
                            }

                        }
                    }
                }
            }

        });
    }

    /**
     * Adds a new <code>ModuleCell</code> to this panel.
     * @param cell The new cell
     */
    private void addModuleCell(final ModuleCell cell) {
        this.cellMap.put(new Integer(cell.getId()), cell);

        this.getGraphLayoutCache().insert(cell);
    }

    /**
     * When a module's state changes this method changes the corresponding <code>ModuleCell</code>.
     * @param id The is of the changed module and the module cell
     * @param module The changed module
     */
    public final void updateModuleCell(final int id, final Module module) {
        if (containsModuleCell(id)) {

            ModuleCell moduleCell = this.cellMap.get(new Integer(id));

            if (module.isActive()) {
                moduleCell.activate();
            } else {
                moduleCell.deactivate();
            }

            this.getGraphLayoutCache().insert(moduleCell);

            Module[] modules = module.getReceivingModules();

            Object[] edges = moduleCell.getEdges();

            if (edges.length > 0) {
                getGraphLayoutCache().remove(edges);
            }

            if (modules != null) {
                for (Module receivingModule : modules) {
                    ModuleCell childModuleCell = this.cellMap
                        .get(new Integer(receivingModule.getId()));

                    ModuleEdge edge = new ModuleEdge(moduleCell.getId(),
                        childModuleCell.getId());

                    edge.setSource(moduleCell.getChildAt(0));

                    edge.setTarget(childModuleCell.getChildAt(0));

                    addChildEdge(moduleCell, edge);

                }
            }
        }
    }

    /**
     * Checks if this panel contains a cell with the given id.
     * @param id The id to search for
     * @return <code>true</code> if a cell with the given id is displayed and <code>false</code> otherwise
     */
    public final boolean containsModuleCell(final int id) {
        boolean flag = this.cellMap.containsKey(new Integer(id));

        return flag;
    }

    /**
     * Removes a cell from this panel.
     * @param id The id of the cell to removef
     */
    public final void removeModuleCell(final int id) {
        if (containsModuleCell(id)) {
            ModuleCell cell =  this.cellMap.get(new Integer(id));

            Object[] o = new Object[] {cell};

            Object[] edges = cell.getEdges();

            if (edges.length > 0) {
                getGraphLayoutCache().remove(edges);
            }

            getGraphLayoutCache().remove(o);

            this.cellMap.remove(new Integer(id));
        }
    }

    /**
     * Adds a module connection to the panel.
     * @param moduleCell the cell, which is the source of the connection
     * @param edge The connection
      */
    public final void addChildEdge(final ModuleCell moduleCell, final ModuleEdge edge) {

        moduleCell.addEdge(edge);

        this.getGraphLayoutCache().insert(edge);

    }


    /**
     * Creates a new <code>ModuleCell</code> for a module with the given atttributes.
     * @param moduleType Is the type of the module
     * @param id Is the id of the module
     * @param name Is the name to be displayed for the module
     */
    public final void createModuleCell(final ModuleType moduleType, final int id, final String name) {
        ModuleCell cell = new ModuleCell(moduleType, id, name);

        int x = this.margin;

        int y = this.margin;

        switch (moduleType) {
            case SOURCE_MODULE:

                x = this.getX() + this.margin;

                break;

            case INTERMEDIATE_MODULE:

                x = (this.getWidth() / 2)
                    - (int) (cell.getBounds().getWidth() / 2);

                break;

            case TARGET_MODULE:

                x = this.getWidth() - (int) (cell.getBounds().getWidth())
                    - this.margin;

                break;

            default:

                break;

        }

        Object o = getFirstCellForLocation(x, y);

        while (o != null) {
            if (o instanceof ModuleCell) {
                y += this.dinstanceX;
            } else {
                break;
            }

            o = getFirstCellForLocation(x, y);
        }

        cell.setLocation(new Point(x, y));

        addModuleCell(cell);

    }

    /**
     * Returns the id of the currently selected cell.
     * @return The id of the currently selected cell or -1 if no module cell is selected
     */
    public static int getSelectedModule() {
        return selected;
    }

}

/*
 * Classname: ModuleCell
 * Version: 1.0
 * Date: 18.10.2005
 * By: Frank@Schlesinger.com
 */

package org.electrocodeogram.ui.modules;

import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import org.electrocodeogram.misc.constants.UIConstants;
import org.electrocodeogram.modulepackage.ModuleType;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.DefaultPort;
import org.jgraph.graph.GraphConstants;

/**
 * This is how a module instance is displayed in the ECG Lab.
 */
public class ModuleCell extends DefaultGraphCell {

    /**
     * The <em>Serialization</em> id.
     */
    private static final long serialVersionUID = 7119988053822917810L;

    /**
     * The id is always the same as the id of the module that is
     * displayed by this cell.
     */
    private int id = -1;

    /**
     * The name that is displayed.
     */
    private String name;

    /**
     * A list of all outgoing connections.
     */
    private ArrayList < ModuleEdge > edgeList;

    /**
     * Creates a new <code>ModuleCell</code>.
     * @param moduleType
     *            The type of the module
     * @param moduleId
     *            The id of the module, which is assigned to the new
     *            <code>ModuleCell</code> also
     * @param moduleName
     *            The name to display
     */
    public ModuleCell(final ModuleType moduleType, final int moduleId,
        final String moduleName) {
        super("");

        this.name = moduleName;

        this.id = moduleId;

        this.edgeList = new ArrayList < ModuleEdge >();

        initializeGraphConstants(moduleType, moduleName);

        DefaultPort port = new DefaultPort();

        this.add(port);

        port.setParent(this);

    }

    /**
     * Sets up the Look 'n Fell of the cell.
     * @param moduleType
     *            The type of the module displayed by this cell
     * @param moduleName
     *            The name to be displayed
     */
    private void initializeGraphConstants(final ModuleType moduleType,
        final String moduleName) {
        GraphConstants.setValue(this.getAttributes(), moduleName);

        GraphConstants.setInset(this.getAttributes(),
            UIConstants.MODULE_CELL_DEFAULT_INSETS);

        GraphConstants.setBounds(this.getAttributes(), new Rectangle2D.Double(
            UIConstants.MODULE_CELL_DEFAULT_X_POS,
            UIConstants.MODULE_CELL_DEFAULT_Y_POS,
            UIConstants.MODULE_CELL_DEFAULT_WIDTH,
            UIConstants.MODULE_CELL_DEFAULT_HEIGHT));

        GraphConstants.setAutoSize(this.getAttributes(), true);

        GraphConstants.setMoveable(this.getAttributes(), true);

        GraphConstants.setEditable(this.getAttributes(), false);

        GraphConstants.setBackground(this.getAttributes(),
            UIConstants.MCL_BACKGROUND_COLOR);

        switch (moduleType) {
            case SOURCE_MODULE:

                GraphConstants.setBorder(this.getAttributes(),
                    new TitledBorder(new LineBorder(
                        UIConstants.MCL_BORDER_COLOR,
                        UIConstants.MCL_BORDER_WIDTH, true), this.name));

                break;

            case INTERMEDIATE_MODULE:

                GraphConstants.setBorder(this.getAttributes(),
                    new TitledBorder(new LineBorder(
                        UIConstants.MCL_BORDER_COLOR,
                        UIConstants.MCL_BORDER_WIDTH, true), this.name));

                break;

            case TARGET_MODULE:

                GraphConstants.setBorder(this.getAttributes(),
                    new TitledBorder(new LineBorder(
                        UIConstants.MCL_BORDER_COLOR,
                        UIConstants.MCL_BORDER_WIDTH, true), this.name));

                break;

            default:

                break;
        }

        GraphConstants.setOpaque(this.getAttributes(), true);
    }

    /**
     * Returns the id of the cell.
     * @return The id of the cell
     */
    public final int getId() {
        return this.id;
    }

    /**
     * Adds the visualization of a module connection to this cell.
     * @param edge
     *            The edge is visualizing a module connection
     */
    public final void addEdge(final ModuleEdge edge) {
        this.edgeList.add(edge);
    }

    /**
     * Returns all edges as an array.
     * @return All edges
     */
    public final ModuleEdge[] getEdges() {
        return this.edgeList.toArray(new ModuleEdge[this.edgeList.size()]);
    }

    /**
     * Returns the name of the cell.
     * @return The name of the cell
     */
    public final String getName() {
        return this.name;
    }

    /**
     * Returns the boundaries of the cell.
     * @return The boundaries of the cell
     */
    public final Rectangle2D getBounds() {
        return GraphConstants.getBounds(this.getAttributes());
    }

    /**
     * Returns the location of the cell.
     * @return Is a point in the surrounding panel
     */
    public final Point getLocation() {
        Rectangle2D rect = GraphConstants.getBounds(this.getAttributes());

        Point point = new Point((int) rect.getX(), (int) rect.getY());

        return point;
    }

    /**
     * Sets the location to display the cell.
     * @param location
     *            Is a point in the surrounding panel
     */
    public final void setLocation(final Point location) {

        GraphConstants.setBounds(this.getAttributes(), new Rectangle2D.Double(
            location.x, location.y, UIConstants.MODULE_CELL_DEFAULT_WIDTH,
            UIConstants.MODULE_CELL_DEFAULT_HEIGHT));

    }

    /**
     * This border is shown when the module is active.
     */
    private static class ActiveBorder extends TitledBorder {

        /**
         * The <em>Serialization</em> id.
         */
        private static final long serialVersionUID = 6142340161190117618L;

        /**
         * Creates the border.
         */
        public ActiveBorder() {
            super(new LineBorder(UIConstants.MCL_ACTIVE_BORDER_TITLE_COLOR,
                UIConstants.MCL_BORDER_WIDTH, true), "active");

            this.setTitleColor(UIConstants.MCL_ACTIVE_BORDER_TITLE_COLOR);
        }

        /**
         * Creates the border and write the given string in it.
         * @param str
         *            Is a string to be displayed as a border title
         */
        public ActiveBorder(final String str) {
            super(new LineBorder(UIConstants.MCL_BORDER_COLOR,
                UIConstants.MCL_BORDER_WIDTH, true), "(" + str + ") active");

            this.setTitleColor(UIConstants.MCL_ACTIVE_BORDER_TITLE_COLOR);
        }
    }

    /**
     * This border is shown when the module is inactive.
     */
    private static class InactiveBorder extends TitledBorder {

        /**
         * The <em>Serialization</em> id.
         */
        private static final long serialVersionUID = -108805078475596177L;

        /**
         * Creates the border.
         */
        public InactiveBorder() {
            super(new LineBorder(UIConstants.MCL_ACTIVE_BORDER_TITLE_COLOR,
                UIConstants.MCL_BORDER_WIDTH, true), "inactive");

            this.setTitleColor(UIConstants.MCL_INACTIVE_BORDER_TITLE_COLOR);
        }
    }

    /**
     * Called when the module becomes active.
     */
    public final void activate() {
        GraphConstants.setBorder(this.getAttributes(), new ActiveBorder());

    }

    // public void select() {
    // GraphConstants.setOpaque(this.getAttributes(), false);
    // }
    //
    // public void deselect() {
    // GraphConstants.setOpaque(this.getAttributes(), true);
    // }

    /**
     * Called when the module becomes inactive.
     */
    public final void deactivate() {
        GraphConstants.setBorder(this.getAttributes(), new InactiveBorder());
    }

    // public void eventReceived() {
    // // this._count++;
    // //
    // // GraphConstants.setBorder(this.getAttributes(), new
    // ActiveBorder(
    //    //				ACTIVITY_SYMBOLS[this._count % 4]));
    //
    //    }
}

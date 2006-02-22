/*
 * Class: ModuleEdge
 * Version: 1.0
 * Date: 16.10.2005
 * By: Frank@Schlesinger.com
 */

package org.electrocodeogram.ui.modules;

import org.electrocodeogram.misc.constants.UIConstants;
import org.jgraph.graph.DefaultEdge;
import org.jgraph.graph.GraphConstants;

/**
 * Is the graphical representation of a module connection.
 *
 */
public class ModuleEdge extends DefaultEdge {

    /**
     * The <em>Serialization</em> id.
     */
    private static final long serialVersionUID = -1207882628829640323L;

    /**
     * From this module.
     */
    private int parentId = -1;

    /**
     * To that module.
     */
    private int childId = -1;

    /**
     * Creates the edge.
     * @param from From the module with this id
     * @param to To the module with this id
     */
    public ModuleEdge(final int from, final int to) {
        super();

        this.parentId = from;

        this.childId = to;

        GraphConstants.setLineEnd(this.getAttributes(),
            GraphConstants.ARROW_SIMPLE);

        GraphConstants.setLineWidth(this.getAttributes(),
            UIConstants.MED_LINE_WIDTH);

        GraphConstants.setLineColor(this.getAttributes(),
            UIConstants.MED_LINE_COLOR);

        GraphConstants.setDisconnectable(this.getAttributes(), false);

        GraphConstants.setBendable(this.getAttributes(), false);

    }

    /**
     * Returns the connection-target module.
     * @return The connection-target module
     */
    public final int getChildId() {
        return this.childId;
    }

    /**
     * Returns the connection-source module.
     * @return The connection-source module
     */

    public final int getParentId() {
        return this.parentId;
    }
}

/*
 * Class: MenuManager
 * Version: 1.0
 * Date: 16.10.2005
 * By: Frank@Schlesinger.com
 */

package org.electrocodeogram.ui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.WindowConstants;

import org.electrocodeogram.module.Module;
import org.electrocodeogram.module.UIModule;
import org.electrocodeogram.module.intermediate.IntermediateModule;
import org.electrocodeogram.module.intermediate.IntermediateModule.ProcessingMode;
import org.electrocodeogram.module.registry.ModuleInstanceNotFoundException;
import org.electrocodeogram.module.registry.ModulePackageNotFoundException;
import org.electrocodeogram.modulepackage.ModuleDescriptor;
import org.electrocodeogram.modulepackage.ModuleProperty;
import org.electrocodeogram.modulepackage.ModuleType;
import org.electrocodeogram.system.System;
import org.electrocodeogram.ui.modules.ModuleGraph;

/**
 * Creates the module menus dynamically.
 */
public final class MenuManager {

    /**
     * A reference to the ECG Lab's root frame.
     */
    private static JFrame frame;

    private static JPopupMenu modulePopupMenu = null;

    private static JPopupMenu moduleFinderMenu;

    private static JPopupMenu edgePopupMenu = null;

    private static JMenuItem mniModuleDetails = new JMenuItem("Details");

    private static JMenuItem mniModuleFinderDetails = new JMenuItem("Details");

    private static JMenuItem mniModuleRemove = new JMenuItem("Remove");

    private static JMenuItem mniEdgeRemove = new JMenuItem("Remove");

    private static JMenuItem mniModuleConnectTo = new JMenuItem("Connect to...");

    private static JMenuItem mniModuleStop = new JMenuItem("Stop");

    private static JMenuItem mniModuleStart = new JMenuItem("Start");

    private static JMenuItem mniMsgWindowShow = new JMenuItem("Event Window");

    private static JMenuItem mniMakeAnnotator = new JMenuItem("Annotator");

    private static JMenuItem mniMakeFilter = new JMenuItem("Filter");

    /**
     * Generates the entries for the module menu and for the module
     * with the given id.
     * @param menu
     *            A reference to the module menu.
     * @param id
     *            The id of the module, which custom entries must be
     *            added to the menu
     */
    public static void populateModuleMenu(final JMenu menu, final int id) {
        if (id == -1) {
            return;
        }

        menu.removeAll();

        menu.add(mniModuleStart);

        menu.add(mniModuleStop);

        menu.addSeparator();

        menu.add(mniModuleConnectTo);

        menu.add(mniModuleRemove);

        ModuleProperty[] moduleProperties = null;

        String moduleId = null;

        try {
            moduleId = org.electrocodeogram.system.System.getInstance()
                .getModuleRegistry().getModule(id).getModulePacketId();
        } catch (ModuleInstanceNotFoundException e) {
            JOptionPane.showMessageDialog(MenuManager.frame, e.getMessage(),
                "Menu Initialisation", JOptionPane.ERROR_MESSAGE);
        }

        ModuleDescriptor moduleDescriptor = null;
        try {
            moduleDescriptor = org.electrocodeogram.system.System.getInstance()
                .getModuleRegistry().getModuleDescriptor(moduleId);
        } catch (ModulePackageNotFoundException e) {
            JOptionPane.showMessageDialog(MenuManager.frame, e.getMessage(),
                "Menu Initialisation", JOptionPane.ERROR_MESSAGE);
        }

        moduleProperties = moduleDescriptor.getProperties();

        if (moduleProperties != null) {
            for (ModuleProperty moduleProperty : moduleProperties) {

                JMenuItem menuItem = new JMenuItem(moduleProperty.getName());

                menuItem.addActionListener(new PropertyActionAdapter(
                    moduleProperty));

                menu.add(menuItem);

            }

        }

        menu.add(mniModuleDetails);

    }

    static {
        frame = org.electrocodeogram.system.System.getInstance()
            .getMainWindow();

        mniModuleRemove.addActionListener(new ActionListener() {

            @SuppressWarnings("synthetic-access")
            public void actionPerformed(@SuppressWarnings("unused")
            final ActionEvent e) {

                try {
                    org.electrocodeogram.system.System.getInstance()
                        .getModuleRegistry().getModule(
                            ModuleGraph.getSelectedModule()).remove();
                } catch (ModuleInstanceNotFoundException e1) {

                    JOptionPane.showMessageDialog(frame, e1.getMessage(),
                        "Remove Module", JOptionPane.ERROR_MESSAGE);
                }

            }
        });

        mniModuleDetails.addActionListener(new ActionListener() {

            public void actionPerformed(@SuppressWarnings("unused")
            final ActionEvent e) {
                org.electrocodeogram.system.System.getInstance().getGui()
                    .showModuleDetails();
            }
        });

        mniModuleFinderDetails.addActionListener(new ActionListener() {

            public void actionPerformed(@SuppressWarnings("unused")
            final ActionEvent e) {
                org.electrocodeogram.system.System.getInstance().getGui()
                    .showModuleFinderDetails();
            }
        });

        mniModuleStop.addActionListener(new ActionListener() {

            @SuppressWarnings("synthetic-access")
            public void actionPerformed(@SuppressWarnings("unused")
            final ActionEvent e) {

                try {
                    org.electrocodeogram.system.System.getInstance()
                        .getModuleRegistry().getModule(
                            ModuleGraph.getSelectedModule()).deactivate();
                } catch (ModuleInstanceNotFoundException e1) {

                    JOptionPane.showMessageDialog(frame, e1.getMessage(),
                        "Stop Module", JOptionPane.ERROR_MESSAGE);
                }

            }
        });

        mniModuleStart.addActionListener(new ActionListener() {

            @SuppressWarnings("synthetic-access")
            public void actionPerformed(@SuppressWarnings("unused")
            final ActionEvent e) {

                try {
                    org.electrocodeogram.system.System.getInstance()
                        .getModuleRegistry().getModule(
                            ModuleGraph.getSelectedModule()).activate();
                } catch (Exception e1) {
                    JOptionPane.showMessageDialog(frame, e1.getMessage(),
                        "Start Module", JOptionPane.ERROR_MESSAGE);
                }

            }
        });

        mniMsgWindowShow.addActionListener(new ActionListener() {

            public void actionPerformed(@SuppressWarnings("unused")
            final ActionEvent e) {
                org.electrocodeogram.system.System.getInstance().getGui()
                    .showMessagesWindow();

            }
        });

        mniMakeAnnotator.addActionListener(new ActionListener() {

            @SuppressWarnings("synthetic-access")
            public void actionPerformed(@SuppressWarnings("unused")
            final ActionEvent e) {
                Module module = null;

                try {
                    module = org.electrocodeogram.system.System.getInstance()
                        .getModuleRegistry().getModule(
                            ModuleGraph.getSelectedModule());
                } catch (ModuleInstanceNotFoundException e1) {
                    JOptionPane.showMessageDialog(frame, e1.getMessage(),
                        "Annotator", JOptionPane.ERROR_MESSAGE);
                }

                if (module instanceof IntermediateModule) {
                    IntermediateModule eventProcessor = (IntermediateModule) module;

                    eventProcessor.setProcessingMode(ProcessingMode.ANNOTATOR);

                }

            }
        });

        mniModuleConnectTo.addActionListener(new ActionListener() {

            public void actionPerformed(@SuppressWarnings("unused")
            final ActionEvent e) {
                org.electrocodeogram.system.System.getInstance().getGui()
                    .enterModuleConnectionMode(ModuleGraph.getSelectedModule());

            }
        });

        mniMakeFilter.addActionListener(new ActionListener() {

            @SuppressWarnings("synthetic-access")
            public void actionPerformed(@SuppressWarnings("unused")
            final ActionEvent e) {
                Module module = null;

                try {
                    module = org.electrocodeogram.system.System.getInstance()
                        .getModuleRegistry().getModule(
                            ModuleGraph.getSelectedModule());
                } catch (ModuleInstanceNotFoundException e1) {
                    JOptionPane.showMessageDialog(frame, e1.getMessage(),
                        "Filter", JOptionPane.ERROR_MESSAGE);
                }

                if (module instanceof IntermediateModule) {
                    IntermediateModule eventProcessor = (IntermediateModule) module;

                    eventProcessor
                        .setProcessingMode(IntermediateModule.ProcessingMode.FILTER);

                }

            }
        });

    }

    /**
     * The constructor is hidden for this utility class.
     */
    private MenuManager() {
    // not implemented
    }

    /**
     * Brigs up the context menu for <em>ModulePackages</em> in the
     * ECG Lab.
     * @param c
     *            Is the component to be the parent od the context
     *            menu
     * @param x
     *            The x value to display the context menu
     * @param y
     *            The y value to display the context menu
     */
    public static void showModuleFinderMenu(final Component c, final int x,
        final int y) {
        moduleFinderMenu = new JPopupMenu();

        moduleFinderMenu.add(mniModuleFinderDetails);

        moduleFinderMenu.show(c, x, y);
    }

    /**
     * Brings up a context menu for an individual module in the ECG
     * Lab.
     * @param moduleId
     *            The id of the module
     * @param c
     *            The parent off the context menu
     * @param x
     *            The x value to display the context menu
     * @param y
     *            The y value to display the context menu
     */
    public static void showModuleMenu(final int moduleId, final Component c,
        final int x, final int y) {

        Module module = null;

        try {
            module = System.getInstance().getModuleRegistry().getModule(
                moduleId);
        } catch (ModuleInstanceNotFoundException e) {

            JOptionPane.showMessageDialog(MenuManager.frame, e.getMessage(), e
                .getClass().getSimpleName(), JOptionPane.ERROR_MESSAGE);

            return;

        }

        modulePopupMenu = new JPopupMenu();

        modulePopupMenu.add(mniModuleStart);

        modulePopupMenu.add(mniModuleStop);

        if (!module.isModuleType(ModuleType.TARGET_MODULE)) {
            modulePopupMenu.add(mniModuleConnectTo);
        }

        modulePopupMenu.add(mniModuleRemove);

        if (module.isModuleType(ModuleType.INTERMEDIATE_MODULE)) {

            modulePopupMenu.addSeparator();

            modulePopupMenu.add(mniMakeAnnotator);

            modulePopupMenu.add(mniMakeFilter);
        }

        ModuleProperty[] moduleProperties = null;

        moduleProperties = module.getRuntimeProperties();

        if (moduleProperties != null) {

            modulePopupMenu.addSeparator();

            for (ModuleProperty moduleProperty : moduleProperties) {

                JMenuItem menuItem = new JMenuItem("Set: "
                                                   + moduleProperty.getName());

                menuItem.addActionListener(new PropertyActionAdapter(
                    moduleProperty));

                modulePopupMenu.add(menuItem);

            }

        }
        if (module instanceof UIModule) {

            modulePopupMenu.addSeparator();

            UIModule uiModule = (UIModule) module;

            JMenuItem menuItem = new JMenuItem("Open: "
                                               + uiModule.getPanelName());

            menuItem.addActionListener(new UIModuleActionAdapter(uiModule));

            modulePopupMenu.add(menuItem);
        }

        modulePopupMenu.addSeparator();

        modulePopupMenu.add(mniMsgWindowShow);

        modulePopupMenu.add(mniModuleDetails);

        modulePopupMenu.show(c, x, y);
    }

    /**
     * Brings up the context menu of a module connection.
     * @param parentId
     *            The id of the module which is the source of the
     *            connection
     * @param childId
     *            The id of the module which is the target of the
     *            connection
     * @param c
     *            The parent off the context menu
     * @param x
     *            The x value to display the context menu
     * @param y
     *            The y value to display the context menu
     */
    public static void showEdgeMenu(final int parentId, final int childId,
        final Component c, final int x, final int y) {
        edgePopupMenu = new JPopupMenu();

        mniEdgeRemove
            .addActionListener(new EdgeRemoveAdapter(parentId, childId));

        edgePopupMenu.add(mniEdgeRemove);

        edgePopupMenu.show(c, x, y);

    }

    /**
     * Is reacting, when the user selects to remove a module
     * connnection.
     */
    private static class EdgeRemoveAdapter implements ActionListener {

        /**
         * The id of the module that is the source of the connection
         */
        private int parentId;

        /**
         * The id of the module that is the target of the connection
         */
        private int childId;

        /**
         * Creates the adapter.
         * @param parent
         *            The id of the module that is the source of the
         *            connection
         * @param child
         *            The id of the module that is the target of the
         *            connection
         */
        public EdgeRemoveAdapter(final int parent, final int child) {

            this.parentId = parent;

            this.childId = child;
        }

        /**
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        @SuppressWarnings("synthetic-access")
        public void actionPerformed(@SuppressWarnings("unused")
        final ActionEvent e) {

            try {
                org.electrocodeogram.system.System.getInstance()
                    .getModuleRegistry().getModule(this.parentId)
                    .disconnectModule(
                        org.electrocodeogram.system.System.getInstance()
                            .getModuleRegistry().getModule(this.childId));
            } catch (Exception e1) {
                JOptionPane.showMessageDialog(MenuManager.frame, e1
                    .getMessage(), "Remove Edge", JOptionPane.ERROR_MESSAGE);
            }

        }

    }

    /**
     * Reacts, when the user selects to change the value of a
     * <em>ModuleProerty</em> in the ECG Lab.
     */
    private static class PropertyActionAdapter implements ActionListener {

        /**
         * Takes the new value for the property.
         */
        private String propertyResult;

        /**
         * A reference to the property the user is changing.
         */
        private ModuleProperty myModuleProperty;

        /**
         * Creates the adapter.
         * @param moduleProperty
         *            A reference to the property the user is changing
         */
        public PropertyActionAdapter(final ModuleProperty moduleProperty) {

            this.myModuleProperty = moduleProperty;

        }

        /**
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        @SuppressWarnings( {"synthetic-access", "synthetic-access",
            "synthetic-access", "synthetic-access", "synthetic-access"})
        public void actionPerformed(@SuppressWarnings("unused")
        final ActionEvent e) {

            try {
                if (this.myModuleProperty.getType().equals(
                    Class.forName("java.lang.String"))) {
                    this.propertyResult = JOptionPane.showInputDialog(
                        MenuManager.frame, "Please enter a new value for "
                                           + this.myModuleProperty.getName(),
                        this.myModuleProperty.getName(),
                        JOptionPane.QUESTION_MESSAGE);
                } else if (this.myModuleProperty.getType().equals(
                    Class.forName("java.lang.Integer"))) {
                    this.propertyResult = JOptionPane.showInputDialog(
                        MenuManager.frame, "Please enter a new value for "
                                           + this.myModuleProperty.getName(),
                        this.myModuleProperty.getName(),
                        JOptionPane.QUESTION_MESSAGE);
                } else if (this.myModuleProperty.getType().equals(
                    Class.forName("java.io.File"))) {

                    JFileChooser fileChooser = new JFileChooser();

                    int result = fileChooser.showOpenDialog(MenuManager.frame);

                    switch (result) {
                        case JFileChooser.CANCEL_OPTION:
                            return;
                        case JFileChooser.ERROR_OPTION:
                            return;
                        case JFileChooser.APPROVE_OPTION:
                            this.propertyResult = fileChooser.getSelectedFile()
                                .getAbsolutePath();
                            break;
                        default:
                            return;
                    }
                } else if (this.myModuleProperty.getType().equals(
                    Class.forName("java.lang.reflect.Method"))) {
                    this.propertyResult = this.myModuleProperty.getValue();
                } else if (this.myModuleProperty.getType().equals(
                    Class.forName("java.lang.Boolean"))) {
                    int result = JOptionPane.showConfirmDialog(
                        MenuManager.frame, "Do you want to enable the "
                                           + this.myModuleProperty.getName()
                                           + " property?",
                        this.myModuleProperty.getName(),
                        JOptionPane.YES_NO_OPTION);

                    if (result == JOptionPane.YES_OPTION) {
                        this.propertyResult = "true";
                    } else {
                        this.propertyResult = "false";
                    }
                }
            } catch (Exception e2) {
                JOptionPane.showMessageDialog(MenuManager.frame, e2
                    .getMessage(), this.myModuleProperty.getName(),
                    JOptionPane.ERROR_MESSAGE);
            }

            if (this.propertyResult == null) {
                return;
            }

            this.myModuleProperty.setValue(this.propertyResult);

        }

    }

    /**
     * Reacts, when the user selects to bring up a module's panel.
     */
    private static class UIModuleActionAdapter implements ActionListener {

        /**
         * The module itself.
         */
        private UIModule uiModule;

        /**
         * Creates the adapter.
         * @param module
         *            The module itself
         */
        public UIModuleActionAdapter(final UIModule module) {

            this.uiModule = module;
        }

        /**
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(@SuppressWarnings("unused")
        final ActionEvent e) {

            JPanel panel = this.uiModule.getPanel();

            String title = this.uiModule.getPanelName();

            if (panel == null || title == null) {

                return;
            }

            JFrame frm = new JFrame(title);

            frm.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

            frm.add(panel);

            frm.pack();

            frm.setVisible(true);

        }

    }

}

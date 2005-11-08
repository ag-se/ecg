package org.electrocodeogram.ui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.WindowConstants;

import org.electrocodeogram.module.Module;
import org.electrocodeogram.module.UIModule;
import org.electrocodeogram.modulepackage.ModuleType;
import org.electrocodeogram.module.intermediate.IntermediateModule;
import org.electrocodeogram.module.intermediate.IntermediateModule.ProcessingMode;
import org.electrocodeogram.module.registry.ModuleInstanceNotFoundException;
import org.electrocodeogram.module.registry.ModulePackageNotFoundException;
import org.electrocodeogram.modulepackage.ModuleDescriptor;
import org.electrocodeogram.modulepackage.ModuleProperty;
import org.electrocodeogram.system.System;
import org.electrocodeogram.ui.modules.ModuleGraph;

/**
 * @author 7oas7er TODO To change the template for this generated type
 *         comment go to Window - Preferences - Java - Code Style -
 *         Code Templates
 */
public class MenuManager {

    static JFrame _frame;

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

    public static void populateModuleMenu(JMenu menu, int id) {
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
            JOptionPane.showMessageDialog(MenuManager._frame, e.getMessage(),
                "Menu Initialisation", JOptionPane.ERROR_MESSAGE);
        }

        ModuleDescriptor moduleDescriptor = null;
        try {
            moduleDescriptor = org.electrocodeogram.system.System.getInstance()
                .getModuleRegistry().getModuleDescriptor(moduleId);
        } catch (ModulePackageNotFoundException e) {
            JOptionPane.showMessageDialog(MenuManager._frame, e.getMessage(),
                "Menu Initialisation", JOptionPane.ERROR_MESSAGE);
        }

        moduleProperties = moduleDescriptor.getProperties();

        if (moduleProperties != null) {
            for (ModuleProperty moduleProperty : moduleProperties) {
                //                String propertyName = moduleProperty.getName();
                //
                //                Class propertyType = moduleProperty.getType();
                //
                //                String propertyValue = moduleProperty.getValue();

                JMenuItem menuItem = new JMenuItem(moduleProperty.getName());

                menuItem.addActionListener(new PropertyActionAdapter(
                    moduleProperty));

                menu.add(menuItem);

            }

        }

        menu.add(mniModuleDetails);

    }

    static {
        _frame = org.electrocodeogram.system.System.getInstance()
            .getMainWindow();

        mniModuleRemove.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                try {
                    org.electrocodeogram.system.System.getInstance()
                        .getModuleRegistry().getModule(
                            ModuleGraph.getSelectedModule()).remove();
                } catch (ModuleInstanceNotFoundException e1) {

                    JOptionPane.showMessageDialog(_frame, e1.getMessage(),
                        "Remove Module", JOptionPane.ERROR_MESSAGE);
                }

            }
        });

        mniModuleDetails.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                org.electrocodeogram.system.System.getInstance().getGui()
                    .showModuleDetails();
            }
        });

        mniModuleFinderDetails.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                org.electrocodeogram.system.System.getInstance().getGui()
                    .showModuleFinderDetails();
            }
        });

        mniModuleStop.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                try {
                    org.electrocodeogram.system.System.getInstance()
                        .getModuleRegistry().getModule(
                            ModuleGraph.getSelectedModule()).deactivate();
                } catch (ModuleInstanceNotFoundException e1) {

                    JOptionPane.showMessageDialog(_frame, e1.getMessage(),
                        "Stop Module", JOptionPane.ERROR_MESSAGE);
                }

            }
        });

        mniModuleStart.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                try {
                    org.electrocodeogram.system.System.getInstance()
                        .getModuleRegistry().getModule(
                            ModuleGraph.getSelectedModule()).activate();
                } catch (Exception e1) {
                    JOptionPane.showMessageDialog(_frame, e1.getMessage(),
                        "Start Module", JOptionPane.ERROR_MESSAGE);
                }

            }
        });

        mniMsgWindowShow.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                org.electrocodeogram.system.System.getInstance().getGui()
                    .showMessagesWindow();

            }
        });

        mniMakeAnnotator.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                Module module = null;

                try {
                    module = org.electrocodeogram.system.System.getInstance()
                        .getModuleRegistry().getModule(
                            ModuleGraph.getSelectedModule());
                } catch (ModuleInstanceNotFoundException e1) {
                    JOptionPane.showMessageDialog(_frame, e1.getMessage(),
                        "Annotator", JOptionPane.ERROR_MESSAGE);
                }

                if (module instanceof IntermediateModule) {
                    IntermediateModule eventProcessor = (IntermediateModule) module;

                    eventProcessor.setProcessingMode(ProcessingMode.ANNOTATOR);

                }

            }
        });

        mniModuleConnectTo.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                org.electrocodeogram.system.System.getInstance().getGui()
                    .enterModuleConnectionMode(ModuleGraph.getSelectedModule());

            }
        });

        mniMakeFilter.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                Module module = null;

                try {
                    module = org.electrocodeogram.system.System.getInstance()
                        .getModuleRegistry().getModule(
                            ModuleGraph.getSelectedModule());
                } catch (ModuleInstanceNotFoundException e1) {
                    JOptionPane.showMessageDialog(_frame, e1.getMessage(),
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

    public static void showModuleFinderMenu(String id, Component c, int x, int y) {
        moduleFinderMenu = new JPopupMenu();

        moduleFinderMenu.add(mniModuleFinderDetails);

        moduleFinderMenu.show(c, x, y);
    }

    public static void showModuleMenu(int moduleId, Component c, int x, int y) {

        Module module = null;

        try {
            module = System.getInstance().getModuleRegistry().getModule(
                moduleId);
        } catch (ModuleInstanceNotFoundException e) {

            JOptionPane.showMessageDialog(MenuManager._frame, e.getMessage(), e
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
                // String propertyName = moduleProperty.getName();
                //
                // Class propertyType = moduleProperty.getType();
                //
                // String propertyValue = moduleProperty.getValue();

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

    public static void showEdgeMenu(int parentId, int childId, Component c,
        int x, int y) {
        edgePopupMenu = new JPopupMenu();

        mniEdgeRemove
            .addActionListener(new EdgeRemoveAdapter(parentId, childId));

        edgePopupMenu.add(mniEdgeRemove);

        edgePopupMenu.show(c, x, y);

    }

    private static class EdgeRemoveAdapter implements ActionListener {

        private int _parentId;

        private int _childId;

        public EdgeRemoveAdapter(int parentId, int childId) {

            this._parentId = parentId;

            this._childId = childId;
        }

        public void actionPerformed(ActionEvent e) {

            try {
                org.electrocodeogram.system.System.getInstance()
                    .getModuleRegistry().getModule(this._parentId)
                    .disconnectReceiverModule(
                        org.electrocodeogram.system.System.getInstance()
                            .getModuleRegistry().getModule(this._childId));
            } catch (Exception e1) {
                JOptionPane.showMessageDialog(MenuManager._frame, e1
                    .getMessage(), "Remove Edge", JOptionPane.ERROR_MESSAGE);
            }

        }

    }

    private static class PropertyActionAdapter implements ActionListener {

        // private int _moduleId;
        //
        // private String _propertyName;
        //
        private String propertyResult;

        //
        // private String _propertyValue;
        //
        // private Class _propertyType;

        private ModuleProperty myModuleProperty;

        public PropertyActionAdapter(ModuleProperty moduleProperty) {

            this.myModuleProperty = moduleProperty;

        }

        public void actionPerformed(ActionEvent e) {

            try {
                if (this.myModuleProperty.getType().equals(
                    Class.forName("java.lang.String"))) {
                    this.propertyResult = JOptionPane.showInputDialog(
                        MenuManager._frame, "Please enter a new value for "
                                            + this.myModuleProperty.getName(),
                        this.myModuleProperty.getName(),
                        JOptionPane.QUESTION_MESSAGE);
                } else if (this.myModuleProperty.getType().equals(
                    Class.forName("java.lang.Integer"))) {
                    this.propertyResult = JOptionPane.showInputDialog(
                        MenuManager._frame, "Please enter a new value for "
                                            + this.myModuleProperty.getName(),
                        this.myModuleProperty.getName(),
                        JOptionPane.QUESTION_MESSAGE);
                } else if (this.myModuleProperty.getType().equals(
                    Class.forName("java.io.File"))) {

                    JFileChooser fileChooser = new JFileChooser();

                    int result = fileChooser.showOpenDialog(MenuManager._frame);

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
                        MenuManager._frame, "Do you want to enable the "
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
                JOptionPane.showMessageDialog(MenuManager._frame, e2
                    .getMessage(), this.myModuleProperty.getName(),
                    JOptionPane.ERROR_MESSAGE);
            }

            if (this.propertyResult == null) {
                return;
            }

            this.myModuleProperty.setValue(this.propertyResult);

        }

    }

    private static class UIModuleActionAdapter implements ActionListener {

        private UIModule uiModule;

        public UIModuleActionAdapter(UIModule module) {

            this.uiModule = module;
        }

        /**
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e) {

            JPanel panel = this.uiModule.getPanel();

            String title = this.uiModule.getPanelName();

            if (panel == null || title == null) {

                return;
            }

            JFrame frame = new JFrame(title);

            frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

            frame.add(panel);

            frame.pack();

            frame.setVisible(true);

        }

    }

}

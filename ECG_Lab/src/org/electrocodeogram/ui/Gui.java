/*
 * Class: Gui
 * Version: 1.0
 * Date: 16.10.2005
 * By: Frank@Schlesinger.com
 */

package org.electrocodeogram.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.HashMap;
import java.util.Observable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import org.electrocodeogram.event.ValidEventPacket;
import org.electrocodeogram.logging.LogHelper;
import org.electrocodeogram.misc.constants.UIConstants;
import org.electrocodeogram.module.Module;
import org.electrocodeogram.module.event.MessageEvent;
import org.electrocodeogram.module.registry.ModuleInstanceNotFoundException;
import org.electrocodeogram.module.registry.ModuleInstantiationException;
import org.electrocodeogram.module.registry.ModulePackageNotFoundException;
import org.electrocodeogram.module.registry.ModuleRegistry;
import org.electrocodeogram.module.registry.ModuleSetupLoadException;
import org.electrocodeogram.module.registry.ModuleSetupStoreException;
import org.electrocodeogram.modulepackage.ModuleDescriptor;
import org.electrocodeogram.modulepackage.ModuleType;
import org.electrocodeogram.ui.event.EventWindow;
import org.electrocodeogram.ui.modules.ModuleGraph;

/**
 * This is the graphical user interface of the ECG Lab.
 */
public class Gui extends JFrame implements IGui {

    /**
     * This is the <em>Serialization</em> id.
     */
    private static final long serialVersionUID = -2251260209448377911L;

    /**
     * This is the logger.
     */
    private static Logger logger = LogHelper.createLogger(Gui.class.getName());

    /**
     * A reference to the windows, where the events passing a module a
     * shown.
     */
    private HashMap < Integer, EventWindow > eventWindoMap;

    /**
     * A reference to panel, where the modules can be configured.
     */
    private ModuleLabPanel pnlModules;

    /**
     * A reference to the statusbar.
     */

    /**
     * A reference to the panel, where the <em>ModulePackages</em>
     * are listed.
     */
    private ModulePackagePanel pnlModulePackages;

    /**
     * This is the splitpane.
     */
    private JSplitPane splitPane;

    /**
     * The module menu.
     */
    private JMenu menuModule;

    /**
     * When the user has initiated to enter a new module connection,
     * the GUI will go into the <em>ModuleConnectionMode</em>,
     * until the connection has been made or the action is aborted by
     * the user.
     */
    private boolean moduleConnectionMode;

    /**
     * The id of the module that is the source of a conection.
     */
    private int sourceModuleId;

    /**
     * Creates the GUI and sets up its components.
     */
    public Gui() {
        super();

        initializeLookAndFeel();

        initializeFrame();

        initializeMenu();

        this.pnlModules = new ModuleLabPanel(this);

        this.pnlModulePackages = new ModulePackagePanel();

        initializeSplitPane();

        //initializeStatusBar();

        this.eventWindoMap = new HashMap < Integer, EventWindow >();

        setVisible(true);
    }

    /**
     * Sets up the split pane.
     */
    private void initializeSplitPane() {
        this.splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

        this.splitPane.add(this.pnlModulePackages, 0);

        this.splitPane.add(this.pnlModules, 1);

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 0;
        c.weighty = 2;
        c.weightx = 2;

        getContentPane().add(this.splitPane, c);
    }

    /**
     * Sets up the menu.
     */
    private void initializeMenu() {
        JMenuBar menuBar = new JMenuBar();

        JMenu menuFile = new JMenu("File");

        JMenuItem mniExit = new JMenuItem("Exit");

        mniExit.addActionListener(new ActionListener() {

            public void actionPerformed(@SuppressWarnings("unused")
            final ActionEvent e) {
                org.electrocodeogram.system.System.getInstance().quit();

            }
        });

        JMenuItem mniSave = new JMenuItem("Save module setup");

        mniSave.addActionListener(new ActionListener() {

            public void actionPerformed(@SuppressWarnings("unused")
            final ActionEvent e) {
                try {
                    JFileChooser fileChooser = new JFileChooser();

                    fileChooser
                        .setDialogTitle("Select the file to store the module setup in");

                    fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);

                    File file = null;

                    int result = fileChooser.showOpenDialog(Gui.this);

                    switch (result) {
                        case JFileChooser.CANCEL_OPTION:
                            return;
                        case JFileChooser.ERROR_OPTION:
                            return;
                        case JFileChooser.APPROVE_OPTION:

                            file = new File(fileChooser.getSelectedFile()
                                .getAbsolutePath());

                            break;
                        default:

                            return;
                    }

                    org.electrocodeogram.system.System.getInstance()
                        .getModuleRegistry().storeModuleSetup(file);
                } catch (ModuleSetupStoreException e1) {
                    JOptionPane
                        .showMessageDialog(Gui.this, e1.getMessage(),
                            "Module setup storage error",
                            JOptionPane.ERROR_MESSAGE);
                }

            }
        });

        JMenuItem mniLoad = new JMenuItem("Load module setup");
        mniLoad.addActionListener(new ActionListener() {

            public void actionPerformed(@SuppressWarnings("unused")
            final ActionEvent e) {
                try {
                    JFileChooser fileChooser = new JFileChooser();

                    fileChooser
                        .setDialogTitle("Select the file to load the module setup from");

                    fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);

                    File file = null;

                    int result = fileChooser.showOpenDialog(Gui.this);

                    switch (result) {
                        case JFileChooser.CANCEL_OPTION:
                            return;
                        case JFileChooser.ERROR_OPTION:
                            return;
                        case JFileChooser.APPROVE_OPTION:

                            file = new File(fileChooser.getSelectedFile()
                                .getAbsolutePath());

                            break;

                        default:
                            return;

                    }

                    org.electrocodeogram.system.System.getInstance()
                        .getModuleRegistry().loadModuleSetup(file);
                } catch (ModuleSetupLoadException e1) {
                    JOptionPane
                        .showMessageDialog(Gui.this, e1.getMessage(),
                            "Module setup loading error",
                            JOptionPane.ERROR_MESSAGE);
                }

            }
        });

        menuFile.add(mniSave);
        menuFile.add(mniLoad);
        menuFile.addSeparator();
        menuFile.add(mniExit);

        Gui.this.menuModule = new JMenu("Module");

        Gui.this.menuModule.setEnabled(false);

        Gui.this.menuModule.addMouseListener(new MouseAdapter() {

            @SuppressWarnings("synthetic-access")
            @Override
            public void mouseEntered(@SuppressWarnings("unused")
            final MouseEvent e) {
                MenuManager.populateModuleMenu(Gui.this.menuModule,
                    Gui.this.pnlModules.getSelectedModuleCell());
            }
        });

        JMenu menuWindow = new JMenu("Window");
        JMenuItem mniShow = new JMenuItem("Event Window");

        mniShow.addActionListener(new ActionListener() {

            public void actionPerformed(@SuppressWarnings("unused")
            final ActionEvent e) {

                showMessagesWindow();

            }
        });
        menuWindow.add(mniShow);

        menuBar.add(menuFile);
        menuBar.add(Gui.this.menuModule);
        menuBar.add(menuWindow);

        this.setJMenuBar(menuBar);
    }

    /**
     * Sets up the main window.
     */
    private void initializeFrame() {
        setTitle("ElectroCodeoGram - ECG Lab");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //setExtendedState(Frame.MAXIMIZED_BOTH);

        setBounds(0, 0, UIConstants.DEFAULT_WINDOW_HEIGHT,
            UIConstants.DEFAULT_WINDOW_WIDTH);

        getContentPane().setLayout(new GridBagLayout());
    }

    /**
     * Sets up the Look'n'Feel.
     */
    private void initializeLookAndFeel() {
        try {

            UIManager
                .setLookAndFeel("org.fife.plaf.Office2003.Office2003LookAndFeel");
        } catch (UnsupportedLookAndFeelException e) {
            try {
                UIManager.setLookAndFeel(UIManager
                    .getSystemLookAndFeelClassName());
            } catch (Exception e1) {
                logger.log(Level.WARNING, "Can not set the LookAndFeel");

                logger.log(Level.FINEST, e.getMessage());
            }

        } catch (Exception e) {
            logger.log(Level.WARNING, "Can not set the LookAndFeel");

            logger.log(Level.FINEST, e.getMessage());
        }
    }

    /**
     * @see org.electrocodeogram.ui.IGui#showMessagesWindow()
     */
    public final void showMessagesWindow() {

        EventWindow window = null;

        int id = Gui.this.pnlModules.getSelectedModuleCell();

        if (this.eventWindoMap.containsKey(new Integer(id))) {
            window = this.eventWindoMap.get(new Integer(id));

        } else {
            try {
                window = new EventWindow(id);
            } catch (ModuleInstanceNotFoundException e) {
                JOptionPane.showMessageDialog(this,e.getClass().getName(),e.getMessage(),JOptionPane.ERROR_MESSAGE);

                return;
            }

            this.eventWindoMap.put(new Integer(id), window);

        }

        window.setVisible(true);

    }

    /**
     * @see java.util.Observer#update(java.util.Observable,
     *      java.lang.Object)
     */
    public final void update(final Observable o, final Object arg) {

        /*
         * if the ModuleRegistry is sending the event, a
         * module-instance has been added or removed or a module class
         * has been installed
         */
        if (o instanceof ModuleRegistry) {

            // a module has been added or removed
            if (arg instanceof Module) {

                Module module = (Module) arg;

                if (this.pnlModules.containsModuleCell(module.getId())) {

                    EventWindow window = this.eventWindoMap.get(new Integer(
                        module.getId()));

                    if (window != null) {
                        window.dispose();

                        this.eventWindoMap.remove(new Integer(module.getId()));
                    }

                    this.pnlModules.removeModuleCell(module.getId());

                } else {

                    this.pnlModules.createModuleCell(module.getModuleType(),
                        module.getId(), module.getName());

                }
            } else if (arg instanceof ModuleDescriptor) {

                ModuleDescriptor moduleDescriptor = (ModuleDescriptor) arg;

                this.pnlModulePackages.addModulePackage(moduleDescriptor);

                this.splitPane.resetToPreferredSizes();

            }
        } else if (arg instanceof ValidEventPacket) {

            ValidEventPacket event = (ValidEventPacket) arg;

            if (this.eventWindoMap
                .containsKey(new Integer(event.getSourceId()))) {
                EventWindow window = this.eventWindoMap.get(new Integer(event
                    .getSourceId()));

                window.appendEvent(event);
            }

        } else if (arg instanceof Module) {
            Module module = (Module) arg;

            int id = module.getId();

            if (this.pnlModules.containsModuleCell(id)) {
                this.pnlModules.updateModuleCell(id, module);
            }
        } else if (arg instanceof MessageEvent) {
            MessageEvent event = (MessageEvent) arg;

            int optionPanetype;

            MessageEvent.MessageType type = event.getMessageType();

            switch (type) {
                case ERROR:

                    optionPanetype = JOptionPane.ERROR_MESSAGE;

                    break;

                case INFO:

                    optionPanetype = JOptionPane.INFORMATION_MESSAGE;

                    break;

                case WARNING:

                    optionPanetype = JOptionPane.WARNING_MESSAGE;

                    break;

                case QUESTION:

                    optionPanetype = JOptionPane.QUESTION_MESSAGE;

                    break;

                default:

                    optionPanetype = JOptionPane.INFORMATION_MESSAGE;

                    break;

            }

            JOptionPane.showMessageDialog(this, event.getMessage(),
                "Message from " + event.getModuleName() + ":"
                                + event.getModuleId(), optionPanetype);

        }

    }

    
    /**
     * @see org.electrocodeogram.ui.IGui#enableModuleMenu(boolean)
     */
    public final void enableModuleMenu(final boolean enable) {
        this.menuModule.setEnabled(enable);
    }

    /**
     * @see org.electrocodeogram.ui.IGui#showModuleDetails()
     */
    public final void showModuleDetails() {

        int id = this.pnlModules.getSelectedModuleCell();

        if (id != -1) {

            String text = "";

            try {
                text = org.electrocodeogram.system.System.getInstance()
                    .getModuleRegistry().getModule(id).getDetails();
            } catch (ModuleInstanceNotFoundException e) {
                JOptionPane.showMessageDialog(this, e.getMessage(),
                    "Module Details", JOptionPane.ERROR_MESSAGE);
            }

            JOptionPane.showMessageDialog(this, text, "Module Details",
                JOptionPane.INFORMATION_MESSAGE);
        }

    }

    /**
     * @see org.electrocodeogram.ui.IGui#showModuleFinderDetails()
     */
    @SuppressWarnings("synthetic-access")
    public final void showModuleFinderDetails() {

        String id = ModulePackagePanel.selectedModuleButton;

        if (id.equals("")) {
            return;
        }

        ModuleDescriptor moduleDescriptor;

        try {
            moduleDescriptor = org.electrocodeogram.system.System.getInstance()
                .getModuleRegistry().getModuleDescriptor(id);

            JOptionPane.showMessageDialog(this, moduleDescriptor
                .getDescription(), "Module Description",
                JOptionPane.INFORMATION_MESSAGE);

        } catch (ModulePackageNotFoundException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(),
                "Module Description", JOptionPane.ERROR_MESSAGE);
        }

    }

    /**
     * @see org.electrocodeogram.ui.IGui#enterModuleConnectionMode(int)
     */
    public final void enterModuleConnectionMode(final int id) {

        this.moduleConnectionMode = true;

        this.sourceModuleId = id;

    }

    /**
     * @see org.electrocodeogram.ui.IGui#getModuleConnectionMode()
     */
    public final boolean getModuleConnectionMode() {

        return this.moduleConnectionMode;
    }

    /**
     * @see org.electrocodeogram.ui.IGui#getSourceModule()
     */
    public final int getSourceModule() {

        return this.sourceModuleId;
    }

    /**
     * @see org.electrocodeogram.ui.IGui#exitModuleConnectionMode()
     */
    public final void exitModuleConnectionMode() {
        this.moduleConnectionMode = false;

        this.sourceModuleId = -1;

    }

    /**
     * This panel is used to connect module instances and to configure
     * them.
     */
    private static class ModuleLabPanel extends JPanel {

        /**
         * Is the <em>Serialization</em> id.
         */
        private static final long serialVersionUID = 987897133772256981L;

        /**
         * A reference to the GUI.
         */
        private Gui rootFrame;

        /**
         * Modules are displayed in a <em>JGraph</em>, which is
         * referenced here.
         */
        private ModuleGraph moduleGraph;

        /**
         * This is a scrollpane.
         */
        private JScrollPane scrollPane;

        /**
         * Creates the panel.
         * @param gui
         *            A reference to the GUI
         */
        public ModuleLabPanel(final Gui gui) {
            this.rootFrame = gui;

            this.moduleGraph = new ModuleGraph();

            this.setLayout(new GridLayout(1, 1));

            this.setBackground(UIConstants.PNL_MODULE_LAB_BORDER_COLOR);

            this.setBorder(new TitledBorder(null, "Module Setup"));

            this.scrollPane = new JScrollPane(this.moduleGraph);

            this.add(this.scrollPane);

        }

        /**
         * Returns the unique int id of the currently selected module
         * or -1 if no module is currently selected.
         * @return The unique int id of the currently selected module
         *         or -1 if no module is currently selected
         */
        public final int getSelectedModuleCell() {
            return ModuleGraph.getSelectedModule();
        }

        /**
         * Creates a new module cell to display.
         * @param moduleType
         *            Is the <em>MODULE_TYPE</em> of the module for
         *            which this cell is created
         * @param id
         *            Id the unique int id of the module, that is also
         *            asigned to the cell
         * @param name
         *            Is the name of the module, that is given to the
         *            cell
         */
        public final void createModuleCell(final ModuleType moduleType,
            final int id, final String name) {
            this.moduleGraph.createModuleCell(moduleType, id, name);

        }

        /**
         * Removes a module cell from display.
         * @param id
         *            Is the unique int id of the cell to remove.
         */
        public final void removeModuleCell(final int id) {
            this.moduleGraph.removeModuleCell(id);

        }

        /**
         * Check if a mocule cell with the gven id is currently
         * displayed.
         * @param id
         *            Is the unqie nt id of that to check
         * @return <code>true</code> if a module cell eith the given
         *         id is currently displayed and <code>false</code>
         *         if not
         */
        public final boolean containsModuleCell(final int id) {
            return this.moduleGraph.containsModuleCell(id);
        }

        /**
         * Changes the module cell with the given id to display the
         * given module.
         * @param id
         *            Is the unique int id of the cell to be updated
         * @param module
         *            The module that is same as before, but it might
         *            have changed its state
         */
        public final void updateModuleCell(final int id, final Module module) {
            this.moduleGraph.updateModuleCell(id, module);
        }
    }

    /**
     * This is the panel where the user browses and selects
     * <em>ModulePackages</em>.
     */
    private static class ModulePackagePanel extends JPanel {

        /**
         * Is the <em>Serialization</em> id.
         */
        private static final long serialVersionUID = -4050093216305936024L;

        /**
         * A panel displaying <em>ModulePackages</em> of
         * <em>SourceModules</em>.
         */
        private JPanel pnlSourceModules;

        /**
         * A panel displaying <em>ModulePackages</em> of
         * <em>IntermediateModules</em>.
         */
        private JPanel pnlIntermediateModules;

        /**
         * A panel displaying <em>ModulePackages</em> of
         * <em>TargetModules</em>.
         */
        private JPanel pnlTargetModules;

        /**
         * Is the currently selected <em>ModulePackage</em> button.
         */
        private static String selectedModuleButton;

        /**
         * Creates the panel.
         */
        public ModulePackagePanel() {
            this.pnlSourceModules = new ModulePackageSubPanel(
                ModuleType.SOURCE_MODULE);

            this.pnlIntermediateModules = new ModulePackageSubPanel(
                ModuleType.INTERMEDIATE_MODULE);

            this.pnlTargetModules = new ModulePackageSubPanel(
                ModuleType.TARGET_MODULE);

            this.setLayout(new GridLayout(3, 1));

            this.setBackground(UIConstants.PNL_MODULE_FINDER_BORDER_COLOR);

            this.setBorder(new TitledBorder(null, "Module Packages"));

            this.add(this.pnlSourceModules, 0);

            this.add(this.pnlIntermediateModules, 1);

            this.add(this.pnlTargetModules, 2);
        }

        /**
         * Adds a <em>ModulePackage</em> to this panel.
         * @param moduleDescriptor
         *            Id the <em>ModuleDescriptor</em> parsed from
         *            the
         *            <em>ModulePackage's</em> <em>ModuleDescription</em>.
         */
        public final void addModulePackage(
            final ModuleDescriptor moduleDescriptor) {
            ModulePackageLabel btnModule = new ModulePackageLabel(
                moduleDescriptor.getName(), moduleDescriptor.getId());

            switch (moduleDescriptor.getModuleType()) {
                case SOURCE_MODULE:

                    this.pnlSourceModules.add(btnModule);

                    break;

                case INTERMEDIATE_MODULE:

                    this.pnlIntermediateModules.add(btnModule);

                    break;

                default:

                    this.pnlTargetModules.add(btnModule);

                    break;

            }
        }
    }

    /**
     * The {@link Gui.ModulePackagePanel} contains three sub-panels.
     * Each one for every <em>MODULE_TYPE</em>.
     */
    private static class ModulePackageSubPanel extends JPanel {

        /**
         * Is the <em>Serialization</em> id.
         */
        private static final long serialVersionUID = -3847783604988648611L;

        /**
         * Creates the panel.
         * @param moduleType
         *            Is the <em>MODULE_TYPE</em> of the
         *            <em>ModulePackages</em> that are displayed in
         *            this panel.
         */
        public ModulePackageSubPanel(final ModuleType moduleType) {
            this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

            this.setBackground(UIConstants.PNL_INNER_FINDER_BACKGROUND_COLOR);

            switch (moduleType) {
                case SOURCE_MODULE:

                    this.setBorder(new TitledBorder(
                        new LineBorder(
                            UIConstants.PNL_INNER_FINDER_SOURCE_BORDER_COLOR,
                            UIConstants.PNL_INNER_FINDER_SOURCE_BORDER_WIDTH,
                            true), "Source Modules"));
                    break;

                case INTERMEDIATE_MODULE:

                    this.setBorder(new TitledBorder(new LineBorder(
                        UIConstants.PNL_INNER_FINDER_INTERMEDIATE_BORDER_COLOR,
                        UIConstants.PNL_INNER_FINDER_INTERMEDIATE_BORDER_WIDTH,
                        true), "Intermediate Modules"));

                    break;

                default:

                    this.setBorder(new TitledBorder(
                        new LineBorder(
                            UIConstants.PNL_INNER_FINDER_TARGET_BORDER_COLOR,
                            UIConstants.PNL_INNER_FINDER_TARGET_BORDER_WIDTH,
                            true), "Target Modules"));

                    break;
            }
        }
    }

    /**
     * This is how a <em>ModulePackage</em> is displayed int the
     * GUI.
     */
    private static class ModulePackageLabel extends JLabel {

        /**
         * Is the <em>Serialization</em> id.
         */
        private static final long serialVersionUID = -4795204347473120025L;

        /**
         * The unique <code>String</code> id of the
         * <em>ModulePackage</em> that is displayed by this label.
         */
        private String modulePackageId;

        /**
         * The name of the <em>ModulePackage</em> that is displayed
         * by this label.
         */

        private String modulePackageName;

        /**
         * Creates a label for a <em>ModulePackage</em>.
         * @param name
         *            The name of the <em>ModulePackage</em> that is
         *            displayed by this label
         * @param id
         *            The unique <code>String</code> id of the
         *            <em>ModulePackage</em> that is displayed by
         *            this label
         */
        public ModulePackageLabel(final String name, final String id) {
            super(name);

            this.modulePackageName = name;

            this.modulePackageId = id;

            this.setToolTipText("Click to add the " + this.modulePackageName
                                + " module. Right-Click to get Information.");

            this.setBackground(UIConstants.LBL_MODULE_BACKGROUND_COLOR);

            this.setOpaque(true);

            this.setVisible(true);

            this.addMouseListener(new MouseListener() {

                @SuppressWarnings("synthetic-access")
                public void mouseClicked(final MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON3) {
                        ModulePackagePanel.selectedModuleButton = ModulePackageLabel.this.modulePackageId;

                        MenuManager.showModuleFinderMenu(
                            ModulePackageLabel.this,
                            ModulePackageLabel.this.getWidth()
                                            - UIConstants.POPUP_MENU_XOFFSET,
                            ModulePackageLabel.this.getHeight()
                                            - UIConstants.POPUP_MENU_YOFFSET);
                    } else if (e.getButton() == MouseEvent.BUTTON1) {

                        try {
                            org.electrocodeogram.system.System.getInstance()
                                .getModuleRegistry().createModule(
                                    ModulePackageLabel.this.modulePackageId,
                                    ModulePackageLabel.this.modulePackageName);
                        } catch (ModuleInstantiationException e1) {
                            JOptionPane
                                .showMessageDialog(
                                    org.electrocodeogram.system.System
                                        .getInstance().getMainWindow(),
                                    e1.getMessage(),
                                    "Add "
                                                    + ModulePackageLabel.this.modulePackageName
                                                    + " module",
                                    JOptionPane.ERROR_MESSAGE);
                        } catch (ModulePackageNotFoundException e1) {
                            JOptionPane
                                .showMessageDialog(
                                    org.electrocodeogram.system.System
                                        .getInstance().getMainWindow(),
                                    e1.getMessage(),
                                    "Add "
                                                    + ModulePackageLabel.this.modulePackageName
                                                    + " module",
                                    JOptionPane.ERROR_MESSAGE);
                        }

                    }

                }

                public void mousePressed(@SuppressWarnings("unused")
                final MouseEvent e) {
                    ModulePackageLabel.this
                        .setBackground(UIConstants.LBL_MODULE_MOUSEPRESSED_BACKGROUND_COLOR);

                    ModulePackageLabel.this.repaint();

                }

                public void mouseReleased(@SuppressWarnings("unused")
                final MouseEvent e) {
                    ModulePackageLabel.this
                        .setBackground(UIConstants.LBL_MODULE_MOUSERELEASED_BACKGROUND_COLOR);

                    ModulePackageLabel.this.repaint();

                }

                public void mouseEntered(@SuppressWarnings("unused")
                final MouseEvent e) {
                    ModulePackageLabel.this
                        .setBackground(UIConstants.LBL_MODULE_MOUSEOVER_BACKGROUND_COLOR);

                    ModulePackageLabel.this.setBorder(new LineBorder(
                        UIConstants.LBL_MODULE_MOUSEOVER_BORDER_COLOR));

                    ModulePackageLabel.this.repaint();

                }

                public void mouseExited(@SuppressWarnings("unused")
                final MouseEvent e) {
                    ModulePackageLabel.this
                        .setBackground(UIConstants.LBL_MODULE_MOUSEOUT_BACKGROUND_COLOR);

                    ModulePackageLabel.this.setBorder(null);

                    ModulePackageLabel.this.repaint();

                }
            });

        }
    }
}

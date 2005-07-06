package org.electrocodeogram.ui;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import org.electrocodeogram.core.SensorServer;
import org.electrocodeogram.module.Module;
import org.electrocodeogram.module.ModuleDescriptor;
import org.electrocodeogram.module.ModuleRegistry;
import org.electrocodeogram.module.Module.ModuleType;

import com.zfqjava.swing.JStatusBar;

/**
 * @author 7oas7er
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */

public class Configurator extends JFrame implements Observer
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * 
     * @uml.property name="theInstance"
     * @uml.associationEnd multiplicity="(0 1)"
     */
    private static Configurator theInstance = null;

    private MessagesFrame frmMessages = null;

    /**
     * 
     * @uml.property name="source"
     * @uml.associationEnd multiplicity="(0 1)"
     */
    //private Configurator me = null;
    private ArrayList sourceModules = new ArrayList();

    private int FileWriterID = -1;

    private int LogggerWriterID = -1;

    /**
     * 
     * @uml.property name="moduleGraph"
     * @uml.associationEnd multiplicity="(0 1)"
     */
    private ModuleGraph moduleGraph = null;

    private SensorGraph sensorGraph = null;

    private JPanel pnlModules;

    private JStatusBar statusBar;

    private JPanel pnlSensors;

    private JPanel pnlButtons = null;

    private JMenuBar menuBar = null;
    
    private JSplitPane splitPane = null;

    private JMenu menu2;

    private JMenu menu3;

    private int selectedModuleCellId = -1;

    private boolean moduleConnectionMode;

    private int sourceModuleId;

    public static Configurator getInstance(Module source)
    {
        assert (source != null);

        if (theInstance == null) {
            theInstance = new Configurator(source);
        }
        else {

            if (source.getModuleType() == ModuleType.SOURCE_MODULE) {
                theInstance.sourceModules.add(source);
            }

        }
        return theInstance;
    }

    /**
     * @return
     */
    public static Configurator getInstance()
    {
        if (theInstance == null) {
            theInstance = new Configurator();
        }
        return theInstance;
    }

    private Configurator()
    {
        super();

        try {

            UIManager.setLookAndFeel("org.fife.plaf.Office2003.Office2003LookAndFeel");
        }
        catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (UnsupportedLookAndFeelException e) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }
            catch (ClassNotFoundException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            catch (InstantiationException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            catch (IllegalAccessException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            catch (UnsupportedLookAndFeelException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }

        setTitle("ElectroCodeoGram Configurator");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setExtendedState(JFrame.MAXIMIZED_BOTH);

        setBounds(0, 0, 800, 600);

        getContentPane().setLayout(new GridBagLayout());

        menuBar = new JMenuBar();

        JMenu menu1 = new JMenu("Datei");
        JMenuItem menuItem11 = new JMenuItem("Beenden");
        menuItem11.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e)
            {
                Configurator.getInstance().dispose();
                System.exit(0);

            }
        });
        menu1.add(menuItem11);

        JMenu menu2 = new JMenu("Aufzeichnung");
        JMenuItem menuitem21 = new JMenuItem("Anhalten");
        menuitem21.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e)
            {

                ModuleRegistry.getInstance().stopModule(1);

            }
        });

        JMenuItem menuitem22 = new JMenuItem("Fortsetzen");
        menuitem22.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e)
            {

                ModuleRegistry.getInstance().startModule(1);

            }
        });

        menu2.add(menuitem21);
        menu2.add(menuitem22);

        menu3 = new JMenu("Modul");
        menu3.addMouseListener(new MouseAdapter() {

            public void mouseEntered(MouseEvent e)
            {
                MenuManager.getInstance().populateModuleMenu(menu3, moduleGraph.getSelectedModuleCellId());
            }
        });

        JMenu menu4 = new JMenu("Fenster");
        JMenuItem menuitem41 = new JMenuItem("Ereignisfenster");
        menuitem41.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e)
            {

                showMessagesWindow();

            }
        });
        menu4.add(menuitem41);

        menuBar.add(menu1);
        menuBar.add(menu2);
        menuBar.add(menu3);
        menuBar.add(menu4);

        this.setJMenuBar(menuBar);

        sensorGraph = new SensorGraph();
        pnlSensors = new JPanel(new GridLayout(1, 1));
        pnlSensors.setBorder(new TitledBorder(
                new LineBorder(new Color(0, 0, 0)), "Active sensors"));
        pnlSensors.add(sensorGraph);

        moduleGraph = new ModuleGraph(this);
        pnlModules = new JPanel(new GridLayout(1, 1));
        pnlModules.setBorder(new TitledBorder(
                new LineBorder(new Color(0, 0, 0)), "Tree of running modules"));
        pnlModules.add(moduleGraph);

        pnlButtons = new JPanel();

        pnlButtons.setLayout(new BoxLayout(pnlButtons, BoxLayout.Y_AXIS));

        pnlButtons.setBackground(Color.WHITE);

        pnlButtons.setBorder(new TitledBorder("Installed modules"));

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

        splitPane.add(pnlButtons, 0);

        JPanel pnlRight = new JPanel(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTH;
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 0;
        c.weighty = 0.2;
        c.weightx = 1;

        pnlRight.add(pnlSensors, c);

        c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTH;
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 1;
        c.weighty = 1;
        c.weightx = 1;

        pnlRight.add(pnlModules, c);

        c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTH;
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 2;
        c.weighty = 1;
        c.weightx = 1;

        //pnlRight.add(pnlMessages, c);

        splitPane.add(pnlRight, 1);

        c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 0;
        c.weighty = 2;
        c.weightx = 2;

        getContentPane().add(splitPane, c);

        statusBar = new JStatusBar(JStatusBar.EXPLORER);

        GridBagConstraints c2 = new GridBagConstraints();
        c2.anchor = GridBagConstraints.SOUTHWEST;
        c2.fill = GridBagConstraints.HORIZONTAL;
        c2.gridx = 0;
        c2.gridy = 2;
        c2.weighty = 0;
        c2.weightx = 2;

        getContentPane().add(statusBar, c2);

        //frmMessages = new MessagesFrame();

        setVisible(true);
    }

    public void showMessagesWindow()
    {
        if(frmMessages == null)
        {
            frmMessages = new MessagesFrame();
            
            frmMessages.setSelectedModul(moduleGraph.getSelectedModuleCellId());
        }
        
        this.frmMessages.show();
    }

    /**
     * @throws java.awt.HeadlessException
     */
    private Configurator(Module source) throws HeadlessException
    {
        this();
        if (source.getModuleType() == ModuleType.SOURCE_MODULE) {

            this.sourceModules.add(source);
            //traverseConnectedModules(null, this.sourceModules.get(0));
        }
    }

    /**
     * 
     * @uml.property name="source"
     */
    //    public Module getSource()
    //    {
    //        return source;
    //    }
    //    public Module getModule(int id)
    //    {
    //
    //        assert (id > 0);
    //
    //        return ModuleRegistry.getInstance().getModuleInstance(id);
    //
    //    }
    /*
     * (non-Javadoc)
     * 
     * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
     */
    public void update(Observable o, Object arg)
    {
        assert (arg != null);

        /* if the ModuleRegistry is sending the event, a module-instance has been added or removed
         * or a module class has been installed
         */
        if (o instanceof ModuleRegistry) {

            // a module has been added or removed
            if (arg instanceof Module && (!(arg instanceof GuiEventWriter))) {

                Module module = (Module) arg;

                if(moduleGraph.containsModuleCell(module.getId()))
                {
                    moduleGraph.removeModuleCell(module.getId());
                }
                else
                {
                    ModuleCell ml = new ModuleCell(module.getModuleType(),
                        module.getId(), module.getName(), module.isActive());

                    moduleGraph.addModuleCell(ml);
                }
               
            }
            // a module class has been intalled
            else if (arg instanceof ModuleDescriptor) {
                ModuleDescriptor moduleDescriptor = (ModuleDescriptor) arg;

                String moduleName = moduleDescriptor.getName();

                JButton btnModule = new JButton(moduleName);

                btnModule.addActionListener(new ActionAdapter(this, moduleName));

                pnlButtons.add(btnModule);
                
                splitPane.remove(pnlButtons);
                
                splitPane.add(pnlButtons);
            }
        }
        else if(o instanceof Module)
        {
            if(arg instanceof Module)
            {
                Module module = (Module) arg;
                
                int id = module.getId();
                
                if(moduleGraph.containsModuleCell(id))
                {
                    moduleGraph.updateModuleCell(id,module);
                }
            }
        }
        else {
            if (arg instanceof SensorServer) {
                SensorServer seso = (SensorServer) arg;

                int activeSensors = seso.getSensorCount();

                String text = "Active Sensors: " + activeSensors;

                JLabel lbl = (JLabel) statusBar.getComponent(4);

                lbl.setText(text);

                String[] address;

                if ((address = seso.getAddress()) != null) {
                    text = "Listening on: " + address[0] + ":" + address[1];

                    lbl = (JLabel) statusBar.getComponent(0);

                    lbl.setText(text);
                }

                if (activeSensors > 0) {
                    pnlSensors.remove(sensorGraph);

                    sensorGraph = new SensorGraph();

                    pnlSensors.add(sensorGraph);

                    InetAddress[] addresses = seso.getSensorAddresses();

                    String[] sensorNames = seso.getSensorNames();

                    for (int i = 0; i < activeSensors; i++) {
                        SensorCell sc;

                        if (sensorNames[i] == null) {
                            sc = new SensorCell(
                                    this,
                                    "Unknown Sensor at" + addresses[i].toString());
                        }
                        else {
                            sc = new SensorCell(
                                    this,
                                    sensorNames[i] + "-Sensor \nat: " + addresses[i].toString());
                        }

                        sensorGraph.addSensorCell(sc);
                    }

                }
                else if (activeSensors == 0) {
                    pnlSensors.remove(sensorGraph);

                    sensorGraph = new SensorGraph();

                    pnlSensors.add(sensorGraph);
                }

            }
            else if (arg instanceof ModuleGraph) {
                this.selectedModuleCellId = moduleGraph.getSelectedModuleCellId();

                if (selectedModuleCellId == -1) {
                    menu3.setEnabled(false);
                }
                else {
                    menu3.setEnabled(true);
                }
                if (frmMessages != null) {
                    frmMessages.setSelectedModul(selectedModuleCellId);
                }
            }
            
        }
      
    }

    /**
     *  
     */
    //    private void traverseConnectedModules(ModuleCell parent, Object module)
    //    {
    //        assert (module != null);
    //
    //        Module m = (Module) module;
    //
    //        ModuleCell ml = new ModuleCell(this, m.getModuleType(), m.getId(),
    //                m.getName(), m.isRunning());
    //
    //        moduleGraph.addModuleCell(ml);
    //
    //        if (parent != null) {
    //            DefaultEdge edge = new DefaultEdge();
    //
    //            GraphConstants.setLineEnd(edge.getAttributes(),
    // GraphConstants.ARROW_CLASSIC);
    //
    //            GraphConstants.setDisconnectable(edge.getAttributes(), false);
    //
    //            edge.setSource(parent.getChildAt(0));
    //
    //            edge.setTarget(ml.getChildAt(0));
    //
    //            moduleGraph.addEdge(edge);
    //        }
    //        if (m.countChildModules() > 0) {
    //            Object[] modules = m.getChildModules();
    //
    //            assert (modules != null);
    //
    //            for (int i = 0; i < modules.length; i++) {
    //                traverseConnectedModules(ml, modules[i]);
    //            }
    //        }
    //
    //    }
    //
    public int getSelectedModuleCellId()
    {
        return selectedModuleCellId;

    }

    /**
     *  
     */
    public void showModuleDetails()
    {

        int id = moduleGraph.getSelectedModuleCellId();

        if (id != -1) {

            String text = ModuleRegistry.getInstance().getModuleDetails(id);

            JOptionPane.showMessageDialog(this, text, "Moduleigenschaften", JOptionPane.INFORMATION_MESSAGE);
        }

    }

    /**
     * @param selectedModuleCellId2
     */
    public void enterModuleConnectionMode(int selectedModuleCellId)
   	{
        
        moduleConnectionMode = true;
        
        sourceModuleId = selectedModuleCellId;
        
        
    }

    /**
     * @return
     */
    public boolean getModuleConnectionMode()
    {
        
        return moduleConnectionMode;
    }

    /**
     * @return
     */
    public int getSourceModule()
    {
        
        return sourceModuleId;
    }

    /**
     * 
     */
    public void exitModuleConnectionMode()
    {
        moduleConnectionMode = false;
        
        sourceModuleId = -1;
        
    }

    public void shutDown()
    {
       this.dispose();
       
       theInstance = null;
        
    }

}
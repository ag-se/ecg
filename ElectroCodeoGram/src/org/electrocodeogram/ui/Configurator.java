package org.electrocodeogram.ui;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.util.List;
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
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.electrocodeogram.core.SensorServer;
import org.electrocodeogram.module.Module;
import org.electrocodeogram.module.ModuleRegistry;
import org.electrocodeogram.sensorwrapper.EventPacket;
import org.jgraph.graph.DefaultEdge;
import org.jgraph.graph.GraphConstants;
import org.jgraph.layout.TreeLayoutAlgorithm;

import com.zfqjava.swing.JStatusBar;
/**
 * @author 7oas7er *  * TODO To change the template for this generated type comment go to * Window - Preferences - Java - Code Style - Code Templates
 */

public class Configurator extends JFrame implements Observer
{

    /**
     * 
     * @uml.property name="theInstance"
     * @uml.associationEnd multiplicity="(0 1)"
     */
    private static Configurator theInstance = null;

    /**
     * 
     * @uml.property name="source"
     * @uml.associationEnd multiplicity="(0 1)"
     */
    //private Configurator me = null;
    private Module source = null;

    
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
    
    private JPanel pnlMessages;
    
    private JTextArea textArea;
    
    private JScrollPane scrollPane;
    
    private JStatusBar statusBar;
    
    private boolean shouldScroll = false;

    private JPanel pnlSensors;
    
    private JMenuBar menuBar = null;
    
    private JMenu menu2;
    
    public static Configurator getInstance(Module source)
    {
        assert(source != null);
        
        
        
        if(theInstance == null)
        {
            theInstance = new Configurator(source);
        }
        else
        {
            try
            {
                assert(source.getModuleType() == Module.SOURCE_MODULE);
                
                theInstance.source = source;
            }
            catch(AssertionError e)
            {
                ;
            }
        }
        return theInstance;
    }
    
    /**
     * @return
     */
    public static Configurator getInstance()
    {
        if(theInstance == null)
        {
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
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
          
        
        
        setTitle("ElectroCodeoGram Configurator");
    
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        
        setBounds(0,0,800,600);
                     
        getContentPane().setLayout(new GridBagLayout());
        
        menuBar = new JMenuBar();
        
        JMenu menu1 = new JMenu("Datei");
        JMenuItem menuItem11 = new JMenuItem("Beenden");
        menuItem11.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent e)
            {
               Configurator.getInstance().dispose();
               System.exit(0);
                
            }});
        menu1.add(menuItem11);
        
        JMenu menu2 = new JMenu("Aufzeichnung");
        JMenuItem menuitem21 = new JMenuItem("Anhalten");
        menuitem21.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent e)
            {
                
                ModuleRegistry.getInstance().getModuleInstance(1).stop();
                
            }});
        
        JMenuItem menuitem22 = new JMenuItem("Fortsetzen");
        menuitem22.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent e)
            {

                ModuleRegistry.getInstance().getModuleInstance(1).start();
                
            }});
        menu2.add(menuitem21);
        menu2.add(menuitem22);
        
        menuBar.add(menu1);
        menuBar.add(menu2);
        
        this.setJMenuBar(menuBar);
        
        
        
        sensorGraph = new SensorGraph();
        pnlSensors = new JPanel(new GridLayout(1,1));
        pnlSensors.setBorder(new TitledBorder(new LineBorder(new Color(0,0,0)),"Active sensors"));
        pnlSensors.add(sensorGraph);
        
        moduleGraph = new ModuleGraph(this);    
        pnlModules  = new JPanel(new GridLayout(1,1));
        pnlModules.setBorder(new TitledBorder(new LineBorder(new Color(0,0,0)),"Tree of running modules"));
        pnlModules.add(moduleGraph);
        
        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setLineWrap(true);
          
        scrollPane = new JScrollPane(textArea);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().getModel().addChangeListener(new ChangeListener(){

            public void stateChanged(ChangeEvent e)
            {
                if (shouldScroll)
                {
                    JScrollBar vertBar = scrollPane.getVerticalScrollBar();
                    vertBar.setValue(vertBar.getMaximum());
                    shouldScroll = false;
                }

                
            }});
                
        pnlMessages = new JPanel(new GridLayout(1,1));
        pnlMessages.setBorder(new TitledBorder(new LineBorder(new Color(0,0,0)),"Event messages in selected module"));
        pnlMessages.add(scrollPane);
        
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        
        splitPane.add(getButtonPanel(),0);
        
        JPanel pnlRight = new JPanel(new GridBagLayout());
        
        
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTH;
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 0;
        c.weighty = 0.2;
        c.weightx = 1;
        
        pnlRight.add(pnlSensors,c);
        
        c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTH;
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 1;
        c.weighty = 1;
        c.weightx = 1;
        
        pnlRight.add(pnlModules,c);
                
        c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTH;
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 2;
        c.weighty = 1;
        c.weightx = 1;
        
        pnlRight.add(pnlMessages,c);
        
        splitPane.add(pnlRight,1);
        
        c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 0;
        c.weighty = 2;
        c.weightx = 2;
        
        getContentPane().add(splitPane,c);
    
        statusBar = new JStatusBar(JStatusBar.EXPLORER);
                           
        GridBagConstraints c2 = new GridBagConstraints();
        c2.anchor = GridBagConstraints.SOUTHWEST;
        c2.fill = GridBagConstraints.HORIZONTAL;
        c2.gridx = 0;
        c2.gridy = 2;
        c2.weighty = 0;
        c2.weightx = 2;
        
        getContentPane().add(statusBar,c2);
        
        setVisible(true);
    }
  
    private JPanel getButtonPanel()
    {
        JPanel pnlButtons = null;
        
        pnlButtons = new JPanel();
                        
        pnlButtons.setLayout(new BoxLayout(pnlButtons,BoxLayout.Y_AXIS));
        
        pnlButtons.setBackground(Color.WHITE);
        
        pnlButtons.setBorder(new TitledBorder("Installed modules"));
        
        ModuleRegistry moduleRegistry = ModuleRegistry.getInstance();
        
        Object[] moduleNameObjects = moduleRegistry.getInstalledModulesNames();
        
        assert(moduleNameObjects != null);
        
        for(int i=0;i<moduleNameObjects.length;i++)
        {
            String moduleName = (String)moduleNameObjects[i];
            
            Class moduleClass = moduleRegistry.getModuleClassForName(moduleName);
      
            JButton btnModule = new JButton("Add a " + moduleName);
                        
            btnModule.addActionListener(new ActionAdapter(this,moduleClass));
            
            pnlButtons.add(btnModule);
            
        }
        
        return pnlButtons;
    }
    
    /**
     * @throws java.awt.HeadlessException
     */
    private Configurator(Module source) throws HeadlessException
    {
              
        this();
        if (source.getModuleType() == Module.SOURCE_MODULE)
        {
            this.source = source;
            traverseConnectedModules(null,source);
        }
    }

    /**
     * 
     * @uml.property name="moduleGraph"
     */
    public ModuleGraph getModuleGraph() {
        return moduleGraph;
    }

    /**
     * 
     * @uml.property name="source"
     */
    public Module getSource() {
        return source;
    }

    
    public Module getModule(int id)
    {

        assert(id > 0);
        
        return ModuleRegistry.getInstance().getModuleInstance(id);

    }
    
    /* (non-Javadoc)
     * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
     */
    public void update(Observable o, Object arg)
    {
        assert(arg != null);
        
        if (arg instanceof Module)
        {
            pnlModules.remove(moduleGraph);
            
            moduleGraph = new ModuleGraph(this);
            
            pnlModules.add(moduleGraph);
            
            traverseConnectedModules(null,source);
            
            TreeLayoutAlgorithm tla = new TreeLayoutAlgorithm();
            
            tla.setOrientation(SwingConstants.WEST);
                   
            tla.setAlignment(SwingConstants.CENTER);
            
            tla.setCenterRoot(true);
                 
            tla.run(moduleGraph,moduleGraph.getRoots());
            
            
        }
        else if(arg instanceof EventPacket)
        {
            // TODO : Writer erben
            
            EventPacket eventPacket = (EventPacket) arg;
            
            if(eventPacket.getEventSourceId() == moduleGraph.getSelectedModuleCellId())
            {
                textArea.append(eventPacket.getTimeStamp().toString() + " : " + eventPacket.getCommandName());
                
                List argList = eventPacket.getArglist();
                
                if (argList != null)
                {
                
                Object[] args = eventPacket.getArglist().toArray();
                
                
                    for(int i = 0; i < args.length; i++)
                    {
                        String str = (String) args[i];
                        
                        textArea.append(" " + str);
                    }
                    
                }
                textArea.append("\n");
                
                JScrollBar vertBar = scrollPane.getVerticalScrollBar();
                if (vertBar.getValue() == vertBar.getMaximum() - vertBar.getVisibleAmount())
                {
                    shouldScroll = true;
                }

                
            }
        }
        else if(arg instanceof SensorServer)
        {
            SensorServer seso = (SensorServer) arg;
            
            int activeSensors = seso.getSensorCount();
            
            String text = "Active Sensors: " + activeSensors;
            
            JLabel lbl = (JLabel) statusBar.getComponent(4);
            
            lbl.setText(text);
            
            String[] address;
            
            if((address = seso.getAddress()) != null)
            {
                text = "Listening on: " + address[0] + ":" + address[1];
                
                lbl = (JLabel) statusBar.getComponent(0);
                
                lbl.setText(text);
            }
            
            if(activeSensors > 0)
            {
                pnlSensors.remove(sensorGraph);
                
                sensorGraph = new SensorGraph();
                
                pnlSensors.add(sensorGraph);
                
                InetAddress[] addresses = seso.getSensorAddresses();
                
                String[] sensorNames = seso.getSensorNames();
                
                for (int i=0;i<activeSensors;i++)
                {
                    SensorCell sc;
                    
                    if(sensorNames[i] == null)
                    {
                        sc = new SensorCell(this,"Unknown Sensor at" + addresses[i].toString());
                    }
                    else
                    {
                        sc = new SensorCell(this,sensorNames[i] + "-Sensor \nat: " + addresses[i].toString());
                    }
		  
	                sensorGraph.addSensorCell(sc);
	            }
              
            }
            else if(activeSensors == 0)
            {
                pnlSensors.remove(sensorGraph);
                
                sensorGraph = new SensorGraph();
                                             
                pnlSensors.add(sensorGraph);
            }
            
            pnlSensors.repaint();
        }
    }


  
    /**
     * 
     */
    private void traverseConnectedModules(ModuleCell parent, Object module)
    {
        assert(module != null);
        
        Module m = (Module) module;
        
        ModuleCell ml = new ModuleCell(this,m.getModuleType(),m.getId(),m.getName(),m.isRunning());
        
        moduleGraph.addModuleCell(ml);
        
        if(parent != null)
        {
            DefaultEdge edge = new DefaultEdge();
            
            GraphConstants.setLineEnd(edge.getAttributes(),GraphConstants.ARROW_CLASSIC);
            
            GraphConstants.setDisconnectable(edge.getAttributes(),false);
            
    		edge.setSource(parent.getChildAt(0));
    		
    		edge.setTarget(ml.getChildAt(0));
    		   		   		
    		moduleGraph.addEdge(edge);
        }
        if(m.countConnectedModules() > 0)
        {
            Object[] modules = m.getConnectedModules();
            
            assert(modules != null);
            
            for(int i=0;i<modules.length;i++)
            {
                traverseConnectedModules(ml,modules[i]);
            }
        }

        
    }

    /**
     * 
     */
    public void showModuleDetails()
    {
       
        int id = moduleGraph.getSelectedModuleCellId();
        
        if(id != -1)
        {
       
	        Module m = ModuleRegistry.getInstance().getModuleInstance(id);
	        
	        
	        
	        String text = m.getDetails();
	        
	        JOptionPane.showMessageDialog(this,text);
        }
        
    }

}

package org.electrocodeogram.ui;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;





import org.electrocodeogram.core.EventPacket;
import org.electrocodeogram.module.Module;
import org.electrocodeogram.module.ModuleRegistry;
import org.jgraph.graph.DefaultEdge;
import org.jgraph.graph.GraphConstants;
import org.jgraph.layout.TreeLayoutAlgorithm;

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


    private JPanel pnlModules;
    
    private JPanel pnlMessages;
    
    private JTextArea textArea;
    
    private JScrollPane scrollPane;
    
    private boolean shouldScroll = false;
    
    public static Configurator getInstance(Module source)
    {
        assert(source != null);
        
        assert(source.getModuleType() == Module.SOURCE_MODULE);
        
        if(theInstance == null)
        {
            theInstance = new Configurator(source);
        }
        return theInstance;
    }
    
    /**
     * @throws java.awt.HeadlessException
     */
    private Configurator(Module source) throws HeadlessException
    {
              
        super();
        
        try {
            
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
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
          
        this.source = source;
        
        setTitle("ElectroCodeoGram Configurator");
    
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        
        setBounds(0,0,800,600);
                     
        getContentPane().setLayout(new GridLayout(3,1));
        
        moduleGraph = new ModuleGraph();
        
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
        
        getContentPane().add(pnlModules);
        
        getContentPane().add(pnlMessages);
        
        traverseConnectedModules(null,source);
        
        createButtons();
                
        setVisible(true);
    }
  
    private void createButtons()
    {
        JPanel pnlButtons = new JPanel(new FlowLayout());
                        
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
        
        getContentPane().add(pnlButtons);
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
            
            moduleGraph = new ModuleGraph();
            
            pnlModules.add(moduleGraph);
            
            traverseConnectedModules(null,source);
            
            TreeLayoutAlgorithm tla = new TreeLayoutAlgorithm();
            
            tla.setOrientation(SwingConstants.NORTH);
                   
            tla.setAlignment(SwingConstants.CENTER);
            
            tla.setCenterRoot(false);
                 
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
  
    }


  
    /**
     * 
     */
    private void traverseConnectedModules(ModuleCell parent, Object module)
    {
        assert(module != null);
        
        Module m = (Module) module;
        
        ModuleCell ml = new ModuleCell(this,m.getModuleType(),m.getId(),m.getName());
        
        moduleGraph.addCell(ml);
        
        if(parent != null)
        {
            DefaultEdge edge = new DefaultEdge();
            
            GraphConstants.setLineEnd(edge.getAttributes(),GraphConstants.ARROW_CLASSIC);
            
            GraphConstants.setDisconnectable(edge.getAttributes(),false);
            
    		edge.setSource(parent.getChildAt(0));
    		
    		edge.setTarget(ml.getChildAt(0));
    		
    		
    		
    		moduleGraph.addCell(edge);
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
 

}

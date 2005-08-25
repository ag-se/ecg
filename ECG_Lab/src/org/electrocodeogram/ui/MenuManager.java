/*
 * Created on 05.04.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.electrocodeogram.ui;

import java.awt.Component;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Properties;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import org.electrocodeogram.module.Module;
import org.electrocodeogram.module.ModuleConnectionException;
import org.electrocodeogram.module.ModuleDescriptor;
import org.electrocodeogram.module.ModuleProperty;
import org.electrocodeogram.module.ModulePropertyException;
import org.electrocodeogram.module.Module.ModuleType;
import org.electrocodeogram.module.intermediate.IIntermediateModule;
import org.electrocodeogram.module.intermediate.IntermediateModule;

import org.electrocodeogram.module.registry.ModuleClassException;
import org.electrocodeogram.module.registry.ModuleInstanceException;
import org.electrocodeogram.system.SystemRoot;



/**
 * @author 7oas7er
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MenuManager
{

    private Gui $gui = null;
 
    private ModulePopupMenu modulePopupMenu = null;
    
    private EdgePopupMenu edgePopupMenu = null;
    
    private JMenuItem mniModuleDetails = new JMenuItem("Eigenschaften");
    
    private JMenuItem mniModuleRemove = new JMenuItem("Entfernen");
    
    private JMenuItem mniEdgeRemove = new JMenuItem("Entfernen");
    
    private JMenuItem mniModuleConnectTo = new JMenuItem("Verbinden mit...");
    
    private JMenuItem mniModuleStop = new JMenuItem("Stop");
        
    private JMenuItem mniModuleStart = new JMenuItem("Start");
    
    private JMenuItem mniMsgWindowShow = new JMenuItem("Ereignisfenster");
    
    private JMenuItem mniMakeAnnotator = new JMenuItem("Annotation");
    
    private JMenuItem mniMakeFilter = new JMenuItem("Filterung");
    
    public MenuManager(Gui gui)
    {
        this.$gui = gui;
        
        mniModuleRemove.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent e)
            {
               
                	try {
                        SystemRoot.getSystemInstance().getSystemModuleRegistry().getRunningModule(SystemRoot.getSystemInstance().getGui().getSelectedModuleCellId()).remove();
                    }
                    catch (ModuleInstanceException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
               
                
            }});
        
        mniModuleDetails.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent e)
            {
            	SystemRoot.getSystemInstance().getGui().showModuleDetails();                
            }});
      
        mniModuleStop.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent e)
            {
               
            	   try {
                    SystemRoot.getSystemInstance().getSystemModuleRegistry().getRunningModule(SystemRoot.getSystemInstance().getGui().getSelectedModuleCellId()).deactivate();
                }
                catch (ModuleInstanceException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
           
                
            }});
        
        mniModuleStart.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent e)
            {
             
            	   try {
                    SystemRoot.getSystemInstance().getSystemModuleRegistry().getRunningModule(SystemRoot.getSystemInstance().getGui().getSelectedModuleCellId()).activate();
                }
                catch (ModuleInstanceException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
           
                
            }});
        
        mniMsgWindowShow.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent e)
            {
            	SystemRoot.getSystemInstance().getGui().showMessagesWindow();
                
            }});
        
        mniMakeAnnotator.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent e)
            {
                Module module = null;
                
                	try {
                        SystemRoot.getSystemInstance().getSystemModuleRegistry().getRunningModule(SystemRoot.getSystemInstance().getGui().getSelectedModuleCellId());
                    }
                    catch (ModuleInstanceException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
              
                if (module instanceof IntermediateModule)
                {
                    IntermediateModule eventProcessor = (IntermediateModule) module;
                    
                    eventProcessor.setProcessingMode(IntermediateModule.ProcessingMode.ANNOTATOR);

                }
                
            }});
    
        mniModuleConnectTo.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent e)
            {
            	SystemRoot.getSystemInstance().getGui().enterModuleConnectionMode(SystemRoot.getSystemInstance().getGui().getSelectedModuleCellId());
                
            }});
        
        mniMakeFilter.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent e)
            {
                Module module = null;
                
                	try {
                        SystemRoot.getSystemInstance().getSystemModuleRegistry().getRunningModule(SystemRoot.getSystemInstance().getGui().getSelectedModuleCellId());
                    }
                    catch (ModuleInstanceException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
               
                if (module instanceof IntermediateModule)
                {
                    IntermediateModule eventProcessor = (IntermediateModule) module;
                    
                    eventProcessor.setProcessingMode(IntermediateModule.ProcessingMode.FILTER);

                }
                
            }});
        
    }

 
    public void showModuleMenu(int moduleId, Component c, int x, int y)
    {
        modulePopupMenu = new ModulePopupMenu();
        
        modulePopupMenu.add(mniModuleStart);
        
        modulePopupMenu.add(mniModuleStop);
        
        modulePopupMenu.addSeparator();
        
        
            try {
                if(!SystemRoot.getSystemInstance().getSystemModuleRegistry().getRunningModule(moduleId).isModuleType(ModuleType.TARGET_MODULE))
                {
                    modulePopupMenu.add(mniModuleConnectTo);
                }
            }
            catch (ModuleInstanceException e2) {
                // TODO Auto-generated catch block
                e2.printStackTrace();
            }
       
                
        modulePopupMenu.add(mniModuleRemove);
        
        modulePopupMenu.addSeparator();
        
        modulePopupMenu.add(mniMsgWindowShow);
        
        
        
       
            try {
                if(SystemRoot.getSystemInstance().getSystemModuleRegistry().getRunningModule(moduleId).isModuleType(ModuleType.INTERMEDIATE_MODULE))
                {
                    modulePopupMenu.addSeparator();
                    
                    modulePopupMenu.add(mniMakeAnnotator);
                    
                    modulePopupMenu.add(mniMakeFilter);
                }
            }
            catch (ModuleInstanceException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
       
        
       
        ModuleProperty[] moduleProperties = null;
        
            
            String moduleClassId = null;
            try {
                moduleClassId = SystemRoot.getSystemInstance().getSystemModuleRegistry().getRunningModule(moduleId).getClassId();
            }
            catch (ModuleInstanceException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
            ModuleDescriptor moduleDescriptor = null;
            try {
                moduleDescriptor = SystemRoot.getSystemInstance().getSystemModuleRegistry().getModuleDescriptor(moduleClassId);
            }
            catch (ModuleClassException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
            moduleProperties = moduleDescriptor.getProperties();
      
        
        if(moduleProperties != null)
        {
                for (int i=0;i<moduleProperties.length;i++)
                {
                    String propertyName = moduleProperties[i].getName();
                    
                    Class propertyType = moduleProperties[i].getType();
                
                    Object propertyValue = moduleProperties[i].getValue();
                    
                        JMenuItem menuItem = new JMenuItem(propertyName);
                      
                        menuItem.addActionListener(new PropertyActionAdapter($gui,moduleId,propertyType,propertyName,propertyValue));                    
                        
                        modulePopupMenu.add(menuItem);
                    
                    
                 
                }
            
        }
        
        
        modulePopupMenu.addSeparator();
        
        modulePopupMenu.add(mniModuleDetails);
        
        modulePopupMenu.show(c,x,y);
    }
    
    public void showEdgeMenu(int parentId, int childId, Component c, int x, int y)
    {
        edgePopupMenu = new EdgePopupMenu();
        
        mniEdgeRemove.addActionListener(new EdgeRemoveAdapter(parentId,childId));
        
        edgePopupMenu.add(mniEdgeRemove);
        
        edgePopupMenu.show(c,x,y);
        
    }
    
    private static class EdgeRemoveAdapter implements ActionListener
    {

        private int parentId;
        
        private int childId;

        /**
         * @param parentId
         * @param childId
         */
        public EdgeRemoveAdapter(int parentId, int childId)
        {
            
            this.parentId = parentId;
            
            this.childId = childId;
        }

        /* (non-Javadoc)
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e)
        {
           
            	try {
                    SystemRoot.getSystemInstance().getSystemModuleRegistry().getRunningModule(parentId).connectReceiverModule(SystemRoot.getSystemInstance().getSystemModuleRegistry().getRunningModule(childId));
                }
                catch (ModuleConnectionException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                catch (ModuleInstanceException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
           
            
        }
        
    }
    
    private static class PropertyActionAdapter implements ActionListener
    {

        private int $moduleId;
        
        private String $propertyName;
        
        private Object $propertyResult;
        
        private Object $propertyValue;
        
        private Class $propertyType;
        
        private Gui $gui;
        
        public PropertyActionAdapter(Gui gui, int moduleId, Class propertyType, String propertyName, Object propertyValue)
        {
            this.$gui = gui;
            
            this.$moduleId = moduleId;
            
            this.$propertyName = propertyName;
            
            this.$propertyType = propertyType;
            
            this.$propertyValue = propertyValue;
            
        }
        
        public void actionPerformed(ActionEvent e)
        {
           
             
                try {
                    if($propertyType.equals(Class.forName("java.lang.String")))
                    {
                       $propertyResult = JOptionPane.showInputDialog($gui,"Geben Sie den neuen Wert ein","",JOptionPane.QUESTION_MESSAGE);
                    }
                    else if($propertyType.equals(Class.forName("java.lang.Integer")))
                    {
                        $propertyResult = JOptionPane.showInputDialog($gui,"Geben Sie den neuen Wert ein","",JOptionPane.QUESTION_MESSAGE);
                    }
                    else if($propertyType.equals(Class.forName("java.io.File")))
                    {
                        JFileChooser fileChooser = new JFileChooser();
                        
                        int result = fileChooser.showOpenDialog($gui);
                        
                        switch(result)
                        {
                        case JFileChooser.CANCEL_OPTION:
                            break;
                        case JFileChooser.ERROR_OPTION:
                            break;
                        case JFileChooser.APPROVE_OPTION:
                            
                            $propertyResult = new File(fileChooser.getSelectedFile().getAbsolutePath());
                            
                            break;
                        }
                    }
                    else if($propertyType.equals(Class.forName("java.lang.reflect.Method")))
                    {
                       $propertyResult = $propertyValue;
                    }
                }
                catch (HeadlessException e2) {
                    // TODO Auto-generated catch block
                    e2.printStackTrace();
                }
                catch (ClassNotFoundException e2) {
                    // TODO Auto-generated catch block
                    e2.printStackTrace();
                }
                if($propertyResult == null)
                {
                    return;
                }
           
                try {
                    SystemRoot.getSystemInstance().getSystemModuleRegistry().getRunningModule($moduleId).setProperty($propertyName,$propertyResult);
                }
                catch (ModulePropertyException e1) {
                    JOptionPane.showMessageDialog($gui,e1.getMessage(),"Error setting property",JOptionPane.ERROR_MESSAGE);
                }
                catch (ModuleInstanceException e2) {
                    // TODO Auto-generated catch block
                    e2.printStackTrace();
                }
            
           
        }
        
          
    }

    private static class ModulePopupMenu extends JPopupMenu
    {
        public ModulePopupMenu()
        {
            super();
         
        }
        
       
    }
    
    private static class EdgePopupMenu extends JPopupMenu
    {
        public EdgePopupMenu()
        {
            super();
        }
    }
    
    /**
     * @param menu3
     * @param id
     */
    public void populateModuleMenu(JMenu menu, int id)
    {
        if(id == -1) return;
        
        menu.removeAll();
        
        menu.add(mniModuleStart);
        
        menu.add(mniModuleStop);
        
        menu.addSeparator();
        
        menu.add(mniModuleConnectTo);
        
        menu.add(mniModuleRemove);
                
        ModuleProperty[] moduleProperties = null;
        
            String moduleId = null;
            try {
                moduleId = SystemRoot.getSystemInstance().getSystemModuleRegistry().getRunningModule(id).getClassId();
            }
            catch (ModuleInstanceException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        
            ModuleDescriptor moduleDescriptor = null;
            try {
                moduleDescriptor = SystemRoot.getSystemInstance().getSystemModuleRegistry().getModuleDescriptor(moduleId);
            }
            catch (ModuleClassException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
            moduleProperties = moduleDescriptor.getProperties();
       
        
        if(moduleProperties != null)
        {
                for (int i=0;i<moduleProperties.length;i++)
	            {
	                String propertyName = moduleProperties[i].getName();
	                
	                Class propertyType = moduleProperties[i].getType();
	            
                    Object propertyValue = moduleProperties[i].getValue();
	                
                        JMenuItem menuItem = new JMenuItem(propertyName);
	                  
	                    menuItem.addActionListener(new PropertyActionAdapter($gui,id,propertyType,propertyName,propertyValue));                    
	                    
	                    menu.add(menuItem);
	                
	                
	             
	            }
	        
        }
        
        menu.add(mniModuleDetails);
     
    }

    /**
     * @param parentId
     * @param childId
     */

}

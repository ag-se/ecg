/*
 * Created on 05.04.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.electrocodeogram.ui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import org.electrocodeogram.core.Core;
import org.electrocodeogram.module.Module;
import org.electrocodeogram.module.ModuleConnectionException;
import org.electrocodeogram.module.Module.ModuleType;
import org.electrocodeogram.module.intermediate.EventProcessor;
import org.electrocodeogram.module.registry.IllegalModuleIDException;
import org.electrocodeogram.module.registry.UnknownModuleIDException;


/**
 * @author 7oas7er
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MenuManager
{

    private static MenuManager theInstance = new MenuManager();
 
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
    
    private MenuManager()
    {
        mniModuleRemove.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent e)
            {
                try {
                	Core.getInstance().getModuleRegistry().getModuleInstance(Core.getInstance().getGui().getSelectedModuleCellId()).remove();
                }
                catch (UnknownModuleIDException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                catch (IllegalModuleIDException e2) {
                    // TODO Auto-generated catch block
                    e2.printStackTrace();
                }
                
            }});
        
        mniModuleDetails.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent e)
            {
            	Core.getInstance().getGui().showModuleDetails();                
            }});
      
        mniModuleStop.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent e)
            {
               try {
            	   Core.getInstance().getModuleRegistry().getModuleInstance(Core.getInstance().getGui().getSelectedModuleCellId()).deactivate();
            }
            catch (IllegalModuleIDException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            catch (UnknownModuleIDException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
                
            }});
        
        mniModuleStart.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent e)
            {
               try {
            	   Core.getInstance().getModuleRegistry().getModuleInstance(Core.getInstance().getGui().getSelectedModuleCellId()).activate();
            }
            catch (IllegalModuleIDException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            catch (UnknownModuleIDException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
                
            }});
        
        mniMsgWindowShow.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent e)
            {
            	Core.getInstance().getGui().showMessagesWindow();
                
            }});
        
        mniMakeAnnotator.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent e)
            {
                Module module = null;
                try {
                	Core.getInstance().getModuleRegistry().getModuleInstance(Core.getInstance().getGui().getSelectedModuleCellId());
                }
                catch (IllegalModuleIDException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                catch (UnknownModuleIDException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                if (module instanceof EventProcessor)
                {
                    EventProcessor eventProcessor = (EventProcessor) module;
                    
                    eventProcessor.setProcessingMode(EventProcessor.ProcessingMode.ANNOTATOR);

                }
                
            }});
    
        mniModuleConnectTo.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent e)
            {
            	Core.getInstance().getGui().enterModuleConnectionMode(Core.getInstance().getGui().getSelectedModuleCellId());
                
            }});
        
        mniMakeFilter.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent e)
            {
                Module module = null;
                try {
                	Core.getInstance().getModuleRegistry().getModuleInstance(Core.getInstance().getGui().getSelectedModuleCellId());
                }
                catch (IllegalModuleIDException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                catch (UnknownModuleIDException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                if (module instanceof EventProcessor)
                {
                    EventProcessor eventProcessor = (EventProcessor) module;
                    
                    eventProcessor.setProcessingMode(EventProcessor.ProcessingMode.FILTER);

                }
                
            }});
        
    }

    public static MenuManager getInstance()
    {
        return theInstance;
    }
    
    public void showModuleMenu(int id, Component c, int x, int y)
    {
        modulePopupMenu = new ModulePopupMenu();
        
        modulePopupMenu.add(mniModuleStart);
        
        modulePopupMenu.add(mniModuleStop);
        
        modulePopupMenu.addSeparator();
        
        try {
            if(!Core.getInstance().getModuleRegistry().getModuleInstance(id).isModuleType(ModuleType.TARGET_MODULE))
            {
                modulePopupMenu.add(mniModuleConnectTo);
            }
        }
        catch (IllegalModuleIDException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        catch (UnknownModuleIDException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
                
        modulePopupMenu.add(mniModuleRemove);
        
        modulePopupMenu.addSeparator();
        
        modulePopupMenu.add(mniMsgWindowShow);
        
        
        
        try {
            if(Core.getInstance().getModuleRegistry().getModuleInstance(id).isModuleType(ModuleType.INTERMEDIATE_MODULE))
            {
                modulePopupMenu.addSeparator();
                
                modulePopupMenu.add(mniMakeAnnotator);
                
                modulePopupMenu.add(mniMakeFilter);
            }
        }
        catch (IllegalModuleIDException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        catch (UnknownModuleIDException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        
        Properties moduleProperties = null;
        try {
            moduleProperties = Core.getInstance().getModuleRegistry().getModulePropertiesForId(id);
        }
        catch (IllegalModuleIDException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        catch (UnknownModuleIDException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        
        if(moduleProperties != null)
        {
        
	        boolean propertiesDeclared = Boolean.valueOf(moduleProperties.getProperty("PROPERTIES_DECLARED")).booleanValue();
	        
	        //TODO : make constants od property names 
	        
	        if (propertiesDeclared)
	        {
	            int count = Integer.parseInt(moduleProperties.getProperty("PROPERTIES_COUNT"));
	            
	            for (int i=1;i<=count;i++)
	            {
	                String propertyDisplayName = moduleProperties.getProperty("MODULE_PROPERTY_DISPLAYNAME_" + i);
	                
	                String propetyType = moduleProperties.getProperty("MODULE_PROPERTY_TYPE_" + i);
	            
	                String propertyName = moduleProperties.getProperty("MODULE_PROPERTY_NAME_" + i);
	                
	                try {
	                    Class clazz = Class.forName(propetyType);
	                    
	                    JMenuItem menuItem = new JMenuItem(propertyDisplayName);
	                  
	                    menuItem.addActionListener(new PropertyActionAdapter(id,clazz,propertyName));                    
	                    
	                    modulePopupMenu.add(menuItem);
	                }
	                catch (ClassNotFoundException e) {
	                    // TODO Auto-generated catch block
	                    e.printStackTrace();
	                }
	             
	            }
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
    
    private class EdgeRemoveAdapter implements ActionListener
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
            	Core.getInstance().getModuleRegistry().getModuleInstance(parentId).connectReceiverModule(Core.getInstance().getModuleRegistry().getModuleInstance(childId));
            }
            catch (UnknownModuleIDException e1) {
              
                // only occurs because this event is fired twice internaly
            }
            catch (ModuleConnectionException e2) {
                // TODO Auto-generated catch block
                e2.printStackTrace();
            }
            catch (IllegalModuleIDException e3) {
                // TODO Auto-generated catch block
                e3.printStackTrace();
            }
            
        }
        
    }
    
    private class PropertyActionAdapter implements ActionListener
    {

        private int moduleId = -1;
        
        private String propertyName = "";
        
        private Object propertyValue = null;
        
        private Class propertyType = null;
        
        public PropertyActionAdapter(int moduleId, Class propertyType, String propertyName)
        {
            
            this.moduleId = moduleId;
            
            this.propertyName = propertyName;
            
            this.propertyType = propertyType;
            
        }
        
        public void actionPerformed(ActionEvent e)
        {
            try {
             
                if(propertyType.equals(Class.forName("java.lang.String")))
                {
                   propertyValue = JOptionPane.showInputDialog(Core.getInstance().getGui(),"Geben Sie den neuen Wert ein","",JOptionPane.QUESTION_MESSAGE);
                }
                else if(propertyType.equals(Class.forName("java.io.File")))
                {
                    JFileChooser fileChooser = new JFileChooser();
                    
                    int result = fileChooser.showOpenDialog(Core.getInstance().getGui());
                    
                    switch(result)
                    {
                    case JFileChooser.CANCEL_OPTION:
                        break;
                    case JFileChooser.ERROR_OPTION:
                        break;
                    case JFileChooser.APPROVE_OPTION:
                        
                        propertyValue = fileChooser.getSelectedFile().getAbsolutePath();
                        
                        break;
                    }
                }
           
                //assert(propertyValue != null);
                
                Core.getInstance().getModuleRegistry().getModuleInstance(moduleId).setProperty(propertyName,propertyValue);
            
            }
            catch (ClassNotFoundException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            catch (IllegalModuleIDException e2) {
                // TODO Auto-generated catch block
                e2.printStackTrace();
            }
            catch (UnknownModuleIDException e3) {
                // TODO Auto-generated catch block
                e3.printStackTrace();
            }
        }
        
          
    }

    private class ModulePopupMenu extends JPopupMenu
    {
        public ModulePopupMenu()
        {
            super();
         
        }
        
       
    }
    
    private class EdgePopupMenu extends JPopupMenu
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
                
        Properties moduleProperties = null;
        try {
            moduleProperties = Core.getInstance().getModuleRegistry().getModulePropertiesForId(id);
        }
        catch (IllegalModuleIDException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        catch (UnknownModuleIDException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        
        if(moduleProperties != null)
        {
        
	        boolean propertiesDeclared = Boolean.valueOf(moduleProperties.getProperty("PROPERTIES_DECLARED")).booleanValue();
	        
	        if (propertiesDeclared)
	        {
	            int count = Integer.parseInt(moduleProperties.getProperty("PROPERTIES_COUNT"));
	            
	            for (int i=1;i<=count;i++)
	            {
	                String propertyDisplayName = moduleProperties.getProperty("MODULE_PROPERTY_DISPLAYNAME_" + i);
	                
	                String propetyType = moduleProperties.getProperty("MODULE_PROPERTY_TYPE_" + i);
	            
	                String propertyName = moduleProperties.getProperty("MODULE_PROPERTY_NAME_" + i);
	                
	                try {
	                    Class clazz = Class.forName(propetyType);
	                    
	                    JMenuItem menuItem = new JMenuItem(propertyDisplayName);
	                  
	                    menuItem.addActionListener(new PropertyActionAdapter(id,clazz,propertyName));                    
	                    
	                    menu.add(menuItem);
	                }
	                catch (ClassNotFoundException e) {
	                    // TODO Auto-generated catch block
	                    e.printStackTrace();
	                }
	             
	            }
	        }
        }
        
        menu.add(mniModuleDetails);
     
    }

    /**
     * @param parentId
     * @param childId
     */

}

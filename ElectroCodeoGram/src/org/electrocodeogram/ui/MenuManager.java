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

import org.electrocodeogram.module.ModuleRegistry;
import org.electrocodeogram.module.Module;
import org.electrocodeogram.module.annotator.EventProcessor;


/**
 * @author 7oas7er
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MenuManager
{

    private static MenuManager theInstance = new MenuManager();
 
    private ModulePopupMenu popupMenu = null;
    
    private JMenuItem mniModuleDetails = new JMenuItem("Eigenschaften");
    
    private JMenuItem mniModuleRemove = new JMenuItem("Entfernen");
    
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
                ModuleRegistry.getInstance().removeModule(Configurator.getInstance().getSelectedModuleCellId());
                
            }});
        
        mniModuleDetails.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent e)
            {
                Configurator.getInstance().showModuleDetails();                
            }});
      
        mniModuleStop.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent e)
            {
               ModuleRegistry.getInstance().stopModule(Configurator.getInstance().getSelectedModuleCellId());
                
            }});
        
        mniModuleStart.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent e)
            {
               ModuleRegistry.getInstance().startModule(Configurator.getInstance().getSelectedModuleCellId());
                
            }});
        
        mniMsgWindowShow.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent e)
            {
                Configurator.getInstance().showMessagesWindow();
                
            }});
        
        mniMakeAnnotator.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent e)
            {
                ModuleRegistry.getInstance().setProcessorMode(EventProcessor.ANNOTATOR,Configurator.getInstance().getSelectedModuleCellId());
                
            }});
    
        mniMakeFilter.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent e)
            {
                ModuleRegistry.getInstance().setProcessorMode(EventProcessor.FILTER,Configurator.getInstance().getSelectedModuleCellId());
                
            }});

    }

    public static MenuManager getInstance()
    {
        return theInstance;
    }
    
    public void showModuleMenu(int id, Component c, int x, int y)
    {
        popupMenu = new ModulePopupMenu();
        
        popupMenu.add(mniModuleStart);
        
        popupMenu.add(mniModuleStop);
        
        popupMenu.addSeparator();
        
        popupMenu.add(mniModuleRemove);
        
        popupMenu.addSeparator();
        
        popupMenu.add(mniMsgWindowShow);
        
        if(ModuleRegistry.getInstance().isModuleType(Module.INTERMEDIATE_MODULE,id))
        {
            popupMenu.addSeparator();
            
            popupMenu.add(mniMakeAnnotator);
            
            popupMenu.add(mniMakeFilter);
        }
        
        Properties moduleProperties = ModuleRegistry.getInstance().getModulePropertiesForId(id);
        
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
	                    
	                    popupMenu.add(menuItem);
	                }
	                catch (ClassNotFoundException e) {
	                    // TODO Auto-generated catch block
	                    e.printStackTrace();
	                }
	             
	            }
	        }
        }
        
        popupMenu.addSeparator();
        
        popupMenu.add(mniModuleDetails);
        
        popupMenu.show(c,x,y);
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
                   propertyValue = JOptionPane.showInputDialog(Configurator.getInstance(),"Geben Sie den neuen Wert ein","",JOptionPane.QUESTION_MESSAGE);
                }
                else if(propertyType.equals(Class.forName("java.io.File")))
                {
                    JFileChooser fileChooser = new JFileChooser();
                    
                    int result = fileChooser.showOpenDialog(Configurator.getInstance());
                    
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
                
                ModuleRegistry.getInstance().setModuleProperty(moduleId,propertyName,propertyValue);
            
            }
            catch (ClassNotFoundException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
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
        
        menu.add(mniModuleRemove);
                
        Properties moduleProperties = ModuleRegistry.getInstance().getModulePropertiesForId(id);
        
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
}

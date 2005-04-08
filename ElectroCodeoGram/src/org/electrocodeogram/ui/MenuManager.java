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
import java.util.ArrayList;
import java.util.Properties;

import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import org.electrocodeogram.module.Module;
import org.electrocodeogram.module.ModuleRegistry;

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
    
    
    
    /**
     * 
     */
    private MenuManager()
    {
        
    }

    public static MenuManager getInstance()
    {
        return theInstance;
    }
    
    public void showModuleMenu(int id, Component c, int x, int y)
    {
        popupMenu = new ModulePopupMenu(id);
        
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
        
        private int moduleId = -1;
        
        public ModulePopupMenu(int moduleId)
        {
            super();
            
            this.moduleId = moduleId;
            
            JMenuItem mniModuleDetails = new JMenuItem("Details");
            mniModuleDetails.addActionListener(new ActionListener(){

                public void actionPerformed(ActionEvent e)
                {
                    Configurator.getInstance().showModuleDetails();                
                }});
            
            this.add(mniModuleDetails);
            
            JMenuItem mniModuleStop = new JMenuItem("Stop");
            mniModuleStop.addActionListener(new ActionListener(){

                public void actionPerformed(ActionEvent e)
                {
                   ModuleRegistry.getInstance().stopModule(getModuleId());
                    
                }});
            this.add(mniModuleStop);           
            
            JMenuItem mniModuleStart = new JMenuItem("Start");
            mniModuleStart.addActionListener(new ActionListener(){

                public void actionPerformed(ActionEvent e)
                {
                   ModuleRegistry.getInstance().startModule(getModuleId());
                    
                }});
            this.add(mniModuleStart);
            
        }
        
        public int getModuleId()
        {
            return moduleId;
        }
        
    }
}

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

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/**
 * @author 7oas7er
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MenuManager
{

    private static MenuManager theInstance = new MenuManager();
 
    private JPopupMenu popupMenu = null;
    
    private ArrayList commonItems = null;
    
    /**
     * 
     */
    private MenuManager()
    {
        commonItems = new ArrayList();
        
        popupMenu = new JPopupMenu();
        
        JMenuItem mniModuleDetails = new JMenuItem("Details");
        mniModuleDetails.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent e)
            {
                Configurator.getInstance().showModuleDetails();                
            }});
        
        commonItems.add(mniModuleDetails);
        
    }

    public static MenuManager getInstance()
    {
        return theInstance;
    }
    
    public void showModuleMenu(Component c, int x, int y)
    {
        for(int i=0;i<commonItems.size();i++)
        {
            popupMenu.add((JMenuItem)commonItems.get(i));
        }
        popupMenu.show(c,x,y);
    }
}

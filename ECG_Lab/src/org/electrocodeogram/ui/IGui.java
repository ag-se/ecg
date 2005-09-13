/**
 * 
 */
package org.electrocodeogram.ui;

import java.util.Observer;

import javax.swing.JFrame;

/**
 *
 */
public interface IGui extends Observer
{

    public JFrame getRootFrame();
    
    public int getSelectedModuleCellId();

    /**
     * 
     */
    public void showModuleDetails();

    /**
     * 
     */
    public void showMessagesWindow();

    /**
     * @param selectedModuleCellId
     */
    public void enterModuleConnectionMode(int selectedModuleCellId);

   
    /**
     * 
     */
    public void exitModuleConnectionMode();

    /**
     * @return
     */
    public boolean getModuleConnectionMode();

    /**
     * @return
     */
    public int getSourceModule();
    
    public MenuManager getMenuManager();
}

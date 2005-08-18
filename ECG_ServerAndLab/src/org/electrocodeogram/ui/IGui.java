/**
 * 
 */
package org.electrocodeogram.ui;

import java.util.Observer;

import org.electrocodeogram.ui.messages.IGuiWriter;

/**
 *
 */
public interface IGui extends Observer
{

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
     * @return
     */
    public IGuiWriter getGuiEventWriter();

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

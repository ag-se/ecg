/**
 * 
 */
package org.electrocodeogram.ui;

import java.util.Observer;

import javax.swing.JFrame;

import org.electrocodeogram.ui.messages.GuiWriter;
import org.electrocodeogram.ui.messages.IGuiWriter;

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
     * @return
     */
    public GuiWriter getGuiEventWriter();

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

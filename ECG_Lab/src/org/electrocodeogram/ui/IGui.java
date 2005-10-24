package org.electrocodeogram.ui;

import java.util.Observer;


public interface IGui extends Observer
{

	 /**
     * 
     */
    public abstract void showModuleDetails();

    /**
     * 
     */
    public abstract void showMessagesWindow();

    /**
     * @param selectedModuleCellId
     */
    public abstract void enterModuleConnectionMode(int selectedModuleCellId);

   
    /**
     * 
     */
    public abstract void exitModuleConnectionMode();

    /**
     * @return
     */
    public abstract boolean getModuleConnectionMode();

    /**
     * @return
     */
    public abstract int getSourceModule();

	/**
	 * 
	 */
	public abstract void showModuleFinderDetails();
   

}

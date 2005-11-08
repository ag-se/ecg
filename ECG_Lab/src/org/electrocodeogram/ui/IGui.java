/*
 * Class: IGui
 * Version: 1.0
 * Date: 16.10.2005
 * By: Frank@Schlesinger.com
 */

package org.electrocodeogram.ui;

import java.util.Observer;

public interface IGui extends Observer {

    /**
     * 
     */
    public abstract void showModuleDetails();

    /**
     * 
     */
    public abstract void showMessagesWindow();

    /**
     * @param id
     */
    public abstract void enterModuleConnectionMode(int id);

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

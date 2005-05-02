/*
 * Created on 11.03.2005
 *
 */
package org.electrocodeogram.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;

import org.electrocodeogram.module.ModuleConnectionException;
import org.electrocodeogram.module.ModuleRegistry;

/**
 * @author 7oas7er *  * TODO To change the template for this generated type comment go to * Window - Preferences - Java - Code Style - Code Templates
 */

public class ActionAdapter implements ActionListener
{

    
    private String moduleName = null;
    /**
     * @param configurator
     * @param selectedModuleCellId
     * @param moduleName
     */
    public ActionAdapter(Configurator configurator, String moduleName)
    {
        
        this.moduleName = moduleName;
    }

    public void actionPerformed(ActionEvent e)
    {

      int selectedModuleCellId = Configurator.getInstance().getSelectedModuleCellId();
        
      if(selectedModuleCellId != -1)
      {
          try
          {
              ModuleRegistry.getInstance().connectNewModuleInstance(selectedModuleCellId,moduleName);
          }
          catch(ModuleConnectionException er)
          {
              JOptionPane.showMessageDialog(Configurator.getInstance(),er.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);  
          }
       
      }
        
    }

}

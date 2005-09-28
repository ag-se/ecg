/*
 * Created on 11.03.2005
 *
 */
package org.electrocodeogram.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


import org.electrocodeogram.module.registry.ModuleClassException;
import org.electrocodeogram.module.registry.ModuleInstantiationException;
import org.electrocodeogram.system.SystemRoot;


/**
 * @author 7oas7er *  * TODO To change the template for this generated type comment go to * Window - Preferences - Java - Code Style - Code Templates
 */

public class ActionAdapter implements ActionListener
{

    
    private String $moduleName;
    
    private String moduleClassId;
    
  
    public ActionAdapter(String moduleClassId, String moduleName)
    {
        
        this.$moduleName = moduleName;
        
        this.moduleClassId = moduleClassId;
    }

    public void actionPerformed(ActionEvent e)
    {

//      int selectedModuleCellId = Configurator.getInstance().getSelectedModuleCellId();
//        
//      if(selectedModuleCellId != -1)
//      {
//          try
//          {
//              ModuleRegistry.getInstance().connectNewModuleInstance(selectedModuleCellId,moduleName);
//          }
//          catch(ModuleConnectionException er)
//          {
//              JOptionPane.showMessageDialog(Configurator.getInstance(),er.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);  
//          }
//       
//      }
        
        
        	try {
                SystemRoot.getSystemInstance().getSystemModuleRegistry().createRunningModule(moduleClassId,$moduleName);
            }
            catch (ModuleInstantiationException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            catch (ModuleClassException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        
  
        
    }

}

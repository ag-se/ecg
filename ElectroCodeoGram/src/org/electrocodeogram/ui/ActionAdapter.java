/*
 * Created on 11.03.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.electrocodeogram.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;

import org.electrocodeogram.module.Module;
import org.electrocodeogram.module.ModuleConnectionException;

/**
 * @author 7oas7er *  * TODO To change the template for this generated type comment go to * Window - Preferences - Java - Code Style - Code Templates
 */

public class ActionAdapter implements ActionListener
{

    /**
     * 
     * @uml.property name="root"
     * @uml.associationEnd multiplicity="(0 1)"
     */
    private Configurator root = null;

    
    private Class moduleClass = null;
    /**
     * @param configurator
     * @param selectedModuleCellId
     * @param moduleClass
     */
    public ActionAdapter(Configurator configurator, Class moduleClass)
    {
        
        this.root = configurator;
     
        this.moduleClass = moduleClass;
    }

    public void actionPerformed(ActionEvent e)
    {

      int selectedModuleCellId = root.getModuleGraph().getSelectedModuleCellId();
        
      if(selectedModuleCellId != -1)
      {
          try
          {
              root.getModule(selectedModuleCellId).connectModule((Module) moduleClass.newInstance());
          }
          catch(ModuleConnectionException er)
          {
              JOptionPane.showMessageDialog(root,er.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);  
          }
        catch (InstantiationException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        catch (IllegalAccessException e2) {
            // TODO Auto-generated catch block
            e2.printStackTrace();
        }
      }
        
    }

}

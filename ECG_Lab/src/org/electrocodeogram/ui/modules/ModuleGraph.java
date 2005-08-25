/*
 * Created on 10.03.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.electrocodeogram.ui.modules;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Observable;

import javax.swing.JOptionPane;
import javax.swing.border.LineBorder;

import org.electrocodeogram.module.Module;
import org.electrocodeogram.module.ModuleConnectionException;

import org.electrocodeogram.module.registry.ModuleInstanceException;
import org.electrocodeogram.module.registry.ModuleRegistry;

import org.electrocodeogram.msdt.validation.EventValidator;
import org.electrocodeogram.system.SystemRoot;
import org.electrocodeogram.ui.Gui;
import org.electrocodeogram.ui.MenuManager;
import org.jgraph.JGraph;
import org.jgraph.event.GraphSelectionEvent;
import org.jgraph.event.GraphSelectionListener;

import org.jgraph.graph.DefaultGraphModel;

import org.jgraph.graph.GraphConstants;

/**
 * @author 7oas7er
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ModuleGraph extends JGraph
{

    private Gui configurator = null;

    private HashMap moduleCells = null;
    
    private int selectedModuleCellId = -1;
    
    private ModuleCell rootCell = null;
    
    private ModuleGraphObserverDummy observerDummy = null;
    
    private ModuleGraph me = null;
    
    public ModuleGraph(Gui configurator){
        
        super(new DefaultGraphModel());
      
        me = this;
        
        this.configurator = configurator;
         
        observerDummy = new ModuleGraphObserverDummy(configurator,this);
        
        moduleCells = new HashMap();
        
        addGraphSelectionListener(new GraphSelectionListener() {

            public void valueChanged(GraphSelectionEvent arg0)
            {
                if(arg0.isAddedCell() && (arg0.getCell() instanceof ModuleCell))
                {   
                    
                    selectedModuleCellId  = ((ModuleCell)(arg0.getCell())).getId();
                    
                    observerDummy.notifyUI();
                }
                else
                {
                    selectedModuleCellId = -1;
                    
                    observerDummy.notifyUI();
                }
                
            }});
           
        this.setBorder(new LineBorder(Color.GRAY));
        
        addMouseListener(new MouseAdapter(){
            
            public void mouseClicked(MouseEvent e)
            {
                if(SystemRoot.getSystemInstance().getGui().getModuleConnectionMode())
                {
                    if(e.getButton() == MouseEvent.BUTTON1)
                    {
                        Object o = getFirstCellForLocation(e.getPoint().x,e.getPoint().y);
		                if(o != null)
		                {
		                    if (o instanceof ModuleCell)
		                    {
			                    ModuleCell mc = (ModuleCell) o;
			                    
			                    selectedModuleCellId = mc.getId();
			                    
			                    if(selectedModuleCellId == SystemRoot.getSystemInstance().getGui().getSourceModule())
			                    {
			                        JOptionPane.showMessageDialog(getGui(),"Sie k�nnen ein Modul nicht mit sich selbst verbinden.", "Ung�ltige Modulverbindung",JOptionPane.ERROR_MESSAGE);
			                    }
			                    else
			                    {
			                        try {
                                        
			                           
			                            	SystemRoot.getSystemInstance().getSystemModuleRegistry().getRunningModule(SystemRoot.getSystemInstance().getGui().getSourceModule()).connectReceiverModule(SystemRoot.getSystemInstance().getSystemModuleRegistry().getRunningModule(selectedModuleCellId));
                                        
                                        
                                        SystemRoot.getSystemInstance().getGui().exitModuleConnectionMode();
                                    }
                                    catch (ModuleConnectionException e1) {
                                        
                                        JOptionPane.showMessageDialog(getGui(),e1.getMessage(), "Ung�ltige Modulverbindung",JOptionPane.ERROR_MESSAGE);
                                    }
                                    catch (ModuleInstanceException e2) {
                                        // TODO Auto-generated catch block
                                        e2.printStackTrace();
                                    }
			                    }
			                }
		                    
		                }
                    }
                    else if(e.getButton() == MouseEvent.BUTTON3)
                    {
                    	SystemRoot.getSystemInstance().getGui().exitModuleConnectionMode();
                    }
                    
                    
                }
                else
                {
	                if(e.getButton() == MouseEvent.BUTTON3)
	                {
		                Object o = getFirstCellForLocation(e.getPoint().x,e.getPoint().y);
		                if(o != null)
		                {
		                    if (o instanceof ModuleCell)
		                    {
			                    ModuleCell mc = (ModuleCell) o;
			                    
			                    selectedModuleCellId = mc.getId();
			                    
                                
                                
                               
                                    
                                    getGui().getMenuManager().showModuleMenu(selectedModuleCellId,me,e.getPoint().x,e.getPoint().y);
                              
                                
			                    
			                }
		                    else if (o instanceof ModuleEdge)
		                    {
		                        ModuleEdge edge = (ModuleEdge) o;
		                        
                                getGui().getMenuManager().showEdgeMenu(edge.getParentId(),edge.getChildId(),me,e.getPoint().x,e.getPoint().y);
		                    }
		                    
		                }
	                }
                }
            }

            });
    }

    /**
     * 
     * @uml.property name="selectedModuleCellId"
     */
    public int getSelectedModuleCellId() {
        return selectedModuleCellId;
    }

    private Gui getGui()
    {
        return this.configurator;
    }
          
    public void addModuleCell(ModuleCell cell)
    {
//        if (rootCell == null)
//        {
//            rootCell = cell;
//        }

        moduleCells.put(new Integer(cell.getId()),cell);
        
        this.getGraphLayoutCache().insert(cell);
    }

    public void updateModuleCell(int id, Module module)
    {
        if(containsModuleCell(id))
        {
            
            ModuleCell moduleCell = (ModuleCell) moduleCells.get(new Integer(id));
            
            GraphConstants.setOpaque(moduleCell.getAttributes(),module.isActive());
            
            this.getGraphLayoutCache().insert(moduleCell);

            Object[] modules = module.getReceivingModules();
            
            Object[] edges = moduleCell.getChildEdges();
            
            if(edges.length > 0)
            {
                getGraphLayoutCache().remove(edges);
            }
            
            if(modules != null)
            {
                for(int i=0;i<modules.length;i++)
                {
                    Module childModule = (Module) modules[i];
                    
                    ModuleCell childModuleCell = (ModuleCell) moduleCells.get(new Integer(childModule.getId()));
                    
                    ModuleEdge edge = new ModuleEdge(moduleCell.getId(),childModuleCell.getId());
                            
                    edge.setSource(moduleCell.getChildAt(0));
                            
                    edge.setTarget(childModuleCell.getChildAt(0));
                            
                    addChildEdge(moduleCell,edge);
                            
                }
            }
//
//            
        }
    }
    
    public boolean containsModuleCell(int id)
    {
        boolean flag = moduleCells.containsKey(new Integer(id));
        
        return flag;
    }
    
    public void removeModuleCell(int id)
    {
        if(containsModuleCell(id))
        {
            ModuleCell cell = (ModuleCell) moduleCells.get(new Integer(id));
            
            Object[] o = new Object[]{cell};
            
            Object[] edges = cell.getChildEdges();
            
            if(edges.length > 0)
            {
                getGraphLayoutCache().remove(edges);
            }
            
            getGraphLayoutCache().remove(o);
            
            moduleCells.remove(new Integer(id));
        }
    }
    /**
     * @param moduleCell
     * @param edge
     * @param childModuleCell
     * @param parentModuleCell
     */
    public void addChildEdge(ModuleCell moduleCell, ModuleEdge edge)
    {
       
        moduleCell.addChildEdge(edge);
        
        this.getGraphLayoutCache().insert(edge);
        
    }
    
    private static class ModuleGraphObserverDummy extends Observable
    {
        private ModuleGraph parent = null;
        
        public ModuleGraphObserverDummy(Gui configurator, ModuleGraph parent)
        {
            super();
            
            this.parent = parent;
            
            this.addObserver(configurator);
            
        }
        
        public void notifyUI()
        {
            setChanged();
            notifyObservers(parent);
            clearChanged();
        }
    }

    
}
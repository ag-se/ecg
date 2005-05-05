/*
 * Created on 10.03.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.electrocodeogram.ui;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Observable;

import javax.swing.JOptionPane;
import javax.swing.border.LineBorder;

import org.electrocodeogram.module.Module;
import org.electrocodeogram.module.ModuleConnectionException;
import org.electrocodeogram.module.ModuleRegistry;
import org.jgraph.JGraph;
import org.jgraph.event.GraphSelectionEvent;
import org.jgraph.event.GraphSelectionListener;
import org.jgraph.graph.DefaultEdge;
import org.jgraph.graph.DefaultGraphModel;
import org.jgraph.graph.Edge;
import org.jgraph.graph.GraphConstants;

/**
 * @author 7oas7er
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ModuleGraph extends JGraph
{

    private Configurator configurator = null;

    private HashMap moduleCells = null;
    
    private int selectedModuleCellId = -1;
    
    private ModuleCell rootCell = null;
    
    private ModuleGraphObserverDummy observerDummy = null;
    
    private ModuleGraph me = null;
    
    public ModuleGraph(Configurator configurator){
        
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
                if(Configurator.getInstance().getModuleConnectionMode())
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
			                    
			                    if(selectedModuleCellId == Configurator.getInstance().getSourceModule())
			                    {
			                        JOptionPane.showMessageDialog(Configurator.getInstance(),"Sie können ein Modul nicht mit sich selbst verbinden.", "Ungültige Modulverbindung",JOptionPane.ERROR_MESSAGE);
			                    }
			                    else
			                    {
			                        try {
                                        
			                            ModuleRegistry.getInstance().connectModule(Configurator.getInstance().getSourceModule(),selectedModuleCellId);
                                        
			                            Configurator.getInstance().exitModuleConnectionMode();
                                    }
                                    catch (ModuleConnectionException e1) {
                                        
                                        JOptionPane.showMessageDialog(Configurator.getInstance(),e1.getMessage(), "Ungültige Modulverbindung",JOptionPane.ERROR_MESSAGE);
                                    }
			                    }
			                }
		                }
                    }
                    else if(e.getButton() == MouseEvent.BUTTON3)
                    {
                        Configurator.getInstance().exitModuleConnectionMode();
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
			                    
			                    MenuManager.getInstance().showModuleMenu(selectedModuleCellId,me,e.getPoint().x,e.getPoint().y);
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
            
            GraphConstants.setOpaque(moduleCell.getAttributes(),module.isRunning());
            
            this.getGraphLayoutCache().insert(moduleCell);
//        
//            Point location = moduleCell.getLocation();
//            
//            Object[] parentEdges = moduleCell.getParentEdges();
//            
//            Object[] childEdges = moduleCell.getChildEdges();
//                                    
//            removeModuleCell(id);
//    
//            ModuleCell ml = new ModuleCell(module.getModuleType(),module.getId(), module.getName(), module.isRunning());
//            
//            ml.setLocation(location);
//            
//            for(int i=0;i<parentEdges.length;i++)
//            {
//                Edge edge = (Edge) parentEdges[i];
//                
//                edge.setTarget(ml.getChildAt(0));
//            }
//            
//            for(int i=0;i<childEdges.length;i++)
//            {
//                Edge edge = (Edge) childEdges[i];
//                
//                edge.setSource(ml.getChildAt(0));
//            }
//            
//            addModuleCell(ml);
//            
            Object[] modules = module.getChildModules();
            
            if(modules != null)
            {
                for(int i=0;i<modules.length;i++)
                {
                    Module childModule = (Module) modules[i];
                    
                    ModuleCell childModuleCell = (ModuleCell) moduleCells.get(new Integer(childModule.getId()));
                    
                    DefaultEdge edge = new DefaultEdge();
                    
                    GraphConstants.setLineEnd(edge.getAttributes(),

                    GraphConstants.ARROW_CLASSIC);
                            
                    GraphConstants.setDisconnectable(edge.getAttributes(), false);
                            
                    edge.setSource(moduleCell.getChildAt(0));
                            
                    edge.setTarget(childModuleCell.getChildAt(0));
                            
                    addEdge(edge);
                            
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
            
            getGraphLayoutCache().removeCells(o);
            
            moduleCells.remove(new Integer(id));
        }
    }
    /**
     * @param edge
     * @param childModuleCell
     * @param parentModuleCell
     */
    public void addEdge(DefaultEdge edge)
    {
       
        
        this.getGraphLayoutCache().insert(edge);
        
    }
    
    private class ModuleGraphObserverDummy extends Observable
    {
        private ModuleGraph parent = null;
        
        public ModuleGraphObserverDummy(Configurator configurator, ModuleGraph parent)
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

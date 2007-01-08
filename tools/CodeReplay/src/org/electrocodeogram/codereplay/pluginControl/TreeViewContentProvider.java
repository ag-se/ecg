package org.electrocodeogram.codereplay.pluginControl;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.electrocodeogram.codereplay.dataProvider.DataProvider;
import org.electrocodeogram.codereplay.dataProvider.Replay;

/*
 * The content provider class is responsible for
 * providing objects to the view. It can wrap
 * existing objects in adapters or simply return
 * objects as-is. These objects may be sensitive
 * to the current input of the view, or ignore
 * it and always show the same content 
 * (like Task List, for example).
 */
 
/**
 * Serves as content provider for the TreeViewer that shows the tree of all Replays.
 * For more info on content providers see {@link ElementListContentProvider} or eclipse 
 * manual.
 * 
 * @author marco kranz
 */
public class TreeViewContentProvider extends LabelProvider implements IStructuredContentProvider, ITreeContentProvider{
	
	
	// constructor
	public TreeViewContentProvider(){
	}
	
	
	/**
	 * @param obj 
	 * @return the internal identifier of the Replay contained in the given object(TreeNode)
	 */
	public String getIdentifier(Object obj){
		return DataProvider.getInstance().getTreeModel().getIdentifier(obj);
	}
	
	
	/**
	 * @param obj
	 * @return the Replay contained in the given object(TreeNode)
	 */
	public Replay getReplay(Object obj){
		return DataProvider.getInstance().getTreeModel().getReplay(obj);
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(Viewer v, Object oldInput, Object newInput) {
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
	 */
	public void dispose() {
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object parent) {
		return DataProvider.getInstance().getTreeModel().getRootChildren(parent);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
	 */
	public Object getParent(Object child) {
		return DataProvider.getInstance().getTreeModel().getParent(child);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object parent) {
		return DataProvider.getInstance().getTreeModel().getChildren(parent);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
	 */
	public boolean hasChildren(Object parent) {
		return DataProvider.getInstance().getTreeModel().hasChildren(parent);
	}

	// -------------------- overwrite methods of LabelProvider
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
	 */
	public String getText(Object obj) {
		return DataProvider.getInstance().getTreeModel().getDisplayText(obj);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
	 */
	public Image getImage(Object obj) {
		String imageKey = ISharedImages.IMG_OBJ_FILE;
		if(DataProvider.getInstance().getTreeModel().hasChildren(obj))
			imageKey = ISharedImages.IMG_OBJ_FOLDER;
		return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
	}
	
	/**
	 * @param identifier the internal identifier of the Replay that should be set as active
	 */
	public void setActiveReplay(String identifier){
		DataProvider.getInstance().setActiveReplay(identifier);
	}
}
package org.electrocodeogram.codereplay.pluginControl;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.electrocodeogram.codereplay.dataProvider.DataProvider;
import org.electrocodeogram.codereplay.dataProvider.ReplayElement;

/**
 * Used as content provider for the treeview representing the list of single ReplayElements. 
 * For infos on the concept of content providers in eclipse please... rtfm! :) (ok kidding)
 * It basically transfers the structure of the data one has in his datamodel into the structure
 * a specific eclipse widged needs to display the content.
 * In this special case this means we use a treeview to represent the ReplayElements, but in the
 * datamodel the ReplayElements are stored in a list rather than a tree(ok they're in fact stored in 
 * a tree-like structure, but thats not what we need :P). So what we do in the content provider is
 * transfering our list representation into a tree representation. Since the tree we need in this case
 * is only a flat tree of depth 1 with only root and leafs(what is really close to being a list), this 
 * is not much of a problem.
 * 
 * @author marco kranz
 */
public class ElementListContentProvider extends LabelProvider implements IStructuredContentProvider, ITreeContentProvider{

	// inner class, only used for identification of tree root
	private TreeRoot root = new TreeRoot();
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object inputElement) {
		if(!(inputElement instanceof ReplayElement) && DataProvider.getInstance().getActiveReplay() != null)
			return DataProvider.getInstance().getActiveReplay().getElements().toArray();
		else
			return new ReplayElement[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		//System.out.println("ElementListContentProvider.inputChanged: \n old input: " + oldInput +" \n new input: "+ newInput);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object parentElement) {
		if(parentElement instanceof TreeRoot && DataProvider.getInstance().getActiveReplay() != null)
			return DataProvider.getInstance().getActiveReplay().getElements().toArray();
		else
			return new ReplayElement[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
	 */
	public Object getParent(Object element) {
		if(element instanceof TreeRoot)
			return null;
		else
			return root;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
	 */
	public boolean hasChildren(Object element) {
		if(element instanceof TreeRoot)
			return true;
		else
			return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
	 */
	public String getText(Object obj) {
		if(obj instanceof ReplayElement){
			Date date = ((ReplayElement)obj).getTimestamp();
			String timestamp = new SimpleDateFormat("dd.MM.yy, HH:mm:ss").format(date);
			return timestamp;
		}
		else
			return "";
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
	 */
	public Image getImage(Object obj) {
		String imageKey = ISharedImages.IMG_OBJ_ELEMENT;
		//if(DataProvider.getDataProvider().getTreeModel().hasChildren(obj))
		//	imageKey = ISharedImages.IMG_OBJ_FOLDER;
		return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
	}
	
	
	/**
	 * @return the currently 'active' ReplayElement of the active Replay
	 */
	public ReplayElement getCurrentElement(){
		return DataProvider.getInstance().getActiveReplay().getCurrentElement();
	}
	
	
	private class TreeRoot{}

}

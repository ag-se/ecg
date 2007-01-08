package org.electrocodeogram.codereplay.dataProvider;

import java.util.ArrayList;

import org.eclipse.core.runtime.IAdaptable;

/**
 * Represents a single node in the {@link TreeModel}. A node can have a value, that is, a Replay, and a name
 * that is either the part of the path this node is representing(if the node has no Replay) or the name of 
 * the Replay. 
 * 
 * @author marco kranz
 */
public class TreeNode implements IAdaptable{
	
	private ArrayList children;
	private Replay rep;
	private String pathpart;
	private TreeNode parent = null;
	
	/**
	 * @param r the Replay
	 * @param path the part of the path this node represents(will be used as name)
	 */
	public TreeNode(Replay r, String path) {
		rep = r;
		pathpart = path;
		children = new ArrayList();
	}
	/**
	 * @param child the node to be added as a child
	 */
	public void addChild(TreeNode child) {
		children.add(child);
		child.setParent(this);
	}
	/**
	 * @param child the node to be removed from the list of children
	 */
	public void removeChild(TreeNode child) {
		children.remove(child);
		child.setParent(null);
	}
	/**
	 * @return array consisting of all children of this node
	 */
	public TreeNode[] getChildren(){
		if(hasChildren())
			return (TreeNode[])children.toArray(new TreeNode[children.size()]);
		return new TreeNode[0];
	}
	/**
	 * @return true if this node has children, false otherwise
	 */
	public boolean hasChildren() {
		return children.size()>0;
	}
	/**
	 * @return the name of this node, that is either the path part it represents or the name of the Replay
	 */
	public String getName() {
		return pathpart;
	}
	/**
	 * @return the identifier of this nodes Replay(null if no Replay exists)
	 */
	public String getIdentifier(){
		if(rep != null)
			return rep.getIdentifier();
		return null;
	}
	/**
	 * @param parent the node to be set as parent for this node
	 */
	public void setParent(TreeNode parent) {
		this.parent = parent;
	}
	/**
	 * @return the parent of this node
	 */
	public TreeNode getParent() {
		return parent;
	}
	/**
	 * @return String representation of this node
	 */
	public String toString() {
		return getName();
	}
	/**
	 * Basic implementation of IAdaptable, taken from an example(does nothing)
	 * 
	 * @return null
	 */
	public Object getAdapter(Class key) {
		return null;
	}
	/**
	 * @return the size of this nodes Replay or 0 if no Replay present
	 */
	public int getSize(){
		if(rep != null)
			return rep.getSize();
		else return 0;
	}
	/**
	 * @return this nodes Replay(null if no Replay exists)
	 */
	public Replay getReplay() {
		return rep;
	}
}
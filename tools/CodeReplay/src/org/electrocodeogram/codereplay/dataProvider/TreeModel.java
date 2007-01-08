package org.electrocodeogram.codereplay.dataProvider;

import java.util.Enumeration;


/**
 * This class creates the tree representation of all {@link Replay}s. In the GUI this can be seen on the
 * left side of the replay perspective.
 * It is a directory like tree build off the path information of each Replay.
 * 
 * info: the implementation is only tested for the current needs. that is trees with the same deph for all 
 * leafs. there is no guaranty that this will work with other input as described above.
 * values in nodes other than leafs are not supported.
 * 
 * @author marco kranz
 */
public class TreeModel{
	
	private TreeNode invisibleRoot;
	
	/**
	 * Creates a new TreeModel
	 */
	TreeModel(){
		invisibleRoot = new TreeNode(null, "");
	}
	
	/**
	 * Adds the replay as a node in the tree if its not already in.
	 * 
	 * @param rep replay to be added
	 */
	// tbh this has not been extensively tested...
	void addNode(Replay rep){
//		 if replay has only one element, it should not be shown
		if(!rep.hasEnoughElements())
			return;
		// otherwise insert it into tree if not already done...
		String[] path = rep.getPath();
		TreeNode parent = invisibleRoot;
		TreeNode child;
		TreeNode[] children;
		for(int i = 0; i < path.length; i++){				// iterate over length of path
			children = parent.getChildren();
			child = null;
			if(children.length == 0){								// no children
				child = new TreeNode(null, path[i]);
				parent.addChild(child);				
				parent = (parent.getChildren())[0];
			}
			else{													// children
				for(int j = 0; j < children.length; j++){
					if(children[j].getName().equals(path[i])){		// path found 
						child = children[j];
						break;
					}
				}
				if(child == null){										// path not found
					child = new TreeNode(null, path[i]);
					parent.addChild(child);
				}
				parent = child;	
			}
		}
		children = parent.getChildren();						// finally, add replay
		String identifier = rep.getIdentifier();
		for(int i = 0; i < children.length; i++){
			if(children[i].getIdentifier().equals(identifier)){		// if replay already in
				return;
			}
		}
		parent.addChild(new TreeNode(rep, rep.getName()));		// insert replay 
	}
	

	/**
	 * Builds up a new tree consisting of all {@link Replays} in the enumeration
	 * 
	 * @param replays an enumeration of replays
	 */
	void rebuildTree(Enumeration replays) {
		reset();
		while(replays.hasMoreElements()){
			addNode((Replay)replays.nextElement());
		}
	}
	
	// not tested!
	/*public void removeNode(Replay rep) {
		System.out.println("removeNode(): enter");
		TreeParent parent = invisibleRoot;
		String[] path = rep.getPath();
		TreeParent[] children;
		TreeParent[] moved_path = new TreeParent[path.length];
		int iterator = 0;
		for(int i = 0; i < path.length; i++){					// look up node to remove
			children = parent.getChildren();
			System.out.println("removeNode(): 1st for "+ i);
			for(int j = 0; j < children.length; j++){
				System.out.println("removeNode(): 2nd for "+ j);
				if(children[j].getPathPart().equals(path[i])){
					parent = children[j];
					moved_path[iterator++] = parent;
					System.out.println("removeNode(): 1st if reached!");
					break;
				}
			}
		}
		String id = rep.getIdentifier();
		children = parent.getChildren();
		for(int i = 0; i < children.length; i++){
			System.out.println("removeNode(): 3rd for "+ i);
			if((children[i].getIdentifier()).equals(id)){		// remove node
				parent.removeChild(children[i]);
				System.out.println("removeNode(): 2nd if reached!");
				break;
			}
		}
		for(int i = moved_path.length; i >= 0; i--){			// clean up path
			System.out.println("removeNode(): 4th for "+ i);
			if(!parent.hasChildren()){
				parent.getParent().removeChild(parent);
				System.out.println("removeNode(): 3rd if reached!");
			}
			else break;
		}
	}*/
	
	/**
	 * @return the tree root
	 */
	public TreeNode getRoot(){
		return invisibleRoot;
	}
	
	/**
	 * @param parent
	 * @return an array consisting of all children of the root node(can be of length 0)
	 */
	public Object[] getRootChildren(Object parent){
		return invisibleRoot.getChildren();
	}
	
	/**
	 * @param child
	 * @return parent of child
	 */
	public Object getParent(Object child) {
		return ((TreeNode)child).getParent();
	}
	
	/**
	 * @param parent
	 * @return children of parent(can be of length 0)
	 */
	public Object[] getChildren(Object parent) {
		return ((TreeNode)parent).getChildren();
	}
	
	/**
	 * @param parent
	 * @return true if parent has children, false otherwise
	 */
	public boolean hasChildren(Object parent) {
		return ((TreeNode)parent).hasChildren();
	}
	
	/**
	 * @param obj
	 * @return the text to be displayed in the GUI for this object(tree node)
	 */
	public String getDisplayText(Object obj){
		if(((TreeNode)obj).getSize() > 0)
			return (((TreeNode)obj).getName()+"("+((TreeNode)obj).getSize()+")");
		else return ((TreeNode)obj).getName();
	}
	
	
	/**
	 * @param obj
	 * @return the internal identifier of this objects(tree nodes) {@link Replay}
	 */
	public String getIdentifier(Object obj) {
		return ((TreeNode)obj).getIdentifier();
	}

	/**
	 * @param obj
	 * @return this objects(tree nodes) {@link Replay}
	 */
	public Replay getReplay(Object obj) {
		return ((TreeNode)obj).getReplay();
	}

	/**
	 * Puts this tree into default state(empty).
	 */
	public void reset() {
		invisibleRoot = new TreeNode(null, "");
	}
}

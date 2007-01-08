package org.electrocodeogram.codereplay.userInterface;


import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.CoolItem;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.part.DrillDownAdapter;
import org.eclipse.ui.part.ViewPart;
import org.electrocodeogram.codereplay.dataProvider.DataProvider;
import org.electrocodeogram.codereplay.dataProvider.IModelChangeListener;
import org.electrocodeogram.codereplay.dataProvider.ModelChangeEvent;
import org.electrocodeogram.codereplay.eventIO.AbstractReader;
import org.electrocodeogram.codereplay.eventIO.AbstractWriter;
import org.electrocodeogram.codereplay.pluginControl.ReplayControl;
import org.electrocodeogram.codereplay.pluginControl.TransferDummy;
import org.electrocodeogram.codereplay.pluginControl.TreeViewContentProvider;


/**
 * This class represents the TreeView of the GUI part of this plugin.
 * So, all GUI elements in the left hand view are created here.
 * 
 * @author marco kranz
 */
public class ReplayTreeView extends ViewPart implements IModelChangeListener{
	private TreeViewer viewer;
	private DrillDownAdapter drillDownAdapter;
	private Action action1;
	private Action action2;
	private Action doubleClickAction;
	private TreeViewContentProvider treecontent_provider = new TreeViewContentProvider();
//	 open file dialog
	private FileDialog openfile;
//	 save file dialog
	private FileDialog savefile;
	private Button open;
	private Button save;
	private MessageBox loading = null;

	
	class NameSorter extends ViewerSorter {
	}

	/**
	 * The constructor.
	 */
	public ReplayTreeView() {
	}
	
	
	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
		
//		 -------------------
		// parent
		// -------------------
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		parent.setLayout(gridLayout);
		
		openfile = new FileDialog(parent.getShell(), SWT.OPEN);
		String[] filter = {"*.*"};
		openfile.setFilterExtensions(filter);
		savefile = new FileDialog(parent.getShell(), SWT.SAVE);
		savefile.setFilterExtensions(filter);
		
		// ---------------
		// menu bar
		// ---------------
		// self made 'cause local menu bar doesn't support additional drop down menus...
		GridData gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		//gridData.verticalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		//gridData.grabExcessVerticalSpace = true;
		gridLayout = new GridLayout();
		gridLayout.horizontalSpacing = 10;
		gridLayout.numColumns = 2;
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(gridLayout);
		comp.setLayoutData(gridData);
		
		// ----------------
		// reader selection
		// ----------------
		//loading = new MessageBox(parent.getShell(), SWT.ICON_WORKING | SWT.CANCEL);
		//loading.setMessage("Loading...");
		Combo reader = new Combo(comp, SWT.DROP_DOWN | SWT.READ_ONLY);
		reader.setToolTipText("select a reader. different readers\n usually provide support for different file types.");
		AbstractReader[] readers = ReplayControl.getInstance().getReaders();
		for(int i = 0; i < readers.length; i++){
			reader.add(readers[i].getName(), i);
		}
		reader.select(0);
		ReplayControl.getInstance().setActiveReader(0);
		reader.addSelectionListener(new SelectionListener(){
			public void widgetSelected(SelectionEvent e) {
				ReplayControl.getInstance().setActiveReader(((Combo)e.widget).getSelectionIndex());
			}
			public void widgetDefaultSelected(SelectionEvent e) {}
		});
		
//		 ----------------
		// load button
		// ----------------
		open = new Button(comp, SWT.PUSH | SWT.FLAT);
		open.setText("open");
		open.setToolTipText("open a file");
		open.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				String result = openfile.open();
				if(result != null && !"".equals(result))
					ReplayControl.getInstance().openFile(new java.io.File(result));
			}
		});
		// ----------------
		// writer selection
		// ----------------
		Combo writer = new Combo(comp, SWT.DROP_DOWN | SWT.READ_ONLY);
		writer.setToolTipText("select a writer. different writers usually provide support for different file types.");
		AbstractWriter[] writers = ReplayControl.getInstance().getWriters();
		for(int i = 0; i < writers.length; i++){
			writer.add(writers[i].getName(), i);
		}
		writer.select(0);
		ReplayControl.getInstance().setActiveWriter(0);
		reader.addSelectionListener(new SelectionListener(){
			public void widgetSelected(SelectionEvent e) {
				ReplayControl.getInstance().setActiveWriter(((Combo)e.widget).getSelectionIndex());
			}
			public void widgetDefaultSelected(SelectionEvent e) {}
		});
		
		// ----------------
		// save button
		// ----------------
		save = new Button(comp, SWT.PUSH | SWT.FLAT);
		save.setText("save");
		save.setToolTipText("save content");
		save.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				String result = savefile.open();
				if(result != null && !"".equals(result))
					ReplayControl.getInstance().saveFile(new java.io.File(result));
			}
		});
		
		
		
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.verticalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		Tree treeWidget = viewer.getTree();
		treeWidget.setLayoutData(gridData);
		drillDownAdapter = new DrillDownAdapter(viewer);
		
		viewer.setContentProvider(treecontent_provider);
		viewer.setLabelProvider(treecontent_provider);
		DataProvider.getInstance().addModelChangeListener(this);
		viewer.setSorter(new NameSorter());
		viewer.setInput(getViewSite());
		//makeActions(parent);
		hookContextMenu();
		hookDoubleClickAction();
		//contributeToActionBars();
		
		
		// --------------------------------------
		// drag and drop support implementation
		// --------------------------------------
		int operations = DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK;
		
		viewer.addDragSupport(operations, new Transfer[]{TransferDummy.getInstance()}, new DragSourceListener() {
		    public void dragStart(DragSourceEvent de) {
		    	/*IStructuredSelection selection = (IStructuredSelection)viewer.getSelection();
		    	Object[] selectedNodes = selection.toArray();
		    	for(int i = 0; i < selection.size(); i++){
		    		
		    		System.out.println("from dragStart()..... "+treecontent_provider.getIdentifier(selectedNodes[i]));
		    	}*/
		    }
		    public void dragSetData(DragSourceEvent de) {}
		    public void dragFinished(DragSourceEvent de) {}
		});		
		
		// replay merge dialog
		final MessageBox merge_yes_no = new MessageBox(parent.getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
		
		viewer.addDropSupport(operations, new Transfer[]{TransferDummy.getInstance()}, new DropTargetListener() {
			public void dragEnter(DropTargetEvent event){}
			public void dragLeave(DropTargetEvent event){}
			public void dragOperationChanged(DropTargetEvent event){}
			public void dragOver(DropTargetEvent event){}
			public void dropAccept(DropTargetEvent event){} 
			public void drop(DropTargetEvent event){
				IStructuredSelection selection = (IStructuredSelection)viewer.getSelection();
				Object[] selectedNodes = selection.toArray();
				if(event.item != null && treecontent_provider.getIdentifier(event.item.getData()) != null){
					String droped_on = treecontent_provider.getIdentifier(event.item.getData());
					merge_yes_no.setMessage(buildMessageString(event, selectedNodes));
					int result = merge_yes_no.open();
					if(result == SWT.YES){
				    	DataProvider.getInstance().mergeReplays(selectedNodes, event.item.getData());
					}
				}
			}
			
			String buildMessageString(DropTargetEvent event, Object[] nodes){
				String droped_on_path = treecontent_provider.getReplay(event.item.getData()).getFullPathAsString();
				String result = "Do you really want to move the content of the replay(s)\n\n";
				for(int i = 0; i < nodes.length; i++){
					result = result + treecontent_provider.getReplay(nodes[i]).getFullPathAsString() + "\n";
				}
				result = result + "\nto the following replay?\n\n" + droped_on_path;
				return result;
			}
		});

	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				ReplayTreeView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	/*private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}*/

	/*private void fillLocalPullDown(IMenuManager manager) {
		manager.add(action1);
		manager.add(new Separator());
		manager.add(action2);
	}*/

	private void fillContextMenu(IMenuManager manager) {
		manager.add(action1);
		manager.add(action2);
		manager.add(new Separator());
		drillDownAdapter.addNavigationActions(manager);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	/*private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(action1);
		manager.add(action2);
		manager.add(new Separator());
		//drillDownAdapter.addNavigationActions(manager);
	}*/

	/*private void makeActions(Composite parent) {
		openfile = new FileDialog(parent.getShell(), SWT.OPEN);
		String[] filter = {"*.*"};
		openfile.setFilterExtensions(filter);
		action1 = new Action() {
			public void run() {
				String result = openfile.open();
				if(result != null && !"".equals(result))
					ReplayControl.getInstance().openFile(new java.io.File(result));
			}
		};
		action1.setText("open");
		action1.setToolTipText("open a file");
		//action1.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_FOLDER));
		savefile = new FileDialog(parent.getShell(), SWT.SAVE);
		savefile.setFilterExtensions(filter);
		action2 = new Action() {
			public void run() {
				ReplayControl control = ReplayControl.getInstance();
				String result = savefile.open();
				if(result != null && !"".equals(result))
					control.saveFile(new java.io.File(result));
			}
		};
		action2.setText("save");
		action2.setToolTipText("save to file");
		//action2.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
	}*/

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				ISelection selection = event.getSelection();
				Object obj = ((IStructuredSelection)selection).getFirstElement();
				if(treecontent_provider.getIdentifier(obj) != null){
					treecontent_provider.setActiveReplay(treecontent_provider.getIdentifier(obj));
				}	
			}
		});
	}
	
	private void showMessage(String message) {
		MessageDialog.openInformation(
			viewer.getControl().getShell(),
			"Sample View",
			message);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	/**
	 * Implementation of IModelChangeListener.(if the data model has changed, refresh view)
	 */
	public void modelChange(ModelChangeEvent event){
		int cause = event.getCause();
		if(cause == ModelChangeEvent.NEW_ELEMENT || cause == ModelChangeEvent.REPLAY_REMOVED)
			viewer.setInput(event);
	}
}
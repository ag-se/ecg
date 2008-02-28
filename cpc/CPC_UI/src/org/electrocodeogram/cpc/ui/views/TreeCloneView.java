package org.electrocodeogram.cpc.ui.views;


import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.ITextEditor;
import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.data.ICloneFile;
import org.electrocodeogram.cpc.core.api.data.ICloneObject;
import org.electrocodeogram.cpc.core.utils.CoreFileUtils;
import org.electrocodeogram.cpc.ui.actions.RemoveCloneAction;
import org.electrocodeogram.cpc.ui.actions.ShowCloneDetailsAction;
import org.electrocodeogram.cpc.ui.actions.ShowCloneInReplayViewAction;
import org.electrocodeogram.cpc.ui.api.ICPCSelectionSourceViewPart;
import org.electrocodeogram.cpc.ui.data.CloneDataModel;
import org.electrocodeogram.cpc.ui.utils.EclipseUtils;


public class TreeCloneView extends ViewPart implements ICPCSelectionSourceViewPart
{
	private static Log log = LogFactory.getLog(TreeCloneView.class);

	public static final String VIEW_ID = "org.electrocodeogram.cpc.ui.views.treecloneview"; //$NON-NLS-1$

	private TreeViewer viewer;
	private CloneSorter sorter;
	private Action showCloneDetailsAction;
	private Action showCloneInReplayViewAction;
	private Action removeCloneAction;
	private Action doubleClickAction;
	private Action selectionChangedAction;

	private CloneDataModel model;
	private TreeColumn stateColumn;
	private TreeColumn projectColumn;
	private TreeColumn fileColumn;
	private TreeColumn posColumn;
	private TreeColumn lenColumn;
	private TreeColumn creatorColumn;

	private IMemento memento;

	public TreeCloneView()
	{
		log.trace("TreeCloneView()"); //$NON-NLS-1$
	}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialise it.
	 */
	@Override
	public void createPartControl(Composite parent)
	{
		if (log.isTraceEnabled())
			log.trace("createPartControl() - parent: " + parent); //$NON-NLS-1$

		model = CloneDataModel.getInstance();

		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		getSite().setSelectionProvider(viewer);

		Tree tree = viewer.getTree();
		tree.setHeaderVisible(true);
		tree.setLinesVisible(true);
		//AutoResizeTableLayout layout = new AutoResizeTableLayout(table);
		//table.setLayout(layout);

		//0 - State
		stateColumn = new TreeColumn(tree, SWT.CENTER, 0);
		stateColumn.setText(Messages.TreeCloneView_ColumnHead_Status);
		stateColumn.setWidth(80);
		//layout.addColumnData(new ColumnPixelData(30));

		//1 - Project
		projectColumn = new TreeColumn(tree, SWT.LEFT, 1);
		projectColumn.setText(Messages.TreeCloneView_ColumnHead_Project);
		projectColumn.setWidth(100);

		//2 - File
		fileColumn = new TreeColumn(tree, SWT.LEFT, 2);
		fileColumn.setText(Messages.TreeCloneView_ColumnHead_File);
		fileColumn.setWidth(250);

		//3 - Position/Offset
		posColumn = new TreeColumn(tree, SWT.RIGHT, 3);
		posColumn.setText(Messages.TreeCloneView_ColumnHead_Position);
		posColumn.setWidth(75);

		//4 - Length
		lenColumn = new TreeColumn(tree, SWT.RIGHT, 4);
		lenColumn.setText(Messages.TreeCloneView_ColumnHead_Length);
		lenColumn.setWidth(50);

		//5 - Creator
		creatorColumn = new TreeColumn(tree, SWT.LEFT, 5);
		creatorColumn.setText(Messages.TreeCloneView_ColumnHead_Creator);
		creatorColumn.setWidth(200);

		viewer.setContentProvider(new TreeCloneContentProvider(VIEW_ID));
		viewer.setLabelProvider(new CloneLabelProvider(model, true));
		viewer.setInput(model);

		createTableSorter();
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		hookSelectionAction();
		contributeToActionBars();
	}

	private void createTableSorter()
	{
		Comparator<ICloneObject> stateComparator = new Comparator<ICloneObject>()
		{
			public int compare(ICloneObject o1, ICloneObject o2)
			{
				if (o1 instanceof IClone && o2 instanceof IClone)
					//TODO: take state into account here
					return ((IClone) o1).getUuid().compareTo(((IClone) o2).getUuid());
				else
					return o1.getUuid().compareTo(o2.getUuid());
			}
		};
		Comparator<ICloneObject> projectComparator = new Comparator<ICloneObject>()
		{
			public int compare(ICloneObject o1, ICloneObject o2)
			{
				if (o1 instanceof IClone && o2 instanceof IClone)
					//TODO: use project here
					return ((IClone) o1).getUuid().compareTo(((IClone) o2).getUuid());
				else
					return o1.getUuid().compareTo(o2.getUuid());
			}
		};
		Comparator<ICloneObject> fileComparator = new Comparator<ICloneObject>()
		{
			public int compare(ICloneObject o1, ICloneObject o2)
			{
				if (o1 instanceof IClone && o2 instanceof IClone)
					//TODO: use file here
					return ((IClone) o1).getUuid().compareTo(((IClone) o2).getUuid());
				else
					return o1.getUuid().compareTo(o2.getUuid());
			}
		};
		Comparator<ICloneObject> posComparator = new Comparator<ICloneObject>()
		{
			public int compare(ICloneObject o1, ICloneObject o2)
			{
				if (o1 instanceof IClone && o2 instanceof IClone)
					return ((IClone) o1).getOffset() - ((IClone) o2).getOffset();
				else
					return o1.getUuid().compareTo(o2.getUuid());
			}
		};
		Comparator<ICloneObject> lenComparator = new Comparator<ICloneObject>()
		{
			public int compare(ICloneObject o1, ICloneObject o2)
			{
				if (o1 instanceof IClone && o2 instanceof IClone)
					return ((IClone) o1).getLength() - ((IClone) o2).getLength();
				else
					return o1.getUuid().compareTo(o2.getUuid());
			}
		};
		Comparator<ICloneObject> creatorComparator = new Comparator<ICloneObject>()
		{
			public int compare(ICloneObject o1, ICloneObject o2)
			{
				if (o1 instanceof IClone && o2 instanceof IClone)
					return ((IClone) o1).getCreator().compareTo(((IClone) o2).getCreator());
				else
					return o1.getUuid().compareTo(o2.getUuid());
			}
		};

		sorter = new CloneSorter(viewer, new TreeColumn[] { stateColumn, projectColumn, fileColumn, posColumn,
				lenColumn, creatorColumn }, new Comparator[] { stateComparator, projectComparator, fileComparator,
				posComparator, lenComparator, creatorComparator });

		//check for saved settings
		if (memento != null)
			//reuse old settings
			sorter.init(memento);
		else
			//no saved settings available, sort by position (default)
			sorter.sortUsingColumn(1);

		viewer.setSorter(sorter);
	}

	private void hookContextMenu()
	{
		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener()
		{
			public void menuAboutToShow(IMenuManager manager)
			{
				TreeCloneView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars()
	{
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager)
	{
		manager.add(showCloneDetailsAction);
		manager.add(showCloneInReplayViewAction);
		manager.add(new Separator());
		manager.add(removeCloneAction);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillContextMenu(IMenuManager manager)
	{
		manager.add(showCloneDetailsAction);
		manager.add(showCloneInReplayViewAction);
		manager.add(removeCloneAction);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager)
	{
		manager.add(showCloneDetailsAction);
	}

	private void makeActions()
	{
		showCloneDetailsAction = new ShowCloneDetailsAction(viewer);
		showCloneInReplayViewAction = new ShowCloneInReplayViewAction(viewer);
		removeCloneAction = new RemoveCloneAction(viewer);

		doubleClickAction = new Action()
		{
			@Override
			public void run()
			{
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection) selection).getFirstElement();

				if (obj instanceof IClone)
				{
					CloneDetailsDialog dialog = new CloneDetailsDialog(viewer.getControl().getShell(), 0, (IClone) obj);
					dialog.open();
				}
				else
				{
					showMessage("Object Details:\n\n" + obj.toString());
				}
			}
		};

		selectionChangedAction = new Action()
		{
			@SuppressWarnings("unchecked")
			@Override
			public void run()
			{
				IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
				if (selection == null)
					return;

				Object obj = selection.getFirstElement();
				if (obj == null)
					return;

				//get a list of all IClone objects in this selection
				List<IClone> selectedClones = new LinkedList<IClone>();
				for (Object selectedObj : selection.toArray())
					if (selectedObj instanceof IClone)
						selectedClones.add((IClone) selectedObj);

				//make sure this is actually a clone object
				if (selectedClones.isEmpty())
				{
					if (log.isTraceEnabled())
						log.trace("selectionChangedAction.run() - ignoring non-IClone selection: " + obj); //$NON-NLS-1$
					return;
				}

				IClone clone = selectedClones.get(0);
				int offset = clone.getOffset();
				int len = clone.getLength();

				if (log.isTraceEnabled())
					log.trace("selectionChangedAction.run() - setting selection - offset: " + offset + ", length: " //$NON-NLS-1$ //$NON-NLS-2$
							+ len);

				//get the active editor and the file handle for the clone's clone file
				IEditorPart editor = EclipseUtils.getActiveEditor();

				ICloneFile cloneFile = CloneDataModel.getInstance().getCloneFileByUuid(clone.getFileUuid());
				if (cloneFile == null)
				{
					log.warn("selectionChangedAction.run() - unable to get clone file for clone: " + clone); //$NON-NLS-1$
					return;
				}

				IFile cloneFileHandle = CoreFileUtils.getFileForCloneFile(cloneFile);
				if (cloneFileHandle == null)
				{
					log.warn("selectionChangedAction.run() - unable to get file handle for clone file: " + cloneFile); //$NON-NLS-1$
					return;
				}

				if ((editor != null) && (editor instanceof ITextEditor))
				{
					//make sure the editor is displaying the correct file for this clone
					if (editor.getEditorInput() != null && editor.getEditorInput() instanceof FileEditorInput)
					{
						if (cloneFileHandle.equals(((FileEditorInput) editor.getEditorInput()).getFile()))
						{
							//select and reveal the clone range
							EclipseUtils.selectInEditor((ITextEditor) editor, offset, len);
						}
						else
						{
							if (log.isTraceEnabled())
								log
										.trace("selectionChangedAction.run() - ignoring clone selection, clone does not belong to currently active editor: " + editor); //$NON-NLS-1$
						}
					}
					else
					{
						log.warn("selectionChangedAction.run() - unable to acquire editor input: " + editor); //$NON-NLS-1$
					}
				}
				else
				{
					log.warn("selectionChangedAction.run() - unable to acquire text editor: " + editor); //$NON-NLS-1$
				}

				//notify other views about this selection
				model.clonesSelected(VIEW_ID, selectedClones);
			}
		};
	}

	private void hookDoubleClickAction()
	{
		viewer.addDoubleClickListener(new IDoubleClickListener()
		{
			public void doubleClick(DoubleClickEvent event)
			{
				doubleClickAction.run();
			}
		});
	}

	private void hookSelectionAction()
	{
		viewer.addSelectionChangedListener(new ISelectionChangedListener()
		{
			public void selectionChanged(SelectionChangedEvent event)
			{
				selectionChangedAction.run();
			}
		});
	}

	private void showMessage(String message)
	{
		MessageDialog.openInformation(viewer.getControl().getShell(),
				Messages.TreeCloneView_MessageDialogTitle_TreeCloneView, message);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.part.ViewPart#saveState(org.eclipse.ui.IMemento)
	 */
	@Override
	public void saveState(IMemento memento)
	{
		super.saveState(memento);
		sorter.saveState(memento);
		//filterAction.saveState(memento);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.part.ViewPart#init(org.eclipse.ui.IViewSite, org.eclipse.ui.IMemento)
	 */
	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException
	{
		super.init(site, memento);
		this.memento = memento;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus()
	{
		//Passing the focus request to the viewer's control.
		viewer.getControl().setFocus();
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.ui.api.ISelectionSourceViewPart#getSelection()
	 */
	@Override
	public IStructuredSelection getSelection()
	{
		return (IStructuredSelection) viewer.getSelection();
	}

}

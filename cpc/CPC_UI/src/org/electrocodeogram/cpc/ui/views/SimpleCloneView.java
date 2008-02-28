package org.electrocodeogram.cpc.ui.views;


import java.util.Comparator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.ITextEditor;
import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.ui.actions.RemoveCloneAction;
import org.electrocodeogram.cpc.ui.actions.ShowCloneDetailsAction;
import org.electrocodeogram.cpc.ui.actions.ShowCloneInReplayViewAction;
import org.electrocodeogram.cpc.ui.api.ICPCSelectionSourceViewPart;
import org.electrocodeogram.cpc.ui.data.CloneDataModel;
import org.electrocodeogram.cpc.ui.utils.EclipseUtils;


/**
 * This sample class demonstrates how to plug-in a new
 * workbench view. The view shows data obtained from the
 * model. The sample creates a dummy model on the fly,
 * but a real implementation would connect to the model
 * available either in this or another plug-in (e.g. the workspace).
 * The view is connected to the model using a content provider.
 * <p>
 * The view uses a label provider to define how model
 * objects should be presented in the view. Each
 * view can present the same model objects using
 * different labels and icons, if needed. Alternatively,
 * a single label provider can be shared between views
 * in order to ensure that objects of the same type are
 * presented in the same way everywhere.
 * <p>
 * 
 * Partly taken from: Eclipse, Building Commercial-Quality Plugins, 2nd Ed.   
 */
public class SimpleCloneView extends ViewPart implements ICPCSelectionSourceViewPart
{
	private static Log log = LogFactory.getLog(SimpleCloneView.class);

	public static final String VIEW_ID = "org.electrocodeogram.cpc.ui.views.simplecloneview"; //$NON-NLS-1$

	private CloneDataModel model;
	private TableViewer viewer;
	private CloneSorter sorter;
	private Action showCloneDetailsAction;
	private Action showCloneInReplayViewAction;
	private Action removeCloneAction;
	private Action doubleClickAction;
	private Action selectionChangedAction;

	private TableColumn stateColumn;
	private TableColumn posColumn;
	private TableColumn lenColumn;
	private TableColumn creatorColumn;
	private TableColumn dateColumn;

	private IMemento memento;

	public SimpleCloneView()
	{
		log.trace("SimpleCloneView()"); //$NON-NLS-1$
	}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	@Override
	public void createPartControl(Composite parent)
	{
		model = CloneDataModel.getInstance();

		viewer = new TableViewer(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
		viewer.setContentProvider(new CloneContentProvider(VIEW_ID));
		viewer.setLabelProvider(new CloneLabelProvider(model, false));
		viewer.setInput(model);

		getSite().setSelectionProvider(viewer);

		Table table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		AutoResizeTableLayout layout = new AutoResizeTableLayout(table);
		table.setLayout(layout);

		//0 - State
		stateColumn = new TableColumn(table, SWT.CENTER);
		stateColumn.setText(Messages.SimpleCloneView_ColumnHead_Status);
		//stateColumn.setWidth(40);
		layout.addColumnData(new ColumnPixelData(30));

		//1 - Position
		posColumn = new TableColumn(table, SWT.RIGHT);
		posColumn.setText(Messages.SimpleCloneView_ColumnHead_Position);
		//posColumn.setWidth(50);
		layout.addColumnData(new ColumnWeightData(50));

		//2 - Length
		lenColumn = new TableColumn(table, SWT.RIGHT);
		lenColumn.setText(Messages.SimpleCloneView_ColumnHead_Length);
		//lenColumn.setWidth(50);
		layout.addColumnData(new ColumnWeightData(50));

		//3 - Creator
		creatorColumn = new TableColumn(table, SWT.LEFT);
		creatorColumn.setText(Messages.SimpleCloneView_ColumnHead_Creator);
		//creatorColumn.setWidth(100);
		layout.addColumnData(new ColumnWeightData(100));

		//4 - Creation Date
		dateColumn = new TableColumn(table, SWT.LEFT);
		dateColumn.setText(Messages.SimpleCloneView_ColumnHead_Date);
		//dateColumn.setWidth(100);
		layout.addColumnData(new ColumnWeightData(100));

		createTableSorter();
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		hookSelectionAction();
		contributeToActionBars();
	}

	private void createTableSorter()
	{
		Comparator<IClone> stateComparator = new Comparator<IClone>()
		{
			public int compare(IClone o1, IClone o2)
			{
				//TODO: take state into account here
				return ((IClone) o1).getUuid().compareTo(((IClone) o2).getUuid());
			}
		};
		Comparator<IClone> posComparator = new Comparator<IClone>()
		{
			public int compare(IClone o1, IClone o2)
			{
				return ((IClone) o1).getOffset() - ((IClone) o2).getOffset();
			}
		};
		Comparator<IClone> lenComparator = new Comparator<IClone>()
		{
			public int compare(IClone o1, IClone o2)
			{
				return ((IClone) o1).getLength() - ((IClone) o2).getLength();
			}
		};
		Comparator<IClone> creatorComparator = new Comparator<IClone>()
		{
			public int compare(IClone o1, IClone o2)
			{
				return ((IClone) o1).getCreator().compareTo(((IClone) o2).getCreator());
			}
		};
		Comparator<IClone> dateComparator = new Comparator<IClone>()
		{
			public int compare(IClone o1, IClone o2)
			{
				return ((IClone) o1).getCreationDate().compareTo(((IClone) o2).getCreationDate());
			}
		};

		sorter = new CloneSorter(viewer, new TableColumn[] { stateColumn, posColumn, lenColumn, creatorColumn,
				dateColumn }, new Comparator[] { stateComparator, posComparator, lenComparator, creatorComparator,
				dateComparator });

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
				SimpleCloneView.this.fillContextMenu(manager);
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

				//make sure this is actually a clone object
				if (!(obj instanceof IClone))
				{
					log.error("selectionChangedAction.run() - unexpected object in selection: " + obj, new Throwable()); //$NON-NLS-1$
					return;
				}

				IClone clone = (IClone) obj;
				int offset = clone.getOffset();
				int len = clone.getLength();

				if (log.isTraceEnabled())
					log.trace("selectionChangedAction.run() - setting selection - offset: " + offset + ", length: " //$NON-NLS-1$ //$NON-NLS-2$
							+ len);

				IEditorPart editor = EclipseUtils.getActiveEditor();
				if ((editor != null) && (editor instanceof ITextEditor))
				{
					EclipseUtils.selectInEditor((ITextEditor) editor, offset, len);
				}
				else
				{
					log.warn("selectionChangedAction.run() - unable to acquire text editor: " + editor); //$NON-NLS-1$
				}

				//notify other views about this selection
				model.clonesSelected(VIEW_ID, selection.toList());
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
				Messages.SimpleCloneView_MessageDialogTitle_SimpleCloneView, message);
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

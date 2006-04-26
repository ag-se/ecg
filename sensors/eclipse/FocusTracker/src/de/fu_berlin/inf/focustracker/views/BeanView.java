package de.fu_berlin.inf.focustracker.views;


import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Formatter;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.viewsupport.JavaElementImageProvider;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import de.fu_berlin.inf.focustracker.interaction.Interaction;
import de.fu_berlin.inf.focustracker.interaction.InteractionListener;
import de.fu_berlin.inf.focustracker.repository.InteractionRepository;
import de.fu_berlin.inf.focustracker.resources.FocusTrackerResources;



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
 */

public abstract class BeanView extends ViewPart implements InteractionListener {

	private static final long REFRESH_INTERVAL = 1000;
	protected TableViewer viewer;
	private Action clearAction;
	private Action restoreAction;
	private Action deleteAction;
	private Action pinAction;
	
	private Table table = null;
	private JavaElementImageProvider imageProvider = new JavaElementImageProvider();
	private boolean disposed;
	private StartWithOffsetFilter startFromFilter;
	private boolean pinnedOutput; 
	private Timer delayedRefreshTimer = new Timer(); 
	
	static {
		Introspector.setBeanInfoSearchPath(new String[] { "de.fu_berlin.inf.focustracker.beaninfo" });
	}
	
	
	/*
	 * The content provider class is responsible for
	 * providing objects to the view. It can wrap
	 * existing objects in adapters or simply return
	 * objects as-is. These objects may be sensitive
	 * to the current input of the view, or ignore
	 * it and always show the same content 
	 * (like Task List, for example).
	 */
	 
	class BeanContentProvider implements IStructuredContentProvider {
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}
		public void dispose() {
		}
		public Object[] getElements(Object parent) {
			return BeanView.this.getObjects();
		}
	}
	public abstract Object[] getObjects();
//	class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {
//		public String getColumnText(Object obj, int index) {
//			return getText(obj);
//		}
//		public Image getColumnImage(Object obj, int index) {
//			return getImage(obj);
//		}
//		public Image getImage(Object obj) {
//			return PlatformUI.getWorkbench().
//					getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
//		}
//	}
	class NameSorter extends ViewerSorter {
	}

	/**
	 * The constructor.
	 */
	public BeanView() {
	}

	public abstract Class getClazz();
	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
		
		BeanInfo beanInfo = null;
//		final Table table = new Table(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        table = new Table(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI
                | SWT.FULL_SELECTION);
		table.setLinesVisible(true);

        viewer = new TableViewer(table);
        try {
        	beanInfo = java.beans.Introspector.getBeanInfo(getClazz());
        	createTableColumns(table, beanInfo);
        } catch (IntrospectionException e1) {
        	// TODO Auto-generated catch block
        	e1.printStackTrace();
        }
		table.setHeaderVisible(true);
		
		viewer.setSorter(new NameSorter());
		viewer.setContentProvider(new BeanContentProvider());
		viewer.setLabelProvider(new BeanLabelProvider(beanInfo));
		viewer.setInput(getViewSite());
		
		setContentDescription("View");
		
		makeActions();
		contributeToActionBars();
		
	}

	private void createTableColumns(Table aTable, BeanInfo aBeanInfo) {
		
		int columnWidth = getViewSite().getShell().getSize().y / aBeanInfo.getPropertyDescriptors().length;
		for (PropertyDescriptor propertyDescriptor : aBeanInfo.getPropertyDescriptors()) {
			TableColumn column = new TableColumn(aTable, SWT.LEFT);
			column.setText(propertyDescriptor.getDisplayName());
//				column.setWidth(100);
			column.setWidth(columnWidth);
		} 
	}
	class BeanLabelProvider extends LabelProvider implements ITableLabelProvider {
		
//		private BeanInfo beanInfo;
		private PropertyDescriptor[] propertyDescriptors;
		BeanLabelProvider(BeanInfo aBeanInfo) {
//			beanInfo = aBeanInfo;
			propertyDescriptors = aBeanInfo.getPropertyDescriptors();
		}
		
		public String getColumnText(Object obj, int index) {
			
			try {
				String prefix = "";
				Object object = propertyDescriptors[index].getReadMethod().invoke(obj, new Object[0]);
				if (object instanceof IJavaElement) {
					
					Formatter formatter = new Formatter();
					prefix = "(" + 
					formatter.format("%1.2f", InteractionRepository.getInstance().getLastScore((IJavaElement)object)).toString()
					+ ") ";
				}
				return prefix + String.valueOf(propertyDescriptors[index].getReadMethod().invoke(obj, new Object[0]));
			} catch (Exception e) {
//				e.printStackTrace();
			}
			return "error";// getText(obj);
		}
		public Image getColumnImage(Object obj, int index) {
//			return getImage(obj);
			Object javaElement;
			try {
				javaElement = propertyDescriptors[index].getReadMethod().invoke(obj, new Object[0]);
				if (javaElement instanceof IJavaElement) {
					return JavaPlugin.getImageDescriptorRegistry().get(imageProvider.getBaseImageDescriptor((IJavaElement)javaElement, 0));			
				}
			} catch (Exception e) {
//				e.printStackTrace();
			}
			return null;
		}
		public Image getImage(Object obj) {
			return PlatformUI.getWorkbench().
			getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
		}
		
		
	}

	protected void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(clearAction);
		manager.add(deleteAction);
		manager.add(restoreAction);
		manager.add(pinAction);
	}
	
	protected void makeActions() {
		clearAction = new Action() {
			public void run() {
				if(startFromFilter != null) {
					viewer.removeFilter(startFromFilter);
				}
				startFromFilter = new StartWithOffsetFilter(getObjects().length);
				viewer.addFilter(startFromFilter);
				viewer.refresh();
			}
		};
		clearAction.setText("Clear view");
		clearAction.setToolTipText("Clear view");
		clearAction.setImageDescriptor(FocusTrackerResources.CLEAR);
		
		deleteAction = new Action() {
			public void run() {
				if(startFromFilter != null) {
					viewer.removeFilter(startFromFilter);
				}
				startFromFilter = new StartWithOffsetFilter(getObjects().length - 1);
				viewer.addFilter(startFromFilter);
				viewer.refresh();
			}
		};
		deleteAction.setText("Delete data");
		deleteAction.setToolTipText("Delete data");
		deleteAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
			getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));
		
		restoreAction = new Action() {
			public void run() {
				if(startFromFilter != null) {
					viewer.removeFilter(startFromFilter);
				}
				startFromFilter = new StartWithOffsetFilter(-1);
				viewer.addFilter(startFromFilter);
				viewer.refresh();
			}
		};
		restoreAction.setText("Restore Log");
		restoreAction.setToolTipText("Restore Log");
		restoreAction.setImageDescriptor(FocusTrackerResources.RESTORE_LOG);
		
		pinAction = new Action("Pin Output", IAction.AS_CHECK_BOX) {
			public void run() {
				pinnedOutput = isChecked();
			}
		};
		pinAction.setText("Pin Output");
		pinAction.setToolTipText("Pin Output");
		pinAction.setImageDescriptor(FocusTrackerResources.PIN_EDITOR);
		
	}
	
	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	public TableViewer getViewer() {
		return viewer;
	}
	
	@Override
	public void dispose() {
		disposed = true;
		super.dispose();
	}
	
	
	public boolean isDisposed() {
		return disposed;
	}
	
	
	class StartWithOffsetFilter extends ViewerFilter {
		
		int startFrom = -1;
		int count = 0;
		public StartWithOffsetFilter(int aStartFrom) {
			startFrom = aStartFrom;
		}
		
		@Override
	    public Object[] filter(Viewer viewer, Object parent, Object[] elements) {
	    	count=0;
	    	return super.filter(viewer, parent, elements);
	    }		
		@Override
		public boolean select(Viewer aViewer, Object aParentElement, Object aElement) {
			count++;
			return(count>startFrom);
		}
		public int getCount() {
			return count;
		}
		public void setCount(int aCount) {
			count = aCount;
		}
		
	}
	public boolean isPinnedOutput() {
		return pinnedOutput;
	}
	
	public boolean isLabelUpdate() {
		return false;
	}

	public void refresh(final boolean aUpdateLabels) {
		
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {
				viewer.refresh(aUpdateLabels);
			}
		});		
		if(startFromFilter != null && startFromFilter.startFrom != -1) {
			setContentDescription("Filter active: Showing " + viewer.getTable().getItemCount() + " of " + getObjects().length + " Elements");
		}
	}
	
	public void notifyInteractionObserved(List<? extends Interaction> aInteractions) {
		
		delayedRefreshTimer.cancel();
		delayedRefreshTimer = new Timer();
		delayedRefreshTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						refresh(isLabelUpdate());
					}
				});
			}
			
		}, REFRESH_INTERVAL);
		
	}
	
}
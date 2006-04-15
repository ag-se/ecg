package de.fu_berlin.inf.focustracker.views;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Composite;

import de.fu_berlin.inf.focustracker.jobs.RefreshViewsJob;
import de.fu_berlin.inf.focustracker.repository.InteractionRepository;


public class VisitedElementsView extends BeanView {

	public static final String ID = "de.fu_berlin.inf.focustracker.views.VisitedElementsView";

	private Action doubleClickAction;

	private RefreshViewsJob refreshJob;
	
	@Override
	public Class getClazz() {
		return IJavaElement.class;
	}
	
	@Override
	public void createPartControl(Composite aParent) {
		super.createPartControl(aParent);
		refreshJob = new RefreshViewsJob(this);
		makeActions();
	}

	@Override
	public Object[] getObjects() {
		return InteractionRepository.getInstance().getJavaElements();
	}
	
	protected void makeActions() {
		super.makeActions();
		doubleClickAction = new Action() {
			public void run() {
				ISelection selection = viewer.getSelection();
//				IJavaElement javaElement = (IJavaElement)((IStructuredSelection)selection).getFirstElement();
//				showMessage("Double-click detected on "+selection.toString());
//				FloatingChartView.findMe().setJavaElement(javaElement);
			}
		};
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});

	}
	
	@Override
	public void dispose() {
		refreshJob.cancel();
		super.dispose();
	}
	
	@Override
	public boolean isLabelUpdate() {
		return true;
	}
	
}

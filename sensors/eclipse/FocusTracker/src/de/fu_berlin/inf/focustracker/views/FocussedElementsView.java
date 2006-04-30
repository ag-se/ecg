package de.fu_berlin.inf.focustracker.views;

import java.util.Comparator;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.widgets.Composite;

import de.fu_berlin.inf.focustracker.EventDispatcher;
import de.fu_berlin.inf.focustracker.jobs.RefreshViewsJob;
import de.fu_berlin.inf.focustracker.repository.Element;
import de.fu_berlin.inf.focustracker.repository.InteractionRepository;


public class FocussedElementsView extends BeanView {

	public static final String ID = "de.fu_berlin.inf.focustracker.views.FocussedElementsView";
	private RefreshViewsJob refreshJob;

	@Override
	public Class getClazz() {
		return Element.class;
	}
	
	@Override
	public void createPartControl(Composite aParent) {
		super.createPartControl(aParent);
		refreshJob = new RefreshViewsJob(this);
		getViewer().setSorter(
				new ViewerSorter() {
					Comparator<Element> comparator = new Comparator<Element>() {
						public int compare(Element aO1, Element aO2) {
							if (aO1 != null && aO2 != null) {
								if (aO1.getRating() >= aO2.getRating()) {
									return -1;
								}
								else { 
									return 1;
								}
							}
							return 0;
						}
					};
					
					@Override
					public int compare(Viewer viewer, Object e1, Object e2) {
						return comparator.compare((Element)e1, (Element)e2);
					}
				}
			);
		EventDispatcher.getInstance().addListener(this);
		makeActions();
	}

	@Override
	public Object[] getObjects() {
//		return InteractionRepository.getInstance().getJavaElements();
//		return InteractionRepository.getInstance().getElements().values().toArray();
		return InteractionRepository.getInstance().getElementsWithRating().toArray();
	}
	
	protected void makeActions() {
		super.makeActions();
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

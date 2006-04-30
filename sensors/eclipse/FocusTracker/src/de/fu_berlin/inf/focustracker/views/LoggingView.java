package de.fu_berlin.inf.focustracker.views;

import java.util.Comparator;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.widgets.Composite;

import de.fu_berlin.inf.focustracker.EventDispatcher;
import de.fu_berlin.inf.focustracker.interaction.Interaction;
import de.fu_berlin.inf.focustracker.interaction.JavaInteraction;
import de.fu_berlin.inf.focustracker.repository.InteractionRepository;


public class LoggingView extends BeanView {

	public static final String ID = "de.fu_berlin.inf.focustracker.views.LoggingView";
	
	@Override
	public Class getClazz() {
		return JavaInteraction.class;
	}

	@Override
	public void createPartControl(Composite aParent) {
		super.createPartControl(aParent);
		getViewer().setSorter(
				new ViewerSorter() {
					Comparator<Interaction> comparator = new Comparator<Interaction>() {
						public int compare(Interaction aO1, Interaction aO2) {
							if (aO1 != null && aO2 != null) {
								return (int)(aO1.getDate().getTime() - aO2.getDate().getTime());
							}
							return 0;
						}
					};
					@Override
					public int compare(Viewer viewer, Object e1, Object e2) {
						return comparator.compare((Interaction)e1, (Interaction)e2);
					}
				}
			);
		EventDispatcher.getInstance().addListener(this);
	}
	
	@Override
	public Object[] getObjects() {
		return InteractionRepository.getInstance().getAllInteractions().toArray();
//		return new Object[] {
//				new SystemInteraction(Action.WINDOW_DEACTIVATED, 0.0f, null, null, null),
//				new SystemInteraction(Action.WINDOW_DEACTIVATED, 0.0f, null, null, null),
//				new SystemInteraction(Action.WINDOW_DEACTIVATED, 0.0f, null, null, null),
//				} ;
	}
}

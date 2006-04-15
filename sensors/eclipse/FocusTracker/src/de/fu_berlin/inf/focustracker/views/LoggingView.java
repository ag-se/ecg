package de.fu_berlin.inf.focustracker.views;

import org.eclipse.swt.widgets.Composite;

import de.fu_berlin.inf.focustracker.interaction.JavaInteraction;
import de.fu_berlin.inf.focustracker.jobs.RefreshViewsJob;
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
		new RefreshViewsJob(this);
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

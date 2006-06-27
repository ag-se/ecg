package de.fu_berlin.inf.focustracker.ui;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import de.fu_berlin.inf.focustracker.EventDispatcher;
import de.fu_berlin.inf.focustracker.interaction.Interaction;
import de.fu_berlin.inf.focustracker.interaction.InteractionListener;
import de.fu_berlin.inf.focustracker.repository.InteractionRepository;

public class FocusTrackerDecorator extends LabelProvider implements ILightweightLabelDecorator, InteractionListener {

	public static final String ID = "de.fu_berlin.inf.focustracker.ui.FocusTrackerDecorator"; 
	
	public static final Font BOLD = JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT);
	
	private DecimalFormat decimalFormat = new DecimalFormat("0.00", new DecimalFormatSymbols(Locale.US)); 
	
	public FocusTrackerDecorator() {
		// must use asyncExec, because otherwise the listener is added to soon.
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {
				EventDispatcher.getInstance().addListener(FocusTrackerDecorator.this);
			}
		});
	}

	public void decorate(Object aElement, IDecoration aDecoration) {
		if(aElement instanceof IJavaElement) {
			double lastScore = InteractionRepository.getInstance().getRating((IJavaElement)aElement);
			if(lastScore == 0) {
				return;
			}
			if(lastScore > 0.5d) {
				aDecoration.setFont(BOLD);
			}
			aDecoration.addSuffix(" [" + decimalFormat.format(lastScore) + "]");
		}
	}

	public void dispose() {
		EventDispatcher.getInstance().removeListener(this);
	}

	public boolean isLabelProperty(Object element, String property) {
		return true;
	}

	public void notifyInteractionObserved(List<? extends Interaction> aInteractions) {
		postLabelEvent(new LabelProviderChangedEvent(FocusTrackerDecorator.this, InteractionRepository.getInstance().getElements().keySet().toArray()));
	}
	
	/**
	 * Post the label event to the UI thread
	 *
	 * @param events  the events to post
	 */
	private void postLabelEvent(final LabelProviderChangedEvent event) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				fireLabelProviderChanged(event);
			}
		});
	}
	
	
}
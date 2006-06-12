package de.fu_berlin.inf.focustracker.rating.event;

import junit.framework.TestCase;
import de.fu_berlin.inf.focustracker.interaction.Action;
import de.fu_berlin.inf.focustracker.interaction.JavaInteraction;
import de.fu_berlin.inf.focustracker.interaction.Origin;
import de.fu_berlin.inf.focustracker.rating.Rating;

public class TestRules extends TestCase {
	
	public void testRateEditorSelectionEvent() throws Exception {
		Rating rating = new Rating();
		double result = rating.rateEvent(
				new EditorSelectionEvent(
						Action.SELECTED, 
						new ElementRegion(0, 100, 100, 1000),
						new JavaInteraction(Action.SELECTED, null, 0, Origin.JAVAEDITOR)));
		
		System.err.println("Result : " + result);
	}
	
	public void testRateElementVisibiltyEvent() throws Exception {
		Rating rating = new Rating();
		double result = rating.rateEvent(
				new ElementVisibiltyEvent(
						Action.VISIBILITY_GAINED,
						null,
						true,
						true,
						new ElementRegion(0, 100, 100, 1000),
						5,
						new JavaInteraction(Action.SELECTED, null, 0, Origin.JAVAEDITOR)));
		
		System.err.println("Result : " + result);
	}
	
	public void testRateFoldingExpanded() throws Exception {
		Rating rating = new Rating();
		double result = rating.rateEvent(
				new ElementFoldingEvent(
						Action.FOLDING_EXPANDED,
						null,
						true,
						new JavaInteraction(Action.SELECTED, null, 0, Origin.JAVAEDITOR)));
		
		System.err.println("Result : " + result);
	}
	
	public void testRateFoldingCollapsed() throws Exception {
		Rating rating = new Rating();
		double result = rating.rateEvent(
				new ElementFoldingEvent(
						Action.FOLDING_COLLAPSED,
						null,
						true,
						new JavaInteraction(Action.SELECTED, null, 0, Origin.JAVAEDITOR)));
		
		System.err.println("Result : " + result);
	}
}

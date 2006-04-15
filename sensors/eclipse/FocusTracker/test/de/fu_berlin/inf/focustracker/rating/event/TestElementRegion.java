package de.fu_berlin.inf.focustracker.rating.event;

import de.fu_berlin.inf.focustracker.rating.event.ElementRegion;
import junit.framework.TestCase;

public class TestElementRegion extends TestCase {

	
	public void testGetPercentageVisible() throws Exception {
		ElementRegion region = new ElementRegion(50, 30, 30, 70);
		System.err.println(region.getPercentageVisible());
		
		region = new ElementRegion(10, 70, 30, 20);
		System.err.println(region.getPercentageVisible());
	}
}

package de.fu_berlin.inf.focustracker.test;

import junit.framework.TestCase;

import org.eclipse.jdt.internal.ui.JavaPlugin;

import de.fu_berlin.inf.focustracker.FocusTrackerPlugin;

public abstract class AbstractPluginTestCase extends TestCase {

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		assertNotNull(JavaPlugin.getDefault());
		assertNotNull(FocusTrackerPlugin.getDefault());
	}
	
}

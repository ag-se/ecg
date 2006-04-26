package de.fu_berlin.inf.focustracker.monitor;

import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.internal.ui.packageview.PackageExplorerPart;

import de.fu_berlin.inf.focustracker.test.AbstractPluginTestCase;

public class TestJavaEditorMonitor extends AbstractPluginTestCase {

	public void testInit() {
		PackageExplorerPart.openInActivePerspective();
//		new CompilationUnitEditor().
	}
}

package de.fu_berlin.inf.focustracker.interaction;

import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.internal.ui.packageview.PackageExplorerPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.internal.console.ConsoleView;
import org.eclipse.ui.views.contentoutline.ContentOutline;

public enum Origin {
		UNKNOWN, 
		JAVAEDITOR, 
		JAVA_CLASSFILE_EDITOR, 
		CONSOLE, 
		OUTLINE, 
		WINDOW_SYSTEM, 
		MONITOR_ACTIVIY, 
		PACKAGE_EXPLORER;
		
		public static Origin getOrigin(IWorkbenchPart aPart) {
			if (aPart instanceof ContentOutline) {
				return OUTLINE;
			} else if (aPart instanceof JavaEditor) {
				return JAVAEDITOR;
			} else if (aPart instanceof ConsoleView) {
				return CONSOLE;
			} else if (aPart instanceof PackageExplorerPart) {
				return PACKAGE_EXPLORER;
			}
			return UNKNOWN;
		}
}

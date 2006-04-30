package de.fu_berlin.inf.focustracker.monitor;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.ui.packageview.PackageExplorerPart;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.ui.IWorkbenchPart;

import de.fu_berlin.inf.focustracker.EventDispatcher;
import de.fu_berlin.inf.focustracker.interaction.Action;
import de.fu_berlin.inf.focustracker.interaction.JavaInteraction;
import de.fu_berlin.inf.focustracker.interaction.Origin;

public class PackageExplorerExpansionMonitor extends AbstractFocusTrackerMonitor implements ITreeViewerListener {

	private PackageExplorerPart part;

	public void deregisterFromPart() {
		part.getTreeViewer().removeTreeListener(this);
	}

	public void registerPart(IWorkbenchPart aPart) {
		if (aPart instanceof PackageExplorerPart) {
			part = (PackageExplorerPart) aPart;
			part.getTreeViewer().addTreeListener(this);
//			part.getTreeViewer().getControl().addMouseMoveListener(new KeyAndMouseListener());
		} else {
			throw new IllegalArgumentException("Wrong type of part : " + aPart.getClass());
		}
	}

	public void treeCollapsed(TreeExpansionEvent aEvent) {
		// TODO: add rating !
		if (aEvent.getElement() instanceof IJavaElement) {
			IJavaElement javaElement = (IJavaElement) aEvent.getElement();
			JavaInteraction javaInteraction = new JavaInteraction(Action.COLLAPSED, javaElement, 0d, Origin.PACKAGE_EXPLORER);
			EventDispatcher.getInstance().notifyInteractionObserved(javaInteraction);
		}
	}

	public void treeExpanded(TreeExpansionEvent aEvent) {
		if (aEvent.getElement() instanceof IJavaElement) {
			IJavaElement javaElement = (IJavaElement) aEvent.getElement();
			JavaInteraction javaInteraction = new JavaInteraction(Action.EXPANDED, javaElement, 1d, Origin.PACKAGE_EXPLORER);
			EventDispatcher.getInstance().notifyInteractionObserved(javaInteraction);
		}
	}

}

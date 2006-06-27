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
import de.fu_berlin.inf.focustracker.rating.RatingException;
import de.fu_berlin.inf.focustracker.rating.event.ElementFoldingEvent;

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
		if (aEvent.getElement() instanceof IJavaElement) {
			IJavaElement javaElement = (IJavaElement) aEvent.getElement();

			try {
				ElementFoldingEvent foldingEvent = new ElementFoldingEvent(Action.COLLAPSED, javaElement, true, null, Origin.PACKAGE_EXPLORER);
				JavaInteraction javaInteraction = new JavaInteraction(Action.COLLAPSED, javaElement, EventDispatcher.getInstance().getRating().rateEvent(foldingEvent), Origin.PACKAGE_EXPLORER);
				EventDispatcher.getInstance().notifyInteractionObserved(javaInteraction);
			} catch (RatingException e) {
				e.printStackTrace();
			}
		}
	}

	public void treeExpanded(TreeExpansionEvent aEvent) {
		if (aEvent.getElement() instanceof IJavaElement) {
			IJavaElement javaElement = (IJavaElement) aEvent.getElement();
			try {
				ElementFoldingEvent foldingEvent = new ElementFoldingEvent(Action.EXPANDED, javaElement, false, null, Origin.PACKAGE_EXPLORER);
				JavaInteraction javaInteraction = new JavaInteraction(Action.EXPANDED, javaElement, EventDispatcher.getInstance().getRating().rateEvent(foldingEvent), Origin.PACKAGE_EXPLORER);
				EventDispatcher.getInstance().notifyInteractionObserved(javaInteraction);
			} catch (RatingException e) {
				e.printStackTrace();
			}
			
		}
	}

}

package de.fu_berlin.inf.focustracker.monitor;

import java.util.Date;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.contentoutline.ContentOutline;

import de.fu_berlin.inf.focustracker.EventDispatcher;
import de.fu_berlin.inf.focustracker.interaction.Action;
import de.fu_berlin.inf.focustracker.interaction.JavaInteraction;
import de.fu_berlin.inf.focustracker.interaction.Origin;


public class OutlineMonitor extends AbstractFocusTrackerMonitor implements ISelectionChangedListener{

	JavaEditor editor;
	private ContentOutline part;
	protected StructuredSelection currentSelection = null;
//	private Tree tree;
	
	public synchronized void selectionChanged(SelectionChangedEvent aEvent) {

		try {
//			System.err.println("outline: " + aEvent.getSource());
//			IJavaElement selectedElement = null;
			if (aEvent.getSelection() instanceof StructuredSelection) {
				StructuredSelection structuredSelection = (StructuredSelection) aEvent.getSelection();

				if (structuredSelection.equals(currentSelection)) {
					return;
				}
				currentSelection = structuredSelection;
				
				
				Object selectedObject = structuredSelection.getFirstElement();
				if (selectedObject instanceof IJavaElement && !JavaEditorMonitor.ignoreElement((IJavaElement)selectedObject)) {
					// rate selection
					JavaInteraction interaction = new JavaInteraction(Action.SELECTION_CHANGED, (IJavaElement)selectedObject, 0.5d, new Date(), null, Origin.OUTLINE);
					EventDispatcher.getInstance().notifyInteractionObserved(interaction);
				}
			}
			
//			IJavaElement javaElement = SelectionConverter.resolveEnclosingElement((JavaEditor) editor, (ITextSelection)aEvent.getSelection());

		} catch (Throwable e) {
			e.printStackTrace();
		}
		
	}

	public void deregisterFromPart() {
		part.removeSelectionChangedListener(this);
	}

	public void registerPart(IWorkbenchPart aPart) {
		if (aPart instanceof ContentOutline) {
			part = (ContentOutline)aPart;
			part.addSelectionChangedListener(this);
//			System.err.println(part.getCurrentPage().getControl());
//			if(part.getCurrentPage().getControl() instanceof Tree) {
//				tree = (Tree)part.getCurrentPage().getControl();
//			}
//			part.getCurrentPage().getControl().addMouseMoveListener(new KeyAndMouseListener());
		} else {
			throw new IllegalArgumentException("Wrong type of part : " + aPart.getClass());
		}
	}

}

package de.fu_berlin.inf.focustracker.monitor;

import java.util.Date;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.contentoutline.ContentOutline;

import de.fu_berlin.inf.focustracker.EventDispatcher;
import de.fu_berlin.inf.focustracker.interaction.Action;
import de.fu_berlin.inf.focustracker.interaction.JavaInteraction;
import de.fu_berlin.inf.focustracker.interaction.Origin;


public class OutlineMonitor implements ISelectionChangedListener, IFocusTrackerMonitor {

	JavaEditor editor;
	private ContentOutline part;
	protected StructuredSelection currentSelection = null;
	private Tree tree;
	
//	public JavaEditorMonitor() {
//		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
//		// update editors that are already opened
//		if (PlatformUI.getWorkbench().getActiveWorkbenchWindow() != null) {
//			if (page != null) {
//				IEditorReference[] references = page.getEditorReferences();
//				for (int i = 0; i < references.length; i++) {
//					IEditorPart part = references[i].getEditor(false);
//					if (part != null && part instanceof JavaEditor) {
//						System.err.println("JavaEditor found !");
//						JavaEditor editor = (JavaEditor) part;
//						editor.getSelectionProvider().addSelectionChangedListener(this);
//						System.err.println("docProvider: " + ((CompilationUnitDocumentProvider)editor.getDocumentProvider()));
//						editor.getViewer().addViewportListener(this);
//						editor.getViewer().addTextListener(this);
//						
////						editor.getSite().getWorkbenchWindow(). getShell().addMouseMoveListener(this);
//						
////						editor.getDocumentProvider().
////						editorTracker.registerEditor(editor);
////						ActiveFoldingListener.resetProjection(editor);
//						
//					}
//				}
//			}
//		}		
//	}

	public synchronized void selectionChanged(SelectionChangedEvent aEvent) {

		try {
			System.err.println("outline: " + aEvent.getSource());
//			IJavaElement selectedElement = null;
			if (aEvent.getSelection() instanceof StructuredSelection) {
				StructuredSelection structuredSelection = (StructuredSelection) aEvent.getSelection();

				if (structuredSelection.equals(currentSelection)) {
					return;
				}
				currentSelection = structuredSelection;
				
				
				Object selectedObject = structuredSelection.getFirstElement();
				if (selectedObject instanceof IJavaElement) {
					// rate selection
					JavaInteraction interaction = new JavaInteraction(Action.SELECTION_CHANGED, (IJavaElement)selectedObject, 1f, new Date(), null, Origin.OUTLINE);
					EventDispatcher.getInstance().notifyInteractionObserved(interaction);
				}
			}
			
//			IJavaElement javaElement = SelectionConverter.resolveEnclosingElement((JavaEditor) editor, (ITextSelection)aEvent.getSelection());

		} catch (Throwable e) {
			e.printStackTrace();
		}
		
	}

//	private List<IJavaElement> findNeighbours(IJavaElement aJavaElement) throws JavaModelException {
//		List<IJavaElement> elements = Arrays.asList(((JavaElement)aJavaElement.getParent()).getChildren());
//		List<IJavaElement> neighbours = new ArrayList<IJavaElement>();
//		
//		int elementIndex = elements.indexOf(aJavaElement);  
//		if(elementIndex > 0) {
//			neighbours.add(elements.get(elementIndex - 1));
//		}
//		if(elementIndex < elements.size() - 1) {
//			neighbours.add(elements.get(elementIndex + 1));
//		}
////		System.err.println("neighbours of: " + aJavaElement + " - " + neighbours.toString());
////		new Exception().printStackTrace();
//		return neighbours;
//		
//	}
	
	public void deregisterFromPart() {
		part.removeSelectionChangedListener(this);
	}

	public void registerPart(IWorkbenchPart aPart) {
		if (aPart instanceof ContentOutline) {
			part = (ContentOutline)aPart;
			part.addSelectionChangedListener(this);
			System.err.println(part.getCurrentPage().getControl());
			if(part.getCurrentPage().getControl() instanceof Tree) {
				tree = (Tree)part.getCurrentPage().getControl();
//				System.err.println(tree.getParent());
			}
//			part.getCurrentPage().getControl().addMouseMoveListener(new KeyAndMouseListener());
		} else {
			throw new IllegalArgumentException("Wrong type of part : " + aPart.getClass());
		}
	}

}

package de.fu_berlin.inf.focustracker.monitor;

import java.util.Date;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.actions.SelectionConverter;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.internal.ui.packageview.PackageExplorerPart;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.console.ConsoleView;

import de.fu_berlin.inf.focustracker.EventDispatcher;
import de.fu_berlin.inf.focustracker.interaction.Action;
import de.fu_berlin.inf.focustracker.interaction.JavaInteraction;
import de.fu_berlin.inf.focustracker.interaction.Origin;
import de.fu_berlin.inf.focustracker.monitor.helper.RegionHelper;
import de.fu_berlin.inf.focustracker.rating.RatingException;
import de.fu_berlin.inf.focustracker.rating.event.EditorSelectionEvent;
import de.fu_berlin.inf.focustracker.rating.event.ElementRegion;
import de.fu_berlin.inf.focustracker.repository.InteractionRepository;


public class SelectionMonitor implements ISelectionListener {

	private IJavaElement oldSelection = null;
	private InteractionRepository repository = InteractionRepository.getInstance();

	public SelectionMonitor() {
		
	}
	public void selectionChanged(IWorkbenchPart aPart, ISelection aSelection) {
//	public synchronized void selectionChanged(SelectionChangedEvent aEvent) {

		if(aPart instanceof JavaEditor) {
			handleJavaEditorSelection(aPart, aSelection);
		} else {
		
			if(aPart instanceof ConsoleView) {
	//			JavaInteraction interaction = new JavaInteraction(Action.SELECTED, javaElement, EventDispatcher.getInstance().getRating().rateEvent(editorSelectionEvent), new Date(), null, Origin.JAVAEDITOR);
	//			EventDispatcher.getInstance().notifyInteractionObserved(interaction);
	//
				System.err.println("console: " + ((TextSelection)aSelection).getText());
			}
			
			if(aSelection instanceof StructuredSelection) {
				StructuredSelection structuredSelection = (StructuredSelection) aSelection;
				Object selectedObject = structuredSelection.getFirstElement();
				if (selectedObject == null)
					return;
				if (selectedObject instanceof IJavaElement) {
					IJavaElement javaElement = (IJavaElement) selectedObject;
					JavaInteraction javaInteraction = new JavaInteraction(getAction(javaElement), javaElement, 1d, Origin.getOrigin(aPart));
					EventDispatcher.getInstance().notifyInteractionObserved(javaInteraction);
					oldSelection = javaElement;
				}				
			} else {
				System.err.println("selectionChanged in " + aPart + " selection: " + aSelection);
			}
		}
		
	}
	private void handleJavaEditorSelection(IWorkbenchPart aPart, ISelection aSelection) {
//		IEditorPart editorPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
//		JavaEditor editor = (CompilationUnitEditor) editorPart;
		JavaEditor editor = (CompilationUnitEditor) aPart;
//		System.err.println("source: " + aEvent.getSource());
		try {
			IJavaElement javaElement = SelectionConverter.resolveEnclosingElement((JavaEditor) editor, (ITextSelection)aSelection);

			// rate neighbours
//			List<IJavaElement> neighbours = findNeighbours(javaElement);
//			for (IJavaElement neighbourElement : neighbours) {
//				float lastScore = InteractionRepository.getInstance().getLastScore(neighbourElement);
//				float newScore = SeverityHelper.calculateSeverity(Action.NEIGHBOUR_SELECTED, lastScore);
//				JavaInteraction neighbourInteraction = new JavaInteraction(Action.NEIGHBOUR_SELECTED, neighbourElement, newScore, new Date(), null, Origin.JAVAEDITOR);
//				EventDispatcher.getInstance().notifyInteractionObserved(neighbourInteraction);
//			}

//			// checks if the selection has changed
//			if(oldSelection != null && !oldSelection.equals(javaElement)) {
//				JavaInteraction interaction = new JavaInteraction(Action.DESELECTED, oldSelection, 0f, new Date(), null, Origin.JAVAEDITOR);
//				EventDispatcher.getInstance().notifyInteractionObserved(interaction);
//			}
			
//			}
//			if(editor.getViewer().getRangeIndication() != null) {
//				System.err.println("rangeind offset: " + editor.getViewer().getRangeIndication().getOffset() + " length: " + editor.getViewer().getRangeIndication().getLength());
//			}
			// number of chars in the visible region
//			System.err.println("indexoffset: " + editor.getViewer().getTopIndexStartOffset() + " - " + editor.getViewer().getBottomIndexEndOffset() + " - topline: " + editor.getViewer().getTopIndex() + " topoffset: " + lineInformationTop.getOffset() + " bottomoffset: " + lineInformationBottom.getOffset() );

			
			// number of rows in the visible region
//			System.err.println("index: " + editor.getViewer().getTopIndex() + " - " + editor.getViewer().getBottomIndex()); 
			
//--			System.err.println("visible offset: " + editor.getViewer().getVisibleRegion().getOffset() + " length: " + editor.getViewer().getVisibleRegion().getLength());
//--			System.err.println("element offset: " + ((ISourceReference)javaElement).getSourceRange().getOffset() + " length: " + ((ISourceReference)javaElement).getSourceRange().getLength());
//--			System.err.println("element source: " + ((ISourceReference)javaElement).getSource());
//			System.err.println("element offset: " + ((ISourceReference)javaElement).getSourceRange().getOffset() + " length: " + ((ISourceReference)javaElement).getSourceRange().getLength());
			
			
			
			// rate selection
			Action action = getAction(javaElement);
			
			
//			System.err.println("--------- collapsed? " + ((JavaEditorMonitor)EventDispatcher.getInstance().getPartMonitor().getMonitorInstance(editor)).getFoldingListener().isCollapsed(javaElement));
			
			ElementRegion elementRegion = RegionHelper.getElementRegion(editor, javaElement);
//			System.err.println("sel-vis: " + elementRegion.getPercentageVisible());
			
			JavaInteraction lastInteraction = repository.getLastInteraction(javaElement);
			EditorSelectionEvent editorSelectionEvent = new EditorSelectionEvent(
					action,
					elementRegion,
					lastInteraction
					);
			
			JavaInteraction interaction = new JavaInteraction(action, javaElement, EventDispatcher.getInstance().getRating().rateEvent(editorSelectionEvent), new Date(), null, Origin.JAVAEDITOR);
			EventDispatcher.getInstance().notifyInteractionObserved(interaction);
			oldSelection = javaElement;
			
		} catch (JavaModelException e) {
			e.printStackTrace();
		} catch (RatingException e) {
			e.printStackTrace();
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		
		// check if other methods could be in the focus, too
//		javaEditor.getViewer().
		
//		System.err.println("editor top: " + ((ISourceViewer)aEvent.getSource()).getTopIndex());
	}
	private Action getAction(IJavaElement javaElement) {
		Action action = Action.SELECTED;
		if(oldSelection == javaElement) {
			action = Action.SELECTION_SAME_ELEMENT;
		}
		return action;
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
//		System.err.println("neighbours of: " + aJavaElement + " - " + neighbours.toString());
////		new Exception().printStackTrace();
//		return neighbours;
//		
//	}
}


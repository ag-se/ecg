package de.fu_berlin.inf.focustracker.monitor;

import java.util.Date;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;

import de.fu_berlin.inf.focustracker.EventDispatcher;
import de.fu_berlin.inf.focustracker.interaction.Action;
import de.fu_berlin.inf.focustracker.interaction.JavaInteraction;
import de.fu_berlin.inf.focustracker.interaction.Origin;


public class OldSelectionMonitor implements ISelectionListener {

	private IJavaElement oldSelection = null;
	
	
	public void selectionChanged(IWorkbenchPart aPart, ISelection aSelection) {
//		String structureKind = InteractionEvent.ID_UNKNOWN;
//		String obfuscatedElementHandle = InteractionEvent.ID_UNKNOWN;
//		String elementHandle = InteractionEvent.ID_UNKNOWN;
//		InteractionEvent.Kind interactionKind = InteractionEvent.Kind.SELECTION;
		IJavaElement javaElement = null;
		Origin origin = Origin.getOrigin(aPart);
		
		if (aSelection instanceof StructuredSelection) {
			StructuredSelection structuredSelection = (StructuredSelection) aSelection;
			Object selectedObject = structuredSelection.getFirstElement();
			if (selectedObject == null)
				return;
			if (selectedObject instanceof IJavaElement) {
				javaElement = (IJavaElement) selectedObject;
//				structureKind = "java:" + javaElement.getClass();
//				elementHandle = javaElement.getHandleIdentifier();
//				obfuscatedElementHandle = obfuscateJavaElementHandle(javaElement);
//				lastSelectedElement = javaElement;
			} 
//			else {
//				structureKind = InteractionEvent.ID_UNKNOWN + ": " + selectedObject.getClass();
//				if (selectedObject instanceof IAdaptable) {
//					IResource resource = (IResource) ((IAdaptable) selectedObject).getAdapter(IResource.class);
//					if (resource != null) {
//						obfuscatedElementHandle = obfuscateResourcePath(resource.getProjectRelativePath());
//					}
//				}
//			}
			
		} 
		// this should be handled by the JavaEditorMonitor!
//		else {
//			if (aSelection instanceof TextSelection && aPart instanceof JavaEditor) {
//				TextSelection textSelection = (TextSelection) aSelection;
////				IJavaElement javaElement;
//				try {
//					javaElement = SelectionConverter.resolveEnclosingElement((JavaEditor) aPart, textSelection);
//					if (javaElement != null) {
////						structureKind = "java:" + javaElement.getClass();
////						obfuscatedElementHandle = obfuscateJavaElementHandle(javaElement);
////						elementHandle = javaElement.getHandleIdentifier();
////						if (javaElement != null && javaElement.equals(lastSelectedElement)) {
////							interactionKind = InteractionEvent.Kind.EDIT;
////						}
////						lastSelectedElement = javaElement;
//					}
//				} catch (JavaModelException e) {
//					// ignore unresolved elements
//					// MylarPlugin.log("Could not resolve java element from text
//					// selection.", this);
//				}
//			} 
////			else if (part instanceof EditorPart) {
////				EditorPart editorPart = (EditorPart) part;
////				IEditorInput input = editorPart.getEditorInput();
////				if (input instanceof IPathEditorInput) {
////					structureKind = "file";
////					obfuscatedElementHandle = obfuscateResourcePath(((IPathEditorInput) input).getPath());
////				}
////			}
//		}
//		IMylarElement node = MylarPlugin.getContextManager().getElement(elementHandle);
//		String delta = "";
//		float selectionFactor = MylarContextManager.getScalingFactors().get(InteractionEvent.Kind.SELECTION).getValue();
//
//		// XXX: broken in 0.4?
//		if (node != null) {
//			if (node.getInterest().getEncodedValue() <= selectionFactor
//					&& node.getInterest().getValue() > selectionFactor) {
//				delta = SELECTION_PREDICTED;
//			} else if (node.getInterest().getEncodedValue() < selectionFactor
//					&& node.getInterest().getDecayValue() > selectionFactor) {
//				delta = SELECTION_DECAYED;
//			} else if (node.getInterest().getValue() == selectionFactor
//					&& node.getInterest().getDecayValue() < selectionFactor) {
//				delta = SELECTION_NEW;
//			} else {
//				delta = SELECTION_DEFAULT;
//			}
//		}
//
//		InteractionEvent event = new InteractionEvent(interactionKind, structureKind, obfuscatedElementHandle, part
//				.getSite().getId(), "null", delta, 0);
//		MylarPlugin.getDefault().notifyInteractionObserved(event);
		
		if(javaElement == null) {
			return;
		}
		
		if(oldSelection != null && !oldSelection.equals(javaElement)) {
			JavaInteraction interaction = new JavaInteraction(Action.DESELECTED, oldSelection, 0f, new Date(), null, origin);
			EventDispatcher.getInstance().notifyInteractionObserved(interaction);
		}
		oldSelection = javaElement;
		JavaInteraction interaction = new JavaInteraction(Action.SELECTED, javaElement, 1f, new Date(), null, origin);
		EventDispatcher.getInstance().notifyInteractionObserved(interaction);
	}
	
//	public void selectionChanged(IWorkbenchPart aPart, ISelection aSelection) {
//		
//	}

}

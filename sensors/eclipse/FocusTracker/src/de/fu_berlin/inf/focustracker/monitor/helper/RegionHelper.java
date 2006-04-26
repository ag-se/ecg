package de.fu_berlin.inf.focustracker.monitor.helper;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;

import de.fu_berlin.inf.focustracker.rating.event.ElementRegion;

public class RegionHelper {

	
	public static ElementRegion getElementRegion(JavaEditor aJavaEditor, IJavaElement aJavaElement) throws BadLocationException, JavaModelException {
		
		IRegion lineInformationTop = aJavaEditor.getViewer().getDocument().getLineInformation(aJavaEditor.getViewer().getTopIndex());
		IRegion lineInformationBottom = aJavaEditor.getViewer().getDocument().getLineInformation(aJavaEditor.getViewer().getBottomIndex());
		
		if(((ISourceReference)aJavaElement).getSourceRange() == null) {
			return null;
		}
		
		return new ElementRegion(
				((ISourceReference)aJavaElement).getSourceRange().getOffset(), 
				((ISourceReference)aJavaElement).getSourceRange().getLength(), 
//				editor.getViewer().getTopIndexStartOffset(),  // buggy eclipse implementation 
				lineInformationTop.getOffset(),
//				editor.getViewer().getBottomIndexEndOffset()  // buggy eclipse implementation
				// da immer das erste Zeichen der Zeile berechnet wird muss die Zeilenlänge hinzugerechnet werden
				lineInformationBottom.getOffset() - lineInformationTop.getOffset() + aJavaEditor.getViewer().getDocument().getLineLength(aJavaEditor.getViewer().getBottomIndex())
		);
		
	}
	
}

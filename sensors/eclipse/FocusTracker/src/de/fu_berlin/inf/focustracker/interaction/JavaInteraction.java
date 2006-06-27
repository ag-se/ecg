package de.fu_berlin.inf.focustracker.interaction;

import java.util.Date;

import org.eclipse.jdt.core.IJavaElement;

public class JavaInteraction extends Interaction {

	private IJavaElement javaElement;
	
	public JavaInteraction(Action aAction, IJavaElement aJavaElement, double aRating, Date aDate, Origin aOrigin) {
		super(aAction, aRating, aDate, aOrigin);
		javaElement = aJavaElement;
		comment = aAction.toString();
	}

	public JavaInteraction(Action aAction, IJavaElement aJavaElement, double aRating, Origin aOrigin, String aComment) {
		this(aAction, aJavaElement, aRating, aOrigin);
		comment = aComment;
	}	
	public JavaInteraction(Action aAction, IJavaElement aJavaElement, double aRating, Origin aOrigin) {
		this(aAction, aJavaElement, aRating, new Date(), aOrigin);
	}
	
	@Override
	public String toString() {
		return date.getTime() + " : "  + origin + " - " + action + " - " + rating + " - " + JavaElementHelper.toString(javaElement) ;
	}

	public IJavaElement getJavaElement() {
		return javaElement;
	}

	public void setJavaElement(IJavaElement aJavaElement) {
		javaElement = aJavaElement;
	}
	
//	public String getJavaElementFormatted() {
////		return javaElement.getPath() + "#" + javaElement.getPrimaryElement().toString().substring(0, javaElement.getPrimaryElement().toString().indexOf('['));
////		return javaElement.getPath() + "#" + javaElement.getPrimaryElement().toString();
//		return JavaElementHelper.toString(javaElement);
//	}
	
}

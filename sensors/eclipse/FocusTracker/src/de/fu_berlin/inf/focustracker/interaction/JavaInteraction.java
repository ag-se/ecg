package de.fu_berlin.inf.focustracker.interaction;

import java.util.Date;

import org.eclipse.jdt.core.IJavaElement;

public class JavaInteraction extends Interaction {

	private IJavaElement javaElement;
	
	public JavaInteraction(Action aAction, IJavaElement aJavaElement, double aSeverity, Date aDate, Date aEndDate, Origin aOrigin) {
		super(aAction, aSeverity, aDate, aEndDate, aOrigin);
		javaElement = aJavaElement;
	}
	
	public JavaInteraction(Action aAction, IJavaElement aJavaElement, double aSeverity, Origin aOrigin) {
		this(aAction, aJavaElement, aSeverity, new Date(), null, aOrigin);
	}
	@Override
	public String toString() {
		return date + ": "  + origin + " - " + action + " - " + javaElement ;
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
//		return JavaElementToStringBuilder.toString(javaElement);
//	}
	
}

package de.fu_berlin.inf.focustracker.interaction;

import org.eclipse.jdt.core.IJavaElement;

public class ElementScore {

	private IJavaElement javaElement;
	private float score;
	public ElementScore(IJavaElement aJavaElement, float aScore) {
		super();
		javaElement = aJavaElement;
		score = aScore;
	}
	public IJavaElement getJavaElement() {
		return javaElement;
	}
	public void setJavaElement(IJavaElement aJavaElement) {
		javaElement = aJavaElement;
	}
	public float getScore() {
		return score;
	}
	public void setScore(float aScore) {
		score = aScore;
	}
	
	@Override
	public String toString() {
		return getElementAndScore();
	}
	
	public String getElementAndScore() {
		return "(" + score + ") " + javaElement.getPrimaryElement(); 
	}
}

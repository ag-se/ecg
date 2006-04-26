package de.fu_berlin.inf.focustracker.repository;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.IJavaElement;

import de.fu_berlin.inf.focustracker.interaction.JavaInteraction;

public class Element implements Comparable<Element> {

	private IJavaElement javaElement;
	private double rating;
	private List<JavaInteraction> interactions = new ArrayList<JavaInteraction>();
	
	public Element(IJavaElement aJavaElement, double aRating) {
		super();
		javaElement = aJavaElement;
		rating = aRating;
	}
	
	public double getRating() {
		return rating;
	}
	public void setRating(double aRating) {
		rating = aRating;
	}
	public IJavaElement getJavaElement() {
		return javaElement;
	}

	public JavaInteraction getLastInteraction() {
		if(interactions.size() > 0) {
			return interactions.get(interactions.size() - 1);
		} else {
			return null;
		}
	}
	
	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((javaElement == null) ? 0 : javaElement.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final Element other = (Element) obj;
		if (javaElement == null) {
			if (other.javaElement != null)
				return false;
		} else if (!javaElement.equals(other.javaElement))
			return false;
		return true;
	}

	public List<JavaInteraction> getInteractions() {
		return interactions;
	}

	public int compareTo(Element aO) {
		return (int)(rating - aO.rating);
	}
	
}

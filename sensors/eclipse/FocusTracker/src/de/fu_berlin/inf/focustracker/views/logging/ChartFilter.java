package de.fu_berlin.inf.focustracker.views.logging;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.IJavaElement;

public class ChartFilter {

	private List<IJavaElement> javaElements = new ArrayList<IJavaElement>();

	public List<IJavaElement> getJavaElements() {
		return javaElements;
	}

	public void setJavaElements(List<IJavaElement> aJavaElements) {
		javaElements = aJavaElements;
	}

}

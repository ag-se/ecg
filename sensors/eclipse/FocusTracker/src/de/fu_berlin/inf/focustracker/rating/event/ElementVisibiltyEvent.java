package de.fu_berlin.inf.focustracker.rating.event;

import org.eclipse.jdt.core.IJavaElement;

import de.fu_berlin.inf.focustracker.interaction.Action;
import de.fu_berlin.inf.focustracker.interaction.JavaInteraction;

public class ElementVisibiltyEvent implements EventHolder {

	private Action action;
	private IJavaElement javaElement;
	private boolean visible;
	private boolean collapsed;
	private ElementRegion elementRegion;
	private JavaInteraction lastInteraction;
	private int numberOfElementsVisible;
	
	public ElementVisibiltyEvent(
			Action aAction,
			IJavaElement aJavaElement,
			boolean aVisible,
			boolean aCollapsed,
			ElementRegion aElementRegion, 
			int aNumberOfElementsVisible, 
			JavaInteraction aLastInteraction) {
		
		action = aAction;
		javaElement = aJavaElement;
		visible = aVisible;
		collapsed = aCollapsed;
		elementRegion = aElementRegion;
		numberOfElementsVisible = aNumberOfElementsVisible;
		lastInteraction = aLastInteraction;
		
	}
	
	public Action getAction() {
		return action;
	}
	public void setAction(Action aAction) {
		action = aAction;
	}

	public JavaInteraction getLastInteraction() {
		return lastInteraction;
	}
	
	public void setLastInteraction(JavaInteraction aLastInteraction) {
		lastInteraction = aLastInteraction;
	}

	public IJavaElement getJavaElement() {
		return javaElement;
	}

	public void setJavaElement(IJavaElement aJavaElement) {
		javaElement = aJavaElement;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean aVisible) {
		visible = aVisible;
	}

	public boolean isCollapsed() {
		return collapsed;
	}

	public void setCollapsed(boolean aCollapsed) {
		collapsed = aCollapsed;
	}

	public ElementRegion getElementRegion() {
		return elementRegion;
	}

	public void setElementRegion(ElementRegion aElementRegion) {
		elementRegion = aElementRegion;
	}

	public int getNumberOfElementsVisible() {
		return numberOfElementsVisible;
	}

	public void setNumberOfElementsVisible(int aNumberOfElementsVisible) {
		numberOfElementsVisible = aNumberOfElementsVisible;
	}
	
	
	
}

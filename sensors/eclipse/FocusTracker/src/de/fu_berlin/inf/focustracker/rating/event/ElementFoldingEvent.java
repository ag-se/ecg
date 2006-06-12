package de.fu_berlin.inf.focustracker.rating.event;

import org.eclipse.jdt.core.IJavaElement;

import de.fu_berlin.inf.focustracker.interaction.Action;
import de.fu_berlin.inf.focustracker.interaction.JavaInteraction;

public class ElementFoldingEvent extends EventHolder {

	private Action action;
	private IJavaElement javaElement;
	private boolean collapsed;
	private JavaInteraction lastInteraction;
	
	
	public ElementFoldingEvent(
			Action aAction,
			IJavaElement aJavaElement,
			boolean aCollapsed,
			JavaInteraction aLastInteraction) {
		
		action = aAction;
		javaElement = aJavaElement;
		collapsed = aCollapsed;
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

	public boolean isCollapsed() {
		return collapsed;
	}

	public void setCollapsed(boolean aCollapsed) {
		collapsed = aCollapsed;
	}
	
	
	
}

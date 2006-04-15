package de.fu_berlin.inf.focustracker.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jdt.core.IJavaElement;

import de.fu_berlin.inf.focustracker.interaction.Interaction;
import de.fu_berlin.inf.focustracker.interaction.JavaInteraction;


public class InteractionRepository {

	private static InteractionRepository instance;
	private HashMap<IJavaElement, List<JavaInteraction>> elements = new HashMap<IJavaElement, List<JavaInteraction>>();
	private List<Interaction> allInteractions = new ArrayList<Interaction>();
	private IJavaElement lastVisitedJavaElement;
	
	private InteractionRepository() {};

	public static InteractionRepository getInstance() {
		if(instance == null) {
			instance = new InteractionRepository();
		}
		return instance;
	}
	
	
	public boolean add(Interaction aInteraction) {
		allInteractions.add(aInteraction);
		if (aInteraction instanceof JavaInteraction) {
			return add((JavaInteraction)aInteraction);
		} else {
			return false;
		}
	}
	
	private boolean add(JavaInteraction aJavaInteraction) {
		boolean createdNewElement = false;
		List<JavaInteraction> javaInteractions = elements.get(aJavaInteraction.getJavaElement());
		if(javaInteractions == null) {
			javaInteractions = new ArrayList<JavaInteraction>();
			elements.put(aJavaInteraction.getJavaElement(), javaInteractions);
			createdNewElement = true;
		}
		Interaction lastInteraction = getLastInteraction(aJavaInteraction.getJavaElement());
		aJavaInteraction.setLastInteraction(lastInteraction);
		javaInteractions.add(aJavaInteraction);
		lastVisitedJavaElement = aJavaInteraction.getJavaElement();
		return createdNewElement;
	}
	
	
	public double getLastScore(IJavaElement aJavaElement) {
		List<JavaInteraction> interactions = elements.get(aJavaElement);
		if(interactions != null) {
			return interactions.get(interactions.size() - 1).getSeverity();
		}
		// not rated, yet
		return 0.0d;
	}
	
	public JavaInteraction getLastInteraction(IJavaElement aJavaElement) {
		List<JavaInteraction> interactions = elements.get(aJavaElement);
		if(interactions != null && interactions.size() > 0) {
			return interactions.get(interactions.size() - 1);
		}
		return null;
	}
	
	public IJavaElement[] getJavaElements() {
		return elements.keySet().toArray(new IJavaElement[elements.keySet().size()]);
	}

	public HashMap<IJavaElement, List<JavaInteraction>> getElements() {
		return elements;
	}

	public List<Interaction> getAllInteractions() {
		return allInteractions;
	}

	public IJavaElement getLastVisitedJavaElement() {
		return lastVisitedJavaElement;
	}
	
	public List<JavaInteraction> getAll(Class<? extends IJavaElement> aClazz) {
		List<JavaInteraction> list = new ArrayList<JavaInteraction>();
		for (Interaction interaction : allInteractions) {
			if (interaction instanceof JavaInteraction) {
				JavaInteraction javaInteraction = (JavaInteraction) interaction;
//				if (javaInteraction.getJavaElement().getClass().isAssignableFrom(aClazz)) {
				if (aClazz.isAssignableFrom(javaInteraction.getJavaElement().getClass())) {
					list.add(javaInteraction);
				}
			}
		}
		return list;
	}
	
}

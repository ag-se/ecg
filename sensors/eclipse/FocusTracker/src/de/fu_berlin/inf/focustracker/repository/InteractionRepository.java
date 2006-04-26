package de.fu_berlin.inf.focustracker.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;

import de.fu_berlin.inf.focustracker.interaction.Interaction;
import de.fu_berlin.inf.focustracker.interaction.JavaInteraction;


public class InteractionRepository {

	private static InteractionRepository instance;
//	private HashMap<IJavaElement, List<JavaInteraction>> elements = new HashMap<IJavaElement, List<JavaInteraction>>();
	private HashMap<IJavaElement, Element> elements = new HashMap<IJavaElement, Element>();
	private List<Interaction> allInteractions = new ArrayList<Interaction>();
	private IJavaElement lastVisitedJavaElement;
//	private HashSet<Element> javaElements = new HashSet<Element>();
	
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
		Element element = elements.get(aJavaInteraction.getJavaElement());
		if(element == null) {
			element = new Element(aJavaInteraction.getJavaElement(), aJavaInteraction.getSeverity());
			elements.put(aJavaInteraction.getJavaElement(), element);
			createdNewElement = true;
		}
		Interaction lastInteraction = getLastInteraction(aJavaInteraction.getJavaElement());
		aJavaInteraction.setLastInteraction(lastInteraction);
		element.getInteractions().add(aJavaInteraction);
//		lastVisitedJavaElement = aJavaInteraction.getJavaElement();
//		javaElements.get(aJavaInteraction.getJavaElement())
//		element.addInteraction(aJavaInteraction);
//		javaElements.add(element);
		
//		IType type = (IType)aJavaInteraction.getJavaElement().getAncestor(IJavaElement.TYPE);
//		if(type != null) {
//			System.err.println("######################### " + JavaModelUtil.getFullyQualifiedName(type));
//		} else {
//			System.err.println("type is null: " + aJavaInteraction.getJavaElement());
//		}
		return createdNewElement;
	}
	
	
	public double getLastScore(IJavaElement aJavaElement) {
		Element element = elements.get(aJavaElement);
		if(element != null) {
			return element.getLastInteraction().getSeverity();
		}
		// not rated, yet
		return 0.0d;
	}
	
	public JavaInteraction getLastInteraction(IJavaElement aJavaElement) {
		Element element = elements.get(aJavaElement);
		if(element != null) {
			return element.getLastInteraction();
		}
		return null;
	}
	
//	public IJavaElement[] getJavaElements() {
//		return elements.keySet().toArray(new IJavaElement[elements.keySet().size()]);
//	}

	public IJavaElement[] getRatedJavaElements() {
		List<IJavaElement> ratedElements = new ArrayList<IJavaElement>();
		for (IJavaElement element : elements.keySet()) {
			if(getLastScore(element) > 0d) {
				ratedElements.add(element);
			}
		}
		IJavaElement[] ret = ratedElements.toArray(new IJavaElement[ratedElements.size()]);
//		Arrays.sort(ret, new Comparator<Ijava>);
		
		return ret;
	}
	
	public HashMap<IJavaElement, Element> getElements() {
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

package de.fu_berlin.inf.focustracker.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.core.IJavaElement;

import de.fu_berlin.inf.focustracker.interaction.Interaction;
import de.fu_berlin.inf.focustracker.interaction.JavaInteraction;
import de.fu_berlin.inf.focustracker.interaction.SystemInteraction;
import de.fu_berlin.inf.focustracker.util.Units;


public class InteractionRepository {

	private static final long INACTIVITY_OFFSET = 10 * Units.SECOND;
	
	private static InteractionRepository instance;
//	private HashMap<IJavaElement, List<JavaInteraction>> elements = new HashMap<IJavaElement, List<JavaInteraction>>();
	private HashMap<IJavaElement, Element> elements = new HashMap<IJavaElement, Element>();
	private List<SystemInteraction> systemInteractions = new ArrayList<SystemInteraction>();
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
	
	
	public void add(Interaction aInteraction) {
		allInteractions.add(aInteraction);
		if (aInteraction instanceof JavaInteraction) {
			add((JavaInteraction)aInteraction);
		} else if(aInteraction instanceof SystemInteraction) {
			systemInteractions.add((SystemInteraction)aInteraction);
		}
	}
	
	private void add(JavaInteraction aJavaInteraction) {
//		boolean createdNewElement = false;
		Element element = elements.get(aJavaInteraction.getJavaElement());
		if(element == null) {
			element = new Element(aJavaInteraction.getJavaElement(), aJavaInteraction.getSeverity());
			elements.put(aJavaInteraction.getJavaElement(), element);
		}
		Interaction lastInteraction = getLastInteraction(aJavaInteraction.getJavaElement());
		aJavaInteraction.setLastInteraction(lastInteraction);
		element.getInteractions().add(aJavaInteraction);
		element.setRating(aJavaInteraction.getSeverity());
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
	}
	
	
	public double getRating(IJavaElement aJavaElement) {
		Element element = elements.get(aJavaElement);
		if(element != null) {
			return element.getRating();
		}
		// not rated, yet
		return 0.0d;
	}
	
//	public double getLastScore(IJavaElement aJavaElement) {
//		Element element = elements.get(aJavaElement);
//		if(element != null) {
//			return element.getLastInteraction().getSeverity();
//		}
//		// not rated, yet
//		return 0.0d;
//	}
//	
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
			if(getRating(element) > 0d) {
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

	public List<Element> getElementsWithRating() {
		
		List<Element> elementsWithRating = new ArrayList<Element>();
		for (Element element : elements.values()) {
			if(element.getRating() > 0d) {
				elementsWithRating.add(element);
			}
		}
		return elementsWithRating;
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

	public int removeInteractions(long aOlderThan) {
		
		int removed = 0;
		for (Element element : elements.values()) {
			
			for (Iterator iter = element.getInteractions().iterator(); iter.hasNext();) {
				Interaction interaction = (Interaction) iter.next();
				// if the interaction is older than the given argument, remove it
				if(interaction.getDate().getTime() < aOlderThan) {
					iter.remove();
					removed++;
				}
			}
			// remove element, if it seems to be unimportant
			if(element.getInteractions().size() == 0 && element.getRating() < 0.01d) {
				elements.remove(element);
			}
		}
		
		for (Iterator<SystemInteraction> iter = systemInteractions.iterator(); iter.hasNext();) {
			SystemInteraction interaction = iter.next();
			if(interaction.getDate().getTime() < aOlderThan && interaction.isExported()) {
				iter.remove();
				removed++;
			}
		}
		
		return removed;
	}
	
	
	public void calculateInactivities() {
		for (Element element : elements.values()) {
			calculateInactivity(element);			
		}
	}
	
	/**
	 * after a specified offset time, the rating of an element should decrease 
	 * @param aElement
	 */
	public void calculateInactivity(Element aElement) {
		if(aElement.getRating() == 0d || aElement.getLastInteraction() == null) {
			return;
		}
		long currentTime = System.currentTimeMillis();
		long start = aElement.getLastInteraction().getDate().getTime() + INACTIVITY_OFFSET;
		if(start < currentTime) {
//			System.err.println("decreasing : " + (((currentTime - start) / Units.SECOND) * 0.005));
			aElement.setRating(aElement.getLastInteraction().getSeverity() - ((currentTime - start) / Units.SECOND) * 0.005); 
		}
		
	}

	public List<SystemInteraction> getSystemInteractions() {
		return systemInteractions;
	}	
	
}

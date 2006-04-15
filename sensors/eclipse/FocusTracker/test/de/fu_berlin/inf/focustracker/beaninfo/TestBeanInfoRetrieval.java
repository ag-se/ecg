package de.fu_berlin.inf.focustracker.beaninfo;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;

import de.fu_berlin.inf.focustracker.beaninfo.JavaInteractionBeanInfo;
import de.fu_berlin.inf.focustracker.interaction.JavaInteraction;

import junit.framework.TestCase;


public class TestBeanInfoRetrieval extends TestCase {

	public void testRetrieveBeanInfo() throws Exception {
//		System.err.println("bd: " + new IJavaElementBeanInfo().getBeanDescriptor().getBeanClass());
		System.err.println("bd: " + new JavaInteractionBeanInfo().getBeanDescriptor().getBeanClass());
		
		Introspector.setBeanInfoSearchPath(new String[] { "de.fu_berlin.inf.focustracker.beaninfo" });
		BeanInfo beanInfo = java.beans.Introspector.getBeanInfo(JavaInteraction.class);
		System.err.println(beanInfo.getClass());
		for (PropertyDescriptor descriptor : beanInfo.getPropertyDescriptors()) {
			System.err.println(descriptor.getDisplayName());
		}
	}
}

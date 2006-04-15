package de.fu_berlin.inf.focustracker.beaninfo;

import org.eclipse.jdt.core.IJavaElement;

import de.fu_berlin.inf.focustracker.beaninfo.util.ExtSimpleBeanInfo;
import de.fu_berlin.inf.focustracker.beaninfo.util.PropertyInfo;


public class IJavaElementBeanInfo extends ExtSimpleBeanInfo {

	@Override
	public PropertyInfo[] getProperties() {
		return new PropertyInfo[] {
//				new PropertyInfo("elementName", "elementName", null, true),	
				new PropertyInfo("primaryElement", "primaryElement", null, true)	
		};
	}

	@Override
	public Class getClazz() {
		return IJavaElement.class;
	}

	@Override
	public Class getCustomizerClazz() {
		return null;
	}

}

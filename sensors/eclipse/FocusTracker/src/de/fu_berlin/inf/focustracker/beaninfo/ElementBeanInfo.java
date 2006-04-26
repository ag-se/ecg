package de.fu_berlin.inf.focustracker.beaninfo;

import de.fu_berlin.inf.focustracker.beaninfo.util.ExtSimpleBeanInfo;
import de.fu_berlin.inf.focustracker.beaninfo.util.PropertyInfo;
import de.fu_berlin.inf.focustracker.repository.Element;


public class ElementBeanInfo extends ExtSimpleBeanInfo {

	@Override
	public PropertyInfo[] getProperties() {
		return new PropertyInfo[] {
				new PropertyInfo("javaElement", "element", null, true)	
		};
	}

	@Override
	public Class getClazz() {
		return Element.class;
	}

	@Override
	public Class getCustomizerClazz() {
		return null;
	}

}

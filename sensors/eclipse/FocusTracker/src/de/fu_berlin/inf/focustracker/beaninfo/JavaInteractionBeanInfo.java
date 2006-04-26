package de.fu_berlin.inf.focustracker.beaninfo;

import de.fu_berlin.inf.focustracker.beaninfo.util.ExtSimpleBeanInfo;
import de.fu_berlin.inf.focustracker.beaninfo.util.PropertyInfo;
import de.fu_berlin.inf.focustracker.interaction.JavaInteraction;

public class JavaInteractionBeanInfo extends ExtSimpleBeanInfo {

	@Override
	public PropertyInfo[] getProperties() {
//		return new PropertyInfo[] {
//				new PropertyInfo("javaElement", "javaElement", null, true),	
//				new PropertyInfo("javaElementFormatted", "javaElementF", null, true),	
//				new PropertyInfo("dateFormatted", "date", null, true),	
//		};
		return null;
	}

	@Override
	public Class getClazz() {
		return JavaInteraction.class;
	}

	@Override
	public Class getCustomizerClazz() {
		return null;
	}

}

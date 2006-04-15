package de.fu_berlin.inf.focustracker.beaninfo;

import de.fu_berlin.inf.focustracker.beaninfo.util.ExtSimpleBeanInfo;
import de.fu_berlin.inf.focustracker.beaninfo.util.PropertyInfo;
import de.fu_berlin.inf.focustracker.interaction.JavaInteraction;

public class JavaInteractionBeanInfo extends ExtSimpleBeanInfo {

	@Override
	public PropertyInfo[] getProperties() {
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

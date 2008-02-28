package org.electrocodeogram.cpc.classifier.provider;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.electrocodeogram.cpc.classifier.CPCClassifierPlugin;
import org.electrocodeogram.cpc.classifier.api.strategy.IClassificationStrategy;
import org.electrocodeogram.cpc.classifier.preferences.CPCPreferenceConstants;
import org.electrocodeogram.cpc.core.common.GenericStrategyDescriptor;


/**
 * Descriptor object for {@link IClassificationStrategy}s.
 * 
 * The ordering of this class is by descending priority and is not consistent with equals/hashcode.
 * 
 * @author vw
 * 
 * @see GenericStrategyDescriptor
 * @see IClassificationStrategy
 * @see ClassificationProvider
 */
public class ClassificationStrategyDescriptor extends GenericStrategyDescriptor
{
	private static final Log log = LogFactory.getLog(ClassificationStrategyDescriptor.class);

	public ClassificationStrategyDescriptor(IConfigurationElement element)
	{
		super(CPCClassifierPlugin.getDefault().getPluginPreferences(),
				CPCPreferenceConstants.PREF_CLASSIFICATION_STRATEGIES_PREFIX, element);

		log.trace("ClassificationStrategyDescriptor() ...");
	}

	/**
	 * Retrieves an instance of this singleton strategy.
	 * 
	 * @return cached instance, never null.
	 * @throws CoreException thrown if underlying strategy can not be instantiated
	 */
	public synchronized IClassificationStrategy getInstance() throws CoreException
	{
		return (IClassificationStrategy) getGenericInstance();
	}

}

package org.electrocodeogram.cpc.notification.provider;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.electrocodeogram.cpc.core.common.GenericStrategyDescriptor;
import org.electrocodeogram.cpc.notification.CPCNotificationPlugin;
import org.electrocodeogram.cpc.notification.api.strategy.INotificationEvaluationStrategy;
import org.electrocodeogram.cpc.notification.preferences.CPCPreferenceConstants;


/**
 * Descriptor object for {@link INotificationEvaluationStrategy}s.
 * 
 * The ordering of this class is by descending priority and is not consistent with equals/hashcode.
 * 
 * @author vw
 * 
 * @see GenericStrategyDescriptor
 * @see INotificationEvaluationStrategy
 * @see NotificationEvaluationProvider
 */
public class NotificationEvaluationStrategyDescriptor extends GenericStrategyDescriptor
{
	private static final Log log = LogFactory.getLog(NotificationEvaluationStrategyDescriptor.class);

	public NotificationEvaluationStrategyDescriptor(IConfigurationElement element)
	{
		super(CPCNotificationPlugin.getDefault().getPluginPreferences(),
				CPCPreferenceConstants.PREF_NOTIFICATION_STRATEGIES_PREFIX, element);

		log.trace("NotificationEvaluationStrategyDescriptor() ...");
	}

	/**
	 * Retrieves an instance of this singleton strategy.
	 * 
	 * @return cached instance, never null.
	 * @throws CoreException thrown if underlying strategy can not be instantiated
	 */
	public synchronized INotificationEvaluationStrategy getInstance() throws CoreException
	{
		return (INotificationEvaluationStrategy) getGenericInstance();
	}

}

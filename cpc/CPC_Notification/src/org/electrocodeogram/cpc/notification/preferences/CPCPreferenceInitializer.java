package org.electrocodeogram.cpc.notification.preferences;


import java.util.List;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.electrocodeogram.cpc.core.CPCCorePlugin;
import org.electrocodeogram.cpc.core.api.provider.notification.INotificationEvaluationProvider;
import org.electrocodeogram.cpc.notification.CPCNotificationPlugin;
import org.electrocodeogram.cpc.notification.provider.NotificationEvaluationProvider;
import org.electrocodeogram.cpc.notification.provider.NotificationEvaluationStrategyDescriptor;


/**
 * Sets the default state of all notification evaluation strategies to <em>true</em>.
 * 
 * @author vw
 */
public class CPCPreferenceInitializer extends AbstractPreferenceInitializer
{
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	@Override
	public void initializeDefaultPreferences()
	{
		IPreferenceStore store = CPCNotificationPlugin.getDefault().getPreferenceStore();

		INotificationEvaluationProvider notificationEvaluationProvider = (INotificationEvaluationProvider) CPCCorePlugin
				.getProviderRegistry().lookupProvider(INotificationEvaluationProvider.class);
		if (notificationEvaluationProvider == null
				|| !(notificationEvaluationProvider instanceof NotificationEvaluationProvider))
			return;

		List<NotificationEvaluationStrategyDescriptor> strategies = ((NotificationEvaluationProvider) notificationEvaluationProvider)
				.getStrategies();
		if (strategies == null)
			return;

		for (NotificationEvaluationStrategyDescriptor strategy : strategies)
		{
			store.setDefault(CPCPreferenceConstants.PREF_NOTIFICATION_STRATEGIES_PREFIX
					+ strategy.getImplementationClass(), true);
		}

	}

}

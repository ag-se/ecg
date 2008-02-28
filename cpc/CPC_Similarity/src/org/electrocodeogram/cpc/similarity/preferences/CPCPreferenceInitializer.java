package org.electrocodeogram.cpc.similarity.preferences;


import java.util.List;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.electrocodeogram.cpc.core.CPCCorePlugin;
import org.electrocodeogram.cpc.core.api.provider.similarity.ISimilarityProvider;
import org.electrocodeogram.cpc.similarity.CPCSimilarityPlugin;
import org.electrocodeogram.cpc.similarity.provider.SimilarityProvider;
import org.electrocodeogram.cpc.similarity.provider.SimilarityStrategyDescriptor;


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
		IPreferenceStore store = CPCSimilarityPlugin.getDefault().getPreferenceStore();

		ISimilarityProvider notificationEvaluationProvider = (ISimilarityProvider) CPCCorePlugin.getProviderRegistry()
				.lookupProvider(ISimilarityProvider.class);
		if (notificationEvaluationProvider == null || !(notificationEvaluationProvider instanceof SimilarityProvider))
			return;

		List<SimilarityStrategyDescriptor> strategies = ((SimilarityProvider) notificationEvaluationProvider)
				.getStrategies();
		if (strategies == null)
			return;

		for (SimilarityStrategyDescriptor strategy : strategies)
		{
			store.setDefault(CPCPreferenceConstants.PREF_SIMILARITY_STRATEGIES_PREFIX
					+ strategy.getImplementationClass(), true);
		}

	}
}

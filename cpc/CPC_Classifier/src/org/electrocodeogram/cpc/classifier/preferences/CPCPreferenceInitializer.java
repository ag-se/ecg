package org.electrocodeogram.cpc.classifier.preferences;


import java.util.List;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.electrocodeogram.cpc.classifier.CPCClassifierPlugin;
import org.electrocodeogram.cpc.classifier.provider.ClassificationProvider;
import org.electrocodeogram.cpc.classifier.provider.ClassificationStrategyDescriptor;
import org.electrocodeogram.cpc.core.CPCCorePlugin;
import org.electrocodeogram.cpc.core.api.provider.classification.IClassificationProvider;


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
		IPreferenceStore store = CPCClassifierPlugin.getDefault().getPreferenceStore();

		IClassificationProvider classificationProvider = (IClassificationProvider) CPCCorePlugin
				.getProviderRegistry().lookupProvider(IClassificationProvider.class);
		if (classificationProvider == null
				|| !(classificationProvider instanceof ClassificationProvider))
			return;

		List<ClassificationStrategyDescriptor> strategies = ((ClassificationProvider) classificationProvider)
				.getStrategies();
		if (strategies == null)
			return;

		for (ClassificationStrategyDescriptor strategy : strategies)
		{
			store.setDefault(CPCPreferenceConstants.PREF_CLASSIFICATION_STRATEGIES_PREFIX
					+ strategy.getImplementationClass(), true);
		}

	}

}

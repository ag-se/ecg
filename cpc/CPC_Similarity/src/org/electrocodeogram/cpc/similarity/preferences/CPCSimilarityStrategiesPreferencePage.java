package org.electrocodeogram.cpc.similarity.preferences;


import java.util.List;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.electrocodeogram.cpc.core.CPCCorePlugin;
import org.electrocodeogram.cpc.core.api.provider.similarity.ISimilarityProvider;
import org.electrocodeogram.cpc.similarity.CPCSimilarityPlugin;
import org.electrocodeogram.cpc.similarity.provider.SimilarityProvider;
import org.electrocodeogram.cpc.similarity.provider.SimilarityStrategyDescriptor;


/**
 * Allow users to turn on/off specific strategies
 * 
 * @author vw
 */
public class CPCSimilarityStrategiesPreferencePage extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage
{

	public CPCSimilarityStrategiesPreferencePage()
	{
		super(GRID);
		setPreferenceStore(CPCSimilarityPlugin.getDefault().getPreferenceStore());
		setDescription(Messages.CPCSimilarityStrategiesPreferencePage_title);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	@Override
	public void createFieldEditors()
	{
		/*
		 * Check that our notification eval. provider implementation is the one which is currently in use.
		 * Otherwise this config page has no meaning.
		 */
		ISimilarityProvider notificationEvaluationProvider = (ISimilarityProvider) CPCCorePlugin.getProviderRegistry()
				.lookupProvider(ISimilarityProvider.class);
		if (notificationEvaluationProvider == null || !(notificationEvaluationProvider instanceof SimilarityProvider))
			return;

		/*
		 * Add one boolean option for each strategy.
		 */
		List<SimilarityStrategyDescriptor> strategies = ((SimilarityProvider) notificationEvaluationProvider)
				.getStrategies();
		if (strategies == null)
			return;

		for (SimilarityStrategyDescriptor strategy : strategies)
		{
			addField(new BooleanFieldEditor(CPCPreferenceConstants.PREF_SIMILARITY_STRATEGIES_PREFIX
					+ strategy.getImplementationClass(), strategy.getName(), getFieldEditorParent()));
		}

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench)
	{
	}

}

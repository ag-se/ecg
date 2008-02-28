package org.electrocodeogram.cpc.classifier.preferences;


import java.util.List;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.electrocodeogram.cpc.classifier.CPCClassifierPlugin;
import org.electrocodeogram.cpc.classifier.provider.ClassificationProvider;
import org.electrocodeogram.cpc.classifier.provider.ClassificationStrategyDescriptor;
import org.electrocodeogram.cpc.core.CPCCorePlugin;
import org.electrocodeogram.cpc.core.api.provider.classification.IClassificationProvider;


/**
 * Allow users to turn on/off specific strategies
 * 
 * @author vw
 */
public class CPCClassificationStrategiesPreferencePage extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage
{

	public CPCClassificationStrategiesPreferencePage()
	{
		super(GRID);
		setPreferenceStore(CPCClassifierPlugin.getDefault().getPreferenceStore());
		setDescription(Messages.CPCClassificationStrategiesPreferencePage_title);
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
		IClassificationProvider classificationProvider = (IClassificationProvider) CPCCorePlugin.getProviderRegistry()
				.lookupProvider(IClassificationProvider.class);
		if (classificationProvider == null || !(classificationProvider instanceof ClassificationProvider))
			return;

		/*
		 * Add one boolean option for each strategy.
		 */
		List<ClassificationStrategyDescriptor> strategies = ((ClassificationProvider) classificationProvider)
				.getStrategies();
		if (strategies == null)
			return;

		for (ClassificationStrategyDescriptor strategy : strategies)
		{
			addField(new BooleanFieldEditor(CPCPreferenceConstants.PREF_CLASSIFICATION_STRATEGIES_PREFIX
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

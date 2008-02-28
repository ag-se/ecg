package org.electrocodeogram.cpc.notification.preferences;


import java.util.List;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.electrocodeogram.cpc.core.CPCCorePlugin;
import org.electrocodeogram.cpc.core.api.provider.notification.INotificationEvaluationProvider;
import org.electrocodeogram.cpc.notification.CPCNotificationPlugin;
import org.electrocodeogram.cpc.notification.provider.NotificationEvaluationProvider;
import org.electrocodeogram.cpc.notification.provider.NotificationEvaluationStrategyDescriptor;


/**
 * Allow users to turn on/off specific strategies
 * 
 * @author vw
 */
public class CPCNotificationEvaluationStrategiesPreferencePage extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage
{

	public CPCNotificationEvaluationStrategiesPreferencePage()
	{
		super(GRID);
		setPreferenceStore(CPCNotificationPlugin.getDefault().getPreferenceStore());
		setDescription(Messages.CPCNotificationEvaluationStrategiesPreferencePage_title);
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
		INotificationEvaluationProvider notificationEvaluationProvider = (INotificationEvaluationProvider) CPCCorePlugin
				.getProviderRegistry().lookupProvider(INotificationEvaluationProvider.class);
		if (notificationEvaluationProvider == null
				|| !(notificationEvaluationProvider instanceof NotificationEvaluationProvider))
			return;

		/*
		 * Add one boolean option for each strategy.
		 */
		List<NotificationEvaluationStrategyDescriptor> strategies = ((NotificationEvaluationProvider) notificationEvaluationProvider)
				.getStrategies();
		if (strategies == null)
			return;

		for (NotificationEvaluationStrategyDescriptor strategy : strategies)
		{
			addField(new BooleanFieldEditor(CPCPreferenceConstants.PREF_NOTIFICATION_STRATEGIES_PREFIX
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

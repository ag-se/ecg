package org.electrocodeogram.cpc.notification.preferences;


import org.eclipse.osgi.util.NLS;


public class Messages extends NLS
{
	private static final String BUNDLE_NAME = "org.electrocodeogram.cpc.notification.preferences.messages"; //$NON-NLS-1$

	public static String CPCMinAgeStrategyPreferencePage_option_minAgeinhours;
	public static String CPCMinAgeStrategyPreferencePage_title;

	public static String CPCNotificationDelayPreferencePage_option_delayInminutes;

	public static String CPCNotificationDelayPreferencePage_title;

	public static String CPCNotificationEvaluationStrategiesPreferencePage_title;
	public static String CPCNotificationPreferencePage_title;

	static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages()
	{
	}
}

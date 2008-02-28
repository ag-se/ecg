package org.electrocodeogram.cpc.classifier.preferences;


import org.eclipse.osgi.util.NLS;


public class Messages extends NLS
{
	private static final String BUNDLE_NAME = "org.electrocodeogram.cpc.classifier.preferences.messages"; //$NON-NLS-1$

	public static String ClassifierPreferencePage_title;

	public static String CPCClassificationStrategiesPreferencePage_title;
	public static String CPCMinLengthClassifierPreferencePage_option_charLen;
	public static String CPCMinLengthClassifierPreferencePage_option_tokenLen;
	public static String CPCMinLengthClassifierPreferencePage_option_lineCount;
	public static String CPCMinLengthClassifierPreferencePage_title;

	static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages()
	{
	}
}

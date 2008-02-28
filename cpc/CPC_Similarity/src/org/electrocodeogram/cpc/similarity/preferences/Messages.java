package org.electrocodeogram.cpc.similarity.preferences;


import org.eclipse.osgi.util.NLS;


public class Messages extends NLS
{
	private static final String BUNDLE_NAME = "org.electrocodeogram.cpc.similarity.preferences.messages"; //$NON-NLS-1$
	public static String CPCJavaCodeNormalisingStrategyPreferencePage_option_stripComments;
	public static String CPCJavaCodeNormalisingStrategyPreferencePage_title;
	public static String CPCSimilarityPreferencePage_title;
	public static String CPCSimilarityStrategiesPreferencePage_title;
	static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages()
	{
	}
}

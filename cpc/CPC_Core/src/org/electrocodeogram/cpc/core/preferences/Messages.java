package org.electrocodeogram.cpc.core.preferences;


import org.eclipse.osgi.util.NLS;


public class Messages extends NLS
{
	private static final String BUNDLE_NAME = "org.electrocodeogram.cpc.core.preferences.messages"; //$NON-NLS-1$

	public static String CPCCorePreferencePage_option_debugChecking;
	public static String CPCCorePreferencePage_title;
	public static String CPCPreferencePage_title;

	static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages()
	{
	}
}

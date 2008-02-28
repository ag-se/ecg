package org.electrocodeogram.cpc.ui.preferences;


import org.eclipse.osgi.util.NLS;


public class Messages extends NLS
{
	private static final String BUNDLE_NAME = "org.electrocodeogram.cpc.ui.preferences.messages"; //$NON-NLS-1$
	public static String CPCUIPreferencePage_title;
	public static String CPCUIRulerPreferencePage_backgroundColour;
	public static String CPCUIRulerPreferencePage_ignoredColour;
	public static String CPCUIRulerPreferencePage_inSyncColour;
	public static String CPCUIRulerPreferencePage_mixedStateColour;
	public static String CPCUIRulerPreferencePage_modifiedColour;
	public static String CPCUIRulerPreferencePage_notifyColour;
	public static String CPCUIRulerPreferencePage_orphanColour;
	public static String CPCUIRulerPreferencePage_rulerWidth;
	public static String CPCUIRulerPreferencePage_title;
	public static String CPCUIRulerPreferencePage_warnColour;
	static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages()
	{
	}
}

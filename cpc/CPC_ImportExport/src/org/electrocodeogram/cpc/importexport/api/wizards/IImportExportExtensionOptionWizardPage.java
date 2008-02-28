package org.electrocodeogram.cpc.importexport.api.wizards;


import java.util.Map;

import org.eclipse.jface.wizard.IWizardPage;


public interface IImportExportExtensionOptionWizardPage extends IWizardPage
{
	/**
	 * Provides this page with the current option map.<br/>
	 * The page may freely modify the map. Changes made by prior pages will be
	 * visible.
	 * 
	 * @param optionMap option map, may be empty, never null.
	 */
	public void init(Map<String, String> optionMap);
}

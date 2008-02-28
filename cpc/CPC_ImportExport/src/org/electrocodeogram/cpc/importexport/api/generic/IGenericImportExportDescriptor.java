package org.electrocodeogram.cpc.importexport.api.generic;


import java.util.List;

import org.electrocodeogram.cpc.importexport.api.wizards.IImportExportExtensionOptionWizardPage;
import org.electrocodeogram.cpc.importexport.api.wizards.IImportExportExtensionOptionWizardPageDescriptor;


/**
 * A generic descriptor for im- and export contributions.
 * <br>
 * The <em>CPC Imports</em> and <em>CPC Exports</em> modules define their own sub-interfaces.
 * 
 * @author vw
 */
public interface IGenericImportExportDescriptor
{
	/**
	 * Retrieves a unique identifier for the {@link IGenericImportExportDescriptor} implementation.
	 * <p>
	 * <b>NOTE:</b> Currently the identifier corresponds to the fully qualified class name of the implementing class.
	 * <br>
	 * However, this fact is not part of the API specification and a user of this interface is <b>not</b> supposed
	 * to create instances of these implementations.
	 * 
	 * @return unique identifier, never null.
	 */
	public String getId();

	/**
	 * Retrieves the human readable name of this descriptor.
	 * 
	 * @return human readable name of this descriptor, never null.
	 */
	public String getName();

	/**
	 * Retrieves generic configuration option descriptions for the {@link IGenericImportExportDescriptor} implementation.
	 * 
	 * @return {@link IGenericExtensionOption}s for this implementation, may be empty, never null.
	 */
	public List<IGenericExtensionOption> getOptionDefinitions();

	/**
	 * Retrieves a list of generic configuration option wizard pages for the {@link IGenericImportExportDescriptor} implementation.
	 * 
	 * @return descriptors of {@link IImportExportExtensionOptionWizardPage}s for this implementation, may be empty, never null.
	 */
	public List<IImportExportExtensionOptionWizardPageDescriptor> getOptionPages();
}

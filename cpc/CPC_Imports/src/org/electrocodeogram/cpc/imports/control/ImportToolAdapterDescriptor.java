package org.electrocodeogram.cpc.imports.control;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.electrocodeogram.cpc.importexport.generic.GenericImportExportDescriptor;
import org.electrocodeogram.cpc.imports.api.imports.adapter.IImportToolAdapter;
import org.electrocodeogram.cpc.imports.api.imports.adapter.IImportToolAdapterDescriptor;


/**
 * Default implementation of {@link IImportToolAdapterDescriptor}.
 * 
 * @author vw
 */
public class ImportToolAdapterDescriptor extends GenericImportExportDescriptor implements IImportToolAdapterDescriptor
{
	private static Log log = LogFactory.getLog(ImportToolAdapterDescriptor.class);

	public ImportToolAdapterDescriptor(IConfigurationElement element) throws IllegalArgumentException
	{
		super(element);
	}

	/**
	 * Creates a new instance of the class specified in the underlying configuration element.
	 */
	public IImportToolAdapter getInstance() throws CoreException
	{
		log.trace("getInstance()");

		return (IImportToolAdapter) element.createExecutableExtension("class");
	}
}

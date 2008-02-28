package org.electrocodeogram.cpc.imports.control;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.electrocodeogram.cpc.importexport.generic.GenericImportExportDescriptor;
import org.electrocodeogram.cpc.imports.api.imports.adapter.IImportToolAdapter;
import org.electrocodeogram.cpc.imports.api.imports.strategy.IImportFilterStrategyDescriptor;


/**
 * Default implementation of {@link IImportFilterStrategyDescriptor}.
 * 
 * @author vw
 */
public class ImportFilterStrategyDescriptor extends GenericImportExportDescriptor implements
		IImportFilterStrategyDescriptor
{
	private static Log log = LogFactory.getLog(ImportFilterStrategyDescriptor.class);

	public ImportFilterStrategyDescriptor(IConfigurationElement element) throws IllegalArgumentException
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

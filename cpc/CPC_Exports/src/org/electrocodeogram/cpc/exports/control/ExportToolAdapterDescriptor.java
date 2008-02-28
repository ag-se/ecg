package org.electrocodeogram.cpc.exports.control;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.electrocodeogram.cpc.exports.api.exports.adapter.IExportToolAdapter;
import org.electrocodeogram.cpc.exports.api.exports.adapter.IExportToolAdapterDescriptor;
import org.electrocodeogram.cpc.importexport.generic.GenericImportExportDescriptor;


/**
 * Default implementation of {@link IExportToolAdapterDescriptor}.
 * 
 * @author vw
 */
public class ExportToolAdapterDescriptor extends GenericImportExportDescriptor implements IExportToolAdapterDescriptor
{
	private static Log log = LogFactory.getLog(ExportToolAdapterDescriptor.class);

	public ExportToolAdapterDescriptor(IConfigurationElement element) throws IllegalArgumentException
	{
		super(element);
	}

	/**
	 * Creates a new instance of the class specified in the underlying configuration element.
	 */
	public IExportToolAdapter getInstance() throws CoreException
	{
		log.trace("getInstance()");

		return (IExportToolAdapter) element.createExecutableExtension("class");
	}

}

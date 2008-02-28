package org.electrocodeogram.cpc.importexport.api.generic;


/**
 * Thrown by the <em>CPC Imports</em> and <em>CPC Exports</em> modules on invalid configuration option data.<br/>
 * <br/>
 * The message is supposed to human readable.
 * 
 * @author vw
 */
@SuppressWarnings("serial")
public class ImportExportConfigurationOptionException extends Exception
{
	public ImportExportConfigurationOptionException(String message)
	{
		super(message);
	}

	public ImportExportConfigurationOptionException(String message, Throwable cause)
	{
		super(message, cause);
	}

}

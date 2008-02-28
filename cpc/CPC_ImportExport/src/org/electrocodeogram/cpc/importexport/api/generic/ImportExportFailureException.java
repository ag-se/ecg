package org.electrocodeogram.cpc.importexport.api.generic;


/**
 * Thrown by the <em>CPC Imports</em> and <em>CPC Exports</em> modules if the import/export
 * failed for some reason.<br/>
 * <br/>
 * The message is supposed to human readable.
 * 
 * @author vw
 */
@SuppressWarnings("serial")
public class ImportExportFailureException extends Exception
{
	public ImportExportFailureException(String message)
	{
		super(message);
	}

	public ImportExportFailureException(String message, Throwable cause)
	{
		super(message, cause);
	}

}

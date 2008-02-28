package org.electrocodeogram.cpc.exports.control;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.electrocodeogram.cpc.exports.api.exports.IExportTask;
import org.electrocodeogram.cpc.importexport.generic.ImportExportTask;


/**
 * Default implementation of {@link IExportTask}.
 * 
 * @author vw
 */
public class ExportTask extends ImportExportTask implements IExportTask
{
	private static final Log log = LogFactory.getLog(ExportTask.class);

	public ExportTask()
	{
		log.trace("ExportTask()");
	}

	/*
	 * No additional methods.
	 */
}

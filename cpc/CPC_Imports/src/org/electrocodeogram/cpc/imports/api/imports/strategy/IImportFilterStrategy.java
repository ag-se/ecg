package org.electrocodeogram.cpc.imports.api.imports.strategy;


import java.util.List;
import java.util.Map;

import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.data.ICloneFile;
import org.electrocodeogram.cpc.imports.api.imports.adapter.IImportToolAdapter;


/**
 * Import filter strategies are applied to the output of an {@link IImportToolAdapter}.
 * <br>
 * These strategies will usually be used to filter out obviously irrelevant or incorrect clones
 * returned by an import tool adapter. They might also be used to merge or modify clones if
 * this seems prudent.
 * <p>
 * Implementations need to be registered with the <em>CPC Imports</em> extension point
 * "org.electrocodeogram.cpc.imports.importFilterStrategies".
 * 
 * @author vw
 */
public interface IImportFilterStrategy
{
	/**
	 * Return status for the {@link IImportFilterStrategy#filterImport(Map, Map)} method.
	 */
	public enum Status
	{
		/**
		 * The filter has finished and the next registered filter should
		 * continue with the processing and filtering of the clone results.
		 */
		CONTINUE,

		/**
		 * The filter has finished and decided that no further filters should
		 * be executed. Filter processing stops at this point.
		 */
		BREAK
	}

	/**
	 * Takes the result of an import operation and filters out clones which are deemed
	 * not be be worth importing. Clones may also be modified by this operation.
	 * <p>
	 * All modifications are to be done in place, in the provided clone results map.
	 * 
	 * @param cloneResults the result map of the clone import, never null.
	 * @param options a configuration options map, never null.
	 * @return whether further filters should be executed or not, never null.
	 */
	public Status filterImport(Map<ICloneFile, List<IClone>> cloneResults, Map<String, String> options);

}

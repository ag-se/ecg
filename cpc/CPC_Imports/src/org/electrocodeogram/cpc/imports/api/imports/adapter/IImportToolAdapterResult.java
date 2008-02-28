package org.electrocodeogram.cpc.imports.api.imports.adapter;


import java.util.List;
import java.util.Map;

import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.data.ICloneFile;
import org.electrocodeogram.cpc.core.api.data.ICloneGroup;


/**
 * Import result wrapper for {@link IImportToolAdapter}s.
 * 
 * @author vw
 * 
 * @see IImportToolAdapter
 */
public interface IImportToolAdapterResult
{
	/**
	 * The storage object for the resulting clone and clone file data of the import process.
	 * <br>
	 * An empty map will automatically be created and should be filled with
	 * result data by the {@link IImportToolAdapter}.
	 * 
	 * @return initially empty map, which is to be filled with the clone results obtained
	 * 		during the import process, never null.
	 */
	public Map<ICloneFile, List<IClone>> getCloneMap();

	/**
	 * The storage object for the resulting clone group data of the import process.
	 * <br>
	 * An empty map will automatically be created and should be filled with
	 * result data by the {@link IImportToolAdapter}.
	 * 
	 * @return initially empty list, which is to be filled with the clone groups created during
	 * 		the import process, never null.
	 */
	public List<ICloneGroup> getCloneGroups();

}

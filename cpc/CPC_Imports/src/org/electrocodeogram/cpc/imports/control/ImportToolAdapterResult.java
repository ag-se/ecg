package org.electrocodeogram.cpc.imports.control;


import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.data.ICloneFile;
import org.electrocodeogram.cpc.core.api.data.ICloneGroup;
import org.electrocodeogram.cpc.imports.api.imports.adapter.IImportToolAdapterResult;


/**
 * Default implementation of {@link IImportToolAdapterResult}.
 * 
 * @author vw
 */
public class ImportToolAdapterResult implements IImportToolAdapterResult
{
	private Map<ICloneFile, List<IClone>> cloneMap;
	private List<ICloneGroup> cloneGroups;

	public ImportToolAdapterResult()
	{
		cloneMap = new HashMap<ICloneFile, List<IClone>>();
		cloneGroups = new LinkedList<ICloneGroup>();
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.imports.api.imports.adapter.IImportToolAdapterResult#getCloneGroups()
	 */
	@Override
	public List<ICloneGroup> getCloneGroups()
	{
		return cloneGroups;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.imports.api.imports.adapter.IImportToolAdapterResult#getCloneMap()
	 */
	@Override
	public Map<ICloneFile, List<IClone>> getCloneMap()
	{
		return cloneMap;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "ImportToolAdapterResult[cloneMap: " + cloneMap + ", cloneGroups: " + cloneGroups + "]";
	}

}

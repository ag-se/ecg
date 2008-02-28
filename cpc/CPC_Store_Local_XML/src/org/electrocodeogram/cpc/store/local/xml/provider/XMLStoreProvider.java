package org.electrocodeogram.cpc.store.local.xml.provider;


import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.data.ICloneFile;
import org.electrocodeogram.cpc.core.api.data.ICloneObject;
import org.electrocodeogram.cpc.core.api.data.special.ICloneObjectExtensionLazyMultiStatefulObject;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider;
import org.electrocodeogram.cpc.store.data.Clone;
import org.electrocodeogram.cpc.store.data.CloneFile;
import org.electrocodeogram.cpc.store.data.CloneGroup;
import org.electrocodeogram.cpc.store.provider.AbstractStoreProvider;


public class XMLStoreProvider extends AbstractStoreProvider implements IStoreProvider
{
	private static Log log = LogFactory.getLog(XMLStoreProvider.class);

	public XMLStoreProvider()
	{
		if (log.isTraceEnabled())
			log.trace("XMLStoreProvider()");

	}

	/*
	 * IStoreProvider Methods
	 */

	/*
	 * AbstractStoreProvider Methods 
	 */

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.store.provider.AbstractStoreProvider#subCheckDataIntegrity()
	 */
	@Override
	protected boolean subCheckDataIntegrity()
	{
		//TODO implement some integrity checking here
		return true;
	}

	@Override
	protected List<IClone> subGetClonesByFile(String fileUuid)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected List<IClone> subGetClonesByGroup(String groupUuid)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Clone subGetCloneByUuid(String cloneUuid, String fileUuid)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected CloneFile subGetCloneFileByPath(String project, String filePath)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected CloneFile subGetCloneFileByUuid(String fileUuid)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected CloneGroup subGetCloneGroupByUuid(String groupUuid)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected ICloneObjectExtensionLazyMultiStatefulObject subGetFullCloneObjectExtension(ICloneObject cloneObject,
			ICloneObjectExtensionLazyMultiStatefulObject extension)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected boolean subPersistCloneFile(ICloneFile file)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void subPersistData(ICloneFile file, Set<IClone> clones)
	{
		// TODO Auto-generated method stub

	}

	@Override
	protected String subGetCloneFileContent(String fileUuid)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void subMoveCloneFile(ICloneFile cloneFile, String project, String path)
	{
		// TODO Auto-generated method stub

	}

	@Override
	protected void subPersistCloneFileContent(String fileUuid, String content)
	{
		// TODO Auto-generated method stub

	}

	@Override
	protected void subPurgeCache()
	{
		// TODO Auto-generated method stub

	}

	@Override
	protected void subPurgeCloneFile(ICloneFile file)
	{
		// TODO Auto-generated method stub

	}

	@Override
	protected void subPurgeData()
	{
		// TODO Auto-generated method stub

	}

	/*
	 * IProvider Methods
	 */

	@Override
	public String getProviderName()
	{
		log.trace("getProviderName()");

		return "CPC Store Local XML: org.electrocodeogram.cpc.store.local.xml.provider.StoreProvider";
	}

	@Override
	public void onLoad()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onUnload()
	{
		// TODO Auto-generated method stub

	}

	/*
	 * Private Methods
	 */

}

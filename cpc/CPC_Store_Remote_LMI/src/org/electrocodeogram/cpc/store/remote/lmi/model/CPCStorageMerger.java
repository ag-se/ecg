package org.electrocodeogram.cpc.store.remote.lmi.model;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.team.core.mapping.IStorageMerger;
import org.electrocodeogram.cpc.core.CPCCorePlugin;
import org.electrocodeogram.cpc.core.api.provider.xml.IMappingProvider;
import org.electrocodeogram.cpc.core.api.provider.xml.MappingException;
import org.electrocodeogram.cpc.core.utils.CoreFileUtils;
import org.electrocodeogram.cpc.core.utils.CoreStringUtils;
import org.electrocodeogram.cpc.store.remote.lmi.CPCStoreRemoteLMIPlugin;
import org.electrocodeogram.cpc.store.remote.lmi.utils.LMIUtils;


/**
 * A very simple {@link IStorageMerger} which handles cpc data files.<br/>
 * No merging in done here. The cpc data file on disk is simply overwritten with the remote
 * version and a copy is stored in the state location directory of the LMI plugin.
 * 
 * @author vw
 */
public class CPCStorageMerger implements IStorageMerger
{
	private static final Log log = LogFactory.getLog(CPCStorageMerger.class);

	public static final String STORAGE_MERGER_ID = "org.electrocodeogram.cpc.store.remote.lmi.model.CPCStorageMerger";

	private IMappingProvider mappingProvider;

	public CPCStorageMerger()
	{
		log.trace("CPCStorageMerger()");

		mappingProvider = (IMappingProvider) CPCCorePlugin.getProviderRegistry().lookupProvider(IMappingProvider.class);
		assert (mappingProvider != null);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.team.core.mapping.IStorageMerger#canMergeWithoutAncestor()
	 */
	@Override
	public boolean canMergeWithoutAncestor()
	{
		log.trace("canMergeWithoutAncestor()");

		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.team.core.mapping.IStorageMerger#merge(java.io.OutputStream, java.lang.String, org.eclipse.core.resources.IStorage, org.eclipse.core.resources.IStorage, org.eclipse.core.resources.IStorage, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public IStatus merge(OutputStream output, String outputEncoding, IStorage ancestor, IStorage target,
			IStorage other, IProgressMonitor monitor) throws CoreException
	{
		if (log.isTraceEnabled())
			log.trace("merge() - output: " + output + ", outputEncoding: " + outputEncoding + ", ancestor: " + ancestor
					+ ", target: " + target + " (" + target.getName() + " - " + target.getFullPath() + "), other: "
					+ other + " (" + other.getName() + " - " + other.getFullPath() + ")");

		/*
		 * We never merge.
		 * CPC data files are just overwritten with the remote data.
		 * Merging is done in memory when the data file is parsed at a later point in time.
		 */
		String otherContent = null;
		try
		{
			//remote content
			otherContent = CoreFileUtils.readStreamContent(other.getContents());
			if (otherContent == null)
			{
				log.error("merge() - error while trying to read contents of remote cpc data file - source: "
						+ other.getFullPath() + ", dest: " + output, new Throwable());
				return new Status(Status.ERROR, CPCStoreRemoteLMIPlugin.PLUGIN_ID,
						"Error while reading contents of remote cpc data file.");
			}

			//extract file UUID from cpc xml data
			String fileUuid = mappingProvider.extractCloneObjectUuidFromString(otherContent);
			if (fileUuid == null)
			{
				log.error(
						"merge() - error while trying to extract clone file UUID from remote cpc data file - source: "
								+ other.getFullPath() + ", dest: " + output, new Throwable());
				log.info("merge() - unparsable cpc data file content: " + CoreStringUtils.quoteString(otherContent));
				return new Status(Status.ERROR, CPCStoreRemoteLMIPlugin.PLUGIN_ID,
						"Error while trying to extract clone file UUID from remote cpc data file.");
			}

			//make a temporary copy of the data
			boolean success = LMIUtils.storeTemporaryFile(fileUuid, "remote.cpc", otherContent);
			if (!success)
			{
				log.error(
						"merge() - error while trying to write contents of remote cpc data file to temporary file - source: "
								+ other.getFullPath() + ", dest: " + output, new Throwable());
				return new Status(Status.ERROR, CPCStoreRemoteLMIPlugin.PLUGIN_ID,
						"Error while writing contents of remote cpc data file to temporary file.");
			}

			//now replace the cpc data file on disk
			InputStream is = new ByteArrayInputStream(otherContent.getBytes("UTF-8"));
			CoreFileUtils.copy(is, output);
		}
		catch (MappingException e)
		{
			log.error("merge() - error while trying to extract clone file UUID from remote cpc data file - source: "
					+ other.getFullPath() + ", dest: " + output + " - " + e, e);
			log.info("merge() - unparsable cpc data file content: " + CoreStringUtils.quoteString(otherContent));
			return new Status(Status.ERROR, CPCStoreRemoteLMIPlugin.PLUGIN_ID,
					"Error while trying to extract clone file UUID from remote cpc data file: " + e, e);
		}
		catch (UnsupportedEncodingException e)
		{
			//this should never happen
			log.error("merge() - error while processing character encoding - source: " + other.getFullPath()
					+ ", dest: " + output + " - " + e, e);
			return new Status(Status.ERROR, CPCStoreRemoteLMIPlugin.PLUGIN_ID,
					"Error while processing character encoding: " + e, e);
		}
		catch (IOException e)
		{
			log.error("merge() - error while trying to update local cpc data file - source: " + other.getFullPath()
					+ ", dest: " + output + " - " + e, e);
			return new Status(Status.ERROR, CPCStoreRemoteLMIPlugin.PLUGIN_ID,
					"Error while trying to update local cpc data file: " + e, e);
		}

		return Status.OK_STATUS;
	}
}

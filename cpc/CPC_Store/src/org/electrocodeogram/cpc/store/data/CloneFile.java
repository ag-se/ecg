package org.electrocodeogram.cpc.store.data;


import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.electrocodeogram.cpc.core.api.data.collection.ICloneFileInterfaces;
import org.electrocodeogram.cpc.core.utils.CoreUtils;


public class CloneFile extends CloneObject implements ICloneFileInterfaces
{
	private static Log log = LogFactory.getLog(CloneFile.class);

	private static final long serialVersionUID = 1L;

	private String project;
	private String path;
	private long size;
	private long modificationDate;
	private String repositoryVersion;
	private boolean remoteDirty = true;

	public CloneFile()
	{
		log.trace("CloneFile()");
	}

	public CloneFile(String uuid)
	{
		super(uuid);

		if (log.isTraceEnabled())
			log.trace("CloneFile() - uuid: " + uuid);
	}

	/**
	 * Create a new CloneFile instance.
	 * 
	 * @param uuid id for the file, must not be null
	 * @param project project the file is located in, must not be null
	 * @param path path to file, relative to project, must not be null
	 * @param size size of the file
	 * @param modificationDate last file modification timestamp from filesystem
	 * @param repositoryVersion version number of this file revision in repository, MAY BE NULL
	 */
	public CloneFile(String uuid, String project, String path, long size, long modificationDate,
			String repositoryVersion)
	{
		super(uuid);

		if (log.isTraceEnabled())
			log.trace("CloneFile() - uuid: " + uuid + ", project: " + project + ", path: " + path + ", size: " + size
					+ ", modificationDate: " + modificationDate + ", repositoryVersion: " + repositoryVersion);
		assert (project != null && path != null && size >= 0);

		this.project = project;
		this.path = path;
		this.size = size;
		this.modificationDate = modificationDate;
		this.repositoryVersion = repositoryVersion;
	}

	/**
	 * Create a new CloneFile instance.<br/>
	 * Generates UUID.
	 *
	 * @param project project the file is located in, must not be null
	 * @param path path to file, relative to project, must not be null
	 * @param size size of the file
	 * @param modificationDate last file modification timestamp from filesystem
	 * @param repositoryVersion version number of this file revision in repository, MAY BE NULL
	 */
	public CloneFile(String project, String path, long size, long modificationDate, String repositoryVersion)
	{
		this(CoreUtils.generateUUID(), project, path, size, modificationDate, repositoryVersion);
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.ICloneFile#getProject()
	 */
	@Override
	public String getProject()
	{
		return project;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.ICloneFile#setProject(java.lang.String)
	 */
	@Override
	public void setProject(String project)
	{
		if (log.isTraceEnabled())
			log.trace("setProject() - project: " + project);
		assert (project != null);

		checkSeal();

		this.project = project;
	}

	/* (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.ICloneFile#getPath()
	 */
	@Override
	public String getPath()
	{
		return path;
	}

	/* (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.ICloneFile#setPath(java.lang.String)
	 */
	@Override
	public void setPath(String path)
	{
		if (log.isTraceEnabled())
			log.trace("setPath() - path: " + path);
		assert (path != null);

		checkSeal();

		this.path = path;
		setDirty(true);
	}

	/* (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.ICloneFile#getSize()
	 */
	@Override
	public long getSize()
	{
		return size;
	}

	/* (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.ICloneFile#setSize(long)
	 */
	@Override
	public void setSize(long size)
	{
		if (log.isTraceEnabled())
			log.trace("setSize() - size: " + size);
		assert (size >= 0);

		checkSeal();

		this.size = size;
		setDirty(true);
	}

	/* (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.ICloneFile#getModificationDate()
	 */
	@Override
	public long getModificationDate()
	{
		return modificationDate;
	}

	/* (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.ICloneFile#setModificationDate(long)
	 */
	@Override
	public void setModificationDate(long modificationDate)
	{
		if (log.isTraceEnabled())
			log.trace("setModificationDate() - modificationDate: " + modificationDate);

		checkSeal();

		this.modificationDate = modificationDate;
		setDirty(true);
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.internal.IRemoteStoreCloneFile#getRepositoryVersion()
	 */
	@Override
	public String getRepositoryVersion()
	{
		return repositoryVersion;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.internal.IRemoteStoreCloneFile#setRepositoryVersion(java.lang.String)
	 */
	@Override
	public void setRepositoryVersion(String repositoryVersion)
	{
		if (log.isTraceEnabled())
			log.trace("setRepositoryVersion() - repositoryVersion: " + repositoryVersion);

		checkSeal();

		this.repositoryVersion = repositoryVersion;
		setDirty(true);
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.internal.IRemoteStoreCloneFile#isRemoteDirty()
	 */
	@Override
	public boolean isRemoteDirty()
	{
		return remoteDirty;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.internal.IRemoteStoreCloneFile#setRemoteDirty(boolean)
	 */
	@Override
	public void setRemoteDirty(boolean remoteDirty)
	{
		if (log.isTraceEnabled())
			log.trace("setRemoteDirty() - remoteDirty: " + remoteDirty);

		checkSeal();

		this.remoteDirty = remoteDirty;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.internal.IStatefulObject#getPersistenceIdentifier()
	 */
	@Override
	public String getPersistenceClassIdentifier()
	{
		return PERSISTENCE_CLASS_IDENTIFIER;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.store.data.CloneObject#getState()
	 */
	@Override
	public Map<String, Comparable<? extends Object>> getState()
	{
		//first get the internal state of our super class
		Map<String, Comparable<? extends Object>> result = super.getState();

		//now add our data
		result.put("project", project);
		result.put("path", path);
		result.put("size", size);
		result.put("modificationDate", modificationDate);
		result.put("repositoryVersion", repositoryVersion);
		result.put("remoteDirty", remoteDirty);

		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.store.data.CloneObject#getStateTypes()
	 */
	@Override
	public Map<String, Class<? extends Object>> getStateTypes()
	{
		//first get the data types of our super class
		Map<String, Class<? extends Object>> result = super.getStateTypes();

		//now add our types
		result.put("project", String.class);
		result.put("path", String.class);
		result.put("size", Long.class);
		result.put("modificationDate", Long.class);
		result.put("repositoryVersion", String.class);
		result.put("remoteDirty", Boolean.class);

		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.store.data.CloneObject#setState(java.util.Map)
	 */
	@Override
	public void setState(Map<String, Comparable<? extends Object>> state)
	{
		if (log.isTraceEnabled())
			log.trace("setState() - state: " + state);
		assert (state != null);

		checkSeal();

		//first initialise the internal state of our super class
		super.setState(state);

		//then initialise our own state
		try
		{
			project = (String) state.get("project");
			path = (String) state.get("path");
			size = (Long) state.get("size");
			modificationDate = (Long) state.get("modificationDate");
			repositoryVersion = (String) state.get("repositoryVersion");
			remoteDirty = (Boolean) state.get("remoteDirty");
		}
		catch (Exception e)
		{
			//this should not happen
			log.error("setState() - error while restoring internal state - state: " + state + " - " + e, e);
		}

	}

	/* (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.ICloneFile#toString()
	 */
	@Override
	public String toString()
	{
		return getSealStatus() + "CloneFile[uuid: " + uuid + ", project: " + project + ", path: " + path + ", size: "
				+ size + ", mod: " + modificationDate + ", repoVer: " + repositoryVersion + "]";
	}

	/*
	 * NOTE:
	 * 	equals() and hashCode() are implemented by CPCDataObject
	 */

	/*
	 * TODO: Overriding getAdapter(Class) might be useful here.
	 * I.e. we could easily provide an IResource and IFile mapping.
	 * 
	 * public Object getAdapter(Class adapter)
	 */
}

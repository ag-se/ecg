package org.electrocodeogram.cpc.store.remote.lmi.utils;


import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.electrocodeogram.cpc.core.CPCCorePlugin;
import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.data.ICloneFile;
import org.electrocodeogram.cpc.core.api.provider.xml.IMappingProvider;
import org.electrocodeogram.cpc.core.api.provider.xml.MappingException;
import org.electrocodeogram.cpc.core.api.provider.xml.MappingStore;
import org.electrocodeogram.cpc.core.utils.CoreStringUtils;
import org.electrocodeogram.cpc.store.remote.lmi.utils.jobs.CheckFolderWorkspaceJob;
import org.electrocodeogram.cpc.store.remote.lmi.utils.jobs.DeleteFileWorkspaceJob;
import org.electrocodeogram.cpc.store.remote.lmi.utils.jobs.WriteXmlDataWorkspaceJob;


public class XMLPersistenceUtils
{
	private static final Log log = LogFactory.getLog(XMLPersistenceUtils.class);

	/**
	 * The name of the sub directory under which all xml clone data will be stored.
	 */
	public static final String XML_PERSISTENCE_DIRECTORY = ".cpc";

	/**
	 * The file extension which should be appended to a given filename in order to
	 * obtain the filename for the clone data xml file.<br/>
	 * The extension does <b>not</b> include the dot.
	 */
	public static final String XML_PERSISTENCE_EXTENSION = "cpc";

	/**
	 * Schedules the creation/update of the cpc xml data file for a given java source file.
	 * 
	 * @param file the source file handle to write data for, never null.
	 * 		This is the source file, <b>not</b> the cpc xml data file.
	 * @param cloneFile the clone file entry for the source file, never null.
	 * @param clones the current list of clones for the file, never null.
	 * @throws CoreException
	 */
	public static void writeXmlData(IFile file, ICloneFile cloneFile, List<IClone> clones) throws CoreException
	{
		if (log.isTraceEnabled())
			log.trace("writeXmlData() - file: " + file + ", cloneFile: " + cloneFile + ", clones: " + clones);
		assert (file != null && cloneFile != null && clones != null);

		//make sure the cpc folder exists
		IFolder cpcFolder = checkXmlDataDirectory(file, false);

		//get a handle for the cpc data file
		IFile cpcFile = getXmlDataFile(file, cpcFolder);
		//It doesn't matter whether the file exists or not. We're going to overwrite the file anyway

		//generate the xml representation of the lcone data
		IMappingProvider mappingProvider = (IMappingProvider) CPCCorePlugin.getProviderRegistry().lookupProvider(
				IMappingProvider.class);
		assert (mappingProvider != null);
		MappingStore mappingStore = new MappingStore(cloneFile, clones);
		String xml;
		try
		{
			xml = mappingProvider.mapToString(mappingStore, true);
		}
		catch (MappingException e)
		{
			log.error("writeXmlData() - error during xml mapping - " + e, e);
			return;
		}

		if (log.isTraceEnabled())
			log.trace("writeXmlData() - scheduling writing of xml data: " + CoreStringUtils.truncateString(xml));

		//overwrite the file
		WriteXmlDataWorkspaceJob job = new WriteXmlDataWorkspaceJob(cpcFolder, cpcFile, xml);
		job.schedule();
	}

	/**
	 * Deletes the cpc xml data file for a given source file. 
	 * 
	 * @param file the source file for which the cpc xml data file should be deleted, never null.
	 * @throws CoreException
	 */
	public static void clearXmlData(IFile file) throws CoreException
	{
		if (log.isTraceEnabled())
			log.trace("clearXmlData() - file: " + file);
		assert (file != null);

		IFolder cpcFolder = getXmlDataFolder(file);

		//get a handle for the cpc data file
		IFile cpcFile = getXmlDataFile(file, cpcFolder);

		if (cpcFile.exists())
		{
			//ok, delete the file			
			if (log.isTraceEnabled())
				log.trace("clearXmlData() - scheduling deletion of xml data file: " + cpcFile);

			DeleteFileWorkspaceJob job = new DeleteFileWorkspaceJob(cpcFile);
			job.schedule();
		}
		else
		{
			log.trace("clearXmlData() - file did not exist, ignoring.");
		}

		//remove cpc folder if this was the last file inside of it
		checkXmlDataDirectory(file, true);
	}

	/**
	 * Checks whether the cpc data sub directory exists.<br/>
	 * In cleanup mode the directory will be deleted, if it is empty.<br/>
	 * If not in cleanup mode the directory will be created if it doesn't exist.
	 * 
	 * @param file the file in question, the cpc data sub directory will be looked up in the same diretory
	 * 		as this file, never null.
	 * @param doClean true if we're in cleanup mode, false otherwise.
	 * @return returns the cpc data folder handle, note: it may already be deleted!, never null.
	 * @throws CoreException if a directory access, creation or deletion fails.
	 */
	public static IFolder checkXmlDataDirectory(IFile file, boolean doClean) throws CoreException
	{
		if (log.isTraceEnabled())
			log.trace("checkXmlDataDirectory() - file: " + file + ", doClean: " + doClean);
		assert (file != null);

		IFolder cpcFolder = getXmlDataFolder(file);

		if (doClean && cpcFolder.exists())
		{
			/*
			 * The folder exists and we're in clean up mode.
			 * Make sure we still need the folder (it's not empty), otherwise remove it.
			 */
			if (cpcFolder.members(IResource.NONE).length == 0)
			{
				//ok, the folder is empty, remove it.
				if (log.isTraceEnabled())
					log.trace("checkXmlDataDirectory() - removing unused cpc folder: " + cpcFolder);

				CheckFolderWorkspaceJob job = new CheckFolderWorkspaceJob(cpcFolder, false);
				job.schedule();
			}
		}
		else if (!doClean && !cpcFolder.exists())
		{
			/*
			 * We're in add/update mode and the folder is missing. Create it.
			 */
			if (log.isTraceEnabled())
				log.trace("checkXmlDataDirectory() - creating cpc folder: " + cpcFolder);

			CheckFolderWorkspaceJob job = new CheckFolderWorkspaceJob(cpcFolder, true);
			job.schedule();
		}
		/*
		 * else: everything is fine.
		 */

		return cpcFolder;
	}

	/**
	 * Retrieves a handle for the cpc xml data folder for the given file.<br/>
	 * The folder might not exist.
	 * 
	 * @param file the java file to get the cpc xml data folder for, never null.
	 * @return cpc xml data folder handle, never null.
	 */
	public static IFolder getXmlDataFolder(IFile file)
	{
		if (log.isTraceEnabled())
			log.trace("getXmlDataFolder() - file: " + file);
		assert (file != null);

		IFolder cpcFolder = file.getParent().getFolder(new Path(XML_PERSISTENCE_DIRECTORY));

		if (log.isTraceEnabled())
			log.trace("getXmlDataFolder() - result: " + cpcFolder);

		return cpcFolder;
	}

	/**
	 * Retrieves a handle for the cpc xml data file for the given file.<br/>
	 * The file and the folder it is located in might not exist.
	 * 
	 * @param file the java file to get the cpc xml data file for, never null.
	 * @param cpcFolder handle to the cpc xml data folder for the file, never null.
	 * @return a handle for the corresponding cpc xml data file, never null.
	 */
	public static IFile getXmlDataFile(IFile file, IFolder cpcFolder)
	{
		if (log.isTraceEnabled())
			log.trace("getXmlDataFile() - file: " + file + ", cpcFolder: " + cpcFolder);
		assert (file != null && cpcFolder != null);

		IFile cpcFile = cpcFolder.getFile(file.getName() + "." + XML_PERSISTENCE_EXTENSION);

		if (log.isTraceEnabled())
			log.trace("getXmlDataFile() - result: " + cpcFile);

		return cpcFile;
	}

	/**
	 * Convenience method.<br/>
	 * Combines {@link XMLPersistenceUtils#getXmlDataFolder(IFile)} and
	 * {@link XMLPersistenceUtils#getXmlDataFile(IFile, IFolder)}.
	 * 
	 * @param file
	 * @return
	 */
	public static IFile getXmlDataFile(IFile file)
	{
		return getXmlDataFile(file, getXmlDataFolder(file));
	}

	/**
	 * Retrieves the corresponding source file for a given cpc xml data file.
	 * 
	 * @param cpcDataResource the cpc xml data file to get the source file for, never null.
	 * 		The file underlying the file handle does not have to exist.
	 * @return a file handle for the source file or NULL if it can't be found.
	 * 
	 * TODO: could we get into problems here if the file doesn't exist?
	 */
	public static IFile getSourceFileForCPCDataResource(IResource cpcDataResource)
	{
		if (log.isTraceEnabled())
			log.trace("sourceFileForCPCDataResource() - cpcDataResource: " + cpcDataResource);
		assert (cpcDataResource != null);

		//get filename of cpc data file
		String cpcDataFilename = cpcDataResource.getFullPath().lastSegment();

		//generate corresponding source-file filename
		String sourceFileName = cpcDataFilename.replaceAll("\\." + XML_PERSISTENCE_EXTENSION + "$", "");

		if (log.isTraceEnabled())
			log.trace("sourceFileForCPCDataResource() - sourceFileName: " + sourceFileName + " (cpcDataFilename: "
					+ cpcDataFilename + ")");

		//check if the source file for this cpc data file exists
		IFile sourceFile = cpcDataResource.getParent().getParent().getFile(new Path(sourceFileName));

		if (log.isTraceEnabled())
			log.trace("sourceFileForCPCDataResource() - result: " + sourceFile);

		return sourceFile;
	}

	/**
	 * Checks whether the given resource is a cpc xml data file.
	 * 
	 * @param resource the resource to check, may be NULL.
	 * @return true if the resource exists and is a cpc xml data file, false otherwise.
	 */
	public static boolean isCPCDataResource(IResource resource)
	{
		if (resource == null || resource.getType() != IResource.FILE)
			return false;

		if (!resource.exists())
			return false;

		if (!resource.getFileExtension().equalsIgnoreCase(XML_PERSISTENCE_EXTENSION))
			return false;

		//cpc data files should always be located within a cpc data folder, lets check
		//that here to be on the safe side.
		if (!resource.getParent().getName().equals(XML_PERSISTENCE_DIRECTORY))
		{
			log.warn("isCPCDataResource() - found cpc data file outside of cpc data directory, ignoring - file: "
					+ resource);
			return false;
		}

		return true;
	}
}

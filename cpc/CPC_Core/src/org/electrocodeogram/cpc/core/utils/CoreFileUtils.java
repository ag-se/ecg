package org.electrocodeogram.cpc.core.utils;


import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.electrocodeogram.cpc.core.CPCCorePlugin;
import org.electrocodeogram.cpc.core.api.data.ICloneFile;
import org.electrocodeogram.cpc.core.api.hub.event.CloneModificationEvent;
import org.electrocodeogram.cpc.core.api.hub.registry.IEventHubRegistry;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider;
import org.electrocodeogram.cpc.core.api.provider.store.StoreLockingException;


/**
 * Utility class which contains static methods for typical file related tasks.
 * 
 * @author vw
 */
public class CoreFileUtils
{
	private static Log log = LogFactory.getLog(CoreFileUtils.class);

	private static QualifiedName persistentPropertyFileUuidQN = new QualifiedName(CPCCorePlugin.PLUGIN_ID, "file_uuid");
	private static QualifiedName persistentPropertyFileRevisionQN = new QualifiedName(CPCCorePlugin.PLUGIN_ID,
			"file_revision");

	private CoreFileUtils()
	{
		//this class is not meant to be instantiated
	}

	/**
	 * Creates a given file/folder/project recursively.
	 * 
	 * @param resource the resource to create, may be NULL.
	 */
	public static void createResourceRecursively(IResource resource) throws CoreException
	{
		if (resource == null || resource.exists())
			return;

		if (!resource.getParent().exists())
			createResourceRecursively(resource.getParent());

		switch (resource.getType())
		{
			case IResource.FILE:
				((IFile) resource).create(new ByteArrayInputStream(new byte[0]), true, null);
				break;
			case IResource.FOLDER:
				((IFolder) resource).create(IResource.NONE, true, null);
				break;
			case IResource.PROJECT:
				((IProject) resource).create(null);
				((IProject) resource).open(null);
				break;
		}
	}

	/**
	 * Convenience method for {@link #getFile(String, String, boolean)}.<br/>
	 * Will return null if the file doesn't exist.
	 * 
	 * @see CoreFileUtils#getFile(String, String, boolean)
	 */
	public static IFile getFile(String project, String filePath)
	{
		return getFile(project, filePath, false);
	}

	/**
	 * Retrieves the {@link IFile} handle for the given file in the given project.
	 * 
	 * @param project the project of the file, never null.
	 * @param filePath the file path, relative to the project, never null.
	 * @param returnHandleIfMissing whether to return null or a handle if the file doesn't exist.
	 * 		If this is <em>true</em> a handle is returned, otherwise null is returned.
	 * @return a file handle for the corresponding file or NULL if the project could not be found
	 * 		or is not open. The return value in case of a non-existing file depends on
	 * 		<em>returnHandleIfMissing</em>.
	 */
	public static IFile getFile(String project, String filePath, boolean returnHandleIfMissing)
	{
		if (log.isTraceEnabled())
			log.trace("getFile() - project: " + project + ", filePath: " + filePath);
		assert (project != null && filePath != null);

		IProject projectHandle = ResourcesPlugin.getWorkspace().getRoot().getProject(project);
		if (projectHandle == null || !projectHandle.isAccessible())
			return null;

		IFile fileHandle = projectHandle.getFile(filePath);
		if (fileHandle == null || (!returnHandleIfMissing && !fileHandle.exists()))
			return null;

		return fileHandle;
	}

	/**
	 * Retrieves a list of all supported source files inside a given project.
	 * 
	 * @param projectName the name of the project, never null.
	 * @return a list of supported source files, NULL on error.
	 */
	public static List<IFile> getSupportedFilesInProject(String projectName)
	{
		if (log.isTraceEnabled())
			log.trace("getSupportedFilesInProject() - projectName: " + projectName);
		assert (projectName != null);

		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		if (project == null || !project.exists())
		{
			log.error("getSupportedFilesInProject() - project doesn't exist - projectName: " + projectName,
					new Throwable());
			return null;
		}

		List<IFile> result = new LinkedList<IFile>();

		try
		{
			//the visitor directly updates the result list.
			project.accept(new ExtensionBasedFileCollectionResourceVisitor(CPCCorePlugin.getConfigurationRegistry()
					.getSupportedFileTypes(), result));
		}
		catch (CoreException e)
		{
			log.error("getSupportedFilesInProject() - exception in resource visitor - " + e, e);
			return null;
		}

		return result;
	}

	/**
	 * Retrieves a list of all supported source files inside all the given projects.
	 * 
	 * @param projects a list of projects to collect supported source files from, never null.
	 * @return a list of supported source files, NULL on error.
	 */
	public static List<IFile> getSupportedFilesInProjects(List<IProject> projects)
	{
		if (log.isTraceEnabled())
			log.trace("getSupportedFilesInProjects() - projects: " + projects);
		assert (projects != null);

		List<IFile> result = new LinkedList<IFile>();

		try
		{
			for (IProject project : projects)
			{
				//skip any projects which don't exist
				if (!project.exists())
				{
					log.warn("getSupportedFilesInProjects() - skipping non-existant project: " + project);
					continue;
				}

				//the visitor directly updates the result list.
				project.accept(new ExtensionBasedFileCollectionResourceVisitor(CPCCorePlugin.getConfigurationRegistry()
						.getSupportedFileTypes(), result));
			}
		}
		catch (CoreException e)
		{
			log.error("getSupportedFilesInProjects() - exception in resource visitor - " + e, e);
			return null;
		}

		return result;
	}

	/**
	 * Retrieves a list of all supported source files encompassing all the given resources and their contents.
	 * 
	 * @param resources a list of resources to collect supported source files from, never null.
	 * @return a list of supported source files, NULL on error.
	 */
	public static List<IFile> getSupportedFilesInResources(List<IResource> resources)
	{
		if (log.isTraceEnabled())
			log.trace("getSupportedFilesInResources() - resources: " + resources);
		assert (resources != null);

		List<IFile> result = new LinkedList<IFile>();

		try
		{
			for (IResource resource : resources)
			{
				//skip any resource which don't exist
				if (!resource.exists())
				{
					log.warn("getSupportedFilesInResources() - skipping non-existant resource: " + resource);
					continue;
				}

				//the visitor directly updates the result list.
				resource.accept(new ExtensionBasedFileCollectionResourceVisitor(CPCCorePlugin
						.getConfigurationRegistry().getSupportedFileTypes(), result));
			}
		}
		catch (CoreException e)
		{
			log.error("getSupportedFilesInResources() - exception in resource visitor - " + e, e);
			return null;
		}

		return result;
	}

	/**
	 * Purges all clone data for the given list of files and notifies interested parties
	 * about the need to refresh their clone data (i.e. CPC UI views).<br/>
	 * Convenience method.<br/>
	 * <br/>
	 * Calling this method requires an exclusive write lock for the store provider.<br/>
	 * <br/>
	 * <b>IMPORTANT:</b> If the chosen {@link IStoreProvider.LockMode} prevents the store provider from sending out
	 * {@link CloneModificationEvent}s, it is up to the caller to create the necessary events.
	 *  
	 * @param storeProvider a valid {@link IStoreProvider} reference, never null.
	 * @param files a list of files to purge all clone data for, never null.
	 * @param removeCloneFiles whether the {@link ICloneFile} entries should be removed as well.
	 * 		See: {@link IStoreProvider#purgeData(ICloneFile, boolean)}.
	 * 
	 * @throws StoreLockingException thrown if this method is used without an exclusive write lock 
	 */
	public static void purgeCloneDataForFiles(IStoreProvider storeProvider, List<IFile> files, boolean removeCloneFiles)
			throws StoreLockingException
	{
		if (log.isTraceEnabled())
			log.trace("purgeCloneDataForFiles() - files: " + files + ", removeCloneFiles: " + removeCloneFiles
					+ ", storeProvider: " + storeProvider);
		assert (storeProvider != null && files != null);

		//get a reference to the IEventHubRegistry
		IEventHubRegistry eventHubRegistry = CPCCorePlugin.getEventHubRegistry();
		assert (eventHubRegistry != null);

		//for each file
		for (IFile file : files)
		{
			//get a clone file handle
			ICloneFile cloneFile = storeProvider.lookupCloneFileByPath(file.getProject().getName(), file
					.getProjectRelativePath().toString(), false, false);

			if (cloneFile == null)
			{
				if (log.isDebugEnabled())
					log.debug("purgeCloneDataForFiles() - unable to obtain clone file for file: " + file);
				continue;
			}

			//and delete all clone data for the file
			storeProvider.purgeData(cloneFile, removeCloneFiles);
		}

	}

	/**
	 * Tries to obtain the file uuid from a persistent property of the given file handle.<br/>
	 * 
	 * @param cloneFileHandle the file handle to retrieve the uuid from, may be NULL.
	 * @return the file's uuid or NULL if no uuid was set or the file was not found.
	 */
	public static String getFileUuidProperty(IFile cloneFileHandle)
	{
		if (log.isTraceEnabled())
			log.trace("getFileUuidProperty() - cloneFileHandle: " + cloneFileHandle);

		if (cloneFileHandle == null || !cloneFileHandle.exists())
			return null;

		String uuid = null;

		try
		{
			uuid = cloneFileHandle.getPersistentProperty(persistentPropertyFileUuidQN);
		}
		catch (CoreException e)
		{
			log.warn("getFileUuidProperty() - unable to retrieve persistent IFile property - file: " + cloneFileHandle
					+ ", property: " + persistentPropertyFileUuidQN + " - " + e, e);
		}

		if (log.isTraceEnabled())
			log.trace("getFileUuidProperty() - result: " + uuid);

		return uuid;
	}

	/**
	 * Sets the file uuid of the given {@link ICloneFile} as a persistent property for the
	 * underlying file resource. The file should exist, otherwise an error is logged and
	 * the call is ignored.
	 * 
	 * @param cloneFile the file to set the file uuid as persistent property for, never null.
	 */
	public static void setFileUuidProperty(ICloneFile cloneFile)
	{
		if (log.isTraceEnabled())
			log.trace("setFileUuidProperty() - cloneFile: " + cloneFile);
		assert (cloneFile != null);

		IFile cloneFileHandle = CoreFileUtils.getFileForCloneFile(cloneFile);
		if (cloneFileHandle == null || !cloneFileHandle.exists())
		{
			log.error("setFileUuidProperty() - unable to obtain IFile handle for clone file: " + cloneFile,
					new Throwable());
			return;
		}

		setFileUuidProperty(cloneFileHandle, cloneFile.getUuid());
	}

	/**
	 * Sets the given file uuid persistent property for the given file.
	 * 
	 * @param cloneFileHandle the file to set the persistent property for, file must exist, never null.
	 * @param fileUuid the file UUID to set, may be NULL. A value of NULL will delete the persistent property.
	 */
	public static void setFileUuidProperty(IFile cloneFileHandle, String fileUuid)
	{
		if (log.isTraceEnabled())
			log.trace("setFileUuidProperty() - cloneFileHandle: " + cloneFileHandle + ", fileUuid: " + fileUuid);
		assert (cloneFileHandle != null && cloneFileHandle.exists());

		try
		{
			cloneFileHandle.setPersistentProperty(persistentPropertyFileUuidQN, fileUuid);
		}
		catch (CoreException e)
		{
			log.warn("setFileUuidProperty() - unable to set persistent IFile property - file: " + cloneFileHandle
					+ ", fileUuid: " + fileUuid + ", property: " + persistentPropertyFileUuidQN + " - " + e, e);
		}
	}

	/**
	 * Tries to obtain the last file revision identifier from a persistent property of the given file handle.<br/>
	 * 
	 * @param fileHandle the file handle to retrieve the revision from, may be NULL.
	 * @return the file's revision or NULL if no revision was set or the file was not found.
	 */
	public static String getFileRevisionProperty(IFile fileHandle)
	{
		if (log.isTraceEnabled())
			log.trace("getFileRevisionProperty() - fileHandle: " + fileHandle);

		if (fileHandle == null || !fileHandle.exists())
			return null;

		String revision = null;

		try
		{
			revision = fileHandle.getPersistentProperty(persistentPropertyFileRevisionQN);
		}
		catch (CoreException e)
		{
			log.warn("getFileRevisionProperty() - unable to retrieve persistent IFile property - file: " + fileHandle
					+ ", property: " + persistentPropertyFileRevisionQN + " - " + e, e);
		}

		if (log.isTraceEnabled())
			log.trace("getFileRevisionProperty() - result: " + revision);

		return revision;
	}

	/**
	 * Sets the given file revision persistent property for the given file.
	 * 
	 * @param fileHandle the file to set the persistent property for, file must exist, never null.
	 * @param revision the file revision to set, never null.
	 */
	public static void setFileRevisionProperty(IFile fileHandle, String revision)
	{
		if (log.isTraceEnabled())
			log.trace("setFileRevisionProperty() - fileHandle: " + fileHandle + ", revision: " + revision);
		assert (fileHandle != null && fileHandle.exists() && revision != null);

		try
		{
			fileHandle.setPersistentProperty(persistentPropertyFileRevisionQN, revision);
		}
		catch (CoreException e)
		{
			log.warn("setFileRevisionProperty() - unable to set persistent IFile property - file: " + fileHandle
					+ ", revision: " + revision + ", property: " + persistentPropertyFileRevisionQN + " - " + e, e);
		}
	}

	/**
	 * Checks whether a given file exists and is located within the workspace.
	 * 
	 * @param fileLocation the file path to check, never null.
	 * @return true if the given path points to an existing file inside of the workspace, false otherwise.
	 */
	public static boolean isFileLocatedInWorkspace(IPath fileLocation)
	{
		if (log.isTraceEnabled())
			log.trace("isFileLocatedInWorkspace() - fileLocation: " + fileLocation);
		assert (fileLocation != null);

		return ResourcesPlugin.getWorkspace().getRoot().exists(fileLocation);
	}

	/**
	 * Checks whether a given file exists and is located within the workspace.
	 * 
	 * @param fileLocation the file path to check, never null.
	 * @return true if the given path points to an existing file inside of the workspace, false otherwise.
	 */
	public static boolean isFileLocatedInWorkspace(String fileLocation)
	{
		if (log.isTraceEnabled())
			log.trace("isFileLocatedInWorkspace() - fileLocation: " + fileLocation);
		assert (fileLocation != null);

		return isFileLocatedInWorkspace(new Path(fileLocation));
	}

	/**
	 * Checks whether a given file exists and is located within the workspace.<br/>
	 * <br/>
	 * This is a convenience method which takes the fact into account that file paths are usually
	 * split into project and project relative path in most places. However, in this does <b>not</b>
	 * mean that the file is located within the workspace. The <em>project</em> is simply the topmost
	 * directory and the <em>filePath</em> the remaining path.<br/>
	 * Internally the two strings are simply concatenated with an additional "/" as separator.
	 * 
	 * @param project the "project" of the file, this is just the name of the topmost folder, may be NULL.
	 * @param filePath the "project relative path" of the file, this is the remaining path after stripping
	 * 		the topmost directory, may be NULL.
	 * @return true if the given path points to an existing file inside of the workspace, false otherwise.
	 * 		Also false if either of the two parameters is null.
	 */
	public static boolean isFileLocatedInWorkspace(String project, String filePath)
	{
		if (log.isTraceEnabled())
			log.trace("isFileLocatedInWorkspace() - project: " + project + ", filePath: " + filePath);

		if (project == null || filePath == null)
			return false;

		return isFileLocatedInWorkspace(new Path(project + "/" + filePath));
	}

	/**
	 * Retrieves the content for the given input stream.<br/>
	 * The stream is expected to contain character data.
	 * 
	 * @param inputStream the {@link InputStream} to retrieve the content for, may be null.
	 * @return the content of the {@link InputStream} or NULL if the stream was null or was not readable.
	 */
	public static String readStreamContent(InputStream inputStream)
	{
		if (log.isTraceEnabled())
			log.trace("readStreamContent() - inputStream: " + inputStream);

		if (inputStream == null)
		{
			log.trace("readStreamContent() - result: null");
			return null;
		}

		try
		{
			char[] buf = new char[16384];
			StringBuilder sb = new StringBuilder();

			BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
			int read;
			do
			{
				read = br.read(buf);
				if (read > 0)
					sb.append(buf, 0, read);
			}
			while (read != -1);
			br.close();

			String result = sb.toString();

			if (log.isTraceEnabled())
				log.trace("readStreamContent() - result: " + CoreStringUtils.truncateString(result));

			return result;
		}
		catch (Exception e)
		{
			log.error("readStreamContent() - error while reading inputStream: " + inputStream + " - " + e, e);
			return null;
		}
	}

	/**
	 * Copies all data from the given input stream to the given output stream.<br/>
	 * Neither of the given streams will be closed by this method.
	 * 
	 * @param source the input stream, never null.
	 * @param destination the output stream, never null.
	 */
	public static void copy(InputStream source, OutputStream destination) throws IOException
	{
		if (log.isTraceEnabled())
			log.trace("copy() - source: " + source + ", destination: " + destination);
		assert (source != null && destination != null);

		byte buffer[] = new byte[8192];
		int bytesRead;

		while ((bytesRead = source.read(buffer)) != -1)
		{
			destination.write(buffer, 0, bytesRead);
		}

		destination.flush();
	}

	/**
	 * Checks whether the project for the given {@link ICloneFile} is accessible.<br/>
	 * Meaning, the project exists and is open.
	 * 
	 * @param cloneFile the clone file to check, never null.
	 * @return true if the project is accessible, false if it doesn't exist or is closed.
	 */
	public static boolean isProjectAccessible(ICloneFile cloneFile)
	{
		IProject projectHandle = ResourcesPlugin.getWorkspace().getRoot().getProject(cloneFile.getProject());

		if (projectHandle == null || !projectHandle.isAccessible())
			return false;

		return true;
	}

	/**
	 * Retrieves an {@link IFile} handle for a given clone file.
	 * 
	 * @param cloneFile the clone file to lookup, never null
	 * @param returnHandleIfMissing true if a file handle should be returned, even if the file does not exist, false otherwise.
	 * @return a corresponding file handle, non existing file handle or NULL if file doesn't exist, depending on the <em>returnHandleIfMissing</em> value.
	 */
	public static IFile getFileForCloneFile(ICloneFile cloneFile, boolean returnHandleIfMissing)
	{
		if (log.isTraceEnabled())
			log.trace("getFileForCloneFile() - cloneFile: " + cloneFile + ", returnHandleIfMissing: "
					+ returnHandleIfMissing);
		assert (cloneFile != null);

		IProject projectHandle = ResourcesPlugin.getWorkspace().getRoot().getProject(cloneFile.getProject());
		IFile fileHandle = projectHandle.getFile(new Path(cloneFile.getPath()));

		if (!fileHandle.exists())
		{
			log
					.trace("getFileForCloneFile() - file does not exist, trying to update clone file entry from store provider, maybe the file was moved.");

			ICloneFile newCloneFile = CoreConfigurationUtils.getStoreProvider().lookupCloneFile(cloneFile.getUuid());
			if (newCloneFile == null)
			{
				//it seems as if the file was deleted in the mean time
				//only warn if this wasn't expected
				if (!returnHandleIfMissing)
					log.warn("getFileForCloneFile() - file was already deleted - file: " + cloneFile, new Throwable());
				else if (log.isTraceEnabled())
					log.trace("getFileForCloneFile() - file was already deleted - file: " + cloneFile);

				if (returnHandleIfMissing)
					return fileHandle;
				else
					return null;
			}
			else
			{
				//ok, the clone file entry does still exist, check if it was moved
				if (newCloneFile.getProject().equals(cloneFile.getProject())
						&& newCloneFile.getPath().equals(cloneFile.getPath()))
				{
					//nope, not moved.
					//only warn if this wasn't expected
					if (!returnHandleIfMissing)
						log.warn("getFileForCloneFile() - unable to find clone file: " + cloneFile, new Throwable());
					else if (log.isTraceEnabled())
						log.trace("getFileForCloneFile() - unable to find clone file: " + cloneFile);

					if (returnHandleIfMissing)
						return fileHandle;
					else
						return null;
				}
				else
				{
					//file moved, redo the check with the new file data
					log.warn("getFileForCloneFile() - file was moved, retrying with new location - old: " + cloneFile
							+ ", new: " + newCloneFile, new Throwable());
					return getFileForCloneFile(newCloneFile, returnHandleIfMissing);
				}
			}
		}

		return fileHandle;
	}

	/**
	 * Retrieves an {@link IFile} handle for a given clone file.
	 * 
	 * @param cloneFile the clone file to lookup, never null
	 * @return a corresponding file handle, NULL if file doesn't exist.
	 */
	public static IFile getFileForCloneFile(ICloneFile cloneFile)
	{
		return getFileForCloneFile(cloneFile, false);
	}

	/**
	 * Retrieves the file modification timestamp for the given file from the file system.
	 * 
	 * @param fileHandle the file to get the modification date for, never null.
	 * @return modification timestamp as returned by {@link IFileInfo#getLastModified()} or -1 on error.
	 * 
	 * @see IFileInfo#getLastModified()
	 */
	public static long getFileModificationDate(IFile fileHandle)
	{
		if (log.isTraceEnabled())
			log.trace("getFileModificationDate() - fileHandle: " + fileHandle);
		assert (fileHandle != null);

		if (!fileHandle.exists())
			return -1;

		try
		{
			IFileInfo fileInfo = EFS.getStore(fileHandle.getLocationURI()).fetchInfo();
			long modTime = fileInfo.getLastModified();

			if (modTime == EFS.NONE)
				return -1;
			else
				return modTime;
		}
		catch (CoreException e)
		{
			//WTF? something strange happened
			log.warn("getFileModificationDate() - unable to get file info - " + e, e);
			return -1;
		}

	}

	/**
	 * Retrieves the file size for the given file from the file system.
	 * 
	 * @param fileHandle the file to get the size for, never null.
	 * @return file size in bytes or -1 on error.
	 * 
	 * @see IFileInfo#getLength()
	 */
	public static long getFileSize(IFile fileHandle)
	{
		if (log.isTraceEnabled())
			log.trace("getFileSize() - fileHandle: " + fileHandle);
		assert (fileHandle != null);

		if (!fileHandle.exists())
			return -1;

		try
		{
			IFileInfo fileInfo = EFS.getStore(fileHandle.getLocationURI()).fetchInfo();
			long size = fileInfo.getLength();

			if (size == EFS.NONE)
				return -1;
			else
				return size;
		}
		catch (CoreException e)
		{
			//WTF? something strange happened
			log.warn("getFileSize() - unable to get file info - " + e, e);
			return -1;
		}

	}
}

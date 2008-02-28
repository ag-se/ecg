package org.electrocodeogram.cpc.core.utils;


import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.electrocodeogram.cpc.core.CPCCorePlugin;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider;


public class CoreConfigurationUtils
{
	private static Set<String> supportedFileTypes = CPCCorePlugin.getConfigurationRegistry().getSupportedFileTypes();

	private static IStoreProvider storeProvider = null;

	public static synchronized IStoreProvider getStoreProvider()
	{
		if (storeProvider == null)
			storeProvider = ((IStoreProvider) CPCCorePlugin.getProviderRegistry().lookupProvider(IStoreProvider.class));

		assert (storeProvider != null);
		return storeProvider;
	}

	/**
	 * Checks whether events for files of the given extension are of interest to some CPC modules.
	 * 
	 * @param fileExtension a file extension to check, not including the dot, i.e. "java", may be NULL.
	 * @return true if some CPC modules are interested in this file type.
	 */
	public static boolean isSupportedFileExtension(String fileExtension)
	{
		assert (supportedFileTypes != null);

		if (fileExtension == null)
			return false;

		return (supportedFileTypes.contains(fileExtension.toLowerCase()));
	}

	/**
	 * Checks whether some CPC modules might be interested receiving events for a given file.<br/>
	 * The decision is made based on the file extension.
	 * 
	 * @param file the file to check, may be NULL.
	 * @return true if some CPC modules are interested in this file type.
	 */
	public static boolean isSupportedFile(IResource file)
	{
		if (file == null)
			return false;

		if (!(file instanceof IFile))
			return false;

		return isSupportedFileExtension(file.getFileExtension());
	}

	/**
	 * Checks whether some CPC modules might be interested receiving events for a given file.<br/>
	 * The decision is made based on the file extension.
	 * 
	 * @param filePath the file to check, may be NULL.
	 * @return true if some CPC modules are interested in this file type.
	 */
	public static boolean isSupportedFile(IPath filePath)
	{
		if (filePath == null)
			return false;

		return isSupportedFileExtension(filePath.getFileExtension());
	}

	/**
	 * Checks whether some CPC modules might be interested receiving events for a given file.<br/>
	 * The decision is made based on the file extension.
	 * 
	 * @param fullFilePath the workspace relative path of the file to check, may be NULL.
	 * @return true if some CPC modules are interested in this file type.
	 */
	public static boolean isSupportedFile(String fullFilePath)
	{
		if (fullFilePath == null)
			return false;

		return isSupportedFileExtension(new Path(fullFilePath).getFileExtension());
	}

	/**
	 * Checks whether some CPC modules might be interested receiving events for a given file.<br/>
	 * The decision is made based on the file extension.
	 * 
	 * @param project the project which contains the file, may be NULL.
	 * @param filePath the project relative path of the file to check, may be NULL.
	 * @return true if some CPC modules are interested in this file type.
	 */
	public static boolean isSupportedFile(String project, String filePath)
	{
		if (project == null || filePath == null)
			return false;

		return isSupportedFileExtension(new Path(filePath).getFileExtension());
	}

	/**
	 * Checks whether the given file is a cpc data file.<br/>
	 * The check is made based on the file extension.<br/>
	 * <br/>
	 * By default cpc data files use the extension "<em>.cpc</em>".
	 * 
	 * @param file the file to check, never null.
	 * @return true if this is a cpc data file, false otherwise.
	 */
	public static boolean isCpcFile(IResource file)
	{
		if (file == null)
			return false;

		if (!(file instanceof IFile))
			return false;

		return isCpcFileExtension(file.getFileExtension());
	}

	/**
	 * Checks whether the given file extension is an extension used for cpc data files.<br/> 
	 * <br/>
	 * By default cpc data files use the extension "<em>.cpc</em>".
	 * 
	 * @param fileExtension the file extension to check, may be NULL.
	 * @return true if this extension is used for cpc data files, false otherwise.
	 */
	private static boolean isCpcFileExtension(String fileExtension)
	{
		if (fileExtension == null)
			return false;

		return "cpc".equalsIgnoreCase(fileExtension);
	}
}

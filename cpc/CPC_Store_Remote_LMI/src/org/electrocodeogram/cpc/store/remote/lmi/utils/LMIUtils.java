package org.electrocodeogram.cpc.store.remote.lmi.utils;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.history.IFileRevision;
import org.electrocodeogram.cpc.core.api.data.ICloneFile;
import org.electrocodeogram.cpc.core.utils.CoreFileUtils;
import org.electrocodeogram.cpc.core.utils.CoreStringUtils;
import org.electrocodeogram.cpc.store.remote.lmi.CPCStoreRemoteLMIPlugin;


public class LMIUtils
{
	private static final Log log = LogFactory.getLog(LMIUtils.class);

	private LMIUtils()
	{
		//this class is not meant to be instantiated
	}

	/**
	 * Checks if full merge data is available for the given <em>baseName</em>.<br/>
	 * Full merge data means a full 3-way-merge is possible:
	 * <ul>
	 * 	<li>local source file content</li>
	 * 	<li>local cpc data file content</li>
	 * 	<li>base source file content</li>
	 * 	<li>base cpc data file content</li>
	 * 	<li>remote source file content</li>
	 * 	<li>remote cpc data file content</li>
	 * </ul>
	 * The <em>baseName</em> is typically an {@link ICloneFile} UUID.
	 * 
	 * @param baseName the base name, i.e. a file UUID, never null.
	 * @return
	 */
	public static boolean isFullMergeDataAvailable(String baseName)
	{
		if (log.isTraceEnabled())
			log.trace("isFullMergeDataAvailable() - baseName: " + baseName);

		IPath stateLoc = CPCStoreRemoteLMIPlugin.getDefault().getStateLocation().makeAbsolute();
		stateLoc.append("temp");

		File[] tmpFiles = new File[] { new File(stateLoc.toString(), baseName + ".local.src"),
				new File(stateLoc.toString(), baseName + ".local.cpc"),
				new File(stateLoc.toString(), baseName + ".base.src"),
				new File(stateLoc.toString(), baseName + ".base.cpc"),
				new File(stateLoc.toString(), baseName + ".remote.src"),
				new File(stateLoc.toString(), baseName + ".remote.cpc"), };

		for (File tmpFile : tmpFiles)
		{
			if (!tmpFile.exists())
			{
				if (log.isTraceEnabled())
					log.trace("isFullMergeDataAvailable - result: false - missing file: " + tmpFile);
				return false;
			}
		}

		log.trace("isFullMergeDataAvailable - result: true");

		return true;
	}

	/**
	 * Copies the content of a given file revision to a temporary file inside the state location of
	 * this plugin.
	 * 
	 * @param baseName the base name, i.e. a file UUID, never null.
	 * @param type an additional extension for the given base name, i.e. "<em>base.cpc</em>", never null.
	 * @param fileRev the file revision to retrieve the content from, may be NULL.
	 * @return <em>true</em> on success, <em>false</em> on failure.
	 */
	public static boolean storeFileRevision(String baseName, String type, IFileRevision fileRev,
			IProgressMonitor monitor)
	{
		if (log.isTraceEnabled())
			log.trace("storeFileRevision() - baseName: " + baseName + ", type: " + type + ", fileRev: " + fileRev);
		assert (baseName != null && type != null);

		if (fileRev == null)
			return false;

		try
		{
			IStorage storage = fileRev.getStorage(monitor);
			if (storage == null)
				return false;

			String content = CoreFileUtils.readStreamContent(storage.getContents());
			if (content == null)
				return false;

			return storeTemporaryFile(baseName, type, content);
		}
		catch (CoreException e)
		{
			log.error("storeFileRevision() - error while storing file revision data - baseName: " + baseName
					+ ", fileRev: " + fileRev + " - " + e, e);
			return false;
		}
	}

	/**
	 * Reads the content of a temporary file with the given name inside the state location of
	 * this plugin.

	 * @param baseName the base name, i.e. a file UUID, never null.
	 * @param type an additional extension for the given base name, i.e. "<em>base.cpc</em>", never null.
	 * @return the content of the temporary file or NULL if the file doesn't exist or couldn't be read.
	 */
	public static String readFileRevision(String baseName, String type)
	{
		if (log.isTraceEnabled())
			log.trace("readFileRevision() - baseName: " + baseName + ", type: " + type);
		assert (baseName != null && type != null);

		return readTemporaryFile(baseName, type);
	}

	/**
	 * Creates a new file within the state location of this plugin and writes the given
	 * content to it. If the file already exists, it is overwritten.<br/>
	 * The naming scheme for the temporary file is "<em>baseName</em>.<em>type</em>".
	 * 
	 * @param fileName filename for the temporary file, must not contain any path segments, never null.
	 * @param content the content to write to the file, never null.
	 * @return <em>true</em> on success, <em>false</em> on failure.
	 */
	public static boolean storeTemporaryFile(String fileName, String type, String content)
	{
		if (log.isTraceEnabled())
			log.trace("storeTemporaryFile() - fileName: " + fileName + ", type:" + type + ", content: "
					+ CoreStringUtils.truncateString(content));
		assert (fileName != null && content != null && type != null);

		IPath stateLoc = CPCStoreRemoteLMIPlugin.getDefault().getStateLocation().makeAbsolute();
		stateLoc.append("temp");
		File tmpFile = new File(stateLoc.toString(), fileName + "." + type);

		try
		{
			BufferedWriter bw = new BufferedWriter(new FileWriter(tmpFile));
			bw.write(content);
			bw.close();
		}
		catch (IOException e)
		{
			log.error("storeTemporaryFile() - unable to write file - file: " + tmpFile + ", content: "
					+ CoreStringUtils.truncateString(content) + " - " + e, e);
			return false;
		}

		return true;
	}

	/**
	 * Reads the content of a temporary file within the state location of this plugin.<br/>
	 * The naming scheme for the temporary file is "<em>baseName</em>.<em>type</em>".
	 * 
	 * @param fileName filename of the temporary file, must not contain any path segments, never null.
	 * @return the content of the temporary file or NULL if the file doesn't exist or couldn't be read.
	 */
	public static String readTemporaryFile(String fileName, String type)
	{
		if (log.isTraceEnabled())
			log.trace("readTemporaryFile() - fileName: " + fileName + ", type:" + type);
		assert (fileName != null && type != null);

		IPath stateLoc = CPCStoreRemoteLMIPlugin.getDefault().getStateLocation().makeAbsolute();
		stateLoc.append("temp");
		File tmpFile = new File(stateLoc.toString(), fileName + "." + type);

		String result = null;

		if (tmpFile.exists())
		{
			if (tmpFile.length() == 0)
			{
				result = "";
			}
			else
			{
				try
				{
					char[] buf = new char[(int) tmpFile.length()];
					BufferedReader br = new BufferedReader(new FileReader(tmpFile));
					int read = br.read(buf);
					br.close();

					result = new String(buf, 0, read);
				}
				catch (IOException e)
				{
					log.error("readTemporaryFile() - unable to read file - file: " + tmpFile + " - " + e, e);
					result = null;
				}
			}
		}

		if (log.isTraceEnabled())
			log.trace("readTemporaryFile() - result: " + CoreStringUtils.truncateString(result));

		return result;
	}
}

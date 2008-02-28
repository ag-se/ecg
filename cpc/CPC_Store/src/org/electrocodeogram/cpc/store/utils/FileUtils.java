package org.electrocodeogram.cpc.store.utils;


import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.Path;


public class FileUtils
{
	public static final String CPC_UUID_FILE_COMMENT_PREFIX = "#CPC - DO NOT REMOVE OR MODIFY - UUID [";
	public static final String CPC_UUID_FILE_COMMENT_POSTFIX = "]#";
	public static final String CPC_DATA_DIR = ".cpc";
	public static final String CPC_DATA_FILE_SUFFIX = ".dat.cpc";

	private static Log log = LogFactory.getLog(FileUtils.class);

	/**
	 * Checks the given file to see whether it contains a special CPC UUID comment.<br/>
	 * UUID Comment Content:<br/>
	 * <code>/+ #CPC - DO NOT REMOVE OR MODIFY - UUID [uuid]# +/</code>
	 * (where + should be *)
	 * 
	 * @param fileHandle the file to check, may be null (will return null)
	 * @return uuid or null if the file can not be read or does not contain a CPC UUID
	 * 
	 * @deprecated if we don't append a CPC UUID to files anywhere, there is no need to search for them either...
	 */
	@Deprecated
	public static String extractUuid(IFile fileHandle)
	{
		if (log.isTraceEnabled())
			log.trace("extractUuid(): " + fileHandle);

		if ((fileHandle == null) || (!fileHandle.exists()))
		{
			log.trace("extractUuid() - result: file not found");
			return null;
		}

		try
		{
			//read content (we don't care about sync status here)
			BufferedReader reader = new BufferedReader(new InputStreamReader(fileHandle.getContents(true)));
			String line;
			int pos;

			//read the file line by line
			while ((line = reader.readLine()) != null)
			{
				//for each line check if it contains a CPC UUID comment
				pos = line.indexOf(CPC_UUID_FILE_COMMENT_PREFIX);
				if (pos >= 0)
				{
					//ok, we found something!
					if (log.isTraceEnabled())
						log.trace("extractUuid() - found UUID comment at pos " + pos + " in line: " + line);

					//the uuid starts directly after the prefix
					int uuidStartPos = pos + CPC_UUID_FILE_COMMENT_PREFIX.length();

					//it ends at the postfix
					int uuidEndPos = line.indexOf(CPC_UUID_FILE_COMMENT_POSTFIX, uuidStartPos);
					if (uuidEndPos > 0)
					{
						if (log.isTraceEnabled())
							log.trace("extractUuid() - extract uuid - start: " + uuidStartPos + ", end: " + uuidEndPos
									+ ", line: " + line);

						//ok, we have start and end pos, extract the uuid
						String uuid = line.substring(uuidStartPos, uuidEndPos);

						if (log.isTraceEnabled())
							log.trace("extractUuid() - result: " + uuid);

						//make sure we close the file
						reader.close();

						return uuid;
					}
					else
					{
						//this shouldn't happen! our cpc uuid comment seems to be broken
						log.warn("extractUuid() - invalid CPC UUID comment - file: "
								+ fileHandle.getFullPath().toString() + ", line: " + line + ", pos: " + pos);
					}
				}
			}

			//make sure we close the file
			reader.close();
		}
		catch (Exception e)
		{
		}

		log.trace("extractUuid() - result: no UUID found");

		return null;
	}

	/**
	 * Checks whether a cpc clone data file exists for the given source file.
	 * 
	 * @param fileHandle java source file to get cpc clone data file for
	 * @return handle for clone data file or null if not found, also null if fileHandle is not a java file.
	 */
	public static IFile getCloneDataFile(IFile fileHandle)
	{
		if (log.isTraceEnabled())
			log.trace("getCloneDataFile(): " + fileHandle);

		//make sure this is a java file
		if (!"java".equals(fileHandle.getFileExtension()))
		{
			log.trace("getCloneDataFile() - result: not a JAVA file");
			return null;
		}

		//get the cpc data folder
		IFolder cpcFolder = fileHandle.getParent().getFolder(new Path(CPC_DATA_DIR));
		if ((cpcFolder == null) || (!cpcFolder.exists()))
		{
			if (log.isTraceEnabled())
				log.trace("getCloneDataFile() - result: no CPC folder - " + cpcFolder);

			return null;
		}

		//now check for a cpc clone data file
		IFile cpcFile = cpcFolder.getFile(new Path(fileHandle.getName() + CPC_DATA_FILE_SUFFIX));
		if ((cpcFile == null) || (!cpcFile.exists()))
		{
			if (log.isTraceEnabled())
				log.trace("getCloneDataFile() - result: no CPC data file - " + cpcFile);

			return null;
		}

		if (log.isTraceEnabled())
			log.trace("getCloneDataFile() - result: " + cpcFile);

		//ok, we found a valid cpc clone data file
		return cpcFile;
	}
}

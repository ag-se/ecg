package org.electrocodeogram.cpc.core.utils;


import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.data.ICloneDataElement;
import org.electrocodeogram.cpc.core.api.data.ICloneFile;
import org.electrocodeogram.cpc.core.api.data.special.ICloneObjectExtensionMultiStatefulObject;
import org.electrocodeogram.cpc.core.api.data.special.ICloneObjectExtensionStatefulObject;
import org.electrocodeogram.cpc.core.api.provider.data.ICloneFactoryProvider;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider;


/**
 * A collection of general utility methods.
 * <p>
 * This class is not meant to be instantiated. 
 * 
 * @author vw
 */
public class CoreUtils
{
	private static Log log = LogFactory.getLog(CoreUtils.class);

	private static String username;

	/**
	 * This class is not meant to be instantiated.
	 */
	private CoreUtils()
	{

	}

	/**
	 * Uses the java util UUID class to generate globally unique UUIDs.<br/>
	 * These UUIDs are type 4 random UUIDs, so a collision is extremely unlikely but not impossible.
	 * 
	 * TODO: vw - maybe we should look for some a usable type 1 (MAC address+time based) generator?
	 * 
	 * @return globally unique UUID for use in unique keys
	 * @see UUID
	 */
	public static String generateUUID()
	{
		//VMID uuid = new VMID();
		String uuid = UUID.randomUUID().toString();

		if (log.isTraceEnabled())
			log.trace("generateUUID(): " + uuid);

		return uuid;
	}

	public static String getLocationFromPart(IWorkbenchPart part)
	{
		String location = part.getTitleToolTip();
		String title = part.getTitle();
		if (location == null || location.length() == 0)
			location = title;
		if (location == null)
			return "{null}";
		if (title != null)
		{
			String[] suffixes = { ".class" };
			for (int i = 0; i < suffixes.length; i++)
				if (!location.endsWith(suffixes[i]) && title.endsWith(suffixes[i]))
					location = location + suffixes[i];
		}

		//internal location strings as returned by IFile or a file buffer begin with
		//a /. We need to emulate this as we're using these location strings as hash keys
		//in some areas.
		if (location.charAt(0) != '/')
			location = '/' + location;

		return location;
	}

	public static String getProjectRelativePathFromPart(IWorkbenchPart part)
	{
		return getProjectRelativePathFromLocation(getLocationFromPart(part));
	}

	public static String getProjectRelativePathFromLocation(String location)
	{
		if (location != null)
		{
			if (location.charAt(0) == IPath.SEPARATOR)
				location = location.substring(1);
			int sepIndex = location.indexOf(IPath.SEPARATOR);
			if (sepIndex != -1)
			{
				String res = location.substring(sepIndex + 1);
				return res;
			}
			else
				return location;
		}
		return "";
	}

	public static String getProjectnameFromPart(IWorkbenchPart part)
	{
		return getProjectnameFromLocation(getLocationFromPart(part));
	}

	public static String getProjectnameFromLocation(String location)
	{
		if (location != null)
		{
			if (location.charAt(0) == IPath.SEPARATOR)
				location = location.substring(1);
			int sepIndex = location.indexOf(IPath.SEPARATOR);
			if (sepIndex != -1)
			{
				String res = location.substring(0, sepIndex);
				return res;
			}
		}
		return "";
	}

	/**
	 * Extracts the name of the project for the given file.<br/>
	 * Mostly equal to <code>file.getProject().getName()</code>, but adds some special
	 * handling for cases where the project was deleted or closed.
	 * 
	 * @param file the file to get the project name for, may be NULL.
	 * @return the project name or NULL if the file was null or the project name could not be determined.
	 */
	public static String getProjectnameFromFile(IResource file)
	{
		if (file == null)
			return null;

		String project = file.getProject().getName();

		if (project == null || project.equals(""))
		{
			//we need to fall back to manually extracting the project name
			project = getProjectnameFromLocation(file.getFullPath().toString());
			if (log.isDebugEnabled())
				log.debug("getProjectnameFromFile() - falling back to manual project name extraction - file: " + file
						+ " - project: " + project);
		}

		if (project == null || project.equals(""))
		{
			log.error("getProjectnameFromFile() - failed to extract project name, returning NULL - file: " + file
					+ " - project: " + project, new Throwable());
			return null;
		}

		return project;
	}

	/**
	 * Extracts the project relative path for the given file.
	 * 
	 * @param file the file to get the project relative path for, may be NULL.
	 * @return the project relative path or NULL if the file was null or path could not be determined.
	 */
	public static String getProjectRelativePathFromFile(IFile file)
	{
		if (file == null)
			return null;

		String filePath;

		if (file.getProjectRelativePath() == null || file.getProjectRelativePath().toString().equals(""))
		{
			filePath = getProjectRelativePathFromFile(file);
			if (log.isDebugEnabled())
				log.debug("getProjectRelativePathFromFile() - falling back to manual file path extraction - file: "
						+ file + " - filePath: " + filePath);
		}
		else
		{
			filePath = file.getProjectRelativePath().toString();
		}

		if (filePath == null || filePath.equals(""))
		{
			log.error("getProjectRelativePathFromFile() - failed to extract file path, returning NULL - file: " + file
					+ " - filePath: " + filePath, new Throwable());
			return null;
		}

		return filePath;
	}

	public static String arrayToString(int[] array)
	{
		if (array == null)
			return "null";

		StringBuilder result = new StringBuilder();
		result.append("{");

		for (int i = 0; i < array.length; ++i)
		{
			if (i > 0)
				result.append(", ");

			result.append(array[i]);
		}

		result.append("}");

		return result.toString();
	}

	public static String arrayToString(byte[] array)
	{
		if (array == null)
			return "null";

		StringBuilder result = new StringBuilder();
		result.append("{");

		for (int i = 0; i < array.length; ++i)
		{
			if (i > 0)
				result.append(", ");

			result.append(array[i]);
		}

		result.append("}");

		return result.toString();
	}

	public static String arrayToString(Object[] array)
	{
		if (array == null)
			return "null";

		StringBuilder result = new StringBuilder();
		result.append("{");

		for (int i = 0; i < array.length; ++i)
		{
			if (i > 0)
				result.append(", ");

			result.append(array[i]);
		}

		result.append("}");

		return result.toString();
	}

	/**
	 * Convenience method.<br/>
	 * <br/>
	 * Calls {@link #getCloneFileForFile(IStoreProvider, IFile, boolean)} with <em>createNewUuidIfNeeded</em>
	 * set to <em>true</em>.
	 * 
	 * @see #getCloneFileForFile(IStoreProvider, IFile, boolean)
	 */
	public static ICloneFile getCloneFileForFile(IStoreProvider storeProvider, IFile fileHandle)
	{
		return getCloneFileForFile(storeProvider, fileHandle, true);
	}

	/**
	 * Takes a valid store provider and an {@link IFile} handle and returns a matching {@link ICloneFile}.
	 * 
	 * @param storeProvider a valid store provider, never null.
	 * @param fileHandle a file handle for the file to lookup, never null.
	 * @param createNewUuidIfNeeded whether to create a new uuid if the file doesn't have one yet,
	 * 	see: {@link IStoreProvider#lookupCloneFileByPath(String, String, boolean, boolean)}.
	 * @return an ICloneFile handle or NULL on error.
	 * 
	 * @see IStoreProvider#lookupCloneFileByPath(String, String, boolean, boolean)
	 */
	public static ICloneFile getCloneFileForFile(IStoreProvider storeProvider, IFile fileHandle,
			boolean createNewUuidIfNeeded)
	{
		if (log.isTraceEnabled())
			log.trace("getCloneFileForFile() - storeProvider: " + storeProvider + ", fileHandle: " + fileHandle
					+ ", createNewUuidIfNeeded: " + createNewUuidIfNeeded);
		assert (storeProvider != null && fileHandle != null);

		return storeProvider.lookupCloneFileByPath(getProjectnameFromFile(fileHandle),
				getProjectRelativePathFromFile(fileHandle), createNewUuidIfNeeded, false);
	}

	/**
	 * Checks if any text editor is currently open for the given file handle and returns a
	 * handle to the {@link ITextEditor} instance.
	 * 
	 * @param fileHandle the file handle to get an editor for, never null.
	 * @return a currently open {@link ITextEditor} for the given file handle, NULL if no matching editor was found.
	 */
	public static ITextEditor getTextEditorForFile(IFile fileHandle)
	{
		if (log.isTraceEnabled())
			log.trace("getTextEditorForFile() - fileHandle: " + fileHandle);
		assert (fileHandle != null);

		//we're going to search for this file as editor input later
		IEditorInput editorInput = new FileEditorInput(fileHandle);

		//now check all open editors to see whether we already have an open copy of that file
		IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
		for (IWorkbenchWindow window : windows)
		{
			//check each window
			IWorkbenchPage[] pages = window.getPages();
			for (IWorkbenchPage page : pages)
			{
				//check each page
				IEditorPart editor = page.findEditor(editorInput);
				if (editor != null)
				{
					if (log.isTraceEnabled())
						log.trace("getTextEditorForFile() - found corresponding editor: " + editor);

					if ((editor != null) && (editor instanceof ITextEditor))
					{
						return (ITextEditor) editor;
					}
					else
					{
						if (log.isDebugEnabled())
							log.debug("getTextEditorForFile() - unexpected editor type, ignoring - editor: " + editor
									+ ", fileHandle: " + fileHandle);
					}
				}
			}
		}

		return null;
	}

	/**
	 * Checks if any text editor is currently open for the given file location and returns a
	 * handle to the {@link ITextEditor} instance.
	 * 
	 * @param location full, workspace relative path to the file in question, never null.
	 * @return a currently open {@link ITextEditor} for the given file location, NULL if file was not found or
	 * 		if no matching editor was found.
	 */
	public static ITextEditor getTextEditorForLocation(IPath location)
	{
		//get a file handle
		IFile fileHandle = ResourcesPlugin.getWorkspace().getRoot().getFile(location);
		if (fileHandle == null || !fileHandle.exists())
			return null;

		return getTextEditorForFile(fileHandle);
	}

	/**
	 * Convenience method.
	 * 
	 * @param file the file to check for, never null.
	 * @return true if the file is currently open in an editor.
	 */
	public static boolean isFileOpenInEditor(IFile file)
	{
		return getTextEditorForFile(file) != null;
	}

	/**
	 * Takes a file handle and tries to obtain the content of the corresponding file
	 * either from an open editor window for that file or from the file system if no
	 * editor is open.
	 * 
	 * @param fileHandle the file to retrieve the content for, never null.
	 * @return the content of the file or NULL on error.
	 */
	public static String getFileContentFromEditorOrFile(IFile fileHandle)
	{
		if (log.isTraceEnabled())
			log.trace("getFileContentFromEditorOrFile() - fileHandle: " + fileHandle);
		assert (fileHandle != null);

		String result = null;

		//now check all open editors to see whether we already have an open copy of that file
		ITextEditor textEditor = CoreUtils.getTextEditorForFile(fileHandle);
		if (textEditor != null)
		{
			if (log.isTraceEnabled())
				log.trace("getFileContentFromEditorOrFile() - found corresponding editor: " + textEditor);

			IDocumentProvider provider = textEditor.getDocumentProvider();
			IDocument document = provider.getDocument(textEditor.getEditorInput());

			result = document.get();
		}

		//check if we got the clone content from one of the open editors
		if (result == null)
		{
			//nope, no clone content yet, we'll have to fall back to the IFile resource
			if (log.isTraceEnabled())
				log
						.trace("getFileContentFromEditorOrFile() - no suitable open editor to extract clone content, falling back to file resource: "
								+ fileHandle);

			result = readFileContent(fileHandle);
		}

		if (log.isTraceEnabled())
			log.trace("getFileContentFromEditorOrFile() - result: " + CoreStringUtils.truncateString(result));

		return result;
	}

	/**
	 * Retrieves a reference to the currently active text editor.
	 * 
	 * @return currently active text editor or NULL if none was found.
	 */
	public static ITextEditor getActiveTextEditor()
	{
		if (log.isTraceEnabled())
			log.trace("getActiveTextEditor()");

		IWorkbench wb = PlatformUI.getWorkbench();
		IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
		if (win == null)
			return null;
		IWorkbenchPage page = win.getActivePage();
		if (page == null)
			return null;
		IEditorPart editor = page.getActiveEditor();
		if (editor == null)
			return null;

		if (editor instanceof ITextEditor)
			return (ITextEditor) editor;
		else
			return null;
	}

	/**
	 * Retrieves an {@link IFile} handle for the file which is loaded in the currently
	 * active text editor.
	 * 
	 * @return file handle for currently active file or NULL if none could be found.
	 */
	public static IFile getActiveTextEditorFile()
	{
		ITextEditor editor = getActiveTextEditor();
		if (editor == null)
			return null;

		if (editor.getEditorInput() != null && editor.getEditorInput() instanceof FileEditorInput)
			return ((FileEditorInput) editor.getEditorInput()).getFile();
		else
			return null;
	}

	/**
	 * Retrieves the content for the given file handle.
	 * 
	 * @param fileHandle the file to retrieve the content for, may be null.
	 * @return the content of the file or NULL if the file handle was null, the file didn't exist or was not readable.
	 */
	public static String readFileContent(IFile fileHandle)
	{
		if (log.isTraceEnabled())
			log.trace("readFileContent() - fileHandle: " + fileHandle);

		if (fileHandle == null || !fileHandle.exists())
		{
			log.trace("readFileContent() - result: null");
			return null;
		}

		try
		{
			long len = EFS.getStore(fileHandle.getLocationURI()).fetchInfo().getLength();
			byte[] buf = new byte[(int) len];

			BufferedInputStream bis = new BufferedInputStream(fileHandle.getContents(true));
			int read = bis.read(buf);
			bis.close();

			if (read != len)
			{
				log.error("readFileContent() - short read while reading file - expected: " + len + ", read: " + read
						+ ", file: " + fileHandle, new Throwable());
				return null;
			}

			String result = new String(buf);

			if (log.isTraceEnabled())
				log.trace("readFileContent() - result: " + CoreStringUtils.truncateString(result));

			return result;
		}
		catch (Exception e)
		{
			log.error("readFileContent() - error while reading file: " + fileHandle + " - " + e, e);
			return null;
		}
	}

	/**
	 * Writes the given content to the file.
	 * 
	 * @param fileHandle the file to write to, never null.
	 * @param content the content to write, never null.
	 * @throws CoreException on error
	 */
	public static void writeFileContent(IFile fileHandle, String content) throws CoreException
	{
		assert (fileHandle != null && content != null);

		if (log.isTraceEnabled())
			log.trace("writeFileContent() - fileHandle: " + fileHandle + ", content: "
					+ CoreStringUtils.truncateString(content));

		ByteArrayInputStream bArrIS = new ByteArrayInputStream(content.getBytes());
		if (!fileHandle.exists())
			fileHandle.create(bArrIS, true, null);
		else
			fileHandle.setContents(bArrIS, true, true, null);
		//TODO: do we need to close the input stream?
		//bArrIS.close();
	}

	/**
	 * Returns the {@link IConfigurationElement} for the extension with the highest <em>priority</em> attribute
	 * value for the given extension point.<br/>
	 * If the highest priority is shared by multiple extensions, one will arbitrarily selected and returned.<br/>
	 * Extensions which are malformed are ignored (an error is logged).
	 * 
	 * @param extensionPoint the extension point to parse, never null.
	 * @return the {@link IConfigurationElement} with the highest priority or null if no extension is registered.
	 */
	public static IConfigurationElement getHighestPriorityExtensionFor(String extensionPoint)
	{
		assert (extensionPoint != null);

		IConfigurationElement highestElement = null;
		byte highestPriority = Byte.MIN_VALUE;

		IConfigurationElement[] extensions = Platform.getExtensionRegistry()
				.getConfigurationElementsFor(extensionPoint);
		for (IConfigurationElement element : extensions)
		{
			try
			{
				byte currentPriority = Byte.parseByte(element.getAttribute("priority"));
				if (currentPriority > highestPriority)
				{
					highestElement = element;
					highestPriority = currentPriority;
				}
			}
			catch (Exception e)
			{
				log.error("getHighestPriorityExtensionFor() - unable to obtain priority for extension - point: "
						+ extensionPoint + ", element: " + element + " - " + e, e);
			}
		}

		return highestElement;
	}

	/**
	 * Takes a plugin id and checks whether the plugin was deactivated by some other plugin.<br/>
	 * A plugin is considered deactivated if at least one extension was registered for the
	 * special "deactivate" extension point. 
	 * 
	 * @param pluginId the plugin id of the plugin which should be checked for deactivation, never null.
	 * @return true if the plugin was deactivated.
	 */
	public static boolean pluginIsDeactivated(String pluginId)
	{
		assert (pluginId != null);

		if (log.isTraceEnabled())
			log.trace("pluginIsDeactivated() - pluginId: " + pluginId);

		String extensionPoint = pluginId + ".deactivate";
		IExtension[] extensions = Platform.getExtensionRegistry().getExtensionPoint(extensionPoint).getExtensions();

		if (log.isTraceEnabled())
			log.trace("pluginIsDeactivated() - extensionPoint: " + extensionPoint + ", extensions: "
					+ arrayToString(extensions));

		if (extensions == null || extensions.length == 0)
		{
			log.trace("pluginIsDeactivated() - result: false");
			return false;
		}
		else
		{
			log.trace("pluginIsDeactivated() - result: true");
			return true;
		}
	}

	/**
	 * Retrieves the username for the currently logged in user.
	 */
	public static synchronized String getUsername()
	{
		if (username == null)
		{
			username = System.getProperty("user.name");
			if (username == null || username.equals(""))
			{
				username = "unknown";
			}
		}

		return username;
	}

	/**
	 * Checks whether two strings are equal if line break encoding (DOS/UNIX/MAC) is ignored.
	 * 
	 * @param stringA a string to compare, may be null.
	 * @param stringB a string to compare, may be null.
	 * @return true if the only difference between the given strings is line break encoding
	 */
	public static boolean equalIgnoringLineBreak(String stringA, String stringB)
	{
		if (stringA == null && stringB == null)
			return true;

		if (stringA == null || stringB == null)
			return false;

		if (stringA.equals(stringB))
			return true;

		//now check if the two strings are equal if \n and \r\n are all treated equally
		//TODO: this may be slow and it does not work for files which contain only \r line terminators
		//		macs do that, don't they?
		stringA = stringA.replaceAll("\r", "");
		stringB = stringB.replaceAll("\r", "");
		if (stringA.equals(stringB))
			return true;

		return false;
	}

	/**
	 * Takes a collection of strings and converts it into a comma separated string.<br/>
	 * If the collection contains elements, the last character is always a comma.<br/>
	 * If the collection is empty or null an empty string is returned.<br/>
	 * <br/>
	 * i.e. A collection containing "<em>A</em>", "<em>B</em>" and "<em>C</em>" will yield: "<em>A,B,C,</em>"
	 * 
	 * @param collection the collection to convert into a string, may be NULL.
	 * @return empty string for null or empty collection, comma separated list of all elements otherwise, never null.
	 */
	public static String collectionToString(Collection<String> collection)
	{
		if (log.isTraceEnabled())
			log.trace("collectionToString(): " + collection);

		if (collection == null || collection.isEmpty())
			return "";

		StringBuilder sb = new StringBuilder();
		for (String str : collection)
		{
			sb.append(str);
			sb.append(',');
		}

		String result = sb.toString();

		if (log.isTraceEnabled())
			log.trace("collectionToString() - result: " + result);

		return result;
	}

	/**
	 * Takes a string in the format generated by {@link CoreUtils#collectionToString(Collection)} and converts
	 * it back into a string collection.
	 * 
	 * @param string the string to convert into a collection, may be NULL.
	 * @return a collection corresponding to the data stored in the string, empty collection if no data is present, never null.
	 */
	public static Collection<String> collectionFromString(String string)
	{
		if (log.isTraceEnabled())
			log.trace("collectionFromString(): " + string);

		if (string == null || string.equals(""))
			return new ArrayList<String>(0);

		Collection<String> result = null;

		if (string.indexOf(',') == -1)
		{
			result = new ArrayList<String>(1);
			result.add(string);
		}
		else
		{
			String[] splitResult = string.split(",");
			result = new ArrayList<String>(splitResult.length);
			for (String str : splitResult)
			{
				//not interested in empty parts
				if ("".equals(str))
					continue;

				result.add(str);
			}
		}

		if (log.isTraceEnabled())
			log.trace("collectionFromString() - result: " + result);

		return result;
	}

	/**
	 * Takes a String, a Collection or an Array and returns its length in brackets.
	 * 
	 * @param obj a {@link String}, a {@link Collection} or an {@link Array}, may be NULL.
	 * @return "[length]" or "[null]" if obj is null, never null.
	 */
	public static String objectToLength(Object obj)
	{
		if (obj == null)
			return "[null]";

		if (obj instanceof String)
			return "[" + ((String) obj).length() + "]";

		if (obj instanceof Collection)
			return "[" + ((Collection<?>) obj).size() + "]";

		if (obj.getClass().isArray())
			return "[" + Array.getLength(obj) + "]";

		return "[unknown object: " + obj + "]";
	}

	/**
	 * @see CoreUtils#cloneCloneList(List, List, boolean)
	 */
	public static void cloneCloneList(List<IClone> source, List<IClone> destination)
	{
		cloneCloneList(source, destination, false);
	}

	/**
	 * Takes a source and destination list and copies clones of all entries of the source
	 * list to the destination list.
	 * 
	 * @param source list from which elements are copied (cloned), never null.
	 * @param destination list to which the cloned elements are added, never null.
	 * @param seal true if all cloned elements should also be sealed.
	 */
	public static void cloneCloneList(List<IClone> source, List<IClone> destination, boolean seal)
	{
		assert (source != null && destination != null);

		for (IClone elem : source)
		{
			assert (elem != null);
			try
			{
				IClone clonedElem = (IClone) elem.clone();
				if (seal)
					clonedElem.seal();
				destination.add(clonedElem);
			}
			catch (CloneNotSupportedException e)
			{
				//this should never happen
				log.error("cloneCloneList() - cloning failed - elem: " + elem + " - " + e, e);
				return;
			}
		}
	}

	/**
	 * Takes a list of clone objects and returns a new list with cloned copies of these objects.
	 *
	 * @return new list with cloned copies, never null.
	 * 
	 * @see CoreUtils#cloneCloneList(List, List, boolean)
	 */
	public static List<IClone> cloneCloneList(List<IClone> source, boolean seal)
	{
		assert (source != null);

		List<IClone> result = new ArrayList<IClone>(source.size());

		cloneCloneList(source, result, seal);

		return result;
	}

	/**
	 * Takes a list of {@link ICloneDataElement} objects and seals all of them.
	 * 
	 * @param list a list of objects to seal, never null.
	 */
	public static void sealList(List<ICloneDataElement> list)
	{
		assert (list != null);

		for (ICloneDataElement element : list)
		{
			assert (element != null);
			element.seal();
		}
	}

	/**
	 * Takes two {@link ICloneObjectExtensionMultiStatefulObject} instances and merges their data together into a new
	 * {@link ICloneObjectExtensionMultiStatefulObject} instance.<br/>
	 * <br/>
	 * First all data from the <b>first</b> extension is copied into the result extension. Then all data from the
	 * <b>second</b> extension is copied. If any sub-element exists in both extensions, the version in the <b>second</b>
	 * extension will <u>overwrite</u> the version from the <b>first</b> extension.<br/>
	 * <br/>
	 * The given instances are <b>not</b> modified.
	 * 
	 * @param cloneFactoryProvider a valid {@link ICloneFactoryProvider} reference, never null.
	 * @param baseExtension the base extension, never null.
	 * @param incrementalExtension an extension with new, but only partial data, which should be merged into the base extension data, never null. 
	 * @return a new {@link ICloneObjectExtensionMultiStatefulObject} which contains the merged data, never null.
	 * 
	 * TODO: move this method into CPC_Store
	 */
	public static ICloneObjectExtensionMultiStatefulObject mergeMultiExtensions(
			ICloneFactoryProvider cloneFactoryProvider, ICloneObjectExtensionMultiStatefulObject baseExtension,
			ICloneObjectExtensionMultiStatefulObject incrementalExtension)
	{
		if (log.isTraceEnabled())
			log.trace("mergeMultiExtensions() - baseExtension: " + baseExtension + ", incrementalExtension: "
					+ incrementalExtension);
		assert (cloneFactoryProvider != null && baseExtension != null && incrementalExtension != null);

		//first get the state data from both extensions
		List<List<Map<String, Comparable<? extends Object>>>> baseStates = baseExtension.getMultiState();
		List<List<Map<String, Comparable<? extends Object>>>> incrementalStates = incrementalExtension.getMultiState();

		//make sure there is actually anything to merge
		boolean allEmpty = true;
		for (List<Map<String, Comparable<? extends Object>>> stateList : incrementalStates)
			if (!stateList.isEmpty())
				//there is at least one state map which needs to be merged
				allEmpty = false;

		if (allEmpty)
		{
			//there is no additional data in the incrementalExtension, we can just return a clone of the base extension
			try
			{
				log
						.trace("mergeMultiExtensions() - incremental extension had no sub-elements, returning clone of base extension.");
				return (ICloneObjectExtensionMultiStatefulObject) baseExtension.clone();
			}
			catch (CloneNotSupportedException e)
			{
				log.fatal("mergeMultiExtensions() - error while cloning extension - " + baseExtension + " - " + e, e);
				assert (false);
			}
		}

		//create a list for the result states, the size of the outer list will always match the size of
		//the outer list in both other lists
		List<List<Map<String, Comparable<? extends Object>>>> resultStates = new ArrayList<List<Map<String, Comparable<? extends Object>>>>(
				baseStates.size());

		//create a new extension result object
		ICloneObjectExtensionMultiStatefulObject result = (ICloneObjectExtensionMultiStatefulObject) cloneFactoryProvider
				.getInstance(baseExtension.getExtensionInterfaceClass());
		assert (result != null);

		/*
		 * We know that both state lists are sorted in ascending order according to their unique key.
		 * The baseStates list is potentially very big, the incrementalStates list is usually very small.
		 * Furthermore, we won't be removing any entries.
		 * 
		 * We thus do a "merge sort", merging approach of two sorted lists. Where elements of the second
		 * list may overwrite those of the first.
		 */
		mergeStateListsByKey(baseExtension.getMultiPersistenceObjectIdentifier(), baseStates, incrementalStates,
				resultStates);

		//set stateful object data from incremental extension
		result.setState(incrementalExtension.getState());

		//set sub-element data from merged state list
		result.setMultiState(resultStates);

		if (log.isTraceEnabled())
			log.trace("mergeMultiExtensions() - result: " + result);
		return result;
	}

	/**
	 * Merges two list of {@link ICloneObjectExtensionStatefulObject} states into a result list.
	 * 
	 * @param keys the unique keys for these state maps, never null.
	 * @param baseStates
	 * @param incrementalStates
	 * @param resultStates resulting list of state maps, should be initially empty, never null.
	 */
	@SuppressWarnings("unchecked")
	private static void mergeStateListsByKey(List<String> keys,
			List<List<Map<String, Comparable<? extends Object>>>> baseStates,
			List<List<Map<String, Comparable<? extends Object>>>> incrementalStates,
			List<List<Map<String, Comparable<? extends Object>>>> resultStates)
	{
		assert (keys != null && baseStates != null && incrementalStates != null && resultStates != null
				&& resultStates.isEmpty() && keys.size() == baseStates.size() && keys.size() == incrementalStates
				.size());

		//do this for every list separately
		for (int topPos = 0; topPos < baseStates.size(); ++topPos)
		{
			//get the current key
			String key = keys.get(topPos);

			//get the two lists
			List<Map<String, Comparable<? extends Object>>> curBaseStates = baseStates.get(topPos);
			List<Map<String, Comparable<? extends Object>>> curIncrStates = incrementalStates.get(topPos);

			//prepare the result sub-list
			//the worst case size of this list is the sum of the base and incremental sizes
			List<Map<String, Comparable<? extends Object>>> curResultList = new ArrayList<Map<String, Comparable<? extends Object>>>(
					curBaseStates.size() + curIncrStates.size());
			resultStates.add(curResultList);

			//position within the baseStates list
			int basePos = 0;
			Map<String, Comparable<? extends Object>> baseState = null;

			//position within the incrementalStates list
			int incPos = 0;
			int lastIncrPos = -1;
			Map<String, Comparable<? extends Object>> curIncrState = null;

			//until we've reached the end of both lists
			while (true)
			{
				/*
				 * check if we've reached the end of any list
				 */
				if (basePos >= curBaseStates.size())
				{
					//we've handled all base states, just copy over any left over
					//incremental states, then we're done.
					while (incPos < curIncrStates.size())
						curResultList.add(curIncrStates.get(incPos++));

					break;
				}
				if (incPos >= curIncrStates.size())
				{
					//we've handled all incremental states, just copy over any left over
					//base states, then we're done.
					while (basePos < curBaseStates.size())
						curResultList.add(curBaseStates.get(basePos++));

					break;
				}

				/*
				 * get current elements
				 */
				baseState = curBaseStates.get(basePos);
				if (incPos != lastIncrPos)
				{
					//minimal performance tweak, as we usually compare the same incr. state against
					//a lot of base states
					curIncrState = curIncrStates.get(incPos);
					lastIncrPos = incPos;
				}

				/*
				 * check if this base state's key is smaller than the currently pending incremental state's key
				 */
				//FIXME: why do I need this cast here?
				int cmp = ((Comparable) baseState.get(key)).compareTo((Comparable) curIncrState.get(key));

				if (cmp < 0)
				{
					//the base state has a lower key value than the current incremental state
					//add the base state to the result list and continue
					curResultList.add(baseState);

					//next base state
					++basePos;
				}
				else if (cmp == 0)
				{
					//the current incremental state is an replacement for the current base state
					//add the incremental state to the result list
					curResultList.add(curIncrState);

					//move to the next incremental state
					++incPos;

					//next base state
					++basePos;
				}
				else
				{
					//the current incremental state needs to be inserted at this point
					curResultList.add(curIncrState);

					//move to the next incremental state
					++incPos;

					//we recheck the current state during the next loop
				}

			}

		}

	}
}

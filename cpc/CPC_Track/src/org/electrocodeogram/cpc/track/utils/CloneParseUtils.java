package org.electrocodeogram.cpc.track.utils;


import java.io.BufferedInputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.data.ICloneFile;
import org.electrocodeogram.cpc.core.utils.CoreFileUtils;
import org.electrocodeogram.cpc.core.utils.CoreUtils;


public class CloneParseUtils
{
	private static Log log = LogFactory.getLog(CloneParseUtils.class);

	/**
	 * This class is not meant to be instantiated.
	 */
	private CloneParseUtils()
	{

	}

	/**
	 * Retrieves the content for the offset range of the given clone.<br/>
	 * This is done by first checking the provided <em>editorContent</em>.
	 * If that is NULL then the buffers of all open editor windows are checked.
	 * Uf the clone file is currently not open in any editor, the clone content
	 * is extracted from the file itself.
	 * 
	 * @param cloneFile the clone file for the clone, must not be null
	 * @param clone the clone to retrieve the content for, must not be null
	 * @param editorContent the content of the editor/text file from which the clone content should be extracted.
	 * 		If this is NULL, the clone content is extracted from a currently open editor or from the file itself.
	 * @return the content for the given clone or null on error
	 */
	public static String getTextForClone(ICloneFile cloneFile, IClone clone, String editorContent)
	{
		assert (cloneFile != null && clone != null);

		if (log.isTraceEnabled())
			log.trace("getTextForClone() - cloneFile: " + cloneFile + ", clone: " + clone + ", editorContent: "
					+ editorContent);

		String result = null;

		/*
		 * If we've been provided with editorContent, use it. 
		 */

		if (editorContent != null)
		{
			log.trace("getTextForClone() - using provided editorContent");
			result = editorContent.substring(clone.getOffset(), clone.getOffset() + clone.getLength());
		}
		else
		{
			//ok, we don't have any editor content, lets try to obtain it

			//first get a handle on the file
			IFile fileHandle = CoreFileUtils.getFileForCloneFile(cloneFile);
			if (fileHandle == null)
			{
				log.warn("getTextForClone() - unable to find clone file: " + cloneFile + ", clone: " + clone);
				return null;
			}

			//now check all open editors to see whether we already have an open copy of that file
			ITextEditor textEditor = CoreUtils.getTextEditorForFile(fileHandle);
			if (textEditor != null)
			{
				if (log.isTraceEnabled())
					log.trace("getTextForClone() - found corresponding editor: " + textEditor);

				IDocumentProvider provider = textEditor.getDocumentProvider();
				IDocument document = provider.getDocument(textEditor.getEditorInput());
				try
				{
					result = document.get(clone.getOffset(), clone.getLength());

					if (log.isTraceEnabled())
						log.trace("getTextForClone() - content extracted from editor: " + result);
				}
				catch (BadLocationException e)
				{
					log.warn("getTextForClone() - editor size: " + document.getLength() + ", content: "
							+ document.get());
					log.error("getTextForClone() - unable to extract clone content from editor - editor: " + textEditor
							+ ", cloneFile: " + cloneFile + ", clone: " + clone, e);
				}
			}

			//check if we got the clone content from one of the open editors
			if (result == null)
			{
				//nope, no clone content yet, we'll have to fall back to the IFile resource
				if (log.isTraceEnabled())
					log
							.trace("getTextForClone() - no suitable open editor to extract clone content, falling back to file resource: "
									+ fileHandle);

				try
				{
					int len = clone.getLength();
					byte[] buf = new byte[len];

					if (log.isTraceEnabled())
						log.trace("getTextForClone() - about to read " + len + " bytes (offset: " + clone.getOffset()
								+ ") from file: " + fileHandle);

					BufferedInputStream bis = new BufferedInputStream(fileHandle.getContents());
					bis.skip(clone.getOffset());
					bis.read(buf, 0, len);

					result = new String(buf);
				}
				catch (Exception e)
				{
					log.error("getTextForClone() - unable to extract clone content from file - " + e + " - file: "
							+ fileHandle + ", cloneFile: " + cloneFile + ", clone: " + clone, e);
				}
			}
		}

		if (result == null)
			log.warn("getTextForClone() - no content could be extracted for clone: " + clone);

		if (log.isTraceEnabled())
			log.trace("getTextForClone() - result: " + result);

		return result;
	}

	/**
	 * Evaluation method. Creates an Eclipse Marker for the given clone.
	 * For now a normal code warning marker is used.
	 * 
	 * This is not meant for use in any final version!
	 */
	/*
	@SuppressWarnings("unchecked")
	protected void createCloneMarker(ICloneFile cloneFile, IClone clone)
	{
		//get a file handle
		IFile fileHandle = CloneParseUtils.getFileForCloneFile(cloneFile);

		Map map = new HashMap();

		MarkerUtilities.setCharStart(map, clone.getPosition().getStartOffset());
		MarkerUtilities.setCharEnd(map, clone.getPosition().getEndOffset() + 1);
		MarkerUtilities.setMessage(map, "Clone: " + clone.getPosition().getStartOffset() + ":"
				+ clone.getPosition().getLength() + " - " + clone.getUuid());

		//handle as warning
		//map.put(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);

		//store uuid
		map.put("clone_uuid", clone.getUuid());

		try
		{
			MarkerUtilities.createMarker(fileHandle, map, IMarker.BOOKMARK);
		}
		catch (CoreException e)
		{
			log
					.error("createCloneMarker() - marker creation failed - cloneFile: " + cloneFile + ", clone: "
							+ clone, e);
		}
	}

	protected void removeCloneMarker(ICloneFile cloneFile, IClone clone)
	{
		//get a file handle
		IFile fileHandle = CloneParseUtils.getFileForCloneFile(cloneFile);

		try
		{
			IMarker[] markers = fileHandle.findMarkers(IMarker.BOOKMARK, false, IResource.DEPTH_ZERO);
			for (IMarker marker : markers)
			{
				if (!marker.exists())
					continue;

				Object cloneUuid = marker.getAttribute("clone_uuid");
				if (cloneUuid != null)
				{
					if (cloneUuid.equals(clone.getUuid()))
					{
						//ok, found it. remove this marker
						marker.delete();

						//we're done
						return;
					}
				}
			}
		}
		catch (CoreException e)
		{
			log.error("createCloneMarker() - marker removal failed - cloneFile: " + cloneFile + ", clone: " + clone, e);
		}
	}*/

}

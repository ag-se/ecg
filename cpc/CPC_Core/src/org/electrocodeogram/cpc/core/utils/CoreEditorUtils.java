package org.electrocodeogram.cpc.core.utils;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ISynchronizable;
import org.eclipse.ui.texteditor.ITextEditor;


public class CoreEditorUtils
{
	private static Log log = LogFactory.getLog(CoreEditorUtils.class);

	/**
	 * Retrieves the internal lock object for the given {@link IDocument} if it is
	 * an {@link ISynchronizable} instance.<br/>
	 * Otherwise a new lock object is returned on each call
	 * (thus, in effect all locking on such an object is "disabled").
	 * 
	 * @param document the document to get a lock object for, may be NULL.
	 * @return a lock object, never null.
	 */
	public static Object getDocumentLockObject(IDocument document)
	{
		Object lock = null;

		/*
		 * TODO: reconsider whether we should really be accessing an internal class here.
		 */
		if (document != null)
			if (document instanceof ISynchronizable)
				lock = ((ISynchronizable) document).getLockObject();

		if (lock == null)
			lock = new Byte((byte) -1);

		if (log.isTraceEnabled())
			log.trace("getDocumentLockObject() - document: " + document + ", lock: " + lock);

		return lock;
	}

	/**
	 * Checks whether the given document has a valid lock object.
	 */
	public static boolean hasDocumentLockObject(IDocument document)
	{
		if (document != null)
			if (document instanceof ISynchronizable)
				if (((ISynchronizable) document).getLockObject() != null)
					return true;

		return false;
	}

	/**
	 * Checks whether a given file is currently open in an editor and contains
	 * unsaved changes. 
	 * 
	 * @param fileHandle the file to check, may be NULL.
	 * @return <em>true</em> if the file exists, is currently open in an editor and is dirty, <em>false</em> otherwise.
	 */
	public static boolean isFileOpenInEditorAndDirty(IFile fileHandle)
	{
		if (log.isTraceEnabled())
			log.trace("isFileOpenInEditorAndDirty() - fileHandle: " + fileHandle);

		if (fileHandle == null)
			return false;

		if (!fileHandle.exists())
			return false;

		ITextEditor editor = CoreUtils.getTextEditorForFile(fileHandle);
		if (editor == null)
			return false;

		return editor.isDirty();
	}
}

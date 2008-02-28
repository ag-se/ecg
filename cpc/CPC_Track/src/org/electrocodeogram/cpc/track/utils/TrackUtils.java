package org.electrocodeogram.cpc.track.utils;


import org.eclipse.jface.text.IDocument;
import org.electrocodeogram.cpc.core.utils.CoreEditorUtils;
import org.electrocodeogram.cpc.track.repository.DocumentDescriptor;


public class TrackUtils
{
	/**
	 * Convenience method.
	 * 
	 * @see CoreEditorUtils#getDocumentLockObject(IDocument)
	 */
	public static Object getDocumentLockObject(DocumentDescriptor docDesc)
	{
		assert (docDesc != null && docDesc.getDocument() != null);

		return CoreEditorUtils.getDocumentLockObject(docDesc.getDocument());
	}

}

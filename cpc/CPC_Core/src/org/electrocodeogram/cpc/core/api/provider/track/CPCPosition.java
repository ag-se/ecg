package org.electrocodeogram.cpc.core.api.provider.track;


import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.electrocodeogram.cpc.core.api.data.IClone;


/**
 * Represents the position of an {@link IClone} object during modifications of an {@link IDocument}.
 * <br>
 * Keeps reference to the corresponding {@link IClone} object.
 * <p>
 * <b>NOTE:</b> updating a {@link CPCPosition} does not directly affect the corresponding {@link IClone} instance.
 * 		Modifications may be cached and written back to the clone objects at a later point in time.
 * <p>
 * NOTE: {@link CPCPosition}s are not encapsulated behind a separate interface for performance reasons.<br/>
 * 		The {@link IPositionUpdateStrategyProvider} may directly access and modify field values to improve performance.
 * 
 * @author vw
 * 
 * @see IPositionUpdateStrategyProvider
 */
public class CPCPosition extends Position
{
	/**
	 * The unique position category name for all CPCPosition entries.
	 */
	public static final String CPC_POSITION_CATEGORY = "org.electrocodeogram.cpc";

	/*
	 * offset and length are defined by Position
	 */

	/**
	 * @see #getClone()
	 */
	public IClone clone;

	/**
	 * @see #getContent()
	 */
	public String content;

	/**
	 * @see #isContentModified()
	 */
	public boolean contentModified;

	/*
	 * If we're required to log all modifications to clones, we'll cache all
	 * incoming DocumentEvents which modified the clone which corresponds to this
	 * position in the following list.
	 * By default the list is NULL.
	 */
	/**
	 * @see #getContentModifyingDocumentEvents()
	 */
	public List<CPCDocumentEvent> contentModifyingDocumentEvents = null;

	/**
	 * Creates a new {@link CPCPosition} with the position of the given clone object.
	 * 
	 * @param clone the clone object which this position should represent, never null.
	 */
	public CPCPosition(IClone clone)
	{
		assert (clone != null);

		this.clone = clone;
		setOffset(clone.getOffset());
		setLength(clone.getLength());
		this.content = clone.getContent();
		this.contentModified = false;
	}

	/**
	 * Retrieves the clone corresponding to this position.
	 * 
	 * @return the clone corresponding to this position, never null.
	 */
	public IClone getClone()
	{
		return clone;
	}

	/**
	 * Checks whether the clone content of this position was modified.
	 * 
	 * @return whether the clone content of this position was modified.
	 */
	public boolean isContentModified()
	{
		return contentModified;
	}

	/**
	 * Specifies whether the clone content of this position was modified.
	 * 
	 * @see #isContentModified()
	 */
	public void setContentModified(boolean contentModified)
	{
		this.contentModified = contentModified;
	}

	//TODO: Once the debug stage is over, we can probably replace the content string with
	//		a simple boolean flag which indicates whether the content was modified.
	//		The content could then be extracted once the clone data is about to be
	//		transmitted to the IStoreProvider.
	/**
	 * Retrieves the current content of the corresponding clone entry.
	 * 
	 * @return the current content of the corresponding clone entry.
	 */
	public String getContent()
	{
		return content;
	}

	//TODO: remove this (?)
	/**
	 * Sets current content of the corresponding clone entry.
	 * 
	 * @see #getContent()
	 */
	public void setContent(String content)
	{
		this.content = content;
	}

	/**
	 * Retrieves a list of events which affected this position.
	 * 
	 * @return may be NULL
	 */
	public List<CPCDocumentEvent> getContentModifyingDocumentEvents()
	{
		return contentModifyingDocumentEvents;
	}

	/**
	 * Adds the given {@link CPCDocumentEvent} to this {@link CPCPosition}.
	 * 
	 * @param cpcEvent the {@link CPCDocumentEvent} to add, never null.
	 */
	public void addContentModifyingDocumentEvent(CPCDocumentEvent cpcEvent)
	{
		assert (cpcEvent != null);

		if (contentModifyingDocumentEvents == null)
			contentModifyingDocumentEvents = new LinkedList<CPCDocumentEvent>();

		contentModifyingDocumentEvents.add(cpcEvent);
	}

	/**
	 * Retrieves the end offset of this position.
	 * <br>
	 * Convenience method.
	 * 
	 * @return value of: {@link CPCPosition#getOffset()} + {@link CPCPosition#getLength()} - 1
	 */
	public int getEndOffset()
	{
		return offset + length - 1;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "POS[" + (isDeleted ? "DEL-" : "") + "o: " + getOffset() + ", l: " + getLength() + ", c: "
				+ clone.getUuid() + ", content: " + content + ", events: " + contentModifyingDocumentEvents + "]";
	}

}

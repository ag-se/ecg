package org.electrocodeogram.cpc.core.api.provider.track;


import org.eclipse.jface.text.DocumentEvent;
import org.electrocodeogram.cpc.core.utils.CoreStringUtils;


/**
 * A {@link CPCDocumentEvent} is similar to a {@link DocumentEvent}. However, it describes only
 * those parts of a {@link DocumentEvent} which were located inside the corresponding clone.
 * <p>
 * I.e. depending on the {@link IPositionUpdateStrategyProvider}, a modification which only partly
 * overlaps with a clone might not become part of the clone. The corresponding {@link CPCDocumentEvent}
 * would therefore differ from the {@link DocumentEvent}. It would only specify the removal of
 * characters from the clone, not the addition of new content (which didn't become part of the clone).
 * <p>
 * It is the responsibility of the {@link IPositionUpdateStrategyProvider} to create {@link CPCDocumentEvent}s
 * which match its internal position updating strategies.
 * 
 * @author vw
 * 
 * @see IPositionUpdateStrategyProvider
 */
public class CPCDocumentEvent
{
	/*
	 * These fields are public for performance reasons.
	 */

	/**
	 * @see #getOffset()
	 */
	public int fOffset;

	/**
	 * @see #getLength()
	 */
	public int fLength;

	/**
	 * @see #getText()
	 */
	public String fText;

	/**
	 * Creates a new {@link CPCDocumentEvent}.
	 * 
	 * @param offset relative position to beginning of clone, always &gt;= 0.
	 * @param length number of removed characters which were part of the clone, always &gt;= 0.
	 * @param text newly inserted text which became part of the clone, may be NULL.
	 */
	public CPCDocumentEvent(int offset, int length, String text)
	{
		this.fOffset = offset;
		this.fLength = length;
		this.fText = text;
	}

	/**
	 * Retrieves the relative position to beginning of clone.
	 * 
	 * @return relative position to beginning of clone, always &gt;= 0.
	 */
	public int getOffset()
	{
		return fOffset;
	}

	/**
	 * Retrieves the number of removed characters which were part of the clone.
	 * 
	 * @return number of removed characters which were part of the clone, always &gt;= 0.
	 */
	public int getLength()
	{
		return fLength;
	}

	/**
	 * Retrieves the newly inserted text which became part of the clone.
	 * 
	 * @return newly inserted text which became part of the clone, may be NULL.
	 */
	public String getText()
	{
		return fText;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "CPCDE[o: " + fOffset + ", l: " + fLength + ", t: " + CoreStringUtils.quoteString(fText) + "]";
	}
}

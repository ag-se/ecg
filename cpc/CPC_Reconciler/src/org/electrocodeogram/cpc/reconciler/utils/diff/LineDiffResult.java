package org.electrocodeogram.cpc.reconciler.utils.diff;


import org.incava.util.diff.Difference;


/**
 * Wrapper for a {@link Difference} object.<br/>
 * TODO: Do we really need this?
 * 
 * @author vw
 */
public class LineDiffResult
{
	private int delOffset;
	private int delLength;
	private int addOffset;
	private int addLength;

	/**
	 * Convert a java-diff {@link Difference} object into a {@link LineDiffResult}.
	 * 
	 * @param difference {@link Difference} object to convert, never null.
	 */
	public LineDiffResult(Difference difference)
	{
		assert (difference != null);

		this.delOffset = difference.getDeletedStart();
		if (difference.getDeletedEnd() == Difference.NONE)
			this.delLength = 0;
		else
			this.delLength = (difference.getDeletedEnd() - this.delOffset) + 1;

		this.addOffset = difference.getAddedStart();
		if (difference.getAddedEnd() == Difference.NONE)
			this.addLength = 0;
		else
			this.addLength = (difference.getAddedEnd() - this.addOffset) + 1;
	}

	/**
	 * The point at which the deletion starts.
	 */
	public int getDelOffset()
	{
		return delOffset;
	}

	/**
	 * The number of elements that were deleted, if any.<br/>
	 * A value equal to 0 means nothing was deleted.
	 */
	public int getDelLength()
	{
		return delLength;
	}

	/**
	 * The point at which the addition starts.
	 */
	public int getAddOffset()
	{
		return addOffset;
	}

	/**
	 * The number of elements that were added, if any.<br/>
	 * A value equal to 0 means nothing was added.
	 */
	public int getAddLength()
	{
		return addLength;
	}

	/**
	 * Compares this object to the other for equality. Both objects must be of
	 * type Difference, with the same starting and ending points.
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof LineDiffResult)
		{
			LineDiffResult other = (LineDiffResult) obj;

			return (delOffset == other.delOffset && delLength == other.delLength && addOffset == other.addOffset && addLength == other.addLength);
		}
		else
		{
			return false;
		}
	}

	/**
	 * Returns a string representation of this difference.
	 */
	@Override
	public String toString()
	{
		return "LineDiffResult[delOffset: " + delOffset + ", delLength: " + delLength + ", addOffset: " + addOffset
				+ ", addLength: " + addLength + "]";
	}

}

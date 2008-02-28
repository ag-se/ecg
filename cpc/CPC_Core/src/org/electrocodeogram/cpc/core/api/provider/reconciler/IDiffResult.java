package org.electrocodeogram.cpc.core.api.provider.reconciler;


/**
 * Result wrapper object for the {@link IDiffProvider}.
 * 
 * @author vw
 *
 * @see IDiffProvider
 */
public interface IDiffResult
{
	/**
	 * The type of this {@link IDiffResult}.
	 */
	public enum Type
	{
		/**
		 * Describes a deletion.
		 */
		DELETE,

		/**
		 * Describes an insertion.
		 */
		INSERT
	}

	/**
	 * Retrieves the type of this diff.
	 * 
	 * @return The type of this diff.
	 */
	public Type getType();

	/**
	 * Retrieves the 0-based character offset in the source text where this insertion/deletion starts.
	 * 
	 * @return offset in the source text where this insertion/deletion starts.
	 */
	public int getOffset();

	/**
	 * Retrieves the length of the added or removed text.
	 * <br>
	 * Convenience method.
	 * 
	 * @return cached value of length of {@link #getText()}
	 */
	public int getLength();

	/**
	 * Retrieves the text which was inserted or deleted.
	 * 
	 * @return The text which was inserted or deleted.
	 */
	public String getText();

	/**
	 * Checks whether this is an insertion.
	 * <br>
	 * Convenience method.
	 * 
	 * @return true if this diff was an INSERT.
	 */
	public boolean isInsert();

	/**
	 * Checks whether this is a deletion.
	 * <br>
	 * Convenience method.
	 * 
	 * @return true if this diff was a DELETE.
	 */
	public boolean isDelete();

}

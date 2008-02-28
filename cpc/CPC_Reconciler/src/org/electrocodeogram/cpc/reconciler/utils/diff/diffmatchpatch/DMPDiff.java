package org.electrocodeogram.cpc.reconciler.utils.diff.diffmatchpatch;


/**
 * Class representing one diff operation.
 */
public class DMPDiff
{
	public DiffMatchPatch.Operation operation;
	// One of: INSERT, DELETE or EQUAL.
	public String text;
	// The text associated with this diff operation.
	public int index;

	// Where in the source text does this diff fit?  Not usualy set.

	/**
	 * Constructor.  Initializes the diff with the provided values.
	 * @param operation One of INSERT, DELETE or EQUAL
	 * @param text The text being applied
	 */
	public DMPDiff(DiffMatchPatch.Operation operation, String text)
	{
		// Construct a diff with the specified operation and text.
		this.operation = operation;
		this.text = text;
		this.index = -1;
	}

	/**
	 * Display a human-readable version of this Diff.
	 * @return text version
	 */
	@Override
	public String toString()
	{
		String prettyText = this.text.replace('\n', '\u00b6');
		return "Diff(" + this.operation + ",\"" + prettyText + "\")";
	}

	/**
	 * Is this Diff equivalent to another Diff?
	 * @param d Another Diff to compare against
	 * @return true or false
	 */
	@Override
	public boolean equals(Object d)
	{
		try
		{
			return (((DMPDiff) d).operation == this.operation) && (((DMPDiff) d).text.equals(this.text));
		}
		catch (ClassCastException e)
		{
			return false;
		}
	}

	/**
	 *
	 * @return number represending this object's values
	 */
	@Override
	public int hashCode()
	{
		return this.operation.hashCode() + this.text.hashCode();
	}
}

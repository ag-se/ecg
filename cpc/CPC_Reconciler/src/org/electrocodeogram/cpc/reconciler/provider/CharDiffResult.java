package org.electrocodeogram.cpc.reconciler.provider;


import org.electrocodeogram.cpc.core.api.provider.reconciler.IDiffProvider;
import org.electrocodeogram.cpc.core.api.provider.reconciler.IDiffResult;
import org.electrocodeogram.cpc.reconciler.utils.diff.diffmatchpatch.DMPDiff;
import org.electrocodeogram.cpc.reconciler.utils.diff.diffmatchpatch.DiffMatchPatch;


/**
 * Default implementation of {@link IDiffResult}.
 * 
 * @author vw
 * 
 * @see IDiffProvider
 */
public class CharDiffResult implements IDiffResult
{
	protected int offset;
	protected int length;
	protected Type type;
	protected String text;

	public CharDiffResult(int offset, DMPDiff diff)
	{
		this.offset = offset;

		if (DiffMatchPatch.Operation.DELETE.equals(diff.operation))
			this.type = Type.DELETE;
		else if (DiffMatchPatch.Operation.INSERT.equals(diff.operation))
			this.type = Type.INSERT;
		else
			throw new IllegalArgumentException("unsupported diff operation type: " + diff.operation);

		this.text = diff.text;

		this.length = this.text.length();
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.reconciler.IDiffResult#getOffset()
	 */
	public int getOffset()
	{
		return offset;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.reconciler.IDiffResult#getLength()
	 */
	public int getLength()
	{
		return length;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.reconciler.IDiffResult#getType()
	 */
	public Type getType()
	{
		return type;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.reconciler.IDiffResult#getText()
	 */
	public String getText()
	{
		return text;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.reconciler.IDiffResult#isInsert()
	 */
	public boolean isInsert()
	{
		return Type.INSERT.equals(this.type);
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.reconciler.IDiffResult#isDelete()
	 */
	public boolean isDelete()
	{
		return Type.DELETE.equals(this.type);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		String prettyText = this.text.replace('\n', '\u00b6');
		return "CharDiffResult[" + this.offset + ", " + this.type + ", \"" + prettyText + "\"]";
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;

		if (obj == null)
			return false;

		if (getClass() != obj.getClass())
			return false;

		final CharDiffResult other = (CharDiffResult) obj;

		if ((!type.equals(other.type)) || (!text.equals(other.text)))
			return false;

		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((text == null) ? 0 : text.hashCode());
		return result;
	}

}

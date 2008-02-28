package org.electrocodeogram.cpc.merge.provider;


import java.util.LinkedList;
import java.util.List;

import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.provider.merge.IMergeResultPerspective;
import org.electrocodeogram.cpc.merge.api.strategy.IWriteableMergeResult.Type;


/**
 * Default implementation of {@link IMergeResultPerspective}.
 * 
 * @author vw
 * 
 * @see MergeResult
 */
public class MergeResultPerspective implements IMergeResultPerspective
{
	private String name;
	private List<IClone> addedClones;
	private List<IClone> removedClones;
	private List<IClone> lostClones;
	private List<IClone> movedClones;
	private List<IClone> modifiedClones;
	private List<IClone> unchangedClones;

	/**
	 * Creates a new {@link MergeResultPerspective} with all lists initialised
	 * as empty lists.
	 */
	public MergeResultPerspective(String name)
	{
		this.name = name;

		addedClones = new LinkedList<IClone>();
		removedClones = new LinkedList<IClone>();
		lostClones = new LinkedList<IClone>();
		movedClones = new LinkedList<IClone>();
		modifiedClones = new LinkedList<IClone>();
		unchangedClones = new LinkedList<IClone>();
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.merge.IMergeResultPerspective#getName()
	 */
	@Override
	public String getName()
	{
		return name;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.merge.IMergeResultPerspective#getAddedClones()
	 */
	@Override
	public List<IClone> getAddedClones()
	{
		return addedClones;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.merge.IMergeResultPerspective#getLostClones()
	 */
	@Override
	public List<IClone> getLostClones()
	{
		return lostClones;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.merge.IMergeResultPerspective#getModifiedClones()
	 */
	@Override
	public List<IClone> getModifiedClones()
	{
		return modifiedClones;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.merge.IMergeResultPerspective#getMovedClones()
	 */
	@Override
	public List<IClone> getMovedClones()
	{
		return movedClones;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.merge.IMergeResultPerspective#getRemovedClones()
	 */
	@Override
	public List<IClone> getRemovedClones()
	{
		return removedClones;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.merge.IMergeResultPerspective#getUnchangedClones()
	 */
	@Override
	public List<IClone> getUnchangedClones()
	{
		return unchangedClones;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "MergeResultPerspective[" + name + " - added: " + addedClones + ", moved: " + movedClones
				+ ", modified: " + modifiedClones + ", removed: " + removedClones + ", unchanged: " + unchangedClones
				+ ", lost: " + lostClones + "]";
	}

	/**
	 * Adds the given {@link IClone} to the list for the given type.
	 * 
	 * @throws IllegalArgumentException on unknown type
	 */
	protected void addClone(IClone clone, Type type)
	{
		if (Type.ADDED.equals(type))
			addedClones.add(clone);
		else if (Type.LOST.equals(type))
			lostClones.add(clone);
		else if (Type.MODFIED.equals(type))
			modifiedClones.add(clone);
		else if (Type.MOVED.equals(type))
			movedClones.add(clone);
		else if (Type.MOVED_MODIFIED.equals(type))
		{
			movedClones.add(clone);
			modifiedClones.add(clone);
		}
		else if (Type.REMOVED.equals(type))
			removedClones.add(clone);
		else if (Type.UNCHANGED.equals(type))
			unchangedClones.add(clone);
		else
			throw new IllegalArgumentException("unknown list type: " + type);
	}

}

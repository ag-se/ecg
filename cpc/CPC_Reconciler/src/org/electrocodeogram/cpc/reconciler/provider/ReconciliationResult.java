package org.electrocodeogram.cpc.reconciler.provider;


import java.util.LinkedList;
import java.util.List;

import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.provider.reconciler.IReconcilerProvider;
import org.electrocodeogram.cpc.core.api.provider.reconciler.IReconciliationResult;


/**
 * Default implementation of {@link IReconciliationResult}.<br/>
 * Not to be instantiated anywhere outside the {@link IReconcilerProvider}.
 * 
 * @author vw
 * 
 * @see IReconciliationResult
 * @see IReconcilerProvider
 */
public class ReconciliationResult implements IReconciliationResult
{
	protected Status status = null;
	protected List<IClone> modifiedClones = null;
	protected List<IClone> movedClones = null;
	protected List<IClone> removedClones = null;
	protected List<IClone> lostClones = null;

	ReconciliationResult()
	{
		modifiedClones = new LinkedList<IClone>();
		movedClones = new LinkedList<IClone>();
		removedClones = new LinkedList<IClone>();
		lostClones = new LinkedList<IClone>();
	}

	/* (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.reconciler.IReconciliationResult#getStatus()
	 */
	public Status getStatus()
	{
		return status;
	}

	void setStatus(Status status)
	{
		assert (status != null);

		this.status = status;
	}

	/* (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.reconciler.IReconciliationResult#isFullyReconciled()
	 */
	public boolean isFullyReconciled()
	{
		return Status.FULL_RECONCILIATION.equals(this.status);
	}

	/* (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.reconciler.IReconciliationResult#getModifiedClones()
	 */
	public List<IClone> getModifiedClones()
	{
		return modifiedClones;
	}

	/* (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.reconciler.IReconciliationResult#getMovedClones()
	 */
	public List<IClone> getMovedClones()
	{
		return movedClones;
	}

	/* (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.reconciler.IReconciliationResult#getRemovedClones()
	 */
	public List<IClone> getRemovedClones()
	{
		return removedClones;
	}

	/* (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.reconciler.IReconciliationResult#getLostClones()
	 */
	@Override
	public List<IClone> getLostClones()
	{
		assert (!isFullyReconciled() || (lostClones == null || lostClones.isEmpty()));

		return lostClones;
	}

	/* (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.reconciler.IReconciliationResult#toString()
	 */
	@Override
	public String toString()
	{
		return "ReconciliationResult[status: " + status + ", removedClones: " + removedClones + ", modifiedClones: "
				+ modifiedClones + ", movedClones: " + movedClones + ", lostClones: " + lostClones + "]";
	}
}

package org.electrocodeogram.cpc.core.api;


import org.electrocodeogram.cpc.core.api.data.CloneDiff;
import org.electrocodeogram.cpc.core.api.data.IClone;


/**
 * Global constants which are potentially of interest for all CPC Modules.
 * <p>
 * This class is not meant to be instantiated.
 * 
 * @author vw
 */
public final class CPCConstants
{
	/**
	 * The {@link IClone#getCreator()} value for clones which were generated
	 * by an automated import, i.e.&nbsp;by the <em>CPC Imports</em> module.
	 */
	public static final String CLONE_CREATOR_AUTOMATED_IMPORT = "CPC_IMPORT";

	/**
	 * The {@link IClone#getCreator()} value for clones which were generated
	 * by a reconciliation operation, i.e.&nbsp;by the <em>CPC Reconciler</em> module.
	 */
	public static final String CLONE_CREATOR_AUTOMATED_RECONCILIATION = "CPC_RECONCILER";

	/**
	 * The {@link CloneDiff#getCreator()} value for clone history diffs which were generated
	 * by an optimisation operation, i.e.&nbsp;by the <em>CPC Optimiser</em> module.
	 */
	public static final String CLONEDIFF_CREATOR_CPC_OPTIMISER = "CPC_OPTIMISER";

	/*
	 * No methods.
	 */

	private CPCConstants()
	{
		//This class is not meant to be instantiated.
	}
}

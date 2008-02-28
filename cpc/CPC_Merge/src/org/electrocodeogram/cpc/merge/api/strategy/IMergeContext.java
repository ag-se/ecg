package org.electrocodeogram.cpc.merge.api.strategy;


import java.util.LinkedList;

import org.electrocodeogram.cpc.core.api.data.IClone;


/**
 * A collection of progress/status information for {@link IMergeStrategy}s as well as some
 * utility functions.
 * 
 * @author vw
 * 
 * @see IMergeStrategy
 */
public interface IMergeContext
{
	/**
	 * Retrieves a list of still unhandled local clones in their pre-merge state.
	 * <br>
	 * This list may be modified by an {@link IMergeStrategy}.
	 * 
	 * @return a list of still unhandled local clones in their pre-merge state, may be empty, never null.
	 */
	public LinkedList<IClone> getPendingLocalClones();

	/**
	 * Retrieves a list of still unhandled remote clones in their pre-merge state.
	 * <br>
	 * This list may be modified by an {@link IMergeStrategy}.
	 * 
	 * @return a list of still unhandled remote clones in their pre-merge state, may be empty, never null.
	 */
	public LinkedList<IClone> getPendingRemoteClones();

	/**
	 * Retrieves a list of still unhandled base clones.
	 * <br>
	 * This list may be modified by an {@link IMergeStrategy}.
	 * 
	 * @return a list of still unhandled base clones, may be empty, never null.
	 */
	public LinkedList<IClone> getPendingBaseClones();

	/**
	 * Checks if there are still some unhandled local or remote clones left in this context.
	 * 
	 * @return <em>true</em> if {@link IMergeContext#getPendingLocalClones()} or {@link IMergeContext#getPendingRemoteClones()}
	 * 		is not empty, <em>false</em> otherwise.
	 */
	public boolean isLocalOrRemoteClonePending();

	/**
	 * Retrieves an {@link ICloneObjectExtensionMerger} instance which can be used to merge the
	 * extension data of a given clone par.
	 * 
	 * @return a {@link ICloneObjectExtensionMerger}, never null.
	 */
	public ICloneObjectExtensionMerger getCloneObjectExtensionMerger();
}

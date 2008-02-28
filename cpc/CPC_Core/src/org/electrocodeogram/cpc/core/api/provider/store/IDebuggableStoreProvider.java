package org.electrocodeogram.cpc.core.api.provider.store;


/**
 * An extension of the {@link IStoreProvider} interface which adds a couple of integrity checking
 * and status methods which are meant to ease debugging efforts.
 * <p>
 * Implementation of this interface is optional.
 * 
 * @author vw
 * 
 * @see IStoreProvider
 */
public interface IDebuggableStoreProvider extends IStoreProvider
{
	/**
	 * Returns a string which contains a number of statistics for the internal caching structures.
	 * <br>
	 * The data contained and the formatting is up to the store provider implementation.
	 * <p>
	 * DEBUG METHOD
	 * 
	 * @return caching statistics, never null.
	 */
	public String getCacheStats();

	/**
	 * Executes an integrity check of the internal cache structures of the store provider implementation.
	 * <br>
	 * This method does not throw an exception under any circumstance. Detailed information about any
	 * violated constraints is logged as level <em>ERROR</em> or <em>FATAL</em>.
	 * <p>
	 * NOTE: this operation may be slow. 
	 * <p>
	 * DEBUG METHOD
	 * 
	 * @return <em>false</em> if integrity constrains have been violated, <em>true</em> otherwise.
	 */
	public boolean checkCacheIntegrity();

	/**
	 * Executes an integrity check of all internal data structures of the store provider implementation.
	 * <br>
	 * This method does not throw an exception under any circumstance. Detailed information about any
	 * violated constraints is logged as level <em>ERROR</em> or <em>FATAL</em>.
	 * <p>
	 * This method also executes all <em>checkCacheIntegrity()</em> checks.
	 * <p>
	 * NOTE: this is a potentially VERY SLOW operation.
	 * <p>
	 * An exclusive write lock is required before this method may be called.
	 * <p>
	 * DEBUG METHOD
	 * 
	 * @return <em>false</em> if integrity constrains have been violated, <em>true</em> otherwise.
	 * @throws StoreLockingException thrown if the current thread does not hold an exclusive write lock.
	 */
	public boolean checkDataIntegrity() throws StoreLockingException;

}

package org.electrocodeogram.cpc.core.api.data.special;


import java.util.List;


/**
 * This interface is for internal use only.
 * <br>
 * It is not intended to be implemented by any client of the CPC framework.
 * 
 * @author vw
 */
public interface IMultiKeyStatefulObject extends IStatefulObject
{
	/**
	 * Retrieves a list of keys which together form a unique identifier for the object.
	 * 
	 * @return list of keys which form a unique identifier, never null.
	 * 
	 * @see IStatefulObject#getPersistenceObjectIdentifier()
	 */
	public List<String> getPersistenceObjectIdentifiers();
}

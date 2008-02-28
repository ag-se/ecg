package org.electrocodeogram.cpc.store.local.sql.utils;


import java.util.List;
import java.util.Map;

import org.electrocodeogram.cpc.core.api.data.special.IMultiKeyStatefulObject;
import org.electrocodeogram.cpc.core.api.data.special.IStatefulObject;


/**
 * An adapter which pretends to be an {@link IStatefulObject} and translates all {@link IStatefulObject}
 * method calls to the immutable values given during construction time. 
 * 
 * @author vw
 */
public class StatefulObjectAdapter implements IMultiKeyStatefulObject
{
	private String persistenceClassIdentifier;
	private List<String> persistenceObjectIdentifiers;
	private Map<String, Comparable<? extends Object>> state;
	private Map<String, Class<? extends Object>> stateTypes;

	public StatefulObjectAdapter(String persistenceClassIdentifier, List<String> persistenceObjectIdentifiers,
			Map<String, Comparable<?>> state)
	{
		this(persistenceClassIdentifier, persistenceObjectIdentifiers, state, null);
	}

	public StatefulObjectAdapter(String persistenceClassIdentifier, List<String> persistenceObjectIdentifiers,
			Map<String, Comparable<?>> state, Map<String, Class<? extends Object>> stateTypes)
	{
		this.persistenceClassIdentifier = persistenceClassIdentifier;
		this.persistenceObjectIdentifiers = persistenceObjectIdentifiers;
		this.state = state;
		this.stateTypes = stateTypes;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.internal.IStatefulObject#getPersistenceClassIdentifier()
	 */
	@Override
	public String getPersistenceClassIdentifier()
	{
		return persistenceClassIdentifier;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.internal.IStatefulObject#getPersistenceObjectIdentifier()
	 */
	@Override
	public String getPersistenceObjectIdentifier()
	{
		if (persistenceObjectIdentifiers.size() > 1)
			throw new IllegalStateException(
					"This StatefulObjectAdapter was initialised with multiple persistent object identifiers, access is only allowed via the IMultiKeyStatefulObject API.");

		return persistenceObjectIdentifiers.get(0);
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.internal.IMultiKeyStatefulObject#getPersistenceObjectIdentifiers()
	 */
	@Override
	public List<String> getPersistenceObjectIdentifiers()
	{
		return persistenceObjectIdentifiers;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.internal.IStatefulObject#getState()
	 */
	@Override
	public Map<String, Comparable<? extends Object>> getState()
	{
		return state;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.internal.IStatefulObject#getStateTypes()
	 */
	@Override
	public Map<String, Class<? extends Object>> getStateTypes()
	{
		if (stateTypes == null)
			throw new IllegalStateException("This StatefulObjectAdapter was not initialised with State Types data.");

		return stateTypes;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.internal.IStatefulObject#setState(java.util.Map)
	 */
	@Override
	public void setState(Map<String, Comparable<? extends Object>> state)
	{
		throw new IllegalStateException("Modifications of a StatefulObjectAdapter are not allowed.");
	}

}

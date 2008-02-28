package org.electrocodeogram.cpc.core.api.data.special;


import java.util.Map;

import org.electrocodeogram.cpc.core.api.data.ICloneObject;
import org.electrocodeogram.cpc.core.api.data.ICloneObjectExtension;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider;


/**
 * Special sub-interface for all objects which are to be persisted by an {@link IStoreProvider}.
 * The methods are used to extract and restore the internal state of an object.
 * 
 * @author vw
 * 
 * @see IStoreProvider
 * @see ICloneObjectExtensionStatefulObject
 */
public interface IStatefulObject
{
	/**
	 * Returns a string which uniquely identifies the object type.
	 * <p>
	 * For the default {@link ICloneObject} sub interfaces the return values <b>must equal</b>
	 * the <em>PERSISTENCE_CLASS_IDENTIFIER</em> constant defined in the interface.
	 * <p>
	 * 3rd party clone objects can define their own persistence identifiers.
	 * <br>
	 * Allowed are only letters, numbers and the underscore. Furthermore
	 * an identifier needs to contain at least one letter, may not begin with an underscore
	 * and may not contain multiple consecutive underscores.
	 * <p>
	 * A typical store provider will use this method to derive directory/file or table names.
	 * 
	 * @return unique identifier for the object type, never null.
	 */
	public String getPersistenceClassIdentifier();

	/**
	 * Returns a key which corresponds to an entry in the state Map returned by <em>getState()</em>
	 * which uniquely identifies an object instance.
	 * <p>
	 * For the default {@link ICloneObject} sub interfaces the return values <b>must equal</b>
	 * the <em>PERSISTENCE_OBJECT_IDENTIFIER</em> constant defined in the {@link ICloneObject} interface.
	 * <p>
	 * By default this is <em>uuid</em>.
	 * <p>
	 * 3rd party clone objects can define their own persistence identifiers.
	 * <br>
	 * Allowed are only letters, numbers and the underscore. Furthermore
	 * an identifier needs to contain at least one letter, may not begin with an underscore
	 * and may not contain multiple consecutive underscores.
	 * <p>
	 * A typical store provider will use this method to derive file names, internal id structure names or table primary keys.
	 * <p>
	 * <b>IMPORTANT:</b> the corresponding value in {@link IStatefulObject#getState()} <b>must not</b> be changed at any point.
	 * <br>
	 * The unique identifiers of all stateful objects <u>must remain unchanged</u> during their entire lifetime.
	 * 
	 * @return key for state Map which yields a unique identifier for an object instance, never null. Will usually return "uuid".
	 * 
	 * @see IStatefulObject#getState()
	 */
	public String getPersistenceObjectIdentifier();

	/**
	 * Returns a map which fully describes the internal state of this object. The map is persisted by
	 * the {@link IStoreProvider} and the <em>setState()</em> method is used to restore a persisted state.
	 * <p>
	 * In case of an {@link ICloneObject}, the map <b>does not</b> include any data for
	 * {@link ICloneObjectExtension} which are stored under the object.
	 * <p>
	 * The values in the map are restricted to the following object types:
	 * <ul>
	 * 	<li>String</li>
	 *  <li>Integer</li>
	 *  <li>Long</li>
	 *  <li>Boolean</li>
	 *  <li>java.util.Date</li>
	 *  <li>null</li>
	 * </ul>
	 * 
	 * The keys in the map should correspond to the following schema:
	 * <ul>
	 *  <li>Key names may not contain any characters other than letters, numbers, underscores and dots.
	 *  	Furthermore an identifier needs to contain at least one letter, may not begin with an underscore
	 * 		and may not contain multiple consecutive underscores.</li>
	 * 	<li>For simple fields the name of the field <b>has to</b> equal the name of the key in the map.
	 * 		I.e. getUuid()/setUuid(String) must use the key "uuid".</li>
	 *  <li>For complex fields the name of the field should be used as a prefix, separated by a dot and
	 *  	then followed by any field names inside the complex field.
	 *  	I.e. getPosition().getStartOffset() should use the key "position.startOffset".</li>
	 * </ul>
	 * 
	 * <b>IMPORTANT NOTEs:</b>
	 * <ul>
	 * 	<li>The key set must be static. An implementation may not change the number or names of
	 * 		keys dynamically. I.e. lists may not be represented as a variable number of keys. They have to
	 * 		be encoded as a single String element. However, keys which would contain a null value may be left out.</li>
	 * 	<li>It is crucial that all implementations adhere to the naming scheme mentioned above as some key semantics are
	 * 		hard coded. (i.e. "uuid" is the primary key)</li>
	 *  <li>The {@link IStatefulObject#getPersistenceObjectIdentifier()} entry in this state map must always be defined
	 *  	and its value <b>may not change</b> during the lifetime of a stateful object.</li>
	 * </ul>
	 * 
	 * @return a map describing the internal state of this object,  never null.
	 * 
	 * @see IStatefulObject#setState(Map)
	 */
	public Map<String, Comparable<? extends Object>> getState();

	/**
	 * Restores the internal state of this object to the state which was extracted by means of <em>getState()</em>
	 * earlier.
	 * <p>
	 * For a description of the internal structure of the map, see <em>getState()</em>.
	 * 
	 * @param state a map describing the internal state of this object, never null
	 * 
	 * @see IStatefulObject#getState()
	 */
	public void setState(Map<String, Comparable<? extends Object>> state);

	/**
	 * Returns a list of <b>all</b> keys which are needed to persist this objects internal state. The
	 * values in the returned Map correspond to the classes of the values which will be used in
	 * the Map returned by <em>getState()</em>.
	 * <p>
	 * I.e. if <em>getState()</em> will return a Map which contains the key "uuid" with the value "jda83ds-..."
	 * then this Map contains the key "uuid" with the value <em>String.class</em>.
	 * 
	 * @return map which contains <b>all</b> keys and their data types, never null
	 * 
	 * @see IStatefulObject#getState()
	 */
	public Map<String, Class<? extends Object>> getStateTypes();
}

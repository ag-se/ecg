package org.electrocodeogram.cpc.core.api.provider.data;


import java.util.List;

import org.electrocodeogram.cpc.core.api.data.ICloneDataElement;
import org.electrocodeogram.cpc.core.api.data.ICloneObject;
import org.electrocodeogram.cpc.core.api.data.ICloneObjectExtension;
import org.electrocodeogram.cpc.core.api.data.ICloneObjectSupport;
import org.electrocodeogram.cpc.core.api.data.special.IStatefulObject;
import org.electrocodeogram.cpc.core.api.provider.IProvider;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider;


/**
 * Public clone data object factory provider API.
 * <br>
 * A clone factory provider is used by all CPC modules for the creation of clone objects.
 * Clone objects are never created directly, circumventing the clone factory provider.
 * <p>
 * There may only be one active clone factory provider at all times.
 * <p>
 * 3rd party extensions should register their own {@link ICloneObject}, {@link ICloneObjectSupport}
 * and {@link ICloneObjectExtension} classes with the clone factory provider via the extension point:
 * <br>
 * <em>org.electrocodeogram.cpc.core.cloneDataElements</em>
 * 
 * @author vw
 * 
 * @see ICloneObject
 * @see ICloneObjectSupport
 * @see ICloneObjectExtension
 */
public interface ICloneFactoryProvider extends IProvider
{
	/**
	 * Creates a new instance of the specified {@link ICloneDataElement} sub class.
	 * <br>
	 * For {@link ICloneObject} sub classes a new unique uuid is automatically generated.
	 * <p>
	 * Valid values for <em>type</em> are:
	 * <ul>
	 * 	<li>IClone.class</li>
	 * 	<li>ICloneFile.class</li>
	 * 	<li>ICloneGroup.class</li>
	 * 	<li>ICloneAnnotation.class</li>
	 *  <li>IClonePosition.class</li>
	 *  <li>any of the currently used implementations of those interfaces</li>
	 *  <li>any other registered {@link ICloneObject}, {@link ICloneObjectSupport} or {@link ICloneObjectExtension}</li>
	 * </ul>
	 * 
	 * @param type the {@link ICloneDataElement} sub class to create a new instance for, never null.
	 * @return a new instance which is guaranteed to be castable to the specified <em>type</em> or null if
	 * 		no such {@link ICloneDataElement} sub class is available.
	 */
	public ICloneDataElement getInstance(Class<? extends ICloneDataElement> type);

	/**
	 * Creates a new instance of the specified {@link ICloneObject} sub class.
	 * <p>
	 * This method can <b>not</b> be used to create instances for {@link ICloneObjectSupport} sub-interfaces. 
	 * <p>
	 * Valid values for <em>type</em> are:
	 * <ul>
	 * 	<li>IClone.class</li>
	 * 	<li>ICloneFile.class</li>
	 * 	<li>ICloneGroup.class</li>
	 * 	<li>ICloneAnnotation.class</li>
	 *  <li>as well as the currently used implementations of those interfaces</li>
	 * </ul>
	 * 
	 * @param type the {@link ICloneObject} sub class to create a new instance for, never null.
	 * @param uuid the unique uuid to use for the newly created instance.
	 * @return a new instance which is guaranteed to be castable to the specified <em>type</em> or null if
	 * 		no such {@link ICloneObject} sub class is available.
	 */
	public ICloneObject getInstance(Class<? extends ICloneObject> type, String uuid);

	/**
	 * Creates a new instance of a registered {@link IStatefulObject} for the given
	 * {@link IStatefulObject#getPersistenceClassIdentifier()} value.
	 * 
	 * @param persistenceClassIdentifier the {@link IStatefulObject} persistence class identifier to create an
	 * 		instance of an implementation class for, never null. 
	 * @return a new instance which is guaranteed to yield <em>persistenceClassIdentifier</em> for
	 * 		{@link IStatefulObject#getPersistenceClassIdentifier()} or NULL if no such class is available.
	 */
	public IStatefulObject getInstanceByPersistenceClassIdentifier(String persistenceClassIdentifier);

	/**
	 * Retrieves a list of all registered {@link ICloneObject} sub-interface implementations.
	 * <br>
	 * Implementations are registered with the clone factory provider via the corresponding extension point.
	 * <p>
	 * The returned list and it's elements may not be modified.
	 * 
	 * @return a list of registered clone objects, never null.
	 */
	public List<Class<? extends ICloneObject>> getRegisteredCloneObjects();

	/**
	 * Retrieves a list of all registered {@link ICloneObjectSupport} sub-interface implementations.
	 * <br>
	 * Implementations are registered with the clone factory provider via the corresponding extension point.
	 * <p>
	 * The returned list and it's elements may not be modified.
	 * 
	 * @return a list of registered clone objects, never null.
	 */
	public List<Class<? extends ICloneObjectSupport>> getRegisteredCloneObjectSupports();

	/**
	 * Retrieves a list of all registered {@link ICloneObjectExtension} implementations.
	 * <br>
	 * Implementations are registered with the clone factory provider via the corresponding extension point.
	 * <p>
	 * The returned list and it's elements may not be modified.
	 * 
	 * @return a list of all registered clone object extensions, never null.
	 */
	public List<Class<? extends ICloneObjectExtension>> getRegisteredCloneObjectExtensions();

	/**
	 * Same as {@link ICloneFactoryProvider#getRegisteredCloneObjectExtensions()} but only returns the extensions
	 * registered for the given {@link ICloneObject} type.
	 * 
	 * @param parentType the {@link ICloneObject} type for which all registered extensions should be returned, never null.
	 * @return a list of registered clone object extensions for the given parent type, never null.
	 */
	public List<Class<? extends ICloneObjectExtension>> getRegisteredCloneObjectExtensions(
			Class<? extends ICloneObject> parentType);

	/**
	 * Some users of the <em>getRegistered...</em> methods need to create temporary instances of the
	 * classes during their processing. This adds overhead which can be critical. An extreme case of this
	 * is the {@link IStoreProvider}.
	 * <br>
	 * This method mirrors {@link ICloneFactoryProvider#getRegisteredCloneObjectExtensions(Class)} but returns
	 * shared instances of the extensions instead of their classes.
	 * <p>
	 * <b>IMPORTANT:</b> the returned objects are <b>shared</b>. Do <b>not</b> modify them in any way.  
	 * 
	 * @param parentType the {@link ICloneObject} type for which all registered extensions should be returned, never null.
	 * @return a list of <b>shared</b> instances of registered clone object extensions for the given parent type, never null.
	 */
	public List<ICloneObjectExtension> getRegisteredCloneObjectExtensionObjects(Class<? extends ICloneObject> parentType);

}

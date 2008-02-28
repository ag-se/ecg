package org.electrocodeogram.cpc.core.api.provider.xml;


import org.electrocodeogram.cpc.core.api.data.ICloneObject;
import org.electrocodeogram.cpc.core.api.provider.IProvider;
import org.electrocodeogram.cpc.core.api.provider.registry.IProviderRegistry;


/**
 * A registry which allows easy access to an {@link IMappingProvider} which supports a given
 * cpc data mapping.
 * <br>
 * All {@link IMappingProvider}s which are registered with the {@link IProviderRegistry}
 * are automatically known to this registry. Individual provider priorities are taken into account
 * if multiple providers claim to support a given cpc data mapping.
 * <p>
 * For convenience reasons the mapping registry also implements part of the {@link IMappingProvider} interface.
 * <br>
 * However, the {@link IMappingProvider} is not explicitly extended here, as many of our implementations
 * have different post conditions.
 * 
 * @author vw
 * 
 * @see IMappingProvider
 * @see IProviderRegistry
 */
public interface IMappingRegistry extends IProvider
{
	/**
	 * Checks whether any of the registered {@link IMappingProvider}s supports the given
	 * cpc data mapping.
	 * <br>
	 * The check is based on {@link IMappingProvider#isSupportedMappingFormat(String)}.
	 * <br>
	 * If multiple mapping providers support the given cpc data mapping the one with the
	 * highest priority is returned.
	 * 
	 * @param data the cpc data mapping to get a mapping provider for, never null.
	 * @return an {@link IMappingProvider} which claims to support the given cpc data mapping
	 * 		or NULL if no corresponding mapping provider could be found.
	 */
	public IMappingProvider lookupMappingProviderForDataFormat(String data);

	/**
	 * Maps the given cpc data string mapping to a {@link MappingStore} using the mapping 
	 * provider with the highest priority which claims to support the given cpc data type.
	 * <br>
	 * Convenience method.
	 * <br>
	 * First does a {@link #lookupMappingProviderForDataFormat(String)} followed by
	 * a {@link IMappingProvider#mapFromString(String)}.
	 * <br>
	 * If the initial lookup fails, NULL is returned.
	 * 
	 * @param data the cpc data mapping to convert, never null. 
	 * @return a valid {@link MappingStore} if an {@link IMappingProvider} could be found for
	 * 		the given cpc data mapping, NULL otherwise.
	 * @throws MappingException passed through from {@link IMappingProvider#mapFromString(String)}.
	 * 
	 * @see #lookupMappingProviderForDataFormat(String)
	 * @see IMappingProvider#mapFromString(String)
	 */
	public MappingStore mapFromString(String data) throws MappingException;

	/**
	 * Extracts the {@link ICloneObject#getUuid()} from the main object in the given cpc
	 * data string mapping by using the mapping provider with the highest priority which
	 * claims to support the given cpc data type.
	 * <br>
	 * Convenience method.
	 * <br>
	 * First does a {@link #lookupMappingProviderForDataFormat(String)} followed by
	 * a {@link IMappingProvider#extractCloneObjectUuidFromString(String)}.
	 * <br>
	 * If the initial lookup fails, NULL is returned.
	 * 
	 * @param data the cpc data mapping to extract the main entries UUID from, never null. 
	 * @return the UUID of the main {@link ICloneObject} entry in the given mapping or NULL.
	 * 		See {@link IMappingProvider#extractCloneObjectUuidFromString(String)}.
	 * @throws MappingException passed through from {@link IMappingProvider#extractCloneObjectUuidFromString(String)}.
	 * 
	 * @see #lookupMappingProviderForDataFormat(String)
	 * @see IMappingProvider#extractCloneObjectUuidFromString(String)
	 */
	public String extractCloneObjectUuidFromString(String data) throws MappingException;
}

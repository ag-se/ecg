package org.electrocodeogram.cpc.core.api.data;


import java.io.Serializable;

import org.electrocodeogram.cpc.core.api.provider.data.ICloneFactoryProvider;


/**
 * This is a super interface for all additional support interfaces/classes which are not themselves
 * {@link ICloneObject} implementations but which are non the less part of the CPC Clone Data objects.
 * <p>
 * Rationale:
 * <blockquote>
 * This interface exists only to provide type safety for calls where a distinction between
 * {@link ICloneObject}s and other {@link ICloneDataElement}s is needed.
 * </blockquote>
 * 
 * @author vw
 * 
 * @see ICloneFactoryProvider#getInstance(Class)
 */
public interface ICloneObjectSupport extends ICloneDataElement, Serializable, Cloneable
{
	/*
	 * This interface defines no methods. 
	 */
}

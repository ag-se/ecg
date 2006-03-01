/*
 * Class: MsdtProvider
 * Version: 1.0
 * Date: 18.10.2005
 * By: Frank@Schlesinger.com
 */

package org.electrocodeogram.msdt;


/**
 * Every ECG module implements this interface in its superclass {@link org.electrocodeogram.module.Module}.
 * To avoid circular dependencies the {@link org.electrocodeogram.msdt.registry.MsdtRegistry} works
 * with this interface rather than with the  <code>Module</code> object.
 */
public interface MsdtProvider {

    /**
     * @see org.electrocodeogram.module.Module#getName()
     */
    String getName();

}

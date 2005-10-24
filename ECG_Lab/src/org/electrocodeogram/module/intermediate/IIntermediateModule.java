/*
 * Class: IIntermediateModule
 * Version: 1.0
 * Date: 18.10.2005
 * By: Frank@Schlesinger.com
 */

package org.electrocodeogram.module.intermediate;

import org.electrocodeogram.module.intermediate.IntermediateModule.ProcessingMode;

/**
 * This <code>Interface</code> is used by {@link org.electrocodeogram.module.Module}
 * to access <em>IntermediateModules</em> to avoid circular dependencies.
 */
public interface IIntermediateModule {

    /**
     * This returns the <em>ProcessingMode</em> the module is operating in.
     * @return The <em>ProcessingMode</em>
     */
    ProcessingMode getProcessingMode();

}

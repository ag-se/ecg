/*
 * Class: IIntermediateModule
 * Version: 1.0
 * Date: 18.10.2005
 * By: Frank@Schlesinger.com
 */

package org.electrocodeogram.module.intermediate;

import org.electrocodeogram.module.intermediate.IntermediateModule.ProcessingMode;

/**
 * This interface is used by {@link org.electrocodeogram.module.Module}
 * to access {@link IntermediateModule} to avoid circular dependencies.
 */
public interface IIntermediateModule {

    /**
     * This returns the <em>ProcessingMode</em> the module is operating in.
     * @return The <em>ProcessingMode</em>
     */
    ProcessingMode getProcessingMode();

}

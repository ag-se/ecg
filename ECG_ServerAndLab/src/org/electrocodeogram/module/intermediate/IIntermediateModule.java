package org.electrocodeogram.module.intermediate;

import org.electrocodeogram.module.intermediate.IntermediateModule.ProcessingMode;

/**
 * This Interface is used by the class Module to access IntermediateModules.
 * Access to the IntermediateModule class itself would lead to circular dependencies
 * as an IntermediateModule is extending the class Module.
 */
public interface IIntermediateModule
{

    /**
     * This returns the processing mode the module is operating in.
     * @return The processing mode
     */
    public ProcessingMode getProcessingMode();

}
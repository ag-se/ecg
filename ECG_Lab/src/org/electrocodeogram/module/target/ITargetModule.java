/*
 * Class: ITargetModule
 * Version: 1.0
 * Date: 18.10.2005
 * By: Frank@Schlesinger.com
 */

package org.electrocodeogram.module.target;

/**
 * This <code>Interface</code> is used by
 * {@link org.electrocodeogram.module.Module} to avoid circular
 * dependencies.
 */
public interface ITargetModule {

    /**
     * This is to be implemented by all actual <em>TargetModules</em>.
     * @throws TargetModuleException
     *             If an <code>Exception</code> occurs while
     *             starting the writer.
     */
    void startWriter() throws TargetModuleException;

    /**
     * This is to be implemented by all actual <em>TargetModules</em>.
     */
    void stopWriter();

}

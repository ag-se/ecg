/*
 * Class: ITargetModule
 * Version: 1.0
 * Date: 18.10.2005
 * By: Frank@Schlesinger.com
 */

package org.electrocodeogram.module.target;

/**
 * This interface is used by
 * {@link org.electrocodeogram.module.Module} to avoid circular
 * dependencies.
 */
public interface ITargetModule {

    /**
     * This is to be implemented by all actual <code>TargetModule</code> implementations.
     * @throws TargetModuleException
     *             If an exception occurs while
     *             starting the writer.
     */
    void startWriter() throws TargetModuleException;

    /**
     * This is to be implemented by all actual <code>TargetModule</code>.
     */
    void stopWriter();

}

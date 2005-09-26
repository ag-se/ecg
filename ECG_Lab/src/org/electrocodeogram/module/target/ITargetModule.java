/**
 * 
 */
package org.electrocodeogram.module.target;

/**
 *
 */
public interface ITargetModule
{

	public abstract void startWriter() throws TargetModuleException;

	public abstract void stopWriter();

}
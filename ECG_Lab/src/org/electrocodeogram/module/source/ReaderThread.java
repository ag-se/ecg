/**
 * 
 */
package org.electrocodeogram.module.source;

/**
 *
 */
public abstract class ReaderThread extends Thread
{

	private SourceModule _sourceModule;
	
	public ReaderThread(SourceModule sourceModule)
	{
		this._sourceModule = sourceModule;
	}
	
	public abstract void run();
	
	
	
}

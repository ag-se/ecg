package org.electrocodeogram.cpc.core.api.data.collection;


import org.electrocodeogram.cpc.core.api.data.ICloneFile;
import org.electrocodeogram.cpc.core.api.data.special.ICreatorCloneFile;
import org.electrocodeogram.cpc.core.api.data.special.IRemoteStoreCloneFile;


/**
 * Includes all interfaces which are required for an {@link ICloneFile} implementation.
 * <br>
 * Convenience collection interface.
 * 
 * @author vw
 */
public interface ICloneFileInterfaces extends ICloneFile, ICreatorCloneFile, IRemoteStoreCloneFile,
		ICloneObjectInterfaces
{
	/*
	 * Defines no methods.
	 */
}

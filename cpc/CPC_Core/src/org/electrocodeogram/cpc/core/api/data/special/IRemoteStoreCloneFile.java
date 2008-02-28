package org.electrocodeogram.cpc.core.api.data.special;


import org.electrocodeogram.cpc.core.api.data.ICloneFile;
import org.electrocodeogram.cpc.core.api.data.ICloneObjectExtension;
import org.electrocodeogram.cpc.core.api.hub.event.EclipseTeamEvent;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider;


/**
 * Extension interface for {@link ICloneFile} which provides access to internal data
 * fields for use only by an {@link IStoreProvider}.
 * <p>
 * All {@link ICloneFile} implementations have to implement this interface.
 * <p>
 * Rationale:
 * <blockquote>
 * These fields should not be accessed by other plugins.
 * They are therefore "hidden" by this extra interface. The fact that an {@link ICloneFile} object
 * will need to be cast to this interface before any of the fields can be accessed is meant
 * to work as a deterrent for accidental access to these fields.
 * <br>
 * The {@link ICloneObjectExtension} mechanism is not used by the base CPC plugins for performance reasons.
 * </blockquote>
 * 
 * @author vw
 * 
 * @see ICloneFile
 */
public interface IRemoteStoreCloneFile extends ICloneFile
{
	/**
	 * The current repository version number for the file underlying this {@link ICloneFile} object.
	 * <br>
	 * This value equals the ones in {@link EclipseTeamEvent#getNewRevision()}. 
	 * 
	 * @return repository version or NULL if not set
	 */
	public String getRepositoryVersion();

	/**
	 * This value equals the ones in {@link EclipseTeamEvent#getNewRevision()}.
	 *  
	 * @param repositoryVersion the repository version to set, may be null
	 */
	public void setRepositoryVersion(String repositoryVersion);

	/**
	 * Whether the clone data for this file was locally modified since the last sync with the repository
	 * and must therefore be sent to the repository with the next commit. 
	 */
	public boolean isRemoteDirty();

	/**
	 * Sets the remote dirty flag for this clone file.<br/>
	 * 
	 * @see #isRemoteDirty()
	 */
	public void setRemoteDirty(boolean remoteDirty);
}

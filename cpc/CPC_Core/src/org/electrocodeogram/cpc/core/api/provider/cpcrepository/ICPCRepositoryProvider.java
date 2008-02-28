package org.electrocodeogram.cpc.core.api.provider.cpcrepository;


import org.electrocodeogram.cpc.core.api.data.ICloneFile;
import org.electrocodeogram.cpc.core.api.provider.IProvider;


/**
 * An {@link ICPCRepositoryProvider} provides a centralised remote storage service for CPC
 * clone data which may be access concurrently from multiple CPC installations.
 * <p>
 * A typical use for a provider of this type is to store the current cpc data for a file
 * in a central location, whenever it is committed into a source repository and to
 * fetch the most up to date cpc data for a file whenever it is checked out, updated
 * or merged.
 * <p>
 * Most methods of this API are likely to require remote calls. They can thus fail and
 * are potentially long running.
 * <p>
 * <b>NOTE:</b> Any implementation must guarantee that concurrent calls to
 * the put and get methods on this and other systems do not result in invalid data.
 * 
 * @author vw
 * 
 * @see ICPCRevision
 */
public interface ICPCRepositoryProvider extends IProvider
{
	/**
	 * Checks whether this cpc repository is currently available.
	 * <p>
	 * This method will typically try to connect to a remote location and
	 * verify that there are no connectivity or version incompatibility
	 * issues which would prevent the normal use of this provider.
	 * <p>
	 * If this method returns <em>false</em> most other methods of this API
	 * are likely to throw an exception when used.
	 * <br>
	 * However return value of <em>true</em> does not guarantee that no
	 * exception will occur. 
	 * 
	 * @return <em>true</em> if the repository is ready for use, <em>false</em> if there are
	 * 		any conditions which prevent normal use.
	 */
	public boolean isAvailable();

	/**
	 * Stores the given cpc data revision in the remote repository.
	 * <br>
	 * An exception will be thrown if the repository already contains an entry
	 * for that file with the same revision identifier.
	 * 
	 * @param cpcRevision the cpc data revision to store, never null.
	 */
	public void putRevision(ICPCRevision cpcRevision) throws CPCRepositoryException;

	/**
	 * Retrieves the cpc data revision with the given <em>revisionId</em> for the
	 * {@link ICloneFile} with the given <em>cloneFileUuid</em>.
	 * 
	 * @param revisionId the revision identifier to retrieve, never null.
	 * @param cloneFileUuid the {@link ICloneFile#getUuid()} value of the file to retrieve the data for, never null.
	 * @return a {@link ICPCRevision} or NULL if no such revision was found.
	 */
	public ICPCRevision getRevision(String revisionId, String cloneFileUuid) throws CPCRepositoryException;

	/**
	 * Retrieves the cpc data revision with the given <em>revisionId</em> for the
	 * {@link ICloneFile} with the given <em>project</em> and <em>filePath</em> location.
	 * 
	 * @param revisionId the revision identifier to retrieve, never null.
	 * @param project the project name of the {@link ICloneFile} to retrieve the data for, never null.
	 * @param filePath the project relative path of the {@link ICloneFile} to retrieve the data for, never null.
	 * @return a {@link ICPCRevision} or NULL if no such revision was found.
	 */
	public ICPCRevision getRevision(String revisionId, String project, String filePath) throws CPCRepositoryException;

	/**
	 * Tells the {@link ICPCRepositoryProvider} that the specified revision is no longer needed and
	 * can be deleted.
	 * <br>
	 * The {@link ICPCRepositoryProvider} may not return a revision to any client
	 * once it has been marked for purging in this way.
	 * 
	 * @param revisionId the revision identifier of the revision to purge, never null.
	 * @param cloneFileUuid cloneFileUuid the {@link ICloneFile#getUuid()} value of the file to purge the revision for, never null.
	 * @return <em>true</em> if such a revision was found and purged, <em>false</em> if no such revision existed.
	 */
	public boolean purgeRevision(String revisionId, String cloneFileUuid) throws CPCRepositoryException;

	/**
	 * Tells the {@link ICPCRepositoryProvider} that the specified revision is no longer needed and
	 * can be deleted.
	 * <br>
	 * The {@link ICPCRepositoryProvider} may not return a revision to any client
	 * once it has been marked for purging in this way.
	 * 
	 * @param revisionId the revision identifier of the revision to purge, never null.
	 * @param project the project name of the {@link ICloneFile} to purge the data for, never null.
	 * @param filePath the project relative path of the {@link ICloneFile} to purge the data for, never null.
	 * @return <em>true</em> if such a revision was found and purged, <em>false</em> if no such revision existed.
	 */
	public boolean purgeRevision(String revisionId, String project, String filePath) throws CPCRepositoryException;

	/**
	 * Creates a new and empty {@link ICPCRevision} instance which can then be filled
	 * by the client.
	 * <p>
	 * {@link ICPCRevision} instances must not be mixed between {@link ICPCRepositoryProvider}s and 
	 * a client may not use its own implementations.
	 * 
	 * @return new and empty {@link ICPCRevision} instance, never null.
	 */
	public ICPCRevision createRevision();

	/**
	 * This method should be called by clients of this interface if it is expected that multiple
	 * repository operations will be done within a short period of time.
	 * <p>
	 * It is up to the implementation of this interface whether to make use of this additional
	 * information. The implementation may not rely on a call to this method. The visible behaviour
	 * of the implementation must not change depending on whether this method is called or not.
	 * <p>
	 * While calling this method is optional, a client must call {@link #hintEndTransaction()} once
	 * it called this method.
	 * <p>
	 * <b>NOTE:</b> this method is only meant to improve performance.
	 * No transactional properties are guaranteed by this API.
	 * <p>
	 * A typical use of this method is the setup of some remote network connection.
	 * 
	 * @see #hintEndTransaction()
	 */
	public void hintStartTransaction() throws CPCRepositoryException;

	/**
	 * This method must only be called if {@link #hintStartTransaction()} was called. For each call to
	 * {@link #hintStartTransaction()} there needs to be exactly one call to {@link #hintEndTransaction()}.
	 * <p>
	 * A typical use of this method is the shutdown of some remote network connection.
	 * 
	 * @see #hintStartTransaction()
	 */
	public void hintEndTransaction();
}

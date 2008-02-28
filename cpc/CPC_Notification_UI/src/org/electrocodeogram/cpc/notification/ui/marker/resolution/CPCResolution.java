package org.electrocodeogram.cpc.notification.ui.marker.resolution;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolution2;
import org.electrocodeogram.cpc.core.CPCCorePlugin;
import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider;
import org.electrocodeogram.cpc.core.api.provider.store.StoreLockingException;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider.LockMode;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider.UpdateMode;
import org.electrocodeogram.cpc.notification.ui.marker.NotificationMarker;
import org.electrocodeogram.cpc.notification.ui.marker.NotificationMarkerResolutionGenerator;


/**
 * Common base class for CPC <em>notificationmarker</em> {@link IMarkerResolution}s.
 * 
 * @author vw
 * 
 * @see NotificationMarker
 * @see NotificationMarkerResolutionGenerator
 */
public abstract class CPCResolution implements IMarkerResolution2
{
	private static final Log log = LogFactory.getLog(CPCResolution.class);

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IMarkerResolution2#getImage()
	 */
	@Override
	public Image getImage()
	{
		//by default there is no image
		return null;
	}

	/**
	 * Takes a cpc <em>notificationmarker</em> and a new {@link IClone.State} and updates the clone
	 * which corresponds to the given marker to the new state.
	 *  
	 * @param marker the <em>notificationmarker</em> which represents the clone, never null.
	 * @param state the new state for the clone, never null.
	 */
	protected void setCloneState(IMarker marker, IClone.State state)
	{
		if (log.isTraceEnabled())
			log.trace("setCloneState() - marker: " + marker + ", state: " + state);
		assert (marker != null && state != null);

		IStoreProvider storeProvider = (IStoreProvider) CPCCorePlugin.getProviderRegistry().lookupProvider(
				IStoreProvider.class);
		assert (storeProvider != null);

		try
		{
			storeProvider.acquireWriteLock(LockMode.DEFAULT);

			//get the clone which corresponds to this marker
			IClone clone = storeProvider.lookupClone((String) marker
					.getAttribute(NotificationMarker.MARKER_FIELD_CLONE_UUID));
			if (clone == null)
			{
				//the clone was concurrently deleted.
				if (log.isDebugEnabled())
					log.debug("setCloneState() - the clone for the marker was concurrently deleted - clone uuid: "
							+ (String) marker.getAttribute(NotificationMarker.MARKER_FIELD_CLONE_UUID));
				return;
			}

			//update the state
			clone.setCloneState(state, 0, null);

			//update the clone
			storeProvider.updateClone(clone, UpdateMode.MOVED);
		}
		catch (CoreException e)
		{
			log.error("run() - unable to read marker - " + marker + " - " + e, e);
		}
		catch (StoreLockingException e)
		{
			log.error("run() - locking error - " + e, e);
		}
		finally
		{
			storeProvider.releaseWriteLock();
		}
	}

	/**
	 * Removes the clone from the CPC database.
	 * 
	 * @param marker the cpc <em>notificationmarker</em> which corresponds to the clone which should be removed, never null.
	 */
	protected void removeClone(IMarker marker)
	{
		if (log.isTraceEnabled())
			log.trace("removeClone() - marker: " + marker);
		assert (marker != null);

		IStoreProvider storeProvider = (IStoreProvider) CPCCorePlugin.getProviderRegistry().lookupProvider(
				IStoreProvider.class);
		assert (storeProvider != null);

		try
		{
			storeProvider.acquireWriteLock(LockMode.DEFAULT);

			//get the clone which corresponds to this marker
			IClone clone = storeProvider.lookupClone((String) marker
					.getAttribute(NotificationMarker.MARKER_FIELD_CLONE_UUID));
			if (clone == null)
			{
				//the clone was concurrently deleted.
				if (log.isDebugEnabled())
					log.debug("removeClone() - the clone for the marker was concurrently deleted - clone uuid: "
							+ (String) marker.getAttribute(NotificationMarker.MARKER_FIELD_CLONE_UUID));
				return;
			}

			//remove the clone
			storeProvider.removeClone(clone);
		}
		catch (CoreException e)
		{
			log.error("run() - unable to read marker - " + marker + " - " + e, e);
		}
		catch (StoreLockingException e)
		{
			log.error("run() - locking error - " + e, e);
		}
		finally
		{
			storeProvider.releaseWriteLock();
		}
	}
}

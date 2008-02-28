package org.electrocodeogram.cpc.notification.ui.marker.resolution;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IMarker;
import org.electrocodeogram.cpc.core.api.data.IClone.State;


public class IgnoreNotificationResolution extends CPCResolution
{
	private static final Log log = LogFactory.getLog(IgnoreNotificationResolution.class);

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IMarkerResolution2#getDescription()
	 */
	@Override
	public String getDescription()
	{
		return "Ignores this CPC notification until the clone is modified again.";
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IMarkerResolution#getLabel()
	 */
	@Override
	public String getLabel()
	{
		return "CPC: Ignore this notification for now";
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IMarkerResolution#run(org.eclipse.core.resources.IMarker)
	 */
	@Override
	public void run(IMarker marker)
	{
		if (log.isTraceEnabled())
			log.trace("run() - marker: " + marker);

		//reset state to modified
		//the marker should be cleared automatically once we do this.
		setCloneState(marker, State.MODIFIED);
	}
}

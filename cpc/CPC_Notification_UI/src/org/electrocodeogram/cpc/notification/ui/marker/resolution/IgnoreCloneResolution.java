package org.electrocodeogram.cpc.notification.ui.marker.resolution;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IMarker;
import org.electrocodeogram.cpc.core.api.data.IClone.State;


public class IgnoreCloneResolution extends CPCResolution
{
	private static final Log log = LogFactory.getLog(IgnoreCloneResolution.class);

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IMarkerResolution2#getDescription()
	 */
	@Override
	public String getDescription()
	{
		return "The clone will be marked as ignored. Its position will still be tracked and it will "
				+ "still be displayed in the user interface. But you will not receive any CPC notifications for "
				+ "modifications made to this clone. An ignored clone can be \"unignored\" at any time.";
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IMarkerResolution#getLabel()
	 */
	@Override
	public String getLabel()
	{
		return "CPC: Ignore this clone";
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

		//mark the clone as IGNOREd
		setCloneState(marker, State.IGNORE);
	}

}

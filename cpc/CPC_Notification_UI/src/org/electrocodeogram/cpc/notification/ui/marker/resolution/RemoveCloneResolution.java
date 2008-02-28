package org.electrocodeogram.cpc.notification.ui.marker.resolution;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IMarker;


public class RemoveCloneResolution extends CPCResolution
{
	private static final Log log = LogFactory.getLog(RemoveCloneResolution.class);

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IMarkerResolution2#getDescription()
	 */
	@Override
	public String getDescription()
	{
		return "The clone will removed from the CPC database. In effect CPC will forget all it knows about this clone. "
				+ "You will no longer be notified about any modification to this clone. This action becomes permanent "
				+ "once you safe the file. It can not be undone.";
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IMarkerResolution#getLabel()
	 */
	@Override
	public String getLabel()
	{
		return "CPC: Remove/forget this clone";
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

		//delete the clone
		removeClone(marker);
	}

}

package org.electrocodeogram.cpc.notification.ui.marker;


import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator2;
import org.electrocodeogram.cpc.notification.ui.marker.resolution.CPCResolution;
import org.electrocodeogram.cpc.notification.ui.marker.resolution.IgnoreCloneResolution;
import org.electrocodeogram.cpc.notification.ui.marker.resolution.IgnoreNotificationResolution;
import org.electrocodeogram.cpc.notification.ui.marker.resolution.RemoveCloneResolution;


public class NotificationMarkerResolutionGenerator implements IMarkerResolutionGenerator2
{
	private static final Log log = LogFactory.getLog(NotificationMarkerResolutionGenerator.class);

	/*
	 * TODO: consider whether it would be a good idea to make this static.
	 * Especially if the overhead for resolution registration should increase some day.
	 */
	private List<CPCResolution> registeredResolutions = null;

	public NotificationMarkerResolutionGenerator()
	{
		log.trace("NotificationMarkerResolutionGenerator()");

		/*
		 * Note: for some reason Eclipse generates new instances of resolution generator classes
		 * all the time. A single click on a cpc notificationmarker, will cause at least
		 * 4-5 instances of this class to be created!
		 * All but the last are only used for hasResolutions() inquiries. The constructor of
		 * this class therefore needs to be lightweight and any serious work should be done only
		 * if getResolutions() is called.
		 */
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IMarkerResolutionGenerator2#hasResolutions(org.eclipse.core.resources.IMarker)
	 */
	@Override
	public boolean hasResolutions(IMarker marker)
	{
		if (log.isTraceEnabled())
			log.trace("hasResolutions() - marker: " + marker);

		//we always have resolutions
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IMarkerResolutionGenerator#getResolutions(org.eclipse.core.resources.IMarker)
	 */
	@Override
	public IMarkerResolution[] getResolutions(IMarker marker)
	{
		if (log.isTraceEnabled())
			log.trace("getResolutions() - marker: " + marker);

		//make sure we have all registered solutions.
		initialiseResolutions();

		return registeredResolutions.toArray(new IMarkerResolution[registeredResolutions.size()]);
	}

	/**
	 * Loads all registered resolutions from the corresponding extension point.
	 */
	private void initialiseResolutions()
	{
		//make sure we only do this once
		if (registeredResolutions != null)
			return;

		registeredResolutions = new LinkedList<CPCResolution>();

		//TODO: retrieve resolutions from an extension point

		registeredResolutions.add(new IgnoreNotificationResolution());
		registeredResolutions.add(new IgnoreCloneResolution());
		registeredResolutions.add(new RemoveCloneResolution());
	}
}

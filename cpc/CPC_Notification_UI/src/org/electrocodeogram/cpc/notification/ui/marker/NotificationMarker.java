package org.electrocodeogram.cpc.notification.ui.marker;


import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.texteditor.MarkerUtilities;
import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.data.ICloneFile;
import org.electrocodeogram.cpc.core.api.provider.notification.IEvaluationResult;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider;
import org.electrocodeogram.cpc.core.utils.CoreFileUtils;


/**
 * Simple convenience class which collects all <em>notificationmarker</em> related methods in one
 * location.<br/>
 * <br/>
 * <b>NOTE:</b> {@link IMarker} instances returned by Eclipse are <b>never</b> instances this class!
 * This class represents a collection of static methods which are located here purely for convenience
 * reasons. This class is never instantiated. 
 * 
 * @author vw
 */
public class NotificationMarker
{
	private static final Log log = LogFactory.getLog(NotificationMarker.class);

	public static final String MARKER_ID = "org.electrocodeogram.cpc.notification.ui.notificationmarker";

	/**
	 * {@link IMarker} attribute name for the clone UUID value of an <em>notificationmarker</em>.<br/>
	 * This is a required field.
	 */
	public static final String MARKER_FIELD_CLONE_UUID = "cpc_clone_uuid";

	/**
	 * {@link IMarker} attribute name for the {@link NotificationMarker.Type} value of an <em>notificationmarker</em>.<br/>
	 * This is a required field.
	 */
	public static final String MARKER_FIELD_TYPE = "cpc_type";

	private static final List<IMarker> EMPTY_MARKER_LIST = new LinkedList<IMarker>();

	public enum Type
	{
		/**
		 * Displayed like an info marker.
		 */
		NOTIFY,

		/**
		 * Displayed like a java compiler warning marker.
		 */
		WARN
	}

	/**
	 * This class is not meant to be instantiated.
	 */
	private NotificationMarker()
	{

	}

	/**
	 * Retrieves a list of all <em>notificationmarker</em>s which are currently added for the given
	 * clone file.
	 * 
	 * @param cloneFile the clone file to retrieve all markers for, never null.
	 * @return a list of <em>notificationmarker</em>s for the given file, never null.
	 * 		These <b>are not</b> {@link NotificationMarker} instances! 
	 */
	public static List<IMarker> getMarkers(ICloneFile cloneFile)
	{
		if (log.isTraceEnabled())
			log.trace("getMarkers() - cloneFile: " + cloneFile);
		assert (cloneFile != null);

		//first get an IFile handle for the clone file
		IFile fileHandle = CoreFileUtils.getFileForCloneFile(cloneFile);
		if (fileHandle == null)
		{
			/*
			 * This might happen if a file is deleted or a project is closed. 
			 */
			if (log.isDebugEnabled())
				log.debug("getMarkers() - unable to obtain file handle for clone file, returning empty list: "
						+ cloneFile);
			return EMPTY_MARKER_LIST;
		}

		try
		{
			IMarker[] problems = fileHandle.findMarkers(MARKER_ID, true, IResource.DEPTH_INFINITE);
			List<IMarker> result = Arrays.asList(problems);

			if (log.isTraceEnabled())
				log.trace("getMarkers() - result: " + result);

			return result;
		}
		catch (CoreException e)
		{
			log.error("getMarkers() - unable to obtain markers for clone file - " + e, e);
			return EMPTY_MARKER_LIST;
		}
	}

	/**
	 * Fetches all marker data for the given file from the store provider and (re)creates
	 * all corresponding markers. Any existing markers will be removed.
	 * 
	 * @param storeProvider a valid store provider reference, never null.
	 * @param cloneFile the clone file for which all markers should be (re)created, never null.
	 */
	public static void createMarkers(IStoreProvider storeProvider, ICloneFile cloneFile)
	{
		if (log.isTraceEnabled())
			log.trace("createMarkers() - cloneFile: " + cloneFile + ", storeProvider: " + storeProvider);
		assert (cloneFile != null && storeProvider != null);

		//first remove all markers from the file
		log.trace("createMarkers() - dropping all markers from file.");
		removeMarkers(cloneFile);

		//get all clones for this file and check whether any of them is marked as clone state NOTIFY or WARN
		List<IClone> clones = storeProvider.getClonesByFile(cloneFile.getUuid());
		int markerCount = 0;
		for (IClone clone : clones)
		{
			if (IClone.State.NOTIFY.equals(clone.getCloneState()) || IClone.State.WARN.equals(clone.getCloneState()))
			{
				//create a new marker for this clone

				if (log.isTraceEnabled())
					log.trace("createMarkers() - creating new marker for clone: " + clone);

				//create the marker
				NotificationMarker.createMarker(cloneFile, clone, Type.valueOf(clone.getCloneState().toString()),
						buildMarkerMessage(clone.getCloneStateMessage()));
				++markerCount;
			}
		}

		if (log.isTraceEnabled())
			log.trace("createMarkers() - done - " + clones.size() + " clones checked, " + markerCount
					+ " markers created.");
	}

	/**
	 * Creates a new <em>notificationmarker</em> of the given type to the clone file. 
	 * 
	 * @param cloneFile the clone file in which the clone is located, never null.
	 * @param clone the clone to create a marker for, never null.
	 * @param type the {@link Type} of this marker, never null.
	 * @param message the message to display in the problems view, never null.
	 */
	public static void createMarker(ICloneFile cloneFile, IClone clone, Type type, String message)
	{
		if (log.isTraceEnabled())
			log.trace("createMarker() - cloneFile: " + cloneFile + ", clone: " + clone + ", type: " + type
					+ ", message: " + message);
		assert (cloneFile != null && clone != null && type != null && message != null);

		//first get an IFile handle for the clone file
		IFile fileHandle = CoreFileUtils.getFileForCloneFile(cloneFile);
		if (fileHandle == null)
		{
			/*
			 * This might happen if a file is deleted or a project is closed.
			 */
			if (log.isDebugEnabled())
				log.debug("createMarker() - unable to obtain file handle for clone file, ignoring: " + cloneFile);
			return;
		}

		//now setup the marker data
		Map<String, Object> attributes = new HashMap<String, Object>(10);
		MarkerUtilities.setCharStart(attributes, clone.getOffset());
		MarkerUtilities.setCharEnd(attributes, clone.getEndOffset());
		MarkerUtilities.setMessage(attributes, message);
		attributes.put(MARKER_FIELD_CLONE_UUID, clone.getUuid());
		attributes.put(MARKER_FIELD_TYPE, type.toString());

		//set the severity according to type
		if (Type.WARN.equals(type))
			attributes.put(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
		else
			attributes.put(IMarker.SEVERITY, IMarker.SEVERITY_INFO);

		//and finally create the marker
		try
		{
			MarkerUtilities.createMarker(fileHandle, attributes, MARKER_ID);
		}
		catch (CoreException e)
		{
			log.error("createMarker() - marker creation failed: " + e, e);
		}
	}

	/**
	 * Removes all <em>notificationmarker</em>s from the given file.
	 * 
	 * @param cloneFile the file to remove all markers from, never null.
	 */
	public static void removeMarkers(ICloneFile cloneFile)
	{
		if (log.isTraceEnabled())
			log.trace("removeMarkers() - cloneFile: " + cloneFile);
		assert (cloneFile != null);

		//first get an IFile handle for the clone file
		IFile fileHandle = CoreFileUtils.getFileForCloneFile(cloneFile);
		if (fileHandle == null)
		{
			/*
			 * This can happen if a file was deleted or a project was closed.
			 * There isn't much for us to do in that case.
			 * 
			 * TODO: Do we need some special handling for project closure?
			 * 		I.e. what happens if the project is closed while it has markers?
			 * 		The markers wouldn't be removed. However, we might be receiving the
			 * 		editor close event first. And on reopening the file we'd recreate
			 * 		the markers anyway? So this might not be a problem.
			 */
			if (log.isDebugEnabled())
				log.debug("removeMarkers() - unable to obtain file handle for clone file, ignoring: " + cloneFile);
			return;
		}

		try
		{
			fileHandle.deleteMarkers(MARKER_ID, true, IResource.DEPTH_INFINITE);
		}
		catch (CoreException e)
		{
			log.error("removeMarkers() - unable to remove cpc notification markers from file - cloneFile: " + cloneFile
					+ " - " + e, e);
		}
	}

	/**
	 * Removes an <em>notificationmarker</em> with the specified clone UUID from the given file.
	 * 
	 * @param cloneFile the file to remove all markers from, never null.
	 * @param cloneUuid clone uuid for which any existing marker should be dropped, never null.
	 */
	public static void removeMarker(ICloneFile cloneFile, String cloneUuid)
	{
		if (log.isTraceEnabled())
			log.trace("removeMarker() - cloneFile: " + cloneFile + ", cloneUuid: " + cloneUuid);
		assert (cloneFile != null && cloneUuid != null);

		//get all markers for the file
		List<IMarker> markers = getMarkers(cloneFile);

		//check clone uuid against each marker
		for (IMarker marker : markers)
		{
			try
			{
				if (cloneUuid.equals(marker.getAttribute(MARKER_FIELD_CLONE_UUID)))
				{
					//ok, this marker matches the given clone uuid, delete it.
					if (log.isTraceEnabled())
						log.trace("removeMarker() - deleting marker: " + marker);

					marker.delete();

					//there shouldn't be more than one marker per clone.
					return;
				}
			}
			catch (CoreException e)
			{
				log.error("removeMarker() - unable to extract clone uuid data from or delete marker - " + e, e);
			}
		}

		log.trace("removeMarker() - no matching marker found.");
	}

	/**
	 * Removes all <em>notificationmarker</em>s from the entire workspace.
	 */
	public static void removeAllMarkers()
	{
		log.trace("removeAllMarkers()");

		try
		{
			ResourcesPlugin.getWorkspace().getRoot().deleteMarkers(MARKER_ID, true, IResource.DEPTH_INFINITE);
		}
		catch (CoreException e)
		{
			log.error("removeAllMarkers() - unable to purge all notificationmarkers from workspace - " + e, e);
		}
	}

	/**
	 * Takes an {@link IEvaluationResult#getMessage()} message and converts it into a message
	 * which is suitable for use in marker creation.<br/>
	 * If the given message is NULL, a default message is returned.
	 * 
	 * @param message the message to convert, may be NULL.
	 * @return the resulting marker message,  never null.
	 */
	public static String buildMarkerMessage(String message)
	{
		if (message != null)
		{
			//the event has a specific message
			return "CPC: " + message;
		}
		else
		{
			//default message
			return "CPC: possible update anomaly during clone modification";
		}
	}
}

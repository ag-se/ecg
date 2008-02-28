package org.electrocodeogram.cpc.sensor.listener;


import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Internal registry for all currently active document listeners.<br/>
 * It is used for cross-listener lookups of the active document listener for a specific document,
 * i.e. to force a flush of it's diff caches. 
 * 
 * @author vw
 * @deprecated
 */
@Deprecated
public class CPCDocumentListenerRegistry
{
	private static final Log log = LogFactory.getLog(CPCDocumentListenerRegistry.class);

	/**
	 * A map which keep references to document listeners which were registered for
	 * specific locations.
	 */
	private static Map<String, CPCSingleDocumentListener> docListenerCache = new HashMap<String, CPCSingleDocumentListener>();;

	/**
	 * This class is not meant to be instantiated.
	 * 
	 * @deprecated
	 */
	@Deprecated
	private CPCDocumentListenerRegistry()
	{

	}

	/**
	 * Looks up a document listener for a given location.
	 *  
	 * @param location the location to get a document listener for, never null.
	 * @return a document listener for the given location if one was registered, NULL otherwise.
	 * @deprecated
	 */
	@Deprecated
	public synchronized static CPCSingleDocumentListener lookupDocumentListenerForLocation(String location)
	{
		//		if (log.isTraceEnabled())
		//			log.trace("getDocumentListenerForLocation() - location: " + location);
		//		assert (location != null);
		//
		//		CPCSingleDocumentListener listener = docListenerCache.get(location);
		//
		//		if (log.isTraceEnabled())
		//		{
		//			log.trace("CURRENT LISTENER CACHE: " + docListenerCache);
		//			log.trace("getDocumentListenerForEditor() - result: " + listener);
		//		}
		//
		//		return listener;
		return null;
	}

	/**
	 * Registers a document listener for a given location.<br/>
	 * It is crucial that each registered document is unregistered again at some point!
	 * 
	 * @param location the location to register the listener for, never null.
	 * @param documentListener the document listener to register, never null.
	 * 
	 * @see CPCDocumentListenerRegistry#unregisterDocumentListenerForLocation(String)
	 * @deprecated
	 */
	@Deprecated
	public synchronized static void registerDocumentListenerForLocation(String location,
			CPCSingleDocumentListener documentListener)
	{
		//		if (log.isTraceEnabled())
		//			log.trace("registerDocumentListenersForLocation() - location: " + location + ", documentListener: "
		//					+ documentListener);
		//		assert (location != null && documentListener != null);
		//
		//		//add it to the document listener cache
		//		docListenerCache.put(location, documentListener);
		//
		//		if (log.isTraceEnabled())
		//			log.trace("CURRENT LISTENER CACHE: " + docListenerCache);
	}

	/**
	 * Unregisters a listener with the registry.<br/>
	 * It is crucial that this method is called for each registered listener, otherwise
	 * listener objects will be leaked.
	 * 
	 * @param location the location for which the listener was registered, never null.
	 * @deprecated
	 */
	@Deprecated
	public synchronized static void unregisterDocumentListenerForLocation(String location)
	{
		//		if (log.isTraceEnabled())
		//			log.trace("unregisterDocumentListenersForLocation() - location: " + location);
		//		assert (location != null);
		//
		//		docListenerCache.remove(location);
		//
		//		if (log.isTraceEnabled())
		//			log.trace("CURRENT LISTENER CACHE: " + docListenerCache);
	}

}

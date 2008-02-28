package org.electrocodeogram.cpc.track.codediff;


import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension4;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IPositionUpdater;
import org.eclipse.jface.text.Position;
import org.eclipse.text.undo.DocumentUndoEvent;
import org.eclipse.text.undo.IDocumentUndoListener;
import org.electrocodeogram.cpc.core.CPCCorePlugin;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider;
import org.electrocodeogram.cpc.core.api.provider.store.StoreLockingException;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider.LockMode;
import org.electrocodeogram.cpc.core.api.provider.track.CPCPosition;
import org.electrocodeogram.cpc.core.api.provider.track.IPositionUpdateStrategyProvider;
import org.electrocodeogram.cpc.core.utils.CoreStringUtils;
import org.electrocodeogram.cpc.track.CPCTrackPlugin;
import org.electrocodeogram.cpc.track.repository.CloneRepository;
import org.electrocodeogram.cpc.track.repository.DocumentDescriptor;
import org.electrocodeogram.cpc.track.utils.TrackUtils;


public class CPCPositionUpdater implements IPositionUpdater, IDocumentListener, IDocumentUndoListener
{
	private static final Log log = LogFactory.getLog(CPCPositionUpdater.class);

	/**
	 * At which buffer size do we start to print warning messages?
	 */
	private static final int MAX_EVENT_BUFFER_SIZE = 50;
	private static final long BUFFER_SYNC_CHECK_INTERVALL = 500;

	private CloneRepository cloneRepository;
	private Object cloneRepositoryCPCPositionLockObject;
	private IStoreProvider storeProvider;
	private IPositionUpdateStrategyProvider positionUpdateStrategy;
	private DocumentDescriptor docDesc;

	private Queue<DocumentEvent> eventBuffer;
	private DocumentEvent lastEvent;
	/**
	 * A special {@link Timer} which executes the {@link EventBufferCleanupTimerTask} in
	 * specific intervalls. The timer is only initialised (and the task scheduled), if the
	 * <em>eventBuffer</em> contains events.
	 * The timer is always destroyed whenever all events have been successfully processed.
	 */
	private Timer bufferSynchronisationTimer;

	public CPCPositionUpdater(DocumentDescriptor docDesc)
	{
		if (log.isTraceEnabled())
			log.trace("CPCPositionUpdater() - docDesc: " + docDesc);

		this.docDesc = docDesc;
		this.cloneRepository = CPCTrackPlugin.getCloneRepository();
		this.storeProvider = cloneRepository.getStoreProvider();
		this.positionUpdateStrategy = (IPositionUpdateStrategyProvider) CPCCorePlugin.getProviderRegistry()
				.lookupProvider(IPositionUpdateStrategyProvider.class);

		this.cloneRepositoryCPCPositionLockObject = cloneRepository.getCPCPositionLockObject();

		this.eventBuffer = new LinkedList<DocumentEvent>();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.text.IPositionUpdater#update(org.eclipse.jface.text.DocumentEvent)
	 */
	@Override
	public void update(DocumentEvent event)
	{
		if (log.isTraceEnabled())
		{
			log.trace("update() - event: " + event);
			log.trace("OFFSET: " + event.getOffset() + ", LEN: " + event.getLength() + ", TEXT: "
					+ CoreStringUtils.quoteString(event.getText()));
		}

		log.trace("update() - acquiring documentRegistry lock");
		synchronized (cloneRepositoryCPCPositionLockObject)
		{
			log.trace("update() - got documentRegistry lock");

			if (docDesc.isStale())
			{
				/*
				 * Ok, now we've got a problem.
				 * documentAboutToBeChanged() failed to load the position data for this document
				 * because some other thread is currently holding an exclusive IStoreProvider lock.
				 * (see also comment in documentAboutToBeChanged())
				 * 
				 * We are thus NOT able to process this DocumentEvent at the current point in time.
				 * We'll need to "buffer" it and process it later.
				 */

				log.warn("update() - document position data is currently unavailable, going to queue event - event: "
						+ event + " (o: " + event.getOffset() + ", l: " + event.getLength() + ", t: "
						+ CoreStringUtils.quoteString(event.getText()) + ")");

				bufferEvent(event);
			}
			else
			{
				/*
				 * The position data for this Document is available.
				 * Continue normal processing.
				 */

				//make sure that any "buffered" events are processed first.
				if (!eventBuffer.isEmpty())
					processBufferedEvents(event.getDocument());

				//now process the latest event
				processEvent(event, event.getDocument());

				//make sure that there is no timer task hanging around
				if (bufferSynchronisationTimer != null)
				{
					log.trace("update() - canceling no longer needed buffer sync timer task.");

					bufferSynchronisationTimer.cancel();
					bufferSynchronisationTimer = null;
				}
			}

			lastEvent = event;

			log.trace("update() - releasing documentRegistry lock");
		}
	}

	/**
	 * Processes a given {@link DocumentEvent}.<br/>
	 * Any caller must hold a lock on <em>cloneRepositoryCPCPositionLockObject</em>.
	 * 
	 * @param event the event to process, never null.
	 * @param positionSourceDocument the document from which to obtain the position data.
	 * 		Usually this will equal the <code>event.getDocument()</code> value.
	 * 		However, in case of an event reply the <em>positionSourceDocument</em> will
	 * 		represent the latest document where as all but the last of the events to
	 * 		process will only contain a {@link DocumentProxy}.
	 */
	private void processEvent(DocumentEvent event, IDocument positionSourceDocument)
	{
		if (log.isTraceEnabled())
		{
			log.trace("processEvent() - event: " + event);
			log.trace("OFFSET: " + event.getOffset() + ", LEN: " + event.getLength() + ", TEXT: "
					+ CoreStringUtils.quoteString(event.getText()));
		}

		if (event.getDocument() instanceof IDocumentExtension4)
			log.trace("mod. time: " + ((IDocumentExtension4) positionSourceDocument).getModificationStamp());

		//get position data
		Position[] positions = null;
		try
		{
			positions = positionSourceDocument.getPositions(CPCPosition.CPC_POSITION_CATEGORY);
		}
		catch (BadPositionCategoryException e)
		{
			log.error("update() - unable to obtain positions - " + e, e);
		}

		if (log.isTraceEnabled())
			for (Position pos : positions)
				log.trace("PRE-POS: " + pos);

		boolean positionsUpdated = positionUpdateStrategy.updatePositions(event, positions);

		if (positionsUpdated)
		{
			//ok, mark the document dirty
			docDesc.setDirty(true);
			docDesc.setLastModification(System.currentTimeMillis());
		}

		if (log.isTraceEnabled())
			for (Position pos : positions)
				log.trace("POST-POS: " + pos);

		//keep track of last document event for auto-reformat-on-paste detection on PASTE events
		cloneRepository.setLastDocumentEvent(event);
	}

	/**
	 * This method may only be called if a lock on <em>cloneRepositoryCPCPositionLockObject</em>
	 * guarantees that no concurrent access is taking place.
	 */
	private void bufferEvent(DocumentEvent event)
	{
		if (log.isTraceEnabled())
			log.trace("bufferEvent() - event: " + event + ", currently " + eventBuffer.size() + " events in queue.");

		DocumentEvent newEvent = new DocumentEvent();

		//copy over primitive values
		newEvent.fOffset = event.fOffset;
		newEvent.fLength = event.fLength;
		newEvent.fText = event.fText;
		newEvent.fModificationStamp = event.fModificationStamp;

		//clone document
		/*
		 * TODO:/FIXME: check if there is any better way than always storing the entire document for each event.
		 * I.e. we could only store the first document content and then "replay" the buffered diff events
		 * 		to obtain the latest document value.
		 * 		It might also be interesting to look into ways of using the internal text store element of
		 * 		the original document.
		 */
		newEvent.fDocument = new DocumentProxy(event.getDocument().get());

		//queue event
		eventBuffer.add(newEvent);

		//make sure that there is a timer task which will handle this event if no further document
		//events come our way
		if (bufferSynchronisationTimer == null)
		{
			log.trace("bufferEvent() - no timer task pending, creating new one.");

			bufferSynchronisationTimer = new Timer();
			bufferSynchronisationTimer.schedule(new EventBufferCleanupTimerTask(), BUFFER_SYNC_CHECK_INTERVALL,
					BUFFER_SYNC_CHECK_INTERVALL);
		}

		if (eventBuffer.size() > MAX_EVENT_BUFFER_SIZE)
		{
			log.warn("bufferEvent() - very large event buffer queue - current size: " + eventBuffer.size()
					+ ", expected: <" + MAX_EVENT_BUFFER_SIZE, new Throwable());
		}
	}

	/**
	 * This method may only be called if a lock on <em>cloneRepositoryCPCPositionLockObject</em>
	 * guarantees that no concurrent access is taking place.
	 */
	private void processBufferedEvents(IDocument positionSourceDocument)
	{
		if (log.isTraceEnabled())
			log.trace("processBufferedEvents() - processing " + eventBuffer.size() + " buffered events.");

		while (!eventBuffer.isEmpty())
		{
			processEvent(eventBuffer.remove(), positionSourceDocument);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.text.IDocumentListener#documentAboutToBeChanged(org.eclipse.jface.text.DocumentEvent)
	 */
	@Override
	public void documentAboutToBeChanged(DocumentEvent event)
	{
		if (log.isTraceEnabled())
			log.trace("documentAboutToBeChanged() - event: " + event);

		/*
		 * The documents clone data may well be stale by now, we need to check for
		 * that possibility and if it is, we need to refresh the clone data from
		 * the store provider.
		 */
		//TODO: to be on the safe side we should acquire a lock for this entire block, however
		//		that would mean acquiring a lock for _every_ position update which might be too slow.
		/*
		 * Elsewhere we're now acquiring a lock on the document first, whenever we want to modify the document registry.
		 * This means that not locking this check should be safe, as we're already holding the lock on the document.
		 */
		if (docDesc.isStale())
		{
			log.trace("documentAboutToBeChanged() - document descriptor is stale, reloading position data.");

			/*
			 * We have a serious problem here.
			 * 
			 * We can't just try to acquire a blocking write lock as this might lead to a deadlock
			 * situation. The problem here is that we're already holding a lock on the current Document
			 * at this point and the locking order 1. Document lock, 2. IStoreProvider lock does not
			 * match the typical locking order of other users of the IStoreProvider.
			 * 
			 * This issue can not be fixed in the IStoreProvider or its usage as another user may have no
			 * notion of Documents and the only approach might be to lock _all_ open documents. But even
			 * then it would be hard to ensure that all locks are obtained in the right order.
			 * Especially because Document locks are the domain of Eclipse and may be acquired in all
			 * sorts of places.
			 * 
			 * The "solution" for this problem is to make use of a non-blocking method to acquire the lock
			 * and to "buffer" all events which can not be handled due to an IStoreProvider lock being in
			 * place.
			 * 
			 * This has several implications. The main issue here is that it is not enough to just cache
			 * the document events themselves. During processing of document events an
			 * IPositionUpdateStrategyProvider will need access to the underlying IDocument
			 * _at the time of the edit event_.
			 * This means that we'll have to store the current Document content with each event.
			 * And that we may thus use a lot of memory if any other threads holds an exclusive IStoreProvider
			 * lock for a long time.
			 * 
			 * The actual "buffering" is done in update() if the document descriptor is stale.
			 */
			reloadPositionData();
		}

	}

	/**
	 * Tries to acquire an exclusive write lock on the {@link IStoreProvider} and to reload
	 * all {@link CPCPosition} data for the current document.<br/>
	 * Does nothing if the store provider lock can not be acquired within a reasonably short
	 * amount of time (currently: 250ms).
	 * 
	 * @return true if the position data was successfully reloaded, false otherwise.
	 */
	private boolean reloadPositionData()
	{
		log.trace("reloadPositionData()");

		boolean gotLock = false;
		try
		{
			gotLock = storeProvider.acquireWriteLockNonBlocking(LockMode.NO_WRITE_LOCK_HOOK_NOTIFY, 250);

			if (gotLock)
			{
				CPCTrackPlugin.getCloneRepository().loadPositionData(docDesc);

				log.trace("reloadPositionData() - success.");

				return true;
			}
		}
		catch (StoreLockingException e)
		{
			log.error("loadPositionData() - locking error - " + e, e);
		}
		catch (InterruptedException e)
		{
			log.warn("loadPositionData() - interrupted while waiting for IStoreProvider lock.", e);
		}
		finally
		{
			if (gotLock)
				storeProvider.releaseWriteLock();
		}

		log.debug("reloadPositionData() - FAILED, unable to obtain exclusive store provider lock.");

		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.text.IDocumentListener#documentChanged(org.eclipse.jface.text.DocumentEvent)
	 */
	@Override
	public void documentChanged(DocumentEvent event)
	{
		if (log.isTraceEnabled())
			log.trace("documentChanged() - event: " + event);

		//not used
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.text.undo.IDocumentUndoListener#documentUndoNotification(org.eclipse.text.undo.DocumentUndoEvent)
	 */
	@Override
	public void documentUndoNotification(DocumentUndoEvent event)
	{
		if (log.isTraceEnabled())
			log.trace("documentUndoNotification() - event: " + event);

		log.trace("type: " + event.getEventType() + " - "
				+ (((event.getEventType() & DocumentUndoEvent.ABOUT_TO_UNDO) != 0) ? "PRE-UNDO" : "")
				+ (((event.getEventType() & DocumentUndoEvent.ABOUT_TO_REDO) != 0) ? "PRE-REDO" : "")
				+ (((event.getEventType() & DocumentUndoEvent.UNDONE) != 0) ? "POST-UNDO" : "")
				+ (((event.getEventType() & DocumentUndoEvent.REDONE) != 0) ? "POST-REDO" : ""));
		log.trace("offset: " + event.getOffset());
		log.trace("text: " + CoreStringUtils.quoteString(event.getText()));
		log.trace("preserved text: " + CoreStringUtils.quoteString(event.getPreservedText()));
		log.trace("source: " + event.getSource());
		log.trace("compound: " + event.isCompound());
		if (event.getDocument() instanceof IDocumentExtension4)
			log.trace("mod. time: " + ((IDocumentExtension4) event.getDocument()).getModificationStamp());

		if ((event.getEventType() & DocumentUndoEvent.REDONE) != 0)
			log.debug("x");
	}

	/**
	 * Checks whether there are any buffered events for this document and tries to update the
	 * position data from the store provider and to process all buffered events, if possible.<br/>
	 * <br/>
	 * NOTE: The caller needs to hold a lock on the document and the documentRegistry before calling
	 * this method.<br/>
	 * <br/>
	 * NOTE: This method will try to acquire an exclusive store provider lock and will wait for a short
	 * amount of time for such a lock to be granted. The action is aborted and false is returned if
	 * an exclusive lock can not be obtained. 
	 * 
	 * @return true if either no events were queued or if they were all processed successfully, false
	 * 		if processing failed (due to failure of acquiring a store provider lock).
	 */
	public boolean refreshPositionsAndProcessBufferedEvents()
	{
		log.trace("refreshPositionsAndProcessBufferedEvents()");

		//now check buffer
		if (!eventBuffer.isEmpty())
		{
			if (log.isTraceEnabled())
				log.trace("refreshPositionsAndProcessBufferedEvents() - event buffer contains " + eventBuffer.size()
						+ " events.");

			//there should always be a lastEvent entry, if events are in buffer
			if (lastEvent != null)
			{
				//and the document should always be stale
				if (docDesc.isStale())
				{
					//reload the position data

					/*
					 * NOTE: We're acquiring the IStoreProvider exclusive lock here _AFTER_ acquiring
					 * the documentRegistry lock. This is the WRONG locking order.
					 * 
					 * However, as we're using the non-blocking locking method in reloadPositionData(),
					 * this will not lead to a deadlock situation. This timer task will just log a
					 * warning and start anew, if it can't get the store provider lock.
					 * Thus the documentRegistry lock is always relinquished between locking attempts
					 * on the store provider.
					 */

					if (reloadPositionData())
					{
						//ok, all position data should now be up to date

						//process the events 
						processBufferedEvents(lastEvent.getDocument());
						lastEvent = null;

						//ok, our work is done, cancel the timer.
						bufferSynchronisationTimer.cancel();
						bufferSynchronisationTimer = null;

						return true;
					}
					else
					{
						//We didn't get an IStoreProvider lock, lets retry it on the next run of this
						//timer task. There is no need to reschedule the task here, as it will be
						//repeated until we cancel the timer.
						log
								.warn("refreshPositionsAndProcessBufferedEvents() - unable to refresh positions, trying again later.");
					}
				}
				else
				{
					log.error(
							"refreshPositionsAndProcessBufferedEvents() - document desriptor is not stale! - docDesc: "
									+ docDesc, new Throwable());
				}
			}
			else
			{
				log
						.error(
								"refreshPositionsAndProcessBufferedEvents() - lastEvent is null during processing of eventBuffer.",
								new Throwable());
			}

			//buffer contains events but nothing was processed
			return false;
		}
		else
		{
			//buffer was empty
			log.trace("refreshPositionsAndProcessBufferedEvents() - event buffer is empty.");
			return true;
		}
	}

	private class EventBufferCleanupTimerTask extends TimerTask
	{
		public EventBufferCleanupTimerTask()
		{
			log.trace("EventBufferCleanupTimerTask()");
		}

		/*
		 * (non-Javadoc)
		 * @see java.util.TimerTask#run()
		 */
		@Override
		public void run()
		{
			//get a lock on the document
			log.trace("EventBufferCleanupTimerTask.run() - acquiring document lock");
			synchronized (TrackUtils.getDocumentLockObject(docDesc))
			{
				log.trace("EventBufferCleanupTimerTask.run() - got document lock");

				//get a lock on the document registry
				log.trace("EventBufferCleanupTimerTask.run() - acquiring documentRegistry lock");
				synchronized (cloneRepositoryCPCPositionLockObject)
				{
					log.trace("EventBufferCleanupTimerTask.run() - got documentRegistry lock");

					refreshPositionsAndProcessBufferedEvents();

					log.trace("EventBufferCleanupTimerTask.run() - releasing documentRegistry lock");
				}

				log.trace("EventBufferCleanupTimerTask.run() - releasing document lock");
			}
		}

	}
}

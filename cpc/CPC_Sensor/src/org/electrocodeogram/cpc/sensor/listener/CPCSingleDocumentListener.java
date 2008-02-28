package org.electrocodeogram.cpc.sensor.listener;


import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.swt.widgets.Display;
import org.electrocodeogram.cpc.core.CPCCorePlugin;
import org.electrocodeogram.cpc.core.api.hub.event.EclipseCodeDiffEvent;
import org.electrocodeogram.cpc.core.utils.CoreUtils;
import org.electrocodeogram.cpc.sensor.CPCSensorPlugin;


/**
 * Very similar to the ECGDocumentListener of the ECG Eclipse Sensor.
 * 
 * @author vw
 * @deprecated this class is no longer in use
 */
@Deprecated
public class CPCSingleDocumentListener implements IDocumentListener
{
	private static Log log = LogFactory.getLog(CPCSingleDocumentListener.class);

	//private static Map<ITextEditor, CPCSingleDocumentListener> docListenerCache = new HashMap<ITextEditor, CPCSingleDocumentListener>();

	//private ITextEditor textEditor;
	private boolean registered = false;
	private String projectName;
	private String filePath;

	private Timer diffTimer = null;

	/**
	 * Flag set to true if a document change has been detected.
	 */
	public boolean documentChanged = false;

	private DiffChangeTimerTask currentDiffTimerTask = null;

	/**
	 * Keeps track of continuous edit events. Needed in order to prevent event flooding.
	 */
	private DiffRange diffRange = null;

	/**
	 * Caches the content of the document at the last event. This is needed in order to
	 * be able to provide removed content in events.
	 */
	private String lastDocumentContent = null;

	/*
	public synchronized static IDocumentListener getDocumentListenerForEditor(ITextEditor textEditor)
	{
		if (log.isTraceEnabled())
			log.trace("getDocumentListenerForEditor() - editor: " + textEditor);

		CPCSingleDocumentListener listener = docListenerCache.get(textEditor);

		if (listener == null)
		{
			//we haven't seen this editor before, create a new listener instance
			log.trace("getDocumentListenerForEditor(): creating new document listener");
			listener = new CPCSingleDocumentListener(textEditor);

			//store it
			docListenerCache.put(textEditor, listener);
		}

		if (log.isTraceEnabled())
			log.trace("getDocumentListenerForEditor() - result: " + listener);

		return listener;
	}

	public synchronized static void removeDocumentListenersForEditor(ITextEditor textEditor)
	{
		if (log.isTraceEnabled())
			log.trace("removeDocumentListenersForEditor(): " + textEditor);

		docListenerCache.remove(textEditor);
	}
	*/

	@Deprecated
	public CPCSingleDocumentListener(String location)
	{
		if (log.isTraceEnabled())
			log.trace("ECGSingleDocumentListener() - location: " + location);

		//These values are not yet available, we'll have to obtain them later during documentChanged() calls.
		/*
		this.registered = false;
		this.projectName = null;
		this.filePath = null;
		*/
		this.projectName = CoreUtils.getProjectnameFromLocation(location);
		this.filePath = CoreUtils.getProjectRelativePathFromLocation(location);
		this.registered = true;

		//we need to register ourself
		CPCDocumentListenerRegistry.registerDocumentListenerForLocation(location, this);

		this.diffTimer = new Timer();
	}

	/*
	private CPCSingleDocumentListener(ITextEditor textEditor)
	{
		if (log.isTraceEnabled())
			log.trace("ECGSingleDocumentListener() - textEditor: " + textEditor);

		//this.textEditor = textEditor;
		this.projectName = CoreUtils.getProjectnameFromPart(textEditor);
		this.filePath = CoreUtils.getFilenameFromPart(textEditor);

		this.diffTimer = new Timer();
	}
	*/

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.text.IDocumentListener#documentAboutToBeChanged(org.eclipse.jface.text.DocumentEvent)
	 */
	public synchronized void documentAboutToBeChanged(DocumentEvent event)
	{
		if (log.isTraceEnabled())
			log.trace("documentAboutToBeChanged(): " + event);

		//if we were not yet able to initialise the project and file strings
		/*
		if (!this.registered)
		{
			//check if we can already get a buffer reference for this document
			ITextFileBuffer buffer = FileBuffers.getTextFileBufferManager().getTextFileBuffer(event.getDocument());
			if (buffer != null)
			{
				String location = buffer.getLocation().toString();

				//yep, we've got the buffer!
				if (log.isTraceEnabled())
					log.trace("documentAboutToBeChanged() - got file buffer: " + buffer + ", location:  " + location);

				//now update project and file data
				this.projectName = CoreUtils.getProjectnameFromLocation(location);
				this.filePath = CoreUtils.getFilenameFromLocation(location);
				this.registered = true;

				//we need to register ourself
				CPCDocumentListenerRegistry.registerDocumentListenerForLocation(location, this);
			}
			else
				log.trace("documentAboutToBeChanged() - still missing a file buffer");
		}
		*/

		//keep a copy of the unmodified document content
		//TODO: check if this has performance implications
		this.lastDocumentContent = event.getDocument().get();

	}

	public synchronized void documentChanged(DocumentEvent event)
	{
		if (log.isTraceEnabled())
			log.trace("documentChanged(): " + event);

		if (event == null)
		{
			log.trace("documentChanged(): NULL - ignoring event.");
			return;
		}

		/*
		 * This document listener is registered by the document setup participant at a very early stage.
		 * This means that we will receive change events which are related to the initial creation of
		 * the document too. I.e. the document will be empty at first and we will see one change event
		 * which sets the entire content.
		 * 
		 * It seems as if that one initial change event which sets the entire file content always
		 * occurs while the filebuffer is still not available. Which means that all events which
		 * occur while projectName and filePath are null can probably be ignored.
		 * 
		 * FIXME: this behaviour might change in future eclipse versions
		 */
		/*
		if (this.projectName == null || this.filePath == null)
		{
			if (log.isDebugEnabled())
				log.debug("documentChanged() - ignoring event while filebuffer is not yet available - event: " + event);

			//some debug checking, if the assumption above holds then this event should always start at offset 0 and have length 0
			if (event.getOffset() != 0 || event.getLength() != 0)
				log.warn("documentChanged() - filebuffer is not yet available but event seems to be real! - event: "
						+ event + ", offset: " + event.getOffset() + ", len: " + event.getLength() + ", text: "
						+ event.getText());

			return;
		}
		*/

		this.documentChanged = true;

		collateDiffEvent(event);
		rescheduleDiffTimer();

		/*
		if (logger.isTraceEnabled())
		{
			//debug dump of position data for document
			for (String posCat : event.getDocument().getPositionCategories())
			{
				logger.trace("position category: "+posCat);
				try
				{
					for (Position pos : event.getDocument().getPositions(posCat))
						logger.trace("   position: "+pos);
				}
				catch (BadPositionCategoryException e)
				{
					e.printStackTrace();
				}
			}
		}
		*/
	}

	/**
	 * Immediately sends out a code diff events, if one is pending.
	 * Called by the text operation listener. 
	 * 
	 * @see CPCTextOperationAction
	 */
	synchronized void fireDiffTimer()
	{
		//TODO: what about concurrency? we might be executing the run method twice
		//		if the timer triggers before we call cancel(). However, the internal
		//		implementation of the run() method currently does not do anything
		//		on the second run if run twice, so this is not a real problem
		if (this.currentDiffTimerTask != null && this.diffTimer != null && this.diffRange != null)
		{
			this.currentDiffTimerTask.run();
			this.diffTimer.cancel();
			this.currentDiffTimerTask = null;
		}
	}

	/**
	 * Resets the timer which will send code diff events.
	 * 
	 * @see ECGEclipseSensor#DIFFCHANGE_INTERVAL
	 */
	private synchronized void rescheduleDiffTimer()
	{
		if (this.diffTimer != null)
		{
			this.diffTimer.cancel();
			this.diffTimer = new Timer();
			// TODO: Is it a good idea to create a  new timer every now and then (= on each key stroke!)?
			this.currentDiffTimerTask = new DiffChangeTimerTask();
			this.diffTimer.schedule(this.currentDiffTimerTask, CPCSensorPlugin.DIFFCHANGE_INTERVAL);
		}
	}

	/**
	 * Try to collate continuous document changes into one change event.
	 * I.e. if the user types a line, each typed character will fire an event, but only the complete line
	 * is meaningful.<br/>
	 * An event is always fired immediately if any data is deleted/overwritten. Only pure
	 * add events are collated.
	 * 
	 * @param event the event to collate
	 */
	private synchronized void collateDiffEvent(DocumentEvent event)
	{
		if (log.isTraceEnabled())
			log.trace("collateDiffEvent() - event: " + event + ", len: " + event.getLength() + ", offset: "
					+ event.getOffset() + ", text: " + event.getText() + ", document: " + event.getDocument());

		//check if anything was deleted or overwritten
		if (event.getLength() != 0)
		{
			//ok, something was removed. => send this event immediately

			//send old cached event first
			sendDiffEvent();

			EclipseCodeDiffEvent newEvent = new EclipseCodeDiffEvent(CoreUtils.getUsername(), this.projectName);

			newEvent.setFilePath(this.filePath);
			newEvent.setOffset(event.getOffset());

			//the newly added text
			newEvent.setAddedText(event.getText());

			//add the new content to the event
			newEvent.setEditorContent(event.getDocument().get());

			if (this.lastDocumentContent != null)
			{
				try
				{
					//the replaced text
					newEvent.setReplacedText(this.lastDocumentContent.substring(event.getOffset(), event.getOffset()
							+ event.getLength()));
				}
				catch (IndexOutOfBoundsException e)
				{
					log.error(
							"ERROR: collateDiffEvent() - last document state does not match size expectations for event: "
									+ event + " - " + e + " - lastDocumentContent: " + lastDocumentContent, e);
					newEvent.setReplacedText("");
				}
			}
			else
			{
				log.error("ERROR: collateDiffEvent() - last document state not available for event: " + event,
						new Throwable());
				newEvent.setReplacedText("");
			}

			CPCCorePlugin.getEventHubRegistry().dispatch(newEvent);
		}
		else
		{
			//check if this event is a direct continuation of the last change
			if ((diffRange != null) && (diffRange.isContinuation(event.getOffset())))
			{
				//ok, we're continuing the existing diff collation
				diffRange.add(event);
			}
			else
			{
				//nope, we need to send the old event and start a new one
				sendDiffEvent();
				diffRange = new DiffRange(event);
			}
		}

		//TODO: set timer for timed autosend of cached data
	}

	/**
	 * Takes the current collated diff data and sends it as a new micro activity event.
	 * Does nothing if no collated diff data is pending.
	 */
	synchronized void sendDiffEvent()
	{
		//only if diff data is pending
		if (diffRange == null)
			return;

		EclipseCodeDiffEvent newEvent = new EclipseCodeDiffEvent(CoreUtils.getUsername(), this.projectName);

		newEvent.setFilePath(this.filePath);
		newEvent.setOffset(diffRange.getStartOffset());
		newEvent.setAddedText(diffRange.getContent());
		newEvent.setReplacedText("");
		newEvent.setEditorContent(diffRange.getEditorContent());

		CPCCorePlugin.getEventHubRegistry().dispatch(newEvent);

		diffRange = null;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "CPCSingleDocumentListener[project: " + projectName + ", file: " + filePath + "]";
	}

	/**
	 * Internal class to store intermediate data on code change events.
	 * Events which represent a continuous editing process are collated together using this class.
	 * 
	 * @author vw
	 */
	private class DiffRange
	{
		private int startOffset;
		private int lineNumber;
		private int offsetInLine;
		private String content;

		/**
		 * We'll need to keep track of the editor content AFTER the change as this will be needed
		 * for sequential processing of events in situations where the followup event has already
		 * modified the editor content but some subscriber of diff events needs to have access to
		 * the editor content in the state it was in directly after this diff event. 
		 */
		private String editorContent;

		/**
		 * Initialises a new DiffRange with the data from the given event.
		 * 
		 * @param event the initial event, never null
		 */
		DiffRange(DocumentEvent event)
		{
			assert (event != null && event.getLength() == 0);

			this.startOffset = event.getOffset();

			try
			{
				this.lineNumber = event.getDocument().getLineOfOffset(event.getOffset());
				this.offsetInLine = event.getOffset()
						- event.getDocument().getLineInformationOfOffset(event.getOffset()).getOffset();
			}
			catch (BadLocationException e)
			{
				log.error("ERROR: DiffRange() - unable to translate offset to line number: " + event + " - " + e, e);
			}

			if (event.getText() != null)
			{
				this.content = event.getText();
			}
			else
			{
				this.content = "";
			}

			//we need to remember the current editor content (AFTER) this event
			this.editorContent = event.getDocument().get();
		}

		/**
		 * If the given offset fulfils one of the following criteria the corresponding
		 * event is considered to be a continuation of the prior edit.
		 * <ul>
		 * 	<li>the offset lies within the range of this diff</li>
		 * 	<li>the offset lies exactly at the end of this diff</li>
		 * 	<li>the offset lies directly in fron of this diff</li>
		 * </ul>
		 * 
		 * @param offset
		 * @return
		 */
		boolean isContinuation(int offset)
		{
			if (log.isTraceEnabled())
				log.trace("isContinuation(): " + offset);

			if (offset < this.startOffset)
			{
				//too far in front
				log.trace("isContinuation() - result: false");
				return false;
			}
			else if (offset > this.startOffset + this.content.length())
			{
				//too far behind
				log.trace("isContinuation() - result: false");
				return false;
			}
			else
			{
				//ok, it's within range
				log.trace("isContinuation() - result: true");
				return true;
			}
		}

		boolean add(DocumentEvent event)
		{
			if (log.isTraceEnabled())
				log.trace("add(): " + event + " FOR " + this);

			if (!isContinuation(event.getOffset()))
			{
				log.trace("add() - not a continuation, ignored");
				return false;
			}

			int pos = event.getOffset() - startOffset;
			if (log.isTraceEnabled())
				log.trace("add() - pos: " + pos);

			//store new content
			if (pos <= 0)
			{
				//add on the left
				content = event.getText() + content;
				startOffset = event.getOffset();
			}
			else if (pos >= content.length())
			{
				//add on the right
				content = content + event.getText();
			}
			else
			{
				//add inbetween
				content = content.substring(0, pos) + event.getText() + content.substring(pos);
			}

			//update editor content
			editorContent = event.getDocument().get();

			if (log.isTraceEnabled())
				log.trace("add() - resulting data: " + this);

			return true;
		}

		int getStartOffset()
		{
			return startOffset;
		}

		int getLineNumber()
		{
			return lineNumber;
		}

		int getOffsetInLine()
		{
			return offsetInLine;
		}

		String getContent()
		{
			return content;
		}

		String getEditorContent()
		{
			return editorContent;
		}

		@Override
		public String toString()
		{
			return "DiffRange[off: " + startOffset + ", lineNumber: " + lineNumber + ", offsetInLine: " + offsetInLine
					+ ", content: " + content + "]";
		}
	}

	class DiffChangeTimerTask extends TimerTask
	{
		/**
		 * This creates the <em>TimerTask</em>.
		 * @param document
		 *            Is the document that has been changed
		 * @param documentName
		 *            Is the name of the document
		 */
		public DiffChangeTimerTask()
		{
			if (log.isTraceEnabled())
				log.trace("DiffChangeTimerTask()");
		}

		/**
		 * @see java.util.TimerTask#run()
		 */
		@Override
		public void run()
		{
			log.trace("DiffChangeTimerTask.run()");

			//to prevent concurrency problems, we ensure that we always send the diff events from
			//the same thread as diff events triggered directly by manual actions
			Display.getDefault().syncExec(new Runnable()
			{
				public void run()
				{
					sendDiffEvent();
				}
			});
		}
	}

}

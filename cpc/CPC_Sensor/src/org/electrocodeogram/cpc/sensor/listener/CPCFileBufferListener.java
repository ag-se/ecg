package org.electrocodeogram.cpc.sensor.listener;


import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.filebuffers.IFileBuffer;
import org.eclipse.core.filebuffers.IFileBufferListener;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension4;
import org.eclipse.ui.texteditor.ITextEditor;
import org.electrocodeogram.cpc.core.CPCCorePlugin;
import org.electrocodeogram.cpc.core.api.hub.event.EclipseFileAccessEvent;
import org.electrocodeogram.cpc.core.api.hub.event.EclipseResourcePersistenceEvent;
import org.electrocodeogram.cpc.core.utils.CoreConfigurationUtils;
import org.electrocodeogram.cpc.core.utils.CoreFileUtils;
import org.electrocodeogram.cpc.core.utils.CoreUtils;
import org.electrocodeogram.cpc.sensor.CPCSensorPlugin;


/**
 * FileBuffers are internal Caches for the actual typed contents of a displayed
 * (text) file. On Save, the underlying file is overwritten by the buffers contents.
 * This is a good place to recognise dirty events and also changes on the
 * underlying file.
 *
 * Taken from the ECG Eclipse Sensor.
 * 
 * @author vw and others
 */
public class CPCFileBufferListener implements IFileBufferListener
{
	private static final Log log = LogFactory.getLog(CPCFileBufferListener.class);

	/*
	 * FIXME: we have one single file buffer object which is notified about all
	 * file buffer operations. These cached values could therefore
	 * end up containing the values for a different document, if two calls
	 * to this listener are executed concurrently.
	 * (does that ever happen?)
	 */
	private long fLastModificationStamp = -1;

	private boolean fBufferWasReplaced = false;

	/**
	 * Keeps a list of all currently open buffers in which we're interested in.
	 * <br>
	 * This is mainly needed to correctly identify deleted files which are of interest.
	 */
	private Set<String> openCpcBuffers;

	public CPCFileBufferListener()
	{
		log.trace("CPCFileBufferListener()");

		openCpcBuffers = Collections.synchronizedSet(new HashSet<String>(20));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.filebuffers.IFileBufferListener#bufferContentAboutToBeReplaced(org.eclipse.core.filebuffers.IFileBuffer)
	 */
	public void bufferContentAboutToBeReplaced(IFileBuffer buffer)
	{
		// not used

		if (log.isTraceEnabled())
			log.trace("bufferContentAboutToBeReplaced(): " + buffer + " - " + buffer.getLocation());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.filebuffers.IFileBufferListener#bufferContentReplaced(org.eclipse.core.filebuffers.IFileBuffer)
	 */
	public void bufferContentReplaced(IFileBuffer buffer)
	{
		fBufferWasReplaced = true;

		if (log.isTraceEnabled())
			log.trace("bufferContentReplaced(): " + buffer + " - " + buffer.getLocation());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.filebuffers.IFileBufferListener#bufferCreated(org.eclipse.core.filebuffers.IFileBuffer)
	 */
	// is nix besser als part opened
	public void bufferCreated(IFileBuffer buffer)
	{
		fBufferWasReplaced = false;

		if (log.isTraceEnabled())
			log.trace("bufferCreated(): " + buffer + " - " + buffer.getLocation() + " (currently "
					+ openCpcBuffers.size() + " open buffers)");

		//make sure we're interested in this type of file
		if (!CoreConfigurationUtils.isSupportedFile(buffer.getLocation()))
		{
			log.trace("bufferCreated() - unsupported file type, ignoring.");
			return;
		}

		//if this file is a text file
		if (buffer instanceof ITextFileBuffer)
		{
			//NOTE: this will not work correctly for editors which are automatically opened at eclipse startup
			//		as our file buffer listener is not yet registered and we're therefore missing some
			//		file open events x_X
			//		We therefore artificially generate file access events via initiallyOpenDocuments()

			/*
			 * We might have a problem here.
			 * If the workspace is out of sync with the actual files in the filesystem, a buffer
			 * may contain an empty document, even though the file does have contents on the filesystem.
			 * Lets try to detect this condition here.
			 */
			if (!buffer.isSynchronized() || !buffer.getStatus().isOK())
			{
				log.warn("bufferCreated() - buffer potentially out of sync, ignoring - location: "
						+ buffer.getLocation() + ", inSync: " + buffer.isSynchronized() + ", status: "
						+ buffer.getStatus());
				/*
				 * So what do we do now?
				 * One situation were this might happen is when eclipse is restarted after a crash.
				 * Is just ignoring the event enough?
				 * What will happen once the editor notices that the file needs to be reloaded?
				 * And is the copy from the filesystem still in sync with our persisted clone data?
				 * 
				 * If the editor detects the out-of-sync situation it will display a reload dialog to
				 * the user, who can then decide whether to refresh the resource. If the user selects
				 * no nothing happens and the editor remains unusable.
				 * If the user selects yes a REVERT will be detected, NO NEW buffer will be created!
				 * By suppressing the buffer creation event we'll end up in a situation where a fully
				 * functional editor is present, but documentInit() in CPC Track was never called.
				 * Fix for now is to listen to REVERT events and to call documentInit() if a revert happens
				 * for a file which does not yet have a documentRegistry entry.
				 * see: CloneRepository
				 * 
				 * TODO: reconsider this
				 */

				return;
			}

			//make sure this file is located within the workspace, we're not interested in external files.
			if (CoreFileUtils.isFileLocatedInWorkspace(buffer.getLocation()))
			{
				//remember this buffer as being of interest
				openCpcBuffers.add(buffer.getLocation().toString());
				if (openCpcBuffers.size() > 100)
				{
					log.warn("bufferCreated() - " + openCpcBuffers.size()
							+ " open cpc file buffers, potential buffer leak?", new Throwable());
				}

				//notify any interested parties about the fact that a text file was just opened
				EclipseFileAccessEvent newEvent = new EclipseFileAccessEvent(CoreUtils.getUsername(), CoreUtils
						.getProjectnameFromLocation(buffer.getLocation().toString()));
				newEvent.setType(EclipseFileAccessEvent.Type.OPENED);
				newEvent.setFilePath(CoreUtils.getProjectRelativePathFromLocation(buffer.getLocation().toString()));
				newEvent.setDocument(((ITextFileBuffer) buffer).getDocument());
				CPCCorePlugin.getEventHubRegistry().dispatch(newEvent);
			}
			else
			{
				if (log.isDebugEnabled())
					log.debug("bufferCreated() - not located within workspace, ignoring file: "
							+ buffer.getLocation().toString());
			}

			//TODO: move to CPC Track
			//then we need to register the CPC document listener to handle clone updates
			/*
			((ITextFileBuffer) buffer).getDocument().addDocumentListener(
					new CPCSingleDocumentListener(buffer.getLocation().toString()));
					*/
		}
		else
		{
			log.warn("bufferCreated() - buffer is not an ITextFileBuffer - buffer: " + buffer);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.filebuffers.IFileBufferListener#bufferDisposed(org.eclipse.core.filebuffers.IFileBuffer)
	 */
	public void bufferDisposed(IFileBuffer buffer)
	{
		fBufferWasReplaced = false;

		if (log.isTraceEnabled())
			log.trace("bufferDisposed(): " + buffer + " - " + buffer.getLocation());

		//make sure we're interested in this type of file
		if (!CoreConfigurationUtils.isSupportedFile(buffer.getLocation()))
		{
			log.trace("bufferDisposed() - unsupported file type, ignoring.");
			return;
		}

		//if this file is a text file
		if (buffer instanceof ITextFileBuffer)
		{
			//make sure this file is located within the workspace, we're not interested in external files.
			//TODO: / FIXME: this will also prevent CLOSED events for files which were just deleted/renamed
			//if (CoreFileUtils.isFileLocatedInWorkspace(buffer.getLocation()))

			/*
			 * If we generated an event at the time when this buffer was created, we should also generate
			 * an event now.
			 * To be on the save side, we also create events for a file which still exists and is located
			 * within the workspace.
			 */
			boolean fileIsOfInterest = openCpcBuffers.remove(buffer.getLocation().toString());
			if (fileIsOfInterest || CoreFileUtils.isFileLocatedInWorkspace(buffer.getLocation()))
			{

				//notify any interested parties about the fact that a text file was just closed
				EclipseFileAccessEvent newEvent = new EclipseFileAccessEvent(CoreUtils.getUsername(), CoreUtils
						.getProjectnameFromLocation(buffer.getLocation().toString()));
				newEvent.setType(EclipseFileAccessEvent.Type.CLOSED);
				newEvent.setFilePath(CoreUtils.getProjectRelativePathFromLocation(buffer.getLocation().toString()));
				newEvent.setDirty(buffer.isDirty());
				newEvent.setDocument(((ITextFileBuffer) buffer).getDocument());
				CPCCorePlugin.getEventHubRegistry().dispatch(newEvent);
			}
			else
			{
				if (log.isDebugEnabled())
					log.debug("bufferDisposed() - not located within workspace, ignoring file: "
							+ buffer.getLocation().toString());
			}

			//TODO: move to CPC Track
			//dispose any document listener which might be registered for this location
			//CPCDocumentListenerRegistry.unregisterDocumentListenerForLocation(buffer.getLocation().toString());
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.filebuffers.IFileBufferListener#stateChangeFailed(org.eclipse.core.filebuffers.IFileBuffer)
	 */
	public void stateChangeFailed(IFileBuffer buffer)
	{
		// not used

		if (log.isTraceEnabled())
			log.trace("stateChangeFailed(): " + buffer + " - " + buffer.getLocation());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.filebuffers.IFileBufferListener#stateChanging(org.eclipse.core.filebuffers.IFileBuffer)
	 */
	public void stateChanging(IFileBuffer buffer)
	{
		//keep track of the modification time stamp of the file before the change
		fLastModificationStamp = buffer.getModificationStamp();

		if (log.isTraceEnabled())
			log.trace("stateChanging(): " + buffer + " - " + buffer.getLocation() + " - modificationStamp: "
					+ fLastModificationStamp);

		//make sure we're interested in this type of file
		if (!CoreConfigurationUtils.isSupportedFile(buffer.getLocation()))
		{
			log.trace("stateChanging() - unsupported file type, ignoring.");
			return;
		}

		if (fLastModificationStamp == IDocumentExtension4.UNKNOWN_MODIFICATION_STAMP)
		{
			/*
			 * FIXME: this can indeed fail!
			 * 
			 * 2007-09-19 15:02:40 TRACE [main] CPCFileBufferListener:106 - stateChanging(): org.eclipse.core.internal.filebuffers.ResourceTextFileBuffer@19b7c62 - /Test/src/bla/BlaXYZ2.java - modificationStamp: -1
			 * 2007-09-19 15:02:40 ERROR [main] CPCFileBufferListener:110 - stateChanging() - unable to retrieve modification time for file: /Test/src/bla/BlaXYZ2.java
			 * 
			 * I.e. this might fail if a file was just deleted. 
			 */
			IResource fileHandle = ResourcesPlugin.getWorkspace().getRoot().findMember(buffer.getLocation());
			if (fileHandle == null || !fileHandle.exists())
			{
				//file deletions/moves are expected
				log.warn("stateChanging() - unable to retrieve modification time for file, file was moved or deleted: "
						+ buffer.getLocation());
			}
			else
			{
				//this shouldn't happen?
				log.error("stateChanging() - unable to retrieve modification time for file: " + buffer.getLocation(),
						new Throwable());
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.filebuffers.IFileBufferListener#stateValidationChanged(org.eclipse.core.filebuffers.IFileBuffer, boolean)
	 */
	public void stateValidationChanged(IFileBuffer buffer, boolean isStateValidated)
	{
		// not used

		if (log.isTraceEnabled())
			log.trace("stateValidationChanged(): " + buffer + " - " + buffer.getLocation());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.filebuffers.IFileBufferListener#dirtyStateChanged(org.eclipse.core.filebuffers.IFileBuffer, boolean)
	 */
	public void dirtyStateChanged(IFileBuffer buffer, boolean isDirty)
	{
		if (log.isTraceEnabled())
			log.trace("dirtyStateChanged() - buffer: " + buffer + ", isDirty: " + isDirty + " - location: "
					+ buffer.getLocation());

		//make sure we're interested in this type of file
		if (!CoreConfigurationUtils.isSupportedFile(buffer.getLocation()))
		{
			log.trace("dirtyStateChanged() - unsupported file type, ignoring.");
			return;
		}

		if (buffer.getLocation() == null)
		{
			//we can't handle a buffer for which we don't have a local file
			if (log.isDebugEnabled())
				log.debug("dirtyStateChanged() - ignoring non-local file for buffer: " + buffer);
			return;
		}

		//make sure this file is located within the workspace, we're not interested in external files.
		if (!(CoreFileUtils.isFileLocatedInWorkspace(buffer.getLocation())))
		{
			if (log.isDebugEnabled())
				log.debug("dirtyStateChanged() - not located within workspace, ignoring file: "
						+ buffer.getLocation().toString());
			return;
		}

		if (!isDirty)
		{
			if (log.isTraceEnabled())
				log.trace("dirtyStateChanged(): old modificationStamp: " + fLastModificationStamp
						+ " - new modificationStamp: " + buffer.getModificationStamp());

			/*
			 * TODO: this can also happen if the workspace was out of sync at the time of eclipse startup.
			 * Eclipse would then refresh the worspace and this can lead to this situation.
			 * The store provider will complain later that we're trying to persist clone data for a file
			 * for which no clone data was loaded.
			 */

			/*
			 * Undoing all changes till the original state of the file is reached again will
			 * also change the state to non-dirty. However, that is not a real revert action.
			 * We therefore filter out this situation and ignore it.
			 * 
			 * We expect to see a call to bufferContentReplaced whenever a real revert happened.
			 */
			if ((fLastModificationStamp == buffer.getModificationStamp()) && (!fBufferWasReplaced))
			{
				/*
				 * This can actually also happen if two normal save operations are following
				 * each other in very close succession. A typical example is the auto reformat
				 * source on save feature of eclipse. It will first save the document once in its
				 * unmodified state. Then it does the reformatting and saves the document _again_.
				 * That second save can end up (will always?) having the same modification stamp.
				 * 
				 * We thus treat this case as a SAVE operation for now.
				 * 
				 * TODO: check if this ever happens during any non-save operation.
				 */
				/*
				if (log.isTraceEnabled())
					log
							.trace("dirtyStateChanged(): ignoring pseudo REVERT - fBufferWasReplaced: "
									+ fBufferWasReplaced);
				return;
				*/
				if (log.isDebugEnabled())
					log
							.debug("dirtyStateChanged() - last modification timestamp remained unchanged but buffer was not replaced, assuming this is a SAVE operation - buffer: "
									+ buffer
									+ ", location: "
									+ buffer.getLocation()
									+ ", fBufferWasReplaced: "
									+ fBufferWasReplaced + ", fLastModificationStamp: " + fLastModificationStamp);

			}

			//we'll need to flush any currently pending diff events
			ITextEditor textEditor = CoreUtils.getTextEditorForLocation(buffer.getLocation());
			//if (textEditor != null)
			//flushDocumentListenerDiffEvents(buffer/*, textEditor*/);

			EclipseResourcePersistenceEvent newEvent = new EclipseResourcePersistenceEvent(CoreUtils.getUsername(),
					CoreUtils.getProjectnameFromLocation(buffer.getLocation().toString()));

			/*
			 * We're using changes to the modification time here to decide whether a file was saved or reverted.
			 * This will not work correctly on file systems which do not keep track of a file modification time.
			 * <em>stateChanging()</em> will log an error in such cases.
			 * Another approach would be to use the fact that <em>bufferContentReplaced()</em> was called as
			 * indication for a REVERT. However, it is unclear whether that method is really only called
			 * due to a revert. There may be other situations which could cause such a call.
			 */
			if (fLastModificationStamp != buffer.getModificationStamp())
			{
				//modification time changed => SAVED
				newEvent.setType(EclipseResourcePersistenceEvent.Type.SAVED);

				//TODO: should we check here that the modification actually _increased_ ?
				//let's add some warning message for now
				if (fLastModificationStamp > buffer.getModificationStamp())
					log.warn(
							"dirtyStateChanged() - modification stamp was DECREASED!, still treating as a SAVE action for now - buffer: "
									+ buffer + ", location: " + buffer.getLocation() + ", fBufferWasReplaced: "
									+ fBufferWasReplaced + ", fLastModificationStamp: " + fLastModificationStamp
									+ ", buffer mod. stamp: " + buffer.getModificationStamp(), new Throwable());
			}
			else if ((fLastModificationStamp == buffer.getModificationStamp()) && (!fBufferWasReplaced))
				//probably? also SAVED, see comment above
				newEvent.setType(EclipseResourcePersistenceEvent.Type.SAVED);
			else
				//modification time is still the same => REVERT
				newEvent.setType(EclipseResourcePersistenceEvent.Type.REVERTED);

			newEvent.setFilePath(CoreUtils.getProjectRelativePathFromLocation(buffer.getLocation().toString()));
			newEvent.setDocument(((ITextFileBuffer) buffer).getDocument());

			/*
			 * It may be interesting to know, if a file was open in an editor at save time or whether
			 * the file was modified and saved by some automated means, i.e. refactorings or code reformats.
			 */
			newEvent.setOpenInEditor((textEditor != null) ? true : false);

			CPCCorePlugin.getEventHubRegistry().dispatch(newEvent);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.filebuffers.IFileBufferListener#underlyingFileDeleted(org.eclipse.core.filebuffers.IFileBuffer)
	 */
	public void underlyingFileDeleted(IFileBuffer buffer)
	{
		if (log.isTraceEnabled())
			log.trace("underlyingFileDeleted() - buffer: " + buffer + " - location: " + buffer.getLocation());

		//not in use
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.filebuffers.IFileBufferListener#underlyingFileMoved(org.eclipse.core.filebuffers.IFileBuffer, org.eclipse.core.runtime.IPath)
	 */
	public void underlyingFileMoved(IFileBuffer buffer, IPath path)
	{
		if (log.isTraceEnabled())
			log.trace("underlyingFileMoved() - buffer: " + buffer + ", path: " + path + " - old location: "
					+ buffer.getLocation());

		//not in use
	}

	/**
	 * Called by {@link CPCSensorPlugin} at startup for all open documents.
	 * <br> 
	 * This is required because the file buffer listener is registered too late to be
	 * notified about the editors which eclipse re-opens automatically at startup.
	 * 
	 * @param project the project name, never null.
	 * @param filePath the projet relative file path, never null.
	 * @param document the document, never null.
	 */
	public void initiallyOpenDocument(String project, String filePath, IDocument document)
	{
		if (log.isTraceEnabled())
			log.trace("initiallyOpenDocument() - project: " + project + ", filePath: " + filePath + ", document: "
					+ document);
		assert (project != null && filePath != null && document != null);

		//TODO: Check that this doesn't lead to multiple file access open events for the same file.
		//		Maybe we need some extra checks for that?

		//make sure we're interested in this type of file
		if (!CoreConfigurationUtils.isSupportedFile(filePath))
		{
			log.trace("initiallyOpenDocument() - unsupported file type, ignoring.");
			return;
		}

		//remember this buffer as being of interest
		openCpcBuffers.add("/" + project + "/" + filePath);

		//create a new file open event
		EclipseFileAccessEvent newEvent = new EclipseFileAccessEvent(CoreUtils.getUsername(), project);

		newEvent.setType(EclipseFileAccessEvent.Type.OPENED);
		newEvent.setFilePath(filePath);
		newEvent.setDocument(document);

		CPCCorePlugin.getEventHubRegistry().dispatch(newEvent);
	}

}

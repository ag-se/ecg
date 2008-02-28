package org.electrocodeogram.cpc.sensor.listener;


import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.electrocodeogram.cpc.core.CPCCorePlugin;
import org.electrocodeogram.cpc.core.api.hub.event.EclipseEditorPartEvent;
import org.electrocodeogram.cpc.core.utils.CoreConfigurationUtils;
import org.electrocodeogram.cpc.core.utils.CoreFileUtils;
import org.electrocodeogram.cpc.core.utils.CoreUtils;


/**
 * This class is listening for events which are affecting GUI parts and
 * editors of Eclipse.
 * 
 * @author multiple
 */
public class CPCPartListener implements IPartListener
{
	private static final Log log = LogFactory.getLog(CPCPartListener.class);

	public CPCPartListener()
	{
		log.trace("CPCPartListener()");
	}

	/**
	 * @see org.eclipse.ui.IPartListener#partActivated(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partActivated(final IWorkbenchPart part)
	{
		if (log.isTraceEnabled())
			log.trace("partActivated() - part: " + part);

		if (part == null)
		{
			log.debug("partActivated() - the parameter part is null. Ignoring event.");
			return;
		}

		if (part instanceof IEditorPart)
		{
			/*
						if (part instanceof ITextEditor)
						{
							ITextEditor textEditor = (ITextEditor) part;
							// register document listener on opened Editors. Should have been done at partOpened
							// but in case of a new document instance for this editor, get sure to be registered.
							// Adding the same listener twice causes no harm. 
							IDocumentProvider provider = textEditor.getDocumentProvider();
							IDocument document = provider.getDocument(textEditor.getEditorInput());
							document.addDocumentListener(CPCSingleDocumentListener.getDocumentListenerForEditor(textEditor));

							// set current active TextEditor
							//TODO: do we need this anywhere?
							//this.sensor.activeTextEditor = textEditor;
						}
						*/

			String location = CoreUtils.getLocationFromPart(part);

			//make sure we're interested in this type of file
			//			if (!CoreConfigurationUtils.isSupportedFile(location))
			//			{
			//				log.trace("partActivated() - unsupported file type, ignoring.");
			//				return;
			//			}

			//we're only interested in files which are located within the workspace
			//			if (CoreFileUtils.isFileLocatedInWorkspace(location))
			//			{
			EclipseEditorPartEvent newEvent = new EclipseEditorPartEvent(CoreUtils.getUsername(), CoreUtils
					.getProjectnameFromLocation(location));

			newEvent.setType(EclipseEditorPartEvent.Type.ACTIVATED);
			newEvent.setFilePath(CoreUtils.getProjectRelativePathFromLocation(location));

			CPCCorePlugin.getEventHubRegistry().dispatch(newEvent);
			//			}
			//			else
			//			{
			//				if (log.isDebugEnabled())
			//					log.debug("partActivated() - ignoring event for file outside of workspace: " + location);
			//			}
		}
		/*
		else if (part instanceof IViewPart)
		{
			PartMicroActivityEvent partEvent = new PartMicroActivityEvent(ECGEclipseSensor.CREATOR, this.sensor
					.getUsername(), part.hashCode());

			partEvent.setActivity(PartMicroActivityEvent.Activity.ACTIVATED);
			partEvent.setPartName(part.getTitle());

			this.sensor.processMicroActivityEvent(partEvent);

		}*/
	}

	/**
	 * @see org.eclipse.ui.IPartListener#partClosed(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partClosed(final IWorkbenchPart part)
	{
		if (log.isTraceEnabled())
			log.trace("partClosed() - part: " + part);

		if (part == null)
		{
			log.debug("partClosed() - the parameter part is null. Ignoring event.");
			return;
		}

		if (part instanceof IEditorPart)
		{

			/*
			if (part instanceof ITextEditor)
			{
				if (log.isTraceEnabled())
					log.trace("partClosed() - clearing document listener: " + part);

				ITextEditor textEditor = (ITextEditor) part;

				//first get the listener
				IDocumentListener listener = CPCSingleDocumentListener.getDocumentListenerForEditor(textEditor);

				//then unregister it
				IDocumentProvider provider = textEditor.getDocumentProvider();
				IDocument document = provider.getDocument(textEditor.getEditorInput());
				document.removeDocumentListener(listener);

				//clear the instance from internal cache
				CPCSingleDocumentListener.removeDocumentListenersForEditor(textEditor);
			}
			*/

			String location = CoreUtils.getLocationFromPart(part);

			//make sure we're interested in this type of file
			//			if (!CoreConfigurationUtils.isSupportedFile(location))
			//			{
			//				log.trace("partClosed() - unsupported file type, ignoring.");
			//				return;
			//			}

			//we're only interested in files which are located within the workspace
			//			if (CoreFileUtils.isFileLocatedInWorkspace(location))
			//			{
			EclipseEditorPartEvent newEvent = new EclipseEditorPartEvent(CoreUtils.getUsername(), CoreUtils
					.getProjectnameFromLocation(location));

			newEvent.setType(EclipseEditorPartEvent.Type.CLOSED);
			newEvent.setFilePath(CoreUtils.getProjectRelativePathFromLocation(location));

			CPCCorePlugin.getEventHubRegistry().dispatch(newEvent);
			//			}
			//			else
			//			{
			//				if (log.isDebugEnabled())
			//					log.debug("partClosed() - ignoring event for file outside of workspace: " + location);
			//			}

		}
		/*
		else if (part instanceof IViewPart)
		{

			PartMicroActivityEvent partEvent = new PartMicroActivityEvent(ECGEclipseSensor.CREATOR, this.sensor
					.getUsername(), part.hashCode());

			partEvent.setActivity(PartMicroActivityEvent.Activity.CLOSED);
			partEvent.setPartName(part.getTitle());

			this.sensor.processMicroActivityEvent(partEvent);
		}*/
	}

	/**
	 * @see org.eclipse.ui.IPartListener#partDeactivated(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partDeactivated(final IWorkbenchPart part)
	{
		if (log.isTraceEnabled())
			log.trace("partDeactivated() - part: " + part);

		if (part == null)
		{
			log.debug("partDeactivated() - the parameter part is null. Ignoring event.");
			return;
		}

		if (part instanceof IEditorPart)
		{
			String location = CoreUtils.getLocationFromPart(part);

			//make sure we're interested in this type of file
			//			if (!CoreConfigurationUtils.isSupportedFile(location))
			//			{
			//				log.trace("partDeactivated() - unsupported file type, ignoring.");
			//				return;
			//			}

			//we're only interested in files which are located within the workspace
			//			if (CoreFileUtils.isFileLocatedInWorkspace(location))
			//			{
			EclipseEditorPartEvent newEvent = new EclipseEditorPartEvent(CoreUtils.getUsername(), CoreUtils
					.getProjectnameFromLocation(location));

			newEvent.setType(EclipseEditorPartEvent.Type.DEACTIVATED);
			newEvent.setFilePath(CoreUtils.getProjectRelativePathFromLocation(location));

			CPCCorePlugin.getEventHubRegistry().dispatch(newEvent);
			//			}
			//			else
			//			{
			//				if (log.isDebugEnabled())
			//					log.debug("partDeactivated() - ignoring event for file outside of workspace: " + location);
			//			}

		}
		/*
		else if (part instanceof IViewPart)
		{
			PartMicroActivityEvent partEvent = new PartMicroActivityEvent(ECGEclipseSensor.CREATOR, this.sensor
					.getUsername(), part.hashCode());

			partEvent.setActivity(PartMicroActivityEvent.Activity.DEACTIVATED);
			partEvent.setPartName(part.getTitle());

			this.sensor.processMicroActivityEvent(partEvent);

		}*/
	}

	/**
	 * @see org.eclipse.ui.IPartListener#partOpened(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partOpened(final IWorkbenchPart part)
	{
		if (log.isTraceEnabled())
			log.trace("partOpened() - part: " + part);

		if (part == null)
		{
			log.debug("partOpened() - the parameter part is null. Ignoring event.");
			return;
		}

		if (part instanceof IEditorPart)
		{
			String location = CoreUtils.getLocationFromPart(part);

			//make sure we're interested in this type of file
			//			if (!CoreConfigurationUtils.isSupportedFile(location))
			//			{
			//				log.trace("partOpened() - unsupported file type, ignoring.");
			//				return;
			//			}

			EclipseEditorPartEvent newEvent = new EclipseEditorPartEvent(CoreUtils.getUsername(), CoreUtils
					.getProjectnameFromLocation(location));

			newEvent.setType(EclipseEditorPartEvent.Type.OPENED);
			newEvent.setFilePath(CoreUtils.getProjectRelativePathFromLocation(location));

			CPCCorePlugin.getEventHubRegistry().dispatch(newEvent);

			/*
			 * We only need to add the special copy&paste actions for supported source files which
			 * are located within the workspace.
			 */
			if (CoreConfigurationUtils.isSupportedFile(location) && CoreFileUtils.isFileLocatedInWorkspace(location))
			{
				// TODO The following line is just for exploration
				//            	part.getSite().getSelectionProvider().addSelectionChangedListener(new ECGSelectionChangedListener());

				if (part instanceof ITextEditor)
				{
					final ITextEditor textEditor = (ITextEditor) part;
					/*
									// register document listener on opened Editors
									IDocumentProvider provider = textEditor.getDocumentProvider();
									IDocument document = provider.getDocument(textEditor.getEditorInput());
									//document.addDocumentListener(this.docListener);
									document.addDocumentListener(CPCSingleDocumentListener.getDocumentListenerForEditor(textEditor));
					*/
					// Register new CCP actions on this editor
					PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable()
					{
						public void run()
						{
							Action action = new CPCTextOperationAction(ResourceBundle
									.getBundle("org.eclipse.ui.texteditor.ConstructedEditorMessages"), "Editor.Cut.",
									textEditor, ITextOperationTarget.CUT); //$NON-NLS-1$
							textEditor.getEditorSite().getActionBars().setGlobalActionHandler(
									ActionFactory.CUT.getId(), action);
							textEditor.setAction(ITextEditorActionConstants.CUT, action);

							action = new CPCTextOperationAction(ResourceBundle
									.getBundle("org.eclipse.ui.texteditor.ConstructedEditorMessages"), "Editor.Copy.",
									textEditor, ITextOperationTarget.COPY); //$NON-NLS-1$
							textEditor.getEditorSite().getActionBars().setGlobalActionHandler(
									ActionFactory.COPY.getId(), action);
							textEditor.setAction(ITextEditorActionConstants.COPY, action);

							action = new CPCTextOperationAction(ResourceBundle
									.getBundle("org.eclipse.ui.texteditor.ConstructedEditorMessages"), "Editor.Paste.",
									textEditor, ITextOperationTarget.PASTE); //$NON-NLS-1$
							textEditor.getEditorSite().getActionBars().setGlobalActionHandler(
									ActionFactory.PASTE.getId(), action);
							textEditor.setAction(ITextEditorActionConstants.PASTE, action);

							textEditor.getEditorSite().getActionBars().updateActionBars();
						}
					});

					// TODO The next line is only for exploration (dirty bit flagged)
					//                    textEditor.addPropertyListener(new ECGPropertyListener());
					// TODO next line is just for exploration (dirty bit flagged, as well)
					//                    provider.addElementStateListener(elementStateListener);

					//The file buffer listener needs to know about this event because it might have to
					//create some artificial EclipseFileAccessEvent OPEN events for editors which
					//were automatically opened at eclipse startup.
					/*
					IDocumentProvider provider = textEditor.getDocumentProvider();
					IDocument document = provider.getDocument(textEditor.getEditorInput());
					fileBufferListener.editorOpenedForDocument(document);
					*/
				}
			}
			else
			{
				if (log.isDebugEnabled())
					log
							.debug("partOpened() - not registering C&P handlers for non-source file or file outside of workspace: "
									+ location);
			}
			//			else
			//			{
			//				if (log.isDebugEnabled())
			//					log
			//							.debug("partOpened() - ignoring event for file outside of workspace, also not registering CP handlers: "
			//									+ location);
			//			}
		}
		/*
		else if (part instanceof IViewPart)
		{

			PartMicroActivityEvent partEvent = new PartMicroActivityEvent(ECGEclipseSensor.CREATOR, this.sensor
					.getUsername(), part.hashCode());

			partEvent.setActivity(PartMicroActivityEvent.Activity.OPENED);
			partEvent.setPartName(part.getTitle());

			this.sensor.processMicroActivityEvent(partEvent);

			this.sensor.activeView = (IViewPart) part;
		}*/
	}

	/**
	 * @see org.eclipse.ui.IPartListener#partBroughtToTop(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partBroughtToTop(IWorkbenchPart part)
	{
		// not used
	}
}

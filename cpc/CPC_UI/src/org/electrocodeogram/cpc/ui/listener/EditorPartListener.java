package org.electrocodeogram.cpc.ui.listener;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.electrocodeogram.cpc.core.api.data.ICloneFile;
import org.electrocodeogram.cpc.core.api.hub.event.CPCEvent;
import org.electrocodeogram.cpc.core.api.hub.event.EclipseEditorPartEvent;
import org.electrocodeogram.cpc.core.api.hub.registry.IEventHubListener;
import org.electrocodeogram.cpc.ui.data.CloneDataModel;


public class EditorPartListener implements IEventHubListener
{
	private static Log log = LogFactory.getLog(EditorPartListener.class);

	@Override
	public void processEvent(CPCEvent event)
	{
		if (log.isTraceEnabled())
			log.trace("processEvent() - event: " + event);

		if (!(event instanceof EclipseEditorPartEvent))
		{
			log.error("processEvent() - illegal event type: " + event, new Throwable());
			return;
		}

		EclipseEditorPartEvent editorEvent = (EclipseEditorPartEvent) event;
		CloneDataModel model = CloneDataModel.getInstance();

		//we're only interested in events for java files
		//		if (!editorEvent.getFilePath().endsWith(".java"))
		//		{
		//			log.trace("processEvent() - ignoring non-java file");
		//			return;
		//		}
		/*
		 * We are mainly interested in events for "source" files. However, even events
		 * for non supported files may be of interest, as they may require us to clear
		 * all clone data from the cpc views.
		 */
		if (!editorEvent.isSupportedFile() || !editorEvent.isFileLocatedInWorkspace())
		{
			log.trace("processEvent() - ignoring non-source file or file not located in workspace.");

			if (editorEvent.getType().equals(EclipseEditorPartEvent.Type.ACTIVATED))
			{
				log.trace("processEvent() - puring clone data from views.");
				model.clearCloneData();
			}

			return;
		}

		if (editorEvent.getType().equals(EclipseEditorPartEvent.Type.ACTIVATED))
		{
			//editor just got the focus, we may need to load the corresponding clone data

			//first check if the clone data model is already linked to this editor
			ICloneFile currentCloneFile = model.getCurrentCloneFile();
			if (currentCloneFile != null && currentCloneFile.getProject().equals(editorEvent.getProject())
					&& currentCloneFile.getPath().equals(editorEvent.getFilePath()))
			{
				//this file is already linked to the model, no need to do anything
				log.trace("processEvent() - editor focus returned to active editor, no change required.");
				return;
			}

			if (log.isTraceEnabled())
				log.trace("processEvent() - clone data needs to be (re)loaded for: " + editorEvent.getProject() + "/"
						+ editorEvent.getFilePath());

			model.loadCloneData(editorEvent.getProject(), editorEvent.getFilePath());
		}
		else if (editorEvent.getType().equals(EclipseEditorPartEvent.Type.CLOSED))
		{
			/*
			 * The editor was closed, we might have to clear the clone data views.
			 * We don't do anything on deactivation of an editor, so some cleanup may be needed here.
			 * However, by the time we receive this event, another editor might already have received the focus.
			 * So we can't blindly reset the clone data here.
			 * 
			 * The clone data should only be reset if the file of the closed editor is still
			 * the currently active file of the model.
			 */

			//check if
			ICloneFile currentFile = model.getCurrentCloneFile();
			if (currentFile == null || !currentFile.getProject().equals(editorEvent.getProject())
					|| !currentFile.getPath().equals(editorEvent.getFilePath()))
			{
				log
						.trace("processEvent() - ignoring editor close event, as no clone data was loaded or another editor has already received focus.");
			}
			else
			{
				log.trace("processEvent() - active editor closed, puring clone data from views.");
				model.clearCloneData();
			}

		}
	}

}

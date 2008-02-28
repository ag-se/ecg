package org.electrocodeogram.cpc.sensor.listener;


/**
 * This class listens for ECG Sensor events and converts them into the corresponding CPC Eclipse events.
 * Converted events are dispatched to the CPC Core event hub registry.<br/>
 * <br/>
 * These are all very simple and straight forward conversions. More complex conversions are
 * handled by separate conversion listeners.<br/>  
 * <br/>
 * NO LONGER IN USE
 * 
 * @author vw
 * @deprecated
 */
@Deprecated
public class SimpleECGSensorConversionListener //implements IMAEEventListener
{
	/*
	private static Log log = LogFactory.getLog(SimpleECGSensorConversionListener.class);

	public void processEvent(MicroActivityEvent event)
	{
		if (log.isTraceEnabled())
			log.trace("processEvent() - event: " + event);

		CPCEvent resultEvent = convertEvent(event);

		if (resultEvent != null)
			CPCCorePlugin.getEventHubRegistry().dispatch(resultEvent);
	}

	private CPCEvent convertEvent(MicroActivityEvent event)
	{
		CPCEvent resultEvent = null;

		if (log.isTraceEnabled())
			log.trace("convertEvent() - event: " + event);

		if (event instanceof TextOperationMicroActivityEvent)
		{
			resultEvent = convertTextOperationMicroActivityEvent((TextOperationMicroActivityEvent) event);
		}
		else if (event instanceof CodeDiffMicroActivityEvent)
		{
			resultEvent = convertCodeDiffMicroActivityEvent((CodeDiffMicroActivityEvent) event);
		}
		else if (event instanceof EditorMicroActivityEvent)
		{
			resultEvent = convertEditorMicroActivityEvent((EditorMicroActivityEvent) event);
		}
		else if (event instanceof ResourceMicroActivityEvent)
		{
			resultEvent = convertResourceMicroActivityEvent((ResourceMicroActivityEvent) event);
		}
		else
		{
			log.warn("convertEvent() - unknown event: " + event);
		}

		if (log.isTraceEnabled())
			log.trace("convertEvent() - result: " + resultEvent);

		return resultEvent;
	}

	private CPCEvent convertResourceMicroActivityEvent(ResourceMicroActivityEvent resourceEvent)
	{
		//we're only interested in the Activity types SAVED and REVERTED
		if (resourceEvent.getActivity().equals(ResourceMicroActivityEvent.Activity.SAVED)
				|| resourceEvent.getActivity().equals(ResourceMicroActivityEvent.Activity.REVERTED))
		{
			EclipseResourcePersistenceEvent newEvent = new EclipseResourcePersistenceEvent(resourceEvent.getUserId(),
					resourceEvent.getProjectId());

			newEvent.setFilePath(resourceEvent.getResourceName());
			newEvent.setOpenInEditor(resourceEvent.isOpenInEditor());

			ResourceMicroActivityEvent.Activity activity = resourceEvent.getActivity();
			if (ResourceMicroActivityEvent.Activity.SAVED.equals(activity))
				newEvent.setType(EclipseResourcePersistenceEvent.Type.SAVED);
			else if (ResourceMicroActivityEvent.Activity.REVERTED.equals(activity))
				newEvent.setType(EclipseResourcePersistenceEvent.Type.REVERTED);

			return newEvent;
		}
		else
		{
			if (log.isDebugEnabled())
				log.debug("convertEvent() - skipping ResourceMicroActivityEvent due to activity missmatch: "
						+ resourceEvent.getActivity() + " - " + resourceEvent);

			return null;
		}
	}

	private CPCEvent convertEditorMicroActivityEvent(EditorMicroActivityEvent editorEvent)
	{
		EclipseEditorPartEvent newEvent = new EclipseEditorPartEvent(editorEvent.getUserId(), editorEvent
				.getProjectId());

		newEvent.setFilePath(editorEvent.getEditorName());

		EditorMicroActivityEvent.Activity activity = editorEvent.getActivity();
		if (EditorMicroActivityEvent.Activity.ACTIVATED.equals(activity))
			newEvent.setType(EclipseEditorPartEvent.Type.ACTIVATED);
		else if (EditorMicroActivityEvent.Activity.CLOSED.equals(activity))
			newEvent.setType(EclipseEditorPartEvent.Type.CLOSED);
		else if (EditorMicroActivityEvent.Activity.DEACTIVATED.equals(activity))
			newEvent.setType(EclipseEditorPartEvent.Type.DEACTIVATED);
		else if (EditorMicroActivityEvent.Activity.OPENED.equals(activity))
			newEvent.setType(EclipseEditorPartEvent.Type.OPENED);

		return newEvent;
	}

	private CPCEvent convertCodeDiffMicroActivityEvent(CodeDiffMicroActivityEvent diffEvent)
	{
		EclipseCodeDiffEvent newEvent = new EclipseCodeDiffEvent(diffEvent.getUserId(), diffEvent.getProjectId());

		newEvent.setAddedText(diffEvent.getAddedText());
		newEvent.setFilePath(diffEvent.getDocumentName());
		newEvent.setOffset(diffEvent.getOffset());
		newEvent.setReplacedText(diffEvent.getReplacedText());
		newEvent.setEditorContent(diffEvent.getEditorContent());

		return newEvent;
	}

	private CPCEvent convertTextOperationMicroActivityEvent(TextOperationMicroActivityEvent textEvent)
	{
		EclipseCutCopyPasteEvent newEvent = new EclipseCutCopyPasteEvent(textEvent.getUserId(), textEvent
				.getProjectId());

		newEvent.setClipboard(textEvent.getClipboard());
		newEvent.setEditorContent(textEvent.getEditorContent());
		newEvent.setFilePath(textEvent.getEditorName());
		newEvent.setOffset(textEvent.getOffset());
		newEvent.setSelection(textEvent.getSelection());

		TextOperationMicroActivityEvent.Activity activity = textEvent.getActivity();
		if (TextOperationMicroActivityEvent.Activity.CUT.equals(activity))
			newEvent.setType(EclipseCutCopyPasteEvent.Type.CUT);
		else if (TextOperationMicroActivityEvent.Activity.COPY.equals(activity))
			newEvent.setType(EclipseCutCopyPasteEvent.Type.COPY);
		else if (TextOperationMicroActivityEvent.Activity.PASTE.equals(activity))
			newEvent.setType(EclipseCutCopyPasteEvent.Type.PASTE);

		return newEvent;
	}
	*/
}

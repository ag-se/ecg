package org.electrocodeogram.cpc.sensor.listener;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.jface.text.IDocument;


/**
 * NO LONGER USED
 * 
 * @author vw
 * @deprecated
 */
@Deprecated
public class CPCDocumentSetupParticipant implements IDocumentSetupParticipant
{
	private static final Log log = LogFactory.getLog(CPCDocumentSetupParticipant.class);

	/**
	 * @deprecated
	 */
	@Deprecated
	public void setup(IDocument document)
	{
		if (log.isTraceEnabled())
			log.trace("setup() - document: " + document);

		ITextFileBuffer buffer = FileBuffers.getTextFileBufferManager().getTextFileBuffer(document);
		log.trace("BUFFER: " + buffer);
		if (buffer != null)
			log.trace("LOCATION: " + buffer.getLocation());

		log.trace("setup() - registering location-less document listener for document.");
		/*
		 * Eclipse does not offer us any way to participate in the destruction of the document.
		 * This means that we will not be able to unregister the listener from the document.
		 * However, as the listener is initially not referenced by any other code, it will
		 * be garbage collected together with the document.
		 * 
		 * Once the listener received an event while an underlying filebuffer was available,
		 * it will register itself with the CPCDocumentListenerRegistry. Starting from
		 * that point the registry will need to take care of deleting its local
		 * reference to the listener to ensure that no listener leaking occurs.
		 */
		//document.addDocumentListener(new CPCSingleDocumentListener());
	}

	//	private class MyDocumentListener implements IDocumentListener
	//	{
	//
	//		@Override
	//		public void documentAboutToBeChanged(DocumentEvent event)
	//		{
	//			/*
	//			if (log.isTraceEnabled())
	//				log.trace("documentAboutToBeChanged() - event: " + event);
	//
	//			log.trace("OFFSET: " + event.getOffset() + ", LEN: " + event.getLength());
	//			log.trace("TEXT: " + event.getText());
	//			*/
	//		}
	//
	//		@Override
	//		public void documentChanged(DocumentEvent event)
	//		{
	//			if (log.isTraceEnabled())
	//				log.trace("documentChanged() - event: " + event);
	//
	//			log.trace("OFFSET: " + event.getOffset() + ", LEN: " + event.getLength());
	//			log.trace("TEXT: " + event.getText());
	//
	//			ITextFileBuffer buffer = FileBuffers.getTextFileBufferManager().getTextFileBuffer(event.getDocument());
	//			log.trace("BUFFER: " + buffer);
	//			if (buffer != null)
	//				log.trace("LOCATION: " + buffer.getLocation());
	//		}
	//	}
}

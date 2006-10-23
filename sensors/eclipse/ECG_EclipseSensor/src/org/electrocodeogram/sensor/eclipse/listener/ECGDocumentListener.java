package org.electrocodeogram.sensor.eclipse.listener;

import java.util.Timer;
import java.util.logging.Level;

import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;
import org.electrocodeogram.sensor.eclipse.ECGEclipseSensor; 

import java.util.TimerTask;
import java.util.logging.Level;

import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorPart;
import org.electrocodeogram.event.CommonData;
import org.electrocodeogram.event.MicroActivity;
import org.electrocodeogram.logging.LogHelper.ECGLevel;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This is listening for events about changes in the text of open
 * documents.
 */
public class ECGDocumentListener implements IDocumentListener {

    /**
     * 
     */
    private final ECGEclipseSensor sensor;
    /**
     * This is used to wait a moment after a
     * <em>DocumentChanged</em> event has been recorded. Only
     * when the user has not changed the document for
     * {@link ECGEclipseSensor#CODECHANGE_INTERVALL} amount of
     * time, a <em>Codechange</em> event is sent.
     */
    private Timer timer = null;

    /**
     * Creates the <em>DocumentListenerAdapter</em> and the
     * <code>Timer</code>.
     * @param sensor TODO
     */
    public ECGDocumentListener(ECGEclipseSensor sensor) {
        this.sensor = sensor;
        ECGEclipseSensor.logger.entering(this.getClass().getName(),
            "DocumentListenerAdapter");

        this.timer = new Timer();

        ECGEclipseSensor.logger.exiting(this.getClass().getName(), "DocumentListenerAdapter");
    }

    /**
     * @see org.eclipse.jface.text.IDocumentListener#documentAboutToBeChanged(org.eclipse.jface.text.DocumentEvent)
     */
    public void documentAboutToBeChanged(
    final DocumentEvent event) {
        ECGEclipseSensor.logger.entering(this.getClass().getName(),
            "documentAboutToBeChanged", new Object[] {event});

        // not supported in Eclipse Sensor.

        ECGEclipseSensor.logger.exiting(this.getClass().getName(),
            "documentAboutToBeChanged");
    }

    /**
     * @see org.eclipse.jface.text.IDocumentListener#documentChanged(org.eclipse.jface.text.DocumentEvent)
     */
    public void documentChanged(final DocumentEvent event) {
        ECGEclipseSensor.logger.entering(this.getClass().getName(), "documentChanged",
            new Object[] {event});

        if (event == null) {
            ECGEclipseSensor.logger.log(Level.FINE,
                "The Parameter event null. Ignoring event.");

            ECGEclipseSensor.logger.exiting(this.getClass().getName(), "documentChanged");

            return;
        }

        this.timer.cancel();

        this.timer = new Timer(); // TODO: Is it a good idea to create a  new timer every now and then (= on each key stroke!)?

        this.timer.schedule(new CodeChangeTimerTask(event.getDocument(),
            this.sensor.activeTextEditor),
            ECGEclipseSensor.CODECHANGE_INTERVAL);

        ECGEclipseSensor.logger.exiting(this.getClass().getName(), "documentChanged");
    }

    /**
     * This <em>TimerTask</em> is used in creating
     * <em>Codechange</em> events. In <em>Eclipse</em> every time
     * the user changes a single character in the active document an
     * <em>DocumentChanged</em> event is fired. To avoid sending
     * <em>Codechange</em> this often, the sensor shall wait for an
     * amount of time after before sending a <em>Codechange</em>.
     * Only when the user does not change the document's text for
     * {@link ECGEclipseSensor#CODECHANGE_INTERVAL} time, a
     * <em>Codechange</em> is sent to the ECG Lab.
     */
    class CodeChangeTimerTask extends TimerTask {

        //private Document msdt_codechange_doc;
        //private Element codechange_username;
        //private Element codechange_projectname;
        //private Element codechange_id;        
        private Element codechange_document;
        private CDATASection codechange_contents;
        private Element codechange_documentname;
        private MicroActivity microActivity;
        
        /**
         * This is the document that has been changed.
         */
        private IDocument doc;

        /**
         * The enclosing editor of the document.
         */
        private IEditorPart textEditor;

        /**
         * This creates the <em>TimerTask</em>.
         * @param document
         *            Is the document that has been changed
         * @param documentName
         *            Is the name of the document
         */
        public CodeChangeTimerTask(final IDocument document, final IEditorPart textEditor) {
            ECGEclipseSensor.logger.entering(this.getClass().getName(), "CodeChangeTimerTask",
                new Object[] {document, textEditor});

            this.doc = document;
            this.textEditor = textEditor;

            microActivity = new MicroActivity();

            Document microactivity_doc = microActivity.getMicroActivityDoc();
            Element codechange = microactivity_doc.createElement("codechange");
            codechange_document = microactivity_doc.createElement("document");
            codechange_contents = microactivity_doc.createCDATASection("");
            codechange_documentname = microactivity_doc.createElement("documentname");

            codechange.appendChild(codechange_document);
            codechange_document.appendChild(codechange_contents);
            codechange.appendChild(codechange_documentname);
            
            microActivity.setCustomElement(codechange);            
                                                    
            CommonData commonData = microActivity.getCommonData();
            commonData.setUsername(sensor.getUsername());
            commonData.setVersion(1); // 1 is default
            commonData.setCreator(ECGEclipseSensor.CREATOR); 

            ECGEclipseSensor.logger.exiting(this.getClass().getName(), "CodeChangeTimerTask");
        }

        /**
         * @see java.util.TimerTask#run()
         */
        public void run() {
            ECGEclipseSensor.logger.entering(this.getClass().getName(), "run");

            ECGEclipseSensor sensor = ECGEclipseSensor.getInstance();

            ECGEclipseSensor.logger.log(ECGLevel.PACKET, "A codechange event has been recorded.");

            CommonData commonData = microActivity.getCommonData();
            commonData.setProjectname(ECGEclipseSensor.getProjectnameFromLocation(textEditor.getTitleToolTip()));
            commonData.setId(String.valueOf(textEditor.hashCode()));
            codechange_contents.setNodeValue(this.doc.get());
            codechange_documentname.setTextContent(ECGEclipseSensor.getFilenameFromLocation(textEditor.getTitleToolTip()));

            sensor.processActivity("msdt.codechange.xsd", microActivity.getSerializedMicroActivity());                    
            
            ECGEclipseSensor.logger.exiting(this.getClass().getName(), "run");

        }
    }

}
package org.electrocodeogram.sensor.eclipse.listener;

import java.util.Timer;
import java.util.logging.Level;

import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;
import org.electrocodeogram.sensor.eclipse.ECGEclipseSensor; 

import java.util.TimerTask;
import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorPart;
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

        private Document msdt_codechange_doc;

        private Element codechange_username;
        private Element codechange_projectname;
        private Element codechange_id;        
        private Element codechange_document;
        private CDATASection codechange_contents;
        private Element codechange_documentname;
        
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

            try {
                
                // initialize DOM skeleton for msdt.codechange.xsd
                msdt_codechange_doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
                Element codechange_microactivity = msdt_codechange_doc.createElement("microActivity");                
                Element codechange_commondata = msdt_codechange_doc.createElement("commonData");
                Element codechange_codechange = msdt_codechange_doc.createElement("codechange");
                codechange_username = msdt_codechange_doc.createElement("username");
                codechange_projectname = msdt_codechange_doc.createElement("projectname");
                codechange_id = msdt_codechange_doc.createElement("id");
                codechange_document = msdt_codechange_doc.createElement("document");
                codechange_contents = msdt_codechange_doc.createCDATASection("");
                codechange_documentname = msdt_codechange_doc.createElement("documentname");

                msdt_codechange_doc.appendChild(codechange_microactivity);
                  codechange_microactivity.appendChild(codechange_commondata);
                    codechange_commondata.appendChild(codechange_username);
                    codechange_commondata.appendChild(codechange_projectname);
                    codechange_commondata.appendChild(codechange_id);
                  codechange_microactivity.appendChild(codechange_codechange);
                    codechange_codechange.appendChild(codechange_document);
                      codechange_document.appendChild(codechange_contents);
                    codechange_codechange.appendChild(codechange_documentname);
                                        
            } catch (ParserConfigurationException e) {
                ECGEclipseSensor.logger.log(Level.SEVERE,
                    "Could not instantiate the DOM Document.");
                ECGEclipseSensor.logger.log(Level.FINE, e.getMessage());
            }
            
            ECGEclipseSensor.logger.exiting(this.getClass().getName(), "CodeChangeTimerTask");
        }

        /**
         * @see java.util.TimerTask#run()
         */
        public void run() {
            ECGEclipseSensor.logger.entering(this.getClass().getName(), "run");

            ECGEclipseSensor sensor = ECGEclipseSensor.getInstance();

            ECGEclipseSensor.logger
                .log(ECGLevel.PACKET, "A codechange event has been recorded.");

            codechange_username.setTextContent(sensor.getUsername());
            codechange_projectname.setTextContent(ECGEclipseSensor.getProjectnameFromLocation(textEditor.getTitleToolTip()));
            codechange_id.setTextContent(String.valueOf(textEditor.hashCode()));
            codechange_contents.setNodeValue(this.doc.get());
            codechange_documentname.setTextContent(ECGEclipseSensor.getFilenameFromLocation(textEditor.getTitleToolTip()));

            sensor.processActivity("msdt.codechange.xsd", 
                sensor.xmlDocumentSerializer.writeToString(msdt_codechange_doc));                    
            
            /* TODO Obsolete code
            sensor.processActivity(
                "msdt.codechange.xsd",
                "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                    + sensor.getUsername()
                    + "</username><projectname>"
                    + sensor.getProjectnameFromLocation(textEditor.getTitleToolTip())
                    + "</projectname><id>"
                    + textEditor.hashCode()
                    + "</id></commonData><codechange><document><![CDATA["
                    + this.doc.get()
                    + "]" + "]" + "></document><documentname>"
                    + sensor.getFilenameFromLocation(textEditor.getTitleToolTip())
                    + "</documentname></codechange></microActivity>");
             */
            
            ECGEclipseSensor.logger.exiting(this.getClass().getName(), "run");

        }
    }

}
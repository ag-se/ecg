package org.electrocodeogram.sensor.eclipse.listener;

import java.util.ResourceBundle;
import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.electrocodeogram.logging.LogHelper.ECGLevel;
import org.electrocodeogram.sensor.eclipse.ECGEclipseSensor;
import org.electrocodeogram.sensor.eclipse.editor.ECGTextOperationAction;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This is listening for events that are affected to GUI parts and
 * editors of <em>Eclipse</em>.
 */
public class ECGPartListener implements IPartListener {

    /**
     * 
     */
    private final ECGEclipseSensor sensor;
    
    private Document msdt_editor_doc;
    private Document msdt_part_doc;
    private Document msdt_codestatus_doc;
    
    private Element editor_username;
    private Element editor_projectname;
    private Element editor_id;        
    private Element editor_activity;
    private Element editor_editorname;
    
    private Element part_username;
    private Element part_id;        
    private Element part_activity;
    private Element part_partname;

    private Element codestatus_username;
    private Element codestatus_projectname;
    private Element codestatus_id;        
    private Element codestatus_document;
    private CDATASection codestatus_contents;
    private Element codestatus_documentname;

    private ECGDocumentListener docListener;
    
    public ECGPartListener(ECGEclipseSensor sensor, ECGDocumentListener docListener) {
        this.sensor = sensor;
        this.docListener = docListener;
        try {
            
            // initialize DOM skeleton for msdt.editor.xsd
            msdt_editor_doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element editor_microactivity = msdt_editor_doc.createElement("microActivity");                
            Element editor_commondata = msdt_editor_doc.createElement("commonData");
            Element editor_editor = msdt_editor_doc.createElement("editor");
            editor_username = msdt_editor_doc.createElement("username");
            editor_projectname = msdt_editor_doc.createElement("projectname");
            editor_id = msdt_editor_doc.createElement("id");
            editor_activity = msdt_editor_doc.createElement("activity");
            editor_editorname = msdt_editor_doc.createElement("editorname");

            msdt_editor_doc.appendChild(editor_microactivity);
              editor_microactivity.appendChild(editor_commondata);
                editor_commondata.appendChild(editor_username);
                editor_commondata.appendChild(editor_projectname);
                editor_commondata.appendChild(editor_id);
              editor_microactivity.appendChild(editor_editor);
                editor_editor.appendChild(editor_activity);
                editor_editor.appendChild(editor_editorname);

            // initialize DOM skeleton for msdt.part.xsd
            msdt_part_doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element part_microactivity = msdt_part_doc.createElement("microActivity");                
            Element part_commondata = msdt_part_doc.createElement("commonData");
            Element part_part = msdt_part_doc.createElement("part");
            part_username = msdt_part_doc.createElement("username");
            part_id = msdt_part_doc.createElement("id");
            part_activity = msdt_part_doc.createElement("activity");
            part_partname = msdt_part_doc.createElement("partname");

            msdt_part_doc.appendChild(part_microactivity);
              part_microactivity.appendChild(part_commondata);
                part_commondata.appendChild(part_username);
                part_commondata.appendChild(part_id);
              part_microactivity.appendChild(part_part);
                part_part.appendChild(part_activity);
                part_part.appendChild(part_partname);

            // initialize DOM skeleton for msdt.codestatus.xsd
            msdt_codestatus_doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element codestatus_microactivity = msdt_codestatus_doc.createElement("microActivity");                
            Element codestatus_commondata = msdt_codestatus_doc.createElement("commonData");
            Element codestatus_codestatus = msdt_codestatus_doc.createElement("codestatus");
            codestatus_username = msdt_codestatus_doc.createElement("username");
            codestatus_projectname = msdt_codestatus_doc.createElement("projectname");
            codestatus_id = msdt_codestatus_doc.createElement("id");
            codestatus_document = msdt_codestatus_doc.createElement("document");
            codestatus_contents = msdt_codestatus_doc.createCDATASection("");
            codestatus_documentname = msdt_codestatus_doc.createElement("documentname");

            msdt_codestatus_doc.appendChild(codestatus_microactivity);
              codestatus_microactivity.appendChild(codestatus_commondata);
                codestatus_commondata.appendChild(codestatus_username);
                codestatus_commondata.appendChild(codestatus_projectname);
                codestatus_commondata.appendChild(codestatus_id);
              codestatus_microactivity.appendChild(codestatus_codestatus);
                codestatus_codestatus.appendChild(codestatus_document);
                  codestatus_document.appendChild(codestatus_contents);
                codestatus_codestatus.appendChild(codestatus_documentname);
                                    
        } catch (ParserConfigurationException e) {
            ECGEclipseSensor.logger.log(Level.SEVERE,
                "Could not instantiate the DOM Document in ECGPartListener.");
            ECGEclipseSensor.logger.log(Level.FINE, e.getMessage());
        }
    }

    /**
     * @see org.eclipse.ui.IPartListener#partActivated(org.eclipse.ui.IWorkbenchPart)
     */
    public void partActivated(final IWorkbenchPart part) {

        ECGEclipseSensor.logger.entering(this.getClass().getName(), "partActivated",
            new Object[] {part});

        if (part == null) {
            ECGEclipseSensor.logger.log(Level.FINE,
                "The Parameter part is null. Ignoring event.");

            ECGEclipseSensor.logger.exiting(this.getClass().getName(), "partActivated");

            return;
        }

        if (part instanceof IEditorPart) {
            
            if (part instanceof ITextEditor) {
                ITextEditor textEditor  = (ITextEditor) part;
                // register document listener on opened Editors. Should have been done at partOpened
                // but in case of a new document instance for this editor, get sure to be registered.
                // Adding the same listener twice causes no harm. 
                IDocumentProvider provider = textEditor.getDocumentProvider();
                IDocument document = provider.getDocument(textEditor.getEditorInput());
                document.addDocumentListener(this.docListener);
                // set current active TextEditor
                this.sensor.activeTextEditor = textEditor;
            }
            
            ECGEclipseSensor.logger.log(ECGLevel.PACKET,
                "An editorActivated event has been recorded.");

            editor_id.setTextContent(String.valueOf(part.hashCode()));
            editor_projectname.setTextContent(ECGEclipseSensor.getProjectnameFromLocation(part.getTitleToolTip()));
            editor_username.setTextContent(this.sensor.username);
            editor_activity.setTextContent("activated");
            editor_editorname.setTextContent(ECGEclipseSensor.getFilenameFromLocation(part.getTitleToolTip()));

            this.sensor.processActivity("msdt.editor.xsd", 
                    this.sensor.xmlDocumentSerializer.writeToString(msdt_editor_doc));

            /* TODO old code, remove if obsolete
            processActivity(
                "msdt.editor.xsd",
                "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                    + ECGEclipseSensor.this.username
                    + "</username><projectname>"
                    + getProjectnameFromLocation(part.getTitleToolTip())
                    + "</projectname><id>"
                    + part.hashCode()
                    + "</id></commonData><editor><activity>activated</activity><editorname>"
                    + getFilenameFromLocation(part.getTitleToolTip())
                    + "</editorname></editor></microActivity>");
             */
        } else if (part instanceof IViewPart) {
            ECGEclipseSensor.logger.log(ECGLevel.PACKET,
                "A partActivated event has been recorded.");

            part_id.setTextContent(String.valueOf(part.hashCode()));
            part_username.setTextContent(this.sensor.username);
            part_activity.setTextContent("activated");
            part_partname.setTextContent(part.getTitle());

            this.sensor.processActivity("msdt.part.xsd", 
                    this.sensor.xmlDocumentSerializer.writeToString(msdt_part_doc));

            /* TODO old code, remove if obsolete
            processActivity(
                "msdt.part.xsd",
                "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                    + ECGEclipseSensor.this.username
                    + "</username><id>"
                    + part.hashCode()
                    + "</id></commonData><part><activity>activated</activity><partname>"
                    + part.getTitle()
                    + "</partname></part></microActivity>");
             */
        } 

        ECGEclipseSensor.logger.exiting(this.getClass().getName(), "partActivated");
    }

    /**
     * @see org.eclipse.ui.IPartListener#partClosed(org.eclipse.ui.IWorkbenchPart)
     */
    public void partClosed(final IWorkbenchPart part) {
        ECGEclipseSensor.logger.entering(this.getClass().getName(), "partClosed",
            new Object[] {part});

        if (part == null) {
            ECGEclipseSensor.logger.log(Level.FINE,
                "The Parameter part is null. Ignoring event.");

            ECGEclipseSensor.logger.exiting(this.getClass().getName(), "partClosed");

            return;
        }

        if (part instanceof IEditorPart) {

            ECGEclipseSensor.logger.log(ECGLevel.PACKET,
                "An editorClosed event has been recorded.");

            editor_id.setTextContent(String.valueOf(part.hashCode()));
            editor_projectname.setTextContent(ECGEclipseSensor.getProjectnameFromLocation(part.getTitleToolTip()));
            editor_username.setTextContent(this.sensor.username);
            editor_activity.setTextContent("closed");
            editor_editorname.setTextContent(ECGEclipseSensor.getFilenameFromLocation(part.getTitleToolTip()));

            this.sensor.processActivity("msdt.editor.xsd",  
                    this.sensor.xmlDocumentSerializer.writeToString(msdt_editor_doc));

            /* TODO old code, remove if obsolete
            processActivity(
                "msdt.editor.xsd",
                "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                    + ECGEclipseSensor.this.username
                    + "</username><projectname>"
                    + getProjectnameFromLocation(part.getTitleToolTip())
                    + "</projectname><id>"
                    + part.hashCode()
                    + "</id></commonData><editor><activity>closed</activity><editorname>"
                    + getFilenameFromLocation(part.getTitleToolTip())
                    + "</editorname></editor></microActivity>");
             */
        } else if (part instanceof IViewPart) {
            ECGEclipseSensor.logger.log(ECGLevel.PACKET,
                "A partClosed event has been recorded.");

            part_id.setTextContent(String.valueOf(part.hashCode()));
            part_username.setTextContent(this.sensor.username);
            part_activity.setTextContent("closed");
            part_partname.setTextContent(part.getTitle());

            this.sensor.processActivity("msdt.part.xsd", 
                    this.sensor.xmlDocumentSerializer.writeToString(msdt_part_doc));

            /* TODO old code, remove if obsolete
            processActivity(
                "msdt.part.xsd",
                "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                    + ECGEclipseSensor.this.username
                    + "</username><id>"
                    + part.hashCode()
                    + "</id></commonData><part><activity>closed</activity><partname>"
                    + part.getTitle()
                    + "</partname></part></microActivity>");
             */
        }

        ECGEclipseSensor.logger.exiting(this.getClass().getName(), "partClosed");
    }

    /**
     * @see org.eclipse.ui.IPartListener#partDeactivated(org.eclipse.ui.IWorkbenchPart)
     */
    public void partDeactivated(final IWorkbenchPart part) {
        ECGEclipseSensor.logger.entering(this.getClass().getName(), "partDeactivated",
            new Object[] {part});

        if (part == null) {
            ECGEclipseSensor.logger.log(Level.FINE,
                "The Parameter part is null. Ignoring event.");

            ECGEclipseSensor.logger.exiting(this.getClass().getName(), "partDeactivated");

            return;
        }

        if (part instanceof IEditorPart) {

            ECGEclipseSensor.logger.log(ECGLevel.PACKET,
                "An editorDeactivated event has been recorded.");

            editor_id.setTextContent(String.valueOf(part.hashCode()));
            editor_projectname.setTextContent(ECGEclipseSensor.getProjectnameFromLocation(part.getTitleToolTip()));
            editor_username.setTextContent(this.sensor.username);
            editor_activity.setTextContent("deactivated");
            editor_editorname.setTextContent(ECGEclipseSensor.getFilenameFromLocation(part.getTitleToolTip()));

            this.sensor.processActivity("msdt.editor.xsd", 
                    this.sensor.xmlDocumentSerializer.writeToString(msdt_editor_doc));

            /* TODO old code, remove if obsolete
            processActivity(
                "msdt.editor.xsd",
                "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                    + ECGEclipseSensor.this.username
                    + "</username><projectname>"
                    + getProjectnameFromLocation(part.getTitleToolTip())
                    + "</projectname><id>"
                    + part.hashCode()
                    + "</id></commonData><editor><activity>deactivated</activity><editorname>"
                    + getFilenameFromLocation(part.getTitleToolTip())
                    + "</editorname></editor></microActivity>");
             */
        } else if (part instanceof IViewPart) {
            ECGEclipseSensor.logger.log(ECGLevel.PACKET,
                "A partDeactivated event has been recorded.");

            part_id.setTextContent(String.valueOf(part.hashCode()));
            part_username.setTextContent(this.sensor.username);
            part_activity.setTextContent("deactivated");
            part_partname.setTextContent(part.getTitle());

            this.sensor.processActivity("msdt.part.xsd", 
                    this.sensor.xmlDocumentSerializer.writeToString(msdt_part_doc));

            /* TODO old code, remove if obsolete
            processActivity(
                "msdt.part.xsd",
                "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                    + ECGEclipseSensor.this.username
                    + "</username><id>"
                    + part.hashCode()
                    + "</id></commonData><part><activity>deactivated</activity><partname>"
                    + part.getTitle()
                    + "</partname></part></microActivity>");
             */
        }

        ECGEclipseSensor.logger.exiting(this.getClass().getName(), "partDeactivated");

    }

    /**
     * @see org.eclipse.ui.IPartListener#partOpened(org.eclipse.ui.IWorkbenchPart)
     */
    public void partOpened(final IWorkbenchPart part) {
        ECGEclipseSensor.logger.entering(this.getClass().getName(), "partOpened",
            new Object[] {part});

        if (part == null) {
            ECGEclipseSensor.logger.log(Level.FINE,
                "The Parameter part is null. Ignoring event.");

            ECGEclipseSensor.logger.exiting(this.getClass().getName(), "partOpened");

            return;
        }

        if (part instanceof IEditorPart) {

            ECGEclipseSensor.logger.log(ECGLevel.PACKET,
                "An editorOpened event has been recorded.");
            
            editor_id.setTextContent(String.valueOf(part.hashCode()));
            editor_projectname.setTextContent(ECGEclipseSensor.getProjectnameFromLocation(part.getTitleToolTip()));
            editor_username.setTextContent(this.sensor.username);
            editor_activity.setTextContent("opened");
            editor_editorname.setTextContent(ECGEclipseSensor.getFilenameFromLocation(part.getTitleToolTip()));

            this.sensor.processActivity("msdt.editor.xsd", 
                    this.sensor.xmlDocumentSerializer.writeToString(msdt_editor_doc));

            /* TODO old code, remove if obsolete
            processActivity(
                "msdt.editor.xsd",
                "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                    + ECGEclipseSensor.this.username
                    + "</username><projectname>"
                    + getProjectnameFromLocation(part.getTitleToolTip())
                    + "</projectname><id>"
                    + part.hashCode()
                    + "</id></commonData><editor><activity>opened</activity><editorname>"
                    + getFilenameFromLocation(part.getTitleToolTip())
                    + "</editorname></editor></microActivity>");
             */                

            // TODO The following line is just for exploration
//            	part.getSite().getSelectionProvider().addSelectionChangedListener(new ECGSelectionChangedListener());
            
            if (part instanceof ITextEditor) {
                final ITextEditor textEditor = (ITextEditor) part;
                // Register new CCP actions on this editor
                PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
                    public void run() {
                    	Action action = new ECGTextOperationAction(ECGPartListener.this.sensor, ResourceBundle.getBundle("org.eclipse.ui.texteditor.ConstructedEditorMessages"), "Editor.Cut.", textEditor, ITextOperationTarget.CUT); //$NON-NLS-1$
                		textEditor.getEditorSite().getActionBars().setGlobalActionHandler(ActionFactory.CUT.getId(), action);
                		textEditor.setAction(ITextEditorActionConstants.CUT, action);
                		
                		action = new ECGTextOperationAction(ECGPartListener.this.sensor, ResourceBundle.getBundle("org.eclipse.ui.texteditor.ConstructedEditorMessages"), "Editor.Copy.", textEditor, ITextOperationTarget.COPY); //$NON-NLS-1$
                		textEditor.getEditorSite().getActionBars().setGlobalActionHandler(ActionFactory.COPY.getId(), action);
                		textEditor.setAction(ITextEditorActionConstants.COPY, action);
                		
                		action = new ECGTextOperationAction(ECGPartListener.this.sensor, ResourceBundle.getBundle("org.eclipse.ui.texteditor.ConstructedEditorMessages"), "Editor.Paste.", textEditor, ITextOperationTarget.PASTE); //$NON-NLS-1$
                		textEditor.getEditorSite().getActionBars().setGlobalActionHandler(ActionFactory.PASTE.getId(), action);
                		textEditor.setAction(ITextEditorActionConstants.PASTE, action);                        	

                        textEditor.getEditorSite().getActionBars().updateActionBars();
                    }
                });

                // register document listener on opened Editors
                IDocumentProvider provider = textEditor.getDocumentProvider();
                IDocument document = provider.getDocument(textEditor.getEditorInput());
                document.addDocumentListener(this.docListener);
                // TODO The next line is only for exploration (dirty bit flagged)
//                    textEditor.addPropertyListener(new ECGPropertyListener());
                // TODO next line is just for exploration (dirty bit flagged, as well)
//                    provider.addElementStateListener(elementStateListener);
                
                ECGEclipseSensor.logger.log(ECGLevel.PACKET, "A code status event has been recorded.");                    

                codestatus_username.setTextContent(this.sensor.getUsername());
                codestatus_projectname.setTextContent(ECGEclipseSensor.getProjectnameFromLocation(textEditor.getTitleToolTip()));
                codestatus_id.setTextContent(String.valueOf(part.hashCode()));
                codestatus_contents.setNodeValue(document.get());
                codestatus_documentname.setTextContent(ECGEclipseSensor.getFilenameFromLocation(textEditor.getTitleToolTip()));

                this.sensor.processActivity("msdt.codestatus.xsd", 
                    this.sensor.xmlDocumentSerializer.writeToString(msdt_codestatus_doc));                    

                /* TODO old code, remove if obsolete
                processActivity(
                        "msdt.codestatus.xsd",
                        "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                            + getUsername()
                            + "</username><projectname>"
                            + getProjectnameFromLocation(textEditor.getTitleToolTip())
                            + "</projectname><id>"
                            + part.hashCode()
                            + "</id></commonData><codestatus><document><![CDATA["
                            + document.get()
                            + "]" + "]" + "></document><documentname>"
                            + getFilenameFromLocation(textEditor.getTitleToolTip())
                            + "</documentname></codestatus></microActivity>");                    
                 */                
            }


        } else if (part instanceof IViewPart) {

            ECGEclipseSensor.logger.log(ECGLevel.PACKET,
                "A partOpened event has been recorded.");

            part_id.setTextContent(String.valueOf(part.hashCode()));
            part_username.setTextContent(this.sensor.username);
            part_activity.setTextContent("opened");
            part_partname.setTextContent(part.getTitle());

            this.sensor.processActivity("msdt.part.xsd", 
                    this.sensor.xmlDocumentSerializer.writeToString(msdt_part_doc));

            /* TODO old code, remove if obsolete
            processActivity(
                "msdt.part.xsd",
                "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                    + ECGEclipseSensor.this.username
                    + "</username><id>"
                    + part.hashCode()
                    + "</id></commonData><part><activity>opened</activity><partname>"
                    + part.getTitle()
                    + "</partname></part></microActivity>");
             */                
            this.sensor.activeView = (IViewPart)part;
        }

        ECGEclipseSensor.logger.exiting(this.getClass().getName(), "partOpened");
    }

    /**
     * @see org.eclipse.ui.IPartListener#partBroughtToTop(org.eclipse.ui.IWorkbenchPart)
     */
    public void partBroughtToTop(IWorkbenchPart part) {
        // not used
    }
}
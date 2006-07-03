package org.electrocodeogram.sensor.eclipse.editor;

import java.util.ResourceBundle;
import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.jface.text.Assert;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.texteditor.IAbstractTextEditorHelpContextIds;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.TextEditorAction;
import org.electrocodeogram.logging.LogHelper.ECGLevel;
import org.electrocodeogram.sensor.eclipse.ECGEclipseSensor;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
     * Actions for Copy/Cut/Paste events. This action is registered on COPY/CUT/PASTE action
     * ids on every opened TextEditor.
     * 
     * Mainly copied from org.eclipse.jdt.internal.ui.javaeditor.ClipboardOperationAction
     * which replaces the usual CCP actions with enhanced ones
     */
    public final class ECGTextOperationAction extends TextEditorAction {
    	
    	/**
         * 
         */
        private final ECGEclipseSensor sensor;
        /** The text operation code */
    	private int fOperationCode= -1;
    	/** The text operation target */
    	private ITextOperationTarget fOperationTarget;

        private Document msdt_user_doc;
        
        private Element user_username;
        private Element user_projectname;
        private Element user_activity;
        private Element user_param1;
        private Element user_param2;
        private CDATASection user_param2_contents;
        private Element user_param3;
        private CDATASection user_param3_contents;
        
    	
    	/**
    	 * Creates the action.
    	 */
    	public ECGTextOperationAction(ECGEclipseSensor sensor, ResourceBundle bundle, String prefix, ITextEditor editor, int operationCode) {
    		super(bundle, prefix, editor);
            this.sensor = sensor;
    		fOperationCode= operationCode;
    		
            // initialize DOM skeleton for msdt.editor.xsd
            try {
                msdt_user_doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
                Element user_microactivity = msdt_user_doc.createElement("microActivity");                
                Element user_commondata = msdt_user_doc.createElement("commonData");
                Element user_user = msdt_user_doc.createElement("user");
                user_username = msdt_user_doc.createElement("username");
                user_projectname = msdt_user_doc.createElement("projectname");
                user_activity = msdt_user_doc.createElement("activity");
                user_param1 = msdt_user_doc.createElement("param1");
                user_param2 = msdt_user_doc.createElement("param2");
                user_param2_contents = msdt_user_doc.createCDATASection("");
                user_param3 = msdt_user_doc.createElement("param3");
                user_param3_contents = msdt_user_doc.createCDATASection("");

                msdt_user_doc.appendChild(user_microactivity);
                  user_microactivity.appendChild(user_commondata);
                    user_commondata.appendChild(user_username);
                    user_commondata.appendChild(user_projectname);
                  user_microactivity.appendChild(user_user);
                    user_user.appendChild(user_activity);
                    user_user.appendChild(user_param1);
                    user_user.appendChild(user_param2);
                      user_param2.appendChild(user_param2_contents);
                    user_user.appendChild(user_param3);
                      user_param3.appendChild(user_param3_contents);
            } catch (ParserConfigurationException e) {
                ECGEclipseSensor.logger.log(Level.SEVERE,
                    "Could not instantiate the DOM Document in ECGTextOperationAction.");
                ECGEclipseSensor.logger.log(Level.FINE, e.getMessage());
            }

            // Register action
    		if (operationCode == ITextOperationTarget.CUT) {
    			setHelpContextId(IAbstractTextEditorHelpContextIds.CUT_ACTION);
    			setActionDefinitionId(ITextEditorActionDefinitionIds.CUT);
    		} else if (operationCode == ITextOperationTarget.COPY) {
    			setHelpContextId(IAbstractTextEditorHelpContextIds.COPY_ACTION);
    			setActionDefinitionId(ITextEditorActionDefinitionIds.COPY);
    		} else if (operationCode == ITextOperationTarget.PASTE) {
    			setHelpContextId(IAbstractTextEditorHelpContextIds.PASTE_ACTION);
    			setActionDefinitionId(ITextEditorActionDefinitionIds.PASTE);
    		} else {
    			Assert.isTrue(false, "Invalid operation code"); //$NON-NLS-1$
    		}
    		update();
    	}
    	
    	private boolean isReadOnlyOperation() {
    		return fOperationCode == ITextOperationTarget.COPY;
    	}

    	
    	/* (non-Javadoc)
    	 * @see org.eclipse.jface.action.IAction#run()
    	 */
    	public void run() {
    		if (fOperationCode == -1 || fOperationTarget == null)
    			return;
    			
    		ITextEditor editor= getTextEditor();

    		if (editor == null)
    			return;

    		if (!isReadOnlyOperation() && !validateEditorInputState())
    			return;

    		Clipboard clipboard= new Clipboard(getDisplay());
    		TextTransfer textTransfer = TextTransfer.getInstance();
    		ISelection sel = editor.getSelectionProvider().getSelection();
    		String selection = (sel instanceof TextSelection ? ((TextSelection)sel).getText() : "");
    		fOperationTarget.doOperation(fOperationCode);
    		if (fOperationCode == ITextOperationTarget.CUT) {
    			ECGEclipseSensor.logger.log(ECGLevel.PACKET, "A Cut operation has been recorded");

                user_projectname.setTextContent(ECGEclipseSensor.getProjectnameFromLocation(editor.getTitleToolTip()));
                user_username.setTextContent(this.sensor.getUsername());
                user_activity.setTextContent("cut");
                user_param1.setTextContent(ECGEclipseSensor.getFilenameFromLocation(editor.getTitleToolTip()));
                user_param2_contents.setNodeValue(selection);
                user_param3_contents.setNodeValue("");

                this.sensor.processActivity("msdt.user.xsd", 
                        this.sensor.xmlDocumentSerializer.writeToString(msdt_user_doc));
/*
                processActivity(
                    "msdt.user.xsd",
                    "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                        + getUsername()
                        + "</username><projectname>"
                        + getProjectnameFromLocation(editor.getTitleToolTip())
                        + "</projectname></commonData><user><activity>cut</activity><param1>"
                        + getFilenameFromLocation(editor.getTitleToolTip())
                        + "</param1><param2><![CDATA["
                        + selection
                        + "]" + "]" + "></param2></user></microActivity>");
*/
    		} else if (fOperationCode == ITextOperationTarget.COPY) {
    			ECGEclipseSensor.logger.log(ECGLevel.PACKET, "A Copy operation has been recorded");

                user_projectname.setTextContent(ECGEclipseSensor.getProjectnameFromLocation(editor.getTitleToolTip()));
                user_username.setTextContent(this.sensor.getUsername());
                user_activity.setTextContent("copy");
                user_param1.setTextContent(ECGEclipseSensor.getFilenameFromLocation(editor.getTitleToolTip()));
                user_param2_contents.setNodeValue(selection);
                user_param3_contents.setNodeValue("");

                this.sensor.processActivity("msdt.user.xsd", 
                        this.sensor.xmlDocumentSerializer.writeToString(msdt_user_doc));
/*
                processActivity(
                    "msdt.user.xsd",
                    "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                        + getUsername()
                        + "</username><projectname>"
                        + getProjectnameFromLocation(editor.getTitleToolTip())
                        + "</projectname></commonData><user><activity>copy</activity><param1>"
                        + getFilenameFromLocation(editor.getTitleToolTip())
                        + "</param1><param2><![CDATA["
                        + selection
                        + "]" + "]" + "></param2></user></microActivity>");
*/
    		} else if (fOperationCode == ITextOperationTarget.PASTE) {
    			ECGEclipseSensor.logger.log(ECGLevel.PACKET, "A Paste operation has been recorded");

                Object clipboardContents = clipboard.getContents(textTransfer);
                user_projectname.setTextContent(ECGEclipseSensor.getProjectnameFromLocation(editor.getTitleToolTip()));
                user_username.setTextContent(this.sensor.getUsername());
                user_activity.setTextContent("paste");
                user_param1.setTextContent(ECGEclipseSensor.getFilenameFromLocation(editor.getTitleToolTip()));
                user_param2_contents.setNodeValue(selection);
                user_param3_contents.setNodeValue(
                        (clipboardContents != null ? clipboardContents.toString() : ""));

                this.sensor.processActivity("msdt.user.xsd", 
                        this.sensor.xmlDocumentSerializer.writeToString(msdt_user_doc));
/*
                processActivity(
                    "msdt.user.xsd",
                    "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                        + getUsername()
                        + "</username><projectname>"
                        + getProjectnameFromLocation(editor.getTitleToolTip())
                        + "</projectname></commonData><user><activity>paste</activity><param1>"
                        + getFilenameFromLocation(editor.getTitleToolTip())
                        + "</param1><param2><![CDATA["
                        + selection
                        + "]" + "]" + "></param2><param3><![CDATA["
                        + clipboard.getContents(textTransfer)
                        + "]" + "]" + "></param3></user></microActivity>");
*/
    		}
    		clipboard.dispose();

    	}
    	
    	private Shell getShell() {
    		ITextEditor editor= getTextEditor();
    		if (editor != null) {
    			IWorkbenchPartSite site= editor.getSite();
    			Shell shell= site.getShell();
    			if (shell != null && !shell.isDisposed()) {
    				return shell;
    			}
    		}
    		return null;
    	}
    	
    	private Display getDisplay() {
    		Shell shell= getShell();
    		if (shell != null) {
    			return shell.getDisplay();
    		}
    		return null;
    	}
    	
    	
    	/* (non-Javadoc)
    	 * @see org.eclipse.ui.texteditor.IUpdate#update()
    	 */
    	public void update() {
    		super.update();
    		
    		if (!isReadOnlyOperation() && !canModifyEditor()) {
    			setEnabled(false);
    			return;
    		}
    		
    		ITextEditor editor= getTextEditor();
    		if (fOperationTarget == null && editor!= null && fOperationCode != -1)
    			fOperationTarget= (ITextOperationTarget) editor.getAdapter(ITextOperationTarget.class);
    			
    		boolean isEnabled= (fOperationTarget != null && fOperationTarget.canDoOperation(fOperationCode));
    		setEnabled(isEnabled);
    	}
    	
    	/* (non-Javadoc)
    	 * @see org.eclipse.ui.texteditor.TextEditorAction#setEditor(org.eclipse.ui.texteditor.ITextEditor)
    	 */
    	public void setEditor(ITextEditor editor) {
    		super.setEditor(editor);
    		fOperationTarget= null;
    	}
    	

    }
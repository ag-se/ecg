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
import org.electrocodeogram.event.CommonData;
import org.electrocodeogram.event.MicroActivity;
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

        private Element textop_activity;
        private Element textop_editorname;
        private Element textop_selection;
        private CDATASection textop_selection_contents;
        private Element textop_clipboard;
        private CDATASection textop_clipboard_contents;
        private Element textop_startline;
        private Element textop_endline;
        private Element textop_offset;
        private MicroActivity microActivity;
    	
    	/**
    	 * Creates the action.
    	 */
    	public ECGTextOperationAction(ECGEclipseSensor sensor, ResourceBundle bundle, String prefix, ITextEditor editor, int operationCode) {
    		super(bundle, prefix, editor);
            this.sensor = sensor;
    		fOperationCode= operationCode;
    		
            microActivity = new MicroActivity();

            Document doc = microActivity.getMicroActivityDoc();
            Element textop = doc.createElement("textoperation");
            textop_activity = doc.createElement("activity");
            textop_editorname = doc.createElement("editorname");
            textop_selection = doc.createElement("selection");
            textop_selection_contents = doc.createCDATASection("");
            textop_clipboard = doc.createElement("clipboard");
            textop_clipboard_contents = doc.createCDATASection("");
            textop_startline = doc.createElement("startline");
            textop_endline = doc.createElement("endline");
            textop_offset = doc.createElement("offset");

            textop.appendChild(textop_activity);
            textop.appendChild(textop_editorname);
            textop.appendChild(textop_selection);
            textop_selection.appendChild(textop_selection_contents);
            textop.appendChild(textop_clipboard);
            textop.appendChild(textop_startline);
            textop.appendChild(textop_endline);
            textop.appendChild(textop_offset);
            
            microActivity.setCustomElement(textop);            
                                                    
            CommonData commonData = microActivity.getCommonData();
            commonData.setUsername(sensor.getUsername());
            commonData.setVersion(1); // 1 is default
            commonData.setCreator(ECGEclipseSensor.CREATOR); 

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

            CommonData commonData = microActivity.getCommonData();
            commonData.setProjectname(ECGEclipseSensor.getProjectnameFromPart(editor));
            commonData.setId(String.valueOf(editor.hashCode()));

            Clipboard clipboard= new Clipboard(getDisplay());
    		TextTransfer textTransfer = TextTransfer.getInstance();
    		ISelection sel = editor.getSelectionProvider().getSelection();
            String selection = "";
            int startline = -1;
            int endline = -1;
            int offset = -1;
            if (sel instanceof TextSelection) {
                TextSelection textsel = (TextSelection)sel;
                selection = textsel.getText();
                startline = textsel.getStartLine();
                endline = textsel.getEndLine();
                offset = textsel.getOffset();
            }
            textop_startline.setTextContent(Integer.toString(startline));
            textop_endline.setTextContent(Integer.toString(endline));
            textop_offset.setTextContent(Integer.toString(offset));
            
    		fOperationTarget.doOperation(fOperationCode);
    		
            if (fOperationCode == ITextOperationTarget.CUT) {
    			ECGEclipseSensor.logger.log(ECGLevel.PACKET, "A Cut operation has been recorded");

                textop_activity.setTextContent("cut");
                textop_editorname.setTextContent(ECGEclipseSensor.getFilenameFromPart(editor));
                textop_selection_contents.setNodeValue(selection);

            } else if (fOperationCode == ITextOperationTarget.COPY) {
    			ECGEclipseSensor.logger.log(ECGLevel.PACKET, "A Copy operation has been recorded");

                textop_activity.setTextContent("copy");
                textop_editorname.setTextContent(ECGEclipseSensor.getFilenameFromPart(editor));
                textop_selection_contents.setNodeValue(selection);

            } else if (fOperationCode == ITextOperationTarget.PASTE) {
    			ECGEclipseSensor.logger.log(ECGLevel.PACKET, "A Paste operation has been recorded");

                Object clipboardContents = clipboard.getContents(textTransfer);
                textop_activity.setTextContent("paste");
                textop_editorname.setTextContent(ECGEclipseSensor.getFilenameFromPart(editor));
                textop_selection_contents.setNodeValue(selection);
                textop_clipboard.appendChild(textop_clipboard_contents);
                textop_clipboard_contents.setNodeValue(
                        (clipboardContents != null ? clipboardContents.toString() : ""));

    		}

            this.sensor.processActivity("msdt.textoperation.xsd", 
                    microActivity.getSerializedMicroActivity());
            
            if (textop_clipboard.hasChildNodes())
                textop_clipboard.removeChild(textop_clipboard_contents);


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
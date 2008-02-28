package org.electrocodeogram.cpc.sensor.listener;


import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.Assert;
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
import org.electrocodeogram.cpc.core.CPCCorePlugin;
import org.electrocodeogram.cpc.core.api.hub.event.EclipseCutCopyPasteEvent;
import org.electrocodeogram.cpc.core.utils.CoreStringUtils;
import org.electrocodeogram.cpc.core.utils.CoreUtils;


/**
     * Actions for Copy/Cut/Paste events. This action is registered on COPY/CUT/PASTE action
     * ids on every opened TextEditor.
     * 
     * Mainly copied from org.eclipse.jdt.internal.ui.javaeditor.ClipboardOperationAction
     * which replaces the usual CCP actions with enhanced ones
     */
public final class CPCTextOperationAction extends TextEditorAction
{
	private static final Log log = LogFactory.getLog(CPCTextOperationAction.class);

	/** The text operation code */
	private int fOperationCode = -1;

	/** The text operation target */
	private ITextOperationTarget fOperationTarget;

	/**
	 * Creates the action.
	 */
	public CPCTextOperationAction(ResourceBundle bundle, String prefix, ITextEditor editor, int operationCode)
	{
		super(bundle, prefix, editor);
		fOperationCode = operationCode;

		// Register action
		if (operationCode == ITextOperationTarget.CUT)
		{
			setHelpContextId(IAbstractTextEditorHelpContextIds.CUT_ACTION);
			setActionDefinitionId(ITextEditorActionDefinitionIds.CUT);
		}
		else if (operationCode == ITextOperationTarget.COPY)
		{
			setHelpContextId(IAbstractTextEditorHelpContextIds.COPY_ACTION);
			setActionDefinitionId(ITextEditorActionDefinitionIds.COPY);
		}
		else if (operationCode == ITextOperationTarget.PASTE)
		{
			setHelpContextId(IAbstractTextEditorHelpContextIds.PASTE_ACTION);
			setActionDefinitionId(ITextEditorActionDefinitionIds.PASTE);
		}
		else
		{
			Assert.isTrue(false, "Invalid operation code"); //$NON-NLS-1$
		}
		update();
	}

	private boolean isReadOnlyOperation()
	{
		return fOperationCode == ITextOperationTarget.COPY;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	@Override
	public void run()
	{
		if (fOperationCode == -1 || fOperationTarget == null)
			return;

		if (log.isTraceEnabled())
			log.trace("run() - fOperationCode: " + fOperationCode + ", fOperationTarget: " + fOperationTarget);

		ITextEditor editor = getTextEditor();

		if (editor == null)
			return;

		if (!isReadOnlyOperation() && !validateEditorInputState())
			return;

		//get the document listener registered for this location
		//CPCSingleDocumentListener docListener = (CPCSingleDocumentListener) CPCSingleDocumentListener
		//		.getDocumentListenerForEditor(editor);
		//CPCSingleDocumentListener docListener = CPCDocumentListenerRegistry.lookupDocumentListenerForLocation(CoreUtils
		//		.getLocationFromPart(editor));

		//before we send out any Cut/Copy/Paste event, we have to purge any currently cached
		//diff event data, if there currently is a docListener registered
		//if (docListener != null)
		//	docListener.fireDiffTimer();

		EclipseCutCopyPasteEvent newEvent = new EclipseCutCopyPasteEvent(CoreUtils.getUsername(), CoreUtils
				.getProjectnameFromPart(editor));

		Clipboard clipboard = new Clipboard(getDisplay());
		TextTransfer textTransfer = TextTransfer.getInstance();
		ISelection sel = editor.getSelectionProvider().getSelection();
		String selection = "";
		int offset = -1;
		if (sel instanceof TextSelection)
		{
			TextSelection textsel = (TextSelection) sel;
			selection = textsel.getText();
			offset = textsel.getOffset();
		}

		newEvent.setOffset(offset);

		//events always need to be fully initialized.
		//the following code may not always do that
		//=> we do some basic initialization here
		//newEvent.setType(EclipseCutCopyPasteEvent.Type.NULL);
		newEvent.setFilePath("");
		newEvent.setSelection("");
		newEvent.setClipboard("");

		if (fOperationCode == ITextOperationTarget.CUT)
		{
			newEvent.setType(EclipseCutCopyPasteEvent.Type.CUT);
			newEvent.setFilePath(CoreUtils.getProjectRelativePathFromPart(editor));
			newEvent.setSelection(selection);
		}
		else if (fOperationCode == ITextOperationTarget.COPY)
		{
			newEvent.setType(EclipseCutCopyPasteEvent.Type.COPY);
			newEvent.setFilePath(CoreUtils.getProjectRelativePathFromPart(editor));
			newEvent.setSelection(selection);
		}
		else if (fOperationCode == ITextOperationTarget.PASTE)
		{
			Object clipboardContents = clipboard.getContents(textTransfer);

			newEvent.setType(EclipseCutCopyPasteEvent.Type.PASTE);
			newEvent.setFilePath(CoreUtils.getProjectRelativePathFromPart(editor));
			newEvent.setSelection(selection);
			newEvent.setClipboard(((clipboardContents != null ? clipboardContents.toString() : "")));
		}
		else
		{
			//does this ever happen?
			//if it does, it means that we're sending out empty events.
			log.error("run() - unknown fOperationCode: " + fOperationCode, new Throwable());
		}

		log.debug("PRE: "
				+ CoreStringUtils.truncateString(editor.getDocumentProvider().getDocument(editor.getEditorInput())
						.get()));

		// perform text operation
		fOperationTarget.doOperation(fOperationCode);

		log.debug("POST: "
				+ CoreStringUtils.truncateString(editor.getDocumentProvider().getDocument(editor.getEditorInput())
						.get()));

		//some event listeners may require the entire source text for their processing
		newEvent.setEditorContent(editor.getDocumentProvider().getDocument(editor.getEditorInput()).get());

		CPCCorePlugin.getEventHubRegistry().dispatch(newEvent);

		// perform text operation
		//fOperationTarget.doOperation(fOperationCode);

		/*
		if (fOperationCode == ITextOperationTarget.PASTE)
		{
			// in case of paste, immediately report code change and code diff events
			//TODO: do we need to send a diff event here? The diff event buffer is probably still empty!
		}*/

		clipboard.dispose();

	}

	private Shell getShell()
	{
		ITextEditor editor = getTextEditor();
		if (editor != null)
		{
			IWorkbenchPartSite site = editor.getSite();
			Shell shell = site.getShell();
			if (shell != null && !shell.isDisposed())
			{
				return shell;
			}
		}
		return null;
	}

	private Display getDisplay()
	{
		Shell shell = getShell();
		if (shell != null)
		{
			return shell.getDisplay();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.IUpdate#update()
	 */
	@Override
	public void update()
	{
		super.update();

		if (!isReadOnlyOperation() && !canModifyEditor())
		{
			setEnabled(false);
			return;
		}

		ITextEditor editor = getTextEditor();
		if (fOperationTarget == null && editor != null && fOperationCode != -1)
			fOperationTarget = (ITextOperationTarget) editor.getAdapter(ITextOperationTarget.class);

		boolean isEnabled = (fOperationTarget != null && fOperationTarget.canDoOperation(fOperationCode));
		setEnabled(isEnabled);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.TextEditorAction#setEditor(org.eclipse.ui.texteditor.ITextEditor)
	 */
	@Override
	public void setEditor(ITextEditor editor)
	{
		super.setEditor(editor);
		fOperationTarget = null;
	}

}

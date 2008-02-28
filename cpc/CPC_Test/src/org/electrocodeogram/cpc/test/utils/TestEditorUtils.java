package org.electrocodeogram.cpc.test.utils;


import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.ErrorEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;
import org.electrocodeogram.cpc.core.utils.CoreUtils;
import org.electrocodeogram.cpc.sensor.listener.CPCTextOperationAction;


public class TestEditorUtils
{
	private static final Log log = LogFactory.getLog(TestEditorUtils.class);

	private ITextEditor textEditor;
	private CPCTextOperationAction cutAction;
	private CPCTextOperationAction copyAction;
	private CPCTextOperationAction pasteAction;

	public TestEditorUtils(ITextEditor textEditor)
	{
		this.textEditor = textEditor;

		//generate some actions 
		cutAction = new CPCTextOperationAction(ResourceBundle
				.getBundle("org.eclipse.ui.texteditor.ConstructedEditorMessages"), "Editor.Cut.", textEditor,
				ITextOperationTarget.CUT); //$NON-NLS-1$

		copyAction = new CPCTextOperationAction(ResourceBundle
				.getBundle("org.eclipse.ui.texteditor.ConstructedEditorMessages"), "Editor.Copy.", textEditor,
				ITextOperationTarget.COPY); //$NON-NLS-1$

		pasteAction = new CPCTextOperationAction(ResourceBundle
				.getBundle("org.eclipse.ui.texteditor.ConstructedEditorMessages"), "Editor.Paste.", textEditor,
				ITextOperationTarget.PASTE); //$NON-NLS-1$

	}

	public void cutAction(int offset, int length)
	{
		//select text in editor
		textEditor.selectAndReveal(offset, length);

		//do the cut
		cutAction.run();
	}

	/**
	 * Simulates a copy action on the given text selection.
	 * 
	 * @param offset start offset
	 * @param length length
	 */
	public void copyAction(int offset, int length)
	{
		//select text in editor
		textEditor.selectAndReveal(offset, length);

		//do the copy
		copyAction.run();
	}

	public void pasteAction(int offset, int length)
	{
		//select text in editor
		textEditor.selectAndReveal(offset, length);

		//do the paste
		pasteAction.run();
	}

	/**
	 * Checks whether the given editor is an {@link ITextEditor}.<br/>
	 * Otherwise an assertion error is thrown. If the given editor instance is an
	 * {@link ErrorEditorPart} its internal {@link IStatus} error element is
	 * extracted and included in the assertion error message.
	 * 
	 * @param editor the editor reference to check, may be NULL.
	 */
	public static void checkEditor(IEditorPart editor)
	{
		assertNotNull("unable to obtain editor", editor);

		//special error case when nested exception was thrown
		if (editor instanceof ErrorEditorPart)
		{
			log.debug("checkEditor() - editor open operation failed - " + editor);

			try
			{
				Field field = editor.getClass().getDeclaredField("error");
				field.setAccessible(true);
				Object status = field.get(editor);
				if (status == null)
				{
					log.debug("checkEditor() - ErrorEditorPart contained no status.");
					assertTrue("unable to obtain editor - no additional information availabe - got: " + editor, false);
					return;
				}

				assertTrue("ErrorEditorPart has invalid status object: " + status, status instanceof IStatus);

				IStatus istatus = (IStatus) status;

				assertTrue("unable to obtain editor - status: " + istatus + ", children: "
						+ CoreUtils.arrayToString(istatus.getChildren()) + ", editor: " + editor, false);
			}
			catch (Exception e)
			{
				log.warn("checkEditor() - unable to obtain status for ErrorEditorPart - " + editor + " - " + e, e);
				assertTrue("unable to obtain editor - no additional information available - got: " + editor
						+ ", extra data retrival failed with: " + e, false);
			}
		}
		else
		{
			//otherwise it should be a text editor
			assertTrue("text editor expected - got: " + editor, editor instanceof ITextEditor);
		}
	}

	public static IEditorPart openEditor(IFile file) throws PartInitException, JavaModelException
	{
		IJavaElement element = JavaCore.createCompilationUnitFrom(file);
		return JavaUI.openInEditor(element, true, true);
	}

	public static IEditorPart openEditorNonJavaFile(IFile file) throws PartInitException, JavaModelException
	{
		return IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file, true, true);
	}

	public static void closeEditor(IEditorPart editor)
	{
		IWorkbenchPartSite site;
		IWorkbenchPage page;
		if (editor != null && (site = editor.getSite()) != null && (page = site.getPage()) != null)
			page.closeEditor(editor, false);
	}

	public static void closeAllEditors()
	{
		IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
		for (int i = 0; i < windows.length; i++)
		{
			IWorkbenchPage[] pages = windows[i].getPages();
			for (int j = 0; j < pages.length; j++)
			{
				IEditorReference[] editorReferences = pages[j].getEditorReferences();
				for (int k = 0; k < editorReferences.length; k++)
					closeEditor(editorReferences[k].getEditor(false));
			}
		}
	}

}

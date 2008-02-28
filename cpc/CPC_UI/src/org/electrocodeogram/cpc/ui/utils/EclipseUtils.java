/*
 * Copyright (c) 2004 Andrei Loskutov. All rights reserved. This program and the accompanying materials are made available under the terms of the BSD License which accompanies this distribution, and is available at http://www.opensource.org/licenses/bsd-license.php Contributor: Andrei Loskutov - initial API and implementation
 */
package org.electrocodeogram.cpc.ui.utils;


import java.util.List;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.texteditor.ITextEditor;
import org.electrocodeogram.cpc.ui.CPCUiPlugin;


/**
 * Partly a copy of de.loskutov.bco.BytecodeOutlinePlugin
 * 
 * @author Andrei
 */
public class EclipseUtils
{

	/**
	 * don't call me ;)
	 */
	private EclipseUtils()
	{
		super();
	}

	/**
	 * @return current active editor in workbench
	 */
	public static IEditorPart getActiveEditor()
	{
		IWorkbenchWindow window = CPCUiPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow();
		if (window != null)
		{
			IWorkbenchPage page = window.getActivePage();
			if (page != null)
			{
				return page.getActiveEditor();
			}
		}
		return null;
	}

	/**
	 * @param part
	 * @return editor input as IJavaElement
	 */
	public static IJavaElement getJavaInput(IEditorPart part)
	{
		IEditorInput editorInput = part.getEditorInput();
		if (editorInput != null)
		{
			IJavaElement input = (IJavaElement) editorInput.getAdapter(IJavaElement.class);
			return input;
		}
		return null;
	}

	/**
	 * @param editor
	 * @param offset
	 * @param length
	 */
	public static void selectInEditor(ITextEditor editor, int offset, int length)
	{
		IEditorPart active = getActiveEditor();
		if (active != editor)
		{
			editor.getSite().getPage().activate(editor);
		}
		editor.selectAndReveal(offset, length);
	}

	/**
	 * @param selectionProvider
	 * @return TextSelection or null, if provider does not provide TextSelection's
	 */
	public static ITextSelection getSelection(ISelectionProvider selectionProvider)
	{
		ISelection selection = selectionProvider.getSelection();
		if (selection instanceof ITextSelection)
		{
			return (ITextSelection) selection;
		}
		return null;
	}

	/**
	 * @param resource
	 * @return full package name in default java notation (with dots)
	 */
	public static String getJavaPackageName(IJavaElement resource)
	{
		String name = resource == null ? null : resource.getElementName(); //$NON-NLS-1$
		if (name == null)
		{
			return ""; //$NON-NLS-1$
		}
		int type = resource.getElementType();
		if (type == IJavaElement.PACKAGE_FRAGMENT || type == IJavaElement.PACKAGE_FRAGMENT_ROOT)
		{
			return name;
		}
		IJavaElement ancestor = resource.getAncestor(IJavaElement.PACKAGE_FRAGMENT);
		if (ancestor != null)
		{
			return ancestor.getElementName();
		}
		return ""; //$NON-NLS-1$
	}

	/**
	 * Takes two {@link IStructuredSelection}s and returns whether they select the same elements.<br/>
	 * <br/>
	 * NOTE: Selections are treated like sets here. This method is likely to break if any elements occur more
	 * 		than once within a selection.
	 * 
	 * @param oldSelection first selection to compare, never null.
	 * @param newSelection second selection to compare, never null.
	 * @return true if both selections represent the same selected elements.
	 */
	public static boolean selectionEqual(IStructuredSelection oldSelection, IStructuredSelection newSelection)
	{
		assert (oldSelection != null && newSelection != null);

		if (oldSelection.size() != newSelection.size())
			return false;

		/*
		 * This isn't the fastest way if implementing this, however, selections are expected
		 * to be pretty small in most cases.
		 */

		List<?> oldElems = oldSelection.toList();
		List<?> newElems = newSelection.toList();

		for (Object oldElem : oldElems)
			if (!newElems.contains(oldElem))
				return false;

		for (Object newElem : newElems)
			if (!oldElems.contains(newElem))
				return false;

		return true;
	}
}

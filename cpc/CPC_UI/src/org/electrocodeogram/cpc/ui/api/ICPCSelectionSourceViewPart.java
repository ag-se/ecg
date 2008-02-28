package org.electrocodeogram.cpc.ui.api;


import org.eclipse.jface.viewers.IStructuredSelection;


/**
 * An interface implemented by all CPC UI views which have one central viewer component.<br/>
 * This interface can be used to obtain information about the currently selected elements.
 * 
 * @author vw
 */
public interface ICPCSelectionSourceViewPart
{
	public IStructuredSelection getSelection();
}

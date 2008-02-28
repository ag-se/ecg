package org.electrocodeogram.cpc.ui.data;


/**
 * CPC UI interface for views which need to be updated about clone data changes in the {@link CloneDataModel}. 
 * 
 * @author vw
 * 
 * @see CloneDataModel
 * @see CloneDataChange
 */
public interface ICloneDataChangeListener
{
	/**
	 * Listener method which will be called for every {@link CloneDataChange} event.<br/>
	 * <br/>
	 * NOTE: This method may be called concurrently from different threads. Access to SWT elements may need to
	 * be explicitly dispatched to the SWT main thread.
	 * 
	 * @param event the clone data change event to process, never null.
	 */
	public void cloneDataChanged(CloneDataChange event);
}

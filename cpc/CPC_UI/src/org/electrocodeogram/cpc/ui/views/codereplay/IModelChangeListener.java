package org.electrocodeogram.cpc.ui.views.codereplay;

/**
 * IModelChangeListener must be implemented in order to register at the DataProvider
 * as a receiver of {@link ModelChangeEvent}s. (used for datamodel -> logic communication)
 * 
 * @author marco kranz
 */
public interface IModelChangeListener {

	/**
	 * @param event the event describing the model change
	 */
	void modelChange(ModelChangeEvent event);
}

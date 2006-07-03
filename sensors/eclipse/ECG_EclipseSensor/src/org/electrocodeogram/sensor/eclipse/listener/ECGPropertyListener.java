package org.electrocodeogram.sensor.eclipse.listener;

import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.IWorkbenchPartConstants;
import org.electrocodeogram.logging.LogHelper.ECGLevel;
import org.electrocodeogram.sensor.eclipse.ECGEclipseSensor;

/**
 * This listens on perperty chnages of ITextEditors. Using Properties, 
 * mostly visible properties are controlled, like titles or the * to indicate
 * dirty files.  
 */
public class ECGPropertyListener implements IPropertyListener {

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPropertyListener#propertyChanged(java.lang.Object, int)
	 */
	public void propertyChanged(Object source, int propId) {
		if (propId == IWorkbenchPartConstants.PROP_DIRTY && source instanceof ISaveablePart) {
			ECGEclipseSensor.logger.log(ECGLevel.INFO, "propertyChanged: at " + source + " dirty is now " + String.valueOf(((ISaveablePart)source).isDirty()));
		}
	}
}
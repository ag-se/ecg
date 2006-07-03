package org.electrocodeogram.sensor.eclipse.listener;

import org.eclipse.ui.texteditor.IElementStateListener;
import org.electrocodeogram.logging.LogHelper.ECGLevel;
import org.electrocodeogram.sensor.eclipse.ECGEclipseSensor;

/**
 * TODO Not Used 
 * Reports on FileEditorInput which is a rather weak place to listen to. It's something
 * between the Editor and the File. It allows for listening to the dirty bit of a 
 * Document Provider
 */
public class ECGElementStateListener implements IElementStateListener {

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.IElementStateListener#elementContentAboutToBeReplaced(java.lang.Object)
	 */
	public void elementContentAboutToBeReplaced(Object element) {
//			logger.log(ECGLevel.INFO, "docAboutToBeReplaced: " + element);

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.IElementStateListener#elementContentReplaced(java.lang.Object)
	 */
	public void elementContentReplaced(Object element) {
//			logger.log(ECGLevel.INFO, "docContentReplaced: " + element);

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.IElementStateListener#elementDeleted(java.lang.Object)
	 */
	public void elementDeleted(Object element) {
		ECGEclipseSensor.logger.log(ECGLevel.INFO, "docDeleted: " + element);

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.IElementStateListener#elementDirtyStateChanged(java.lang.Object, boolean)
	 */
	public void elementDirtyStateChanged(Object element, boolean isDirty) {
		ECGEclipseSensor.logger.log(ECGLevel.INFO, "docDirtyStateChanged: " + element);

	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.IElementStateListener#elementMoved(java.lang.Object, java.lang.Object)
	 */
	public void elementMoved(Object originalElement, Object movedElement) {
		ECGEclipseSensor.logger.log(ECGLevel.INFO, "docMoved: " + originalElement + "->" + movedElement);

	}

}
package org.electrocodeogram.sensor.eclipse.listener;

import org.eclipse.core.filebuffers.IFileBuffer;
import org.eclipse.core.filebuffers.IFileBufferListener;
import org.eclipse.core.runtime.IPath;
import org.electrocodeogram.logging.LogHelper.ECGLevel;
import org.electrocodeogram.sensor.eclipse.ECGEclipseSensor;

/**
 * FileBuffers are internal Caches for the actual typed contents of a displayed
 * (text) file. On Save, the underlying file is overwritten by the buffers contents.
 * This is a good place to recognize ditry events and also changes on the
 * underlying file 
 *
 */
public class ECGFileBufferListener implements IFileBufferListener {

	/**
     * 
     */
    private final ECGEclipseSensor sensor;

    /**
     * @param sensor
     */
    public ECGFileBufferListener(ECGEclipseSensor sensor) {
        this.sensor = sensor;
    }

    /* (non-Javadoc)
	 * @see org.eclipse.core.filebuffers.IFileBufferListener#bufferContentAboutToBeReplaced(org.eclipse.core.filebuffers.IFileBuffer)
	 */
	public void bufferContentAboutToBeReplaced(IFileBuffer buffer) {
		// not used
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.filebuffers.IFileBufferListener#bufferContentReplaced(org.eclipse.core.filebuffers.IFileBuffer)
	 */
	public void bufferContentReplaced(IFileBuffer buffer) {
		// not used
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.filebuffers.IFileBufferListener#bufferCreated(org.eclipse.core.filebuffers.IFileBuffer)
	 * 
	 */
	// is nix besser als part opened
	public void bufferCreated(IFileBuffer buffer) {
		// not used
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.filebuffers.IFileBufferListener#bufferDisposed(org.eclipse.core.filebuffers.IFileBuffer)
	 */
	public void bufferDisposed(IFileBuffer buffer) {
		// not used
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.filebuffers.IFileBufferListener#stateChangeFailed(org.eclipse.core.filebuffers.IFileBuffer)
	 */
	public void stateChangeFailed(IFileBuffer buffer) {
		// not used
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.filebuffers.IFileBufferListener#stateChanging(org.eclipse.core.filebuffers.IFileBuffer)
	 */
	public void stateChanging(IFileBuffer buffer) {
		// not used
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.filebuffers.IFileBufferListener#stateValidationChanged(org.eclipse.core.filebuffers.IFileBuffer, boolean)
	 */
	public void stateValidationChanged(IFileBuffer buffer, boolean isStateValidated) {
		// not used
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.filebuffers.IFileBufferListener#dirtyStateChanged(org.eclipse.core.filebuffers.IFileBuffer, boolean)
	 */
	public void dirtyStateChanged(IFileBuffer buffer, boolean isDirty) {

        ECGEclipseSensor.logger.entering(this.getClass().getName(), "dirtyStateChanged", 
                new Object[] {buffer, Boolean.valueOf(isDirty)});
		
        if (!isDirty) {

        	ECGEclipseSensor.logger.log(ECGLevel.PACKET, "A resourceSaved event has been recorded.");

            this.sensor.processActivity(
                "msdt.resource.xsd",
                "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                    + this.sensor.username
                    + "</username><projectname>"
                    + ECGEclipseSensor.getProjectnameFromLocation(buffer.getLocation().toString())
                    + "</projectname><id>"
                    + buffer.hashCode()
                    + "</id></commonData><resource><activity>saved</activity><resourcename>"
                    + ECGEclipseSensor.getFilenameFromLocation(buffer.getLocation().toString())
                    + "</resourcename></resource></microActivity>");

        }

        ECGEclipseSensor.logger.exiting(this.getClass().getName(), "dirtyStateChanged");

	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.filebuffers.IFileBufferListener#underlyingFileDeleted(org.eclipse.core.filebuffers.IFileBuffer)
	 */
	public void underlyingFileDeleted(IFileBuffer buffer) {

		ECGEclipseSensor.logger.entering(this.getClass().getName(), "underlyingFileDeleted", new Object[] {buffer});
		
    	ECGEclipseSensor.logger.log(ECGLevel.PACKET, "A resourceDeleted event has been recorded.");

        this.sensor.processActivity(
            "msdt.resource.xsd",
            "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                + this.sensor.username
                + "</username><projectname>"
                + ECGEclipseSensor.getProjectnameFromLocation(buffer.getLocation().toString())
                + "</projectname><id>"
                + buffer.hashCode()
                + "</id></commonData><resource><activity>deleted</activity><resourcename>"
                + ECGEclipseSensor.getFilenameFromLocation(buffer.getLocation().toString())
                + "</resourcename></resource></microActivity>");

        ECGEclipseSensor.logger.exiting(this.getClass().getName(), "underlyingFileDeleted");

	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.filebuffers.IFileBufferListener#underlyingFileMoved(org.eclipse.core.filebuffers.IFileBuffer, org.eclipse.core.runtime.IPath)
	 */
	public void underlyingFileMoved(IFileBuffer buffer, IPath path) {

		ECGEclipseSensor.logger.entering(this.getClass().getName(), "underlyingFileMoved", new Object[] {buffer});
		
    	ECGEclipseSensor.logger.log(ECGLevel.PACKET, "A resourceDeleted event has been recorded.");

        this.sensor.processActivity(
            "msdt.resource.xsd",
            "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                + this.sensor.username
                + "</username><projectname>" 
                + ECGEclipseSensor.getProjectnameFromLocation(buffer.getLocation().toString())
                + "</projectname><id>"
                + buffer.hashCode()
                + "</id></commonData><resource><activity>moved</activity><resourcename>"
                + ECGEclipseSensor.getFilenameFromLocation(buffer.getLocation().toString())
                + "</resourcename></resource></microActivity>");

        ECGEclipseSensor.logger.exiting(this.getClass().getName(), "underlyingFileDeleted");

	}

}
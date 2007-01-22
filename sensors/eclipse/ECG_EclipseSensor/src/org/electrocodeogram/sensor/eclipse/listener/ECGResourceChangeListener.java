package org.electrocodeogram.sensor.eclipse.listener;

import java.util.logging.Level;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.electrocodeogram.logging.LogHelper.ECGLevel;
import org.electrocodeogram.sensor.eclipse.ECGEclipseSensor;

/**
 * This is listeneing for events on resources like files,
 * directories or projects.
 * TODO: This is currently not being used. ResourceListeners are useful for basic file management.
 * It treats every file, folder, etc. equally, but is seldom useful 
 */
class ECGResourceChangeListener implements IResourceChangeListener {

    /**
     * 
     */
    private final ECGEclipseSensor sensor;

    /**
     * @param sensor
     */
    public ECGResourceChangeListener(ECGEclipseSensor sensor) {
        this.sensor = sensor;
    }

    /**
     * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
     */
    public void resourceChanged(final IResourceChangeEvent event) {

        ECGEclipseSensor.logger.entering(this.getClass().getName(), "resourceChanged",
            new Object[] {event});

        if (event == null) {
            ECGEclipseSensor.logger.log(Level.FINE,
                "The Parameter \"event\" is null. Ignoring event.");

            ECGEclipseSensor.logger.exiting(this.getClass().getName(), "resourceChanged");

            return;

        }

        if (event.getType() != IResourceChangeEvent.POST_CHANGE) {
        	// it's just to take for sure that only opst events are processed
        	// the listener itself has been registered on post events only
            ECGEclipseSensor.logger.exiting(this.getClass().getName(), "resourceChanged");
            return;
        }

        IResourceDelta resourceDelta = event.getDelta();

        IResourceDeltaVisitor deltaVisitor = new IResourceDeltaVisitor() {

            public boolean visit(final IResourceDelta delta) {
                ECGEclipseSensor.logger.entering(this.getClass().getName(), "visit",
                    new Object[] {delta});

                if (delta == null) {

                    ECGEclipseSensor.logger.log(Level.FINE,
                        "The Parameter \"delta\" is null. Ignoring event.");

                    ECGEclipseSensor.logger.exiting(this.getClass().getName(), "visit");

                    return false;
                }

                // get the kind of the ResourceChangedEvent
                int kind = delta.getKind();

                // get the resource
                IResource resource = delta.getResource();

                String resourceType = null;

                switch (resource.getType()) {
                    case IResource.ROOT:
                        resourceType = "root";
                        return true;

                    case IResource.PROJECT:
                        resourceType = "project";
                        break;

                    case IResource.FOLDER:
                        resourceType = "folder";
                        break;

                    case IResource.FILE:
                        resourceType = "file";
                        break;

                    default:
                        resourceType = "n.a.";
                        break;

                }

                String resourceChangeKind = null;

                switch (kind) {
                	case IResourceDelta.ADDED:
                		resourceChangeKind = "ADDED";
                		break;
                    case IResourceDelta.REMOVED:
                		resourceChangeKind = "REMOVED";
                		break;
                    case IResourceDelta.CHANGED:
                		resourceChangeKind = "CHANGED";
                		break;
                    default:
                		resourceChangeKind = "n.a.";
            			break;
                }
                
                String resourceChangeFlags = "[";
                
                if ((event.getDelta().getFlags() & IResourceDelta.CONTENT) > 0)
                	resourceChangeFlags += "CONTENT,";
                if ((event.getDelta().getFlags() & IResourceDelta.ENCODING) > 0)
                	resourceChangeFlags += "ENCODING,";
                if ((event.getDelta().getFlags() & IResourceDelta.DESCRIPTION) > 0)
                	resourceChangeFlags += "DESCRIPTION,";
                if ((event.getDelta().getFlags() & IResourceDelta.OPEN) > 0)
                	resourceChangeFlags += "OPEN,";
                if ((event.getDelta().getFlags() & IResourceDelta.TYPE) > 0)
                	resourceChangeFlags += "TYPE,";
                if ((event.getDelta().getFlags() & IResourceDelta.SYNC) > 0)
                	resourceChangeFlags += "SYNC,";
                if ((event.getDelta().getFlags() & IResourceDelta.MARKERS) > 0)
                	resourceChangeFlags += "MARKERS,";
                if ((event.getDelta().getFlags() & IResourceDelta.REPLACED) > 0)
                	resourceChangeFlags += "REPLACED,";
                if ((event.getDelta().getFlags() & IResourceDelta.MOVED_TO) > 0)
                	resourceChangeFlags += "MOVED_TO,";
                if ((event.getDelta().getFlags() & IResourceDelta.MOVED_FROM) > 0)
                	resourceChangeFlags += "MOVED_FROM,";
                resourceChangeFlags += "]";

                if (resource.getType() == IResource.FILE) {
        			ECGEclipseSensor.logger.log(ECGLevel.INFO, "RCE:: " + resourceChangeKind + " " + resourceType + " "
        					+ resourceChangeFlags + "(" + event.getDelta().getFlags() + ")"
                			+ " / " + resource.getName() + "(" + resource.getLocation() + " at "
                			+ resource.getClass() + ")");
                }

                /* Frank's original code to send msdt.resource events. Not used anymore
                    switch (kind) {
                        // a resource has been added
                        case IResourceDelta.ADDED:

                            logger.log(ECGLevel.PACKET,
                                "A resourceAdded event has been recorded.");

                            processActivity(
                                "msdt.resource.xsd",
                                "<?xml version=\"1.0\"?><microActivity><commonData><version>1</version><creator>" + ECGEclipseSensor.CREATOR + "</creator><username>"
                                                + ECGEclipseSensor.this.username
                                                + "</username><projectname>"
                                                + ECGEclipseSensor.this.projectname
                                                + "</projectname></commonData><resource><activity>added</activity><resourcename>"
                                                + resource.getName()
                                                + "</resourcename><resourcetype>"
                                                + resourceType
                                                + "</resourcetype></resource></microActivity>");

                            break;
                        // a resource has been removed
                        case IResourceDelta.REMOVED:

                            logger.log(ECGLevel.PACKET,
                                "A resourceRemoved event has been recorded.");

                            processActivity(
                                "msdt.resource.xsd",
                                "<?xml version=\"1.0\"?><microActivity><commonData><version>1</version><creator>" + ECGEclipseSensor.CREATOR + "</creator><username>"
                                                + ECGEclipseSensor.this.username
                                                + "</username><projectname>"
                                                + ECGEclipseSensor.this.projectname
                                                + "</projectname></commonData><resource><activity>removed</activity><resourcename>"
                                                + resource.getName()
                                                + "</resourcename><resourcetype>"
                                                + resourceType
                                                + "</resourcetype></resource></microActivity>");

                            break;
                        // a resource has been changed
                        case IResourceDelta.CHANGED:

                            // if its a project change, set the name
                            // of the
                            // project to be the name used.
                            if (resource instanceof IProject) {
                                ECGEclipseSensor.this.projectname = resource
                                    .getName();
                            } else {
                                logger
                                    .log(ECGLevel.PACKET,
                                        "A resourceChanged event has been recorded.");

                                processActivity(
                                    "msdt.resource.xsd",
                                    "<?xml version=\"1.0\"?><microActivity><commonData><version>1</version><creator>" + ECGEclipseSensor.CREATOR + "</creator><username>"
                                                    + ECGEclipseSensor.this.username
                                                    + "</username><projectname>"
                                                    + ECGEclipseSensor.this.projectname
                                                    + "</projectname></commonData><resource><activity>changed</activity><resourcename>"
                                                    + resource.getName()
                                                    + "</resourcename><resourcetype>"
                                                    + resourceType
                                                    + "</resourcetype></resource></microActivity>");
                            }
                            break;

                        default:
                            logger.log(ECGLevel.PACKET,
                                "An unknown resource event has been recorded.");
                    }
*/

                ECGEclipseSensor.logger.exiting(this.getClass().getName(), "visit");

                return true;
            }

        };

        try {
            resourceDelta.accept(deltaVisitor);
        } catch (CoreException e) {
            ECGEclipseSensor.logger
                .log(Level.SEVERE,
                    "An Eclipse internal Exception occured during resourceEvent analysis.");

            ECGEclipseSensor.logger.log(Level.FINEST, e.getMessage());
        }

        ECGEclipseSensor.logger.exiting(this.getClass().getName(), "resourceChanged");

    }
}
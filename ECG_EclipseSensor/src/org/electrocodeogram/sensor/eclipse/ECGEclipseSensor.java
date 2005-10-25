/*
 * Class: ECGEclipseSensor
 * Version: 1.0
 * Date: 16.10.2005
 * By: Frank@Schlesinger.com
 */

package org.electrocodeogram.sensor.eclipse;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.RuntimeProcess;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.electrocodeogram.event.WellFormedEventPacket;
import org.electrocodeogram.logging.LogHelper;
import org.electrocodeogram.logging.LogHelper.ECGLevel;
import org.hackystat.kernel.shell.SensorShell;
import org.hackystat.stdext.sensor.eclipse.EclipseSensor;
import org.hackystat.stdext.sensor.eclipse.EclipseSensorPlugin;
import org.hackystat.stdext.sensor.eclipse.EclipseSensorShell;

/**
 * This is the <em>ECG EclipseSensor</em>. It is a collection of
 * <em>EventListeners</em> that are registered at different
 * interesrting pointsi in the Eclipse Plugin-API. These
 * <em>Listeners</em> are recording events. The events are then
 * encoded in <em>MicroActivityEvents</em> and sent to the ECG Lab.
 * The actual sending is done in the
 * {@link org.hackystat.kernel.shell.SensorShell}. This sensor uses
 * the original <em>HackyStat EclipseSensor</em> and extends it with
 * additional <em>Listeners</em>. So all original
 * <em>HackyStat</em> events are recorded along with the ECG events.
 */
public final class ECGEclipseSensor {

    /**
     * This is the logger.
     */
    private static Logger logger = LogHelper
        .createLogger(ECGEclipseSensor.class.getName());

    /**
     * This constant specifies how long to wait after user input
     * before a <em>Codechange</em> event is sent.
     */
    private static final int CODECHANGE_INTERVALL = 2000;

    /**
     * This is the name of the user, who is running <em>Eclipse</em>.
     */
    private String username;

    /**
     * The name of the project the user is currently working on.
     */
    private String projectname;

    /**
     * A reference to the currently active editor in <em>Eclipse</em>.
     */
    private ITextEditor activeTextEditor;

    /**
     * The name of the active window in <em>Eclipse</em>.
     */
    private String activeWindowName;

    /**
     * This is the <em>Singleton</em> instance of the
     * <em>ECGEclipseSensor</em>.
     */
    private static ECGEclipseSensor theInstance;

    /**
     * A reference to the <em>HackyStat EclipseSensor</em>, which
     * is used by this sensor.
     */
    private EclipseSensor hackyEclipse;

    /**
     * This is another <em>HackyStat</em> object that is kind of a
     * wrapper for the {@link SensorShell}. This sensor is using it
     * to.
     */
    private EclipseSensorShell eclipseSensorShell;

    /**
     * This is the private contstructor creating the <em>ECG
     * EclipseSensor</em>.
     */
    private ECGEclipseSensor() {
        logger.entering(this.getClass().getName(), "ECGEclipseSensor");

        /*
         * Create and get the original singleton HackyStat Eclipse
         * sensor instance.
         */
        this.hackyEclipse = EclipseSensor.getInstance();

        logger.log(Level.FINE, "HackyStat Eclipse sensor created.");

        this.eclipseSensorShell = this.hackyEclipse.getEclipseSensorShell();

        logger.log(Level.FINE, "Got HackyStat EclipseSensorShell.");

        /*
         * The next lines are needed for the InlineServer mode. In
         * that case the ECG SensorShell needs to now where the ECG
         * Lab application is stored locally. The ECG Lab is stored in
         * a PlugIns subdirectory called "ecg" per default. So we get
         * the PlugIn directory name itself and are adding the "ecg"
         * subdirectory.
         */
        // String id =
        // EclipseSensorPlugin.getInstance().getDescriptor()
        // .getUniqueIdentifier();
        //
        // String version =
        // EclipseSensorPlugin.getInstance().getDescriptor()
        // .getVersionIdentifier().toString();
        String[] path = {EclipseSensorPlugin.getInstance().getSensorPath() + File.separator
                         + "ecg"};

        List list = Arrays.asList(path);

        /*
         * The only way to communicate with the ECG SensorShell is by
         * using the HackyStat's EclipseSensorShell, since we are not
         * having a reference to the SensorShell itself.
         */
        this.eclipseSensorShell.doCommand(SensorShell.ECG_LAB_PATH, list);

        logger.log(Level.FINE,
            "The Sensorpath has been sent to the SensorShell.");

        // Try to get the username from the operating system
        // environment
        this.username = System.getenv("username");

        if (this.username == null || this.username.equals("")) {
            this.username = "n.a.";
        }

        logger.log(Level.FINE, "Username is set to" + this.username);

        // add the WindowListener for listening on
        // window events.
        IWorkbench workbench = PlatformUI.getWorkbench();

        workbench.addWindowListener(new WindowListenerAdapter());

        logger.log(Level.FINE, "Added WindowListener.");

        // add the PartListener for listening on
        // part events.
        IWorkbenchWindow[] activeWindows = workbench.getWorkbenchWindows();

        IWorkbenchPage activePage = null;

        for (int i = 0; i < activeWindows.length; i++) {
            activePage = activeWindows[i].getActivePage();

            activePage.addPartListener(new PartAndEditorListenerAdapter());
        }

        logger.log(Level.FINE, "Added PartListener.");

        // add the DocumentListener for listening on
        // document events.
        IEditorPart part = activePage.getActiveEditor();

        if (part instanceof ITextEditor) {
            processActivity(
                "msdt.editor.xsd",
                "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                                + ECGEclipseSensor.this.username
                                + "</username><projectname>"
                                + ECGEclipseSensor.this.projectname
                                + "</projectname></commonData><editor><activity>opened</activity><editorname>"
                                + part.getTitle()
                                + "</editorname></editor></microActivity>");

            this.activeTextEditor = (ITextEditor) part;

            IDocumentProvider provider = this.activeTextEditor
                .getDocumentProvider();

            IDocument document = provider.getDocument(part.getEditorInput());

            document.addDocumentListener(new DocumentListenerAdapter());
        }

        logger.log(Level.FINE, "Added DocumentListener.");

        // add the ResourceChangeListener to the workspace for
        // listening on
        // resource events.
        IWorkspace workspace = ResourcesPlugin.getWorkspace();

        workspace.addResourceChangeListener(new ResourceChangeAdapter(),
            IResourceChangeEvent.POST_CHANGE);

        // add the DebugEventSetListener to listen to run and debug
        // events.
        DebugPlugin dp = DebugPlugin.getDefault();

        dp.addDebugEventListener(new DebugEventSetAdapter());

        logger.log(Level.FINE, "Added DebugEventSetListener.");

        logger.exiting(this.getClass().getName(), "ECGEclipseSensor");
    }

    /**
     * This returns the current username.
     * @return The current username
     */
    protected String getUsername() {
        logger.entering(this.getClass().getName(), "getUsername");

        logger.exiting(this.getClass().getName(), "getUsername", this.username);

        return this.username;

    }

    /**
     * This returns the current projectname.
     * @return The current projectname
     */
    protected String getProjectname() {
        logger.entering(this.getClass().getName(), "getProjectname");

        logger.exiting(this.getClass().getName(), "getProjectname",
            this.projectname);

        return this.projectname;
    }

    /**
     * This method returns the <em>Singleton</em> instance of the
     * <em>ECG
     * EclipseSensor</em>.
     * @return The <em>Singleton</em> instance of the <em>ECG
     * EclipseSensor</em>
     */
    public static ECGEclipseSensor getInstance() {
        logger.entering(ECGEclipseSensor.class.getName(), "getInstance");

        if (theInstance == null) {
            theInstance = new ECGEclipseSensor();
        }

        logger.exiting(ECGEclipseSensor.class.getName(), "getInstance",
            theInstance);

        return theInstance;
    }

    /**
     * This method takes the data of a recorded event and generates a
     * <em>HackyStat Activity SensorDataType</em> conform event with
     * the given event data from it. The <em>HackyStat</em> command
     * name property is set to the value "add" and the
     * <em>HackyStat</em> activtiy-type property is set to the value
     * "MicroActivity" to indicate that this event is a
     * <em>MicroActivityEvent</em>. At last the event is passed to
     * the ECG {@link SensorShell} for further transmission to the ECG
     * Lab.
     * @param msdt
     *            This is the name of the <em>MicroSensorDataType</em>
     *            of the event.
     * @param data
     *            This is the actual <em>MicroActivityEvent</em>
     *            encoded in an XML document string that is an
     *            instance of a <em>MicroSensorDataType</em>.
     */
    public void processActivity(final String msdt, final String data) {
        logger.entering(this.getClass().getName(), "processActivity",
            new Object[] {msdt, data});

        if (data == null) {
            logger.log(Level.FINE,
                "The parameter \"data\" is null. Ignoring event.");

            logger.exiting(this.getClass().getName(), "processActivity");

            return;
        }

        String[] args = {WellFormedEventPacket.HACKYSTAT_ADD_COMMAND, msdt,
            data};

        // if Eclipse is shutting down the EclipseSensorShell might be
        // gone
        // allready
        if (this.eclipseSensorShell != null) {
            this.eclipseSensorShell.doCommand(
                WellFormedEventPacket.HACKYSTAT_ACTIVITY_STRING, Arrays
                    .asList(args));

            logger.log(Level.FINE,
                "An event has been passed event to the EclipseSensorShell");

        }

        logger.exiting(this.getClass().getName(), "processActivity");
    }

    /**
     * This is listening for events of the <em>Eclipse</em> window.
     */
    private class WindowListenerAdapter implements IWindowListener {

        /**
         * @see org.eclipse.ui.IWindowListener#windowActivated(org.eclipse.ui.IWorkbenchWindow)
         */
        @SuppressWarnings("synthetic-access")
        public void windowActivated(final IWorkbenchWindow window) {

            logger.entering(this.getClass().getName(), "windowActivated",
                new Object[] {window});

            if (window == null) {
                logger.log(Level.FINE,
                    "The Parameter  \"window\" is null. Ignoring event.");

                logger.exiting(this.getClass().getName(), "windowActivated");

                return;

            }

            if (window.getActivePage() != null) {
                ECGEclipseSensor.this.activeWindowName = window.getActivePage()
                    .getLabel();

                logger.log(ECGLevel.PACKET,
                    "A windowActivated event has been recorded.");

                processActivity(
                    "msdt.window.xsd",
                    "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                                    + ECGEclipseSensor.this.username
                                    + "</username><projectname>"
                                    + ECGEclipseSensor.this.projectname
                                    + "</projectname></commonData><window><activity>activated</activity><windowname>"
                                    + ECGEclipseSensor.this.activeWindowName
                                    + "</windowname></window></microActivity>");
            }

            logger.exiting(this.getClass().getName(), "windowActivated");
        }

        /**
         * @see org.eclipse.ui.IWindowListener#windowClosed(org.eclipse.ui.IWorkbenchWindow)
         */
        @SuppressWarnings("synthetic-access")
        public void windowClosed(@SuppressWarnings("unused")
        final IWorkbenchWindow window) {
            logger.entering(this.getClass().getName(), "windowClosed",
                new Object[] {window});

            if (window == null) {
                logger.log(Level.FINE,
                    "The Parameter \"window\" is null. Ignoring event.");

                logger.exiting(this.getClass().getName(), "windowClosed");

                return;

            }

            logger.log(ECGLevel.PACKET,
                "A windowClosed event has been recorded.");

            processActivity(
                "msdt.window.xsd",
                "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                                + ECGEclipseSensor.this.username
                                + "</username><projectname>"
                                + ECGEclipseSensor.this.projectname
                                + "</projectname></commonData><window><activity>closed</activity><windowname>"
                                + ECGEclipseSensor.this.activeWindowName
                                + "</windowname></window></microActivity>");

            logger.exiting(this.getClass().getName(), "windowClosed");
        }

        /**
         * @see org.eclipse.ui.IWindowListener#windowDeactivated(org.eclipse.ui.IWorkbenchWindow)
         */
        @SuppressWarnings("synthetic-access")
        public void windowDeactivated(@SuppressWarnings("unused")
        final IWorkbenchWindow window) {

            logger.entering(this.getClass().getName(), "windowDeactivated",
                new Object[] {window});

            if (window == null) {
                logger.log(Level.FINE,
                    "The Parameter \"window\" is null. Ignoring event.");

                logger.exiting(this.getClass().getName(), "windowDeactivated");

                return;

            }

            logger.log(ECGLevel.PACKET,
                "A windowDeactivated event has been recorded.");

            processActivity(
                "msdt.window.xsd",
                "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                                + ECGEclipseSensor.this.username
                                + "</username><projectname>"
                                + ECGEclipseSensor.this.projectname
                                + "</projectname></commonData><window><activity>deactivated</activity><windowname>"
                                + ECGEclipseSensor.this.activeWindowName
                                + "</windowname></window></microActivity>");

            logger.exiting(this.getClass().getName(), "windowDeactivated");
        }

        /**
         * @see org.eclipse.ui.IWindowListener#windowOpened(org.eclipse.ui.IWorkbenchWindow)
         */
        @SuppressWarnings("synthetic-access")
        public void windowOpened(final IWorkbenchWindow window) {

            logger.entering(this.getClass().getName(), "windowOpened",
                new Object[] {window});

            logger.log(Level.FINE,
                "The Parameter \"window\" is null. Ignoring event.");

            if (window == null) {
                logger.log(Level.FINEST, "window is null");

                logger.exiting(this.getClass().getName(), "windowOpened");

                return;

            }

            logger.log(ECGLevel.PACKET,
                "A windowOpened event has been recorded.");

            processActivity(
                "msdt.window.xsd",
                "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                                + ECGEclipseSensor.this.username
                                + "</username><projectname>"
                                + ECGEclipseSensor.this.projectname
                                + "</projectname></commonData><window><activity>deactivated</activity><windowname>"
                                + window.getActivePage().getLabel()
                                + "</windowname></window></microActivity>");

            logger.exiting(this.getClass().getName(), "windowOpened");
        }
    }

    /**
     * This is listeneing for events on resources like files,
     * directories or projects.
     */
    private class ResourceChangeAdapter implements IResourceChangeListener {

        /**
         * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
         */
        @SuppressWarnings("synthetic-access")
        public void resourceChanged(final IResourceChangeEvent event) {

            logger.entering(this.getClass().getName(), "resourceChanged",
                new Object[] {event});

            if (event == null) {
                logger.log(Level.FINE,
                    "The Parameter \"event\" is null. Ignoring event.");

                logger.exiting(this.getClass().getName(), "resourceChanged");

                return;

            }

            if (event.getType() != IResourceChangeEvent.POST_CHANGE) {
                logger
                    .log(Level.FINE,
                        "The Parameter  \"event\" is a POST_CHANGE event. Ignoring event.");

                logger.exiting(this.getClass().getName(), "resourceChanged");

                return;
            }

            IResourceDelta resourceDelta = event.getDelta();

            IResourceDeltaVisitor deltaVisitor = new IResourceDeltaVisitor() {

                public boolean visit(final IResourceDelta delta) {
                    logger.entering(this.getClass().getName(), "visit",
                        new Object[] {delta});

                    if (delta == null) {

                        logger.log(Level.FINE,
                            "The Parameter \"delta\" is null. Ignoring event.");

                        logger.exiting(this.getClass().getName(), "visit");

                        return false;
                    }

                    // get the kind of the ResourceChangedEvent
                    int kind = delta.getKind();

                    // get the resource
                    IResource resource = delta.getResource();

                    String resourceType = null;

                    // get the resourceType String
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

                    switch (kind) {
                        // a resource has been added
                        case IResourceDelta.ADDED:

                            logger.log(ECGLevel.PACKET,
                                "A resourceAdded event has been recorded.");

                            processActivity(
                                "msdt.resource.xsd",
                                "<?xml version=\"1.0\"?><microActivity><commonData><username>"
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
                                "<?xml version=\"1.0\"?><microActivity><commonData><username>"
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
                                    "<?xml version=\"1.0\"?><microActivity><commonData><username>"
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

                    logger.exiting(this.getClass().getName(), "visit");

                    return true;
                }

            };

            try {
                resourceDelta.accept(deltaVisitor);
            } catch (CoreException e) {
                logger
                    .log(Level.SEVERE,
                        "An Eclipse internal Exception occured during resourceEvent analysis.");

                logger.log(Level.FINEST, e.getMessage());
            }

            logger.exiting(this.getClass().getName(), "resourceChanged");

        }
    }

    /**
     * This is listening for run and debug events.
     */
    private class DebugEventSetAdapter implements IDebugEventSetListener {

        /**
         * A reference to the current programm launch.
         */
        private ILaunch currentLaunch;

        /**
         * @see org.eclipse.debug.core.IDebugEventSetListener#handleDebugEvents(org.eclipse.debug.core.DebugEvent[])
         */
        @SuppressWarnings("synthetic-access")
        public void handleDebugEvents(final DebugEvent[] events) {

            logger.entering(this.getClass().getName(), "handleDebugEvents");

            if (events == null || events.length == 0) {
                logger.log(Level.FINE,
                    "The Parameter events null or empty. Ignoring event.");

                logger.exiting(this.getClass().getName(), "handleDebugEvents");

                return;
            }

            Object source = events[0].getSource();

            if (source instanceof RuntimeProcess) {
                RuntimeProcess rp = (RuntimeProcess) source;

                ILaunch launch = rp.getLaunch();

                if (this.currentLaunch == null) {
                    this.currentLaunch = launch;

                    analyseLaunch(launch);
                } else if (!this.currentLaunch.equals(launch)) {
                    this.currentLaunch = launch;

                    analyseLaunch(launch);
                }
            }

            logger.exiting(this.getClass().getName(), "handleDebugEvents");
        }

        /**
         * This method is analysing the current program lauch and determines if it is a run or a debug launch.
         * @param launch Is the launch to analyse
         */
        @SuppressWarnings("synthetic-access")
        private void analyseLaunch(final ILaunch launch) {
            logger.entering(this.getClass().getName(), "analyseLaunch");

            if (launch == null) {
                logger.log(Level.FINE,
                    "The Parameter lauch is null. Ignoring event.");

                logger.exiting(this.getClass().getName(), "analyseLaunch");

                return;
            }

            if (launch.getLaunchMode().equals("run")) {

                logger.log(ECGLevel.PACKET, "A run event has been recorded.");

                processActivity(
                    "msdt.rundebug.xsd",
                    "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                                    + ECGEclipseSensor.this.username
                                    + "</username><projectname>"
                                    + ECGEclipseSensor.this.projectname
                                    + "</projectname></commonData><run debug=\"false\"></run></microActivity>");
            } else {
                logger.log(ECGLevel.PACKET, "A debug event has been recorded.");

                processActivity(
                    "msdt.rundebug.xsd",
                    "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                                    + ECGEclipseSensor.this.username
                                    + "</username><projectname>"
                                    + ECGEclipseSensor.this.projectname
                                    + "</projectname></commonData><run debug=\"true\"></run></microActivity>");
            }

            logger.exiting(this.getClass().getName(), "analyseLaunch");
        }
    }

    /**
     * This is listening for events that are affected to GUI parts and editors of <em>Eclipse</em>.
     */
    private class PartAndEditorListenerAdapter implements IPartListener {

        /**
         * @see org.eclipse.ui.IPartListener#partActivated(org.eclipse.ui.IWorkbenchPart)
         */
        @SuppressWarnings("synthetic-access")
        public void partActivated(final IWorkbenchPart part) {

            logger.entering(this.getClass().getName(), "partActivated", new Object[] {part});

            if (part == null) {
                logger.log(Level.FINE,
                    "The Parameter part is null. Ignoring event.");

                logger.exiting(this.getClass().getName(), "partActivated");

                return;
            }

            if (part instanceof ITextEditor) {

                ECGEclipseSensor.this.activeTextEditor = (ITextEditor) part;

                IDocumentProvider provider = ECGEclipseSensor.this.activeTextEditor
                    .getDocumentProvider();

                IDocument document = provider
                    .getDocument(ECGEclipseSensor.this.activeTextEditor
                        .getEditorInput());

                document.addDocumentListener(new DocumentListenerAdapter());

                logger.log(ECGLevel.PACKET,
                    "An editorActivated event has been recorded.");

                processActivity(
                    "msdt.editor.xsd",
                    "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                                    + ECGEclipseSensor.this.username
                                    + "</username><projectname>"
                                    + ECGEclipseSensor.this.projectname
                                    + "</projectname></commonData><editor><activity>activated</activity><editorname>"
                                    + part.getTitle()
                                    + "</editorname></editor></microActivity>");

            } else {
                logger.log(ECGLevel.PACKET,
                    "A partActivated event has been recorded.");

                processActivity(
                    "msdt.part.xsd",
                    "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                                    + ECGEclipseSensor.this.username
                                    + "</username><projectname>"
                                    + ECGEclipseSensor.this.projectname
                                    + "</projectname></commonData><part><activity>activated</activity><partname>"
                                    + part.getTitle()
                                    + "</partname></part></microActivity>");
            }

            logger.exiting(this.getClass().getName(), "partActivated");
        }

        /**
         * @see org.eclipse.ui.IPartListener#partClosed(org.eclipse.ui.IWorkbenchPart)
         */
        @SuppressWarnings("synthetic-access")
        public void partClosed(final IWorkbenchPart part) {
            logger.entering(this.getClass().getName(), "partClosed", new Object[] {part});

            if (part == null) {
                logger.log(Level.FINE,
                    "The Parameter part is null. Ignoring event.");

                logger.exiting(this.getClass().getName(), "partClosed");

                return;
            }

            if (part instanceof ITextEditor) {

                logger.log(ECGLevel.PACKET,
                    "An editorClosed event has been recorded.");

                processActivity(
                    "msdt.editor.xsd",
                    "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                                    + ECGEclipseSensor.this.username
                                    + "</username><projectname>"
                                    + ECGEclipseSensor.this.projectname
                                    + "</projectname></commonData><editor><activity>closed</activity><editorname>"
                                    + part.getTitle()
                                    + "</editorname></editor></microActivity>");

            } else {
                logger.log(ECGLevel.PACKET,
                    "A partClosed event has been recorded.");

                processActivity(
                    "msdt.part.xsd",
                    "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                                    + ECGEclipseSensor.this.username
                                    + "</username><projectname>"
                                    + ECGEclipseSensor.this.projectname
                                    + "</projectname></commonData><part><activity>closed</activity><partname>"
                                    + part.getTitle()
                                    + "</partname></part></microActivity>");
            }

            logger.exiting(this.getClass().getName(), "partClosed");
        }

        /**
         * @see org.eclipse.ui.IPartListener#partDeactivated(org.eclipse.ui.IWorkbenchPart)
         */
        @SuppressWarnings("synthetic-access")
        public void partDeactivated(final IWorkbenchPart part) {
            logger.entering(this.getClass().getName(), "partDeactivated", new Object[] {part});

            if (part == null) {
                logger.log(Level.FINE,
                    "The Parameter part is null. Ignoring event.");

                logger.exiting(this.getClass().getName(), "partDeactivated");

                return;
            }

            if (part instanceof ITextEditor) {

                logger.log(ECGLevel.PACKET,
                    "An editorDeactivated event has been recorded.");

                processActivity(
                    "msdt.editor.xsd",
                    "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                                    + ECGEclipseSensor.this.username
                                    + "</username><projectname>"
                                    + ECGEclipseSensor.this.projectname
                                    + "</projectname></commonData><editor><activity>deactivated</activity><editorname>"
                                    + part.getTitle()
                                    + "</editorname></editor></microActivity>");

            } else {
                logger.log(ECGLevel.PACKET,
                    "A partDeactivated event has been recorded.");

                processActivity(
                    "msdt.part.xsd",
                    "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                                    + ECGEclipseSensor.this.username
                                    + "</username><projectname>"
                                    + ECGEclipseSensor.this.projectname
                                    + "</projectname></commonData><part><activity>deactivated</activity><partname>"
                                    + part.getTitle()
                                    + "</partname></part></microActivity>");
            }

            logger.exiting(this.getClass().getName(), "partDeactivated");

        }

        /**
         * @see org.eclipse.ui.IPartListener#partOpened(org.eclipse.ui.IWorkbenchPart)
         */
        @SuppressWarnings("synthetic-access")
        public void partOpened(final IWorkbenchPart part) {
            logger.entering(this.getClass().getName(), "partOpened", new Object[] {part});

            if (part == null) {
                logger.log(Level.FINE,
                    "The Parameter part is null. Ignoring event.");

                logger.exiting(this.getClass().getName(), "partOpened");

                return;
            }

            if (part instanceof ITextEditor) {

                logger.log(ECGLevel.PACKET,
                    "An editorOpened event has been recorded.");

                processActivity(
                    "msdt.editor.xsd",
                    "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                                    + ECGEclipseSensor.this.username
                                    + "</username><projectname>"
                                    + ECGEclipseSensor.this.projectname
                                    + "</projectname></commonData><editor><activity>opened</activity><editorname>"
                                    + part.getTitle()
                                    + "</editorname></editor></microActivity>");

            } else {
                logger.log(ECGLevel.PACKET,
                    "A partOpened event has been recorded.");

                processActivity(
                    "msdt.part.xsd",
                    "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                                    + ECGEclipseSensor.this.username
                                    + "</username><projectname>"
                                    + ECGEclipseSensor.this.projectname
                                    + "</projectname></commonData><part><activity>opened</activity><partname>"
                                    + part.getTitle()
                                    + "</partname></part></microActivity>");
            }

            logger.exiting(this.getClass().getName(), "partOpened");
        }

        /**
         * @see org.eclipse.ui.IPartListener#partBroughtToTop(org.eclipse.ui.IWorkbenchPart)
         */
        @SuppressWarnings("synthetic-access")
        public void partBroughtToTop(@SuppressWarnings("unused")
        final IWorkbenchPart part) {
            logger.entering(this.getClass().getName(), "partBroughtToTop", new Object[] {part});

            // not implemented

            logger.exiting(this.getClass().getName(), "partBroughtToTop");
        }
    }

    /**
     This is listening for events about changes in the text of open documents.
     */
    private class DocumentListenerAdapter implements IDocumentListener {

        /**
         * This is used to wait a moment after a <em>DocumentChanged</em>
         * event has been recorded. Only when the user has not changed the
         * document for {@link ECGEclipseSensor#CODECHANGE_INTERVALL} amount of time,
         * a <em>Codechange</em> event is sent.
         */
        private Timer timer = null;

        /**
         * Creates the <em>DocumentListenerAdapter</em> and the <code>Timer</code>.
         */
        @SuppressWarnings("synthetic-access")
        public DocumentListenerAdapter() {
            logger.entering(this.getClass().getName(),
                "DocumentListenerAdapter");

            this.timer = new Timer();

            logger
                .exiting(this.getClass().getName(), "DocumentListenerAdapter");
        }

        /**
         * @see org.eclipse.jface.text.IDocumentListener#documentAboutToBeChanged(org.eclipse.jface.text.DocumentEvent)
         */
        @SuppressWarnings("synthetic-access")
        public void documentAboutToBeChanged(@SuppressWarnings("unused")
        final DocumentEvent event) {
            logger.entering(this.getClass().getName(),
                "documentAboutToBeChanged", new Object[] {event});

            // not supported in Eclipse Sensor.

            logger.exiting(this.getClass().getName(),
                "documentAboutToBeChanged");
        }

        /**
         * @see org.eclipse.jface.text.IDocumentListener#documentChanged(org.eclipse.jface.text.DocumentEvent)
         */
        @SuppressWarnings("synthetic-access")
        public void documentChanged(final DocumentEvent event) {
            logger.entering(this.getClass().getName(), "documentChanged", new Object[] {event});

            if (event == null) {
                logger.log(Level.FINE,
                    "The Parameter event null. Ignoring event.");

                logger.exiting(this.getClass().getName(), "documentChanged");

                return;
            }

            this.timer.cancel();

            this.timer = new Timer();

            this.timer.schedule(new CodeChangeTimerTask(event.getDocument(),
                ECGEclipseSensor.this.activeTextEditor.getTitle()),
                ECGEclipseSensor.CODECHANGE_INTERVALL);

            logger.exiting(this.getClass().getName(), "documentChanged");
        }
    }

    /**
     * This <em>TimerTask</em> is used in creating <em>Codechange</em> events.
     * In <em>Eclipse</em> every time the user changes a single character in the
     * active document an <em>DocumentChanged</em> event is fired. To avoid
     * sending <em>Codechange</em> this often, the sensor shall wait for an amount
     * of time after before sending a <em>Codechange</em>.
     * Only when the user does not change the document's text for {@link ECGEclipseSensor#CODECHANGE_INTERVALL}
     * time, a <em>Codechange</em> is sent to the ECG Lab.
     *
     */
    private static class CodeChangeTimerTask extends TimerTask {

        /**
         * This is the document that has been changed.
         */
        private IDocument doc;

        /**
         * The name of the document.
         */
        private String name;

        /**
         * This creates the <em>TimerTask</em>.
         * @param document
         *            Is the document that has been changed
         * @param documentName
         *            Is the name of the document
         */
        @SuppressWarnings("synthetic-access")
        public CodeChangeTimerTask(final IDocument document, final String documentName) {
            logger.entering(this.getClass().getName(), "CodeChangeTimerTask", new Object[] {document, documentName});

            this.doc = document;

            this.name = documentName;

            logger.exiting(this.getClass().getName(), "CodeChangeTimerTask");
        }

        /**
         * @see java.util.TimerTask#run()
         */
        @SuppressWarnings("synthetic-access")
        @Override
        public void run() {
            logger.entering(this.getClass().getName(), "run");

            ECGEclipseSensor sensor = ECGEclipseSensor.getInstance();

            logger
                .log(ECGLevel.PACKET, "A codechange event has been recorded.");

            sensor
                .processActivity(
                    "msdt.codechange.xsd",
                    "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                                    + sensor.getUsername()
                                    + "</username><projectname>"
                                    + sensor.getProjectname()
                                    + "</projectname></commonData><codechange><document><![CDATA["
                                    + this.doc.get()
                                    + "]]></document><documentname>"
                                    + this.name
                                    + "</documentname></codechange></microActivity>");

            // while(true)
            // {
            // try
            // {
            // Thread.sleep(1000);
            // }
            // catch (InterruptedException e)
            // {
            // // TODO Auto-generated catch block
            // e.printStackTrace();
            // }
            // _logger.log(ECGLevel.PACKET,"A codechange event has
            // been recorded.");
            // sensor.processActivity("msdt.codechange.xsd","<?xml
            // version=\"1.0\"?><microActivity><commonData><username>"
            // + sensor.getUsername() + "</username><projectname>" +
            // sensor.getProjectname() +
            // "</projectname></commonData><codechange><document><![CDATA["
            // + this._document.get() + "]]></document><documentname>"
            // + this._documentName +
            // "</documentname></codechange></microActivity>");
            // }

            logger.exiting(this.getClass().getName(), "run");

        }
    }

}

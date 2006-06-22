/*
 * Freie Universit‰t Berlin, 2006
 */

package org.electrocodeogram.sensor.eclipse;

import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IExecutionListener;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.IFileBuffer;
import org.eclipse.core.filebuffers.IFileBufferListener;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.RuntimeProcess;
import org.eclipse.jdt.internal.junit.ui.JUnitPlugin;
import org.eclipse.jdt.junit.ITestRunListener;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.Assert;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartConstants;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.texteditor.IAbstractTextEditorHelpContextIds;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.IElementStateListener;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.TextEditorAction;
import org.electrocodeogram.event.WellFormedEventPacket;
import org.electrocodeogram.logging.LogHelper;
import org.electrocodeogram.logging.LogHelper.ECGLevel;
import org.hackystat.kernel.shell.SensorShell;
import org.hackystat.stdext.sensor.eclipse.EclipseSensor;
import org.hackystat.stdext.sensor.eclipse.EclipseSensorPlugin;
import org.hackystat.stdext.sensor.eclipse.EclipseSensorShell;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

/**
 * A collection of
 * <em>EventListeners</em> that are registered at different 
 * interesting points in the Eclipse Workbench. These
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
     * This is to log program states.
     */
    private static Logger logger = LogHelper
        .createLogger(ECGEclipseSensor.class.getName());

    /**
     * This constant specifies how long to wait after user input
     * before a <em>Codechange</em> event is sent.
     */
    private static final int CODECHANGE_INTERVAL = 2000;

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
     * A reference to the currently active editor in <em>Eclipse</em>.
     */
    private IViewPart activeView;

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
     * The one and only PartListener, because all Parts are treated similarly 
     */
    private ECGPartListener partListener;
    
    /**
     * The one and only ShellListener, because all Shells are treated similarly 
     */
    private ECGShellListener shellListener;
    
    /**
     * The one and only RunDebugListener, because all Launch events are treated similarly 
     */
    private ECGRunDebugListener runDebugListener;
    
    /**
     * The one and only DocumentListener, because all Text files are treated similarly 
     */
    private ECGDocumentListener docListener;
    
    /**
     * The one and only TestListener, because all test runs are treated similarly 
     */
    private ECGTestListener testListener;

    // TODO the next line is just for exploration
    private ECGElementStateListener elementStateListener;
    
    /**
     * The one and only TestListener, because all test runs are treated similarly 
     */
    private ArrayList openDialogs = new ArrayList();
    
    /**
     * A serializer to output XML DOM structures 
     */
    private LSSerializer xmlDocumentSerializer; 

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
         * The next line is needed for the InlineServer mode. In
         * that case the ECG SensorShell needs to now where the ECG
         * Lab application is stored locally. The ECG Lab is stored in
         * a PlugIns subdirectory called "ecg" per default. So we get
         * the PlugIn directory name itself and are adding the "ecg"
         * subdirectory.
         */
        String[] path = {EclipseSensorPlugin.getInstance().getSensorPath()
                         + File.separator + "ecg"};

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
            this.username = "unknown";
        }
        
        try {
            // get DOM Implementation using DOM Registry
            // TODO Besser auf Xerxes setzen. Dies ist auﬂerdem wohl nur in JDK 5.0
            System.setProperty(DOMImplementationRegistry.PROPERTY, "com.sun.org.apache.xerces.internal.dom.DOMXSImplementationSourceImpl");
            DOMImplementationRegistry registry;
            registry = DOMImplementationRegistry.newInstance();
            // Retrieve load/save features
            DOMImplementationLS impl = 
                (DOMImplementationLS)registry.getDOMImplementation("LS");
            // create DOMWriter
            xmlDocumentSerializer = impl.createLSSerializer();   
            xmlDocumentSerializer.getDomConfig().setParameter("xml-declaration", Boolean.FALSE);
        } catch (Exception e) { // TODO Ok, that's bad...
            logger.log(Level.SEVERE,
                "Could not instantiate the DOM Implementation.");
            logger.log(Level.FINE, e.getMessage());
        }

        logger.log(Level.FINE, "Username is set to" + this.username);

        // All the listener registration is done in the SWT event thread
        //   to get sure that the GUI elements are completely built.
        PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
            public void run() {
            	ECGEclipseSensor.this.init();
            }
        });

        logger.exiting(this.getClass().getName(), "ECGEclipseSensor");
                
    }

    /**
     * Does all the listener registration 
     */
    protected void init() {
    	
    	IWorkbench workbench = PlatformUI.getWorkbench();
    	IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();

		// ------
		// Register Listeners
		
		// add some DisplayListener for listening on shell activation events.
        Listener displayListener = new ECGDisplayListener();
        PlatformUI.getWorkbench().getDisplay().addFilter(SWT.Activate, displayListener);                
        PlatformUI.getWorkbench().getDisplay().addFilter(SWT.Dispose,displayListener);
        PlatformUI.getWorkbench().getDisplay().addFilter(SWT.Deactivate, displayListener);
        // others are SWT.Deiconify, SWT.Close, SWT.Iconify, SWT.FocusIn, SWT.FocusOut, SWT.Hide, SWT.Show
        // especially interesting are SWT.Selection (includes text selection), SWT.Modify (text being modified) 
		logger.log(Level.FINE, "Added some DisplayListeners.");
		
		// TODO: the following is just for experimentation 
//		ICommandService commandService; 
//		commandService = (ICommandService)workbench.getService(ICommandService.class);
//		if (commandService != null) {
//			commandService.addExecutionListener(new ECGCommandExecutionListener());
//		}
		
		// add the PartListener for listening on
		// part events.
		IPartService partService = null;
		partListener = new ECGPartListener();
		for (int i = 0; i < windows.length; i++) {
		    partService = windows[i].getPartService();
			partService.addPartListener(partListener);
		}
		logger.log(Level.FINE, "Added PartListener.");
		
		// add ShellListener for ShellEvents
		shellListener = new ECGShellListener();
        Shell shell = null;
        // There could be (really) more than one Window/Shell at start-up
        for (int i = 0; i < windows.length; i++) {
            shell = windows[i].getShell();
            shell.addShellListener(shellListener);
            // send window open 
            shellListener.shellOpened(shell);
            
            /* TODO old code, remove if obsolete
            logger.log(ECGLevel.PACKET,
                "A windowOpened event has been recorded on startup.");
            processActivity(
                "msdt.window.xsd",
                "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                    + ECGEclipseSensor.this.username
                    + "</username><id>"
                    + windows[i].getShell().hashCode()
                    + "</id></commonData><window><activity>opened</activity><windowname>"
                    + windows[i].getShell().getText()
                    + "</windowname></window></microActivity>");
             */
        }
		logger.log(Level.FINE, "Added ShellListener.");
		
		/* TODO: Currently, no resource change events are used. This would be the place, 
		 * where renaming etc. is detected.
		// add the ResourceChangeListener to the workspace for
		// listening on resource events.
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace.addResourceChangeListener(new ECGResourceChangeListener(),
		    IResourceChangeEvent.POST_CHANGE);
		*/
		
		// add the DebugEventSetListener to listen to run and debug events.
		DebugPlugin dp = DebugPlugin.getDefault();
		runDebugListener = new ECGRunDebugListener();
		dp.addDebugEventListener(runDebugListener);
		logger.log(Level.FINE, "Added DebugEventSetListener.");
		
		// add listener for testRun events
		testListener = new ECGTestListener();        
		JUnitPlugin.getDefault().addTestRunListener(testListener);
		logger.log(Level.FINE, "Added TestListener.");
		
		// add listener for jobs (used for team tasks)
		// TODO It's just for exploring, remove this line and the Listener if not useful
//		Platform.getJobManager().addJobChangeListener(new ECGJobListener());
		
		//TODO the next line is just for exploration
		FileBuffers.getTextFileBufferManager().addFileBufferListener(new ECGFileBufferListener());
		
		docListener = new ECGDocumentListener();
		// TODO the next line is just for exploration
//		elementStateListener = new ECGElementStateListener();
		
		// -------
		// Send initial events on startup: which parts are open and which are activated
		for (int i = 0; i < windows.length; i++) {
		
		    IWorkbenchPage page = windows[i].getActivePage();
		
		    // send opened event on all views
		    IViewReference[] views = page.getViewReferences();
		    for (int j = 0; j < views.length; j++) {
		        IViewPart view = views[j].getView(false);
		        if (view != null) {
		        	// just call the related listener method
		        	partListener.partOpened(view);
		        }
		    }
		    // send opened event on all editors
		    IEditorReference[] editors = page.getEditorReferences();
		    for (int j = 0; j < editors.length; j++) {
		        IEditorPart editor = editors[j].getEditor(false);		
		        if (editor != null) {
		        	// just call the related listener method
		        	partListener.partOpened(editor);
		        }
		    }
		    // send event for activated part
		    IWorkbenchPart activePart = page.getActivePart();
			partListener.partActivated(activePart);
		}
	}

	/**
     * This returns the current username.
     * @return The current username
     */
    protected String getUsername() {

        return this.username;

    }

    /**
     * This returns the current projectname.
     * @return The current projectname
     */
    protected String getProjectname() {

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

    private String getFilenameFromLocation(String location) {
    	if (location != null) {
    		if (location.charAt(0) == IPath.SEPARATOR)
    			location = location.substring(1);
    		int sepIndex = location.indexOf(IPath.SEPARATOR);
    		if (sepIndex != -1)
    		{
    			String res = location.substring(sepIndex+1);
    			return res;
    		}
    	}
    	return "";
	}

	private String getProjectnameFromLocation(String location) {
    	if (location != null) {
    		if (location.charAt(0) == IPath.SEPARATOR)
    			location = location.substring(1);
    		int sepIndex = location.indexOf(IPath.SEPARATOR);
    		if (sepIndex != -1)
    		{
    			String res = location.substring(0, sepIndex);
    			return res;
    		}
    	}
    	return "";
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
        // gone already
        if (this.eclipseSensorShell != null) {
            this.eclipseSensorShell.doCommand(
                WellFormedEventPacket.HACKYSTAT_ACTIVITY_STRING, Arrays
                    .asList(args));

            logger.log(Level.FINE,
                "An event has been passed event to the EclipseSensorShell");

        }

        logger.exiting(this.getClass().getName(), "processActivity");
    }

    
    // -----------------------------------------------------------------------------
    
    /**
     * This is listeneing for events on resources like files,
     * directories or projects.
     * TODO: This is currently not being used. ResourceListeners are useful for basic file management.
     * It treats every file, folder, etc. equally, but is seldom useful 
     */
    private class ECGResourceChangeListener implements IResourceChangeListener {

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
            	// it's just to take for sure that only opst events are processed
            	// the listener itself has been registered on post events only
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
            			logger.log(ECGLevel.INFO, "RCE:: " + resourceChangeKind + " " + resourceType + " "
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
*/

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
    private class ECGRunDebugListener implements IDebugEventSetListener {

        /**
         * A reference to the current programm launch.
         */
        private ILaunch currentLaunch;

        /**
         * @see org.eclipse.debug.core.IDebugEventSetListener#handleDebugEvents(org.eclipse.debug.core.DebugEvent[])
         * 
         */
        public void handleDebugEvents(final DebugEvent[] events) {

            logger.entering(this.getClass().getName(), "handleDebugEvents");

            if (events == null || events.length == 0) {
                logger.log(Level.FINE,
                    "The Parameter events null or empty. Ignoring event.");

                logger.exiting(this.getClass().getName(), "handleDebugEvents");

                return;
            }
                        
            // TODO: Allow for more than one parallel launch
            for (int i=0; i < events.length; i++) {
                Object source = events[i].getSource();

                if (source instanceof RuntimeProcess) {
                    RuntimeProcess rp = (RuntimeProcess) source;

                    ILaunch launch = rp.getLaunch();
                    
                    if (launch.equals(this.currentLaunch) && events[i].getKind() == DebugEvent.TERMINATE) {
                    	// An active launch got a TERMINATE event
                    	if (analyseTermination(rp, launch))
                    		this.currentLaunch = null;
                    } 
                    else if (this.currentLaunch == null || !this.currentLaunch.equals(launch)) {
                    	// There are many events on the same process and launch.
                    	// analyseLaunch returns true if this launch has been recognized
                    	// Otherwise re-analyse every following similiar event
                    	if (analyseLaunch(rp, launch))
                    		this.currentLaunch = launch;
                    }
                }            	
            }

            logger.exiting(this.getClass().getName(), "handleDebugEvents");
        }

        /**
         * This method is analysing the current program lauch and
         * determines if it is a run or a debug launch.
         * @param launch
         *            Is the launch to analyse
         * @param process the IProcess for this launch
         * @returns true if event has been recognized and processed
         */
        private boolean analyseLaunch(final RuntimeProcess process, final ILaunch launch) {
            logger.entering(this.getClass().getName(), "analyseLaunch");

            if (launch == null) {
                logger.log(Level.FINE,
                    "The Parameter lauch is null. Ignoring event.");

                logger.exiting(this.getClass().getName(), "analyseLaunch");
                return false;
            }

            if (process != null 
            		&& process.getAttribute(IProcess.ATTR_PROCESS_TYPE) != null
            		&& process.getAttribute(IProcess.ATTR_PROCESS_TYPE)
            			.toLowerCase().endsWith("antprocess")) {

                String cmdLine = process.getAttribute(IProcess.ATTR_CMDLINE);
            	
            	if (cmdLine == null) {
            		// It may occur that the process attribute fields are not set
            		// return false to wait for the next event of a DebugEvent.CHANGE kind
            		// which may be a change of the attribute settings
            		return false;
            	}

                logger.log(ECGLevel.PACKET, "An ant event has been recorded.");

            	// extract Ant's buildfile and target settings from the 
            	// java-call of Ant. It relies on the build file stated
                // after "-buildfile" and the target name being the last
                // argument (w/o '_' prefix) and without an "-"option 
                // preceeding
            	String buildfile = "";
            	String target = "";
            	String[] args = DebugPlugin.parseArguments(cmdLine);
                for (int i=0; i < args.length; i++) {
                	if (args[i].equals("-buildfile") 
                			&& i+1 < args.length 
                			&& args[i+1] != null) {
                		buildfile = args[i+1]; 
                	}
                	if (i+1 == args.length 
                			&& !args[i-1].startsWith("-") 
                			&& !args[i].startsWith("-")) {
                		target = args[i];
                	}
                }
                
                processActivity(
                    "msdt.antrun.xsd",
                    "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                        + ECGEclipseSensor.this.username
                        + "</username><id>"
                        + launch.hashCode()
                        + "</id></commonData><ant>"
                        + "<id>" + launch.hashCode() + "</id>"
                        + "<mode>" + launch.getLaunchMode() + "</mode>"
                        + "<buildfile>" + buildfile + "</buildfile>"
                        + "<target>" + target + "</target>"
                        + "</ant></microActivity>");
            	
            }
            else {

                logger.log(ECGLevel.PACKET, "A run event has been recorded.");

                String config = "null";
                if (launch.getLaunchConfiguration() != null)
                	config = launch.getLaunchConfiguration().getName();
                
                processActivity(
                    "msdt.run.xsd",
                    "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                        + ECGEclipseSensor.this.username
                        + "</username><id>"
                        + launch.hashCode()
                        + "</id></commonData><run>"
                        + "<id>" + launch.hashCode() + "</id>"
                        + "<mode>" + launch.getLaunchMode() + "</mode>"
                        + "<launch>" + config + "</launch>"
                        + "</run></microActivity>");
            }

            logger.exiting(this.getClass().getName(), "analyseLaunch");
            
            return true;
        }

        /**
         * This method is analysing a program termination.
         * @param launch Is the launch to analyse
         * @param process the IProcess for this launch
         * @returns true if event has been recognized and processed
         */
        private boolean analyseTermination(final RuntimeProcess process, final ILaunch launch) {
            logger.entering(this.getClass().getName(), "analyseTermination");

            if (launch == null) {
                logger.log(Level.FINE,
                    "The Parameter lauch is null. Ignoring event.");

                logger.exiting(this.getClass().getName(), "analyseTermination");
                return false;
            }

            if (process.getAttribute(IProcess.ATTR_PROCESS_TYPE).toLowerCase().endsWith("antprocess")) {

                logger.log(ECGLevel.PACKET, "An ant termination event has been recorded.");

                processActivity(
                    "msdt.antrun.xsd",
                    "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                        + ECGEclipseSensor.this.username
                        + "</username><id>"
                        + launch.hashCode()
                        + "</id></commonData><ant>"
                        + "<id>" + launch.hashCode() + "</id>"
                        + "<mode>termination</mode>"
                        + "<buildfile></buildfile>"
                        + "<target></target>"
                        + "</ant></microActivity>");
            	
            }
            else {

                logger.log(ECGLevel.PACKET, "A run/debug termination event has been recorded.");

                String config = "null";
                if (launch.getLaunchConfiguration() != null)
                	config = launch.getLaunchConfiguration().getName();
                
                processActivity(
                    "msdt.run.xsd",
                    "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                        + ECGEclipseSensor.this.username
                        + "</username><id>"
                        + launch.hashCode()
                        + "</id></commonData><run>"
                        + "<id>" + launch.hashCode() + "</id>"
                        + "<mode>termination</mode>"
                        + "<launch>" + config + "</launch>"
                        + "</run></microActivity>");
            }
            
            logger.exiting(this.getClass().getName(), "analyseTermination");
            
            return true;
        }

    }

    /**
     * This is listening for events that are affected to GUI parts and
     * editors of <em>Eclipse</em>.
     */
    private class ECGPartListener implements IPartListener {

        private Document msdt_editor_doc;
        private Document msdt_part_doc;
        private Document msdt_codestatus_doc;
        
        private Element editor_username;
        private Element editor_projectname;
        private Element editor_id;        
        private Element editor_activity;
        private Element editor_editorname;
        
        private Element part_username;
        private Element part_id;        
        private Element part_activity;
        private Element part_partname;

        private Element codestatus_username;
        private Element codestatus_projectname;
        private Element codestatus_id;        
        private Element codestatus_document;
        private CDATASection codestatus_contents;
        private Element codestatus_documentname;
        
        public ECGPartListener() {
            try {
                
                // initialize DOM skeleton for msdt.editor.xsd
                msdt_editor_doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
                Element editor_microactivity = msdt_editor_doc.createElement("microActivity");                
                Element editor_commondata = msdt_editor_doc.createElement("commonData");
                Element editor_editor = msdt_editor_doc.createElement("editor");
                editor_username = msdt_editor_doc.createElement("username");
                editor_projectname = msdt_editor_doc.createElement("projectname");
                editor_id = msdt_editor_doc.createElement("id");
                editor_activity = msdt_editor_doc.createElement("activity");
                editor_editorname = msdt_editor_doc.createElement("editorname");

                msdt_editor_doc.appendChild(editor_microactivity);
                  editor_microactivity.appendChild(editor_commondata);
                    editor_commondata.appendChild(editor_username);
                    editor_commondata.appendChild(editor_projectname);
                    editor_commondata.appendChild(editor_id);
                  editor_microactivity.appendChild(editor_editor);
                    editor_editor.appendChild(editor_activity);
                    editor_editor.appendChild(editor_editorname);

                // initialize DOM skeleton for msdt.part.xsd
                msdt_part_doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
                Element part_microactivity = msdt_part_doc.createElement("microActivity");                
                Element part_commondata = msdt_part_doc.createElement("commonData");
                Element part_part = msdt_part_doc.createElement("part");
                part_username = msdt_part_doc.createElement("username");
                part_id = msdt_part_doc.createElement("id");
                part_activity = msdt_part_doc.createElement("activity");
                part_partname = msdt_part_doc.createElement("partname");

                msdt_part_doc.appendChild(part_microactivity);
                  part_microactivity.appendChild(part_commondata);
                    part_commondata.appendChild(part_username);
                    part_commondata.appendChild(part_id);
                  part_microactivity.appendChild(part_part);
                    part_part.appendChild(part_activity);
                    part_part.appendChild(part_partname);

                // initialize DOM skeleton for msdt.codestatus.xsd
                msdt_codestatus_doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
                Element codestatus_microactivity = msdt_codestatus_doc.createElement("microActivity");                
                Element codestatus_commondata = msdt_codestatus_doc.createElement("commonData");
                Element codestatus_codestatus = msdt_codestatus_doc.createElement("codestatus");
                codestatus_username = msdt_codestatus_doc.createElement("username");
                codestatus_projectname = msdt_codestatus_doc.createElement("projectname");
                codestatus_id = msdt_codestatus_doc.createElement("id");
                codestatus_document = msdt_codestatus_doc.createElement("document");
                codestatus_contents = msdt_codestatus_doc.createCDATASection("");
                codestatus_documentname = msdt_codestatus_doc.createElement("documentname");

                msdt_codestatus_doc.appendChild(codestatus_microactivity);
                  codestatus_microactivity.appendChild(codestatus_commondata);
                    codestatus_commondata.appendChild(codestatus_username);
                    codestatus_commondata.appendChild(codestatus_projectname);
                    codestatus_commondata.appendChild(codestatus_id);
                  codestatus_microactivity.appendChild(codestatus_codestatus);
                    codestatus_codestatus.appendChild(codestatus_document);
                      codestatus_document.appendChild(codestatus_contents);
                    codestatus_codestatus.appendChild(codestatus_documentname);
                                        
            } catch (ParserConfigurationException e) {
                logger.log(Level.SEVERE,
                    "Could not instantiate the DOM Document in ECGPartListener.");
                logger.log(Level.FINE, e.getMessage());
            }
        }

        /**
         * @see org.eclipse.ui.IPartListener#partActivated(org.eclipse.ui.IWorkbenchPart)
         */
        public void partActivated(final IWorkbenchPart part) {

            logger.entering(this.getClass().getName(), "partActivated",
                new Object[] {part});

            if (part == null) {
                logger.log(Level.FINE,
                    "The Parameter part is null. Ignoring event.");

                logger.exiting(this.getClass().getName(), "partActivated");

                return;
            }

            if (part instanceof IEditorPart) {
                
                if (part instanceof ITextEditor) {
                    ITextEditor textEditor  = (ITextEditor) part;
                    // register document listener on opened Editors. Should have been done at partOpened
                    // but in case of a new document instance for this editor, get sure to be registered.
                    // Adding the same listener twice causes no harm. 
                    IDocumentProvider provider = textEditor.getDocumentProvider();
                    IDocument document = provider.getDocument(textEditor.getEditorInput());
                    document.addDocumentListener(ECGEclipseSensor.this.docListener);
                    // set current active TextEditor
                    ECGEclipseSensor.this.activeTextEditor = textEditor;
                }
                
                logger.log(ECGLevel.PACKET,
                    "An editorActivated event has been recorded.");

                editor_id.setTextContent(String.valueOf(part.hashCode()));
                editor_projectname.setTextContent(getProjectnameFromLocation(part.getTitleToolTip()));
                editor_username.setTextContent(ECGEclipseSensor.this.username);
                editor_activity.setTextContent("activated");
                editor_editorname.setTextContent(getFilenameFromLocation(part.getTitleToolTip()));

                processActivity("msdt.editor.xsd", 
                        xmlDocumentSerializer.writeToString(msdt_editor_doc));

                /* TODO old code, remove if obsolete
                processActivity(
                    "msdt.editor.xsd",
                    "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                        + ECGEclipseSensor.this.username
                        + "</username><projectname>"
                        + getProjectnameFromLocation(part.getTitleToolTip())
                        + "</projectname><id>"
                        + part.hashCode()
                        + "</id></commonData><editor><activity>activated</activity><editorname>"
                        + getFilenameFromLocation(part.getTitleToolTip())
                        + "</editorname></editor></microActivity>");
                 */
            } else if (part instanceof IViewPart) {
                logger.log(ECGLevel.PACKET,
                    "A partActivated event has been recorded.");

                part_id.setTextContent(String.valueOf(part.hashCode()));
                part_username.setTextContent(ECGEclipseSensor.this.username);
                part_activity.setTextContent("activated");
                part_partname.setTextContent(part.getTitle());

                processActivity("msdt.part.xsd", 
                        xmlDocumentSerializer.writeToString(msdt_part_doc));

                /* TODO old code, remove if obsolete
                processActivity(
                    "msdt.part.xsd",
                    "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                        + ECGEclipseSensor.this.username
                        + "</username><id>"
                        + part.hashCode()
                        + "</id></commonData><part><activity>activated</activity><partname>"
                        + part.getTitle()
                        + "</partname></part></microActivity>");
                 */
            } 

            logger.exiting(this.getClass().getName(), "partActivated");
        }

        /**
         * @see org.eclipse.ui.IPartListener#partClosed(org.eclipse.ui.IWorkbenchPart)
         */
        public void partClosed(final IWorkbenchPart part) {
            logger.entering(this.getClass().getName(), "partClosed",
                new Object[] {part});

            if (part == null) {
                logger.log(Level.FINE,
                    "The Parameter part is null. Ignoring event.");

                logger.exiting(this.getClass().getName(), "partClosed");

                return;
            }

            if (part instanceof IEditorPart) {

                logger.log(ECGLevel.PACKET,
                    "An editorClosed event has been recorded.");

                editor_id.setTextContent(String.valueOf(part.hashCode()));
                editor_projectname.setTextContent(getProjectnameFromLocation(part.getTitleToolTip()));
                editor_username.setTextContent(ECGEclipseSensor.this.username);
                editor_activity.setTextContent("closed");
                editor_editorname.setTextContent(getFilenameFromLocation(part.getTitleToolTip()));

                processActivity("msdt.editor.xsd",  
                        xmlDocumentSerializer.writeToString(msdt_editor_doc));

                /* TODO old code, remove if obsolete
                processActivity(
                    "msdt.editor.xsd",
                    "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                        + ECGEclipseSensor.this.username
                        + "</username><projectname>"
                        + getProjectnameFromLocation(part.getTitleToolTip())
                        + "</projectname><id>"
                        + part.hashCode()
                        + "</id></commonData><editor><activity>closed</activity><editorname>"
                        + getFilenameFromLocation(part.getTitleToolTip())
                        + "</editorname></editor></microActivity>");
                 */
            } else if (part instanceof IViewPart) {
                logger.log(ECGLevel.PACKET,
                    "A partClosed event has been recorded.");

                part_id.setTextContent(String.valueOf(part.hashCode()));
                part_username.setTextContent(ECGEclipseSensor.this.username);
                part_activity.setTextContent("closed");
                part_partname.setTextContent(part.getTitle());

                processActivity("msdt.part.xsd", 
                        xmlDocumentSerializer.writeToString(msdt_part_doc));

                /* TODO old code, remove if obsolete
                processActivity(
                    "msdt.part.xsd",
                    "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                        + ECGEclipseSensor.this.username
                        + "</username><id>"
                        + part.hashCode()
                        + "</id></commonData><part><activity>closed</activity><partname>"
                        + part.getTitle()
                        + "</partname></part></microActivity>");
                 */
            }

            logger.exiting(this.getClass().getName(), "partClosed");
        }

        /**
         * @see org.eclipse.ui.IPartListener#partDeactivated(org.eclipse.ui.IWorkbenchPart)
         */
        public void partDeactivated(final IWorkbenchPart part) {
            logger.entering(this.getClass().getName(), "partDeactivated",
                new Object[] {part});

            if (part == null) {
                logger.log(Level.FINE,
                    "The Parameter part is null. Ignoring event.");

                logger.exiting(this.getClass().getName(), "partDeactivated");

                return;
            }

            if (part instanceof IEditorPart) {

                logger.log(ECGLevel.PACKET,
                    "An editorDeactivated event has been recorded.");

                editor_id.setTextContent(String.valueOf(part.hashCode()));
                editor_projectname.setTextContent(getProjectnameFromLocation(part.getTitleToolTip()));
                editor_username.setTextContent(ECGEclipseSensor.this.username);
                editor_activity.setTextContent("deactivated");
                editor_editorname.setTextContent(getFilenameFromLocation(part.getTitleToolTip()));

                processActivity("msdt.editor.xsd", 
                        xmlDocumentSerializer.writeToString(msdt_editor_doc));

                /* TODO old code, remove if obsolete
                processActivity(
                    "msdt.editor.xsd",
                    "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                        + ECGEclipseSensor.this.username
                        + "</username><projectname>"
                        + getProjectnameFromLocation(part.getTitleToolTip())
                        + "</projectname><id>"
                        + part.hashCode()
                        + "</id></commonData><editor><activity>deactivated</activity><editorname>"
                        + getFilenameFromLocation(part.getTitleToolTip())
                        + "</editorname></editor></microActivity>");
                 */
            } else if (part instanceof IViewPart) {
                logger.log(ECGLevel.PACKET,
                    "A partDeactivated event has been recorded.");

                part_id.setTextContent(String.valueOf(part.hashCode()));
                part_username.setTextContent(ECGEclipseSensor.this.username);
                part_activity.setTextContent("deactivated");
                part_partname.setTextContent(part.getTitle());

                processActivity("msdt.part.xsd", 
                        xmlDocumentSerializer.writeToString(msdt_part_doc));

                /* TODO old code, remove if obsolete
                processActivity(
                    "msdt.part.xsd",
                    "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                        + ECGEclipseSensor.this.username
                        + "</username><id>"
                        + part.hashCode()
                        + "</id></commonData><part><activity>deactivated</activity><partname>"
                        + part.getTitle()
                        + "</partname></part></microActivity>");
                 */
            }

            logger.exiting(this.getClass().getName(), "partDeactivated");

        }

        /**
         * @see org.eclipse.ui.IPartListener#partOpened(org.eclipse.ui.IWorkbenchPart)
         */
        public void partOpened(final IWorkbenchPart part) {
            logger.entering(this.getClass().getName(), "partOpened",
                new Object[] {part});

            if (part == null) {
                logger.log(Level.FINE,
                    "The Parameter part is null. Ignoring event.");

                logger.exiting(this.getClass().getName(), "partOpened");

                return;
            }

            if (part instanceof IEditorPart) {

                logger.log(ECGLevel.PACKET,
                    "An editorOpened event has been recorded.");
                
                editor_id.setTextContent(String.valueOf(part.hashCode()));
                editor_projectname.setTextContent(getProjectnameFromLocation(part.getTitleToolTip()));
                editor_username.setTextContent(ECGEclipseSensor.this.username);
                editor_activity.setTextContent("opened");
                editor_editorname.setTextContent(getFilenameFromLocation(part.getTitleToolTip()));

                processActivity("msdt.editor.xsd", 
                        xmlDocumentSerializer.writeToString(msdt_editor_doc));

                /* TODO old code, remove if obsolete
                processActivity(
                    "msdt.editor.xsd",
                    "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                        + ECGEclipseSensor.this.username
                        + "</username><projectname>"
                        + getProjectnameFromLocation(part.getTitleToolTip())
                        + "</projectname><id>"
                        + part.hashCode()
                        + "</id></commonData><editor><activity>opened</activity><editorname>"
                        + getFilenameFromLocation(part.getTitleToolTip())
                        + "</editorname></editor></microActivity>");
                 */                

                // TODO The following line is just for exploration
//            	part.getSite().getSelectionProvider().addSelectionChangedListener(new ECGSelectionChangedListener());
                
                if (part instanceof ITextEditor) {
                    final ITextEditor textEditor = (ITextEditor) part;
                    // Register new CCP actions on this editor
                    PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
                        public void run() {
                        	Action action= new ECGTextOperationAction(ResourceBundle.getBundle("org.eclipse.ui.texteditor.ConstructedEditorMessages"), "Editor.Cut.", textEditor, ITextOperationTarget.CUT); //$NON-NLS-1$
                    		textEditor.getEditorSite().getActionBars().setGlobalActionHandler(ActionFactory.CUT.getId(), action);
                    		textEditor.setAction(ITextEditorActionConstants.CUT, action);
                    		
                    		action= new ECGTextOperationAction(ResourceBundle.getBundle("org.eclipse.ui.texteditor.ConstructedEditorMessages"), "Editor.Copy.", textEditor, ITextOperationTarget.COPY); //$NON-NLS-1$
                    		textEditor.getEditorSite().getActionBars().setGlobalActionHandler(ActionFactory.COPY.getId(), action);
                    		textEditor.setAction(ITextEditorActionConstants.COPY, action);
                    		
                    		action= new ECGTextOperationAction(ResourceBundle.getBundle("org.eclipse.ui.texteditor.ConstructedEditorMessages"), "Editor.Paste.", textEditor, ITextOperationTarget.PASTE); //$NON-NLS-1$
                    		textEditor.getEditorSite().getActionBars().setGlobalActionHandler(ActionFactory.PASTE.getId(), action);
                    		textEditor.setAction(ITextEditorActionConstants.PASTE, action);                        	

                            textEditor.getEditorSite().getActionBars().updateActionBars();
                        }
                    });

                    // register document listener on opened Editors
                    IDocumentProvider provider = textEditor.getDocumentProvider();
                    IDocument document = provider.getDocument(textEditor.getEditorInput());
                    document.addDocumentListener(ECGEclipseSensor.this.docListener);
                    // TODO The next line is only for exploration (dirty bit flagged)
//                    textEditor.addPropertyListener(new ECGPropertyListener());
                    // TODO next line is just for exploration (dirty bit flagged, as well)
//                    provider.addElementStateListener(elementStateListener);
                    
                    logger.log(ECGLevel.PACKET, "A code status event has been recorded.");                    

                    codestatus_username.setTextContent(getUsername());
                    codestatus_projectname.setTextContent(getProjectnameFromLocation(textEditor.getTitleToolTip()));
                    codestatus_id.setTextContent(String.valueOf(part.hashCode()));
                    codestatus_contents.setNodeValue(document.get());
                    codestatus_documentname.setTextContent(getFilenameFromLocation(textEditor.getTitleToolTip()));

                    processActivity("msdt.codestatus.xsd", 
                        xmlDocumentSerializer.writeToString(msdt_codestatus_doc));                    

                    /* TODO old code, remove if obsolete
                    processActivity(
                            "msdt.codestatus.xsd",
                            "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                                + getUsername()
                                + "</username><projectname>"
                                + getProjectnameFromLocation(textEditor.getTitleToolTip())
                                + "</projectname><id>"
                                + part.hashCode()
                                + "</id></commonData><codestatus><document><![CDATA["
                                + document.get()
                                + "]" + "]" + "></document><documentname>"
                                + getFilenameFromLocation(textEditor.getTitleToolTip())
                                + "</documentname></codestatus></microActivity>");                    
                     */                
                }


            } else if (part instanceof IViewPart) {

                logger.log(ECGLevel.PACKET,
                    "A partOpened event has been recorded.");

                part_id.setTextContent(String.valueOf(part.hashCode()));
                part_username.setTextContent(ECGEclipseSensor.this.username);
                part_activity.setTextContent("opened");
                part_partname.setTextContent(part.getTitle());

                processActivity("msdt.part.xsd", 
                        xmlDocumentSerializer.writeToString(msdt_part_doc));

                /* TODO old code, remove if obsolete
                processActivity(
                    "msdt.part.xsd",
                    "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                        + ECGEclipseSensor.this.username
                        + "</username><id>"
                        + part.hashCode()
                        + "</id></commonData><part><activity>opened</activity><partname>"
                        + part.getTitle()
                        + "</partname></part></microActivity>");
                 */                
                ECGEclipseSensor.this.activeView = (IViewPart)part;
            }

            logger.exiting(this.getClass().getName(), "partOpened");
        }

        /**
         * @see org.eclipse.ui.IPartListener#partBroughtToTop(org.eclipse.ui.IWorkbenchPart)
         */
        public void partBroughtToTop(IWorkbenchPart part) {
            // not used
        }
    }

    /**
     * This is listening for events about changes in the text of open
     * documents.
     */
    private class ECGDocumentListener implements IDocumentListener {

        /**
         * This is used to wait a moment after a
         * <em>DocumentChanged</em> event has been recorded. Only
         * when the user has not changed the document for
         * {@link ECGEclipseSensor#CODECHANGE_INTERVALL} amount of
         * time, a <em>Codechange</em> event is sent.
         */
        private Timer timer = null;

        /**
         * Creates the <em>DocumentListenerAdapter</em> and the
         * <code>Timer</code>.
         */
        @SuppressWarnings("synthetic-access")
        public ECGDocumentListener() {
            logger.entering(this.getClass().getName(),
                "DocumentListenerAdapter");

            this.timer = new Timer();

            logger.exiting(this.getClass().getName(), "DocumentListenerAdapter");
        }

        /**
         * @see org.eclipse.jface.text.IDocumentListener#documentAboutToBeChanged(org.eclipse.jface.text.DocumentEvent)
         */
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
        public void documentChanged(final DocumentEvent event) {
            logger.entering(this.getClass().getName(), "documentChanged",
                new Object[] {event});

            if (event == null) {
                logger.log(Level.FINE,
                    "The Parameter event null. Ignoring event.");

                logger.exiting(this.getClass().getName(), "documentChanged");

                return;
            }

            this.timer.cancel();

            this.timer = new Timer(); // TODO: Is it a good idea to create a  new timer every now and then (= on each key stroke!)?

            this.timer.schedule(new CodeChangeTimerTask(event.getDocument(),
                ECGEclipseSensor.this.activeTextEditor),
                ECGEclipseSensor.CODECHANGE_INTERVAL);

            logger.exiting(this.getClass().getName(), "documentChanged");
        }
    }

    /**
     * This is listening for events about tests and testruns.
     */
    private class ECGTestListener implements ITestRunListener {

        /**
         * Stores the number of individual tests in the current
         * testrun.
         */
        private int currentTestCount;

        /**
         * @see org.eclipse.jdt.junit.ITestRunListener#testRunStarted(int)
         */
        public void testRunStarted(final int testCount) {

            logger.entering(this.getClass().getName(), "testRunStarted",
                new Object[] {new Integer(testCount)});

            this.currentTestCount = testCount;

            logger.log(ECGLevel.PACKET,
                "An testRunStarted event has been recorded.");

            processActivity(
                "msdt.testrun.xsd",
                "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                    + ECGEclipseSensor.this.username
                    + "</username><id>"
                    + this.hashCode()
                    + "</id></commonData><testrun><activity>started</activity><elapsedtime>0</elapsedtime><testcount>"
                    + testCount
                    + "</testcount></testrun></microActivity>");

            logger.exiting(this.getClass().getName(), "testRunStarted");

        }

        /**
         * @see org.eclipse.jdt.junit.ITestRunListener#testRunEnded(long)
         */
        public void testRunEnded(final long elapsedTime) {
            logger.entering(this.getClass().getName(), "testRunEnded",
                new Object[] {new Long(elapsedTime)});

            logger.log(ECGLevel.PACKET,
                "An testRunEnded event has been recorded.");

            processActivity(
                "msdt.testrun.xsd",
                "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                    + ECGEclipseSensor.this.username
                    + "</username><id>"
                    + this.hashCode()
                    + "</id></commonData><testrun><activity>ended</activity><elapsedtime>"
                    + elapsedTime + "</elapsedtime><testcount>"
                    + this.currentTestCount
                    + "</testcount></testrun></microActivity>");

            logger.exiting(this.getClass().getName(), "testRunEnded");

        }

        /**
         * @see org.eclipse.jdt.junit.ITestRunListener#testRunStopped(long)
         */
        public void testRunStopped(final long elapsedTime) {
            logger.entering(this.getClass().getName(), "testRunStopped",
                new Object[] {new Long(elapsedTime)});

            logger.log(ECGLevel.PACKET,
                "An testRunStopped event has been recorded.");

            processActivity(
                "msdt.testrun.xsd",
                "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                    + ECGEclipseSensor.this.username
                    + "</username><projectname>"
                    + ECGEclipseSensor.this.projectname
                    + "</projectname></commonData><testrun><activity>stopped</activity><elapsedtime>"
                    + elapsedTime + "</elapsedtime><testcount>"
                    + this.currentTestCount
                    + "</testcount></testrun></microActivity>");

            logger.exiting(this.getClass().getName(), "testRunStopped");

        }

        /**
         * @see org.eclipse.jdt.junit.ITestRunListener#testRunTerminated()
         */
        public void testRunTerminated() {
            logger.entering(this.getClass().getName(), "testRunTerminated");

            logger.log(ECGLevel.PACKET,
                "An testRunTerminated event has been recorded.");

            processActivity(
                "msdt.testrun.xsd",
                "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                    + ECGEclipseSensor.this.username
                    + "</username><id>"
                    + this.hashCode()
                    + "</id></commonData><testrun><activity>terminated</activity><elapsedtime>0</elapsedtime><testcount>"
                    + this.currentTestCount
                    + "</testcount></testrun></microActivity>");

            logger.exiting(this.getClass().getName(), "testRunTerminated");

        }

        /**
         * @see org.eclipse.jdt.junit.ITestRunListener#testStarted(java.lang.String,
         *      java.lang.String)
         */
        public void testStarted(final String testId, final String testName) {
            logger.entering(this.getClass().getName(), "testStarted",
                new Object[] {testId, testName});

            logger.log(ECGLevel.PACKET,
                "An testStarted event has been recorded.");

            processActivity(
                "msdt.test.xsd",
                "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                    + ECGEclipseSensor.this.username
                    + "</username><id>"
                    + testId
                    + "</id></commonData><test><activity>started</activity><name>"
                    + testName
                    + "</name><id>"
                    + testId
                    + "</id><status>OK</status></test></microActivity>");

            logger.exiting(this.getClass().getName(), "testStarted");

        }

        /**
         * @see org.eclipse.jdt.junit.ITestRunListener#testEnded(java.lang.String,
         *      java.lang.String)
         */
        public void testEnded(final String testId, final String testName) {
            logger.entering(this.getClass().getName(), "testEnded",
                new Object[] {testId, testName});

            logger
                .log(ECGLevel.PACKET, "An testEnded event has been recorded.");

            processActivity(
                "msdt.test.xsd",
                "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                    + ECGEclipseSensor.this.username
                    + "</username><id>"
                    + testId
                    + "</id></commonData><test><activity>ended</activity><name>"
                    + testName
                    + "</name><id>"
                    + testId
                    + "</id><status>OK</status></test></microActivity>");

            logger.exiting(this.getClass().getName(), "testEnded");

        }

        /**
         * @see org.eclipse.jdt.junit.ITestRunListener#testFailed(int,
         *      java.lang.String, java.lang.String, java.lang.String)
         */
        public void testFailed(final int status, final String testId,
            final String testName, final String trace) {
            logger.entering(this.getClass().getName(), "testFailed",
                new Object[] {testId, testName, new Integer(status), trace});

            String statusString;

            switch (status) {
                case ITestRunListener.STATUS_OK:

                    statusString = "OK";

                    break;

                case ITestRunListener.STATUS_ERROR:

                    statusString = "ERROR";

                    break;

                case ITestRunListener.STATUS_FAILURE:

                    statusString = "FAILURE";

                    break;

                default:

                    statusString = "";

                    break;
            }

            logger.log(ECGLevel.PACKET,
                "An testFailed event has been recorded.");

            processActivity(
                "msdt.test.xsd",
                "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                    + ECGEclipseSensor.this.username
                    + "</username><id>"
                    + testId
                    + "</id></commonData><test><activity>failed</activity><name>"
                    + testName + "</name><id>" + testId
                    + "</id><status>" + statusString
                    + "</status></test></microActivity>");

            logger.exiting(this.getClass().getName(), "testFailed");

        }

        /**
         * @see org.eclipse.jdt.junit.ITestRunListener#testReran(java.lang.String,
         *      java.lang.String, java.lang.String, int,
         *      java.lang.String)
         */
        public void testReran(final String testId, @SuppressWarnings("unused")
        final String testClass, final String testName, final int status,
            final String trace) {
            logger.entering(this.getClass().getName(), "testReran",
                new Object[] {testId, testName, new Integer(status), trace});

            String statusString;

            switch (status) {
                case ITestRunListener.STATUS_OK:

                    statusString = "OK";

                    break;

                case ITestRunListener.STATUS_ERROR:

                    statusString = "ERROR";

                    break;

                case ITestRunListener.STATUS_FAILURE:

                    statusString = "FAILURE";

                    break;

                default:

                    statusString = "";

                    break;
            }

            logger
                .log(ECGLevel.PACKET, "An testReran event has been recorded.");

            processActivity(
                "msdt.test.xsd",
                "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                    + ECGEclipseSensor.this.username
                    + "</username><id>"
                    + testId
                    + "</id></commonData><test><activity>reran</activity><name>"
                    + testName + "</name><id>" + testId
                    + "</id><status>" + statusString
                    + "</status></test></microActivity>");

            logger.exiting(this.getClass().getName(), "testReran");

        }

    }
    
    /**
     * Listens for events on the windows shell, i.e. Eclipse's main window
     * Note: No shell opened event exists. It is send on startup explicitely
     */
    private class ECGShellListener implements ShellListener {

        private Document msdt_window_doc;
        
        private Element window_username;
        private Element window_projectname;
        private Element window_id;        
        private Element window_activity;
        private Element window_windowname;
        
        public ECGShellListener() {

            try {
                // initialize DOM skeleton for msdt.editor.xsd
                msdt_window_doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

                Element window_microactivity = msdt_window_doc.createElement("microActivity");                
                Element window_commondata = msdt_window_doc.createElement("commonData");
                Element window_window = msdt_window_doc.createElement("window");
                window_username = msdt_window_doc.createElement("username");
                window_id = msdt_window_doc.createElement("id");
                window_activity = msdt_window_doc.createElement("activity");
                window_windowname = msdt_window_doc.createElement("windowname");

                msdt_window_doc.appendChild(window_microactivity);
                window_microactivity.appendChild(window_commondata);
                window_commondata.appendChild(window_username);
                window_commondata.appendChild(window_id);
                window_microactivity.appendChild(window_window);
                window_window.appendChild(window_activity);
                window_window.appendChild(window_windowname);

            } catch (ParserConfigurationException e) {
                logger.log(Level.SEVERE,
                    "Could not instantiate the DOM Document.");
                logger.log(Level.FINE, e.getMessage());
            }

        }

        /**
         * @see org.eclipse.swt.events.ShellListener#shellActivated(org.eclipse.swt.events.ShellEvent)
         */
        public void shellActivated(ShellEvent e) {
            logger.entering(this.getClass().getName(), "shellActivated",
                    new Object[] {e});

            Shell shell = ((Shell)e.widget);
        
            if (shell == null) {
                logger.log(Level.FINE,
                    "The Parameter  \"e.widget\" is null. Ignoring event.");
                logger.exiting(this.getClass().getName(), "shellActivated");
                return;
            }

            logger.log(ECGLevel.PACKET,
                "A windowActivated event has been recorded.");

            window_username.setTextContent(ECGEclipseSensor.this.username);
            window_id.setTextContent(String.valueOf(shell.hashCode()));
            window_activity.setTextContent("activated");
            window_windowname.setTextContent(shell.getText());
            
            processActivity("msdt.window.xsd",
                    xmlDocumentSerializer.writeToString(msdt_window_doc));

            /* TODO old code, remove if obsolete
            processActivity(
                "msdt.window.xsd",
                "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                    + ECGEclipseSensor.this.username
                    + "</username><id>"
                    + shell.hashCode()
                    + "</id></commonData><window><activity>activated</activity><windowname>"
                    + shell.getText()
                    + "</windowname></window></microActivity>");
             */
            logger.exiting(this.getClass().getName(), "shellActivated");
        }

        /**
         * @see org.eclipse.swt.events.ShellListener#shellClosed(org.eclipse.swt.events.ShellEvent)
         */
        public void shellClosed(ShellEvent e) {
            logger.entering(this.getClass().getName(), "shellClosed", new Object[] {e});

            Shell shell = ((Shell)e.widget);
        
            if (shell == null) {
                logger.log(Level.FINE,
                    "The Parameter  \"e.widget\" is null. Ignoring event.");
                logger.exiting(this.getClass().getName(), "shellClosed");
                return;
            }

            logger.log(ECGLevel.PACKET,
                "A windowClosed event has been recorded.");

            window_username.setTextContent(ECGEclipseSensor.this.username);
            window_id.setTextContent(String.valueOf(shell.hashCode()));
            window_activity.setTextContent("closed");
            window_windowname.setTextContent((!shell.isDisposed() ? shell.getText() : "Eclipse"));
            
            processActivity("msdt.window.xsd",
                    xmlDocumentSerializer.writeToString(msdt_window_doc));
            
            /* TODO old code, remove if obsolete
            processActivity(
                "msdt.window.xsd",
                "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                    + ECGEclipseSensor.this.username
                    + "</username><id>"
                    + shell.hashCode()
                    + "</id></commonData><window><activity>closed</activity><windowname>"
                    + (!shell.isDisposed() ? shell.getText() : "Eclipse")
                    + "</windowname></window></microActivity>");
             */
            logger.exiting(this.getClass().getName(), "shellClosed");
        }

        /**
         * @see org.eclipse.swt.events.ShellListener#shellDeactivated(org.eclipse.swt.events.ShellEvent)
         */
        public void shellDeactivated(ShellEvent e) {
            logger.entering(this.getClass().getName(), "shellDeactivated", new Object[] {e});

            Shell shell = ((Shell)e.widget);
        
            if (shell == null) {
                logger.log(Level.FINE,
                    "The Parameter  \"e.widget\" is null. Ignoring event.");
                logger.exiting(this.getClass().getName(), "shellDeactivated");
                return;
            }

            logger.log(ECGLevel.PACKET,
                "A windowDeactivated event has been recorded.");

            window_username.setTextContent(ECGEclipseSensor.this.username);
            window_id.setTextContent(String.valueOf(shell.hashCode()));
            window_activity.setTextContent("deactivated");
            window_windowname.setTextContent(shell.getText());
            
            processActivity("msdt.window.xsd",
                    xmlDocumentSerializer.writeToString(msdt_window_doc));

            /* TODO old code, remove if obsolete
            processActivity(
                "msdt.window.xsd",
                "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                    + ECGEclipseSensor.this.username
                    + "</username><id>"
                    + shell.hashCode()
                    + "</id></commonData><window><activity>deactivated</activity><windowname>"
                    + shell.getText()
                    + "</windowname></window></microActivity>");
             */
            logger.exiting(this.getClass().getName(), "shellDeactivated");
        }

        /**
         * @see org.eclipse.swt.events.ShellListener#shellDeiconified(org.eclipse.swt.events.ShellEvent)
         */
        public void shellDeiconified(ShellEvent e) {
            // not used by ECG
        }

        /**
         * @see org.eclipse.swt.events.ShellListener#shellIconified(org.eclipse.swt.events.ShellEvent)
         */
        public void shellIconified(ShellEvent e) {
            // not used by ECG
        }

        /**
         * Unfortunately, this is no official ShellListener handler
         */
        public void shellOpened(Shell shell) {
            logger.entering(this.getClass().getName(), "shellOpened", new Object[] {shell});

            if (shell == null) {
                logger.log(Level.FINE,
                    "The Parameter  \"shell\" is null. Ignoring event.");
                logger.exiting(this.getClass().getName(), "shellOpened");
                return;
            }

            logger.log(ECGLevel.PACKET,
                "A windowOpened event has been recorded.");

            window_username.setTextContent(ECGEclipseSensor.this.username);
            window_id.setTextContent(String.valueOf(shell.hashCode()));
            window_activity.setTextContent("opened");
            window_windowname.setTextContent(shell.getText());
            
            processActivity("msdt.window.xsd",
                    xmlDocumentSerializer.writeToString(msdt_window_doc));

            /* TODO old code, remove if obsolete
            processActivity(
                "msdt.window.xsd",
                "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                    + ECGEclipseSensor.this.username
                    + "</username><id>"
                    + shell.hashCode()
                    + "</id></commonData><window><activity>opened</activity><windowname>"
                    + shell.getText()
                    + "</windowname></window></microActivity>");
             */
            logger.exiting(this.getClass().getName(), "shellOpened");
        }

    }

    /**
     * Listens for events on the display. It's used for cathing windows others than
     * the main window, i.e. dialogs
     */
    private class ECGDisplayListener implements Listener {

        /**
         * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
         */
        public void handleEvent(Event event) {
            logger.entering(this.getClass().getName(), "handleEvent", new Object[] {event});

            if (event == null || event.widget == null) {
                // could happen, don't know why
                logger.exiting(this.getClass().getName(), "handleEvent");
                return;                
            }
            if (event.widget.getClass() == org.eclipse.swt.widgets.Shell.class) {
                Shell shell = (Shell)event.widget;
                // is it a dialog below the main window or another dialog
                if (shell.getParent() == null) {
                    // seems to be main window which is handeled by ShellListener
                    logger.exiting(this.getClass().getName(), "handleEvent");
                    return;
                }
                if (event.type == SWT.Activate) {
                    // in case of Activate, it may be opened the first time. Check if shell is known
                    Shell foundOpenedShell = null;
                    for (Iterator it = openDialogs.iterator(); it.hasNext() && foundOpenedShell == null; ) {
                        Shell s = (Shell)it.next();
                        if (s.equals(shell))
                            foundOpenedShell = s;
                    }
                    if (foundOpenedShell == null) {
                        // new shell, send opened event
                        openDialogs.add(shell);
                        // send window open as well
                        logger.log(ECGLevel.PACKET,
                            "A dialogOpened event has been recorded.");
                        processActivity(
                            "msdt.dialog.xsd",
                            "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                                + ECGEclipseSensor.this.username
                                + "</username><id>"
                                + shell.hashCode()
                                + "</id></commonData><dialog><activity>opened</activity><dialogname>"
                                + shell.getText()
                                + "</dialogname></dialog></microActivity>");
                    }
                    // finally send activate event
                    logger.log(ECGLevel.PACKET,
                        "A dialogActivated event has been recorded.");
                    processActivity(
                        "msdt.dialog.xsd",
                        "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                            + ECGEclipseSensor.this.username
                            + "</username><id>"
                            + shell.hashCode()
                            + "</id></commonData><dialog><activity>activated</activity><dialogname>"
                            + shell.getText()
                            + "</dialogname></dialog></microActivity>");
                }
                else if (event.type == SWT.Deactivate) {
                    logger.log(ECGLevel.PACKET,
                        "A dialogDeactivated event has been recorded.");
                    processActivity(
                        "msdt.dialog.xsd",
                        "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                            + ECGEclipseSensor.this.username
                            + "</username><id>"
                            + shell.hashCode()
                            + "</id></commonData><dialog><activity>deactivated</activity><dialogname>"
                            + shell.getText()
                            + "</dialogname></dialog></microActivity>");
                }
                else if (event.type == SWT.Dispose) {
                    // in case of Dispose, remove dialog from opened dialogs
                    if (openDialogs.remove(shell)) {
                        // if known opened dialog send deactivate first
                        logger.log(ECGLevel.PACKET,
                            "A dialogDeactivated event has been recorded.");
                        processActivity(
                            "msdt.dialog.xsd",
                            "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                                + ECGEclipseSensor.this.username
                                + "</username><id>"
                                + shell.hashCode()
                                + "</id></commonData><dialog><activity>deactivated</activity><dialogname>"
                                + shell.getText()
                                + "</dialogname></dialog></microActivity>");
                        // ...and closed afterwards
                        logger.log(ECGLevel.PACKET,
                            "A dialogClosed event has been recorded.");
                        processActivity(
                            "msdt.dialog.xsd",
                            "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                                + ECGEclipseSensor.this.username
                                + "</username><id>"
                                + shell.hashCode()
                                + "</id></commonData><dialog><activity>closed</activity><dialogname>"
                                + shell.getText()
                                + "</dialogname></dialog></microActivity>");
                    }
                }
            }

            logger.exiting(this.getClass().getName(), "handleEvent");
        }

    }

    /**
	 * TODO Not used 
     * Records background jobs. Mostly of no interest, but may be the only way
	 * to register on Team activities like commits
	 */
    private class ECGJobListener implements IJobChangeListener {

		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.jobs.IJobChangeListener#aboutToRun(org.eclipse.core.runtime.jobs.IJobChangeEvent)
		 */
		public void aboutToRun(IJobChangeEvent event) {
			// not useful
		}

		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.jobs.IJobChangeListener#awake(org.eclipse.core.runtime.jobs.IJobChangeEvent)
		 */
		public void awake(IJobChangeEvent event) {
//			logger.log(ECGLevel.INFO, "awake: " + event.getJob());

		}

		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.jobs.IJobChangeListener#done(org.eclipse.core.runtime.jobs.IJobChangeEvent)
		 */
		public void done(IJobChangeEvent event) {
//			logger.log(ECGLevel.INFO, "done: " + event.getJob());

		}

		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.jobs.IJobChangeListener#running(org.eclipse.core.runtime.jobs.IJobChangeEvent)
		 */
		public void running(IJobChangeEvent event) {
			// not useful
		}

		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.jobs.IJobChangeListener#scheduled(org.eclipse.core.runtime.jobs.IJobChangeEvent)
		 */
		public void scheduled(IJobChangeEvent event) {
			// not useful
		}

		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.jobs.IJobChangeListener#sleeping(org.eclipse.core.runtime.jobs.IJobChangeEvent)
		 */
		public void sleeping(IJobChangeEvent event) {
			// not useful
		}
	}

	/**
	 * TODO Not Used 
     * Reports on FileEditorInput which is a rather weak place to listen to. It's something
	 * between the Editor and the File. It allows for listening to the dirty bit of a 
	 * Document Provider
	 */
    private class ECGElementStateListener implements IElementStateListener {

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
			logger.log(ECGLevel.INFO, "docDeleted: " + element);

		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.texteditor.IElementStateListener#elementDirtyStateChanged(java.lang.Object, boolean)
		 */
		public void elementDirtyStateChanged(Object element, boolean isDirty) {
			logger.log(ECGLevel.INFO, "docDirtyStateChanged: " + element);

		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.texteditor.IElementStateListener#elementMoved(java.lang.Object, java.lang.Object)
		 */
		public void elementMoved(Object originalElement, Object movedElement) {
			logger.log(ECGLevel.INFO, "docMoved: " + originalElement + "->" + movedElement);

		}

	}

	/**
	 * FileBuffers are internal Caches for the actual typed contents of a displayed
	 * (text) file. On Save, the underlying file is overwritten by the buffers contents.
	 * This is a good place to recognize ditry events and also changes on the
	 * underlying file 
	 *
	 */
    private class ECGFileBufferListener implements IFileBufferListener {

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

            logger.entering(this.getClass().getName(), "dirtyStateChanged", new Object[] {buffer, isDirty});
			
            if (!isDirty) {

            	logger.log(ECGLevel.PACKET, "A resourceSaved event has been recorded.");

                processActivity(
                    "msdt.resource.xsd",
                    "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                        + ECGEclipseSensor.this.username
                        + "</username><projectname>"
                        + getProjectnameFromLocation(buffer.getLocation().toString())
                        + "</projectname><id>"
                        + buffer.hashCode()
                        + "</id></commonData><resource><activity>saved</activity><resourcename>"
                        + getFilenameFromLocation(buffer.getLocation().toString())
                        + "</resourcename></resource></microActivity>");

            }

            logger.exiting(this.getClass().getName(), "dirtyStateChanged");

		}

		/* (non-Javadoc)
		 * @see org.eclipse.core.filebuffers.IFileBufferListener#underlyingFileDeleted(org.eclipse.core.filebuffers.IFileBuffer)
		 */
		public void underlyingFileDeleted(IFileBuffer buffer) {

			logger.entering(this.getClass().getName(), "underlyingFileDeleted", new Object[] {buffer});
			
        	logger.log(ECGLevel.PACKET, "A resourceDeleted event has been recorded.");

            processActivity(
                "msdt.resource.xsd",
                "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                    + ECGEclipseSensor.this.username
                    + "</username><projectname>"
                    + getProjectnameFromLocation(buffer.getLocation().toString())
                    + "</projectname><id>"
                    + buffer.hashCode()
                    + "</id></commonData><resource><activity>deleted</activity><resourcename>"
                    + getFilenameFromLocation(buffer.getLocation().toString())
                    + "</resourcename></resource></microActivity>");

            logger.exiting(this.getClass().getName(), "underlyingFileDeleted");

		}

		/* (non-Javadoc)
		 * @see org.eclipse.core.filebuffers.IFileBufferListener#underlyingFileMoved(org.eclipse.core.filebuffers.IFileBuffer, org.eclipse.core.runtime.IPath)
		 */
		public void underlyingFileMoved(IFileBuffer buffer, IPath path) {

			logger.entering(this.getClass().getName(), "underlyingFileMoved", new Object[] {buffer});
			
        	logger.log(ECGLevel.PACKET, "A resourceDeleted event has been recorded.");

            processActivity(
                "msdt.resource.xsd",
                "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                    + ECGEclipseSensor.this.username
                    + "</username><projectname>" 
                    + getProjectnameFromLocation(buffer.getLocation().toString())
                    + "</projectname><id>"
                    + buffer.hashCode()
                    + "</id></commonData><resource><activity>moved</activity><resourcename>"
                    + getFilenameFromLocation(buffer.getLocation().toString())
                    + "</resourcename></resource></microActivity>");

            logger.exiting(this.getClass().getName(), "underlyingFileDeleted");

		}

	}

	/**
	 * This listens on perperty chnages of ITextEditors. Using Properties, 
	 * mostly visible properties are controlled, like titles or the * to indicate
	 * dirty files.  
	 */
    private class ECGPropertyListener implements IPropertyListener {

		/* (non-Javadoc)
		 * @see org.eclipse.ui.IPropertyListener#propertyChanged(java.lang.Object, int)
		 */
		public void propertyChanged(Object source, int propId) {
			if (propId == IWorkbenchPartConstants.PROP_DIRTY && source instanceof ISaveablePart) {
				logger.log(ECGLevel.INFO, "propertyChanged: at " + source + " dirty is now " + String.valueOf(((ISaveablePart)source).isDirty()));
			}
		}
	}
	
	
	/**
     * This <em>TimerTask</em> is used in creating
     * <em>Codechange</em> events. In <em>Eclipse</em> every time
     * the user changes a single character in the active document an
     * <em>DocumentChanged</em> event is fired. To avoid sending
     * <em>Codechange</em> this often, the sensor shall wait for an
     * amount of time after before sending a <em>Codechange</em>.
     * Only when the user does not change the document's text for
     * {@link ECGEclipseSensor#CODECHANGE_INTERVAL} time, a
     * <em>Codechange</em> is sent to the ECG Lab.
     */
    private static class CodeChangeTimerTask extends TimerTask {

        private Document msdt_codechange_doc;

        private Element codechange_username;
        private Element codechange_projectname;
        private Element codechange_id;        
        private Element codechange_document;
        private CDATASection codechange_contents;
        private Element codechange_documentname;
        
        /**
         * This is the document that has been changed.
         */
        private IDocument doc;

        /**
         * The enclosing editor of the document.
         */
        private IEditorPart textEditor;

        /**
         * This creates the <em>TimerTask</em>.
         * @param document
         *            Is the document that has been changed
         * @param documentName
         *            Is the name of the document
         */
        @SuppressWarnings("synthetic-access")
        public CodeChangeTimerTask(final IDocument document, final IEditorPart textEditor) {
            logger.entering(this.getClass().getName(), "CodeChangeTimerTask",
                new Object[] {document, textEditor});

            this.doc = document;
            this.textEditor = textEditor;

            try {
                
                // initialize DOM skeleton for msdt.codechange.xsd
                msdt_codechange_doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
                Element codechange_microactivity = msdt_codechange_doc.createElement("microActivity");                
                Element codechange_commondata = msdt_codechange_doc.createElement("commonData");
                Element codechange_codechange = msdt_codechange_doc.createElement("codechange");
                codechange_username = msdt_codechange_doc.createElement("username");
                codechange_projectname = msdt_codechange_doc.createElement("projectname");
                codechange_id = msdt_codechange_doc.createElement("id");
                codechange_document = msdt_codechange_doc.createElement("document");
                codechange_contents = msdt_codechange_doc.createCDATASection("");
                codechange_documentname = msdt_codechange_doc.createElement("documentname");

                msdt_codechange_doc.appendChild(codechange_microactivity);
                  codechange_microactivity.appendChild(codechange_commondata);
                    codechange_commondata.appendChild(codechange_username);
                    codechange_commondata.appendChild(codechange_projectname);
                    codechange_commondata.appendChild(codechange_id);
                  codechange_microactivity.appendChild(codechange_codechange);
                    codechange_codechange.appendChild(codechange_document);
                      codechange_document.appendChild(codechange_contents);
                    codechange_codechange.appendChild(codechange_documentname);
                                        
            } catch (ParserConfigurationException e) {
                logger.log(Level.SEVERE,
                    "Could not instantiate the DOM Document.");
                logger.log(Level.FINE, e.getMessage());
            }
            
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

            codechange_username.setTextContent(sensor.getUsername());
            codechange_projectname.setTextContent(sensor.getProjectnameFromLocation(textEditor.getTitleToolTip()));
            codechange_id.setTextContent(String.valueOf(textEditor.hashCode()));
            codechange_contents.setNodeValue(this.doc.get());
            codechange_documentname.setTextContent(sensor.getFilenameFromLocation(textEditor.getTitleToolTip()));

            sensor.processActivity("msdt.codechange.xsd", 
                sensor.xmlDocumentSerializer.writeToString(msdt_codechange_doc));                    
            
            /* TODO Obsolete code
            sensor.processActivity(
                "msdt.codechange.xsd",
                "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                    + sensor.getUsername()
                    + "</username><projectname>"
                    + sensor.getProjectnameFromLocation(textEditor.getTitleToolTip())
                    + "</projectname><id>"
                    + textEditor.hashCode()
                    + "</id></commonData><codechange><document><![CDATA["
                    + this.doc.get()
                    + "]" + "]" + "></document><documentname>"
                    + sensor.getFilenameFromLocation(textEditor.getTitleToolTip())
                    + "</documentname></codechange></microActivity>");
             */
            
            logger.exiting(this.getClass().getName(), "run");

        }
    }

    /**
     * Actions for Copy/Cut/Paste events. This action is registered on COPY/CUT/PASTE action
     * ids on every opened TextEditor.
     * 
     * Mainly copied from org.eclipse.jdt.internal.ui.javaeditor.ClipboardOperationAction
     * which replaces the usual CCP actions with enhanced ones
     */
    public final class ECGTextOperationAction extends TextEditorAction {
    	
    	/** The text operation code */
    	private int fOperationCode= -1;
    	/** The text operation target */
    	private ITextOperationTarget fOperationTarget;

        private Document msdt_user_doc;
        
        private Element user_username;
        private Element user_projectname;
        private Element user_activity;
        private Element user_param1;
        private Element user_param2;
        private CDATASection user_param2_contents;
        private Element user_param3;
        private CDATASection user_param3_contents;
        
    	
    	/**
    	 * Creates the action.
    	 */
    	public ECGTextOperationAction(ResourceBundle bundle, String prefix, ITextEditor editor, int operationCode) {
    		super(bundle, prefix, editor);
    		fOperationCode= operationCode;
    		
            // initialize DOM skeleton for msdt.editor.xsd
            try {
                msdt_user_doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
                Element user_microactivity = msdt_user_doc.createElement("microActivity");                
                Element user_commondata = msdt_user_doc.createElement("commonData");
                Element user_user = msdt_user_doc.createElement("user");
                user_username = msdt_user_doc.createElement("username");
                user_projectname = msdt_user_doc.createElement("projectname");
                user_activity = msdt_user_doc.createElement("activity");
                user_param1 = msdt_user_doc.createElement("param1");
                user_param2 = msdt_user_doc.createElement("param2");
                user_param2_contents = msdt_user_doc.createCDATASection("");
                user_param3 = msdt_user_doc.createElement("param3");
                user_param3_contents = msdt_user_doc.createCDATASection("");

                msdt_user_doc.appendChild(user_microactivity);
                  user_microactivity.appendChild(user_commondata);
                    user_commondata.appendChild(user_username);
                    user_commondata.appendChild(user_projectname);
                  user_microactivity.appendChild(user_user);
                    user_user.appendChild(user_activity);
                    user_user.appendChild(user_param1);
                    user_user.appendChild(user_param2);
                      user_param2.appendChild(user_param2_contents);
                    user_user.appendChild(user_param3);
                      user_param3.appendChild(user_param3_contents);
            } catch (ParserConfigurationException e) {
                logger.log(Level.SEVERE,
                    "Could not instantiate the DOM Document in ECGTextOperationAction.");
                logger.log(Level.FINE, e.getMessage());
            }

            // Register action
    		if (operationCode == ITextOperationTarget.CUT) {
    			setHelpContextId(IAbstractTextEditorHelpContextIds.CUT_ACTION);
    			setActionDefinitionId(ITextEditorActionDefinitionIds.CUT);
    		} else if (operationCode == ITextOperationTarget.COPY) {
    			setHelpContextId(IAbstractTextEditorHelpContextIds.COPY_ACTION);
    			setActionDefinitionId(ITextEditorActionDefinitionIds.COPY);
    		} else if (operationCode == ITextOperationTarget.PASTE) {
    			setHelpContextId(IAbstractTextEditorHelpContextIds.PASTE_ACTION);
    			setActionDefinitionId(ITextEditorActionDefinitionIds.PASTE);
    		} else {
    			Assert.isTrue(false, "Invalid operation code"); //$NON-NLS-1$
    		}
    		update();
    	}
    	
    	private boolean isReadOnlyOperation() {
    		return fOperationCode == ITextOperationTarget.COPY;
    	}

    	
    	/* (non-Javadoc)
    	 * @see org.eclipse.jface.action.IAction#run()
    	 */
    	public void run() {
    		if (fOperationCode == -1 || fOperationTarget == null)
    			return;
    			
    		ITextEditor editor= getTextEditor();

    		if (editor == null)
    			return;

    		if (!isReadOnlyOperation() && !validateEditorInputState())
    			return;

    		Clipboard clipboard= new Clipboard(getDisplay());
    		TextTransfer textTransfer = TextTransfer.getInstance();
    		ISelection sel = editor.getSelectionProvider().getSelection();
    		String selection = (sel instanceof TextSelection ? ((TextSelection)sel).getText() : "");
    		fOperationTarget.doOperation(fOperationCode);
    		if (fOperationCode == ITextOperationTarget.CUT) {
    			logger.log(ECGLevel.PACKET, "A Cut operation has been recorded");

                user_projectname.setTextContent(getProjectnameFromLocation(editor.getTitleToolTip()));
                user_username.setTextContent(getUsername());
                user_activity.setTextContent("cut");
                user_param1.setTextContent(getFilenameFromLocation(editor.getTitleToolTip()));
                user_param2_contents.setNodeValue(selection);
                user_param3_contents.setNodeValue("");

                processActivity("msdt.user.xsd", 
                        xmlDocumentSerializer.writeToString(msdt_user_doc));
/*
                processActivity(
                    "msdt.user.xsd",
                    "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                        + getUsername()
                        + "</username><projectname>"
                        + getProjectnameFromLocation(editor.getTitleToolTip())
                        + "</projectname></commonData><user><activity>cut</activity><param1>"
                        + getFilenameFromLocation(editor.getTitleToolTip())
                        + "</param1><param2><![CDATA["
                        + selection
                        + "]" + "]" + "></param2></user></microActivity>");
*/
    		} else if (fOperationCode == ITextOperationTarget.COPY) {
    			logger.log(ECGLevel.PACKET, "A Copy operation has been recorded");

                user_projectname.setTextContent(getProjectnameFromLocation(editor.getTitleToolTip()));
                user_username.setTextContent(getUsername());
                user_activity.setTextContent("copy");
                user_param1.setTextContent(getFilenameFromLocation(editor.getTitleToolTip()));
                user_param2_contents.setNodeValue(selection);
                user_param3_contents.setNodeValue("");

                processActivity("msdt.user.xsd", 
                        xmlDocumentSerializer.writeToString(msdt_user_doc));
/*
                processActivity(
                    "msdt.user.xsd",
                    "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                        + getUsername()
                        + "</username><projectname>"
                        + getProjectnameFromLocation(editor.getTitleToolTip())
                        + "</projectname></commonData><user><activity>copy</activity><param1>"
                        + getFilenameFromLocation(editor.getTitleToolTip())
                        + "</param1><param2><![CDATA["
                        + selection
                        + "]" + "]" + "></param2></user></microActivity>");
*/
    		} else if (fOperationCode == ITextOperationTarget.PASTE) {
    			logger.log(ECGLevel.PACKET, "A Paste operation has been recorded");

                Object clipboardContents = clipboard.getContents(textTransfer);
                user_projectname.setTextContent(getProjectnameFromLocation(editor.getTitleToolTip()));
                user_username.setTextContent(getUsername());
                user_activity.setTextContent("paste");
                user_param1.setTextContent(getFilenameFromLocation(editor.getTitleToolTip()));
                user_param2_contents.setNodeValue(selection);
                user_param3_contents.setNodeValue(
                        (clipboardContents != null ? clipboardContents.toString() : ""));

                processActivity("msdt.user.xsd", 
                        xmlDocumentSerializer.writeToString(msdt_user_doc));
/*
                processActivity(
                    "msdt.user.xsd",
                    "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                        + getUsername()
                        + "</username><projectname>"
                        + getProjectnameFromLocation(editor.getTitleToolTip())
                        + "</projectname></commonData><user><activity>paste</activity><param1>"
                        + getFilenameFromLocation(editor.getTitleToolTip())
                        + "</param1><param2><![CDATA["
                        + selection
                        + "]" + "]" + "></param2><param3><![CDATA["
                        + clipboard.getContents(textTransfer)
                        + "]" + "]" + "></param3></user></microActivity>");
*/
    		}
    		clipboard.dispose();

    	}
    	
    	private Shell getShell() {
    		ITextEditor editor= getTextEditor();
    		if (editor != null) {
    			IWorkbenchPartSite site= editor.getSite();
    			Shell shell= site.getShell();
    			if (shell != null && !shell.isDisposed()) {
    				return shell;
    			}
    		}
    		return null;
    	}
    	
    	private Display getDisplay() {
    		Shell shell= getShell();
    		if (shell != null) {
    			return shell.getDisplay();
    		}
    		return null;
    	}
    	
    	
    	/* (non-Javadoc)
    	 * @see org.eclipse.ui.texteditor.IUpdate#update()
    	 */
    	public void update() {
    		super.update();
    		
    		if (!isReadOnlyOperation() && !canModifyEditor()) {
    			setEnabled(false);
    			return;
    		}
    		
    		ITextEditor editor= getTextEditor();
    		if (fOperationTarget == null && editor!= null && fOperationCode != -1)
    			fOperationTarget= (ITextOperationTarget) editor.getAdapter(ITextOperationTarget.class);
    			
    		boolean isEnabled= (fOperationTarget != null && fOperationTarget.canDoOperation(fOperationCode));
    		setEnabled(isEnabled);
    	}
    	
    	/* (non-Javadoc)
    	 * @see org.eclipse.ui.texteditor.TextEditorAction#setEditor(org.eclipse.ui.texteditor.ITextEditor)
    	 */
    	public void setEditor(ITextEditor editor) {
    		super.setEditor(editor);
    		fOperationTarget= null;
    	}
    	

    }
}
  
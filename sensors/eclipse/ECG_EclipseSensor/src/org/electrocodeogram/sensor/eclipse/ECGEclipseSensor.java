/*
 * Freie Universit‰t Berlin, 2006
 */

package org.electrocodeogram.sensor.eclipse;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.jdt.internal.junit.ui.JUnitPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;
import org.electrocodeogram.event.WellFormedEventPacket;
import org.electrocodeogram.logging.LogHelper;
import org.electrocodeogram.sensor.eclipse.listener.ECGDisplayListener;
import org.electrocodeogram.sensor.eclipse.listener.ECGDocumentListener;
import org.electrocodeogram.sensor.eclipse.listener.ECGElementStateListener;
import org.electrocodeogram.sensor.eclipse.listener.ECGFileBufferListener;
import org.electrocodeogram.sensor.eclipse.listener.ECGPartListener;
import org.electrocodeogram.sensor.eclipse.listener.ECGRunDebugListener;
import org.electrocodeogram.sensor.eclipse.listener.ECGShellListener;
import org.electrocodeogram.sensor.eclipse.listener.ECGTestListener;
import org.hackystat.kernel.shell.SensorShell;
import org.hackystat.stdext.sensor.eclipse.EclipseSensor;
import org.hackystat.stdext.sensor.eclipse.EclipseSensorPlugin;
import org.hackystat.stdext.sensor.eclipse.EclipseSensorShell;
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
    static public Logger logger = LogHelper
        .createLogger(ECGEclipseSensor.class.getName());

    /**
     * This constant specifies how long to wait after user input
     * before a <em>Codechange</em> event is sent.
     */
    static public final int CODECHANGE_INTERVAL = 2000;

    /**
     * This is the name of the user, who is running <em>Eclipse</em>.
     */
    public String username;

    /**
     * The name of the project the user is currently working on.
     */
    public String projectname;

    /**
     * A reference to the currently active editor in <em>Eclipse</em>.
     */
    public ITextEditor activeTextEditor;

    /**
     * A reference to the currently active editor in <em>Eclipse</em>.
     */
    public IViewPart activeView;

    /**
     * The one and only TestListener, because all test runs are treated similarly 
     */
    public ArrayList openDialogs = new ArrayList();
    
    /**
     * A serializer to output XML DOM structures 
     */
    public LSSerializer xmlDocumentSerializer; 


    
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
        Listener displayListener = new ECGDisplayListener(this);
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
		
        // Create DocumentationListener
        docListener = new ECGDocumentListener(this);

        // add the PartListener for listening on
		// part events.
		IPartService partService = null;
		partListener = new ECGPartListener(this, docListener);
		for (int i = 0; i < windows.length; i++) {
		    partService = windows[i].getPartService();
			partService.addPartListener(partListener);
		}
		logger.log(Level.FINE, "Added PartListener.");
		
		// add ShellListener for ShellEvents
		shellListener = new ECGShellListener(this);
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
		runDebugListener = new ECGRunDebugListener(this);
		dp.addDebugEventListener(runDebugListener);
		logger.log(Level.FINE, "Added DebugEventSetListener.");
		
		// add listener for testRun events
		testListener = new ECGTestListener(this);        
		JUnitPlugin.getDefault().addTestRunListener(testListener);
		logger.log(Level.FINE, "Added TestListener.");
		
		// add listener for jobs (used for team tasks)
		// TODO It's just for exploring, remove this line and the Listener if not useful
//		Platform.getJobManager().addJobChangeListener(new ECGJobListener());
		
		//TODO the next line is just for exploration
		FileBuffers.getTextFileBufferManager().addFileBufferListener(new ECGFileBufferListener(this));
		
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
    public String getUsername() {

        return this.username;

    }

    /**
     * This returns the current projectname.
     * @return The current projectname
     */
    public String getProjectname() {

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

    static public String getFilenameFromLocation(String location) {
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

	static public String getProjectnameFromLocation(String location) {
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
}
  
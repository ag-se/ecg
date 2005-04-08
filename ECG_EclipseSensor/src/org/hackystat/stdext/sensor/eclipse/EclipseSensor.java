package org.hackystat.stdext.sensor.eclipse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointListener;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.debug.core.model.RuntimeProcess;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.junit.ui.JUnitPlugin;
import org.eclipse.jdt.junit.ITestRunListener;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.electrocodeogram.sensorwrapper.SensorShell;
import org.electrocodeogram.sensorwrapper.SensorProperties;
import org.hackystat.stdext.sensor.eclipse.event.EclipseSensorEvent;
import org.hackystat.stdext.sensor.eclipse.event.IEclipseSensorEventListener;
import org.hackystat.stdext.sensor.eclipse.junit.EclipseJUnitListener;

/**
 * Provides all the necessary sensor initialization and collects data in this
 * singleton class. A client can use one static method to get this instance:
 * <p>
 * A client can set Eclipse sensor by calling <code>getInstance()</code>, ant
 * can use the following process methods: Because of lazy instantiation, any
 * activity was not set until the initial call for <code>getInstance()</code>.
 * </p>
 * 
 * @author Takuya Yamashita
 * @version $Id: EclipseSensor.java,v 1.38 2004/11/06 00:00:00 hongbing Exp $
 */
public class EclipseSensor
{

    private static final EclipseSensor theInstance = new EclipseSensor();

    /**
     * Supports all the sensor types. Namely the user who has the
     * ENABLE_ECLIPSE_SENSOR=true can use all the supported Sensor Data Types
     * such as Activity, FileMetric, UnitTest, and so forth.
     */
    public static final String ECLIPSE = "ECLIPSE";

    /**
     * Supports Eclipse sensor monitor sensor. Used for displaying sensor status
     * in the status line at the bottom of the Eclipse IDE.
     */
    public static final String ECLIPSE_MONITOR = "ECLIPSE_MONITOR";

    /** Supports Eclipse buffer transition sensor. */
    public static final String ECLIPSE_BUFFTRANS = "ECLIPSE_BUFFTRANS";

    /** Supports Eclipse buffer transition time interval */
    public static final String ECLIPSE_BUFFTRANS_INTERVAL = "HACKYSTAT_BUFFTRANS_INTERVAL";

    /**
     * Supports Eclipse update sensor to check new sensor availability in a
     * server.
     */
    public static final String ECLIPSE_UPDATE = "ECLIPSE_UPDATE";

    /** The Eclipse update url to be used to get its value from properties. */
    public static final String ECLIPSE_UPDATE_URL = "ECLIPSE_UPDATE_URL";

    /** Supports Eclipse filemetric sensor. */
    public static final String ECLIPSE_FILEMETRIC = "ECLIPSE_FILEMETRIC";

    /** Supports Eclipse build sensor. */
    public static final String ECLIPSE_BUILD = "ECLIPSE_BUILD";

    /**
     * The number of seconds of the state change after which timer will wake up
     * again.
     */
    private long timerStateChangeInterval;

    /**
     * The number of seconds of the buffer transition after which time will wake
     * up again
     */
    private long timeBuffTransInterval;

    /**
     * The ITextEdtior instance to hold the active editor's (file's)
     * information.
     */
    private ITextEditor activeTextEditor;

    /** The active buffer to hold the buffer size of the active file. */
    private int activeBufferSize;

    /** The ITextEditor instance to hold the previous acitve editor's information */
    private ITextEditor previousTextEditor;

    /**
     * The ITextEdtior instance to hold the de-active editor's (file's)
     * information. to see if several partDeactivated call backs occur in the
     * same time.
     */
    private ITextEditor deactivatedTextEditor;

    /**
     * The threshold buffer size at an file activation to be compared with
     * activeBufferSize
     */
    private int thresholdBufferSize;

    /** The boolean value to check if an previous file is modified. */
    private boolean isModifiedFromFile;

    /** The boolean value to check if the current opened window is active. */
    private boolean isActivatedWindow;

    /**
     * Initialized from sensor.properties. Used for the flag on whether sensor
     * is enable or not. Helps to determine whether sensor would be executed or
     * not. Set true if sensor is going to be executed.
     */
    private boolean isEnabled;

    /**
     * The flag for the buffer transitions. Sets true if buffer trans property
     * is true.
     */
    private boolean isEnabledBuffTrans;

    /** The flag for the bcml sensor. Sets true if bcml sensor property is true. */
    private boolean isEnabledBcml;

    /**
     * The flag for the build sensor. Sets true if build sensor property is
     * true.
     */
    private boolean isEnabledBuild;

    /**
     * The flag for the sensor update. Sets true if sensor update property is
     * true.
     */
    private boolean isEnabledUpdate;

    /** The sensor shell instance used by this sensor. */
    private String dirKey;

    /** The update url */
    private String updateUrl;

    /** The SensorShell wrapper class for eclipse. */
    private EclipseSensorShell eclipseSensorShell;

    /** The sensor properties instance */
    private SensorProperties sensorProperties;

    /**
     * The List to contain event listeners for Eclipse sensor plugin. A client
     * of the EclipseSensor listener is added to this container.
     */
    private List eventListeners;

    /**
     * The ITestRunListener to check if the listener is added to the
     * JUnitPlugin.
     */
    private ITestRunListener junitListener;

    /**
     * 12 characters hackystat directory key to check if the new sensor shell
     * should be set.
     */
    private Timer timer;

  
    /**
     * The TimerTask instance to do the task of the buffer transitions when the
     * timer wakes up.
     */
    private TimerTask buffTransTimerTask;

    /**
     * The WindowListerAdapter instance to check if this instance is added or
     * not.
     */
    private WindowListenerAdapter windowListener;

    /**
     * Provides instantiation of SensorProperties, which has information in the
     * sensor.properties file, and executes <code>doCommand</code> to activate
     * sensor. Note that the Eclipse instance is lazily instantiated when static
     * <code>getInstance()</code> was called.
     */
    private EclipseSensor()
    {
        this.eventListeners = new ArrayList();
        this.timer = new Timer();
        this.buffTransTimerTask = new BuffTransTimertask();
        // Get sensor status from property file.
        this.sensorProperties = new SensorProperties(EclipseSensor.ECLIPSE);
        this.isEnabled = sensorProperties.isSensorEnabled();
        this.isEnabledBuffTrans = sensorProperties.isSensorTypeEnabled(ECLIPSE_BUFFTRANS);
        this.isEnabledBcml = sensorProperties.isSensorTypeEnabled(ECLIPSE_FILEMETRIC);
        this.isEnabledBuild = sensorProperties.isSensorTypeEnabled(ECLIPSE_BUILD);
        this.isEnabledUpdate = sensorProperties.isSensorTypeEnabled(EclipseSensor.ECLIPSE_UPDATE);
        this.dirKey = sensorProperties.getKey();
        this.updateUrl = sensorProperties.getProperty(ECLIPSE_UPDATE_URL);
        this.timerStateChangeInterval = sensorProperties.getStateChangeInterval();
        String bufftransInterval = sensorProperties.getProperty(ECLIPSE_BUFFTRANS_INTERVAL);

        // Sets bufftrans interval. if there is no bufftrans interval property,
        // 5 seconds are set.
        this.timeBuffTransInterval = (bufftransInterval != null) ? Long.parseLong(bufftransInterval) : 5;
        // Adds this EclipseSensorPlugin instance to IResourceChangeListener
        // so that project event and file save event is notified even though
        // initial sensor is disabled.
        IWorkspace workspace = ResourcesPlugin.getWorkspace();

        // Adds IResourceChangeListener-implemented ResourceChangeAdapter before
        // checking isEnabled
        // for piemontese sensor because this listener is used in piemontese
        // sensor even though
        // the main sensor.properties does not exist in the <hackystat_home>.
        workspace.addResourceChangeListener(new ResourceChangeAdapter(), IResourceChangeEvent.POST_CHANGE);

        // Adds element changed listener to get the corresponding change of
        // refactoring.
        //JavaCore.addElementChangedListener(new
        // JavaElementChangedAdapter(this));
        //JavaCore.addElementChangedListener(new JavaStructureChangeDetector(this));
        // Listens for the changes on C/C++ code.

        if (!this.isEnabled) {
            return;
        }
        initializeSensor();
    }

    /**
     * Initializes sensor and JUnitListener instance if the sensor is enabled.
     * Note that JUnit listener instance is added only when the instance is not
     * instantiated.
     */
    public void initializeSensor()
    {
        // Check if the new sensor property file enable sensor to be activated.
        if (this.isEnabled) {
            SensorShell shell = new SensorShell(this.sensorProperties, false,
                    "eclipse");
            //SensorClient shell = new SensorClient(this.sensorProperties,
            // false, "eclipse");
            this.eclipseSensorShell = new EclipseSensorShell(shell);
            String[] args = { "setTool", "Eclipse" };
            this.eclipseSensorShell.doCommand("Activity", Arrays.asList(args));
           
            // Sets buffer transition time schedule.
            if (this.buffTransTimerTask.scheduledExecutionTime() == 0) {
                this.timer.schedule(this.buffTransTimerTask, this.timeBuffTransInterval * 1000, this.timeBuffTransInterval * 1000);
            }

            // Check if the TestRunListener is not added yet.
            if (this.junitListener == null) {
                this.junitListener = new EclipseJUnitListener(this);
                JUnitPlugin.getDefault().addTestRunListener(this.junitListener);
            }

            // Checks if Eclipse update flag is true.
            if (this.isEnabled && this.isEnabledUpdate) {
                // Avoid the delays for the version check in the sensor plug-in
                // instantiation time.
                Thread versionCheck = new Thread() {
                    public void run()
                    {
                        String title = EclipseSensorI18n.getString("VersionCheck.messageDialogTitle");
                        String first = EclipseSensorI18n.getString("VersionCheck.messageDialogMessageFirst");
                        String betweenKey = "VersionCheck.messageDialogMessageBetween";
                        String between = EclipseSensorI18n.getString(betweenKey);
                        String last = EclipseSensorI18n.getString("VersionCheck.messageDialogMessageLast");
                        String messages[] = { first, between, last };
                        VersionCheck.processUpdateDialog(updateUrl, title, messages);
                    }
                };
                versionCheck.start();
            }

            initializeListeners();
        }
    }

    /**
     * Provide the initialization of the listeners additions. The Window, Part,
     * and Document Listener are added. Note that sensor shell should be
     * instantiated before this method is called because
     * <code>processActivity()</code> method uses sensor shell instance.
     */
    private void initializeListeners()
    {
        IWorkbench workbench = EclipseSensorPlugin.getInstance().getWorkbench();

        // :RESOLVED: JULY 1, 2003
        // Supports the multiple window for sensor collection.
        IWorkbenchWindow[] activeWindows = workbench.getWorkbenchWindows();

        // Check if window listener is not added yet. Otherwise multi instances
        // are notified.
        if (this.windowListener == null) {
            this.windowListener = new WindowListenerAdapter();
            workbench.addWindowListener(new WindowListenerAdapter());
        }
        for (int i = 0; i < activeWindows.length; i++) {
            IWorkbenchPage activePage = activeWindows[i].getActivePage();
            activePage.addPartListener(new PartListenerAdapter());
            IEditorPart activeEditorPart = activePage.getActiveEditor();

            // Adds this EclipseSensorPlugin instance to IDocumentListener
            // only when activeEditorPart is the instance of ITextEditor
            // so that null case is also ignored.
            if (activeEditorPart instanceof ITextEditor) {
                // Sets activeTextEditor. Otherwise a first activated file would
                // not be recorded.
                this.activeTextEditor = (ITextEditor) activeEditorPart;
                // Gets opened file since the initial opened file is not
                // notified from IPartListener.
                String fileName = EclipseSensor.this.getFqFileName(this.activeTextEditor);
                EclipseSensor.this.processActivity("Open File", fileName);
                IDocumentProvider provider = this.activeTextEditor.getDocumentProvider();
                IDocument document = provider.getDocument(activeEditorPart.getEditorInput());

                // Initially sets active buffer and threshold buffer.
                // Otherwise a first activated buffer would not be recorded.
                this.activeBufferSize = document.getLength();
                this.thresholdBufferSize = document.getLength();
                document.addDocumentListener(new DocumentListenerAdapter());
            }
        }

        IBreakpointManager bpManager = DebugPlugin.getDefault().getBreakpointManager();
        bpManager.addBreakpointListener(new HackystatBreakPointerListener());

        DebugPlugin dp = DebugPlugin.getDefault();
        dp.addDebugEventListener(new DebugEventSetAdapter());
    }

    /**
     * Gets the EclipseSensorShell instance. Clients may invoke the eclipse
     * specific doCommand and send, println method through this instance.
     * 
     * @return the EclipseSensorShell instance.
     */
    public EclipseSensorShell getEclipseSensorShell()
    {
        return this.eclipseSensorShell;
    }

    /**
     * Returns the (singleton) EclipseSensor instance. This method is initially
     * called by EclipseSensorPlugin client class for instantiation.
     * 
     * @return The (singleton) instance.
     */
    public static EclipseSensor getInstance()
    {
        return theInstance;
    }

    /**
     * Gets the timerStateChangeInterval attribute of the EclipseSensor class.
     * Assumed to be used by a Timer class instance to let it know the time
     * interval after which sensor wakes up.
     * 
     * @return The timerStateChangeInterval value
     */
    private long getTimerStateChangeInterval()
    {
        return this.timerStateChangeInterval;
    }

    /**
     * Process FileMetric for the active file.
     * <p>
     * Most of this procedure is concerned with obtaining the absolute file
     * name, fully qualified class name, and class path for the active file. All
     * we need to have is:
     * <ol>
     * <li>Class path (classPath). For example, C:/cvs/foobarproject/build
     * </li>
     * <li>Absolute file name path (canonicalActiveBuffer). For example,
     * C:\cvs\foobarproject\src\foo\bar\Baz.java</li>
     * <li>Class package name (canonicalActiveBuffer). For example, foo.bar.Baz
     * </li>
     * </ol>
     * </p>
     * <p>
     * Finally, we pass the absolute file Name path, the fully qualified class
     * name, and the path to the .class output file directory into the String
     * array, and then invoke the <code>doCommand</code> with "FileMetric"
     * sensor data type.
     * </p>
     */
    public void processFileMetric()
    {
        if (!this.isEnabled || !this.isEnabledBcml || (this.activeTextEditor == null)) {
            return;
        }

        // Sends the file, the fully qualified class name and the root directory
        // to SensorShell.
        String activeFileName = this.getFqFileName(this.activeTextEditor);
        if (activeFileName.endsWith(".java")) {
            String fullyQualifiedClassName = this.getFullyQualifedClassName();
            String classPath = this.getClassPath();
            String[] args = { "addJava", activeFileName, fullyQualifiedClassName, classPath };

            this.eclipseSensorShell.doCommand("FileMetric", Arrays.asList(args));
        }
    }

    /**
     * Keeps track of the user's interaction with the Eclipse browser. The
     * Browser event classes will invoke this method to record actions. Note
     * that if the sensor is not enabled, this method will immediately return,
     * so clients do not have to explicitly check whether the sensor is enabled.
     * 
     * @param activityType
     *            The activity type.
     * @param data
     *            The data associated with this activity type.
     */
    public void processActivity(String activityType, String data)
    {
        if (!this.isEnabled) {
            return;
        }

        // Adds the current activity.
        String[] args = { "add", activityType, data };
        this.eclipseSensorShell.doCommand("MicroActivity", Arrays.asList(args));
    }

    /**
     * Keep track of the latest state change file to avoid sending out repeated
     * data.
     */
    private String latestStateChangeFileName = "";

    private int latestStateChangeFileSize = 0;

    /**
     * Process the state change activity whose element consists of the
     * (absolute) file name and its buffer size (or file size).
     * @param text
     */
    public void processStateChangeActivity(String text)
    {
        if (!this.isEnabled) {
            return;
        }
        String activeFileName = getFqFileName(activeTextEditor);
        String activeTitle = getTitle(activeTextEditor);
        
        assert(! activeFileName.equals(""));
        assert(! activeTitle.equals(""));
        
        int bs = activeBufferSize;
    
        processActivity("Codechange",activeTitle + " " + text + " " + String.valueOf(bs) + " " + activeFileName);
        
        this.latestStateChangeFileName = activeFileName;
        this.latestStateChangeFileSize = activeBufferSize;

        // Send the new metrics if there are any
        this.processFileMetric();
        
    }

    /** Keep track the last buffer trans data incase of repeation. */
    private String latestBuffTrans = "";

    /**
     * Process the buffer transition to check if the current buffer is visiting
     * a file and if that file is different from the file visited by the buffer
     * during the last wakeup. Its element consists of the the (absolute) file
     * name (or last-time-visited file name) from which an user is visiting, the
     * (aboslute) file name (or current-visiting file name) to which the user is
     * visiting, and the modification status of the last-time-visited file.
     */
    public void processBuffTrans()
    {
        // check if BufferTran property is enable
        if (!this.isEnabled || !this.isEnabledBuffTrans || (this.activeTextEditor == null) || (this.previousTextEditor == null)) {
            return;
        }
        String toFileName = this.getFqFileName(this.activeTextEditor);
        String fromFileName = this.getFqFileName(this.previousTextEditor);
        if (!toFileName.equals(fromFileName) && !toFileName.equals("") && !fromFileName.equals("")) {
            String buffTrans = fromFileName + "#" + toFileName;
            // :RESOVED: 5/21/04 ISSUE:HACK109
            if (!latestBuffTrans.equals(buffTrans)) {
                String[] args = { "add", fromFileName, toFileName, String.valueOf(this.isModifiedFromFile) };
                this.eclipseSensorShell.doCommand("BuffTrans", Arrays.asList(args));
                latestBuffTrans = buffTrans;
            }
        }
    }

    /**
     * Keeps track of the user's invocation of JUnit Test in the Eclipse IDE.
     * Note that if the sensor is not enabled, this method will immediately
     * return, so clients do not have to explicitly check whether the sensor is
     * enabled.
     * <ul>
     * <li>argList element 0 is the method name string to be invoked, "add".
     * </li>
     * <li>argList element 1 is the test class name string,
     * "org.hackystat.Foo".</li>
     * <li>argList element 2 is the test case (method) name string, "testBar".
     * </li>
     * <li>argList element 3 is the elapsed time string, whose unit is
     * millisecond, "10".</li>
     * <li>argList element 4 is the failure stack trace string.</li>
     * <li>argList element 5 is the error stack trace string.</li>
     * </ul>
     * 
     * @param argList
     *            the List that contains the JUnit invocation results for a test
     *            case.
     */
    public void processUnitTest(List argList)
    {
        if (!this.isEnabled) {
            return;
        }
        this.eclipseSensorShell.doCommand("UnitTest", argList);
    }

    /**
     * Sets the SensorProperties instance to create a new SensorShell instance.
     * Clients can set a new SensorProperties instance with new
     * sensor.properties file in order to read a different sensor.properties
     * file. This might mean that the clients can load the HACKYSTAT_KEY defined
     * in the different sensor.properties file. Used for the client of the
     * Hackystat Eclipse sensor such as Piemontese sensor.
     * 
     * @param sensorProperties
     *            The sensorProperties instance to hold a new file path to a
     *            sensor.properties file.
     */
    public void setSensorProperties(SensorProperties sensorProperties)
    {
        // Check if the previous sensor is enabled, then the data is sent.
        if (this.isEnabled) {
            this.eclipseSensorShell.send();
        }
        this.dirKey = sensorProperties.getKey();
        this.isEnabled = sensorProperties.isSensorEnabled();
        boolean isEnabledMonitor = sensorProperties.isSensorTypeEnabled(EclipseSensor.ECLIPSE_MONITOR);
        this.eclipseSensorShell.setMonitorEnabled(isEnabledMonitor);
        this.isEnabledBuffTrans = sensorProperties.isSensorTypeEnabled(EclipseSensor.ECLIPSE_BUFFTRANS);
        this.isEnabledBcml = sensorProperties.isSensorTypeEnabled(ECLIPSE_FILEMETRIC);
        this.isEnabledBuild = sensorProperties.isSensorTypeEnabled(ECLIPSE_BUILD);
        this.isEnabledUpdate = sensorProperties.isSensorTypeEnabled(EclipseSensor.ECLIPSE_UPDATE);
        this.updateUrl = sensorProperties.getProperty(ECLIPSE_UPDATE_URL);
        this.timerStateChangeInterval = sensorProperties.getStateChangeInterval();
        String bufftransInterval = sensorProperties.getProperty(ECLIPSE_BUFFTRANS_INTERVAL);

        // Sets bufftrans interval. if there is no bufftrans interval property,
        // 5 seconds are set.
        this.timeBuffTransInterval = (bufftransInterval != null) ? Long.parseLong(bufftransInterval) : 5;
        this.sensorProperties = sensorProperties;
        // Checki if the new sensor property file enable sensor to be activated.
        if (this.isEnabled) {
            initializeSensor();
        }
    }

    /**
     * Adds an IEclipseSensorEventListener implementing class to this object.
     * The added implementing class would be notified when the EclipseSensor is
     * initialized.
     * 
     * @param listener
     *            The IEclipseSensorEventListener implementing class to be
     *            added.
     */
    public void addEclipseSensorEventListener(IEclipseSensorEventListener listener)
    {
        this.eventListeners.add(listener);
    }

    /**
     * Notifies all listener classes which implements the
     * IEclipseSensorEventListener. The purpose of this notification is to let
     * the client know the properties information in the initialization time.
     * 
     * @param event
     *            The IResource instance that contains the root resource.
     */
    private void notifyAll(EclipseSensorEvent event)
    {
        for (Iterator i = this.eventListeners.iterator(); i.hasNext();) {
            ((IEclipseSensorEventListener) i.next()).notify(event);
        }
    }

    /**
     * Gets the fully qualified class name for an active file. For example, its
     * value is foo.bar.Baz.
     * 
     * @return The fully qualified class name. For example,foo.bar.Baz.
     */
    private String getFullyQualifedClassName()
    {
        if (this.activeTextEditor != null) {
            IFileEditorInput fileEditorInput = (IFileEditorInput) this.activeTextEditor.getEditorInput();
            IFile file = fileEditorInput.getFile();
            if (file.exists() && file.getName().endsWith(".java")) {
                ICompilationUnit compilationUnit = (ICompilationUnit) JavaCore.create(file);
                try {
                    return compilationUnit.getTypes()[0].getFullyQualifiedName();
                }
                catch (JavaModelException e) {
                    // Ignores this because this occurs
                    // if this element does not exist or if an exception occurs
                    // while accessing its corresponding resource
                }
            }
        }
        return "";
    }

    /**
     * Gets the class path for an active project. For example, its value is
     * C:\cvs\foobarproject\build.
     * 
     * @return The class path. For example, C:\cvs\foobarproject\build.
     */
    private String getClassPath()
    {
        if (this.activeTextEditor != null) {
            IWorkspaceRoot root = EclipseSensorPlugin.getWorkspace().getRoot();
            String projectName = this.getProjectName();
            if (!projectName.equals("")) {
                IProject project = root.getProject(projectName);
                IPath projectPath = project.getLocation();
                IJavaProject javaProject = JavaCore.create(project);
                try {
                    IPath outputPath = javaProject.getOutputLocation().removeFirstSegments(1);
                    if (outputPath != null) {
                        return projectPath.append(outputPath).toString();
                    }
                }
                catch (JavaModelException e) {
                    this.eclipseSensorShell.println(e.getMessage());
                }
            }
        }
        return "";
    }

    /**
     * Gets the fully qualified file name, namely, absolute path to the java
     * file with its extension. For example,
     * C:\cvs\foobarproject\src\foo\bar\Bar.java.
     * 
     * @param textEditor
     *            A ITextEditor instance form which the file name is retrieved.
     * @return The fully qualified file name. For example,
     *         C:\cvs\foobarproject\src\foo\bar\Bar.java.
     */
    private String getFqFileName(ITextEditor textEditor)
    {
        if (textEditor != null) {
            IEditorInput editorInput = textEditor.getEditorInput();
            if (editorInput instanceof IFileEditorInput) {
                IFileEditorInput input = (IFileEditorInput) editorInput;
                return input.getFile().getLocation().toString();
            }
        }
        return "";
    }
    
    private String getTitle(ITextEditor textEditor)
    {
        if (textEditor != null) {
            return textEditor.getTitle();
        }
        return "";
    }

    /**
     * Gets the project name. For example, "Foo Bar Project". Note that it is
     * not necessary to be the same as the actual module directory name such as
     * "fooBarProject" directory.
     * 
     * @return The project name. For example, Foo Bar Project.
     */
    private String getProjectName()
    {
        if (this.activeTextEditor != null) {
            IEditorInput editorInput = this.activeTextEditor.getEditorInput();
            if (editorInput instanceof IFileEditorInput) {
                IFileEditorInput input = (IFileEditorInput) editorInput;
                return input.getFile().getProject().getName();
            }
        }
        return "";
    }

    /**
     * Proiveds the IWindowListener-implemented class to catch the "Browser
     * activated", "Browser closing" event. This inner class is designed to be
     * used by the outer EclipseSensor class.
     * 
     * @author Takuya Yamashita
     * @version $Id: EclipseSensor.java,v 1.38 2004/11/06 00:00:00 hongbing Exp $
     */
    private class WindowListenerAdapter implements IWindowListener
    {
        /**
         * Provides manipulation of browser open status due to implement
         * <code>IWindowListener</code>. This method must not be called by
         * client because it is called by platform. Do nothing for Eclipse
         * sensor so far.
         * 
         * @param window
         *            An IWorkbenchWindow instance to be triggered when a window
         *            is activated.
         */
        public void windowActivated(IWorkbenchWindow window)
        {

            processActivity("Window Activated", "");

//            IEditorPart activeEditorPart = window.getActivePage().getActiveEditor();
//            if (activeEditorPart instanceof ITextEditor) {
//                EclipseSensor.this.activeTextEditor = (ITextEditor) activeEditorPart;
//                ITextEditor editor = EclipseSensor.this.activeTextEditor;
//                IDocumentProvider provider = editor.getDocumentProvider();
//                IDocument document = provider.getDocument(editor.getEditorInput());
//                
//                document.addDocumentListener(new DocumentListenerAdapter());
//                int activeBufferSize = provider.getDocument(editor.getEditorInput()).getLength();
//
//                // BuffTrans: Copy the new active file size to the threshold
//                // buffer size .
//                EclipseSensor.this.thresholdBufferSize = activeBufferSize;
//                EclipseSensor.this.activeBufferSize = activeBufferSize;
//            }
        }

        /**
         * Provides manipulation of browser close status due to implement
         * <code>IWindowListener</code>. This method must not be called by
         * client because it is called by platform. Whenever window is closing,
         * set all the current active file to process file metrics, and then try
         * to send them to server.
         * 
         * @param window
         *            An IWorkbenchWindow instance to be triggered when a window
         *            is closed.
         */
        public void windowClosed(IWorkbenchWindow window)
        {

            processActivity("Window Closed", "");

            EclipseSensor.this.processFileMetric();
            EclipseSensor.this.eclipseSensorShell.send();
        }

        /**
         * Provides manipulation of browser deactivation status due to implement
         * <code>IWindowListener</code>. This method must not be called by
         * client because it is called by platform. Do nothing for Eclipse
         * sensor so far.
         * 
         * @param window
         *            An IWorkbenchWindow instance to be triggered when a window
         *            is deactivated.
         */
        public void windowDeactivated(IWorkbenchWindow window)
        {

            processActivity("Window Deactivated", "");

            EclipseSensor.this.isActivatedWindow = false;
            IEditorPart activeEditorPart = window.getActivePage().getActiveEditor();
            if (activeEditorPart instanceof ITextEditor) {
                ITextEditor editor = (ITextEditor) activeEditorPart;
                IDocumentProvider provider = editor.getDocumentProvider();

                // provider could be null if the text editor is closed befor
                // this method is called.
                EclipseSensor.this.previousTextEditor = editor;
                int fromFileBufferSize = provider.getDocument(editor.getEditorInput()).getLength();

                // Check if a threshold buffer is either dirty or
                // not the same as the current from file buffer size;
                EclipseSensor.this.isModifiedFromFile = (editor.isDirty() || (EclipseSensor.this.thresholdBufferSize != fromFileBufferSize));
            }
        }

        /**
         * Provides manipulation of browser window open status due to implement
         * <code>IWindowListener</code>. This method must not be called by
         * client because it is called by platform. Do nothing for Eclipse
         * sensor so far.
         * 
         * @param window
         *            An IWorkbenchWindow instance to be triggered when a window
         *            is opened.
         */
        public void windowOpened(IWorkbenchWindow window)
        {

            processActivity("Window Opened", "");

            EclipseSensor.this.initializeListeners();
        }
    }

    /**
     * Provides the IPartListener-implemented class to catch "part opened",
     * "part closed" event as well as setting active editor part to the
     * activeTextEditor instance and setting active buffer size of the
     * activeBufferSize field of the EclipseSensor class. Note that methods are
     * called by the following order:
     * <ol>
     * <li>partClosed() or partOpened()</li>
     * <li>partDeactivated()</li>
     * <li>partActivate() if any</li>
     * </ol>
     * 
     * @author Takuya Yamashita
     * @version $Id: EclipseSensor.java,v 1.38 2004/11/06 00:00:00 hongbing Exp $
     */
    private class PartListenerAdapter implements IPartListener
    {
        /**
         * Provides manipulation of browser part activation status due to
         * implement <code>IPartListener</code>. This method must not be
         * called by client because it is called by platform. Do nothing for
         * Eclipse sensor so far.
         * 
         * @param part
         *            An IWorkbenchPart instance to be triggered when a part is
         *            activated.
         */
        public void partActivated(IWorkbenchPart part)
        {

            if (part instanceof ITextEditor) {

                //System.out.println("Sensor : " + part);
                EclipseSensor.this.isActivatedWindow = true;
                EclipseSensor.this.activeTextEditor = (ITextEditor) part;
                ITextEditor editor = EclipseSensor.this.activeTextEditor;

                processActivity("Editor activated", editor.getTitle());

                IDocumentProvider provider = editor.getDocumentProvider();
                IDocument document = provider.getDocument(editor.getEditorInput());
                //document.addDocumentListener(new DocumentListenerAdapter());
                int activeBufferSize = provider.getDocument(editor.getEditorInput()).getLength();

                // BuffTrans: Copy the new active file size to the threshold
                // buffer size .
                EclipseSensor.this.thresholdBufferSize = activeBufferSize;
                EclipseSensor.this.activeBufferSize = activeBufferSize;
            }
            else {
                processActivity("Part activated", part.getTitle());
            }
        }

        /**
         * Provides manipulation of browser part brought-to-top status due to
         * implement <code>IPartListener</code>. This method must not be
         * called by client because it is called by platform. Do nothing for
         * Eclipse sensor so far.
         * 
         * @param part
         *            An IWorkbenchPart instance to be triggered when a part is
         *            brought to top.
         */
        public void partBroughtToTop(IWorkbenchPart part)
        {
            // not supported in Eclipse Sensor.
        }

        /**
         * Provides manipulation of browser part brought-to-top status due to
         * implement <code>IPartListener</code>. This method must not be
         * called by client because it is called by platform. Whenever part is
         * closing, check whether or not part is the instance of
         * <code>IEditorPart</code>, if so, set process activity as
         * <code>ActivityType.CLOSE_FILE</code> with its absolute path.
         * 
         * @param part
         *            An IWorkbenchPart instance to be triggered when a part is
         *            closed.
         */
        public void partClosed(IWorkbenchPart part)
        {
            if (part instanceof ITextEditor) {

                String fileName = EclipseSensor.this.getFqFileName((ITextEditor) part);
                
                String title = getTitle((ITextEditor) part);
                
                processActivity("Editor closed", title + " " + fileName);

                
                IEditorPart activeEditorPart = part.getSite().getPage().getActiveEditor();
                if (activeEditorPart == null) {
                    EclipseSensor.this.activeTextEditor = null;
                }
            }
            else {
                processActivity("Part closed", part.getTitle());
            }
        }

        /**
         * Provides manipulation of browser part deactivation status due to
         * implement <code>IPartListener</code>. This method must not be
         * called by client because it is called by platform. Sets active text
         * editor to be null when the text editor part is deactivated.
         * 
         * @param part
         *            An IWorkbenchPart instance to be triggered when a part is
         *            deactivated.
         */
        public void partDeactivated(IWorkbenchPart part)
        {
            if (part instanceof ITextEditor && (part != EclipseSensor.this.deactivatedTextEditor)) {
                EclipseSensor.this.deactivatedTextEditor = (ITextEditor) part;

                processActivity("Editor deactivated", part.getTitle());

                if (EclipseSensor.this.isActivatedWindow) {
                    IEditorPart activeEditorPart = part.getSite().getPage().getActiveEditor();

                    // Sets activeTextEdtior to be null only when there is no
                    // more active editor.
                    // Otherwise the case that the non text editor part is
                    // active causes the activeTextEditor
                    // to be null so that sensor is not collected after that.
                    if (activeEditorPart == null) {
                        EclipseSensor.this.activeTextEditor = null;
                    }

                    // BuffTrans to get the toFrom buffer size.
                    ITextEditor editor = (ITextEditor) part;
                    IDocumentProvider provider = editor.getDocumentProvider();

                    // provider could be null if the text editor is closed
                    // before this method is called.
                    if (provider != null) {
                        EclipseSensor.this.previousTextEditor = editor;
                        int fromFileBufferSize = provider.getDocument(editor.getEditorInput()).getLength();

                        // Check if a threshold buffer is either dirty or
                        // not the same as the current from file buffer size;
                        EclipseSensor.this.isModifiedFromFile = (editor.isDirty() || (EclipseSensor.this.thresholdBufferSize != fromFileBufferSize));
                    }
                    else {
                        EclipseSensor.this.isModifiedFromFile = false;
                        EclipseSensor.this.previousTextEditor = null;
                    }
                }
                else {
                    EclipseSensor.this.isActivatedWindow = true;
                }
            }
            else {
                processActivity("Part deactivated", part.getTitle());
            }
        }

        /**
         * Provides manipulation of browser part brought-to-top status due to
         * implement <code>IPartListener</code>. This method must not be
         * called by client because it is called by platform. Whenever part is
         * opened, check whether or not part is the instance of
         * <code>IEditorPart</code>, if so, set process activity as
         * <code>ActivityType.OPEN_FILE</code> with its absolute path.
         * 
         * @param part
         *            An IWorkbenchPart instance to be triggered when part is
         *            opened.
         */
        public void partOpened(IWorkbenchPart part)
        {
            if (part instanceof ITextEditor) {

                processActivity("Editor opened", getFqFileName((ITextEditor) part));

                ITextEditor editor = (ITextEditor) part;
                
                IDocumentProvider provider = editor.getDocumentProvider();
                IDocument document = provider.getDocument(editor.getEditorInput());
                document.addDocumentListener(new DocumentListenerAdapter());
               
                
                EclipseSensor.this.activeTextEditor = (ITextEditor) part;
            }
            else {
                processActivity("Part opened", part.getTitle());
            }
        }
    }

    /**
     * Listens to the break point changes to send activity data to Hackystat
     * server.
     * 
     * @author Hongbing Kou
     * @version $Id: EclipseSensor.java,v 1.38 2004/11/06 00:00:00 hongbing Exp $
     */
    private class HackystatBreakPointerListener implements IBreakpointListener
    {
        /**
         * Creates a breakpoint listener instance.
         */
        HackystatBreakPointerListener()
        {
        }

        /**
         * Listen to the break point added event.
         * 
         * @param breakpoint
         *            Break point.
         */
        public void breakpointAdded(IBreakpoint breakpoint)
        {
            // Ignores breakpoints other than line break point
            if (!(breakpoint instanceof ILineBreakpoint)) {
                return;
            }

            IFileEditorInput fileEditorInput = (IFileEditorInput) EclipseSensor.this.activeTextEditor.getEditorInput();
            IFile file = fileEditorInput.getFile();
            try {
                EclipseSensor.this.processActivity("Set Breakpoint", file.getLocation().toString() + "#" + String.valueOf(((ILineBreakpoint) breakpoint).getLineNumber()));
            }
            catch (CoreException e) {
                EclipseSensorPlugin.getInstance().log(e);
            }
        }

        /**
         * Listens to break point changed event.
         * 
         * @param breakpoint
         *            Break point.
         * @param delta
         *            Delta change.
         */
        public void breakpointChanged(IBreakpoint breakpoint, IMarkerDelta delta)
        {
        }

        /**
         * Listens to break point removed event.
         * 
         * @param breakpoint
         *            Breakpoint.
         * @param delta
         *            Marker changes.
         */
        public void breakpointRemoved(IBreakpoint breakpoint, IMarkerDelta delta)
        {
            // Ignores breakpoints other than line break point
            if (!(breakpoint instanceof ILineBreakpoint)) {
                return;
            }

            IFileEditorInput fileEditorInput = (IFileEditorInput) EclipseSensor.this.activeTextEditor.getEditorInput();
            IFile file = fileEditorInput.getFile();
            try {
                EclipseSensor.this.processActivity("Unset Breakpoint", file.getLocation().toString() + "#" + String.valueOf(((ILineBreakpoint) breakpoint).getLineNumber()));
            }
            catch (CoreException e) {
                EclipseSensorPlugin.getInstance().log(e);
            }
        }
    }

    /** Holds the latest compilation problem to avoid of repeat. */
    private String latestCompilationProblem = "";

    /**
     * Installs a problem requestor to the current active editor if the working
     * file is a java file.
     */
    private void detectBuildProblem()
    {
        try {
            if (!isEnabled || !isEnabledBuild || this.activeTextEditor == null) {
                return;
            }
            IFileEditorInput fileEditorInput = (IFileEditorInput) this.activeTextEditor.getEditorInput();
            // Skip over non-java file.
            String fileName = fileEditorInput.getFile().getLocation().toString();
            if (!fileName.endsWith(".java")) {
                return;
            }

            IMarker[] markers = fileEditorInput.getFile().findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
            if (markers != null) {
                for (int i = 0; i < markers.length; i++) {
                    IMarker marker = markers[i];
                    String data = fileName + "#" + (String) marker.getAttribute("message");
                    if (!data.equals(this.latestCompilationProblem)) {
                        String[] args = { "add", "Build Error", data };
                        this.eclipseSensorShell.doCommand("Activity", Arrays.asList(args));
                        this.latestCompilationProblem = data;
                    }
                }
            }
            else { // If marker is removed
                this.latestCompilationProblem = "";
            }
        }
        catch (CoreException e) { // Log out error in case of error in Eclipse
                                  // sensor.
            EclipseSensorPlugin.getInstance().log(e);
        }
    }

    /**
     * Provides IDocuementListener-implemented class to set an active buffer
     * size when a document is being edited.
     * 
     * @author Takuya Yamashita
     * @version $Id: EclipseSensor.java,v 1.38 2004/11/06 00:00:00 hongbing Exp $
     */
    private class DocumentListenerAdapter implements IDocumentListener
    {
        
        /**
         * Do nothing right now. Just leave it due to implementation of
         * IDocumentationListener.
         * 
         * @param event
         *            An event triggered when a document is about to be changed.
         */
        public void documentAboutToBeChanged(DocumentEvent event)
        {
            // not supported in Eclipse Sensor.
        }

        /**
         * Provides the invocation of DeltaResource.setFileSize(long fileSize)
         * method in order to get buffer size. This method is called every
         * document change since this EclipseSensorPlugin instance was added to
         * IDocumentLister. Since this method, the current buffer size of an
         * active file could be grabbed.
         * 
         * @param event
         *            An event triggered when a document is changed.
         */
        public void documentChanged(DocumentEvent event)
        {
            EclipseSensor.this.activeBufferSize = event.getDocument().getLength();
            
                        
            timer.cancel();
            timer = new Timer();
            timer.schedule(new CodeChangeTimerTask(event.getText()),2000);
            
        }
    }

    /**
     * Provides "Open Project, "Close Project", and "Save File" events. Note that this implementing
     * class uses Visitor pattern so that key point to gather these event information is inside the
     * visitor method which is implemented from <code>IResourceDeltaVisitor</code> class.
     * 
     * @author Takuya Yamashita
     * @version $Id: EclipseSensor.java,v 1.38 2004/11/06 00:00:00 hongbing Exp $
     */
    private class ResourceChangeAdapter implements IResourceChangeListener
    {
        /**
         * Proivdes manipulation of IResourceChangeEvent instance due to implement
         * <code>IResourceChangeListener</code>. This method must not be called by client because it
         * is called by platform when resources are changed.
         * 
         * @param event A resource change event to describe changes to resources.
         */
        public void resourceChanged(IResourceChangeEvent event)
        {

            if (event.getType() != IResourceChangeEvent.POST_CHANGE)
                return;

            //debuglog("a resource has been changed");

            IResourceDelta delta = event.getDelta();

            IResourceDeltaVisitor visitor = new IResourceDeltaVisitor() {

                public boolean visit(IResourceDelta delta) throws CoreException
                {

                    int kind = delta.getKind();
                    int flags = delta.getFlags();

                    switch (kind)
                    {
                    case IResourceDelta.ADDED:

                        processActivity("Resource added ", delta.getResource().getName());

                        if (flags == IResourceDelta.MOVED_FROM || flags == IResourceDelta.MOVED_TO) {
                            //eventlog("it has been moved");
                        }

                        break;

                    case IResourceDelta.REMOVED:

                        processActivity("Resource removed ", delta.getResource().getName());

                        if (flags == IResourceDelta.MOVED_FROM || flags == IResourceDelta.MOVED_TO) {
                            //eventlog("it has been moved");
                        }
                        break;

                    case IResourceDelta.CHANGED:

                        String name = delta.getResource().getName();

                        if (name != null)

                            processActivity("Resource changed ", delta.getResource().getName());

                        switch (flags)
                        {
                        case IResourceDelta.OPEN:

                            processActivity("Resource opened or closed ", delta.getResource().getName());

                            break;

                        case IResourceDelta.CONTENT:

                            break;

                        case IResourceDelta.DESCRIPTION:

                            break;

                        case IResourceDelta.MARKERS:

                            break;

                        case IResourceDelta.TYPE:

                            break;

                        case IResourceDelta.SYNC:

                            break;

                        case IResourceDelta.REPLACED:

                            processActivity("Resource replaced", delta.getResource().getName());

                            break;

                        default:
                        //debuglog("a child resource must have been changed");
                        }

                        break;

                    }

                    return true;
                }

            };

            try {
                delta.accept(visitor);
            }
            catch (CoreException e) {

            }

        }
    }

    private class DebugEventSetAdapter implements IDebugEventSetListener
    {
        
        private ILaunch currentLaunch = null;
        
        public void handleDebugEvents(DebugEvent[] events)
        {
            Object source = events[0].getSource();
            
            if (source instanceof RuntimeProcess)
            {
                RuntimeProcess rp = (RuntimeProcess) source;
                
                ILaunch launch = rp.getLaunch();
                
                if(currentLaunch == null)
                {
                    currentLaunch = launch;
                    
                    analyseLaunch(launch);
                }
                else if(!currentLaunch.equals(launch))
                {
                    currentLaunch = launch;
                    
	                analyseLaunch(launch);
                }
            }
        }

        /**
         * @param launch
         */
        private void analyseLaunch(ILaunch launch)
        {
            if(launch.getLaunchMode().equals("run"))
            {
                processActivity("Run","");
            }
            else
            {
                processActivity("Debug","");
            }
        }
    }
    
    /**
     * Gets SensorProperties instance.
     * 
     * @return the SensorProperties instance.
     */
    public SensorProperties getSensorProperties()
    {
        return this.sensorProperties;
    }
}
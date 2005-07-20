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
import org.electrocodeogram.event.EventPacket;
import org.hackystat.kernel.admin.SensorProperties;
import org.hackystat.kernel.shell.SensorShell;
import org.hackystat.stdext.sensor.eclipse.event.EclipseSensorEvent;
import org.hackystat.stdext.sensor.eclipse.event.IEclipseSensorEventListener;
import org.hackystat.stdext.sensor.eclipse.junit.EclipseJUnitListener;

public class EclipseSensor
{

    private static final EclipseSensor theInstance = new EclipseSensor();

    public static final String ECLIPSE = "ECLIPSE";

    public static final String ECLIPSE_MONITOR = "ECLIPSE_MONITOR";

    public static final String ECLIPSE_BUFFTRANS = "ECLIPSE_BUFFTRANS";

    public static final String ECLIPSE_BUFFTRANS_INTERVAL = "HACKYSTAT_BUFFTRANS_INTERVAL";

    public static final String ECLIPSE_UPDATE = "ECLIPSE_UPDATE";

    public static final String ECLIPSE_UPDATE_URL = "ECLIPSE_UPDATE_URL";

    public static final String ECLIPSE_FILEMETRIC = "ECLIPSE_FILEMETRIC";

    public static final String ECLIPSE_BUILD = "ECLIPSE_BUILD";

    private long timerStateChangeInterval;

    private long timeBuffTransInterval;

    private ITextEditor activeTextEditor;

    private int activeBufferSize;

    private ITextEditor previousTextEditor;

    private ITextEditor deactivatedTextEditor;

    private int thresholdBufferSize;

    private boolean isModifiedFromFile;

    private boolean isActivatedWindow;

    private boolean isEnabled;

    private boolean isEnabledBuffTrans;

    private boolean isEnabledBcml;

    private boolean isEnabledBuild;

    private boolean isEnabledUpdate;

    private String dirKey;

    private String updateUrl;

    private EclipseSensorShell eclipseSensorShell;

    private SensorProperties sensorProperties;

    private List eventListeners;

    private ITestRunListener junitListener;

    private Timer timer;
  
    private TimerTask buffTransTimerTask;

    private WindowListenerAdapter windowListener;

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

        this.timeBuffTransInterval = (bufftransInterval != null) ? Long.parseLong(bufftransInterval) : 5;
        IWorkspace workspace = ResourcesPlugin.getWorkspace();

        workspace.addResourceChangeListener(new ResourceChangeAdapter(), IResourceChangeEvent.POST_CHANGE);
       
        if (!this.isEnabled) {
            return;
        }
        initializeSensor();
    }

    public void initializeSensor()
    {
        if (this.isEnabled) {
            SensorShell shell = new SensorShell(this.sensorProperties, false,"eclipse");
            this.eclipseSensorShell = new EclipseSensorShell(shell);
            
            String[] args = { "setTool", "Eclipse" };
            this.eclipseSensorShell.doCommand("Activity", Arrays.asList(args));
           
            if (this.buffTransTimerTask.scheduledExecutionTime() == 0) {
                this.timer.schedule(this.buffTransTimerTask, this.timeBuffTransInterval * 1000, this.timeBuffTransInterval * 1000);
            }

            if (this.junitListener == null) {
                this.junitListener = new EclipseJUnitListener(this);
                JUnitPlugin.getDefault().addTestRunListener(this.junitListener);
            }

            if (this.isEnabled && this.isEnabledUpdate) {
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

    private void initializeListeners()
    {
        IWorkbench workbench = EclipseSensorPlugin.getInstance().getWorkbench();

        IWorkbenchWindow[] activeWindows = workbench.getWorkbenchWindows();

        if (this.windowListener == null) {
            this.windowListener = new WindowListenerAdapter();
            workbench.addWindowListener(new WindowListenerAdapter());
        }
        for (int i = 0; i < activeWindows.length; i++) {
            IWorkbenchPage activePage = activeWindows[i].getActivePage();
            activePage.addPartListener(new PartListenerAdapter());
            IEditorPart activeEditorPart = activePage.getActiveEditor();

            if (activeEditorPart instanceof ITextEditor) {
                this.activeTextEditor = (ITextEditor) activeEditorPart;
                String fileName = EclipseSensor.this.getFqFileName(this.activeTextEditor);
                EclipseSensor.this.processActivity(EventPacket.ECG_TYPE_OPEN_FILE, "FILENAME:" + fileName);
                
                // TODO : finalize arguments
                
                IDocumentProvider provider = this.activeTextEditor.getDocumentProvider();
                IDocument document = provider.getDocument(activeEditorPart.getEditorInput());

                this.activeBufferSize = document.getLength();
                this.thresholdBufferSize = document.getLength();
                document.addDocumentListener(new DocumentListenerAdapter(document));
            }
        }

        IBreakpointManager bpManager = DebugPlugin.getDefault().getBreakpointManager();
        bpManager.addBreakpointListener(new HackystatBreakPointerListener());

        DebugPlugin dp = DebugPlugin.getDefault();
        dp.addDebugEventListener(new DebugEventSetAdapter());
    }

        public EclipseSensorShell getEclipseSensorShell()
    {
        return this.eclipseSensorShell;
    }

    public static EclipseSensor getInstance()
    {
        return theInstance;
    }

    private long getTimerStateChangeInterval()
    {
        return this.timerStateChangeInterval;
    }

    
    public void processFileMetric()
    {
        if (!this.isEnabled || !this.isEnabledBcml || (this.activeTextEditor == null)) {
            return;
        }

        
        String activeFileName = this.getFqFileName(this.activeTextEditor);
        if (activeFileName.endsWith(".java")) {
            String fullyQualifiedClassName = this.getFullyQualifedClassName();
            String classPath = this.getClassPath();
            String[] args = { "addJava", activeFileName, fullyQualifiedClassName, classPath };

            this.eclipseSensorShell.doCommand("FileMetric", Arrays.asList(args));
        }
    }

    
    public void processActivity(String ecgCommandName, String data)
    {
        if (!this.isEnabled) {
            return;
        }

        String[] args = { "add", ecgCommandName, data};
        
        this.eclipseSensorShell.doCommand("Activity", Arrays.asList(args));
    }

   
    private String latestStateChangeFileName = "";

    private int latestStateChangeFileSize = 0;

   
    public void processStateChangeActivity(IDocument oldDocument, IDocument newDocument)
    {
        if (!this.isEnabled) {
            return;
        }
        String activeFileName = getFqFileName(activeTextEditor);
        String activeTitle = getTitle(activeTextEditor);
        
        assert(! activeFileName.equals(""));
        assert(! activeTitle.equals(""));
        
        int bs = activeBufferSize;
    
        processActivity(EventPacket.ECG_TYPE_CODECHANGE, "TITLE:" + activeTitle + ",FILENAME:" + activeFileName + ",BEFORE:" + oldDocument.get() + ",AFTER:" + newDocument.get());
        
        this.latestStateChangeFileName = activeFileName;
        this.latestStateChangeFileSize = activeBufferSize;

        // Send the new metrics if there are any
        this.processFileMetric();
        
    }

    /** Keep track the last buffer trans data incase of repeation. */
    private String latestBuffTrans = "";

   
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

   
    public void processUnitTest(List argList)
    {
        if (!this.isEnabled) {
            return;
        }
        this.eclipseSensorShell.doCommand("UnitTest", argList);
    }

   
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

   
    public void addEclipseSensorEventListener(IEclipseSensorEventListener listener)
    {
        this.eventListeners.add(listener);
    }

    private void notifyAll(EclipseSensorEvent event)
    {
        for (Iterator i = this.eventListeners.iterator(); i.hasNext();) {
            ((IEclipseSensorEventListener) i.next()).notify(event);
        }
    }

   
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

   
    private class WindowListenerAdapter implements IWindowListener
    {
    
        public void windowActivated(IWorkbenchWindow window)
        {

           processActivity(EventPacket.ECG_TYPE_WINDOW_ACTIVATED, "");

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

            processActivity(EventPacket.ECG_TYPE_WINDOW_DEACTIVATED, "");

            EclipseSensor.this.processFileMetric();
            EclipseSensor.this.eclipseSensorShell.send();
        }

       
        public void windowDeactivated(IWorkbenchWindow window)
        {

            processActivity(EventPacket.ECG_TYPE_WINDOW_DEACTIVATED, "");

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

        
        public void windowOpened(IWorkbenchWindow window)
        {

            processActivity(EventPacket.ECG_TYPE_WINDOW_OPENED, "");

            EclipseSensor.this.initializeListeners();
        }
    }

 
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

                processActivity(EventPacket.ECG_TYPE_EDITOR_ACTIVATED, "TITLE:" + editor.getTitle());

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
                processActivity(EventPacket.ECG_TYPE_PART_ACTIVATED, "TITLE:" + part.getTitle());
            }
        }

       
        public void partBroughtToTop(IWorkbenchPart part)
        {
            // not supported in Eclipse Sensor.
        }

       
        public void partClosed(IWorkbenchPart part)
        {
            if (part instanceof ITextEditor) {

                String fileName = EclipseSensor.this.getFqFileName((ITextEditor) part);
                
                String title = getTitle((ITextEditor) part);
                
                processActivity(EventPacket.ECG_TYPE_EDITOR_CLOSED, "TITLE:" + title);

                
                IEditorPart activeEditorPart = part.getSite().getPage().getActiveEditor();
                if (activeEditorPart == null) {
                    EclipseSensor.this.activeTextEditor = null;
                }
            }
            else {
                processActivity(EventPacket.ECG_TYPE_PART_CLOSED, "TITLE:" + part.getTitle());
            }
        }

       
        public void partDeactivated(IWorkbenchPart part)
        {
            if (part instanceof ITextEditor && (part != EclipseSensor.this.deactivatedTextEditor)) {
                EclipseSensor.this.deactivatedTextEditor = (ITextEditor) part;

                processActivity(EventPacket.ECG_TYPE_EDITOR_DEACTIVATED,"TITLE:" + part.getTitle());

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
                processActivity(EventPacket.ECG_TYPE_PART_DEACTIVATED, "TITLE:" + part.getTitle());
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

                processActivity(EventPacket.ECG_TYPE_EDITOR_OPENED, "TITLE:" + part.getTitle());

                ITextEditor editor = (ITextEditor) part;
                
                IDocumentProvider provider = editor.getDocumentProvider();
                IDocument document = provider.getDocument(editor.getEditorInput());
                document.addDocumentListener(new DocumentListenerAdapter(document));
               
                
                EclipseSensor.this.activeTextEditor = (ITextEditor) part;
            }
            else {
                processActivity(EventPacket.ECG_TYPE_PART_OPENED, "TITLE:" + part.getTitle());
            }
        }
    }

   
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
                EclipseSensor.this.processActivity(EventPacket.ECG_TYPE_BREAKPOINT_SET, file.getLocation().toString() + "#" + String.valueOf(((ILineBreakpoint) breakpoint).getLineNumber()));
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
                EclipseSensor.this.processActivity(EventPacket.ECG_TYPE_BREAKPOINT_UNSET, file.getLocation().toString() + "#" + String.valueOf(((ILineBreakpoint) breakpoint).getLineNumber()));
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
        
        private IDocument backupedDocument = null;
        
        /**
         * @param document
         */
        public DocumentListenerAdapter(IDocument document)
        {
            
            	backupDocument(document);
        }

        /**
         * @param document
         */
        private void backupDocument(IDocument document)
        {
            backupedDocument = new org.eclipse.jface.text.Document();
            
            backupedDocument.set(document.get());
        }

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
            activeBufferSize = event.getDocument().getLength();
                       
            timer.cancel();
            
            timer = new Timer();
            
            timer.schedule(new CodeChangeTimerTask(backupedDocument,event.getDocument()),2000);
            
            backupDocument(event.getDocument());
           
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
                processActivity(EventPacket.ECG_TYPE_RUN,"");
            }
            else
            {
                processActivity(EventPacket.ECG_TYPE_DEBUG,"");
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
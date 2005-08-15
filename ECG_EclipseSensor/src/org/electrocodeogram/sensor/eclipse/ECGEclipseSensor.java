package org.electrocodeogram.sensor.eclipse;

import java.util.Arrays;
import java.util.Timer;

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
import org.hackystat.stdext.sensor.eclipse.EclipseSensor;

import org.hackystat.stdext.sensor.eclipse.EclipseSensorShell;



/**
 * This is the ECG EclipseSensor. It uses the original HackyStat EclipseSensor
 * and extends. So all original HackyStat events are recorded along with any
 * newly introduced ECG events.
 */
public class ECGEclipseSensor
{

    private static ECGEclipseSensor theInstance = null;

    private EclipseSensor hackyEclipse = null;

    private boolean isEnabled = true;

    private EclipseSensorShell eclipseSensorShell;
    
    private String $username = null;
    
    private String $projectname = null;

    private Timer $timer = null;
    
    private ITextEditor $activeTextEditor;
    
    /**
     * This constant holds the HackyStat activity-type String which indicates
     * that an event is in ECG MicroActivity event.
     */
    private static final String MICRO_ACTIVITY_STRING = "MicroActivity";
    
    /**
     * This constand holds the HackyStat add-command, which tells the HackyStat
     * server to add this event to its list of events.
     */
    private static final String HACKYSTAT_ADD_COMMAND = "add";

    /**
     * This constant holds the HackyStat Activity String which indicates that an
     * event is in HackyStat Activity event. This is also true for all ECG
     * MicroActivity events.
     */
    private static final String HACKYSTAT_ACTIVITY_STRING = "Activity";
    
    
    /**
     * This constanst tells how long to ait after a user input before a Codechange event
     * is send.
     */
    private static final int CODECHANGE_INTERVALL = 2000;
    
    private ECGEclipseSensor()
    {
      
        this.hackyEclipse = EclipseSensor.getInstance();
        
        this.eclipseSensorShell = this.hackyEclipse.getEclipseSensorShell();
        
        // try to get the username from the operating system environment
        this.$username = System.getenv("username");
        
        if(this.$username == null || this.$username.equals(""))
        {
            this.$username = "n.a.";
        }
      
        this.$timer  = new Timer();
        
        // add the WindowListener for listening on
        // window events.
        
        IWorkbench workbench = PlatformUI.getWorkbench();
        
        workbench.addWindowListener(new WindowListenerAdapter());
        
        // add the PartListener for listening on
        // part events.
        
        IWorkbenchWindow[] activeWindows = workbench.getWorkbenchWindows();
        
        IWorkbenchPage activePage = null;
        
        for (int i = 0; i < activeWindows.length; i++)
        {
            activePage = activeWindows[i].getActivePage();
        
            activePage.addPartListener(new PartListenerAdapter());
        }
        
        // add the DocumentListener for listening on
        // document events.
        
        IEditorPart part = activePage.getActiveEditor();
        
        if (part instanceof ITextEditor)
        {
            processActivity("<?xml version=\"1.0\"?><microActivity><commonData><username>"+getUsername()+"</username><projectname>"+getProjectname()+"</projectname></commonData><editor><activity>opened</activity><editorname>" + part.getTitle() + "</editorname></editor></microActivity>");
            
            this.$activeTextEditor = (ITextEditor) part;
            
            IDocumentProvider provider =  this.$activeTextEditor.getDocumentProvider();
            
            IDocument document = provider.getDocument(part.getEditorInput());

            document.addDocumentListener(new DocumentListenerAdapter());
        }
        
        // add the ResourceChangeListener to the workspace for listening on
        // resource events.
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        
        workspace.addResourceChangeListener(new ResourceChangeAdapter(), IResourceChangeEvent.POST_CHANGE);
        
        
        // add the DebugEventSetListener to listen to run and debug events.
        DebugPlugin dp = DebugPlugin.getDefault();
        
        dp.addDebugEventListener(new DebugEventSetAdapter());
    }


    void setActiveTextEditor(ITextEditor activeTextEditor)
    {
        this.$activeTextEditor = activeTextEditor;
    }
    
    ITextEditor getActiveTextEditor()
    {
        return this.$activeTextEditor;
    }
    
    Timer getTimer()
    {
        return this.$timer;
    }
    
    void setTimer(Timer timer)
    {
        this.$timer = timer;
    }
    
    /**
     * This method is returning the unsername of the user currently logged into
     * the system this plug-in is working in.
     * 
     * @return The username of the currently logged in user
     */
    String getUsername()
    {
        return this.$username;
    }
    
    /**
     * This methos is used by the internal listeners to change the name of the current open project,
     * @param projectname Is tha nam of the project the user orks on
     */
    void setProjectname(String projectname)
    {
        this.$projectname = projectname;
    }
    
    /**
     * This method returns the name of the project the user is currently working on.  
     * @return The name of the project the user is currently working on
     */
    String getProjectname()
    {
        return this.$projectname;
    }
    
    /**
     * This method returns the sengleton instancve of the ECGEclipseSensor.
     * 
     * @return The sengleton instancve of the ECGEclipseSensor
     */
    public static ECGEclipseSensor getInstance()
    {
        if(theInstance == null)
        {
            theInstance = new ECGEclipseSensor();
        }
        
        return theInstance;
    }
 
    /**
     * This method takes the data of a recorded MicroActivity event and
     * generates and sends a HackyStat Activity event with the given event data.
     * The HackyStat command name is set to the value "add" and the HackyStat
     * activtiy-type is set to the value "MicroActivity".
     * 
     * @param data
     *            This is the actual MicroActivity encoded in an XML String that
     *            is conforming to one of the defined XML Schemas.s
     */
    public void processActivity(String data)
    {
        if (!this.isEnabled ) {
            return;
        }
        
        String[] args = { HACKYSTAT_ADD_COMMAND, MICRO_ACTIVITY_STRING, data};
        
        this.eclipseSensorShell.doCommand(HACKYSTAT_ACTIVITY_STRING, Arrays.asList(args));
    }
    
    /**
     * This class is the WindowListenerAdapter which is registered for listening to Window events.
     *
     */
    private class WindowListenerAdapter implements IWindowListener
    {
    
        /**
         * @see org.eclipse.ui.IWindowListener#windowActivated(org.eclipse.ui.IWorkbenchWindow)
         */
        public void windowActivated(IWorkbenchWindow window)
        {

           processActivity("<?xml version=\"1.0\"?><microActivity><commonData><username>"+getUsername()+"</username><projectname>"+getProjectname()+"</projectname></commonData><window><activity>activated</activity><windowname>" + window.getActivePage().getLabel() + "</window></window></microActivity>");

        }

       
        /**
         * @see org.eclipse.ui.IWindowListener#windowClosed(org.eclipse.ui.IWorkbenchWindow)
         */
        public void windowClosed(IWorkbenchWindow window)
        {

            processActivity("<?xml version=\"1.0\"?><microActivity><commonData><username>"+getUsername()+"</username><projectname>"+getProjectname()+"</projectname></commonData><window><activity>closed</activity><windowname>" + window.getActivePage().getLabel() + "</window></window></microActivity>");
        }

       
        /**
         * @see org.eclipse.ui.IWindowListener#windowDeactivated(org.eclipse.ui.IWorkbenchWindow)
         */
        public void windowDeactivated(IWorkbenchWindow window)
        {

            processActivity("<?xml version=\"1.0\"?><microActivity><commonData><username>"+getUsername()+"</username><projectname>"+getProjectname()+"</projectname></commonData><window><activity>deactivated</activity><windowname>" + window.getActivePage().getLabel() + "</window></window></microActivity>");
        }

        
        /**
         * @see org.eclipse.ui.IWindowListener#windowOpened(org.eclipse.ui.IWorkbenchWindow)
         */
        public void windowOpened(IWorkbenchWindow window)
        {

            processActivity("<?xml version=\"1.0\"?><microActivity><commonData><username>"+getUsername()+"</username><projectname>"+getProjectname()+"</projectname></commonData><window><activity>deactivated</activity><windowname>" + window.getActivePage().getLabel() + "</window></window></microActivity>");
        }
    }

    
    /**
     * This class is the RecourceChangeAdapter which is registered for listening to ResourceChange events.
     *
     */
    private class ResourceChangeAdapter implements IResourceChangeListener
    {
        
        /**
         * Provides manipulation of IResourceChangeEvent instance due to
         * implement <code>IResourceChangeListener</code>. This method must
         * not be called by client because it is called by platform when
         * resources are changed.
         * 
         * @param event
         *            A resource change event to describe changes to resources.
         */
        public void resourceChanged(IResourceChangeEvent event)
        {

            if (event.getType() != IResourceChangeEvent.POST_CHANGE)
                return;

            IResourceDelta $delta = event.getDelta();

            IResourceDeltaVisitor visitor = new IResourceDeltaVisitor() {

                public boolean visit(IResourceDelta delta)
                {

                    // get the kind of the ResourceChangedEvent
                    int kind = delta.getKind();
                   
                    // get the resource
                    IResource resource = delta.getResource();
         
                    String resourceType = null;
                    
                    // get the resourceType String
                    switch(resource.getType())
                    {
                        case IResource.ROOT:
                            
                            resourceType = "root";
                            
                            return true;
                            
                        case IResource.PROJECT:
                            
                            resourceType = "project";
                            
                            break;
                            
                        case IResource.FOLDER:
                            
                            resourceType = "folder";
                            
                            break;
                            
                        case  IResource.FILE:
                            
                            resourceType = "file";
                            
                            break;
                            
                            default:
                                
                                resourceType = "n.a.";
                            
                            break;
                        
                    }

                    switch (kind)
                    {
                    // a resource has been added
                    case IResourceDelta.ADDED:

                        processActivity("<?xml version=\"1.0\"?><microActivity><commonData><username>"+getUsername()+"</username><projectname>"+getProjectname()+"</projectname></commonData><resource><activity>added</activity><resourcename>" + resource.getName() + "</resourcename><resourcetype>"+ resourceType +"</resourcetype></resource></microActivity>");

                        break;
                    // a resource has been removed
                    case IResourceDelta.REMOVED:

                        processActivity("<?xml version=\"1.0\"?><microActivity><commonData><username>"+getUsername()+"</username><projectname>"+getProjectname()+"</projectname></commonData><resource><activity>removed</activity><resourcename>" + resource.getName() + "</resourcename><resourcetype>"+ resourceType +"</resourcetype></resource></microActivity>");

                        break;
                        // a resource has been changed
                    case IResourceDelta.CHANGED:
                    
                        // if its a project change, set the name of the project to be the name used.
                        if(resource instanceof IProject)
                        {
                            setProjectname(resource.getName());
                        }
                        else
                        {
                            processActivity("<?xml version=\"1.0\"?><microActivity><commonData><username>"+getUsername()+"</username><projectname>"+getProjectname()+"</projectname></commonData><resource><activity>changed</activity><resourcename>" + resource.getName() + "</resourcename><resourcetype>"+ resourceType +"</resourcetype></resource></microActivity>");
                        }
                        break;
                    }
                    
                    return true;
                }

            };

           
                try {
                    $delta.accept(visitor);
                }
                catch (CoreException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
           
        }
    }
 
    /**
     * This class is the DebugEventSetAdapter which is registered for listening to DebugEventSet events.
     *
     */
    private class DebugEventSetAdapter implements IDebugEventSetListener
    {
        
        private ILaunch currentLaunch = null;
        
        
        /**
         * @see org.eclipse.debug.core.IDebugEventSetListener#handleDebugEvents(org.eclipse.debug.core.DebugEvent[])
         */
        public void handleDebugEvents(DebugEvent[] events)
        {
            Object source = events[0].getSource();
            
            if (source instanceof RuntimeProcess)
            {
                RuntimeProcess rp = (RuntimeProcess) source;
                
                ILaunch launch = rp.getLaunch();
                
                if(this.currentLaunch == null)
                {
                    this.currentLaunch = launch;
                    
                    analyseLaunch(launch);
                }
                else if(!this.currentLaunch.equals(launch))
                {
                    this.currentLaunch = launch;
                    
                    analyseLaunch(launch);
                }
            }
        }
       
        private void analyseLaunch(ILaunch launch)
        {
            if(launch.getLaunchMode().equals("run"))
            {
                
                processActivity("<?xml version=\"1.0\"?><microActivity><commonData><username>"+getUsername()+"</username><projectname>"+getProjectname()+"</projectname></commonData><run debug=\"false\"></run></microActivity>");
            }
            else
            {
                processActivity("<?xml version=\"1.0\"?><microActivity><commonData><username>"+getUsername()+"</username><projectname>"+getProjectname()+"</projectname></commonData><run debug=\"true\"></run></microActivity>");
            }
        }
    }

    /**
     * This class is the PartListenerAdapter which is registered for listening to Part events.
     *
     */
    private class PartListenerAdapter implements IPartListener
    {
       
        /**
         * @see org.eclipse.ui.IPartListener#partActivated(org.eclipse.ui.IWorkbenchPart)
         */
        public void partActivated(IWorkbenchPart part)
        {

            if (part instanceof ITextEditor) {

                setActiveTextEditor((ITextEditor) part);
                
                processActivity("<?xml version=\"1.0\"?><microActivity><commonData><username>"+getUsername()+"</username><projectname>"+getProjectname()+"</projectname></commonData><editor><activity>activated</activity><editorname>" + part.getTitle() + "</editorname></editor></microActivity>");
            
            }
            else {
                processActivity("<?xml version=\"1.0\"?><microActivity><commonData><username>"+getUsername()+"</username><projectname>"+getProjectname()+"</projectname></commonData><part><activity>activated</activity><partname>" + part.getTitle() + "</partname></part></microActivity>");
            }
        }

        /**
         * @see org.eclipse.ui.IPartListener#partClosed(org.eclipse.ui.IWorkbenchPart)
         */
        public void partClosed(IWorkbenchPart part)
        {
            if (part instanceof ITextEditor) {

                processActivity("<?xml version=\"1.0\"?><microActivity><commonData><username>"+getUsername()+"</username><projectname>"+getProjectname()+"</projectname></commonData><editor><activity>closed</activity><editorname>" + part.getTitle() + "</editorname></editor></microActivity>");
            
            }
            else {
                processActivity("<?xml version=\"1.0\"?><microActivity><commonData><username>"+getUsername()+"</username><projectname>"+getProjectname()+"</projectname></commonData><part><activity>closed</activity><partname>" + part.getTitle() + "</partname></part></microActivity>");
            }
        }

       
        /**
         * @see org.eclipse.ui.IPartListener#partDeactivated(org.eclipse.ui.IWorkbenchPart)
         */
        public void partDeactivated(IWorkbenchPart part)
        {
            if (part instanceof ITextEditor) {

                processActivity("<?xml version=\"1.0\"?><microActivity><commonData><username>"+getUsername()+"</username><projectname>"+getProjectname()+"</projectname></commonData><editor><activity>deactivated</activity><editorname>" + part.getTitle() + "</editorname></editor></microActivity>");
            
            }
            else {
                processActivity("<?xml version=\"1.0\"?><microActivity><commonData><username>"+getUsername()+"</username><projectname>"+getProjectname()+"</projectname></commonData><part><activity>deactivated</activity><partname>" + part.getTitle() + "</partname></part></microActivity>");
            }
        }

        /**
         * @see org.eclipse.ui.IPartListener#partOpened(org.eclipse.ui.IWorkbenchPart)
         */
        public void partOpened(IWorkbenchPart part)
        {
            if (part instanceof ITextEditor) {

                processActivity("<?xml version=\"1.0\"?><microActivity><commonData><username>"+getUsername()+"</username><projectname>"+getProjectname()+"</projectname></commonData><editor><activity>opened</activity><editorname>" + part.getTitle() + "</editorname></editor></microActivity>");
            
            }
            else {
                processActivity("<?xml version=\"1.0\"?><microActivity><commonData><username>"+getUsername()+"</username><projectname>"+getProjectname()+"</projectname></commonData><part><activity>opened</activity><partname>" + part.getTitle() + "</partname></part></microActivity>");
            }
        }

        /**
         * @see org.eclipse.ui.IPartListener#partBroughtToTop(org.eclipse.ui.IWorkbenchPart)
         */
        public void partBroughtToTop(@SuppressWarnings("unused") IWorkbenchPart part)
        {
            // not implemented
            
        }
    }

    
    /**
     * This class is the DocumentListenerAdapter which is registered for listening to Document events.
     *
     */
    private class DocumentListenerAdapter implements IDocumentListener
    {
        
       
        
        /**
         * @see org.eclipse.jface.text.IDocumentListener#documentAboutToBeChanged(org.eclipse.jface.text.DocumentEvent)
         */
        public void documentAboutToBeChanged(@SuppressWarnings("unused") DocumentEvent event)
        {
            // not supported in Eclipse Sensor.
        }

        
        /**
         * @see org.eclipse.jface.text.IDocumentListener#documentChanged(org.eclipse.jface.text.DocumentEvent)
         */
        public void documentChanged(DocumentEvent event)
        {
            getTimer().cancel();
            
            setTimer(new Timer());
            
            getTimer().schedule(new CodeChangeTimerTask(event.getDocument(),getActiveTextEditor().getTitle()),ECGEclipseSensor.CODECHANGE_INTERVALL);
            
        }
    }
}

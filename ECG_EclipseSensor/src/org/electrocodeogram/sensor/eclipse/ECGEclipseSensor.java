package org.electrocodeogram.sensor.eclipse;

import java.io.File;
import java.io.PrintWriter;
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
import org.electrocodeogram.logging.LogHelper;
import org.hackystat.kernel.admin.SensorProperties;
import org.hackystat.kernel.shell.SensorShell;
import org.hackystat.stdext.sensor.eclipse.EclipseSensor;
import org.hackystat.stdext.sensor.eclipse.EclipseSensorPlugin;
import org.hackystat.stdext.sensor.eclipse.EclipseSensorShell;



/**
 * This is the ECG EclipseSensor. It uses the original HackyStat EclipseSensor
 * and extends. So all original HackyStat events are recorded along with any
 * newly introduced ECG events.
 */
public class ECGEclipseSensor
{

	private PrintWriter writer;
	
    private static ECGEclipseSensor _theInstance = null;

    private EclipseSensor _hackyEclipse = null;

    private boolean _isEnabled = true;

    private EclipseSensorShell _eclipseSensorShell;
    
    private Logger _logger;
    
    String _username = null;
    
    String _projectname = null;

    private Timer _timer = null;
    
    ITextEditor _activeTextEditor;
    
    String _activeWindowName;

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
    	this._logger = LogHelper.createLogger(this);
    	
    	this._hackyEclipse = EclipseSensor.getInstance();
        
        this._eclipseSensorShell = this._hackyEclipse.getEclipseSensorShell();
          
        String id = EclipseSensorPlugin.getInstance().getDescriptor().getUniqueIdentifier();
    	
    	String version = EclipseSensorPlugin.getInstance().getDescriptor().getVersionIdentifier().toString();
    	
    	String[] path = {"plugins" + File.separator + id + "_" + version + File.separator + "ecg"};
        
    	List list = Arrays.asList(path);
		 
    	this._eclipseSensorShell.doCommand(SensorShell.ECG_LAB_PATH,list);
    	
        // try to get the username from the operating system environment
        this._username = System.getenv("username");
        
        if(this._username == null || this._username.equals(""))
        {
            this._username = "n.a.";
        }
      
        this._logger.log(Level.INFO,"Username is set to" + this._username);
        
        this._timer  = new Timer();
        
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
            processActivity("<?xml version=\"1.0\"?><microActivity><commonData><username>"+ECGEclipseSensor.this._username+"</username><projectname>"+ECGEclipseSensor.this._projectname+"</projectname></commonData><editor><activity>opened</activity><editorname>" + part.getTitle() + "</editorname></editor></microActivity>");
            
            this._activeTextEditor = (ITextEditor) part;
            
            IDocumentProvider provider =  this._activeTextEditor.getDocumentProvider();
            
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
  
	Timer getTimer()
    {
        return this._timer;
    }
    
    void setTimer(Timer timer)
    {
        this._timer = timer;
    }
    
    /**
     * This returns the current username.
     * @return The current username
     */
    public String getUsername()
    {
    	return this._username;
    }
  
    /**
     * This returns the current projectname.
     * @return The current projectname
     */
    public String getProjectname()
    {
    	return this._projectname;
    }
    
    /**
     * This method returns the sengleton instancve of the ECGEclipseSensor.
     * 
     * @return The sengleton instancve of the ECGEclipseSensor
     */
    public static ECGEclipseSensor getInstance()
    {
        if(_theInstance == null)
        {
            _theInstance = new ECGEclipseSensor();
        }
        
        return _theInstance;
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
        if (!this._isEnabled ) {
            return;
        }
        
        // TODO : bring constants to event
         
        String[] args = { HACKYSTAT_ADD_COMMAND, MICRO_ACTIVITY_STRING, data};
        
        // if eclipse is shutting down the eclipseSensorShell might be gone allready
        if(this._eclipseSensorShell != null)
        {
        	this._eclipseSensorShell.doCommand(HACKYSTAT_ACTIVITY_STRING, Arrays.asList(args));
        	
        }
        
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

        	ECGEclipseSensor.this._activeWindowName = window.getActivePage().getLabel();
        	
        	processActivity("<?xml version=\"1.0\"?><microActivity><commonData><username>"+ECGEclipseSensor.this._username+"</username><projectname>"+ECGEclipseSensor.this._projectname+"</projectname></commonData><window><activity>activated</activity><windowname>" + ECGEclipseSensor.this._activeWindowName + "</windowname></window></microActivity>");

        }

       
        /**
         * @see org.eclipse.ui.IWindowListener#windowClosed(org.eclipse.ui.IWorkbenchWindow)
         */
        public void windowClosed(@SuppressWarnings("unused") IWorkbenchWindow window)
        {
            processActivity("<?xml version=\"1.0\"?><microActivity><commonData><username>"+ECGEclipseSensor.this._username+"</username><projectname>"+ECGEclipseSensor.this._projectname+"</projectname></commonData><window><activity>closed</activity><windowname>" + ECGEclipseSensor.this._activeWindowName + "</windowname></window></microActivity>");
        }

       
        /**
         * @see org.eclipse.ui.IWindowListener#windowDeactivated(org.eclipse.ui.IWorkbenchWindow)
         */
        public void windowDeactivated(@SuppressWarnings("unused") IWorkbenchWindow window)
        {

            processActivity("<?xml version=\"1.0\"?><microActivity><commonData><username>"+ECGEclipseSensor.this._username+"</username><projectname>"+ECGEclipseSensor.this._projectname+"</projectname></commonData><window><activity>deactivated</activity><windowname>" + ECGEclipseSensor.this._activeWindowName + "</windowname></window></microActivity>");
        }

        
        /**
         * @see org.eclipse.ui.IWindowListener#windowOpened(org.eclipse.ui.IWorkbenchWindow)
         */
        public void windowOpened(IWorkbenchWindow window)
        {

            processActivity("<?xml version=\"1.0\"?><microActivity><commonData><username>"+ECGEclipseSensor.this._username+"</username><projectname>"+ECGEclipseSensor.this._projectname+"</projectname></commonData><window><activity>deactivated</activity><windowname>" + window.getActivePage().getLabel() + "</windowname></window></microActivity>");
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

                        processActivity("<?xml version=\"1.0\"?><microActivity><commonData><username>"+ECGEclipseSensor.this._username+"</username><projectname>"+ECGEclipseSensor.this._projectname+"</projectname></commonData><resource><activity>added</activity><resourcename>" + resource.getName() + "</resourcename><resourcetype>"+ resourceType +"</resourcetype></resource></microActivity>");

                        break;
                    // a resource has been removed
                    case IResourceDelta.REMOVED:

                        processActivity("<?xml version=\"1.0\"?><microActivity><commonData><username>"+ECGEclipseSensor.this._username+"</username><projectname>"+ECGEclipseSensor.this._projectname+"</projectname></commonData><resource><activity>removed</activity><resourcename>" + resource.getName() + "</resourcename><resourcetype>"+ resourceType +"</resourcetype></resource></microActivity>");

                        break;
                        // a resource has been changed
                    case IResourceDelta.CHANGED:
                    
                        // if its a project change, set the name of the project to be the name used.
                        if(resource instanceof IProject)
                        {
                            ECGEclipseSensor.this._projectname = resource.getName();
                        }
                        else
                        {
                            processActivity("<?xml version=\"1.0\"?><microActivity><commonData><username>"+ECGEclipseSensor.this._username+"</username><projectname>"+ECGEclipseSensor.this._projectname+"</projectname></commonData><resource><activity>changed</activity><resourcename>" + resource.getName() + "</resourcename><resourcetype>"+ resourceType +"</resourcetype></resource></microActivity>");
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
                
                processActivity("<?xml version=\"1.0\"?><microActivity><commonData><username>"+ECGEclipseSensor.this._username+"</username><projectname>"+ECGEclipseSensor.this._projectname+"</projectname></commonData><run debug=\"false\"></run></microActivity>");
            }
            else
            {
                processActivity("<?xml version=\"1.0\"?><microActivity><commonData><username>"+ECGEclipseSensor.this._username+"</username><projectname>"+ECGEclipseSensor.this._projectname+"</projectname></commonData><run debug=\"true\"></run></microActivity>");
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

                ECGEclipseSensor.this._activeTextEditor = (ITextEditor) part;
                
                IDocumentProvider provider =  ECGEclipseSensor.this._activeTextEditor.getDocumentProvider();
                
                IDocument document = provider.getDocument(ECGEclipseSensor.this._activeTextEditor.getEditorInput());

                document.addDocumentListener(new DocumentListenerAdapter());
                
                processActivity("<?xml version=\"1.0\"?><microActivity><commonData><username>"+ECGEclipseSensor.this._username+"</username><projectname>"+ECGEclipseSensor.this._projectname+"</projectname></commonData><editor><activity>activated</activity><editorname>" + part.getTitle() + "</editorname></editor></microActivity>");
            
            }
            else {
                processActivity("<?xml version=\"1.0\"?><microActivity><commonData><username>"+ECGEclipseSensor.this._username+"</username><projectname>"+ECGEclipseSensor.this._projectname+"</projectname></commonData><part><activity>activated</activity><partname>" + part.getTitle() + "</partname></part></microActivity>");
            }
        }

        /**
         * @see org.eclipse.ui.IPartListener#partClosed(org.eclipse.ui.IWorkbenchPart)
         */
        public void partClosed(IWorkbenchPart part)
        {
            if (part instanceof ITextEditor) {

                processActivity("<?xml version=\"1.0\"?><microActivity><commonData><username>"+ECGEclipseSensor.this._username+"</username><projectname>"+ECGEclipseSensor.this._projectname+"</projectname></commonData><editor><activity>closed</activity><editorname>" + part.getTitle() + "</editorname></editor></microActivity>");
            
            }
            else {
                processActivity("<?xml version=\"1.0\"?><microActivity><commonData><username>"+ECGEclipseSensor.this._username+"</username><projectname>"+ECGEclipseSensor.this._projectname+"</projectname></commonData><part><activity>closed</activity><partname>" + part.getTitle() + "</partname></part></microActivity>");
            }
        }

       
        /**
         * @see org.eclipse.ui.IPartListener#partDeactivated(org.eclipse.ui.IWorkbenchPart)
         */
        public void partDeactivated(IWorkbenchPart part)
        {
            if (part instanceof ITextEditor) {

                processActivity("<?xml version=\"1.0\"?><microActivity><commonData><username>"+ECGEclipseSensor.this._username+"</username><projectname>"+ECGEclipseSensor.this._projectname+"</projectname></commonData><editor><activity>deactivated</activity><editorname>" + part.getTitle() + "</editorname></editor></microActivity>");
            
            }
            else {
                processActivity("<?xml version=\"1.0\"?><microActivity><commonData><username>"+ECGEclipseSensor.this._username+"</username><projectname>"+ECGEclipseSensor.this._projectname+"</projectname></commonData><part><activity>deactivated</activity><partname>" + part.getTitle() + "</partname></part></microActivity>");
            }
        }

        /**
         * @see org.eclipse.ui.IPartListener#partOpened(org.eclipse.ui.IWorkbenchPart)
         */
        public void partOpened(IWorkbenchPart part)
        {
            if (part instanceof ITextEditor) {

                processActivity("<?xml version=\"1.0\"?><microActivity><commonData><username>"+ECGEclipseSensor.this._username+"</username><projectname>"+ECGEclipseSensor.this._projectname+"</projectname></commonData><editor><activity>opened</activity><editorname>" + part.getTitle() + "</editorname></editor></microActivity>");
            
            }
            else {
                processActivity("<?xml version=\"1.0\"?><microActivity><commonData><username>"+ECGEclipseSensor.this._username+"</username><projectname>"+ECGEclipseSensor.this._projectname+"</projectname></commonData><part><activity>opened</activity><partname>" + part.getTitle() + "</partname></part></microActivity>");
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
            
            getTimer().schedule(new CodeChangeTimerTask(event.getDocument(),ECGEclipseSensor.this._activeTextEditor.getTitle()),ECGEclipseSensor.CODECHANGE_INTERVALL);
            
        }
    }
    
    private static class CodeChangeTimerTask extends TimerTask
    {

        private IDocument $document = null;
        
        private String $documentName = null;

        /**
         * This creates the Task.
         * @param document Is the document in which the codechange has occured.
         * @param documentName Is the name of the document the codechange has occured.
         */
        public CodeChangeTimerTask(IDocument document, String documentName)
        {
           this.$document = document;
           
           this.$documentName = documentName;
        }

        /**
         * @see java.util.TimerTask#run()
         */
        @Override
        public void run()
        {
            ECGEclipseSensor sensor = ECGEclipseSensor.getInstance();

            sensor.processActivity("<?xml version=\"1.0\"?><microActivity><commonData><username>"+sensor.getUsername()+"</username><projectname>"+sensor.getProjectname()+"</projectname></commonData><codechange><document><![CDATA["+this.$document.get()+"]]></document><documentname>"+this.$documentName+"</documentname></codechange></microActivity>");
            
//          while(true)
//          {
//        	try
//			{
//				Thread.sleep(10);
//			}
//			catch (InterruptedException e)
//			{
//				SensorShell.log(e.getMessage());
//			}
//        	 
//        	 sensor.processActivity("<?xml version=\"1.0\"?><microActivity><commonData><username>"+sensor.getUsername()+"</username><projectname>"+sensor.getProjectname()+"</projectname></commonData><window><activity>activated</activity><windowname>foo</windowname></window></microActivity>");
//          }

        }
    }
   
}

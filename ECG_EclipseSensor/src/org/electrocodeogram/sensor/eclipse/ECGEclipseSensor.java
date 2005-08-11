package org.electrocodeogram.sensor.eclipse;

import java.util.Arrays;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.hackystat.stdext.sensor.eclipse.EclipseSensor;
import org.hackystat.stdext.sensor.eclipse.EclipseSensorShell;

/**
 * This is the ECG EclipseSensor.
 * It uses the original HackyStat EclipseSensor and extends.
 * So all original HackyStat events are recorded along with any newly
 * introduced ECG events.
 */
public class ECGEclipseSensor
{

    private static ECGEclipseSensor theInstance = null;

    private EclipseSensor hackyEclipse = null;

    private boolean isEnabled = true;

    private EclipseSensorShell eclipseSensorShell;
    
    private String $username = null;
    
    private ECGEclipseSensor()
    {
      
        this.hackyEclipse = EclipseSensor.getInstance();
        
        this.eclipseSensorShell = this.hackyEclipse.getEclipseSensorShell();
        
        this.$username = System.getenv("username");
        
        if(this.$username == null || this.$username.equals(""))
        {
            this.$username = "n.a.";
        }
      
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        
        workspace.addResourceChangeListener(new ResourceChangeAdapter(), IResourceChangeEvent.POST_CHANGE);
    }

    String getUsername()
    {
        return this.$username;
    }
    
    public static ECGEclipseSensor getInstance()
    {
        if(theInstance == null)
        {
            theInstance = new ECGEclipseSensor();
        }
        
        return theInstance;
    }
 
    /**
     * This method takes the data of a recorded event and generates
     * and sends a HackyStat Activity event with the given event data.
     * @param data Additional data
     */
    public void processActivity(String data)
    {
        if (!this.isEnabled ) {
            return;
        }

        String[] args = { "add", "MicroActivity", data};
        
        this.eclipseSensorShell.doCommand("Activity", Arrays.asList(args));
    }
    
    private class ResourceChangeAdapter implements IResourceChangeListener
    {
        
        /**
         * Provides manipulation of IResourceChangeEvent instance due to implement
         * <code>IResourceChangeListener</code>. This method must not be called by client because it
         * is called by platform when resources are changed.
         * 
         * @param event A resource change event to describe changes to resources.
         */
        public void resourceChanged(IResourceChangeEvent event)
        {

            if (event.getType() != IResourceChangeEvent.POST_CHANGE)
                return;

            IResourceDelta $delta = event.getDelta();

            IResourceDeltaVisitor visitor = new IResourceDeltaVisitor() {

                public boolean visit(IResourceDelta delta)
                {

                    int kind = delta.getKind();
                    
                    int flags = delta.getFlags();
                    
                    String name = delta.getResource().getName();

                    switch (kind)
                    {
                    case IResourceDelta.ADDED:

                        processActivity("<?xml version=\"1.0\"?><microActivity><commonData><username>"+getUsername()+"</username><projectname>bar</projectname></commonData><resource><activity>added</activity><resourcename>" + name + "</resourcename><resourcetype>"+ delta.getResource().getType() +"</resourcetype></resource></microActivity>");

                        break;

                    case IResourceDelta.REMOVED:

                        processActivity("<?xml version=\"1.0\"?><microActivity><commonData><username>"+getUsername()+"</username><projectname>bar</projectname></commonData><resource><activity>removed</activity><resourcename>" + name + "</resourcename><resourcetype>"+ delta.getResource().getType() +"</resourcetype></resource></microActivity>");

                        break;

                    case IResourceDelta.CHANGED:

                       

                        if (name != null)

                            processActivity("<?xml version=\"1.0\"?><microActivity><commonData><username>"+getUsername()+"</username><projectname>bar</projectname></commonData><resource><activity>changed</activity><resourcename>" + name + "</resourcename><resourcetype>"+ delta.getResource().getType() +"</resourcetype></resource></microActivity>");

                        switch (flags)
                        {
                        case IResourceDelta.OPEN:

                          if(delta.getResource().getType() == IResource.PROJECT)
                          {
                              processActivity("<?xml version=\"1.0\"?><microActivity><commonData><username>"+getUsername()+"</username><projectname>bar</projectname></commonData><resource><activity>opened or closed</activity><resourcename>" + name + "</resourcename><resourcetype>"+ delta.getResource().getType() +"</resourcetype></resource></microActivity>");
                                                            
                              break;

                          }
                            

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
                            
                            break;

                        default:
                        
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
    
}

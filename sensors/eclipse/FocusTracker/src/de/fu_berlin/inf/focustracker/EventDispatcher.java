package de.fu_berlin.inf.focustracker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.drools.IntegrationException;
import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.internal.ui.javaeditor.ClassFileEditor;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jdt.internal.ui.packageview.PackageExplorerPart;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.contentoutline.ContentOutline;
import org.xml.sax.SAXException;

import de.fu_berlin.inf.focustracker.interaction.Interaction;
import de.fu_berlin.inf.focustracker.interaction.InteractionListener;
import de.fu_berlin.inf.focustracker.jobs.ActivityMonitorJob;
import de.fu_berlin.inf.focustracker.jobs.ECGExportJob;
import de.fu_berlin.inf.focustracker.jobs.InteractionGCJob;
import de.fu_berlin.inf.focustracker.monitor.JavaEditorMonitor;
import de.fu_berlin.inf.focustracker.monitor.ProjectLifecycleMonitor;
import de.fu_berlin.inf.focustracker.monitor.OutlineMonitor;
import de.fu_berlin.inf.focustracker.monitor.PackageExplorerExpansionMonitor;
import de.fu_berlin.inf.focustracker.monitor.PartMonitor;
import de.fu_berlin.inf.focustracker.monitor.SelectionMonitor;
import de.fu_berlin.inf.focustracker.monitor.SystemActivityMonitor;
import de.fu_berlin.inf.focustracker.monitor.WindowStateMonitor;
import de.fu_berlin.inf.focustracker.rating.Rating;
import de.fu_berlin.inf.focustracker.repository.InteractionRepository;


public class EventDispatcher {

	private static EventDispatcher instance;
	private static List<InteractionListener> listeners = new ArrayList<InteractionListener>();
	private static boolean started;
	
	private InteractionRepository interactionRepository;
	private Rating rating;
	private PartMonitor partMonitor;
	private SystemActivityMonitor systemMonitor;
	private ActivityMonitorJob activityMonitorJob;
	private ECGExportJob ecgExportJob;
	private WindowStateMonitor windowStateMonitor;
	private InteractionGCJob interactionGCJob;

	// debug variable
	private int numberOfRetries = 5;
	private ProjectLifecycleMonitor lifecycleMonitor;

	
	private EventDispatcher() {
		try {
			init();
			started = true;
		} catch (IntegrationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void init() throws IntegrationException, SAXException, IOException {
		
		boolean partMonitorAdded = false;
		
		interactionRepository = InteractionRepository.getInstance();
//		rating = new Rating();
		
		// monitors : 
		partMonitor = new PartMonitor();
		partMonitor.registerMonitor(JavaEditorMonitor.class, CompilationUnitEditor.class);
		partMonitor.registerMonitor(JavaEditorMonitor.class, ClassFileEditor.class);
		partMonitor.registerMonitor(OutlineMonitor.class, ContentOutline.class);
		partMonitor.registerMonitor(PackageExplorerExpansionMonitor.class, PackageExplorerPart.class);
		
		
		// browse through all pages of all windows (de- and activated)
		IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
		for (IWorkbenchWindow window : windows) {
			for (IWorkbenchPage page : window.getPages()) {
				page.addPartListener(partMonitor);
				partMonitorAdded = true;
				System.err.println("partmonitor added to " + page );
			}
		}
		
		// this is ugly, but sometimes the listeners won't be added
		if(!partMonitorAdded) {
			System.err.println("partmonitor not yet added, trying again");
			numberOfRetries--;
			if(numberOfRetries == 0) {
				String message = "Unable to register listeners! Please restart the plugin.";
				ErrorDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), 
						"Unable to start.",
						message,
						new Status(IStatus.ERROR, FocusTrackerPlugin.ID, 0, message, new Exception()));
				try {
					FocusTrackerPlugin.getDefault().stop(null);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				init();
			}
		}
		
		ISelectionService service = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService();
		
//		windowStateMonitor = new WindowStateMonitor();
//		PlatformUI.getWorkbench().addWindowListener(windowStateMonitor);
		
		lifecycleMonitor = new ProjectLifecycleMonitor();
		((Workspace)ResourcesPlugin.getWorkspace()).addLifecycleListener(lifecycleMonitor);
		
		systemMonitor = new SystemActivityMonitor();
		Display.getDefault().addFilter(SWT.MouseMove, systemMonitor);
		Display.getDefault().addFilter(SWT.KeyDown, systemMonitor);
		
		
		// jobs :
		activityMonitorJob = new ActivityMonitorJob();
		ecgExportJob = new ECGExportJob();
		
		activityMonitorJob.schedule(ActivityMonitorJob.getInactivityDetectionTimeout());
		ecgExportJob.schedule(ECGExportJob.getExportInterval());
		
		interactionGCJob = new InteractionGCJob();
		interactionGCJob.schedule(InteractionGCJob.DELAY);
		
		service.addPostSelectionListener(new SelectionMonitor());
	}
	
	public static EventDispatcher getInstance() {
		if(instance == null) {
			instance = new EventDispatcher();
		}
		return instance;
	}
	
	public void addListener(InteractionListener aInteractionListener) {
		listeners.add(aInteractionListener);
		System.err.println("adding interaction listener: " + aInteractionListener);
	}

	public void removeListener(InteractionListener aInteractionListener) {
		listeners.remove(aInteractionListener);
	}

	public void notifyInteractionObserved(Interaction aInteraction) {
//		if(aInteraction instanceof JavaInteraction) {
//			interactionRepository.add((JavaInteraction)aInteraction);
//		}
//		notifyInteractionListeners(aInteraction);
		List<Interaction> interList = new ArrayList<Interaction>();
		interList.add(aInteraction);
		notifyInteractionObserved(interList);
	}

	public void notifyInteractionObserved(List<? extends Interaction> aInteractions) {
		for (Interaction interaction : aInteractions) {
//			if(interaction instanceof JavaInteraction) {
//				interactionRepository.add((JavaInteraction)interaction);
//			}
			interactionRepository.add(interaction);
		}
		notifyInteractionListeners(aInteractions);
	}
	
	
	private void notifyInteractionListeners(List<? extends Interaction> aInteractions) {
		for (InteractionListener interactionListener : listeners) {
			interactionListener.notifyInteractionObserved(aInteractions);
		}
	}

	public Rating getRating() {
		if(rating == null) {
			try {
				rating = new Rating();
			} catch (IntegrationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return rating;
	}

	public PartMonitor getPartMonitor() {
		return partMonitor;
	}

	public SystemActivityMonitor getSystemMonitor() {
		return systemMonitor;
	}
	
	
	public void shutdown() {
		// TODO deregister all jobs and listeners
		activityMonitorJob.cancel();
		ecgExportJob.cancel();
		interactionGCJob.cancel();
		lifecycleMonitor = null;
	}

	public static boolean isStarted() {
		return started;
	}
	
}

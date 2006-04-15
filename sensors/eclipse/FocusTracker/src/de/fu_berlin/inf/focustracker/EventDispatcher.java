package de.fu_berlin.inf.focustracker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.drools.IntegrationException;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jdt.internal.ui.packageview.PackageExplorerPart;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.contentoutline.ContentOutline;
import org.xml.sax.SAXException;

import de.fu_berlin.inf.focustracker.interaction.Interaction;
import de.fu_berlin.inf.focustracker.interaction.InteractionListener;
import de.fu_berlin.inf.focustracker.interaction.JavaInteraction;
import de.fu_berlin.inf.focustracker.monitor.JavaEditorMonitor;
import de.fu_berlin.inf.focustracker.monitor.OutlineMonitor;
import de.fu_berlin.inf.focustracker.monitor.PackageExplorerExpansionMonitor;
import de.fu_berlin.inf.focustracker.monitor.PartMonitor;
import de.fu_berlin.inf.focustracker.monitor.SelectionMonitor;
import de.fu_berlin.inf.focustracker.monitor.WindowMonitor;
import de.fu_berlin.inf.focustracker.rating.Rating;
import de.fu_berlin.inf.focustracker.repository.InteractionRepository;


public class EventDispatcher {

	private static EventDispatcher instance;
	private List<InteractionListener> listeners = new ArrayList<InteractionListener>();
	
	private InteractionRepository interactionRepository;
	private Rating rating;
	private PartMonitor partMonitor;
	
	private EventDispatcher() {
		try {
			init();
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
		
		interactionRepository = InteractionRepository.getInstance();
		rating = new Rating();
		ISelectionService service = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService();
		
		// monitors : 
		partMonitor = new PartMonitor();
		partMonitor.registerMonitor(JavaEditorMonitor.class, CompilationUnitEditor.class);
		partMonitor.registerMonitor(OutlineMonitor.class, ContentOutline.class);
		partMonitor.registerMonitor(PackageExplorerExpansionMonitor.class, PackageExplorerPart.class);
		
		
		// browse through all pages of all windows (de- and activated)
		IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
		for (IWorkbenchWindow window : windows) {
			for (IWorkbenchPage page : window.getPages()) {
				page.addPartListener(partMonitor);
				System.err.println("partmonitor added to " + page );
			}
		}
		
		PlatformUI.getWorkbench().addWindowListener(new WindowMonitor());
		
//		PlatformUI.getWorkbench().getActiveWorkbenchWindow(). getShell().addMouseMoveListener(this);
//		PlatformUI.getWorkbench().getDisplay().addListener(SWT.MouseMove, new TypedListener(this));

		// jobs :
//		new ActivityMonitorJob().schedule();
		
		
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
	}

	public void removeListener(InteractionListener aInteractionListener) {
		listeners.remove(aInteractionListener);
	}

	public void notifyInteractionObserved(Interaction aInteraction) {
//		System.err.println(aInteraction.toString());
		if(aInteraction instanceof JavaInteraction) {
			interactionRepository.add((JavaInteraction)aInteraction);
		}
		notifyInteractionListeners(aInteraction);
		
	}
	
	private void notifyInteractionListeners(Interaction aInteraction) {
		for (InteractionListener interactionListener : listeners) {
			interactionListener.notifyInteractionObserved(aInteraction);
		}
	}

	public Rating getRating() {
		return rating;
	}

	public PartMonitor getPartMonitor() {
		return partMonitor;
	}
	
}

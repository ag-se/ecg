package de.fu_berlin.inf.focustracker.monitor;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;


public class PartMonitor implements IPartListener {

	private Map<Class<? extends IWorkbenchPart>, Class<? extends IFocusTrackerMonitor>> registeredMonitorClasses = new HashMap<Class<? extends IWorkbenchPart>, Class<? extends IFocusTrackerMonitor>>(); 
	private Map<IWorkbenchPart, IFocusTrackerMonitor> monitorInstances = new HashMap<IWorkbenchPart, IFocusTrackerMonitor>(); 

	
	public void registerMonitor(Class<? extends IFocusTrackerMonitor> aMonitorClass, Class<? extends IWorkbenchPart> aPart) {
		
		if(registeredMonitorClasses.containsKey(aPart)) {
			throw new IllegalArgumentException("This part is already registered!");
		}
		registeredMonitorClasses.put(aPart, aMonitorClass);
		
		// check if already some intstances of this part are opened!
//		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IWorkbenchPage[] pages = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPages();
//		List<IWorkbenchPart> alreadyOpenedParts = new ArrayList<IWorkbenchPart>();
		for (IWorkbenchPage page : pages) {
			for (IEditorReference ref : page.getEditorReferences()) {
				IWorkbenchPart part = ref.getEditor(false);
				createMonitorInstance(part);
			}
			for (IViewReference ref : page.getViewReferences()) {
				IWorkbenchPart part = ref.getView(false);
				createMonitorInstance(part);
			}
		}
	}
	
	public void partActivated(IWorkbenchPart aPart) {
		IFocusTrackerMonitor monitor = monitorInstances.get(aPart);
		if(monitorInstances.get(aPart) != null) {
			monitor.partActivated();
		}
	}

	public void partBroughtToTop(IWorkbenchPart aPart) {
	}

	public void partDeactivated(IWorkbenchPart aPart) {
		IFocusTrackerMonitor monitor = monitorInstances.get(aPart);
		if(monitorInstances.get(aPart) != null) {
			monitor.partDeactivated();
		}
	}
	
	public void partClosed(IWorkbenchPart aPart) {
		IFocusTrackerMonitor monitor = monitorInstances.get(aPart);
		if(monitorInstances.get(aPart) == null) {
			return;
		}
		monitor.partClosed();
		monitor.deregisterFromPart();
		monitorInstances.remove(aPart);
		System.err.println("partmonitor removed from : " + aPart);
	}

	public void partOpened(IWorkbenchPart aPart) {
//		System.err.println("partOpened: " + aPart);
		createMonitorInstance(aPart);
	}

	private void createMonitorInstance(IWorkbenchPart aPart) {
		if(aPart == null || registeredMonitorClasses.get(aPart.getClass()) == null) {
//			System.err.println("no listener registered for : " + aPart);
			return;
		}
		
		if(monitorInstances.get(aPart) != null) {
			System.err.println("this instance has already an issued monitor : " + aPart);
			return;
		}
		try {
			IFocusTrackerMonitor monitor = registeredMonitorClasses.get(aPart.getClass()).newInstance();
			monitorInstances.put(aPart, monitor);
			monitor.registerPart(aPart);
			System.err.println("partmonitor instance created for : " + aPart);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	public IFocusTrackerMonitor getMonitorInstance(IWorkbenchPart aPart) {
		return monitorInstances.get(aPart);
	}
	
}

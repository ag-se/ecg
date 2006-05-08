package de.fu_berlin.inf.focustracker.monitor;

import org.eclipse.core.internal.events.ILifecycleListener;
import org.eclipse.core.internal.events.LifecycleEvent;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaCore;

import de.fu_berlin.inf.focustracker.EventDispatcher;
import de.fu_berlin.inf.focustracker.interaction.Action;
import de.fu_berlin.inf.focustracker.interaction.JavaInteraction;
import de.fu_berlin.inf.focustracker.interaction.Origin;

public class ProjectLifecycleMonitor implements ILifecycleListener{

	public void handleEvent(LifecycleEvent aEvent) throws CoreException {
		
		IProject project;
		switch (aEvent.kind) {
		case LifecycleEvent.PRE_PROJECT_OPEN:
			// project opened
			project = (IProject)aEvent.resource;
			System.err.println("project about to be opened: " + project.getName());
			EventDispatcher.getInstance().notifyInteractionObserved(
					new JavaInteraction(Action.PROJECT_OPENED, JavaCore.create(project), 1d, Origin.WORKSPACE)
					);
			break;

		case LifecycleEvent.PRE_PROJECT_CLOSE:
		case LifecycleEvent.PRE_PROJECT_DELETE:
			// project closed or deleted
			project = (IProject)aEvent.resource;
			System.err.println("project about to be closed/deleted: " + project.getName());
			EventDispatcher.getInstance().notifyInteractionObserved(
					new JavaInteraction(Action.PROJECT_CLOSED, JavaCore.create(project), 0d, Origin.WORKSPACE)
					);
			break;
			
		default:
			break;
		}
	}

}

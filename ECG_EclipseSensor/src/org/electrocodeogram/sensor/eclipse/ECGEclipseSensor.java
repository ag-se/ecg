package org.electrocodeogram.sensor.eclipse;

import java.io.File;
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
import org.electrocodeogram.event.WellFormedEventPacket;
import org.electrocodeogram.logging.LogHelper;
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

	static Logger _logger = LogHelper.createLogger(ECGEclipseSensor.class.getName());

	/*
	 * This constant specifies how long to wait after user input before a
	 * Codechange event is send.
	 */
	private static final int CODECHANGE_INTERVALL = 2000;

	String _username;

	String _projectname;

	ITextEditor _activeTextEditor;

	String _activeWindowName;

	private static ECGEclipseSensor _theInstance;

	private EclipseSensor _hackyEclipse;

	private EclipseSensorShell _eclipseSensorShell;

	/*
	 * This is the private contstructor creating the ECG EclipseSensor.
	 */
	@SuppressWarnings( { "deprecation", "deprecation" })
	private ECGEclipseSensor()
	{
		_logger.entering(this.getClass().getName(), "ECGEclipseSensor");

		/*
		 * Create and get the original singleton HackyStat Eclipse sensor
		 * instance.
		 */
		this._hackyEclipse = EclipseSensor.getInstance();

		_logger.log(Level.INFO, "HackyStat Eclipse sensor created.");

		this._eclipseSensorShell = this._hackyEclipse.getEclipseSensorShell();

		_logger.log(Level.INFO, "Got HackyStat EclipseSensorShell.");

		/*
		 * The next lines are needed for the InlineServer mode. In that case the
		 * ECG SensorShell needs to now where the ECG Lab application is stored
		 * locally.
		 * 
		 * The ECG Lab is stored in a PlugIns subdirectory called "ecg" per
		 * default. So we get the PlugIn directory name itself and are adding
		 * the "ecg" subdirectory.
		 */
		String id = EclipseSensorPlugin.getInstance().getDescriptor().getUniqueIdentifier();

		String version = EclipseSensorPlugin.getInstance().getDescriptor().getVersionIdentifier().toString();

		String[] path = { "plugins" + File.separator + id + "_" + version + File.separator + "ecg" };

		List list = Arrays.asList(path);

		/*
		 * The only way to communicate with the ECG SensorShell is by using the
		 * HackyStat's EclipseSensorShell, since we are not having a reference
		 * to the SensorShell itself.
		 */
		this._eclipseSensorShell.doCommand(SensorShell.ECG_LAB_PATH, list);

		_logger.log(Level.INFO, "Send ECG Lab path to ECG SensorShell");

		// Try to get the username from the operating system environment
		this._username = System.getenv("username");

		if (this._username == null || this._username.equals(""))
		{
			this._username = "n.a.";
		}

		_logger.log(Level.INFO, "Username is set to" + this._username);

		// add the WindowListener for listening on
		// window events.
		IWorkbench workbench = PlatformUI.getWorkbench();

		workbench.addWindowListener(new WindowListenerAdapter());

		_logger.log(Level.INFO, "Added WindowListener.");

		// add the PartListener for listening on
		// part events.
		IWorkbenchWindow[] activeWindows = workbench.getWorkbenchWindows();

		IWorkbenchPage activePage = null;

		for (int i = 0; i < activeWindows.length; i++)
		{
			activePage = activeWindows[i].getActivePage();

			activePage.addPartListener(new PartListenerAdapter());
		}

		_logger.log(Level.INFO, "Added PartListener.");

		// add the DocumentListener for listening on
		// document events.
		IEditorPart part = activePage.getActiveEditor();

		if (part instanceof ITextEditor)
		{
			processActivity("msdt.editor.xsd","<?xml version=\"1.0\"?><microActivity><commonData><username>" + ECGEclipseSensor.this._username + "</username><projectname>" + ECGEclipseSensor.this._projectname + "</projectname></commonData><editor><activity>opened</activity><editorname>" + part.getTitle() + "</editorname></editor></microActivity>");

			this._activeTextEditor = (ITextEditor) part;

			IDocumentProvider provider = this._activeTextEditor.getDocumentProvider();

			IDocument document = provider.getDocument(part.getEditorInput());

			document.addDocumentListener(new DocumentListenerAdapter());
		}

		_logger.log(Level.INFO, "Added DocumentListener.");

		// add the ResourceChangeListener to the workspace for listening on
		// resource events.
		IWorkspace workspace = ResourcesPlugin.getWorkspace();

		workspace.addResourceChangeListener(new ResourceChangeAdapter(), IResourceChangeEvent.POST_CHANGE);

		// add the DebugEventSetListener to listen to run and debug events.
		DebugPlugin dp = DebugPlugin.getDefault();

		dp.addDebugEventListener(new DebugEventSetAdapter());

		_logger.log(Level.INFO, "Added DebugEventSetListener.");

		_logger.exiting(this.getClass().getName(), "ECGEclipseSensor");
	}

	/**
	 * This returns the current username.
	 * 
	 * @return The current username
	 */
	protected String getUsername()
	{
		_logger.entering(this.getClass().getName(), "getUsername");

		_logger.exiting(this.getClass().getName(), "getUsername");

		return this._username;

	}

	/**
	 * This returns the current projectname.
	 * 
	 * @return The current projectname
	 */
	protected String getProjectname()
	{
		_logger.entering(this.getClass().getName(), "getProjectname");

		_logger.exiting(this.getClass().getName(), "getProjectname");

		return this._projectname;
	}

	/**
	 * This method returns the singleton instance of the ECG EclipseSensor.
	 * 
	 * @return The sengleton instance of the ECG EclipseSensor
	 */
	public static ECGEclipseSensor getInstance()
	{
		_logger.entering(ECGEclipseSensor.class.getName(), "getInstance");

		if (_theInstance == null)
		{
			_theInstance = new ECGEclipseSensor();
		}

		_logger.exiting(ECGEclipseSensor.class.getName(), "getInstance");

		return _theInstance;
	}

	/**
	 * This method takes the data of a recorded ECG MicroActivity event and
	 * generates a HackyStat Activity event with the given event data from it.
	 * The HackyStat command name property is set to the value "add" and the
	 * HackyStat activtiy-type property is set to the value "MicroActivity". At
	 * last the event is passed to the ECG SensorShell for further
	 * transportation to the ECG Lab.
	 * 
	 * @param data
	 *            This is the actual MicroActivity encoded in an XML String that
	 *            is conforming to one of the defined XML Schemas.s
	 */
	public void processActivity(String msdt,String data)
	{
		_logger.entering(this.getClass().getName(), "processActivity");

		if (data == null)
		{
			_logger.log(Level.FINEST, "data is null");

			_logger.exiting(this.getClass().getName(), "processActivity");

			return;
		}

		String[] args = { WellFormedEventPacket.HACKYSTAT_ADD_COMMAND, WellFormedEventPacket.MICRO_ACTIVITY_PREFIX+msdt, data };

		// if Eclipse is shutting down the EclipseSensorShell might be gone
		// allready
		if (this._eclipseSensorShell != null)
		{
			this._eclipseSensorShell.doCommand(WellFormedEventPacket.HACKYSTAT_ACTIVITY_STRING, Arrays.asList(args));

			_logger.log(Level.INFO,"Passed to EclipseSensorShell");
		}

		_logger.exiting(this.getClass().getName(), "processActivity");
	}

	/**
	 * This class is the WindowListenerAdapter which is registered for listening
	 * to Window events.
	 * 
	 */
	private class WindowListenerAdapter implements IWindowListener
	{

		/**
		 * @see org.eclipse.ui.IWindowListener#windowActivated(org.eclipse.ui.IWorkbenchWindow)
		 */
		public void windowActivated(IWorkbenchWindow window)
		{

			_logger.entering(this.getClass().getName(), "windowActivated");

			if (window == null)
			{
				_logger.log(Level.FINEST, "window is null");

				_logger.exiting(this.getClass().getName(), "windowActivated");

				return;

			}

			if (window.getActivePage() != null)
			{
				ECGEclipseSensor.this._activeWindowName = window.getActivePage().getLabel();

				processActivity("msdt.window.xsd","<?xml version=\"1.0\"?><microActivity><commonData><username>" + ECGEclipseSensor.this._username + "</username><projectname>" + ECGEclipseSensor.this._projectname + "</projectname></commonData><window><activity>activated</activity><windowname>" + ECGEclipseSensor.this._activeWindowName + "</windowname></window></microActivity>");
			}

			_logger.exiting(this.getClass().getName(), "windowActivated");
		}

		/**
		 * @see org.eclipse.ui.IWindowListener#windowClosed(org.eclipse.ui.IWorkbenchWindow)
		 */
		public void windowClosed(@SuppressWarnings("unused")
		IWorkbenchWindow window)
		{
			_logger.entering(this.getClass().getName(), "windowClosed");

			if (window == null)
			{
				_logger.log(Level.FINEST, "window is null");

				_logger.exiting(this.getClass().getName(), "windowClosed");

				return;

			}

			processActivity("msdt.window.xsd","<?xml version=\"1.0\"?><microActivity><commonData><username>" + ECGEclipseSensor.this._username + "</username><projectname>" + ECGEclipseSensor.this._projectname + "</projectname></commonData><window><activity>closed</activity><windowname>" + ECGEclipseSensor.this._activeWindowName + "</windowname></window></microActivity>");

			_logger.exiting(this.getClass().getName(), "windowClosed");
		}

		/**
		 * @see org.eclipse.ui.IWindowListener#windowDeactivated(org.eclipse.ui.IWorkbenchWindow)
		 */
		public void windowDeactivated(@SuppressWarnings("unused")
		IWorkbenchWindow window)
		{

			_logger.entering(this.getClass().getName(), "windowDeactivated");

			if (window == null)
			{
				_logger.log(Level.FINEST, "window is null");

				_logger.exiting(this.getClass().getName(), "windowDeactivated");

				return;

			}

			processActivity("msdt.window.xsd","<?xml version=\"1.0\"?><microActivity><commonData><username>" + ECGEclipseSensor.this._username + "</username><projectname>" + ECGEclipseSensor.this._projectname + "</projectname></commonData><window><activity>deactivated</activity><windowname>" + ECGEclipseSensor.this._activeWindowName + "</windowname></window></microActivity>");

			_logger.exiting(this.getClass().getName(), "windowDeactivated");
		}

		/**
		 * @see org.eclipse.ui.IWindowListener#windowOpened(org.eclipse.ui.IWorkbenchWindow)
		 */
		public void windowOpened(IWorkbenchWindow window)
		{

			_logger.entering(this.getClass().getName(), "windowOpened");

			if (window == null)
			{
				_logger.log(Level.FINEST, "window is null");

				_logger.exiting(this.getClass().getName(), "windowOpened");

				return;

			}

			processActivity("msdt.window.xsd","<?xml version=\"1.0\"?><microActivity><commonData><username>" + ECGEclipseSensor.this._username + "</username><projectname>" + ECGEclipseSensor.this._projectname + "</projectname></commonData><window><activity>deactivated</activity><windowname>" + window.getActivePage().getLabel() + "</windowname></window></microActivity>");

			_logger.exiting(this.getClass().getName(), "windowOpened");
		}
	}

	/**
	 * This class is the RecourceChangeAdapter which is registered for listening
	 * to ResourceChange events.
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

			_logger.entering(this.getClass().getName(), "resourceChanged");

			if (event == null)
			{
				_logger.log(Level.FINEST, "event is null");

				_logger.exiting(this.getClass().getName(), "resourceChanged");

				return;

			}

			if (event.getType() != IResourceChangeEvent.POST_CHANGE)
			{
				_logger.log(Level.FINEST, "event is of type POST_CHANGE");

				_logger.exiting(this.getClass().getName(), "resourceChanged");

				return;
			}

			IResourceDelta resourceDelta = event.getDelta();

			IResourceDeltaVisitor deltaVisitor = new IResourceDeltaVisitor()
			{

				public boolean visit(IResourceDelta delta)
				{
					_logger.entering(this.getClass().getName(), "visit");

					if (delta == null)
					{

						_logger.log(Level.FINEST, "delta is null");

						_logger.exiting(this.getClass().getName(), "visit");

						return false;
					}

					// get the kind of the ResourceChangedEvent
					int kind = delta.getKind();

					// get the resource
					IResource resource = delta.getResource();

					String resourceType = null;

					// get the resourceType String
					switch (resource.getType())
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

						case IResource.FILE:

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

							processActivity("msdt.resource.xsd","<?xml version=\"1.0\"?><microActivity><commonData><username>" + ECGEclipseSensor.this._username + "</username><projectname>" + ECGEclipseSensor.this._projectname + "</projectname></commonData><resource><activity>added</activity><resourcename>" + resource.getName() + "</resourcename><resourcetype>" + resourceType + "</resourcetype></resource></microActivity>");

							break;
						// a resource has been removed
						case IResourceDelta.REMOVED:

							processActivity("msdt.resource.xsd","<?xml version=\"1.0\"?><microActivity><commonData><username>" + ECGEclipseSensor.this._username + "</username><projectname>" + ECGEclipseSensor.this._projectname + "</projectname></commonData><resource><activity>removed</activity><resourcename>" + resource.getName() + "</resourcename><resourcetype>" + resourceType + "</resourcetype></resource></microActivity>");

							break;
						// a resource has been changed
						case IResourceDelta.CHANGED:

							// if its a project change, set the name of the
							// project to be the name used.
							if (resource instanceof IProject)
							{
								ECGEclipseSensor.this._projectname = resource.getName();
							}
							else
							{
								processActivity("msdt.resource.xsd","<?xml version=\"1.0\"?><microActivity><commonData><username>" + ECGEclipseSensor.this._username + "</username><projectname>" + ECGEclipseSensor.this._projectname + "</projectname></commonData><resource><activity>changed</activity><resourcename>" + resource.getName() + "</resourcename><resourcetype>" + resourceType + "</resourcetype></resource></microActivity>");
							}
							break;
					}

					_logger.exiting(this.getClass().getName(), "visit");

					return true;
				}

			};

			try
			{
				resourceDelta.accept(deltaVisitor);
			}
			catch (CoreException e)
			{
				_logger.log(Level.SEVERE, "An Eclipse internal Exception occured during resourceEvent analysis.");

				_logger.log(Level.FINEST, e.getMessage());
			}

			_logger.exiting(this.getClass().getName(), "resourceChanged");

		}
	}

	/**
	 * This class is the DebugEventSetAdapter which is registered for listening
	 * to DebugEventSet events.
	 * 
	 */
	private class DebugEventSetAdapter implements IDebugEventSetListener
	{

		private ILaunch _currentLaunch = null;

		/**
		 * @see org.eclipse.debug.core.IDebugEventSetListener#handleDebugEvents(org.eclipse.debug.core.DebugEvent[])
		 */
		public void handleDebugEvents(DebugEvent[] events)
		{

			_logger.entering(this.getClass().getName(), "handleDebugEvents");

			if (events == null || events.length == 0)
			{
				_logger.log(Level.FINEST, "events is null or empty");

				_logger.exiting(this.getClass().getName(), "handleDebugEvents");

				return;
			}

			Object source = events[0].getSource();

			if (source instanceof RuntimeProcess)
			{
				RuntimeProcess rp = (RuntimeProcess) source;

				ILaunch launch = rp.getLaunch();

				if (this._currentLaunch == null)
				{
					this._currentLaunch = launch;

					analyseLaunch(launch);
				}
				else if (!this._currentLaunch.equals(launch))
				{
					this._currentLaunch = launch;

					analyseLaunch(launch);
				}
			}

			_logger.exiting(this.getClass().getName(), "handleDebugEvents");
		}

		private void analyseLaunch(ILaunch launch)
		{
			_logger.entering(this.getClass().getName(), "analyseLaunch");

			if (launch == null)
			{
				_logger.log(Level.FINEST, "lauch is null");

				_logger.exiting(this.getClass().getName(), "analyseLaunch");

				return;
			}

			if (launch.getLaunchMode().equals("run"))
			{

				processActivity("msdt.rundebug.xsd","<?xml version=\"1.0\"?><microActivity><commonData><username>" + ECGEclipseSensor.this._username + "</username><projectname>" + ECGEclipseSensor.this._projectname + "</projectname></commonData><run debug=\"false\"></run></microActivity>");
			}
			else
			{
				processActivity("msdt.rundebug.xsd","<?xml version=\"1.0\"?><microActivity><commonData><username>" + ECGEclipseSensor.this._username + "</username><projectname>" + ECGEclipseSensor.this._projectname + "</projectname></commonData><run debug=\"true\"></run></microActivity>");
			}

			_logger.exiting(this.getClass().getName(), "analyseLaunch");
		}
	}

	/**
	 * This class is the PartListenerAdapter which is registered for listening
	 * to Part events.
	 * 
	 */
	private class PartListenerAdapter implements IPartListener
	{

		/**
		 * @see org.eclipse.ui.IPartListener#partActivated(org.eclipse.ui.IWorkbenchPart)
		 */
		public void partActivated(IWorkbenchPart part)
		{

			_logger.entering(this.getClass().getName(), "partActivated");

			if (part == null)
			{
				_logger.log(Level.FINEST, "part is null");

				_logger.exiting(this.getClass().getName(), "partActivated");

				return;
			}

			if (part instanceof ITextEditor)
			{

				ECGEclipseSensor.this._activeTextEditor = (ITextEditor) part;

				IDocumentProvider provider = ECGEclipseSensor.this._activeTextEditor.getDocumentProvider();

				IDocument document = provider.getDocument(ECGEclipseSensor.this._activeTextEditor.getEditorInput());

				document.addDocumentListener(new DocumentListenerAdapter());

				processActivity("msdt.editor.xsd","<?xml version=\"1.0\"?><microActivity><commonData><username>" + ECGEclipseSensor.this._username + "</username><projectname>" + ECGEclipseSensor.this._projectname + "</projectname></commonData><editor><activity>activated</activity><editorname>" + part.getTitle() + "</editorname></editor></microActivity>");

			}
			else
			{
				processActivity("msdt.part.xsd","<?xml version=\"1.0\"?><microActivity><commonData><username>" + ECGEclipseSensor.this._username + "</username><projectname>" + ECGEclipseSensor.this._projectname + "</projectname></commonData><part><activity>activated</activity><partname>" + part.getTitle() + "</partname></part></microActivity>");
			}

			_logger.exiting(this.getClass().getName(), "partActivated");
		}

		/**
		 * @see org.eclipse.ui.IPartListener#partClosed(org.eclipse.ui.IWorkbenchPart)
		 */
		public void partClosed(IWorkbenchPart part)
		{
			_logger.entering(this.getClass().getName(), "partClosed");

			if (part == null)
			{
				_logger.log(Level.FINEST, "part is null");

				_logger.exiting(this.getClass().getName(), "partClosed");

				return;
			}

			if (part instanceof ITextEditor)
			{

				processActivity("msdt.editor.xsd","<?xml version=\"1.0\"?><microActivity><commonData><username>" + ECGEclipseSensor.this._username + "</username><projectname>" + ECGEclipseSensor.this._projectname + "</projectname></commonData><editor><activity>closed</activity><editorname>" + part.getTitle() + "</editorname></editor></microActivity>");

			}
			else
			{
				processActivity("msdt.part.xsd","<?xml version=\"1.0\"?><microActivity><commonData><username>" + ECGEclipseSensor.this._username + "</username><projectname>" + ECGEclipseSensor.this._projectname + "</projectname></commonData><part><activity>closed</activity><partname>" + part.getTitle() + "</partname></part></microActivity>");
			}

			_logger.exiting(this.getClass().getName(), "partClosed");
		}

		/**
		 * @see org.eclipse.ui.IPartListener#partDeactivated(org.eclipse.ui.IWorkbenchPart)
		 */
		public void partDeactivated(IWorkbenchPart part)
		{
			_logger.entering(this.getClass().getName(), "partDeactivated");

			if (part == null)
			{
				_logger.log(Level.FINEST, "part is null");

				_logger.exiting(this.getClass().getName(), "partDeactivated");

				return;
			}

			if (part instanceof ITextEditor)
			{

				processActivity("msdt.editor.xsd","<?xml version=\"1.0\"?><microActivity><commonData><username>" + ECGEclipseSensor.this._username + "</username><projectname>" + ECGEclipseSensor.this._projectname + "</projectname></commonData><editor><activity>deactivated</activity><editorname>" + part.getTitle() + "</editorname></editor></microActivity>");

			}
			else
			{
				processActivity("msdt.part.xsd","<?xml version=\"1.0\"?><microActivity><commonData><username>" + ECGEclipseSensor.this._username + "</username><projectname>" + ECGEclipseSensor.this._projectname + "</projectname></commonData><part><activity>deactivated</activity><partname>" + part.getTitle() + "</partname></part></microActivity>");
			}

			_logger.exiting(this.getClass().getName(), "partDeactivated");

		}

		/**
		 * @see org.eclipse.ui.IPartListener#partOpened(org.eclipse.ui.IWorkbenchPart)
		 */
		public void partOpened(IWorkbenchPart part)
		{
			_logger.entering(this.getClass().getName(), "partOpened");

			if (part == null)
			{
				_logger.log(Level.FINEST, "part is null");

				_logger.exiting(this.getClass().getName(), "partOpened");

				return;
			}

			if (part instanceof ITextEditor)
			{

				processActivity("msdt.editor.xsd","<?xml version=\"1.0\"?><microActivity><commonData><username>" + ECGEclipseSensor.this._username + "</username><projectname>" + ECGEclipseSensor.this._projectname + "</projectname></commonData><editor><activity>opened</activity><editorname>" + part.getTitle() + "</editorname></editor></microActivity>");

			}
			else
			{
				processActivity("msdt.part.xsd","<?xml version=\"1.0\"?><microActivity><commonData><username>" + ECGEclipseSensor.this._username + "</username><projectname>" + ECGEclipseSensor.this._projectname + "</projectname></commonData><part><activity>opened</activity><partname>" + part.getTitle() + "</partname></part></microActivity>");
			}

			_logger.exiting(this.getClass().getName(), "partOpened");
		}

		/**
		 * @see org.eclipse.ui.IPartListener#partBroughtToTop(org.eclipse.ui.IWorkbenchPart)
		 */
		public void partBroughtToTop(@SuppressWarnings("unused")
		IWorkbenchPart part)
		{
			_logger.entering(this.getClass().getName(), "partBroughtToTop");

			// not implemented

			_logger.exiting(this.getClass().getName(), "partBroughtToTop");
		}
	}

	/**
	 * This class is the DocumentListenerAdapter which is registered for
	 * listening to Document events.
	 * 
	 */
	private class DocumentListenerAdapter implements IDocumentListener
	{

		private Timer _timer = null;

		/**
		 * Creates the DocumentListenerAdapter with its Timer.
		 * 
		 */
		public DocumentListenerAdapter()
		{
			_logger.entering(this.getClass().getName(), "DocumentListenerAdapter");

			this._timer = new Timer();

			_logger.exiting(this.getClass().getName(), "DocumentListenerAdapter");
		}

		/**
		 * @see org.eclipse.jface.text.IDocumentListener#documentAboutToBeChanged(org.eclipse.jface.text.DocumentEvent)
		 */
		public void documentAboutToBeChanged(@SuppressWarnings("unused")
		DocumentEvent event)
		{
			_logger.entering(this.getClass().getName(), "documentAboutToBeChanged");

			// not supported in Eclipse Sensor.

			_logger.exiting(this.getClass().getName(), "documentAboutToBeChanged");
		}

		/**
		 * @see org.eclipse.jface.text.IDocumentListener#documentChanged(org.eclipse.jface.text.DocumentEvent)
		 */
		public void documentChanged(DocumentEvent event)
		{
			_logger.entering(this.getClass().getName(), "documentChanged");

			if (event == null)
			{
				_logger.log(Level.FINEST, "part is null");

				_logger.exiting(this.getClass().getName(), "documentChanged");

				return;
			}

			this._timer.cancel();

			this._timer = new Timer();

			this._timer.schedule(new CodeChangeTimerTask(event.getDocument(),
					ECGEclipseSensor.this._activeTextEditor.getTitle()), ECGEclipseSensor.CODECHANGE_INTERVALL);

			_logger.exiting(this.getClass().getName(), "documentChanged");
		}
	}

	private static class CodeChangeTimerTask extends TimerTask
	{

		private IDocument _document;

		private String _documentName;

		/**
		 * This creates the Task.
		 * 
		 * @param document
		 *            Is the document in which the codechange has occured.
		 * @param documentName
		 *            Is the name of the document the codechange has occured.
		 */
		public CodeChangeTimerTask(IDocument document, String documentName)
		{
			_logger.entering(this.getClass().getName(), "CodeChangeTimerTask");

			this._document = document;

			this._documentName = documentName;

			_logger.exiting(this.getClass().getName(), "CodeChangeTimerTask");
		}

		/**
		 * @see java.util.TimerTask#run()
		 */
		@Override
		public void run()
		{
			_logger.entering(this.getClass().getName(), "run");

			ECGEclipseSensor sensor = ECGEclipseSensor.getInstance();

			sensor.processActivity("msdt.codechange.xsd","<?xml version=\"1.0\"?><microActivity><commonData><username>" + sensor.getUsername() + "</username><projectname>" + sensor.getProjectname() + "</projectname></commonData><codechange><document><![CDATA[" + this._document.get() + "]]></document><documentname>" + this._documentName + "</documentname></codechange></microActivity>");

			_logger.log(Level.INFO,"Codechange...");
			
			while(true)
			{
				try
				{
					Thread.sleep(10);
				}
				catch (InterruptedException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				sensor.processActivity("msdt.codechange.xsd","<?xml version=\"1.0\"?><microActivity><commonData><username>" + sensor.getUsername() + "</username><projectname>" + sensor.getProjectname() + "</projectname></commonData><codechange><document><![CDATA[" + this._document.get() + "]]></document><documentname>" + this._documentName + "</documentname></codechange></microActivity>");
			}
			
			//_logger.exiting(this.getClass().getName(), "run");

		}
	}

}

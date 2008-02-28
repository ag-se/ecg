package org.electrocodeogram.cpc.test.utils;


import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.electrocodeogram.cpc.core.CPCCorePlugin;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider.LockMode;
import org.electrocodeogram.cpc.test.CPCTestPlugin;
import org.electrocodeogram.cpc.ui.views.SimpleCloneView;
import org.electrocodeogram.eclipse.core.logging.JUnitFailureAppender;


public class TestUtils
{
	private static Log log = LogFactory.getLog(TestUtils.class);

	private static IStoreProvider storeProvider = null;
	private static SimpleCloneView simpleCloneView = null;

	public static void defaultSetUp(String projectName, String templateName, boolean initStoreProvider,
			boolean showCloneView) throws CoreException, IOException, PartInitException
	{
		log.trace("defaultSetUp()");

		disableLogging();

		// enable extra junit assertions 
		JUnitFailureAppender.setInUnitTest(true);

		// Initialize the test fixture for each test
		// that is run.
		TestUtils.waitForJobs();

		if (initStoreProvider)
			storeProvider = (IStoreProvider) CPCCorePlugin.getProviderRegistry().lookupProvider(IStoreProvider.class);

		TestUtils.createProject(projectName);

		TestUtils.copyFilesToProject(projectName, templateName);

		if (showCloneView)
			//make sure the clone tracking view is visible
			simpleCloneView = (SimpleCloneView) TestUtils.showView(SimpleCloneView.VIEW_ID);

		//wait a bit
		TestUtils.waitForJobs();

		log.trace("defaultSetUp() - DONE.");

		enableLogging();
	}

	public static void defaultTearDown(String projectName) throws Exception
	{
		disableLogging();

		log.trace("defaultTearDown()");

		//close all editors
		TestEditorUtils.closeAllEditors();

		if (simpleCloneView != null)
		{
			//hide clone tracking view
			TestUtils.hideView(simpleCloneView);

			simpleCloneView = null;
		}

		// wait
		TestUtils.waitForJobs();

		if (storeProvider != null)
		{
			// clear clone data
			try
			{
				storeProvider.acquireWriteLock(LockMode.DEFAULT);
				storeProvider.purgeData();
			}
			finally
			{
				storeProvider.releaseWriteLock();
			}

			storeProvider = null;
		}

		TestUtils.removeProject(projectName);

		// wait
		TestUtils.waitForJobs();

		log.trace("defaultTearDown() - DONE.");

		enableLogging();
	}

	/**
	 * @return NULL unless {@link TestUtils#defaultSetUp(String, String, boolean, boolean)}
	 * 			was used with <em>initStoreProvider</em> set to true.
	 */
	public static IStoreProvider getStoreProvider()
	{
		return storeProvider;
	}

	/**
	 * @return NULL unless {@link TestUtils#defaultSetUp(String, String, boolean, boolean)}
	 * 			was used with <em>showCloneView</em> set to true.
	 */
	public static SimpleCloneView getSimpleCloneView()
	{
		return simpleCloneView;
	}

	/**
	 * Creates a new java project with the given name.
	 * 
	 * @param projectName
	 * @throws CoreException
	 */
	public static void createProject(String projectName) throws CoreException
	{
		log.trace("createProject() - projectName: " + projectName);

		//create a new generic project
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		IProject project = root.getProject(projectName);
		project.create(null);
		project.open(null);

		//add the java nature
		IProjectDescription desc = project.getDescription();
		desc.setNatureIds(new String[] { JavaCore.NATURE_ID });
		project.setDescription(desc, null);

		//set output location
		IJavaProject javaProject = JavaCore.create(project);
		IFolder binDir = project.getFolder("bin");
		IPath binPath = binDir.getFullPath();
		javaProject.setOutputLocation(binPath, null);

		//set source dir
		IFolder srcDir = project.getFolder("src");
		IPath srcPath = srcDir.getFullPath();
		javaProject.setRawClasspath(new IClasspathEntry[] { JavaCore.newSourceEntry(srcPath) }, null);

		//add basic java libraries to class path
		IClasspathEntry[] oldEntries = javaProject.getRawClasspath();
		IClasspathEntry[] newEntries = new IClasspathEntry[oldEntries.length + 1];
		System.arraycopy(oldEntries, 0, newEntries, 0, oldEntries.length);
		newEntries[oldEntries.length] = JavaRuntime.getDefaultJREContainerEntry();
		javaProject.setRawClasspath(newEntries, null);
	}

	/**
	 * Permanently removes the project with the given name.
	 * 
	 * @param projectName
	 * @throws CoreException
	 */
	public static void removeProject(String projectName) throws CoreException
	{
		log.trace("removeProject() - projectName: " + projectName);

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject project = workspace.getRoot().getProject(projectName);

		//close it
		project.close(null);

		//remove it
		project.delete(true, true, null);
	}

	/**
	 * Initializes the given project with the data from a project template.
	 * 
	 * @param projectName the name of the project to copy the files into
	 * @param templateName the name of the template to copy the files from
	 * @throws IOException 
	 * @throws CoreException 
	 */
	public static void copyFilesToProject(String projectName, String templateName) throws CoreException, IOException
	{
		log.trace("copyFilesToProject() - projectName: " + projectName + ", templateName: " + templateName);

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject project = workspace.getRoot().getProject(projectName);

		importResources(project, "/templates/" + templateName);
	}

	/**
	 * Process UI input but do not return for the
	 * specified time interval.
	 *
	 * @param waitTimeMillis the number of milliseconds
	 */
	public static void delay(long waitTimeMillis)
	{
		Display display = Display.getCurrent();

		// If this is the UI thread,
		// then process input.
		if (display != null)
		{
			long endTimeMillis = System.currentTimeMillis() + waitTimeMillis;
			while (System.currentTimeMillis() < endTimeMillis)
			{
				if (!display.readAndDispatch())
					display.sleep();
			}
			display.update();
		}
		// Otherwise, perform a simple sleep.
		else
		{
			try
			{
				Thread.sleep(waitTimeMillis);
			}
			catch (InterruptedException e)
			{
				// Ignored.
			}
		}
	}

	/**
	* Wait until all background tasks are complete.
	*/
	public static void waitForJobs()
	{
		while (Job.getJobManager().currentJob() != null)
			delay(1000);
	}

	/**
	 * Imports resources from <code>bundleSourcePath</code> to <code>importTarget</code>.
	 * 
	 * Source: org.eclipse.jdt.testplugin.JavaProjectHelper
	 * 
	 * @param importTarget the parent container
	 * @param bundleSourcePath the path to a folder containing resources
	 * 
	 * @throws CoreException import failed
	 * @throws IOException import failed
	 */
	@SuppressWarnings("unchecked")
	private static void importResources(IContainer importTarget, String bundleSourcePath) throws CoreException,
			IOException
	{
		Enumeration<String> entryPaths = CPCTestPlugin.getDefault().getBundle().getEntryPaths(bundleSourcePath);
		if (entryPaths == null)
		{
			log.warn("importResources() - unable to find source - bundleSourcePath: " + bundleSourcePath
					+ ", importTarget: " + importTarget);
			return;
		}

		while (entryPaths.hasMoreElements())
		{
			String path = entryPaths.nextElement();
			IPath name = new Path(path.substring(bundleSourcePath.length()));

			if (path.contains(".svn/"))
				//don't copy over svn dirs
				continue;

			if (path.endsWith("/"))
			{
				IFolder folder = importTarget.getFolder(name);
				//create it if it doesn't exist
				if (!folder.exists())
					folder.create(false, true, null);
				importResources(folder, path);
			}
			else
			{
				URL url = CPCTestPlugin.getDefault().getBundle().getEntry(path);
				IFile file = importTarget.getFile(name);
				file.create(url.openStream(), true, null);
			}
		}
	}

	public static IFile getFile(String projectName, String filePath)
	{
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject project = workspace.getRoot().getProject(projectName);

		return project.getFile(new Path(filePath));
	}

	public static IViewPart showView(String viewId) throws PartInitException
	{
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(viewId);
	}

	public static void hideView(IViewPart view)
	{
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().hideView(view);
	}

	/**
	 * Assert that the two arrays are equal.
	 * Throw an AssertionException if they are not.
	 *
	 * @param expected first array
	 * @param actual second array
	 */
	/*
	private static void assertEquals(Object[] expected, Object[] actual)
	{
		if (expected == null)
		{
			if (actual == null)
				return;
			throw new AssertionFailedError("expected is null, but actual is not");
		}
		else
		{
			if (actual == null)
				throw new AssertionFailedError("actual is null, but expected is not");
		}

		assertEquals("expected.length " + expected.length + ", but actual.length " + actual.length, expected.length,
				actual.length);

		for (int i = 0; i < actual.length; i++)
			assertEquals("expected[" + i + "] is not equal to actual[" + i + "]", expected[i], actual[i]);
	}
	*/

	/**
	 * Disables Log4J logging.
	 */
	public static void disableLogging()
	{
		//FIXME: somehow this seems to have no effect
		//		LoggerRepository hierarchy = LogManager.getLoggerRepository();
		//		hierarchy.setThreshold(Level.OFF);
	}

	/**
	 * Re-enables Log4J logging.
	 */
	public static void enableLogging()
	{
		//		LoggerRepository hierarchy = LogManager.getLoggerRepository();
		//		hierarchy.setThreshold(Level.ALL);
	}

}

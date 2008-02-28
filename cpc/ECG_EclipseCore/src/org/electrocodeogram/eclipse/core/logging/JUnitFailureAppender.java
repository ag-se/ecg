package org.electrocodeogram.eclipse.core.logging;


import static org.junit.Assert.fail;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.electrocodeogram.eclipse.core.ECGEclipseCorePlugin;


public class JUnitFailureAppender extends AppenderSkeleton
{
	private static boolean inUnitTest = false;

	/**
	 * Specifies whether CPC is currently being executed inside a JUnit test case or
	 * as a normal application.
	 * 
	 * @param inUnitTest true if running as JUnit test case
	 */
	public static void setInUnitTest(boolean inUnitTest)
	{
		JUnitFailureAppender.inUnitTest = inUnitTest;
	}

	public static boolean isInUnitTest()
	{
		return inUnitTest;
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.log4j.AppenderSkeleton#append(org.apache.log4j.spi.LoggingEvent)
	 */
	@Override
	protected void append(final LoggingEvent event)
	{
		if (event.getLevel().isGreaterOrEqual(Level.FATAL))
		{
			//try to display a message to the user
			Display.getDefault().asyncExec(new Runnable()
			{
				public void run()
				{
					IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					Shell shell = workbenchWindow.getShell();

					IStatus errorStatus = new Status(IStatus.ERROR, ECGEclipseCorePlugin.PLUGIN_ID, IStatus.ERROR,
							event.getRenderedMessage(), (event.getThrowableInformation() != null ? event
									.getThrowableInformation().getThrowable() : null));

					ErrorDialog
							.openError(
									shell,
									"Fatal CPC Error",
									"A fatal CPC error occured. You may want to review the CPC log files for more details. Please report any such errors on the CPC website.",
									errorStatus);
				}
			});
		}

		if (event.getLevel().isGreaterOrEqual(Level.ERROR))
		{
			//check if we're running inside a JUnit test case.
			if (inUnitTest)
			{
				//ok we should fail here
				fail(event.getRenderedMessage());
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.log4j.AppenderSkeleton#close()
	 */
	@Override
	public void close()
	{
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.log4j.AppenderSkeleton#requiresLayout()
	 */
	@Override
	public boolean requiresLayout()
	{
		return true;
	}

}

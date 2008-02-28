package org.electrocodeogram.cpc.core.utils;


import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.widgets.Display;


public class CPCCoreDebugThread extends Thread
{
	private static final Log log = LogFactory.getLog(CPCCoreDebugThread.class);

	private boolean isRunning = true;

	/**
	 * How long do we wait for eclipse to be fully loaded, before we start our checks?
	 */
	private static final long INITIAL_STARTUP_DELAY = 3 * 60 * 1000; // 3 minutes

	/**
	 * How often do we check for a dead-locked main thread?<br/>
	 * This value must be larger than <em>ASYNCDISPATCH_RECHECK_DELAY*ASYNCDISPATCH_CHECK_RETRIES</em>.
	 */
	private static final long DEADLOCK_CHECK_DELAY = 15 * 1000; //every 30 secs

	/**
	 * How long do we wait between checks whether our async dispatched runnable
	 * was already executed?
	 */
	private static final long ASYNCDISPATCH_RECHECK_DELAY = 1000; //once a second

	/**
	 * How often do we recheck whether the runnable was executed?
	 */
	private static final int ASYNCDISPATCH_CHECK_RETRIES = 5; //retry 5 times (=> 5 seconds)

	/**
	 * To prevent spamming the log, we'll stop all checking and logging once we've seen
	 * this number of dead locks.
	 */
	private static final int MAXLOGGED_ERRORS = 3;

	private int errorCount = 0;

	public CPCCoreDebugThread()
	{
		super("CPCCoreDebugThread");

		log.trace("CPCCoreDebugThread()");
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run()
	{
		try
		{
			Thread.sleep(INITIAL_STARTUP_DELAY);
		}
		catch (InterruptedException e1)
		{
			log.info("run() - interrupted during initial startup delay.");
		}

		while (true)
		{
			//sleep a bit
			try
			{
				Thread.sleep(DEADLOCK_CHECK_DELAY);
			}
			catch (InterruptedException e)
			{
				log.info("run() - interrupted.");
			}

			if (!isRunning)
			{
				log.debug("run() - shutting down.");
				return;
			}

			log.trace("run() - rechecking main thread.");

			//now check whether the main thread is still accepting jobs
			if (!isMainThreadAlive())
			{
				//ok, log a complete stack trace of all threads to the error log.

				log.warn("run() - unable to dispatch work to main thread, possible deadlock detected.");

				dumpCompleteStackTrace();

				++errorCount;
				if (errorCount > MAXLOGGED_ERRORS)
				{
					log.warn("run() - maximum error count reached, not doing any more checks - errorCount: "
							+ errorCount);
					return;
				}
			}
		}
	}

	public void shutdown()
	{
		log.trace("shutdown()");
		isRunning = false;
		this.interrupt();
	}

	private boolean isMainThreadAlive()
	{
		log.trace("isMainThreadAlive()");

		//first check if the thread has died completely, this shouldn't happen
		if (!Display.getDefault().getThread().isAlive())
		{
			//this shouldn't happen
			log.warn("isMainThreadAlive() - main display thread is NOT ALIVE!");
			return false;
		}

		//now check if we can still dispatch tasks to the main thread
		MainThreadCheckerRunnable checker = new MainThreadCheckerRunnable();
		Display.getDefault().asyncExec(checker);

		//now wait a bit and check if the runnable was successfully executed.
		for (int i = 0; i < ASYNCDISPATCH_CHECK_RETRIES; ++i)
		{
			if (checker.wasRun())
				//ok, the main thread is still alive
				break;

			//wait a bit, then try again
			try
			{
				Thread.sleep(ASYNCDISPATCH_RECHECK_DELAY);
			}
			catch (InterruptedException e)
			{
				log.info("isMainThreadAlive() - interrupted.");
			}
		}

		if (!checker.wasRun)
		{
			log.warn("isMainThreadAlive() - main display thread did not react, it may be dead-locked.");
			return false;
		}

		log.trace("isMainThreadAlive() - result: true");
		return true;
	}

	private void dumpCompleteStackTrace()
	{
		log.trace("dumpCompleteStackTrace()");

		/*
		 * TODO: check if there is any way to check whether we're currently running under
		 * the control of a debugger. If we are, there is no need for these dumps.
		 * Especially because they'd be most likely caused by the user suspending the
		 * main thread.
		 * Right now this would lead to bogus error message spamming by this thread.
		 */

		log.info("Dumping full thread status.");

		Map<Thread, StackTraceElement[]> stackTraces = Thread.getAllStackTraces();
		for (Map.Entry<Thread, StackTraceElement[]> entry : stackTraces.entrySet())
		{
			log.info("------------------------------------------------------------------------");
			log.info("THREAD - name: " + entry.getKey().getName() + ", alive: " + entry.getKey().isAlive() + " - "
					+ entry.getKey());
			for (int i = 0; i < entry.getValue().length; ++i)
			{
				log.info("   " + entry.getValue()[i].toString());
			}
		}
	}

	private class MainThreadCheckerRunnable implements Runnable
	{
		private volatile boolean wasRun = false;

		/*
		 * (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run()
		{
			log.trace("MainThreadCheckerRunnable.run()");
			wasRun = true;
		}

		public boolean wasRun()
		{
			return wasRun;
		}
	}
}

package de.fu_berlin.inf.atl;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.InvalidPreferencesFormatException;
import java.util.prefs.Preferences;

import org.eclipse.swt.SWT;
import org.eclipse.swt.internal.win32.OS;
import org.eclipse.swt.internal.win32.TCHAR;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import de.fu_berlin.inf.atl.storer.DisabledStorer;
import de.fu_berlin.inf.atl.storer.FileStorer;
import de.fu_berlin.inf.atl.storer.Storer;

/*
 * Created on 12.04.2006
 * 
 * Christopher Oezbek - 2006 - oezbek@inf.fu-berlin.de
 * 
 */
public class Main {

	// Log problems and failures
	Logger fLogger = Logger.getLogger("ATL");

	// For writing the application title logs
	Storer events, durations;

	int fUpdateInterval;

	class WindowInfo {
		int windowHandle, threadHandle, processHandle;

		String windowTitle;

		long activationTimestamp;

		void getFromCurrent() {
			windowHandle = OS.GetForegroundWindow();

			TCHAR a = new TCHAR(1, 1024);
			OS.GetWindowText(windowHandle, a, 1024);

			windowTitle = a.toString().trim();

			int[] pid = new int[1];
			threadHandle = OS.GetWindowThreadProcessId(windowHandle, pid);
			processHandle = pid[0];
		}

		public boolean equals(Object other) {
			if (!(other instanceof WindowInfo)) {
				return false;
			}
			return this.windowHandle == ((WindowInfo) other).windowHandle;
		}

		public void activate() {
			long now = System.currentTimeMillis();
			activationTimestamp = now;

			events.log(now + "\t" + processHandle + "\t" + threadHandle + "\t"
					+ windowHandle + "\tACTIVATE\t" + windowTitle);
		}

		public void rename() {
			long now = System.currentTimeMillis();

			events.log(now + "\t" + processHandle + "\t" + threadHandle + "\t"
					+ windowHandle + "\tRENAME\t" + windowTitle);

			durations.log(activationTimestamp + "\t" + now + "\t"
					+ (now - activationTimestamp) + "\t" + processHandle + "\t"
					+ threadHandle + "\t" + windowHandle + "\t" + windowTitle);

			activationTimestamp = now;
		}

		public void deactivate() {
			long now = System.currentTimeMillis();

			events.log(now + "\t" + processHandle + "\t" + threadHandle + "\t"
					+ windowHandle + "\tDEACTIVATE\t" + windowTitle);
			durations.log(activationTimestamp + "\t" + now + "\t"
					+ (now - activationTimestamp) + "\t" + processHandle + "\t"
					+ threadHandle + "\t" + windowHandle + "\t" + windowTitle);
		}
	}

	Label label;

	public void init() {

		{ // Import preferences from file each time
			InputStream is = null;
			try {
				is = new BufferedInputStream(new FileInputStream("atl.conf"));
			} catch (FileNotFoundException e) {
				fLogger.config("Configuration file atl.conf was not found.");
			}

			try {
				Preferences.importPreferences(is);
				is.close();
			} catch (InvalidPreferencesFormatException e) {
				fLogger.warning("Invalid Preference Format in atl.conf.");
			} catch (IOException e) {
				fLogger.warning("Error reading atl.conf.");
			}
		}

		Preferences prefs = Preferences.userNodeForPackage(this.getClass());

		boolean doEvents = prefs.getBoolean("events", true);
		String eventsFilename = prefs.get("eventFile", "atl-events-$t.txt");

		boolean doDurations = prefs.getBoolean("durations", true);
		String durationsFilename = prefs.get("durationsFile",
				"atl-durations-$t.txt");

		if (doEvents) {
			events = new FileStorer(eventsFilename);
		} else {
			events = new DisabledStorer();
		}

		if (doDurations) {
			durations = new FileStorer(durationsFilename);
		} else {
			durations = new DisabledStorer();
		}

		fUpdateInterval = prefs.getInt("updateInterval", 200);
		if (fUpdateInterval <= 0) {
			fUpdateInterval = 200;
		}

		// Update Prefs-Store
		prefs.putBoolean("events", doEvents);
		prefs.put("eventsFile", eventsFilename);

		prefs.putBoolean("durations", doDurations);
		prefs.put("durations", durationsFilename);

		prefs.putInt("updateInterval", fUpdateInterval);

		// Write to xml
		try {
			prefs.exportNode(new FileOutputStream("atl.conf",/* append */
			false));
		} catch (FileNotFoundException e) {
			fLogger.warning("Could not open atl.conf.");
		} catch (IOException e) {
			fLogger.warning("Could not write atl.conf.");
		} catch (BackingStoreException e) {
			// This is an internal problem, we don't know how to deal with it.
			// Does not really make sense to report it.
		}
	}

	private void shutdown() {
		events.shutdown();
		durations.shutdown();
	}

	class Timer implements Runnable {

		boolean fShutdown = false;

		WindowInfo fActiveWindow;

		public void shutdown() {
			fShutdown = true;
			if (fActiveWindow != null)
				fActiveWindow.deactivate();
		}

		WindowInfo getCurrentWindow() {
			WindowInfo result = new WindowInfo();
			result.getFromCurrent();
			return result;
		}

		public void run() {

			if (fShutdown)
				return;

			WindowInfo newWindow = getCurrentWindow();

			// If we did not have a previous window
			if (fActiveWindow == null) {
				fActiveWindow = newWindow;
				fActiveWindow.activate();
			}

			// Did the active window change?
			if (newWindow.windowHandle != fActiveWindow.windowHandle) {
				fActiveWindow.deactivate();
				fActiveWindow = newWindow;
				fActiveWindow.activate();
			} else {
				// Did the title change?
				if (!newWindow.windowTitle.equals(fActiveWindow.windowTitle)) {
					fActiveWindow.windowTitle = newWindow.windowTitle;
					fActiveWindow.rename();
				}
			}

			// Re-execute timer
			fDisplay.timerExec(fUpdateInterval, this);
		}
	}

	Display fDisplay;

	public void run(String[] args) {

		init();

		{
			fDisplay = new Display();
			Shell shell = new Shell(fDisplay);
			label = new Label(shell, SWT.NONE);

			shell.pack();
			label.pack();

			Timer timer = new Timer();
			fDisplay.timerExec(fUpdateInterval, timer);

			shell.setSize(200, 200);
			shell.open();
			while (!shell.isDisposed()) {
				if (!fDisplay.readAndDispatch())
					fDisplay.sleep();
			}
			timer.shutdown();
		}

		shutdown();

		fDisplay.dispose();
	}

	public static void main(String[] args) {
		Main app = new Main();
		app.run(args);
	}
}

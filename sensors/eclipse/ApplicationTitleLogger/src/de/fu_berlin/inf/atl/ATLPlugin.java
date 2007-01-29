package de.fu_berlin.inf.atl;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.internal.win32.OS;
import org.eclipse.swt.internal.win32.TCHAR;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import de.fu_berlin.inf.atl.ntps.NTProcess;
import de.fu_berlin.inf.atl.preferences.PreferenceConstants;
import de.fu_berlin.inf.atl.storer.DisabledStorer;
import de.fu_berlin.inf.atl.storer.ECGStorer;
import de.fu_berlin.inf.atl.storer.FileStorer;
import de.fu_berlin.inf.atl.storer.OutStorer;
import de.fu_berlin.inf.atl.storer.Storer;

/**
 * The activator class controls the plug-in life cycle
 */
public class ATLPlugin extends AbstractUIPlugin implements IStartup {

	// The plug-in ID
	public static final String PLUGIN_ID = "ApplicationTitleSensor";

    // For writing the application title logs
    Storer events;

    Display fDisplay;
    Timer fTimer;

    int fUpdateInterval;

	// The shared instance
	private static ATLPlugin plugin;
	
	/**
	 * The constructor
	 */
	public ATLPlugin() {
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
        init();
        fTimer = new Timer();
        PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
            public void run() {
                fDisplay = PlatformUI.getWorkbench().getDisplay();
                fDisplay.timerExec(fUpdateInterval, fTimer);
            }
        });
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
        fTimer.shutdown();
        events.shutdown();
		super.stop(context);
	}

    public void earlyStartup() {
        // TODO Auto-generated method stub
        
    }

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static ATLPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

    public void init() {

        IPreferenceStore wsProperties = this.getPreferenceStore();

        boolean doEvents = wsProperties.getBoolean(PreferenceConstants.P_ENABLED);

        if (doEvents) {
            //events = new FileStorer("atl.log");
            events = new ECGStorer();
        } else {
            events = new DisabledStorer();
        }

        fUpdateInterval = wsProperties.getInt(PreferenceConstants.P_INTERVAL);
        if (fUpdateInterval <= 0) {
            fUpdateInterval = 200;
        }
    }

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

        String processName;

        public String getProcessName() {
            if (processName != null)
                return processName;
            int hProcess = 0;
            try {
                hProcess = NTProcess.OpenProcess(processHandle);
                if (hProcess != 0) {
                    int[] hModule = NTProcess.EnumProcessModules(hProcess);
                    if (hModule != null) {
                        processName = NTProcess.GetModuleBaseName(hProcess,
                                hModule[0]);
                    }
                }
            } finally {
                if (hProcess != 0)
                    NTProcess.CloseHandle(hProcess);
            }

            return processName;
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

            events.log(now, processHandle, threadHandle, 
                    windowHandle, "ACTIVATE", windowTitle, 
                    getProcessName());
        }

        public void rename() {
            long now = System.currentTimeMillis();

            events.log(now, processHandle, threadHandle, 
                    windowHandle, "RENAME", windowTitle, 
                    getProcessName());
            activationTimestamp = now;
        }

        public void deactivate() {
            long now = System.currentTimeMillis();

            events.log(now, processHandle, threadHandle, 
                    windowHandle, "DEACTIVATE", windowTitle, 
                    getProcessName());
        }
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

}

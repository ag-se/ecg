package org.electrocodeogram.cpc.test.utils;


import org.electrocodeogram.cpc.core.CPCCorePlugin;
import org.electrocodeogram.cpc.test.CPCTestPlugin;
import org.electrocodeogram.eclipse.core.ECGEclipseCorePlugin;


public class StandaloneTestUtils
{
	public static void initEclipseEnvironment()
	{
		@SuppressWarnings("unused")
		ECGEclipseCorePlugin ecgcore = new ECGEclipseCorePlugin();
		ecgcore.configureLogging();

		@SuppressWarnings("unused")
		CPCTestPlugin testp = new CPCTestPlugin();

		@SuppressWarnings("unused")
		CPCCorePlugin cpccore = new CPCCorePlugin();

		//CPCTrackPlugin cpctp = new CPCTrackPlugin();
		//cpctp.earlyStartup();

	}

	public static void printMem()
	{
		System.out.println("total: " + Runtime.getRuntime().totalMemory() + ", free: "
				+ Runtime.getRuntime().freeMemory() + ", used: "
				+ (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
	}

}

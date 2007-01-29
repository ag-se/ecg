package de.fu_berlin.inf.atl.ntps;

class Main {
	
	public static void main(String args[]) {

		if (NTProcess.Initialize()) {
			int[] pids = NTProcess.EnumProcesses();
			if (pids != null) {
				System.out.println("List of Processes ....");
				for (int i = 0; i < pids.length; i++) {
					int hProcess = NTProcess.OpenProcess(pids[i]);
					if (hProcess != 0) {
						int[] hModule = NTProcess.EnumProcessModules(hProcess);
						if (hModule != null) {
							System.out.println("PID: "
									+ pids[i]
									+ "  Image Name: "
									+ NTProcess.GetModuleBaseName(hProcess,
											hModule[0]));
						}
					}
					NTProcess.CloseHandle(hProcess);
				}
			}
		}
	}
	
}
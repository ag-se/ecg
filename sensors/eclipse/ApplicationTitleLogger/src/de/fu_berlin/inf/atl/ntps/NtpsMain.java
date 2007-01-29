/* 
 * Copyright (C) Iulian Tarhon, 1999
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * Optionally, you may find a copy of the GNU General Public License
 * from http://www.fsf.org/copyleft/gpl.txt
 */

package de.fu_berlin.inf.atl.ntps;

class NtpsMain {
	
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
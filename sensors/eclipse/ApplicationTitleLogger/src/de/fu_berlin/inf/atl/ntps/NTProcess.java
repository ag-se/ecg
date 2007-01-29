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

/*
 * Class : NTProcess
 * Author: 
 * Date : May, 1999
 */
public class NTProcess {

	/**
	 * check psapi.dll and psapi functions return true if NT process can be
	 * enumerate
	 */
	public static native boolean Initialize();

	/**
	 * Description:The EnumProcesses function retrieves the process identifier
	 * for each process object in the system return value:the list of process
	 * identifiers call psapi function EnumProcesses
	 */
	public static native int[] EnumProcesses();

	/**
	 * Description:The OpenProcess function returns a handle to an existing
	 * process object. return value:If the function succeeds, the return value
	 * is an open handle to the specified process call kernel32 function
	 * OpenProcess
	 */
	public static native int OpenProcess(int Pid);

	/**
	 * Description:The EnumProcessModules function retrieves a handle for each
	 * module in the specified process return value:the list of module handles
	 * call psapi function EnumProcessModules
	 */
	public static native int[] EnumProcessModules(int hProcess);

	/**
	 * Description:The GetModuleFileNameEx function retrieves the fully
	 * qualified path for the specified module return value:the fully qualified
	 * path for the specified module call psapi function GetModuleFileNameEx
	 */
	public static native String GetModuleFileName(int hProcess, int hModule);

	/**
	 * Description:The GetModuleBaseName function retrieves the base name of the
	 * specified module return value:the base name of the specified module call
	 * psapi function GetModuleBaseName
	 */
	public static native String GetModuleBaseName(int hProcess, int hModule);

	/**
	 * Description:The GetProcessMemoryInfo function retrieves information about
	 * the memory usage of the specified process. return value:MemoryInfo - if
	 * succes call psapi function GetProcessMemoryInfo
	 */
	public static native MemoryInfo GetProcessMemoryInfo(int hProcess);

	/**
	 * Description:function closes an open object handle return value:true if
	 * succesfuly call kernel32 function CloseHandle
	 */
	public static native boolean CloseHandle(int handle);

	static {
		try {
			System.loadLibrary("NTPS");
			Initialize();
		} catch (java.lang.UnsatisfiedLinkError e) {
			System.out.println(e);
		}

	}
}
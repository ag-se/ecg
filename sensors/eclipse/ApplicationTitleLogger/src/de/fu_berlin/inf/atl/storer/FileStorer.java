/* 
 * Copyright (C) Christopher Oezbek, 2006 - oezbek@inf.fu-berlin.de
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

package de.fu_berlin.inf.atl.storer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.logging.Logger;

public class FileStorer implements Storer {

	Logger fLogger = Logger.getLogger("ATL");

	String fFilename;

	PrintWriter out;

	boolean enabled;

	StringBuffer fDataToFlush = new StringBuffer();

	void open() {
		try {
			File f = new File(fFilename);
			File dir = f.getParentFile();
			if (dir != null && !dir.exists())
				dir.mkdirs();
			out = new PrintWriter(new FileWriter(f,
			/* append */true), /* autoflush */true);
		} catch (IOException e) {
			out = null; // In case out was != null when entering
			fLogger.severe("Could not open output-file. Storing to memory.");
		}
	}

	/**
	 * Will replace $t in the filename with the current time-stamp and open the
	 * output stream
	 */
	public FileStorer(String filename) {

		Calendar now = Calendar.getInstance();
		String nowString = String.format("%1$tY-%1$tm-%1$td-%1$tH.%1$tM.%1$tS",
				now);
		fFilename = filename.replaceAll("\\$t", nowString);

		open();

		enabled = true;
	}

	public void log(long now, 
                    int processHandle, 
                    int threadHandle, 
                    int windowHandle,
                    String type,
                    String windowTitle, 
                    String processName) {
		if (!enabled)
			return;

        String log = now + "\t" + processHandle + "\t" + threadHandle + "\t"
            + windowHandle + "\t" + type + "\t" + windowTitle + "\t"
            + processName;
		// Write to buffer in case something goes wrong
		fDataToFlush.append(log + "\n");

		flush();
	}

	void flush() {
		if (out == null)
			open();

		if (out == null)
			return;

		out.print(fDataToFlush.toString());
		if (out.checkError()) {
			fLogger.severe("Writing to file " + fFilename
					+ " failed. Storing to memory");
			out.close();
			out = null;
		} else {
			// Successfully written!
			// Start new buffer
			fDataToFlush = new StringBuffer();
		}
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean disabled) {
		this.enabled = disabled;
	}

	public void shutdown() {
		flush();
		if (fDataToFlush.length() > 0){
			fLogger.severe("Could not write all data before shuting down. Missing the following lines:\n" + fDataToFlush.toString());
		}
		if (out != null)
			out.close();
	}
}

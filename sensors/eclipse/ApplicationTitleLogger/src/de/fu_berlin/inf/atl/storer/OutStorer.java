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

import java.util.logging.Logger;

public class OutStorer implements Storer {

	Logger fLogger = Logger.getLogger("ATL");

	boolean enabled;

	StringBuffer fDataToFlush = new StringBuffer();

	/**
	 * Will replace $t in the filename with the current time-stamp and open the
	 * output stream
	 */
	public OutStorer() {

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

        System.out.println(now + "\t" + processHandle + "\t" + threadHandle + "\t"
            + windowHandle + "\t" + type + "\t" + windowTitle + "\t"
            + processName);
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean disabled) {
		this.enabled = disabled;
	}

	public void shutdown() {
	}
}

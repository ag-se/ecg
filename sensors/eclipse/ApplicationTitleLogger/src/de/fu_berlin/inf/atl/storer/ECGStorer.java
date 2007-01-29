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

import org.electrocodeogram.event.CommonData;
import org.electrocodeogram.event.MicroActivity;
import org.electrocodeogram.sensor.eclipse.ECGEclipseSensor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ECGStorer implements Storer {

	Logger fLogger = Logger.getLogger("ATL");

	boolean enabled = false;

    private Element application_processHandle; 
    private Element application_threadHandle; 
    private Element application_windowHandle;
    private Element application_type;
    private Element application_windowTitle; 
    private Element application_processName;
    private MicroActivity microActivity;

    /**
	 * Will replace $t in the filename with the current time-stamp and open the
	 * output stream
	 */
	public ECGStorer() {

        microActivity = new MicroActivity();

        Document microactivity_doc = microActivity.getMicroActivityDoc();
        Element application = microactivity_doc.createElement("application");
        application_processHandle = microactivity_doc.createElement("processHandle");
        application_threadHandle = microactivity_doc.createElement("threadHandle");
        application_windowHandle = microactivity_doc.createElement("windowHandle");
        application_type = microactivity_doc.createElement("type");
        application_windowTitle = microactivity_doc.createElement("windowTitle");
        application_processName = microactivity_doc.createElement("processName");

        application.appendChild(application_processHandle);
        application.appendChild(application_threadHandle);
        application.appendChild(application_windowHandle);
        application.appendChild(application_type);
        application.appendChild(application_windowTitle);
        application.appendChild(application_processName);
        
        microActivity.setCustomElement(application);            
                                                
        CommonData commonData = microActivity.getCommonData();
        commonData.setVersion(1); // 1 is default
        commonData.setCreator("ApplicationTitleLogger1.0.0"); 

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
        
        if (windowHandle == 0)
            return;

        ECGEclipseSensor sensor = ECGEclipseSensor.getInstance();
        
        CommonData commonData = microActivity.getCommonData();
        commonData.setProjectname(null);
        commonData.setId(String.valueOf(windowHandle));
        commonData.setUsername(sensor.getUsername());

        application_processHandle.setTextContent(String.valueOf(processHandle));
        application_threadHandle.setTextContent(String.valueOf(threadHandle));
        application_windowHandle.setTextContent(String.valueOf(windowHandle));
        application_type.setTextContent(type);
        application_windowTitle.setTextContent(windowTitle);
        application_processName.setTextContent(processName);

        sensor.processActivity("msdt.application.xsd",
                microActivity.getSerializedMicroActivity());
        
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

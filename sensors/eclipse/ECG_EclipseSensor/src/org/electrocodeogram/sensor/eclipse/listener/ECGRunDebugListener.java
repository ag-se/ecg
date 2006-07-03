package org.electrocodeogram.sensor.eclipse.listener;

import java.util.logging.Level;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.RuntimeProcess;
import org.electrocodeogram.logging.LogHelper.ECGLevel;
import org.electrocodeogram.sensor.eclipse.ECGEclipseSensor;

/**
 * This is listening for run and debug events.
 */
public class ECGRunDebugListener implements IDebugEventSetListener {

    /**
     * 
     */
    private final ECGEclipseSensor sensor;

    /**
     * @param sensor
     */
    public ECGRunDebugListener(ECGEclipseSensor sensor) {
        this.sensor = sensor;
    }

    /**
     * A reference to the current programm launch.
     */
    private ILaunch currentLaunch;

    /**
     * @see org.eclipse.debug.core.IDebugEventSetListener#handleDebugEvents(org.eclipse.debug.core.DebugEvent[])
     * 
     */
    public void handleDebugEvents(final DebugEvent[] events) {

        ECGEclipseSensor.logger.entering(this.getClass().getName(), "handleDebugEvents");

        if (events == null || events.length == 0) {
            ECGEclipseSensor.logger.log(Level.FINE,
                "The Parameter events null or empty. Ignoring event.");

            ECGEclipseSensor.logger.exiting(this.getClass().getName(), "handleDebugEvents");

            return;
        }
                    
        // TODO: Allow for more than one parallel launch
        for (int i=0; i < events.length; i++) {
            Object source = events[i].getSource();

            if (source instanceof RuntimeProcess) {
                RuntimeProcess rp = (RuntimeProcess) source;

                ILaunch launch = rp.getLaunch();
                
                if (launch.equals(this.currentLaunch) && events[i].getKind() == DebugEvent.TERMINATE) {
                	// An active launch got a TERMINATE event
                	if (analyseTermination(rp, launch))
                		this.currentLaunch = null;
                } 
                else if (this.currentLaunch == null || !this.currentLaunch.equals(launch)) {
                	// There are many events on the same process and launch.
                	// analyseLaunch returns true if this launch has been recognized
                	// Otherwise re-analyse every following similiar event
                	if (analyseLaunch(rp, launch))
                		this.currentLaunch = launch;
                }
            }            	
        }

        ECGEclipseSensor.logger.exiting(this.getClass().getName(), "handleDebugEvents");
    }

    /**
     * This method is analysing the current program lauch and
     * determines if it is a run or a debug launch.
     * @param launch
     *            Is the launch to analyse
     * @param process the IProcess for this launch
     * @returns true if event has been recognized and processed
     */
    private boolean analyseLaunch(final RuntimeProcess process, final ILaunch launch) {
        ECGEclipseSensor.logger.entering(this.getClass().getName(), "analyseLaunch");

        if (launch == null) {
            ECGEclipseSensor.logger.log(Level.FINE,
                "The Parameter lauch is null. Ignoring event.");

            ECGEclipseSensor.logger.exiting(this.getClass().getName(), "analyseLaunch");
            return false;
        }

        if (process != null 
        		&& process.getAttribute(IProcess.ATTR_PROCESS_TYPE) != null
        		&& process.getAttribute(IProcess.ATTR_PROCESS_TYPE)
        			.toLowerCase().endsWith("antprocess")) {

            String cmdLine = process.getAttribute(IProcess.ATTR_CMDLINE);
        	
        	if (cmdLine == null) {
        		// It may occur that the process attribute fields are not set
        		// return false to wait for the next event of a DebugEvent.CHANGE kind
        		// which may be a change of the attribute settings
        		return false;
        	}

            ECGEclipseSensor.logger.log(ECGLevel.PACKET, "An ant event has been recorded.");

        	// extract Ant's buildfile and target settings from the 
        	// java-call of Ant. It relies on the build file stated
            // after "-buildfile" and the target name being the last
            // argument (w/o '_' prefix) and without an "-"option 
            // preceeding
        	String buildfile = "";
        	String target = "";
        	String[] args = DebugPlugin.parseArguments(cmdLine);
            for (int i=0; i < args.length; i++) {
            	if (args[i].equals("-buildfile") 
            			&& i+1 < args.length 
            			&& args[i+1] != null) {
            		buildfile = args[i+1]; 
            	}
            	if (i+1 == args.length 
            			&& !args[i-1].startsWith("-") 
            			&& !args[i].startsWith("-")) {
            		target = args[i];
            	}
            }
            
            this.sensor.processActivity(
                "msdt.antrun.xsd",
                "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                    + this.sensor.username
                    + "</username><id>"
                    + launch.hashCode()
                    + "</id></commonData><ant>"
                    + "<id>" + launch.hashCode() + "</id>"
                    + "<mode>" + launch.getLaunchMode() + "</mode>"
                    + "<buildfile>" + buildfile + "</buildfile>"
                    + "<target>" + target + "</target>"
                    + "</ant></microActivity>");
        	
        }
        else {

            ECGEclipseSensor.logger.log(ECGLevel.PACKET, "A run event has been recorded.");

            String config = "null";
            if (launch.getLaunchConfiguration() != null)
            	config = launch.getLaunchConfiguration().getName();
            
            this.sensor.processActivity(
                "msdt.run.xsd",
                "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                    + this.sensor.username
                    + "</username><id>"
                    + launch.hashCode()
                    + "</id></commonData><run>"
                    + "<id>" + launch.hashCode() + "</id>"
                    + "<mode>" + launch.getLaunchMode() + "</mode>"
                    + "<launch>" + config + "</launch>"
                    + "</run></microActivity>");
        }

        ECGEclipseSensor.logger.exiting(this.getClass().getName(), "analyseLaunch");
        
        return true;
    }

    /**
     * This method is analysing a program termination.
     * @param launch Is the launch to analyse
     * @param process the IProcess for this launch
     * @returns true if event has been recognized and processed
     */
    private boolean analyseTermination(final RuntimeProcess process, final ILaunch launch) {
        ECGEclipseSensor.logger.entering(this.getClass().getName(), "analyseTermination");

        if (launch == null) {
            ECGEclipseSensor.logger.log(Level.FINE,
                "The Parameter lauch is null. Ignoring event.");

            ECGEclipseSensor.logger.exiting(this.getClass().getName(), "analyseTermination");
            return false;
        }

        if (process.getAttribute(IProcess.ATTR_PROCESS_TYPE).toLowerCase().endsWith("antprocess")) {

            ECGEclipseSensor.logger.log(ECGLevel.PACKET, "An ant termination event has been recorded.");

            this.sensor.processActivity(
                "msdt.antrun.xsd",
                "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                    + this.sensor.username
                    + "</username><id>"
                    + launch.hashCode()
                    + "</id></commonData><ant>"
                    + "<id>" + launch.hashCode() + "</id>"
                    + "<mode>termination</mode>"
                    + "<buildfile></buildfile>"
                    + "<target></target>"
                    + "</ant></microActivity>");
        	
        }
        else {

            ECGEclipseSensor.logger.log(ECGLevel.PACKET, "A run/debug termination event has been recorded.");

            String config = "null";
            if (launch.getLaunchConfiguration() != null)
            	config = launch.getLaunchConfiguration().getName();
            
            this.sensor.processActivity(
                "msdt.run.xsd",
                "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                    + this.sensor.username
                    + "</username><id>"
                    + launch.hashCode()
                    + "</id></commonData><run>"
                    + "<id>" + launch.hashCode() + "</id>"
                    + "<mode>termination</mode>"
                    + "<launch>" + config + "</launch>"
                    + "</run></microActivity>");
        }
        
        ECGEclipseSensor.logger.exiting(this.getClass().getName(), "analyseTermination");
        
        return true;
    }

}
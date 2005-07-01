package org.electrocodeogram.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.electrocodeogram.event.IllegalEventParameterException;
import org.electrocodeogram.event.ValidEventPacket;
import org.electrocodeogram.module.ModuleRegistry;
import org.electrocodeogram.module.source.SourceModule;
import org.hackystat.kernel.admin.SensorProperties;
import org.hackystat.kernel.shell.SensorShell;

/**
 * This is the root class of the ECG Server & Lab component. It is also the entry point
 * for the collected event data to be processed through the ECG Lab's modules.
 *  
 * The SensorShellWrapper extends and uses the HackyStat SensorShell class to validate
 * incoming events as beeing events of a legal HackyStat SensorDataType.
 */
public class SensorShellWrapper extends SensorShell
{
    private static SensorShellWrapper theInstance = null;

    private SourceModule sensorSource = null;

    private SensorServer sensorServer = null;

    private ModuleRegistry moduleRegistry = null;

    /*
     * The private constructot creates one instance of the SensorShellWrapper and
     * also creates all other needed ECG Server & Lab components.
     */
    private SensorShellWrapper()
    {
        super(new SensorProperties("", ""), false, "ElectroCodeoGram");

        Console gob = new Console();

        this.sensorServer = SensorServer.getInstance();

        this.sensorServer.start();

        this.sensorSource = new SourceModule();

        this.sensorSource.setName("Sensor Source");

        this.moduleRegistry = ModuleRegistry.getInstance();

        this.moduleRegistry.addModuleInstance(this.sensorSource);

        gob.start();

    }

    private SensorShellWrapper(File file)
    {
        this();

        ModuleRegistry.getInstance(file);
    }

    /**
     * This method returns the singleton instance of the SensorShellWrapper object.
     * @return The singleton instance of the SensorShellWrapper object
     */
    public static SensorShellWrapper getInstance()
    {
        if (theInstance == null) {
            theInstance = new SensorShellWrapper();
        }

        return theInstance;
    }

    /**
     * @see org.hackystat.kernel.shell.SensorShell#doCommand(java.util.Date, java.lang.String, java.util.List)
     * 
     * This is the overriden version of the HackyStat SensorShell's method. After calling the original
     * method it performs further steps to pass the event data into the ECG Lab.
     */
    @Override
    public synchronized boolean doCommand(Date timeStamp, String commandName, List argList)
    {
        boolean result = super.doCommand(timeStamp, commandName, argList);

        if (result) {

            List<String> newArgList = new ArrayList<String>(argList.size());

            Object[] entries = argList.toArray();

            for (int i = 0; i < entries.length; i++) {
                String entryString = (String) entries[i];

                if (commandName.equals("Activity") && i == 0) {
                    entryString = "HS_ACTIVITY_TYPE:" + entryString;
                }

                newArgList.add(entryString);
            }

            appendToEventSource(timeStamp, "HS_COMMAND:" + commandName, newArgList);

            return true;
        }
        return false;

    }

    /*
     * This method is used to pass the event data to the first module.
     *
     */
    private void appendToEventSource(Date timeStamp, String commandName, List argList)
    {
        if (this.sensorSource != null) {
            ValidEventPacket toAppend;
            try {

                toAppend = new ValidEventPacket(0, timeStamp, commandName,
                        argList);

                this.sensorSource.append(toAppend);
            }
            catch (IllegalEventParameterException e) {

                e.printStackTrace();
            }
        }
    }

    private class Console extends Thread
    {

        private BufferedReader bufferedReader = null;

        public Console()
        {
            System.out.println("ElectroCodeoGram Server & Lab is starting...");

            this.bufferedReader = new BufferedReader(new InputStreamReader(
                    System.in));

        }

        public void run()
        {

            while (true) {

                System.out.println(">>");

                String inputString = "" + this.readLine();

                System.out.println("Echo: " + inputString);

                if (inputString.equalsIgnoreCase("quit")) {
                    this.quit();
                    return;
                }
            }
        }

        private String readLine()
        {
            try {
                return this.bufferedReader.readLine();
            }
            catch (IOException e) {
                return "quit";
            }
        }

        private void quit()
        {
            System.exit(0);
        }
    }

    public static void main(String[] args)
    {

        File file = null;

        if (args != null && args.length > 0) {

            file = new File(args[0]);

            if (file.exists() && file.isDirectory()) {
                SensorShellWrapper shell = new SensorShellWrapper(file);
            }
            else {
                SensorShellWrapper shell = new SensorShellWrapper();
            }

        }
        else {
            SensorShellWrapper shell = new SensorShellWrapper();
        }

    }
}
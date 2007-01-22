package org.electrocodeogram.sensor.eclipse.listener;

import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.widgets.Shell;
import org.electrocodeogram.logging.LogHelper.ECGLevel;
import org.electrocodeogram.sensor.eclipse.ECGEclipseSensor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Listens for events on the windows shell, i.e. Eclipse's main window
 * Note: No shell opened event exists. It is send on startup explicitely
 */
public class ECGShellListener implements ShellListener {

    /**
     * 
     */
    private final ECGEclipseSensor sensor;

    private Document msdt_window_doc;
    
    private Element window_version;
    private Element window_creator;
    private Element window_username;
    private Element window_id;        
    private Element window_activity;
    private Element window_windowname;
    
    public ECGShellListener(ECGEclipseSensor sensor) {

        this.sensor = sensor;
        try {
            // initialize DOM skeleton for msdt.editor.xsd
            msdt_window_doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

            Element window_microactivity = msdt_window_doc.createElement("microActivity");                
            Element window_commondata = msdt_window_doc.createElement("commonData");
            Element window_window = msdt_window_doc.createElement("window");
            window_version = msdt_window_doc.createElement("version");
            window_creator = msdt_window_doc.createElement("creator");
            window_username = msdt_window_doc.createElement("username");
            window_id = msdt_window_doc.createElement("id");
            window_activity = msdt_window_doc.createElement("activity");
            window_windowname = msdt_window_doc.createElement("windowname");

            msdt_window_doc.appendChild(window_microactivity);
            window_microactivity.appendChild(window_commondata);
            window_commondata.appendChild(window_version);
            window_commondata.appendChild(window_creator);
            window_commondata.appendChild(window_username);
            window_commondata.appendChild(window_id);
            window_microactivity.appendChild(window_window);
            window_window.appendChild(window_activity);
            window_window.appendChild(window_windowname);

        } catch (ParserConfigurationException e) {
            ECGEclipseSensor.logger.log(Level.SEVERE,
                "Could not instantiate the DOM Document.");
            ECGEclipseSensor.logger.log(Level.FINE, e.getMessage());
        }

    }

    /**
     * @see org.eclipse.swt.events.ShellListener#shellActivated(org.eclipse.swt.events.ShellEvent)
     */
    public void shellActivated(ShellEvent e) {
        ECGEclipseSensor.logger.entering(this.getClass().getName(), "shellActivated",
                new Object[] {e});

        Shell shell = ((Shell)e.widget);
    
        if (shell == null) {
            ECGEclipseSensor.logger.log(Level.FINE,
                "The Parameter  \"e.widget\" is null. Ignoring event.");
            ECGEclipseSensor.logger.exiting(this.getClass().getName(), "shellActivated");
            return;
        }

        ECGEclipseSensor.logger.log(ECGLevel.PACKET,
            "A windowActivated event has been recorded.");

        window_version.setTextContent("1");
        window_creator.setTextContent(ECGEclipseSensor.CREATOR);
        window_username.setTextContent(this.sensor.username);
        window_id.setTextContent(String.valueOf(shell.hashCode()));
        window_activity.setTextContent("activated");
        window_windowname.setTextContent(shell.getText());
        
        this.sensor.processActivity("msdt.window.xsd",
                this.sensor.xmlDocumentSerializer.writeToString(msdt_window_doc));

        /* TODO old code, remove if obsolete
        processActivity(
            "msdt.window.xsd",
            "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                + ECGEclipseSensor.this.username
                + "</username><id>"
                + shell.hashCode()
                + "</id></commonData><window><activity>activated</activity><windowname>"
                + shell.getText()
                + "</windowname></window></microActivity>");
         */
        ECGEclipseSensor.logger.exiting(this.getClass().getName(), "shellActivated");
    }

    /**
     * @see org.eclipse.swt.events.ShellListener#shellClosed(org.eclipse.swt.events.ShellEvent)
     */
    public void shellClosed(ShellEvent e) {
        ECGEclipseSensor.logger.entering(this.getClass().getName(), "shellClosed", new Object[] {e});

        Shell shell = ((Shell)e.widget);
    
        if (shell == null) {
            ECGEclipseSensor.logger.log(Level.FINE,
                "The Parameter  \"e.widget\" is null. Ignoring event.");
            ECGEclipseSensor.logger.exiting(this.getClass().getName(), "shellClosed");
            return;
        }

        ECGEclipseSensor.logger.log(ECGLevel.PACKET,
            "A windowClosed event has been recorded.");

        window_version.setTextContent("1");
        window_creator.setTextContent(ECGEclipseSensor.CREATOR);
        window_username.setTextContent(this.sensor.username);
        window_id.setTextContent(String.valueOf(shell.hashCode()));
        window_activity.setTextContent("closed");
        window_windowname.setTextContent((!shell.isDisposed() ? shell.getText() : "Eclipse"));
        
        this.sensor.processActivity("msdt.window.xsd",
                this.sensor.xmlDocumentSerializer.writeToString(msdt_window_doc));
        
        /* TODO old code, remove if obsolete
        processActivity(
            "msdt.window.xsd",
            "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                + ECGEclipseSensor.this.username
                + "</username><id>"
                + shell.hashCode()
                + "</id></commonData><window><activity>closed</activity><windowname>"
                + (!shell.isDisposed() ? shell.getText() : "Eclipse")
                + "</windowname></window></microActivity>");
         */
        ECGEclipseSensor.logger.exiting(this.getClass().getName(), "shellClosed");
    }

    /**
     * @see org.eclipse.swt.events.ShellListener#shellDeactivated(org.eclipse.swt.events.ShellEvent)
     */
    public void shellDeactivated(ShellEvent e) {
        ECGEclipseSensor.logger.entering(this.getClass().getName(), "shellDeactivated", new Object[] {e});

        Shell shell = ((Shell)e.widget);
    
        if (shell == null) {
            ECGEclipseSensor.logger.log(Level.FINE,
                "The Parameter  \"e.widget\" is null. Ignoring event.");
            ECGEclipseSensor.logger.exiting(this.getClass().getName(), "shellDeactivated");
            return;
        }

        ECGEclipseSensor.logger.log(ECGLevel.PACKET,
            "A windowDeactivated event has been recorded.");

        window_version.setTextContent("1");
        window_creator.setTextContent(ECGEclipseSensor.CREATOR);
        window_username.setTextContent(this.sensor.username);
        window_id.setTextContent(String.valueOf(shell.hashCode()));
        window_activity.setTextContent("deactivated");
        window_windowname.setTextContent(shell.getText());
        
        this.sensor.processActivity("msdt.window.xsd",
                this.sensor.xmlDocumentSerializer.writeToString(msdt_window_doc));

        /* TODO old code, remove if obsolete
        processActivity(
            "msdt.window.xsd",
            "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                + ECGEclipseSensor.this.username
                + "</username><id>"
                + shell.hashCode()
                + "</id></commonData><window><activity>deactivated</activity><windowname>"
                + shell.getText()
                + "</windowname></window></microActivity>");
         */
        ECGEclipseSensor.logger.exiting(this.getClass().getName(), "shellDeactivated");
    }

    /**
     * @see org.eclipse.swt.events.ShellListener#shellDeiconified(org.eclipse.swt.events.ShellEvent)
     */
    public void shellDeiconified(ShellEvent e) {
        // not used by ECG
    }

    /**
     * @see org.eclipse.swt.events.ShellListener#shellIconified(org.eclipse.swt.events.ShellEvent)
     */
    public void shellIconified(ShellEvent e) {
        // not used by ECG
    }

    /**
     * Unfortunately, this is no official ShellListener handler
     */
    public void shellOpened(Shell shell) {
        ECGEclipseSensor.logger.entering(this.getClass().getName(), "shellOpened", new Object[] {shell});

        if (shell == null) {
            ECGEclipseSensor.logger.log(Level.FINE,
                "The Parameter  \"shell\" is null. Ignoring event.");
            ECGEclipseSensor.logger.exiting(this.getClass().getName(), "shellOpened");
            return;
        }

        ECGEclipseSensor.logger.log(ECGLevel.PACKET,
            "A windowOpened event has been recorded.");

        window_version.setTextContent("1");
        window_creator.setTextContent(ECGEclipseSensor.CREATOR);
        window_username.setTextContent(this.sensor.username);
        window_id.setTextContent(String.valueOf(shell.hashCode()));
        window_activity.setTextContent("opened");
        window_windowname.setTextContent(shell.getText());
        
        this.sensor.processActivity("msdt.window.xsd",
                this.sensor.xmlDocumentSerializer.writeToString(msdt_window_doc));

        /* TODO old code, remove if obsolete
        processActivity(
            "msdt.window.xsd",
            "<?xml version=\"1.0\"?><microActivity><commonData><username>"
                + ECGEclipseSensor.this.username
                + "</username><id>"
                + shell.hashCode()
                + "</id></commonData><window><activity>opened</activity><windowname>"
                + shell.getText()
                + "</windowname></window></microActivity>");
         */
        ECGEclipseSensor.logger.exiting(this.getClass().getName(), "shellOpened");
    }

}
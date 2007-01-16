/*
 * Class: ManualReader
 * Version: 1.0
 * Date: 16.10.2005
 * By: Frank@Schlesinger.com
 */

package org.electrocodeogram.module.source.implementation;

import java.awt.FlowLayout;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Logger;


import org.electrocodeogram.event.IllegalEventParameterException;
import org.electrocodeogram.event.WellFormedEventPacket;
import org.electrocodeogram.logging.LogHelper;
import org.electrocodeogram.misc.xml.ECGWriter;
import org.electrocodeogram.modulepackage.ModuleProperty;
import org.electrocodeogram.module.source.EventReader;
import org.electrocodeogram.module.source.EventReaderException;
import org.electrocodeogram.module.source.SourceModule;

/**
 * This <em>EventReader</em> implementation reads the events by
 * waiting for the user to submit the events by pushing a button in a
 * GUI dialog.
 */
public class ManualReader extends EventReader {

    /**
     * This is the logger.
     */
    private static Logger logger = LogHelper.createLogger(ManualReader.class
        .getName());

    
    /**
     * This buffer is filled with the events the user hhas submitted.
     * It is read by {@link #read()}.
     */
    private EventQueue eventBuffer;

    /**
     * Configuration string: CSL of event names
     */
    private String events = "";

    /**
     * Configuration string: CSL of episode names
     */
    private String episodes = "";

    /**
     * A reference to the dialog.
     */
    private ManualAnnotatorFrame frame;

    /**
     * Start time for next episode, equals end time of current event
     */
    private Date changeTime = new Date(System.currentTimeMillis());

    /**
     * Crates the <em>Eventreader</em>.
     * @param sourceModule Is the module to pass the events to.
     */
    public ManualReader(SourceModule sourceModule) {
        super(sourceModule);

        logger.entering(this.getClass().getName(), "ManualReader",
            new Object[] {sourceModule});

        this.eventBuffer = new EventQueue(this);

        boolean showDialog = false;
        ModuleProperty[] runtimeProperties = sourceModule.getRuntimeProperties();
        for (ModuleProperty property : runtimeProperties) {
            if (property.getName().equals("Events")) {
                events = property.getValue();
            }
            if (property.getName().equals("Episodes")) {
                episodes = property.getValue();
            }
            if (property.getName().equals("Show Dialog")) {
                showDialog = property.getValue().equalsIgnoreCase("true");
            }
        }

        this.frame = new ManualAnnotatorFrame(this, events, episodes);

        if (showDialog) {
            this.frame.setVisible(true);
            this.frame.setAlwaysOnTop(true);
        }
        
        logger.exiting(this.getClass().getName(), "ManualReader");

    }
    
    /**
     * @see org.electrocodeogram.module.source.EventReader#read()
     */
    public WellFormedEventPacket read() throws EventReaderException {

        return this.eventBuffer.remove();

    }

    /**
     * @param eventName
     */
    public void sendManualEvent(String eventName) {
        String microActivity = "<?xml version=\"1.0\"?><microActivity>"
            + "<commonData>"
            + "<version>1</version>"
            + "<creator>ManualAnnotationModule1.1.5</creator>"
            + "<username>" + System.getProperty("user.name") + "</username>"
            + "<projectname>" + this.frame.getCurrentProject() + "</projectname>"
            + "</commonData>"
            + "<manualevent><type>"
            + eventName
            + "</type><remark>" 
            + this.frame.getCurrentRemark()
            + "</remark></manualevent></microActivity>";

        String[] args = {"add", "msdt.manualevent.xsd", microActivity};
        
        try {
            WellFormedEventPacket packet = new WellFormedEventPacket(
                new Date(),
                "Activity", Arrays.asList(args));
            this.eventBuffer.add(packet);
        } catch (IllegalEventParameterException e1) {
            // is checked before 
        }
        
        this.frame.resetRemark();
    }

    /**
     * @param episodeName
     */
    /**
     * @param episodeName
     */
    public void sendManualEpisode(String episodeName) {
        
        String microActivity = "<?xml version=\"1.0\"?><microActivity>"
            + "<commonData>"
            + "<version>1</version>"
            + "<creator>ManualAnnotationModule1.1.5</creator>"
            + "<username>" + System.getProperty("user.name") + "</username>"
            + "<projectname>" + this.frame.getCurrentProject() + "</projectname>"
            + "</commonData>"
            + "<manualepisode><type>"
            + episodeName
            + "</type><remark>"
            + this.frame.getCurrentRemark()
            + "</remark><starttime>"
            + ECGWriter.formatDate(this.changeTime)
            + "</starttime></manualepisode></microActivity>";

        String[] args = {"add", "msdt.manualepisode.xsd", microActivity};
        
        try {
            WellFormedEventPacket packet = new WellFormedEventPacket(
                    new Date(),
                    "Activity", Arrays.asList(args));
            this.eventBuffer.add(packet);
        } catch (IllegalEventParameterException e1) {
            // is checked before 
        }
        
        this.changeTime = new Date(System.currentTimeMillis());
        this.frame.resetRemark();
    }

    /**
     * 
     */
    public void showDialog() {

        if (this.frame == null) {
            this.frame = new ManualAnnotatorFrame(this, events, episodes);
        }

        this.frame.setVisible(true);

    }

    /**
     * 
     */
    public void hideDialog() {

        this.frame.dispose();

        this.frame = null;

    }

    /**
     * @param episodes the episodes to set
     */
    public void setEpisodes(String episodes) {
        this.episodes = episodes;
    }

    /**
     * @param events the events to set
     */
    public void setEvents(String events) {
        this.events = events;
    }
}

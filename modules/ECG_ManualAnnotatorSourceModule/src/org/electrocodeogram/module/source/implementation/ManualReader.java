/*
 * Class: ManualReader
 * Version: 1.0
 * Date: 16.10.2005
 * By: Frank@Schlesinger.com
 */

package org.electrocodeogram.module.source.implementation;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

import org.electrocodeogram.event.IllegalEventParameterException;
import org.electrocodeogram.event.WellFormedEventPacket;
import org.electrocodeogram.logging.LogHelper;
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
     * This object represents the events that are defined in the
     * <em>ModuleDescription</em>. These are displayed in the GUI
     * dialog.
     */
    ManualAnnotatorEvents events;

    /**
     * A reference to the dialog.
     */
    private ManualAnnotatorFrame frame;

    /**
     * Crates the <em>Eventreader</em>.
     * @param sourceModule Is the module to pass the events to.
     */
    public ManualReader(SourceModule sourceModule) {
        super(sourceModule);

        logger.entering(this.getClass().getName(), "ManualReader",
            new Object[] {sourceModule});

        this.eventBuffer = new EventQueue();

        ModuleProperty[] runtimeProperties = sourceModule
            .getRuntimeProperties();

        for (ModuleProperty property : runtimeProperties) {
            if (property.getName().equals("Events")) {
                this.events = new ManualAnnotatorEvents(property.getValue());
            }
        }

        this.frame = new ManualAnnotatorFrame(this);

        this.frame.setVisible(true);

        logger.exiting(this.getClass().getName(), "ManualReader");

    }

    /**
     * @see org.electrocodeogram.module.source.EventReader#read()
     */
    @Override
    public WellFormedEventPacket read() throws EventReaderException {

        return this.eventBuffer.remove();

    }

    /**
     * This is the GUI dialog.
     *
     */
    private static class ManualAnnotatorFrame extends JFrame {

        /**
         * The <em>seriialization</em> id. 
         */
        private static final long serialVersionUID = 9081596703350543307L;
        
        /**
         * A reference to the <em>EventReader</em>.
         */
        private ManualReader reader;

        /**
         * Creates the GUI dialog.
         * @param eventReader A reference to the <em>EventReader</em>
         */
        public ManualAnnotatorFrame(ManualReader eventReader) {
            this.reader = eventReader;

            this.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

            this.setTitle("Manual Annotation");

            this.setBounds(10, 10, 200, 200);

            this.setLayout(new FlowLayout());

            this.setEvents(this.reader.events.getEvents());

            this.pack();

            this.setVisible(true);

        }

        /**
         * Creates a button for every event that is defined in the <em>ModuleDescription</em>.
         * @param events The events from the <em>ModuleDescription</em>
         */
        public void setEvents(String[] events) {
            for (String event : events) {
                if (event != null && !event.equals("")) {
                    JButton btnEvent = new JButton(event);

                    btnEvent.addActionListener(new EventActionAdapter(event,
                        this.reader));

                    this.getContentPane().add(btnEvent);
                }
            }
        }

        /**
         * Is reacting on the button-pushs.
         *
         */
        private static class EventActionAdapter implements ActionListener {

            /**
             * The name of the event.
             */
            private String eventName;

            /**
             * A reference to the <em>EventReader</em>.
             */
            private ManualReader reader;

            /**
             * Creates the <em>EventActionAdapter</em>.
             * @param event The name of the event, that has been submitted by the user
             * @param manualReader The reference to the <em>EventReader</em>
             */
            public EventActionAdapter(String event, ManualReader manualReader) {
                this.eventName = event;

                this.reader = manualReader;
            }

            /**
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             */
            @SuppressWarnings("synthetic-access")
            public void actionPerformed(@SuppressWarnings("unused")
            ActionEvent e) {
                String microActivity = "<?xml version=\"1.0\"?><microActivity><manual><value>"
                                       + this.eventName
                                       + "</value></manual></microActivity>";

                String[] args = {"add", "msdt.manual.xsd", microActivity};

                try {
                    WellFormedEventPacket packet = new WellFormedEventPacket(
                        new Date(),
                        "Activity", Arrays.asList(args));

                    this.reader.eventBuffer.add(packet);
                } catch (IllegalEventParameterException e1) {
                    // is checked before 
                }
            }

        }
    }

    /**
     * This is a buffer for the events that are submittted by the user.
     * As the {@link ManualReader#read()} method is continously reading events,
     * the <em>EventReader</em> has to <em>wait</em> until the user
     * has submitted a new event.<br>
     * This causes the {@link ManualReader#read()} method to block.
     * 
     */
    private static class EventQueue {

        /**
         * Is the logger.
         */
        private Logger eventQueueLogger = LogHelper
            .createLogger(EventQueue.class.getName());

        /**
         * Events are stored in this list.
         */
        private ArrayList < WellFormedEventPacket > queue;

        /**
         * Creates the buffer.
         *
         */
        public EventQueue() {

            this.eventQueueLogger.entering(this.getClass().getName(),
                "EventQueue");

            this.queue = new ArrayList < WellFormedEventPacket >();

            this.eventQueueLogger.exiting(this.getClass().getName(),
                "EventQueue");
        }

        /**
         * This adds a event after the user has pushed the event's button.
         * @param packet Is the event
         */
        public void add(WellFormedEventPacket packet) {

            this.eventQueueLogger.entering(this.getClass().getName(), "add",
                new Object[] {packet});

            synchronized (this) {
                this.queue.add(packet);

                this.eventQueueLogger.log(Level.FINE,
                    "Added a packet... Size is " + this.queue.size());

                notifyAll();
            }

            this.eventQueueLogger.exiting(this.getClass().getName(), "add");
        }

        /**
         * Retruns and removes the first event from the buffer.
         * @return The first event from the buffer
         * @throws EventReaderException If this method is interrupted while waiting for the buffer to fill.
         */
        public WellFormedEventPacket remove() throws EventReaderException {

            this.eventQueueLogger.entering(this.getClass().getName(), "remove");

            synchronized (this) {

                if (this.queue.size() > 0) {

                    this.eventQueueLogger.log(Level.INFO,
                        "Remove a packet... Size is " + this.queue.size());

                    WellFormedEventPacket toReturn = this.queue.remove(0);

                    this.eventQueueLogger.exiting(this.getClass().getName(),
                        "remove", toReturn);

                    return toReturn;

                }

                try {

                    this.eventQueueLogger.log(Level.INFO,
                        "Wating to remove a packet... Size is "
                                        + this.queue.size());

                    wait();

                    WellFormedEventPacket toReturn = this.queue.remove(0);

                    this.eventQueueLogger.exiting(this.getClass().getName(),
                        "remove", toReturn);

                    return toReturn;

                } catch (InterruptedException e) {

                    this.eventQueueLogger.exiting(this.getClass().getName(),
                        "remove");

                    throw new EventReaderException(
                        "The EventReader has been interrupted while waiting for events to be submittet by the user.");

                }

            }

        }

    }

    /**
     * 
     */
    public void showDialog() {

        if (this.frame == null) {
            this.frame = new ManualAnnotatorFrame(this);
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
}

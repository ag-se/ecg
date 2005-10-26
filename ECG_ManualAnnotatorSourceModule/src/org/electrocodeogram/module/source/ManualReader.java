/**
 * 
 */
package org.electrocodeogram.module.source;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JFrame;

import org.electrocodeogram.event.IllegalEventParameterException;
import org.electrocodeogram.event.WellFormedEventPacket;
import org.electrocodeogram.module.ModuleProperty;



/**
 *
 */
public class ManualReader extends EventReader {

    private ArrayList<WellFormedEventPacket> eventBuffer;
    
    ManualAnnotatorEvents events;
    
    private ManualAnnotatorFrame frame;
    
    /**
     * @param sourceModule
     */
    public ManualReader(SourceModule sourceModule) {
        super(sourceModule);
        
        this.eventBuffer = new ArrayList<WellFormedEventPacket>();
        
        ModuleProperty[] runtimeProperties = sourceModule.getRuntimeProperties();

        for (ModuleProperty property : runtimeProperties) {
            if (property.getName().equals("Events")) {
                this.events = new ManualAnnotatorEvents(property.getValue());
            }
        }
        
        this.frame = new ManualAnnotatorFrame(this);
        
        this.frame.setVisible(true);
           
    }

    /**
     * @see org.electrocodeogram.module.source.EventReader#read()
     */
    @Override
    public WellFormedEventPacket read() throws EventReaderException {
       
        return null;
    }
    
    private static class ManualAnnotatorFrame extends JFrame {

        private ManualReader reader;

        public ManualAnnotatorFrame(ManualReader eventReader) {
            this.reader = eventReader;

            this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

            this.setTitle("Manual Annotation");

            this.setBounds(10, 10, 200, 200);

            this.setLayout(new FlowLayout());

            this.setEvents(this.reader.events.getEvents());

            this.pack();

            this.setVisible(true);

        }

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

        private static class EventActionAdapter implements ActionListener {

            private String _event;

            private ManualReader reader;

            public EventActionAdapter(String event,
                ManualReader manualReader) {
                this._event = event;

                this.reader = manualReader;
            }

            /**
             * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
             */
            public void actionPerformed(ActionEvent e) {
                String microActivity = "<?xml version=\"1.0\"?><microActivity><manual><value>"
                                       + this._event
                                       + "</value></manual></microActivity>";

                String[] args = {"add", "MicroActivity", microActivity};

                try {
                    WellFormedEventPacket packet = new WellFormedEventPacket(
                        this.reader.getSourceModule().getId(), new Date(), "Activity",
                        Arrays.asList(args));

                    this.reader.eventBuffer.add(packet);
                } catch (IllegalEventParameterException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }

        }
    }

}

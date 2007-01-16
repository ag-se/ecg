package org.electrocodeogram.module.source.implementation;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.StringTokenizer;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

/**
 * This is the GUI dialog.
 *
 */
public class ManualAnnotatorFrame extends JFrame {

    /**
     * The <em>serialization</em> id. 
     */
    private static final long serialVersionUID = 9081596703350543307L;
    
    /**
     * This field holds the user typed name of the current project.
     */
    private JTextField txtProjects;

    /**
     * This field holds a remark for the event/episode.
     */
    private JTextArea txtRemark;
    
    /**
     * A reference to the <em>EventReader</em>.
     */
    private ManualReader reader;

    /**
     * Holds the latest selected episode
     */
    private String currentEpisode = null;

    /**
     * Creates the GUI dialog.
     * @param eventReader A reference to the <em>EventReader</em>
     */
    public ManualAnnotatorFrame(ManualReader eventReader, String events, String episodes) {
        this.reader = eventReader;

        this.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        this.setTitle("Manual Annotation");
        //this.setBounds(10, 10, 200, 200);
        this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));

        this.txtProjects = new JTextField(20);
        this.getContentPane().add(txtProjects);
        
        createEpisodesControls(episodes);
        createEventControls(events);
        
        this.txtRemark = new JTextArea(10,10);
        this.getContentPane().add(txtRemark);
        
        this.pack();
    }
    
    /**
     * @return contents of project text field
     */
    public String getCurrentProject() {
        return this.txtProjects.getText();
    }

    /**
     * @return contents of remark text area
     */
    public String getCurrentRemark() {
        return this.txtRemark.getText();
    }

    /**
     * Deletes remark area
     */
    public void resetRemark() {
        this.txtRemark.setText("");
    }

    /**
     * Creates a button for every event that is defined in the <em>ModuleDescription</em>.
     * @param events The events from the <em>ModuleDescription</em>
     */
    public void createEventControls(String events) {

        StringTokenizer stringTokenizer = new StringTokenizer(events, ",");

        ActionListener actionListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JButton b = (JButton)e.getSource();
                ManualAnnotatorFrame.this.reader.sendManualEvent(b.getText());
            }            
        };
        
        int tokensCount = stringTokenizer.countTokens();
        for (int i = 0; i < tokensCount; i++) {
            String event = stringTokenizer.nextToken().trim();
            if (event != null && !event.equals("")) {
                JButton btnEvent = new JButton(event);
                btnEvent.addActionListener(actionListener);
                this.getContentPane().add(btnEvent);
            }
        }
    }

    /**
     * Creates a ComboBox with entries every episode that is defined in the <em>ModuleDescription</em>.
     * @param episodes The episodes from the <em>ModuleDescription</em>
     */
    public void createEpisodesControls(String episodes) {

        JComboBox cbbEpisodes = new JComboBox();

        StringTokenizer stringTokenizer = new StringTokenizer(episodes, ",");
        int tokensCount = stringTokenizer.countTokens();
        for (int i = 0; i < tokensCount; i++) {
            String episode = stringTokenizer.nextToken().trim();
            if (episode != null && !episode.equals("")) {
                cbbEpisodes.addItem(episode);
            }
        }

        this.getContentPane().add(cbbEpisodes);
        this.currentEpisode = cbbEpisodes.getItemAt(0).toString();

        cbbEpisodes.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() != ItemEvent.SELECTED)
                    return;
                JComboBox cb = (JComboBox)e.getSource();
                ManualAnnotatorFrame.this.reader.sendManualEpisode(ManualAnnotatorFrame.this.currentEpisode);
                ManualAnnotatorFrame.this.currentEpisode = cb.getSelectedItem().toString();
            }
        });

    }

    /**
     * Apart from disposing, the current episode is reported as finished
     * @see java.awt.Window#dispose()
     */
    public void dispose() {
        this.reader.sendManualEpisode(this.currentEpisode);
        super.dispose();
    }


}
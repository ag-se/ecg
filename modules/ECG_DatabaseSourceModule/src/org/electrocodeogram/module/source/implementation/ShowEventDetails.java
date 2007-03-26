/**
 * 
 */
package org.electrocodeogram.module.source.implementation;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * @author jule
 *
 */
public class ShowEventDetails extends JFrame {
    
    /**
     * 
     */
    private static final long serialVersionUID = 3831446218751164246L;
    
    JScrollPane scrollPane;
    
    public ShowEventDetails(JPanel detailPanel){
        scrollPane = new JScrollPane(detailPanel);
        this.getContentPane().add(scrollPane);
        this.setSize(600,400);
        this.setVisible(true);
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }
    
  
    
}

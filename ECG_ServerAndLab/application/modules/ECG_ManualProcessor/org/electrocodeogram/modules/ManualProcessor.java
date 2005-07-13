/*
 * Created on 02.05.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.electrocodeogram.modules;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JFrame;

import org.electrocodeogram.EventPacket;

import org.electrocodeogram.module.source.Source;

/**
 * @author 7oas7er
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ManualProcessor extends Source
{

    
    private ManualProcessorUI ui = null;
    
    public ManualProcessor()
    {
        super();
        
        initialize();
    }

    private void initialize()
    {
        ui = new ManualProcessorUI();
    }
    
    private class ManualProcessorUI extends JFrame
    {
       
        public ManualProcessorUI()
        {
            super();

            setBounds(100,100,400,400);
            
            setTitle("Manueller Ereigniserzeuger");
            
            JButton btnTest = new JButton("Testereignis");
            
            btnTest.addActionListener(new ActionListener(){

                public void actionPerformed(ActionEvent e)
                {
                    String[] args = new String[] {EventPacket.ECG_TYPE_PREFIX + "Test","manuell"};
                   
                    EventPacket eventPacket = new EventPacket(getId(),new Date(),"Activity",Arrays.asList(args));
                    
                    append(eventPacket);
                    
                }});
            
            getContentPane().add(btnTest);
            
            setVisible(true);
            
            setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        }
   
    }
}

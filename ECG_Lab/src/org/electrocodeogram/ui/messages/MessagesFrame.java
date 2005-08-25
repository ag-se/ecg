/*
 * Created on 11.04.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.electrocodeogram.ui.messages;

import java.awt.Color;
import java.awt.GridLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


import org.electrocodeogram.module.registry.ModuleInstanceException;
import org.electrocodeogram.module.registry.ModuleRegistry;

import org.electrocodeogram.msdt.EventValidator;
import org.electrocodeogram.system.SystemRoot;

/**
 * @author 7oas7er
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MessagesFrame extends JFrame implements MessagesTarget
{

    // TODO : filter message types
    
    private JPanel pnlMessages;

    private JScrollPane scrollPane;

    private JTextArea textArea;

    private boolean shouldScroll = false;
    
    private TitledBorder titledBorder = null;
    
    private static final String NOTHING_SELECTED = "Kein Modul ausgewählt";
    
    private  static final String MODULE_SELECTED = "Ereignisstrom durch das Modul: ";

    public void append(String text)
    {
        textArea.append(text);

        JScrollBar vertBar = scrollPane.getVerticalScrollBar();
        if (vertBar.getValue() == vertBar.getMaximum() - vertBar.getVisibleAmount()) {
            shouldScroll = true;
        }
    }

    public MessagesFrame()
    {

        this.setTitle("Ereignisfenster");

        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        this.setBounds(0, 0, 400, 300);

        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setLineWrap(true);

        scrollPane = new JScrollPane(textArea);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().getModel().addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e)
            {
                if (shouldScroll) {
                    JScrollBar vertBar = scrollPane.getVerticalScrollBar();
                    vertBar.setValue(vertBar.getMaximum());
                    shouldScroll = false;
                }

            }
        });

        titledBorder = new TitledBorder(new LineBorder(
                new Color(0, 0, 0)), NOTHING_SELECTED);
        
        pnlMessages = new JPanel(new GridLayout(1, 1));
        pnlMessages.setBorder(titledBorder);
        pnlMessages.add(scrollPane);

        this.getContentPane().add(pnlMessages);

        SystemRoot.getSystemInstance().getGui().getGuiEventWriter().setTarget(this);
    }

    public void setSelectedModul(int moduleId)
    {
        if(moduleId == -1)
        {
            this.titledBorder.setTitle(NOTHING_SELECTED);
        }
        else
        {
            
                try {
                    this.titledBorder.setTitle(MODULE_SELECTED + SystemRoot.getSystemInstance().getSystemModuleRegistry().getRunningModule(moduleId).getName());
                }
                catch (ModuleInstanceException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
           
        }
        repaint();
    }
}
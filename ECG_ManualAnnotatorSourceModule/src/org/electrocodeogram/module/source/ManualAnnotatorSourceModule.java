package org.electrocodeogram.module.source;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JFrame;

import org.electrocodeogram.event.IllegalEventParameterException;
import org.electrocodeogram.event.WellFormedEventPacket;
import org.electrocodeogram.logging.LogHelper;
import org.electrocodeogram.module.ModuleProperty;
import org.electrocodeogram.module.ModulePropertyException;

/**
 * 
 */
public class ManualAnnotatorSourceModule extends SourceModule {

    private Logger logger = LogHelper
        .createLogger(ManualAnnotatorSourceModule.class.getName());

    

    /**
     * @param arg0
     * @param arg1
     */
    public ManualAnnotatorSourceModule(String arg0, String arg1) {
        super(arg0, arg1);

    }

    /**
     * @param propertyName
     * @param propertyValue
     * @throws ModulePropertyException
     */
    @Override
    public void propertyChanged(ModuleProperty moduleProperty)
    {
//        throws ModulePropertyException {
//        if (moduleProperty.getName().equals("Show Dialog")) {
//            if (this.frame == null) {
//                this.frame = new ManualAnnotatorFrame(this);
//            }
//
//            this.frame.setVisible(true);
//        }
    }

    public void update() {

    }

    /**
     * @see org.electrocodeogram.module.Module#initialize()
     */
    @Override
    public void initialize() {
        
       

    }

   

    /**
     * @see org.electrocodeogram.module.source.SourceModule#getEventReader()
     */
    @Override
    public EventReader[] getEventReader() {
        return new EventReader[] {new ManualReader(this)};
    }

    /**
     * @see org.electrocodeogram.module.source.SourceModule#preStart()
     */
    @Override
    public void preStart() {

    }

    /**
     * @see org.electrocodeogram.module.source.SourceModule#postStop()
     */
    @Override
    public void postStop() {
        
    }
}

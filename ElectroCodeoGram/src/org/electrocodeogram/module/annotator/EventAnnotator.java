/*
 * Created on 10.03.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.electrocodeogram.module.annotator;

import java.util.Observable;

import org.electrocodeogram.core.EventPacket;
import org.electrocodeogram.module.Module;


/**
 * @author 7oas7er
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public abstract class EventAnnotator extends Module
{
    public static final int PRE_ANNOTATION = 0;
    
    public static final int POST_ANNOTATION = 1;
    
    private String separator = "";
    
    private int annnotationType = -1;

    /**
     * @param name
     */
    public EventAnnotator(int annotationType, String separator,  String name)
    {
        super(Module.INTERMEDIATE_MODULE,name);
        
        this.annnotationType = annotationType;
        
        this.separator = separator;
    }

    public void update(Observable o, Object arg)
    {
        if(arg instanceof EventPacket)
        {
            EventPacket eventPacket = (EventPacket) arg;
            EventPacket annotationPacket = getAnnotation(eventPacket);
            
            if(annnotationType == EventAnnotator.PRE_ANNOTATION)
            {
                processEventPacket(annotationPacket);
                processEventPacket(eventPacket);

            }
            else
            {
                processEventPacket(eventPacket);
                processEventPacket(annotationPacket);
            }
        }
    }

    /**
     * @param str
     */
    private EventPacket getAnnotation(EventPacket eventPacket)
    {
    
        return annotate(eventPacket);
        
    }

    /**
     * @param str
     * @return
     */
    public abstract EventPacket annotate(EventPacket eventPacket);
        
}

package org.electrocodeogram.module.source.implementation;

import java.util.StringTokenizer;
import java.util.logging.Logger;

import org.electrocodeogram.logging.LogHelper;

/**
 * This is the representation for the events that are defined
 * in the <em>ModuleDescription<em>.
 *
 */
public class ManualAnnotatorEvents {

    /**
     * This is the logger.
     */
    private static Logger logger = LogHelper.createLogger(ManualAnnotatorEvents.class
        .getName());
    
    /**
     * The event names.
     */
    private String[] events;

    /**
     * Creates the <em>ManualAnnotatorEvents</em> object.
     * @param value Is the comma separated event name <code>String</code> 
     */
    public ManualAnnotatorEvents(final String value) {
        
        logger.entering(this.getClass().getName(),"ManualAnnotatorEvents", new Object[] {value});
        
        if (value == null || value.equals("")) {
            
            logger.exiting(this.getClass().getName(),"ManualAnnotatorEvents");
            
            return;
        }

        StringTokenizer stringTokenizer = new StringTokenizer(value, ",");

        this.events = new String[stringTokenizer.countTokens()];

        for (int i = 0; i < this.events.length; i++) {
            this.events[i] = stringTokenizer.nextToken();
        }
        
        logger.exiting(this.getClass().getName(),"ManualAnnotatorEvents");
    }

    /**
     * Returns the events that are defined in the <em>ModuleDescriptiony</em>.
     * @return The events that are defined in the <em>ModuleDescriptiony</em>
     */
    public String[] getEvents() {
        
        logger.entering(this.getClass().getName(),"getEvents");
        
        
        logger.exiting(this.getClass().getName(),"getEvents", this.events);
        
        return this.events;
    }
}

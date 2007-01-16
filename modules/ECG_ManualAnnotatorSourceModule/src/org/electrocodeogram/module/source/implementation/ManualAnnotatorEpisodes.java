package org.electrocodeogram.module.source.implementation;

import java.util.StringTokenizer;
import java.util.logging.Logger;

import org.electrocodeogram.logging.LogHelper;

/**
 * This is the representation for the episodes that are defined
 * in the <em>ModuleDescription<em>.
 *
 */
public class ManualAnnotatorEpisodes {

    /**
     * This is the logger.
     */
    private static Logger logger = LogHelper.createLogger(ManualAnnotatorEpisodes.class
        .getName());
    
    /**
     * The event names.
     */
    private String[] episodes;

    /**
     * Creates the <em>ManualAnnotatorEvents</em> object.
     * @param value Is the comma separated event name <code>String</code> 
     */
    public ManualAnnotatorEpisodes(final String value) {
        
        logger.entering(this.getClass().getName(),"ManualAnnotatorEpisodes", new Object[] {value});
        
        if (value == null || value.equals("")) {
            
            logger.exiting(this.getClass().getName(),"ManualAnnotatorEpisodes");
            
            return;
        }

        StringTokenizer stringTokenizer = new StringTokenizer(value, ",");

        this.episodes = new String[stringTokenizer.countTokens()];

        for (int i = 0; i < this.episodes.length; i++) {
            this.episodes[i] = stringTokenizer.nextToken();
        }
        
        logger.exiting(this.getClass().getName(),"ManualAnnotatorEvents");
    }

    /**
     * Returns the episodes that are defined in the <em>ModuleDescriptiony</em>.
     * @return The episodes that are defined in the <em>ModuleDescriptiony</em>
     */
    public String[] getEpisodes() {
        
        logger.entering(this.getClass().getName(),"getEpisodes");
        
        
        logger.exiting(this.getClass().getName(),"getEpisodes", this.episodes);
        
        return this.episodes;
    }
}

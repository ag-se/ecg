/**
 * 
 */
package org.electrocodeogram.module.intermediate.implementation;

import java.util.Collection;
import java.util.Collections;

import org.electrocodeogram.event.ValidEventPacket;

/**
 * @author jekutsch
 *
 */
public abstract class AbstractSingleEpisodeRecognizer implements
        EpisodeRecognizer {

    /**
     * Implemented to return only one or no episode
     *  
     * @see org.electrocodeogram.module.intermediate.implementation.EpisodeRecognizer#analyse(org.electrocodeogram.event.ValidEventPacket, long)
     */
    public Collection<ValidEventPacket> analyse(ValidEventPacket packet,
            long minDuration) {
        ValidEventPacket e = analyseSingle(packet, minDuration);
        if (e == null)
            return null;
        return Collections.singleton(e);
    }
    
    /**
     * Works like analyse() but is designed to return only one single episode or none. It is
     * a convinience method for providing single episode emitting recognizers,
     * 
     * @param packet an event packet just like in class IntermediateModule
     * @param minDuration minimal duration of an emitted episode, in ms 
     * @return One or no episode event
     * 
     * @see org.electrocodeogram.module.intermediate.implementation.EpisodeRecognizer#analyse(org.electrocodeogram.event.ValidEventPacket, long)
     */
    abstract public ValidEventPacket analyseSingle(ValidEventPacket packet, long minDuration);

}

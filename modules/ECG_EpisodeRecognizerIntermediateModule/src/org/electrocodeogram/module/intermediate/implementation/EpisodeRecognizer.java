/*
 * (c) Freie Universität Berlin - AG SoftwareEngineering - 2006
 */

package org.electrocodeogram.module.intermediate.implementation;

import org.electrocodeogram.event.ValidEventPacket;

/**
 * Interface for implementing Recognizers to be registered with 
 * EpisodeRecognizerIntermediateModule.
 * 
 * It defines minimal implementation requirements for Episode recognizers. 
 * Usually, implementations should also implement the equals() method 
 * since it is been used in the recognizer management in 
 * EpisodeRecognizerManager. The Manager also dynamically creates instances 
 * of each registered Recognizers therefore a default constructor must be 
 * provided.
 *
 */
public interface EpisodeRecognizer {

	/**
     * Indicates whether this recognizer instance is still in its initial state.
     * 
     * A recognizer is still in its initial state if it didn't 
     * react to any of the events provided in analyse() yet.
     * 
	 * @return true, if recognizer is in initial state, false otherwise
	 */
	boolean isInInitialState();
	
    /**
     * Idicates whether this recognizer instance is in its final state.
     * 
     * A recognizer is in final state if it won't react to any call of
     * analyse() any more.
     * 
     * @return true, if recognizer is in final state, false otherwise
     */
	boolean isInFinalState();
	
    /**
     * Processes next event 
     * 
     * anaylse() is been called each time, the next event
     * from the stream must be processed. The recognizer is expected to react to
     * this event in in of three kinds:
     * <li> ignore it if event is of no interest to this recognizer, return null
     * <li> react internally by changing state (i.e. altering internal
     * attributes) but do not emit a new episode, return null
     * <li> react to this event by emitting a newly recognized episode, return
     * this episode as a special event. 
     *
     * An episode should not be emitted if its duration would be less than 
     * minDuration. Emitting an episode event often but not always results in 
     * a final state.
     * 
     * @param packet an event packet just like in class IntermediateModule
     * @param minDuration minimal duration of an emitted episode, in ms 
     * @return a new episode-like event, if one has been detected, null
     *         otherwise
     */
    ValidEventPacket analyse(ValidEventPacket packet, long minDuration);

}

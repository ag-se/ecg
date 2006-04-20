/*
 * Freie Universität Berlin, 2006
 */

package org.electrocodeogram.ui.event;

import org.electrocodeogram.event.EventPacket;

/**
 * Holds an event which has been processed by a module. Used for displaying 
 * (e.g. EventWindow) and logging purposes.
 *
 */
public class ProcessedEventPacket {

	/**
     * Every module is handling <code>ValidEventPackets</code> twice. It receives
     * them and creates a new one that is then sent to other
     * modules. The DELIVERY_STATE as told by
     * {@link ProcessedEventPacket#getDeliveryState()} says if the event
     * is the received one or if it is the sent one. This is actually
     * only used in the GUI to toggle which events to display.
     */
    public enum DELIVERY_STATE {
        /**
         * The event was sent by a module.
         */
        SENT,

        /**
         * The event was received by a module.
         */
        RECEIVED;
    }

    /**
     * The <em>DELIVERY_STATE</em> of this event.
     */
    private DELIVERY_STATE deliveryState = null;

    /**
     * The original EventPacket that has been processed.
     */
	private EventPacket eventPacket;
	
    /**
     * The unique int id of the module that processed this event at
     * last.
     */
    private int sourceId = -1;

	/**
	 * Constructor
	 * 
	 * @param sourceId
	 * @param eventPacket
	 * @param deliveryState
	 */
	public ProcessedEventPacket(int sourceId, EventPacket eventPacket, DELIVERY_STATE deliveryState) {
		this.sourceId = sourceId;
		this.eventPacket = eventPacket;
		this.deliveryState = deliveryState;
	}

	/**
	 * @return the deliveryState
	 */
	public DELIVERY_STATE getDeliveryState() {
		return deliveryState;
	}

	/**
	 * @param deliveryState the deliveryState to set
	 */
	public void setDeliveryState(DELIVERY_STATE deliveryState) {
		this.deliveryState = deliveryState;
	}

	/**
	 * @return the sourceId
	 */
	public int getSourceId() {
		return sourceId;
	}

	/**
	 * @param sourceId the sourceId to set
	 */
	public void setSourceId(int sourceId) {
		this.sourceId = sourceId;
	}

	/**
	 * @return the eventPacket
	 */
	public EventPacket getEventPacket() {
		return eventPacket;
	}

    /* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return eventPacket.toString();
	}

    
}

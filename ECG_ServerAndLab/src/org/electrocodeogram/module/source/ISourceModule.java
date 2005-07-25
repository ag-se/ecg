package org.electrocodeogram.module.source;

import org.electrocodeogram.event.ValidEventPacket;

/**
 * @author 7oas7er
 *
 */
public interface ISourceModule
{
    /**
     * @param eventPacket
     */
    public void append(ValidEventPacket eventPacket);
}

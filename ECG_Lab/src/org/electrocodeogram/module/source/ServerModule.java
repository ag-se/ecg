/*
 * Class: ServerModule
 * Version: 1.0
 * Date: 19.10.2005
 * By: Frank@Schlesinger.com
 */

package org.electrocodeogram.module.source;

/**
 * This interface is just a marker interface for {@link org.electrocodeogram.module.source.SourceModule} implementations.
 * It indicates that the <em>SourceModule</em> is a kind of a server and
 * wants to control the starting and stopping of its {@link org.electrocodeogram.module.source.EventReader}
 * by itself.
 * Normally the <em>EventReader</em> that are implemented for a <em>SourceModule</em>
 * are started when the module becomes active. But for a server this does not hold.
 * A server will start his <em>EventReader</em>, when an incoming connection request
 * is made.
 */
public interface ServerModule {

    // just a merker interface
}

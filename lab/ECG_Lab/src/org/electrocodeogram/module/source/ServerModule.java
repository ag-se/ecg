/*
 * Class: ServerModule
 * Version: 1.0
 * Date: 19.10.2005
 * By: Frank@Schlesinger.com
 */

package org.electrocodeogram.module.source;

/**
 * This interface is just a marker interface for {@link org.electrocodeogram.module.source.SourceModule} implementations.
 * It indicates that the <code>SourceModule</code> is kind of a server and
 * wants to control the starting and stopping of its {@link org.electrocodeogram.module.source.EventReader}
 * on its own.
 * Normally the <code>EventReader</code> that are implemented for a <code>SourceModule</code>
 * are started when the module becomes active. But for a server this does not hold.
 * A server will start his <code>EventReader</code>, when an incoming connection request
 * is made.
 */
public interface ServerModule {

    // just a merker interface
}

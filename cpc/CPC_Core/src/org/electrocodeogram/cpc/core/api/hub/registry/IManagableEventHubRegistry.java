package org.electrocodeogram.cpc.core.api.hub.registry;


import org.electrocodeogram.cpc.core.registry.DefaultEventHubRegistry;


/**
 * Management extension to the {@link IEventHubRegistry} interface.
 * <p>
 * All {@link IEventHubRegistry} implementation need to implement this interface too.
 * <p>
 * Methods of this interface may only be used by the <em>CPC Core</em> module.
 * 
 * @author vw
 * 
 * @see IEventHubRegistry
 * @see DefaultEventHubRegistry
 */
public interface IManagableEventHubRegistry extends IEventHubRegistry
{
	/**
	 * Called when the event hub registry is being shut down.
	 * <br>
	 * This typically only happens when the Eclipse IDE is being shutdown.
	 * <p>
	 * A registry implementation should not depend on a call to this method as there
	 * might be shutdown scenarios in which the <em>CPC Core</em> module is unable
	 * to call this method in time.
	 */
	public void shutdown();

}

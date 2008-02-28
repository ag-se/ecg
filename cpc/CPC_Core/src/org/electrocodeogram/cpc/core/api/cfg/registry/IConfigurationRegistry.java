package org.electrocodeogram.cpc.core.api.cfg.registry;


import java.util.Set;

import org.electrocodeogram.cpc.core.CPCCorePlugin;


/**
 * A special, global CPC Core registry for configuration data.
 * <p>
 * An instance can be obtained from {@link CPCCorePlugin#getConfigurationRegistry()}.
 * 
 * @author vw
 * 
 * @see CPCCorePlugin
 */
public interface IConfigurationRegistry
{
	/**
	 * Retrieves a list of all file types which are supported by the currently
	 * installed CPC modules.
	 * <br>
	 * The value is used to filter out events for file types which are of no interest
	 * to any CPC module. This is done to improve performance.
	 * <p>
	 * File types are <u>lower case</u> file extensions as seen on the filesystem, <u>not including the dot</u>.
	 * I.e.:
	 * <ul>
	 * 	<li>"java"</li>
	 *  <li>"cpp"</li>
	 * </ul>
	 * <b>NOTE:</b> The returned {@link Set} implementation should be chosen to support efficient {@link Set#contains(Object)}
	 * lookups.
	 * 
	 * @return a list of file types which should be taken into account by CPC modules, never null.
	 */
	public Set<String> getSupportedFileTypes();
}

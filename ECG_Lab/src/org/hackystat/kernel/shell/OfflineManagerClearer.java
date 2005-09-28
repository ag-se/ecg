/**
 * 
 */
package org.hackystat.kernel.shell;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.electrocodeogram.logging.LogHelper;

/**
 *
 */
public class OfflineManagerClearer
{

	private static Logger _logger = LogHelper.createLogger(OfflineManagerClearer.class.getName());
	
	public static void clearOfflineManager()
	{
		_logger.entering(OfflineManager.class.getName(),"clearOfflineManager");
		
		OfflineManager.getInstance().clear();
		
		_logger.log(Level.INFO,"HackyStat SensorShell's OfflineManager hasd been cleared.");
		
		_logger.exiting(OfflineManager.class.getName(),"clearOfflineManager");
	}
	
}

package org.electrocodeogram.cpc.optimiser.task;


import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;


/**
 * Generic interface which is implemented by all <em>CPC Optimiser</em> tasks.
 *  
 * @author vw
 */
public interface IOptimiserTask
{
	/**
	 * Prepares the task/job for execution on the given projects.<br/>
	 * If the parameter is null, all projects are included.
	 * 
	 * @param projects list of projects to execute this task on, NULL for all.
	 * @param optionsMap optional map with configuration options for this task, may be NULL.
	 */
	public void init(List<IProject> projects, Map<String, String> optionsMap) throws OptimiserTaskException,
			InterruptedException;
}

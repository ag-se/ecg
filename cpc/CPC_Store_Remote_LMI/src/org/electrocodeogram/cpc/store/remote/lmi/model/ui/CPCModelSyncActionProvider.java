package org.electrocodeogram.cpc.store.remote.lmi.model.ui;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.team.ui.mapping.SynchronizationActionProvider;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;


public class CPCModelSyncActionProvider extends SynchronizationActionProvider
{
	private static final Log log = LogFactory.getLog(CPCModelSyncActionProvider.class);

	public CPCModelSyncActionProvider()
	{
		super();

		log.trace("CPCModelSyncActionProvider()");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.SynchronizationActionProvider#initialize()
	 */
	@Override
	protected void initialize()
	{
		log.trace("initialize()");

		super.initialize();
		final ISynchronizePageConfiguration configuration = getSynchronizePageConfiguration();
		// We provide custom handlers that ensure that the MOD files get updated properly
		// when MOE files are merged.
		//registerHandler(MERGE_ACTION_ID, new ModelMergeActionHandler(configuration, false));
		//registerHandler(OVERWRITE_ACTION_ID, new ModelMergeActionHandler(configuration, true));
		// We can just use the default mark as merged handler
	}
}

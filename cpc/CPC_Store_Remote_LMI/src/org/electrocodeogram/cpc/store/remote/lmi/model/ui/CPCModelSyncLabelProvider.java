package org.electrocodeogram.cpc.store.remote.lmi.model.ui;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.team.ui.mapping.SynchronizationLabelProvider;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;


public class CPCModelSyncLabelProvider extends SynchronizationLabelProvider
{
	private static final Log log = LogFactory.getLog(CPCModelSyncLabelProvider.class);

	private CPCModelNavigatorLabelProvider delegate;

	public CPCModelSyncLabelProvider()
	{
		super();

		log.trace("CPCModelSyncLabelProvider()");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.SynchronizationLabelProvider#init(org.eclipse.ui.navigator.ICommonContentExtensionSite)
	 */
	@Override
	public void init(ICommonContentExtensionSite site)
	{
		if (log.isTraceEnabled())
			log.trace("init() - site: " + site);

		super.init(site);
		delegate = new CPCModelNavigatorLabelProvider();
		delegate.init(site);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.AbstractSynchronizeLabelProvider#dispose()
	 */
	@Override
	public void dispose()
	{
		log.trace("dispose()");

		super.dispose();
		if (delegate != null)
			delegate.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.AbstractSynchronizeLabelProvider#getDelegateLabelProvider()
	 */
	@Override
	protected ILabelProvider getDelegateLabelProvider()
	{
		log.trace("getDelegateLabelProvider()");

		return delegate;
	}

}

package org.electrocodeogram.cpc.store.remote.lmi.model.ui;


import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.mapping.ResourceTraversal;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.team.core.mapping.ISynchronizationContext;
import org.eclipse.team.ui.mapping.SynchronizationContentProvider;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;
import org.eclipse.ui.navigator.IPipelinedTreeContentProvider;
import org.eclipse.ui.navigator.PipelinedShapeModification;
import org.eclipse.ui.navigator.PipelinedViewerUpdate;
import org.electrocodeogram.cpc.store.remote.lmi.model.CPCModelProvider;


public class CPCModelSyncContentProvider extends SynchronizationContentProvider implements
		IPipelinedTreeContentProvider
{
	private static final Log log = LogFactory.getLog(CPCModelSyncContentProvider.class);

	private CPCModelNavigatorContentProvider delegate;

	public CPCModelSyncContentProvider()
	{
		super();

		log.trace("CPCModelSyncContentProvider()");
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.SynchronizationContentProvider#init(org.eclipse.ui.navigator.ICommonContentExtensionSite)
	 */
	@Override
	public void init(ICommonContentExtensionSite site)
	{
		if (log.isTraceEnabled())
			log.trace("init() - site: " + site);

		super.init(site);
		//		delegate = new CPCModelNavigatorContentProvider(getContext() != null);
		//		delegate.init(site);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.SynchronizationContentProvider#dispose()
	 */
	@Override
	public void dispose()
	{
		log.trace("dispose()");

		super.dispose();
		//		if (delegate != null)
		//			delegate.dispose();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.SynchronizationContentProvider#getDelegateContentProvider()
	 */
	@Override
	protected ITreeContentProvider getDelegateContentProvider()
	{
		log.trace("getDelegateContentProvider()");

		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.SynchronizationContentProvider#getModelProviderId()
	 */
	@Override
	protected String getModelProviderId()
	{
		log.trace("getModelProviderId()");

		return CPCModelProvider.PROVIDER_ID;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.SynchronizationContentProvider#getModelRoot()
	 */
	@Override
	protected Object getModelRoot()
	{
		log.trace("getModelRoot()");

		//TODO
		return ResourcesPlugin.getWorkspace().getRoot();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.SynchronizationContentProvider#getTraversals(org.eclipse.team.core.mapping.ISynchronizationContext, java.lang.Object)
	 */
	@Override
	protected ResourceTraversal[] getTraversals(ISynchronizationContext context, Object object)
	{
		if (log.isTraceEnabled())
			log.trace("getTraversals() - context: " + context + ", object: " + object);

		// TODO
		return new ResourceTraversal[0];
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.navigator.IPipelinedTreeContentProvider#getPipelinedChildren(java.lang.Object, java.util.Set)
	 */
	@Override
	public void getPipelinedChildren(Object parent, Set theCurrentChildren)
	{
		if (log.isTraceEnabled())
			log.trace("getPipelinedChildren() - parent: " + parent + ", theCurrentChildren: " + theCurrentChildren);

		// Nothing to do
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.navigator.IPipelinedTreeContentProvider#getPipelinedElements(java.lang.Object, java.util.Set)
	 */
	@Override
	public void getPipelinedElements(Object anInput, Set theCurrentElements)
	{
		if (log.isTraceEnabled())
			log.trace("getPipelinedElements() - anInput: " + anInput + ", theCurrentElements: " + theCurrentElements);

		// TODO

	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.navigator.IPipelinedTreeContentProvider#getPipelinedParent(java.lang.Object, java.lang.Object)
	 */
	@Override
	public Object getPipelinedParent(Object anObject, Object suggestedParent)
	{
		if (log.isTraceEnabled())
			log.trace("getPipelinedParent() - anObject: " + anObject + ", suggestedParent: " + suggestedParent);

		// We're not changing the parenting of any resources
		return suggestedParent;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.navigator.IPipelinedTreeContentProvider#interceptAdd(org.eclipse.ui.navigator.PipelinedShapeModification)
	 */
	@Override
	public PipelinedShapeModification interceptAdd(PipelinedShapeModification anAddModification)
	{
		if (log.isTraceEnabled())
			log.trace("interceptAdd() - anAddModification: " + anAddModification);

		//TODO
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.navigator.IPipelinedTreeContentProvider#interceptRefresh(org.eclipse.ui.navigator.PipelinedViewerUpdate)
	 */
	@Override
	public boolean interceptRefresh(PipelinedViewerUpdate refreshSynchronization)
	{
		if (log.isTraceEnabled())
			log.trace("interceptRefresh() - refreshSynchronization: " + refreshSynchronization);

		// No need to intercept the refresh
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.navigator.IPipelinedTreeContentProvider#interceptRemove(org.eclipse.ui.navigator.PipelinedShapeModification)
	 */
	@Override
	public PipelinedShapeModification interceptRemove(PipelinedShapeModification removeModification)
	{
		if (log.isTraceEnabled())
			log.trace("interceptRemove() - removeModification: " + removeModification);

		// No need to intercept the remove
		return removeModification;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.navigator.IPipelinedTreeContentProvider#interceptUpdate(org.eclipse.ui.navigator.PipelinedViewerUpdate)
	 */
	@Override
	public boolean interceptUpdate(PipelinedViewerUpdate anUpdateSynchronization)
	{
		if (log.isTraceEnabled())
			log.trace("interceptUpdate() - anUpdateSynchronization: " + anUpdateSynchronization);

		// No need to intercept the update
		return false;
	}

}

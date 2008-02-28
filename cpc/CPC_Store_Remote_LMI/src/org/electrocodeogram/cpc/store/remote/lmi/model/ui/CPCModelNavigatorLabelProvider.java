package org.electrocodeogram.cpc.store.remote.lmi.model.ui;


import org.eclipse.ui.IMemento;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;
import org.eclipse.ui.navigator.ICommonLabelProvider;


public class CPCModelNavigatorLabelProvider extends WorkbenchLabelProvider implements ICommonLabelProvider
{

	private ICommonContentExtensionSite extensionSite;

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.navigator.ICommonLabelProvider#init(org.eclipse.ui.navigator.ICommonContentExtensionSite)
	 */
	@Override
	public void init(ICommonContentExtensionSite config)
	{
		extensionSite = config;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.navigator.IMementoAware#restoreState(org.eclipse.ui.IMemento)
	 */
	@Override
	public void restoreState(IMemento memento)
	{
		// Nothing to do
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.navigator.IMementoAware#saveState(org.eclipse.ui.IMemento)
	 */
	@Override
	public void saveState(IMemento memento)
	{
		// Nothing to do
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.navigator.IDescriptionProvider#getDescription(java.lang.Object)
	 */
	@Override
	public String getDescription(Object anElement)
	{
		//		if (anElement instanceof ModelObject) {
		//			return ((ModelObject) anElement).getPath();
		//		}
		//TODO
		return "bla - " + (anElement != null ? anElement.toString() : "null");
	}

	/**
	 * Return the extension site for this label provider.
	 * @return the extension site for this label provider
	 */
	public ICommonContentExtensionSite getExtensionSite()
	{
		return extensionSite;
	}

}

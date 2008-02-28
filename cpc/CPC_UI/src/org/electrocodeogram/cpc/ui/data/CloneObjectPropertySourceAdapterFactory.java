package org.electrocodeogram.cpc.ui.data;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ui.views.properties.IPropertySource;
import org.electrocodeogram.cpc.core.api.data.IClone;


public class CloneObjectPropertySourceAdapterFactory implements IAdapterFactory
{
	private static Log log = LogFactory.getLog(CloneObjectPropertySourceAdapterFactory.class);

	public CloneObjectPropertySourceAdapterFactory()
	{
		log.trace("CloneObjectPropertySourceAdapterFactory()");
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
	 */
	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType)
	{
		if (log.isTraceEnabled())
			log.trace("getAdapter() - adaptableObj: " + adaptableObject + ", adapterType: " + adapterType);

		if (adapterType == IPropertySource.class && adaptableObject instanceof IClone)
		{
			ClonePropertySource result = new ClonePropertySource((IClone) adaptableObject);

			if (log.isTraceEnabled())
				log.trace("getAdapter() - result: " + result);

			return result;
		}

		log.trace("getAdapter() - result: null");

		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Class[] getAdapterList()
	{
		log.trace("getAdapterList()");

		return new Class[] { IPropertySource.class };
	}

}

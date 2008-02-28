package org.electrocodeogram.eclipse.core.logging;


/**
 * Very simple factory implementation which allows apache commons logging code to
 * fall back to our ECG Eclipse Core Plugin logger.
 * 
 * @author vw
 * @deprecated no longer in use
 */
@Deprecated
public class CommonsLogFactoryImpl extends AbstractCommonsLogFactory
{
	//	public CommonsLogFactoryImpl()
	//	{
	//		System.err.println("CommonsLogFactoryImpl()");
	//	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.eclipse.core.logging.AbstractCommonsLogFactory#getPluginLogManager()
	 */
	//	@Override
	//	protected ILogManager getPluginLogManager()
	//	{
	//		return ECGEclipseCorePlugin.getDefault().getPluginLogManager();
	//	}
}

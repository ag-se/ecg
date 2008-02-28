package org.electrocodeogram.eclipse.core.logging;


import org.apache.log4j.Logger;


public interface ILogManager
{
	Logger getLogger(String name);

	JDKStyleLogger getJDKStyleLogger(String name);

	void shutdown();
}

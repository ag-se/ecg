package org.electrocodeogram.cpc.store.local.sql.data;


import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.electrocodeogram.cpc.core.utils.CoreStringUtils;


public class CloneFileContent implements ICloneFileContent
{
	private static final long serialVersionUID = 1L;

	private static Log log = LogFactory.getLog(CloneFileContent.class);

	private String fileUuid = null;
	private String content = null;

	public CloneFileContent()
	{
		super();

		log.trace("CloneFileContent()");
	}

	public CloneFileContent(String fileUuid, String content)
	{
		super();

		assert (fileUuid != null);

		if (log.isTraceEnabled())
			log.trace("CloneFileContent() - fileUuid: " + fileUuid + ", content: " + content);

		this.fileUuid = fileUuid;
		this.content = content;
	}

	/*
	 * ICloneFileContent methods.
	 */

	/* (non-Javadoc)
	 * @see org.electrocodeogram.cpc.store.local.sql.data.ICloneFileContent#getFileUuid()
	 */
	@Override
	public String getFileUuid()
	{
		return fileUuid;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.store.local.sql.data.ICloneFileContent#setFileUuid(java.lang.String)
	 */
	@Override
	public void setFileUuid(String fileUuid)
	{
		if (log.isTraceEnabled())
			log.trace("setFileUuid(): " + fileUuid);

		this.fileUuid = fileUuid;
	}

	/* (non-Javadoc)
	 * @see org.electrocodeogram.cpc.store.local.sql.data.ICloneFileContent#getContent()
	 */
	@Override
	public String getContent()
	{
		return content;
	}

	/* (non-Javadoc)
	 * @see org.electrocodeogram.cpc.store.local.sql.data.ICloneFileContent#setContent(java.lang.String)
	 */
	@Override
	public void setContent(String content)
	{
		if (log.isTraceEnabled())
			log.trace("setContent(): " + content);

		this.content = content;
	}

	/*
	 * IStatefulObject methods.
	 */

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.internal.IStatefulObject#getPersistenceClassIdentifier()
	 */
	@Override
	public String getPersistenceClassIdentifier()
	{
		return PERSISTENCE_CLASS_IDENTIFIER;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.internal.IStatefulObject#getPersistenceObjectIdentifier()
	 */
	@Override
	public String getPersistenceObjectIdentifier()
	{
		return PERSISTENCE_OBJECT_IDENTIFIER;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.internal.IStatefulObject#getState()
	 */
	@Override
	public Map<String, Comparable<? extends Object>> getState()
	{
		log.trace("getState()");

		Map<String, Comparable<? extends Object>> state = new HashMap<String, Comparable<? extends Object>>(2);

		state.put("fileUuid", fileUuid);
		state.put("content", content);

		if (log.isTraceEnabled())
			log.trace("getState() - result: " + state);

		return state;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.internal.IStatefulObject#getStateTypes()
	 */
	@Override
	public Map<String, Class<? extends Object>> getStateTypes()
	{
		log.trace("getStateTypes()");

		Map<String, Class<? extends Object>> state = new HashMap<String, Class<? extends Object>>(2);

		state.put("fileUuid", String.class);
		state.put("content", String.class);

		if (log.isTraceEnabled())
			log.trace("getStateTypes() - result: " + state);

		return state;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.internal.IStatefulObject#setState(java.util.Map)
	 */
	@Override
	public void setState(Map<String, Comparable<? extends Object>> state)
	{
		assert (state != null && state.containsKey("fileUuid"));

		if (log.isTraceEnabled())
			log.trace("setState() - state: " + CoreStringUtils.truncateMap(state));

		fileUuid = (String) state.get("fileUuid");
		content = (String) state.get("content");
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.ICloneDataElement#isSealed()
	 */
	@Override
	public boolean isSealed()
	{
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.data.ICloneDataElement#seal()
	 */
	@Override
	public void seal()
	{
		//we have no need for sealing, this type of object is only used internally.
	}

}

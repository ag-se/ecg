package org.electrocodeogram.cpc.core.api.hub.event;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * @author vw
 *
 * @deprecated this class is no longer in use, please refer to the <em>CPC Track</em> module.
 */
@Deprecated
public class EclipseCodeDiffEvent extends EclipseEvent
{
	private static Log log = LogFactory.getLog(EclipseCodeDiffEvent.class);

	protected String filePath;
	protected int offset = -1;
	protected String addedText;
	protected String replacedText;
	protected String editorContent;

	public EclipseCodeDiffEvent(String user, String project)
	{
		super(user, project);

		log.trace("EclipseCodeDiffEvent(...)");
	}

	/**
	 * @return offset at which the diff starts, starts at 0
	 */
	public int getOffset()
	{
		return offset;
	}

	public void setOffset(int offset)
	{
		if (log.isTraceEnabled())
			log.trace("setOffset(): " + offset);
		assert (offset >= 0);

		checkSeal();

		this.offset = offset;
	}

	/**
	 * @return text which was added in this diff, never null
	 */
	public String getAddedText()
	{
		return addedText;
	}

	public void setAddedText(String addedText)
	{
		if (log.isTraceEnabled())
			log.trace("setAddedText(): " + addedText);
		assert (addedText != null);

		checkSeal();

		this.addedText = addedText;
	}

	/**
	 * @return text which was replaced in this diff, never null
	 */
	public String getReplacedText()
	{
		return replacedText;
	}

	public void setReplacedText(String replacedText)
	{
		if (log.isTraceEnabled())
			log.trace("setReplacedText(): " + replacedText);
		assert (replacedText != null);

		checkSeal();

		this.replacedText = replacedText;
	}

	/**
	 * @return the content of the editor AFTER this event
	 */
	public String getEditorContent()
	{
		return editorContent;
	}

	public void setEditorContent(String editorContent)
	{
		if (log.isTraceEnabled())
			log.trace("setEditorContent(): " + editorContent);
		assert (editorContent != null);

		checkSeal();

		this.editorContent = editorContent;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.hub.event.EclipseEvent#isValid()
	 */
	@Override
	public boolean isValid()
	{
		if (offset < 0)
			return false;

		if (addedText == null || replacedText == null || editorContent == null)
			return false;

		return super.isValid();
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.hub.event.CPCEvent#toString()
	 */
	@Override
	public String toString()
	{
		return "EclipseCodeDiffEvent[" + super.subToString() + ", offset: " + offset + ", addedText: " + addedText
				+ ", replacedText: " + replacedText + /* ", editorContent: " + editorContent +*/"]";
	}
}

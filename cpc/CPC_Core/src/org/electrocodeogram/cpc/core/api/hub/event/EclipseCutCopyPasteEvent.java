package org.electrocodeogram.cpc.core.api.hub.event;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * This event is generated by the <em>CPC Sensor</em> module, whenever the programmer
 * executes a cut, copy or paste operation.
 * <br>
 * Besides the type and position, the event provides information about the current
 * selection, the clipboard and the content of the editor.
 * 
 * @author vw
 */
public class EclipseCutCopyPasteEvent extends EclipseEvent
{
	private static Log log = LogFactory.getLog(EclipseCutCopyPasteEvent.class);

	/**
	 * The possible types of {@link EclipseCutCopyPasteEvent}s. 
	 */
	public enum Type
	{
		/**
		 * A copy operation.
		 */
		COPY,

		/**
		 * A cut operation.
		 */
		CUT,

		/**
		 * A paste operation.
		 */
		PASTE,

		/**
		 * This is a special activity type which must not be sent
		 * to the dispatcher. It's for internal initialisation only. 
		 */
		NULL
	}

	protected Type type;
	protected String selection;
	protected String clipboard;
	protected String editorContent;
	protected int offset = -1;

	/**
	 * Creates a new {@link EclipseCutCopyPasteEvent} for the given
	 * user and project. 
	 * 
	 * @param user the current user, never null.
	 * @param project the project for the file affected by this operation, never null.
	 */
	public EclipseCutCopyPasteEvent(String user, String project)
	{
		super(user, project);

		log.trace("EclipseCutCopyPasteEvent(...)");
	}

	/**
	 * Retrieves the {@link EclipseCutCopyPasteEvent.Type} of this event.
	 * 
	 * @return type of this event, never null.
	 */
	public Type getType()
	{
		return type;
	}

	/**
	 * Sets the {@link EclipseCutCopyPasteEvent.Type} for this event.
	 * <br>
	 * The value may not be {@link EclipseCutCopyPasteEvent.Type#NULL}.
	 * <p>
	 * This is a required value.
	 * 
	 * @param type the type for this event, never null.
	 */
	public void setType(Type type)
	{
		if (log.isTraceEnabled())
			log.trace("setType(): " + type);
		assert (type != null && !Type.NULL.equals(type));

		checkSeal();

		this.type = type;
	}

	/**
	 * Retrieves the current selection in the editor.
	 * 
	 * @return the current selection in the editor, never null.
	 */
	public String getSelection()
	{
		return selection;
	}

	/**
	 * Sets the current selection in the editor.
	 * <p>
	 * This is a required value.
	 * 
	 * @param selection the current selection, never null.
	 */
	public void setSelection(String selection)
	{
		if (log.isTraceEnabled())
			log.trace("setSelection(): " + selection);
		assert (selection != null);

		checkSeal();

		this.selection = selection;
	}

	/**
	 * Retrieves the current clipboard content.
	 * 
	 * @return current clipboard content, never null.
	 */
	public String getClipboard()
	{
		return clipboard;
	}

	/**
	 * Sets the current clipboard content.
	 * <p>
	 * This is a required value.
	 * 
	 * @param clipboard the current clipboard content, never null.
	 */
	public void setClipboard(String clipboard)
	{
		if (log.isTraceEnabled())
			log.trace("setClipboard(): " + clipboard);
		assert (clipboard != null);

		checkSeal();

		this.clipboard = clipboard;
	}

	/**
	 * Retrieves the current content of the file/editor which
	 * was affected by this operation.
	 * 
	 * @return current editor content, never null.
	 */
	public String getEditorContent()
	{
		return editorContent;
	}

	/**
	 * Sets the current content of the file/editor which was
	 * affected by this operation.
	 * <p>
	 * This is a required value.
	 * 
	 * @param editorContent current editor content, never null.
	 */
	public void setEditorContent(String editorContent)
	{
		if (log.isTraceEnabled())
			log.trace("setEditorContent(): " + editorContent);
		assert (editorContent != null);

		checkSeal();

		this.editorContent = editorContent;
	}

	/**
	 * Retrieves the offset in the document at which the operation occurred.
	 * <br> 
	 * If the current selection is not empty, this is also the start offset
	 * of the selection. For a paste operation, the clipboard is inserted
	 * at this position.
	 * <br>
	 * The offset is the zero-based position character position within the file.
	 * 
	 * @return offset of this event, always &gt;=0.
	 */
	public int getOffset()
	{
		return offset;
	}

	/**
	 * Sets the offset within the document at which the operation occurred.
	 * <p>
	 * This is a required value.
	 * 
	 * @param offset the offset of this event, always &gt;=0.
	 */
	public void setOffset(int offset)
	{
		if (log.isTraceEnabled())
			log.trace("setOffset(): " + offset);
		assert (offset >= 0);

		checkSeal();

		this.offset = offset;
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

		if (type == null || selection == null || clipboard == null || editorContent == null)
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
		return "EclipseCutCopyPasteEvent[" + super.subToString() + ", type: " + type + ", offset: " + offset
				+ ", selection: " + selection + ", clipboard: " + clipboard
				+ /*", editorContent: " + editorContent +*/"]";
	}

}
package org.electrocodeogram.cpc.ui.ruler;


import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.source.AbstractRulerColumn;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.rulers.IContributedRulerColumn;
import org.eclipse.ui.texteditor.rulers.RulerColumnDescriptor;
import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.ui.CPCUiPlugin;
import org.electrocodeogram.cpc.ui.data.CloneDataChange;
import org.electrocodeogram.cpc.ui.data.CloneDataModel;
import org.electrocodeogram.cpc.ui.data.ICloneDataChangeListener;
import org.electrocodeogram.cpc.ui.preferences.CPCPreferenceConstants;


/**
 * Implements the CPC clone information ruler which displays the coloured bars next to clone entries.
 * 
 * @author vw
 * 
 * @see CloneDataModel
 * @see CPCContributedRulerAnnotationHover
 */
public class CPCContributedRulerColumn extends AbstractRulerColumn implements IContributedRulerColumn,
		ICloneDataChangeListener
{
	private static final Log log = LogFactory.getLog(CPCContributedRulerColumn.class);
	private static final String RULER_ID = "org.electrocodeogram.cpc.ui.rulers.cpcclones";

	/** The contribution descriptor. */
	private RulerColumnDescriptor fDescriptor;
	/** The target editor. */
	private ITextEditor fEditor;

	private CloneDataModel model;
	private IClone[] clones;
	private CPCContributedRulerMouseListener mouseListener;

	private Color defaultBackgroundColor;

	/*
	 * Cached colours.
	 */
	private static final int COLOUR_BACKGROUND = 0;
	private static final int COLOUR_MIXED = 1;
	private static final int COLOUR_SYNC = 2;
	private static final int COLOUR_MODIFIED = 3;
	private static final int COLOUR_NOTIFY = 4;
	private static final int COLOUR_WARN = 5;
	private static final int COLOUR_IGNORE = 6;
	private static final int COLOUR_ORPHAN = 7;

	private Color[] colours = new Color[8];

	public CPCContributedRulerColumn()
	{
		log.trace("CPCContributedRulerColumn()");

		Preferences prefs = CPCUiPlugin.getDefault().getPluginPreferences();
		assert (prefs != null);

		setWidth(prefs.getInt(CPCPreferenceConstants.PREF_UI_RULER_WIDTH));
		setHover(new CPCContributedRulerAnnotationHover());

		model = CloneDataModel.getInstance();

	}

	/**
	 * Allocate colours.
	 */
	private void initialiseColours(Display display)
	{
		assert (display != null);

		/*
		 * Dispose existing colours, if eneded.
		 */
		disposeColours();

		IPreferenceStore prefStore = CPCUiPlugin.getDefault().getPreferenceStore();
		assert (prefStore != null);

		colours[COLOUR_BACKGROUND] = new Color(display, PreferenceConverter.getColor(prefStore,
				CPCPreferenceConstants.PREF_UI_RULER_COLOUR_BACKGROUND));
		colours[COLOUR_MIXED] = new Color(display, PreferenceConverter.getColor(prefStore,
				CPCPreferenceConstants.PREF_UI_RULER_COLOUR_MIXED));
		colours[COLOUR_SYNC] = new Color(display, PreferenceConverter.getColor(prefStore,
				CPCPreferenceConstants.PREF_UI_RULER_COLOUR_SYNC));
		colours[COLOUR_MODIFIED] = new Color(display, PreferenceConverter.getColor(prefStore,
				CPCPreferenceConstants.PREF_UI_RULER_COLOUR_MODIFIED));
		colours[COLOUR_NOTIFY] = new Color(display, PreferenceConverter.getColor(prefStore,
				CPCPreferenceConstants.PREF_UI_RULER_COLOUR_NOTIFY));
		colours[COLOUR_WARN] = new Color(display, PreferenceConverter.getColor(prefStore,
				CPCPreferenceConstants.PREF_UI_RULER_COLOUR_WARN));
		colours[COLOUR_IGNORE] = new Color(display, PreferenceConverter.getColor(prefStore,
				CPCPreferenceConstants.PREF_UI_RULER_COLOUR_IGNORE));
		colours[COLOUR_ORPHAN] = new Color(display, PreferenceConverter.getColor(prefStore,
				CPCPreferenceConstants.PREF_UI_RULER_COLOUR_ORPHAN));
	}

	private void disposeColours()
	{
		if (colours != null)
			for (Color colour : colours)
				if (colour != null && !colour.isDisposed())
					colour.dispose();
	}

	/*
	 * @see org.eclipse.ui.texteditor.rulers.IContributedRulerColumn#getDescriptor()
	 */
	@Override
	public final RulerColumnDescriptor getDescriptor()
	{
		return fDescriptor;
	}

	/*
	 * @see org.eclipse.ui.texteditor.rulers.IContributedRulerColumn#setDescriptor(org.eclipse.ui.texteditor.rulers.RulerColumnDescriptor)
	 */
	@Override
	public final void setDescriptor(RulerColumnDescriptor descriptor)
	{
		Assert.isLegal(descriptor != null);
		Assert.isTrue(fDescriptor == null);
		fDescriptor = descriptor;
	}

	/*
	 * @see org.eclipse.ui.texteditor.rulers.IContributedRulerColumn#setEditor(org.eclipse.ui.texteditor.ITextEditor)
	 */
	@Override
	public final void setEditor(ITextEditor editor)
	{
		Assert.isLegal(editor != null);
		Assert.isTrue(fEditor == null);
		fEditor = editor;
	}

	/*
	 * @see org.eclipse.ui.texteditor.rulers.IContributedRulerColumn#getEditor()
	 */
	@Override
	public final ITextEditor getEditor()
	{
		return fEditor;
	}

	/*
	 * @see org.eclipse.ui.texteditor.rulers.IContributedRulerColumn#columnCreated()
	 */
	@Override
	public void columnCreated()
	{
		//get current clone data
		clones = model.getCloneData();

		//register for clone data change events
		model.addChangeListener(this);
	}

	/*
	 * @see org.eclipse.ui.texteditor.rulers.IContributedRulerColumn#columnRemoved()
	 */
	@Override
	public void columnRemoved()
	{
		//unregister from clone data change events
		model.removeChangeListener(this);

		//remove mouse listener
		if (!getControl().isDisposed())
			getControl().removeMouseListener(mouseListener);
		mouseListener = null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.text.source.AbstractRulerColumn#createControl(org.eclipse.jface.text.source.CompositeRuler, org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public Control createControl(CompositeRuler parentRuler, Composite parentControl)
	{
		Control control = super.createControl(parentRuler, parentControl);

		//add a mouse event listener
		mouseListener = new CPCContributedRulerMouseListener();
		getControl().addMouseListener(mouseListener);

		initialiseColours(control.getDisplay());

		//defaultBackgroundColor = getDefaultBackground();
		defaultBackgroundColor = colours[COLOUR_BACKGROUND];

		return control;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.text.source.AbstractRulerColumn#paintLine(org.eclipse.swt.graphics.GC, int, int, int, int)
	 */
	@Override
	protected void paintLine(GC gc, int modelLine, int widgetLine, int linePixel, int lineHeight)
	{
		gc.setBackground(getBarColourForLine(modelLine));
		gc.fillRectangle(0, linePixel, getWidth(), lineHeight);
	}

	protected Color getBarColourForLine(int line)
	{
		if (log.isTraceEnabled())
			log.trace("getBarColourForLine() - line: " + line);

		if (clones.length == 0)
			//there are no clones in this document
			return defaultBackgroundColor;

		//get offsets for line
		IDocument document = getParentRuler().getTextViewer().getDocument();
		try
		{
			IRegion lineInfo = document.getLineInformation(line);
			int offset = lineInfo.getOffset();
			int length = lineInfo.getLength();

			//now go through all clones and check if any is spanning over our line range
			IClone cloneOnLine = null;
			for (IClone clone : clones)
			{
				if (clone.intersects(offset, length))
				{
					if (log.isTraceEnabled())
						log.trace("getBarColourForLine() - clone on line: " + clone);

					if (cloneOnLine != null)
					{
						//we have multiple clones on this line, return a special colour
						if (log.isTraceEnabled())
							log.trace("getBarColourForLine() - multiple clones on line " + line + "(" + offset + ":"
									+ length + "): " + clone + " - " + cloneOnLine);

						/*
						 * TODO: we chould also consider drawning multiple stripes or merging the colours somehow. 
						 */
						//return getControl().getDisplay().getSystemColor(SWT.COLOR_BLACK);
						return colours[COLOUR_MIXED];
					}

					cloneOnLine = clone;
				}
			}

			if (cloneOnLine != null)
			{
				//ok, we have exactly one clone on this line.
				//The colour depends on its state and age.

				/*
				 * TODO: add different colour shades depending on age here.
				 * TODO: read colour data from some configuration file / allow user to change colours
				 * 		i.e. using a custom colour theme, one of the default colours might not be
				 * 		visible at all.
				 */

				if (IClone.State.DEFAULT.equals(cloneOnLine.getCloneState()))
				{
					//normal clones are green
					//return getControl().getDisplay().getSystemColor(SWT.COLOR_GREEN);
					return colours[COLOUR_SYNC];
				}
				else if (IClone.State.MODIFIED.equals(cloneOnLine.getCloneState()))
				{
					//clones with notifications are yellow
					//return getControl().getDisplay().getSystemColor(SWT.COLOR_BLUE);
					return colours[COLOUR_MODIFIED];
				}
				else if (IClone.State.NOTIFY.equals(cloneOnLine.getCloneState()))
				{
					//clones with notifications are yellow
					//return getControl().getDisplay().getSystemColor(SWT.COLOR_YELLOW);
					return colours[COLOUR_NOTIFY];
				}
				else if (IClone.State.WARN.equals(cloneOnLine.getCloneState()))
				{
					//clones with warnings are red
					//return getControl().getDisplay().getSystemColor(SWT.COLOR_RED);
					return colours[COLOUR_WARN];
				}
				else if (IClone.State.IGNORE.equals(cloneOnLine.getCloneState()))
				{
					//ignored clones are always a dark gray
					//return getControl().getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY);
					return colours[COLOUR_IGNORE];
				}
				else if (IClone.State.ORPHAN.equals(cloneOnLine.getCloneState()))
				{
					//TODO: checl CPC configuration here to decide whether to display this clone or not
					//if displayed, orphaned/standalone clones are always a light gray
					//return getControl().getDisplay().getSystemColor(SWT.COLOR_GRAY);
					return colours[COLOUR_ORPHAN];
				}
				else
				{
					log.warn("getBarColourForLine() - unknown clone state: " + cloneOnLine.getCloneState() + " - "
							+ cloneOnLine);
				}
			}
		}
		catch (BadLocationException e)
		{
			log.error("getBarColourForLine() - unable to obtain offset for line: " + line, e);
		}

		log.trace("getBarColourForLine() - result: no clone");
		return defaultBackgroundColor;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.ui.data.ICloneDataChangeListener#cloneDataChanged(org.electrocodeogram.cpc.ui.data.CloneDataChange)
	 */
	@Override
	public void cloneDataChanged(CloneDataChange event)
	{
		//re-get current clone data
		clones = model.getCloneData();

		//update ruler
		Display.getDefault().syncExec(new Runnable()
		{
			@Override
			public void run()
			{
				redraw();
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.text.source.AbstractRulerColumn#dispose()
	 */
	@Override
	public void dispose()
	{
		/*
		 * Dispose all colours.
		 */
		disposeColours();

		super.dispose();
	}

	/**
	 * Allow users to click on the CPC clone information ruler to select a clone in the editor and
	 * all CPC views.
	 * 
	 * @author vw
	 */
	class CPCContributedRulerMouseListener implements MouseListener
	{
		/*
		 * (non-Javadoc)
		 * @see org.eclipse.swt.events.MouseListener#mouseDown(org.eclipse.swt.events.MouseEvent)
		 */
		@Override
		public void mouseDown(MouseEvent event)
		{
			if (log.isTraceEnabled())
				log.trace("mouseDown() - event: " + event);

			//get the offset for the currently selected line
			int activeLine = getLineOfLastMouseButtonActivity();
			if (activeLine < 0)
			{
				if (log.isTraceEnabled())
					log.trace("mouseDown() - no active line, ignoring - activeLine: " + activeLine);
				return;
			}

			IDocument document = getParentRuler().getTextViewer().getDocument();
			IRegion lineRange;
			try
			{
				lineRange = document.getLineInformation(activeLine);
			}
			catch (BadLocationException e)
			{
				log.error("mouseDown() - unable to translate line into offset: " + activeLine + " - " + e, e);
				return;
			}

			//check if there are any clones for that range
			List<IClone> clones = CloneDataModel.getInstance().getClonesForRange(lineRange.getOffset(),
					lineRange.getLength());
			if (clones.isEmpty())
			{
				log.trace("mouseDown() - no clones for this line, ignoring.");
				return;
			}

			//even if there are multiple clones, just pick the first one
			IClone clone = clones.get(0);

			//reveal and select this clone
			getParentRuler().getTextViewer().revealRange(clone.getOffset(), clone.getLength());
			getParentRuler().getTextViewer().setSelectedRange(clone.getOffset(), clone.getLength());

			//notify other views about this selection
			model.clonesSelected(RULER_ID, clones);
		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events.MouseEvent)
		 */
		@Override
		public void mouseUp(MouseEvent e)
		{
		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.swt.events.MouseListener#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
		 */
		@Override
		public void mouseDoubleClick(MouseEvent e)
		{
		}

	}
}

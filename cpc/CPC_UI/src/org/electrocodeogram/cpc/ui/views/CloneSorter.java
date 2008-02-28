package org.electrocodeogram.cpc.ui.views;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IMemento;
import org.electrocodeogram.cpc.core.api.data.ICloneGroup;
import org.electrocodeogram.cpc.core.utils.CoreUtils;


/**
 * Taken from: Eclipse, Building Commercial-Quality Plugins, 2nd Ed.   
 */
public class CloneSorter extends ViewerSorter
{
	private static Log log = LogFactory.getLog(CloneSorter.class);

	// Simple data structure for grouping
	// sort information by column.
	private class SortInfo
	{
		int columnIndex;
		@SuppressWarnings("unchecked")
		Comparator comparator;
		boolean descending;
	}

	private ColumnViewer viewer;
	private SortInfo[] infos;

	private static final String TAG_DESCENDING = "descending";
	private static final String TAG_COLUMN_INDEX = "columnIndex";
	private static final String TAG_TYPE = "SortInfo";
	private static final String TAG_TRUE = "true";

	/**
	 * 
	 * @param viewer {@link TableViewer} or {@link TreeViewer}, never null.
	 * @param columns array of {@link TableColumn}s or {@link TreeColumn}s, never null.
	 * @param comparators array of comparators to use, never null.
	 */
	@SuppressWarnings("unchecked")
	public CloneSorter(ColumnViewer viewer, Item[] columns, Comparator[] comparators)
	{
		if (log.isTraceEnabled())
			log.trace("CloneSorter() - viewer: " + viewer + ", columns: " + CoreUtils.arrayToString(columns)
					+ ", comparators: " + CoreUtils.arrayToString(comparators));
		assert (viewer != null && (viewer instanceof TableViewer || viewer instanceof TreeViewer) && columns != null && comparators != null);

		this.viewer = viewer;
		infos = new SortInfo[columns.length];
		for (int i = 0; i < columns.length; i++)
		{
			infos[i] = new SortInfo();
			infos[i].columnIndex = i;
			infos[i].comparator = comparators[i];
			infos[i].descending = false;
			createSelectionListener(columns[i], infos[i]);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public int compare(Viewer viewer, Object element1, Object element2)
	{
		for (int i = 0; i < infos.length; i++)
		{
			int result = infos[i].comparator.compare(element1, element2);
			if (result != 0)
			{
				//decending/ascending is ignored for non-leafs (clone groups)
				if (!(element1 instanceof ICloneGroup) && (infos[i].descending))
					return -result;
				return result;
			}
		}
		return 0;
	}

	/**
	 * Sort the data by the value of the given column. If the column was already selected
	 * the sorting mode is toggled between ascending and descending.
	 * 
	 * @param index index of the column to sort by, starts at 0. Must not exceed column count.
	 */
	public void sortUsingColumn(int index)
	{
		assert (index < infos.length);

		for (int i = 0; i < infos.length; i++)
		{
			if (infos[i].columnIndex == index)
			{
				sortUsing(infos[i]);
				break;
			}
		}
	}

	private void createSelectionListener(final Item column, final SortInfo info)
	{
		assert (column != null && (column instanceof TableColumn || column instanceof TreeColumn));

		SelectionAdapter selectionAdapter = new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				sortUsing(info);
			}
		};

		if (column instanceof TableColumn)
			((TableColumn) column).addSelectionListener(selectionAdapter);
		else if (column instanceof TreeColumn)
			((TreeColumn) column).addSelectionListener(selectionAdapter);
	}

	protected void sortUsing(SortInfo info)
	{
		if (info == infos[0])
			info.descending = !info.descending;
		else
		{
			for (int i = 0; i < infos.length; i++)
			{
				if (info == infos[i])
				{
					System.arraycopy(infos, 0, infos, 1, i);
					infos[0] = info;
					info.descending = false;
					break;
				}
			}
		}
		viewer.refresh();
	}

	public void saveState(IMemento memento)
	{
		for (int i = 0; i < infos.length; i++)
		{
			SortInfo info = infos[i];
			IMemento mem = memento.createChild(TAG_TYPE);
			mem.putInteger(TAG_COLUMN_INDEX, info.columnIndex);

			if (info.descending)
				mem.putString(TAG_DESCENDING, TAG_TRUE);
		}
	}

	public void init(IMemento memento)
	{
		List<SortInfo> newInfos = new ArrayList<SortInfo>(infos.length);
		IMemento[] mems = memento.getChildren(TAG_TYPE);

		for (int i = 0; i < mems.length; i++)
		{
			IMemento mem = mems[i];
			Integer value = mem.getInteger(TAG_COLUMN_INDEX);
			if (value == null)
				continue;

			int index = value.intValue();
			if (index < 0 || index >= infos.length)
				continue;

			SortInfo info = infos[index];

			if (newInfos.contains(info))
				continue;

			info.descending = TAG_TRUE.equals(mem.getString(TAG_DESCENDING));
			newInfos.add(info);
		}

		for (int i = 0; i < infos.length; i++)
			if (!newInfos.contains(infos[i]))
				newInfos.add(infos[i]);

		infos = (SortInfo[]) newInfos.toArray(new SortInfo[newInfos.size()]);
	}
}

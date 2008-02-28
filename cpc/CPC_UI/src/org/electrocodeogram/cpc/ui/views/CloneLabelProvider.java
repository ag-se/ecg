package org.electrocodeogram.cpc.ui.views;


import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.data.ICloneGroup;
import org.electrocodeogram.cpc.ui.data.CloneDataModel;


public class CloneLabelProvider extends LabelProvider implements ITableLabelProvider
{
	private static Log log = LogFactory.getLog(CloneLabelProvider.class);
	private SimpleDateFormat dateFormat;
	private boolean treeMode;
	private CloneDataModel model;

	public CloneLabelProvider(CloneDataModel model, boolean treeMode)
	{
		if (log.isTraceEnabled())
			log.trace("CloneLabelProvider() - model: " + model + ", treeMode: " + treeMode);

		this.model = model;
		this.treeMode = treeMode;
		this.dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
	 */
	@Override
	public String getColumnText(Object element, int columnIndex)
	{
		if (log.isTraceEnabled())
			log.trace("getColumnText() - columnIndex: " + columnIndex + ", element: " + element);
		assert (element != null && columnIndex >= 0);

		if (treeMode && element instanceof ICloneGroup)
		{
			ICloneGroup cloneGroup = (ICloneGroup) element;

			if (columnIndex == 1)
			{
				//Project

				//TODO: caching could be used here to improve performance
				List<IClone> groupClones = model.getStoreProvider().getClonesByGroup(cloneGroup.getUuid());

				return Integer.toString(groupClones.size()) + " clone(s)";
			}
			else if (columnIndex == 2)
			{
				//File

				//TODO: caching could be used here to improve performance
				List<IClone> groupClones = model.getStoreProvider().getClonesByGroup(cloneGroup.getUuid());

				//count the number of distinct files involved
				Set<String> cloneFileUuids = new HashSet<String>(groupClones.size());
				for (IClone groupClone : groupClones)
					cloneFileUuids.add(groupClone.getFileUuid());

				return "in " + cloneFileUuids.size() + " file(s)";
			}
			else
				return null;
		}
		else if (element instanceof IClone)
		{
			IClone clone = (IClone) element;

			if (treeMode)
			{
				switch (columnIndex)
				{
					case 0: //state
						return "";
					case 1: //project
						return model.getCloneFileByUuid(clone.getFileUuid()).getProject();
					case 2: //file name
						String path = model.getCloneFileByUuid(clone.getFileUuid()).getPath();
						return (new Path(path)).lastSegment();
					case 3: //start offset
						return Integer.toString(clone.getOffset());
					case 4: //length
						return Integer.toString(clone.getLength());
					case 5: //creator
						return clone.getCreator() + " - " + dateFormat.format(clone.getCreationDate());
					default:
						log.error("getColumnText() - INTERNAL ERROR - unknown column: " + columnIndex + ", element: "
								+ element, new Throwable());
						return "";
				}
			}
			else
			{
				switch (columnIndex)
				{
					case 0: //state
						return "";
					case 1: //start offset
						return Integer.toString(clone.getOffset());
					case 2: //length
						return Integer.toString(clone.getLength());
					case 3: //creator
						return clone.getCreator();
					case 4: //creation date
						return dateFormat.format(clone.getCreationDate());
					default:
						log.error("getColumnText() - INTERNAL ERROR - unknown column: " + columnIndex + ", element: "
								+ element, new Throwable());
						return "";
				}
			}
		}
		else
		{
			log.error("getColumnText() - INTERNAL ERROR - element of unknown type: " + element, new Throwable());
			return "";
		}

	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
	 */
	@Override
	public Image getColumnImage(Object element, int columnIndex)
	{
		if (log.isTraceEnabled())
			log.trace("getColumnImage() - columnIndex: " + columnIndex + ", element: " + element);

		if (columnIndex == 0)
			return getImage(element);
		else
			return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
	 */
	@Override
	public Image getImage(Object element)
	{
		if (log.isTraceEnabled())
			log.trace("getImage() - element: " + element);

		return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
	}

}

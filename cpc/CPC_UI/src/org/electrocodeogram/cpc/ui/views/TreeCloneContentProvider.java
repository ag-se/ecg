package org.electrocodeogram.cpc.ui.views;


import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Display;
import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.data.ICloneGroup;
import org.electrocodeogram.cpc.core.utils.CoreUtils;
import org.electrocodeogram.cpc.ui.data.CloneDataChange;
import org.electrocodeogram.cpc.ui.data.CloneDataModel;
import org.electrocodeogram.cpc.ui.data.ICloneDataChangeListener;
import org.electrocodeogram.cpc.ui.utils.EclipseUtils;


public class TreeCloneContentProvider implements ITreeContentProvider, ICloneDataChangeListener
{
	private static Log log = LogFactory.getLog(TreeCloneContentProvider.class);
	private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

	protected String viewId = null;
	protected TreeViewer viewer = null;
	protected CloneDataModel model = null;

	public TreeCloneContentProvider(String viewId)
	{
		log.trace("TreeCloneContentProvider() - viewId: " + viewId);
		assert (viewId != null);

		this.viewId = viewId;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.ui.views.CloneContentProvider#getElements(java.lang.Object)
	 */
	@Override
	public Object[] getElements(Object parent)
	{
		if (log.isTraceEnabled())
			log.trace("getElements() - parent: " + parent);

		Object[] result = null;
		if (model != null)
			result = model.getCloneGroupData();
		else
		{
			log.warn("getElements() - model is null");
			result = EMPTY_OBJECT_ARRAY;
		}

		if (log.isTraceEnabled())
			log.trace("getElements() - result: " + CoreUtils.arrayToString(result));

		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	@Override
	public Object[] getChildren(Object parentElement)
	{
		if (log.isTraceEnabled())
			log.trace("getChildren() - parentElement: " + parentElement);
		assert (parentElement != null);

		if (parentElement instanceof ICloneGroup)
		{
			//get a list of all clones known for that group
			List<IClone> clones = model.getStoreProvider().getClonesByGroup(((ICloneGroup) parentElement).getUuid());
			Object[] result = clones.toArray();

			if (log.isTraceEnabled())
				log.trace("getChildren() - result: " + CoreUtils.arrayToString(result));

			return result;
		}
		else if (parentElement instanceof IClone)
		{
			//a clone is a leaf, there are no children
			log.trace("getChildren() - result: {}");
			return EMPTY_OBJECT_ARRAY;
		}
		else
		{
			//this should never happen
			log.error("getChildren() - invalid element type, expected ICloneGroup or IClone - element: "
					+ parentElement, new Throwable());
			log.trace("getChildren() - result: {}");
			return EMPTY_OBJECT_ARRAY;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
	 */
	@Override
	public Object getParent(Object element)
	{
		if (log.isTraceEnabled())
			log.trace("getParent() - element: " + element);
		assert (element != null);

		if (element instanceof ICloneGroup)
		{
			//a group has no parent
			log.trace("getParent() - result: null");
			return null;
		}
		else if (element instanceof IClone)
		{
			//return the group of this clone
			assert (((IClone) element).getGroupUuid() != null);
			ICloneGroup result = model.getCloneGroupByUuid(((IClone) element).getGroupUuid());

			if (log.isTraceEnabled())
				log.trace("getParent() - result: " + result);

			//the result may actually be null if the content of the model was modified concurrently
			//assert (result != null);

			return result;
		}
		else
		{
			//this shouldn't happen
			log.error("getParent() - invalid element type, expected ICloneGroup or IClone - element: " + element,
					new Throwable());
			log.trace("getParent() - result: null");
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
	 */
	@Override
	public boolean hasChildren(Object element)
	{
		if (log.isTraceEnabled())
			log.trace("hasChildren() - element: " + element);
		assert (element != null);

		boolean result;

		if (element instanceof ICloneGroup)
		{
			//a group always has children
			result = true;
		}
		else if (element instanceof IClone)
		{
			//a clone is a leaf
			result = false;
		}
		else
		{
			//this shouldn't happen
			log.error("getParent() - invalid element type, expected ICloneGroup or IClone - element: " + element,
					new Throwable());
			result = false;
		}

		if (log.isTraceEnabled())
			log.trace("hasChildren() - result: " + result);

		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	@Override
	public void dispose()
	{
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
	{
		if (log.isTraceEnabled())
			log.trace("inputChanged() - viewer: " + viewer + ", oldInput: " + oldInput + ", newInput: " + newInput);
		assert (viewer != null && viewer instanceof TreeViewer);

		this.viewer = (TreeViewer) viewer;

		if (model != null)
			model.removeChangeListener(this);

		model = null;

		if (newInput != null && newInput instanceof CloneDataModel)
			model = (CloneDataModel) newInput;

		if (model != null)
			model.addChangeListener(this);
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.ui.data.ICloneDataChangeListener#cloneDataChanged(org.electrocodeogram.cpc.ui.data.CloneDataChange)
	 */
	@Override
	public void cloneDataChanged(CloneDataChange event)
	{
		if (log.isTraceEnabled())
			log.trace("cloneDataChanged() - event: " + event);

		//access to the GUI is restricted to the GUI thread, let's make sure that we're
		//running the following code in that GUI thread.
		Display.getDefault().syncExec(new CloneDataChangedRunner(event));
	}

	protected class CloneDataChangedRunner implements Runnable
	{
		private CloneDataChange event;

		public CloneDataChangedRunner(CloneDataChange event)
		{
			this.event = event;
		}

		public void run()
		{
			if (log.isTraceEnabled())
				log.trace("CloneDataChangedRunner.run() - event: " + event);

			//make sure the view hasn't been disposed by now
			if (viewer.getControl().isDisposed())
			{
				log.warn("CloneDataChangedRunner.run() - view is already disposed, exiting.");
				return;
			}

			viewer.getTree().setRedraw(false);

			try
			{
				/*
				 * Groups
				 */
				if (event.getRemovedCloneGroups() != null && event.getRemovedCloneGroups().length > 0)
				{
					if (log.isTraceEnabled())
						log.trace("CloneDataChangedRunner.run() - removing groups from view: "
								+ CoreUtils.arrayToString(event.getRemovedCloneGroups()));

					viewer.remove(event.getRemovedCloneGroups());
				}

				if (event.getUpdatedCloneGroups() != null && event.getUpdatedCloneGroups().length > 0)
				{
					if (log.isTraceEnabled())
						log.trace("CloneDataChangedRunner.run() - updating groups in view: "
								+ CoreUtils.arrayToString(event.getUpdatedCloneGroups()));

					viewer.update(event.getUpdatedCloneGroups(), null);
				}

				if (event.getAddedCloneGroups() != null && event.getAddedCloneGroups().length > 0)
				{
					if (log.isTraceEnabled())
						log.trace("CloneDataChangedRunner.run() - adding groups to view: "
								+ CoreUtils.arrayToString(event.getAddedCloneGroups()));

					viewer.add(model, event.getAddedCloneGroups());
				}

				/*
				 * Clones
				 */
				if (event.getRemovedClones() != null && event.getRemovedClones().length > 0)
				{
					if (log.isTraceEnabled())
						log.trace("CloneDataChangedRunner.run() - removing clones from view: "
								+ CoreUtils.arrayToString(event.getRemovedClones()));

					viewer.remove(event.getRemovedClones());
				}

				if (event.getUpdatedClones() != null && event.getUpdatedClones().length > 0)
				{
					if (log.isTraceEnabled())
						log.trace("CloneDataChangedRunner.run() - updating clones in view: "
								+ CoreUtils.arrayToString(event.getUpdatedClones()));

					viewer.update(event.getUpdatedClones(), null);
				}

				if (event.getAddedClones() != null && event.getAddedClones().length > 0)
				{
					if (log.isTraceEnabled())
						log.trace("CloneDataChangedRunner.run() - added clones to view: "
								+ CoreUtils.arrayToString(event.getAddedClones()));

					for (IClone clone : event.getAddedClones())
					{
						ICloneGroup parentGroup = null;

						if (clone.getGroupUuid() != null)
							parentGroup = model.getCloneGroupByUuid(clone.getGroupUuid());

						if (parentGroup != null)
						{
							viewer.add(parentGroup, clone);
						}
						else
						{
							if (log.isDebugEnabled())
								log.debug("CloneDataChangedRunner.run() - no parent group for clone: " + clone);
						}
					}
				}

				//check if our selection needs to be changed
				if (event.getSelectedClones() != null)
				{
					//ignore selection changes which we created ourself
					if (!viewId.equals(event.getSelectionOrigin()))
					{
						//first check if the selection matches our current selection
						//TODO: this check can probably be removed
						IStructuredSelection oldSelection = (IStructuredSelection) viewer.getSelection();
						IStructuredSelection newSelection = new StructuredSelection(event.getSelectedClones());

						if (!EclipseUtils.selectionEqual(oldSelection, newSelection))
							//ok, update the selection
							viewer.setSelection(newSelection, true);
					}
				}
			}
			finally
			{
				viewer.getTree().setRedraw(true);
			}

			log.trace("CloneDataChangedRunner.run() - done.");
		}
	}

}

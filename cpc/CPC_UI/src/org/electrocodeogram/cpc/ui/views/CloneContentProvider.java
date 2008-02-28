package org.electrocodeogram.cpc.ui.views;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Display;
import org.electrocodeogram.cpc.ui.data.CloneDataChange;
import org.electrocodeogram.cpc.ui.data.CloneDataModel;
import org.electrocodeogram.cpc.ui.data.ICloneDataChangeListener;
import org.electrocodeogram.cpc.ui.utils.EclipseUtils;


public class CloneContentProvider implements IStructuredContentProvider, ICloneDataChangeListener
{
	private static Log log = LogFactory.getLog(CloneContentProvider.class);

	protected String viewId = null;
	protected TableViewer viewer = null;
	protected CloneDataModel model = null;

	public CloneContentProvider(String viewId)
	{
		log.trace("CloneContentProvider() - viewId: " + viewId);
		assert (viewId != null);

		this.viewId = viewId;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	@Override
	public Object[] getElements(Object parent)
	{
		if (model != null)
		{
			return model.getCloneData();
		}
		else
		{
			log.warn("getElements() - no model present");
			return new Object[0];
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
	{
		assert (viewer != null && viewer instanceof TableViewer);

		this.viewer = (TableViewer) viewer;

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
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	@Override
	public void dispose()
	{
	}

	public void cloneDataChanged(CloneDataChange event)
	{
		if (viewer == null)
		{
			log.warn("cloneDataChanged() - viewer is null");
			return;
		}

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

			viewer.getTable().setRedraw(false);

			try
			{
				if (event.getRemovedClones() != null)
					viewer.remove(event.getRemovedClones());
				if (event.getUpdatedClones() != null)
					viewer.update(event.getUpdatedClones(), null);
				if (event.getAddedClones() != null)
					viewer.add(event.getAddedClones());

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
				viewer.getTable().setRedraw(true);
			}
		}
	}
}

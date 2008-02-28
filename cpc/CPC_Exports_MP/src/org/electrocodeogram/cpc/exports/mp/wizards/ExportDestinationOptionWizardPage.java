package org.electrocodeogram.cpc.exports.mp.wizards;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;
import org.electrocodeogram.cpc.exports.mp.exports.MPExportToolAdapter;
import org.electrocodeogram.cpc.importexport.wizards.AbstractImportExportExtensionOptionWizardPage;


public class ExportDestinationOptionWizardPage extends AbstractImportExportExtensionOptionWizardPage
{
	private static final Log log = LogFactory.getLog(ExportDestinationOptionWizardPage.class);

	public ExportDestinationOptionWizardPage()
	{
		super("Mapping Provider File Export Adapter Options",
				"Please select the destination directory for this export.");
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent)
	{
		if (log.isTraceEnabled())
			log.trace("createControl() - parent: " + parent);

		Composite container = new Composite(parent, SWT.NULL);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		container.setLayout(gridLayout);
		setControl(container);

		GridData gridData;
		Label label;

		label = new Label(container, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		label.setLayoutData(gridData);
		label.setText("Export destination folder:");

		final Text selectedFolder = new Text(container, SWT.BORDER | SWT.READ_ONLY);
		gridData = new GridData();
		gridData.horizontalSpan = 1;
		selectedFolder.setLayoutData(gridData);

		Button button = new Button(container, SWT.NONE);
		button.setText("Select...");
		gridData = new GridData();
		gridData.horizontalSpan = 1;
		button.setLayoutData(gridData);
		button.addMouseListener(new MouseListener()
		{

			@Override
			public void mouseDoubleClick(MouseEvent e)
			{
			}

			@Override
			public void mouseDown(MouseEvent e)
			{
				ContainerSelectionDialog dialog = new ContainerSelectionDialog(getShell(), ResourcesPlugin
						.getWorkspace().getRoot(), true,
						"Please specify the destination directory for this export. The folder does not have to exist.");
				dialog.open();
				Object[] selection = dialog.getResult();
				log.trace("GOT: " + selection);

				if (selection != null && selection.length > 0)
				{
					log.trace("1st elem: " + selection[0] + " (" + selection[0].getClass() + ")");
					selectedFolder.setText(selection[0].toString());
					optionMap.put(MPExportToolAdapter.OPTION_EXPORT_DESTINATION_FOLDER, selection[0].toString());
					updatePageComplete();
				}
			}

			@Override
			public void mouseUp(MouseEvent e)
			{
			}
		});

		setControl(container);

		updatePageComplete();
	}

	/**
	 * Listener method which is called whenever one of the form elements on this wizard page is modified.
	 */
	protected void updatePageComplete()
	{
		log.trace("updatePageComplete()");

		setPageComplete(false);

		//make sure a provider was selected
		String destFolder = optionMap.get(MPExportToolAdapter.OPTION_EXPORT_DESTINATION_FOLDER);
		if (destFolder == null)
		{
			setMessage(null);
			setErrorMessage("You need to select a destination folder.");
			return;
		}

		//all ok
		setPageComplete(true);

		setMessage(null);
		setErrorMessage(null);
	}

}

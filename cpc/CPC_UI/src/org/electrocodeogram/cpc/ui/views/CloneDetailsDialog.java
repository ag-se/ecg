package org.electrocodeogram.cpc.ui.views;


import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.electrocodeogram.cpc.core.CPCCorePlugin;
import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.data.ICloneFile;
import org.electrocodeogram.cpc.core.api.data.extension.ICloneModificationHistoryExtension;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider;


/**
* This code was edited or generated using CloudGarden's Jigloo
* SWT/Swing GUI Builder, which is free for non-commercial
* use. If Jigloo is being used commercially (ie, by a corporation,
* company or business for any purpose whatever) then you
* should purchase a license for each developer using Jigloo.
* Please visit www.cloudgarden.com for details.
* Use of Jigloo implies acceptance of these licensing terms.
* A COMMERCIAL LICENSE HAS NOT BEEN PURCHASED FOR
* THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED
* LEGALLY FOR ANY CORPORATE OR COMMERCIAL PURPOSE.
*/
public class CloneDetailsDialog extends org.eclipse.swt.widgets.Dialog
{
	private static final Log log = LogFactory.getLog(CloneDetailsDialog.class);

	private Shell dialogShell;
	private Group groupLocation;
	private Text textLength;
	private Button buttonClose;
	private Text textModificationLast;
	private Label labelModificationLast;
	private Text textModificationCount;
	private Label labelModificationCount;
	private Group groupModification;
	private Label labelGroupOrigin;
	private Text textGroupMemberList;
	private Text textGroupSize;
	private Label labelGroupSize;
	private Group groupGroup;
	private Text textStateMessage;
	private Label labelStateMessage;
	private Text textStateDismissed;
	private Label labelStateDismissed;
	private Group groupState;
	private Label labelStateState;
	private Text textStateWeight;
	private Label labelStateWeight;
	private Text textStateOn;
	private Label labelStateOn;
	private Text textStateState;
	private Text textClassification;
	private Group groupClassification;
	private Text textCreationBy;
	private Label labelCreationBy;
	private Text textCreationOn;
	private Label labelCreationOn;
	private Group groupCreation;
	private Label labelLength;
	private Text textOffset;
	private Label labelOffset;
	private Group groupPosition;
	private Text textFile;
	private Label labelFile;
	private Text textProject;
	private Label labelProject;

	private IClone clone;

	/**
	* Auto-generated main method to display this 
	* org.eclipse.swt.widgets.Dialog inside a new Shell.
	*/
	public static void main(String[] args)
	{
		try
		{
			Display display = Display.getDefault();
			Shell shell = new Shell(display);
			CloneDetailsDialog inst = new CloneDetailsDialog(shell, SWT.NULL, null);
			inst.open();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public CloneDetailsDialog(Shell parent, int style, IClone clone)
	{
		super(parent, style);

		assert (clone != null);

		this.clone = clone;
	}

	public void open()
	{
		try
		{
			Shell parent = getParent();
			dialogShell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);

			GridLayout dialogShellLayout = new GridLayout();
			dialogShellLayout.numColumns = 4;
			dialogShell.setLayout(dialogShellLayout);
			dialogShell.layout();
			dialogShell.pack();
			dialogShell.setSize(730, 410);
			dialogShell.setText("CPC Clone Details");
			{
				groupLocation = new Group(dialogShell, SWT.NONE);
				GridLayout groupLocationLayout = new GridLayout();
				groupLocationLayout.numColumns = 2;
				groupLocation.setLayout(groupLocationLayout);
				GridData groupLocationLData = new GridData();
				groupLocationLData.horizontalSpan = 4;
				groupLocationLData.horizontalAlignment = GridData.FILL;
				groupLocationLData.grabExcessHorizontalSpace = true;
				groupLocation.setLayoutData(groupLocationLData);
				groupLocation.setText("Location");
				{
					labelProject = new Label(groupLocation, SWT.NONE);
					labelProject.setText("Project:");
				}
				{
					GridData textProjectLData = new GridData();
					textProjectLData.horizontalAlignment = GridData.FILL;
					textProjectLData.grabExcessHorizontalSpace = true;
					textProject = new Text(groupLocation, SWT.NONE);
					textProject.setLayoutData(textProjectLData);
					textProject.setEditable(false);
				}
				{
					labelFile = new Label(groupLocation, SWT.NONE);
					labelFile.setText("File:");
				}
				{
					GridData textFileLData = new GridData();
					textFileLData.horizontalAlignment = GridData.FILL;
					textFileLData.grabExcessHorizontalSpace = true;
					textFile = new Text(groupLocation, SWT.NONE);
					textFile.setLayoutData(textFileLData);
					textFile.setEditable(false);
				}
			}
			{
				groupPosition = new Group(dialogShell, SWT.NONE);
				GridLayout groupPositionLayout = new GridLayout();
				groupPositionLayout.numColumns = 2;
				groupPosition.setLayout(groupPositionLayout);
				GridData groupPositionLData = new GridData();
				groupPositionLData.verticalAlignment = GridData.FILL;
				groupPositionLData.horizontalAlignment = GridData.FILL;
				groupPosition.setLayoutData(groupPositionLData);
				groupPosition.setText("Position");
				{
					labelOffset = new Label(groupPosition, SWT.NONE);
					labelOffset.setText("Offset:");
				}
				{
					textOffset = new Text(groupPosition, SWT.NONE);
					textOffset.setEditable(false);
				}
				{
					labelLength = new Label(groupPosition, SWT.NONE);
					labelLength.setText("Length:");
				}
				{
					textLength = new Text(groupPosition, SWT.NONE);
					textLength.setEditable(false);
				}
			}
			{
				groupCreation = new Group(dialogShell, SWT.NONE);
				GridLayout groupCreationLayout = new GridLayout();
				groupCreationLayout.numColumns = 2;
				groupCreation.setLayout(groupCreationLayout);
				GridData groupCreationLData = new GridData();
				groupCreationLData.horizontalSpan = 3;
				groupCreationLData.verticalAlignment = GridData.FILL;
				groupCreationLData.horizontalAlignment = GridData.FILL;
				groupCreationLData.grabExcessHorizontalSpace = true;
				groupCreation.setLayoutData(groupCreationLData);
				groupCreation.setText("Creation");
				{
					labelCreationOn = new Label(groupCreation, SWT.NONE);
					labelCreationOn.setText("On:");
				}
				{
					GridData textCreationOnLData = new GridData();
					textCreationOnLData.horizontalAlignment = GridData.FILL;
					textCreationOnLData.grabExcessHorizontalSpace = true;
					textCreationOn = new Text(groupCreation, SWT.NONE);
					textCreationOn.setLayoutData(textCreationOnLData);
					textCreationOn.setEditable(false);
				}
				{
					labelCreationBy = new Label(groupCreation, SWT.NONE);
					labelCreationBy.setText("By:");
				}
				{
					GridData textCreationByLData = new GridData();
					textCreationByLData.horizontalAlignment = GridData.FILL;
					textCreationByLData.grabExcessHorizontalSpace = true;
					textCreationBy = new Text(groupCreation, SWT.NONE);
					textCreationBy.setLayoutData(textCreationByLData);
					textCreationBy.setEditable(false);
				}
			}
			{
				groupClassification = new Group(dialogShell, SWT.NONE);
				FillLayout groupClassificationLayout = new FillLayout(org.eclipse.swt.SWT.HORIZONTAL);
				groupClassification.setLayout(groupClassificationLayout);
				GridData groupClassificationLData = new GridData();
				groupClassificationLData.horizontalAlignment = GridData.FILL;
				groupClassificationLData.verticalAlignment = GridData.FILL;
				groupClassification.setLayoutData(groupClassificationLData);
				groupClassification.setText("Classification");
				{
					textClassification = new Text(groupClassification, SWT.MULTI | SWT.WRAP);
					textClassification.setEditable(false);
				}
			}
			{
				groupState = new Group(dialogShell, SWT.NONE);
				GridLayout groupStateLayout = new GridLayout();
				groupStateLayout.numColumns = 4;
				groupState.setLayout(groupStateLayout);
				GridData groupStateLData = new GridData();
				groupStateLData.horizontalSpan = 3;
				groupStateLData.horizontalAlignment = GridData.FILL;
				groupStateLData.grabExcessHorizontalSpace = true;
				groupStateLData.verticalAlignment = GridData.FILL;
				groupState.setLayoutData(groupStateLData);
				groupState.setText("Clone State");
				{
					labelStateState = new Label(groupState, SWT.NONE);
					labelStateState.setText("State:");
				}
				{
					GridData textStateStateLData = new GridData();
					textStateStateLData.horizontalAlignment = GridData.FILL;
					textStateStateLData.grabExcessHorizontalSpace = true;
					textStateState = new Text(groupState, SWT.NONE);
					textStateState.setLayoutData(textStateStateLData);
					textStateState.setEditable(false);
				}
				{
					labelStateWeight = new Label(groupState, SWT.NONE);
					labelStateWeight.setText("Weight:");
				}
				{
					GridData textStateWeightLData = new GridData();
					textStateWeightLData.grabExcessHorizontalSpace = true;
					textStateWeightLData.horizontalAlignment = GridData.FILL;
					textStateWeight = new Text(groupState, SWT.NONE);
					textStateWeight.setLayoutData(textStateWeightLData);
					textStateWeight.setEditable(false);
				}
				{
					labelStateOn = new Label(groupState, SWT.NONE);
					labelStateOn.setText("On:");
				}
				{
					GridData textStateOnLData = new GridData();
					textStateOnLData.horizontalAlignment = GridData.FILL;
					textStateOn = new Text(groupState, SWT.NONE);
					textStateOn.setLayoutData(textStateOnLData);
					textStateOn.setEditable(false);
				}
				{
					labelStateDismissed = new Label(groupState, SWT.NONE);
					labelStateDismissed.setText("Dismissed:");
				}
				{
					GridData textStateDismissedLData = new GridData();
					textStateDismissedLData.horizontalAlignment = GridData.FILL;
					textStateDismissed = new Text(groupState, SWT.NONE);
					textStateDismissed.setLayoutData(textStateDismissedLData);
					textStateDismissed.setEditable(false);
				}
				{
					labelStateMessage = new Label(groupState, SWT.NONE);
					labelStateMessage.setText("Message:");
				}
				{
					GridData textStateMessageLData = new GridData();
					textStateMessageLData.horizontalSpan = 3;
					textStateMessageLData.horizontalAlignment = GridData.FILL;
					textStateMessage = new Text(groupState, SWT.NONE);
					textStateMessage.setLayoutData(textStateMessageLData);
					textStateMessage.setEditable(false);
				}
			}
			{
				groupGroup = new Group(dialogShell, SWT.NONE);
				GridLayout groupGroupLayout = new GridLayout();
				groupGroupLayout.numColumns = 3;
				groupGroup.setLayout(groupGroupLayout);
				GridData groupGroupLData = new GridData();
				groupGroupLData.horizontalAlignment = GridData.FILL;
				groupGroupLData.verticalAlignment = GridData.FILL;
				groupGroupLData.horizontalSpan = 4;
				groupGroup.setLayoutData(groupGroupLData);
				groupGroup.setText("Group");
				{
					labelGroupSize = new Label(groupGroup, SWT.NONE);
					labelGroupSize.setText("Size:");
				}
				{
					textGroupSize = new Text(groupGroup, SWT.NONE);
					GridData textGroupSizeLData = new GridData();
					textGroupSizeLData.horizontalAlignment = GridData.FILL;
					textGroupSize.setLayoutData(textGroupSizeLData);
					textGroupSize.setEditable(false);
				}
				{
					GridData textGroupMemberListLData = new GridData();
					textGroupMemberListLData.horizontalAlignment = GridData.FILL;
					textGroupMemberListLData.grabExcessHorizontalSpace = true;
					textGroupMemberListLData.verticalAlignment = GridData.FILL;
					textGroupMemberListLData.verticalSpan = 2;
					textGroupMemberList = new Text(groupGroup, SWT.MULTI | SWT.WRAP);
					textGroupMemberList.setLayoutData(textGroupMemberListLData);
					textGroupMemberList.setEditable(false);
				}
				{
					labelGroupOrigin = new Label(groupGroup, SWT.NONE);
					GridData labelGroupOriginLData = new GridData();
					labelGroupOriginLData.horizontalSpan = 2;
					labelGroupOriginLData.horizontalAlignment = GridData.END;
					labelGroupOrigin.setLayoutData(labelGroupOriginLData);
					labelGroupOrigin.setText("* = Origin");
				}
			}
			{
				groupModification = new Group(dialogShell, SWT.NONE);
				GridLayout groupModificationLayout = new GridLayout();
				groupModificationLayout.numColumns = 4;
				groupModification.setLayout(groupModificationLayout);
				GridData groupModificationLData = new GridData();
				groupModificationLData.horizontalSpan = 2;
				groupModificationLData.horizontalAlignment = GridData.FILL;
				groupModificationLData.grabExcessHorizontalSpace = true;
				groupModification.setLayoutData(groupModificationLData);
				groupModification.setText("Modification");
				{
					labelModificationCount = new Label(groupModification, SWT.NONE);
					labelModificationCount.setText("Count:");
				}
				{
					textModificationCount = new Text(groupModification, SWT.NONE);
					GridData textModificationCountLData = new GridData();
					textModificationCountLData.horizontalAlignment = GridData.FILL;
					textModificationCountLData.heightHint = 15;
					textModificationCount.setLayoutData(textModificationCountLData);
					textModificationCount.setEditable(false);
					textModificationCount.setSize(26, 15);
				}
				{
					labelModificationLast = new Label(groupModification, SWT.NONE);
					labelModificationLast.setText("Last:");
				}
				{
					GridData textModificationLastLData = new GridData();
					textModificationLastLData.horizontalAlignment = GridData.FILL;
					textModificationLastLData.grabExcessHorizontalSpace = true;
					textModificationLast = new Text(groupModification, SWT.NONE);
					textModificationLast.setLayoutData(textModificationLastLData);
					textModificationLast.setEditable(false);
				}
			}
			{
				buttonClose = new Button(dialogShell, SWT.PUSH | SWT.CENTER);
				GridData buttonCloseLData = new GridData();
				buttonCloseLData.horizontalAlignment = GridData.END;
				buttonCloseLData.horizontalSpan = 2;
				buttonCloseLData.grabExcessHorizontalSpace = true;
				buttonClose.setLayoutData(buttonCloseLData);
				buttonClose.setText("Close");
				buttonClose.addMouseListener(new MouseAdapter()
				{
					@Override
					public void mouseDown(MouseEvent evt)
					{
						dialogShell.dispose();
					}
				});
			}

			fillFields();

			dialogShell.setLocation(getParent().toDisplay(100, 100));
			dialogShell.open();
			Display display = dialogShell.getDisplay();
			while (!dialogShell.isDisposed())
			{
				if (!display.readAndDispatch())
					display.sleep();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Fill in the {@link IClone} data in all fields.
	 */
	private void fillFields()
	{
		/*
		 * We'll need a store provider for extra info.
		 */
		IStoreProvider storeProvider = (IStoreProvider) CPCCorePlugin.getProviderRegistry().lookupProvider(
				IStoreProvider.class);
		assert (storeProvider != null);

		/*
		 * Get the file for this clone. 
		 */
		ICloneFile file = storeProvider.lookupCloneFile(clone.getFileUuid());
		if (file == null)
		{
			log.error("fillFields() - failed to obtain clone file for clone - clone: " + clone, new Throwable());
			return;
		}

		textProject.setText(file.getProject());
		textFile.setText(file.getPath());

		textOffset.setText(Integer.toString(clone.getOffset()));
		textLength.setText(Integer.toString(clone.getLength()));

		textCreationBy.setText(clone.getCreator());
		textCreationOn.setText(clone.getCreationDate().toString());

		for (String classification : clone.getClassifications())
			textClassification.append(classification + "\n");

		textStateState.setText(clone.getCloneState().toString());
		textStateOn.setText(clone.getCloneStateChangeDate().toString());
		textStateWeight.setText(Double.toString(clone.getCloneStateWeight()));
		textStateMessage.setText((clone.getCloneStateMessage() == null ? "" : clone.getCloneStateMessage()));
		textStateDismissed.setText((clone.getCloneStateDismissalDate() == null ? "" : clone
				.getCloneStateDismissalDate().toString()));

		/*
		 * Get group info.
		 */
		List<IClone> groupClones = null;

		if (clone.getGroupUuid() != null)
			groupClones = storeProvider.getClonesByGroup(clone.getGroupUuid());

		textGroupSize.setText(Integer.toString((groupClones == null ? 0 : groupClones.size())));

		if (groupClones != null)
		{
			for (IClone groupClone : groupClones)
			{
				if (groupClone.equals(clone))
					continue;

				/*
				 * Get the file info for this clone.
				 */
				ICloneFile groupFile = storeProvider.lookupCloneFile(groupClone.getFileUuid());
				if (groupFile == null)
				{
					log.error("fillFields() - unable to obtain clone file for group clone - groupClone: " + groupClone
							+ ", base clone: " + clone, new Throwable());
					continue;
				}

				textGroupMemberList.append((groupClone.getUuid().equals(clone.getOriginUuid()) ? "* " : "")
						+ groupFile.getProject() + " : " + groupFile.getPath() + " @ " + groupClone.getOffset() + "-"
						+ groupClone.getEndOffset() + "\n");
			}
		}

		/*
		 * Get modification info.
		 */

		int modCount = 0;
		ICloneModificationHistoryExtension history = (ICloneModificationHistoryExtension) storeProvider
				.getFullCloneObjectExtension(clone, ICloneModificationHistoryExtension.class);
		if (history != null)
			modCount = history.getCloneDiffs().size();

		textModificationCount.setText(Integer.toString(modCount));
		textModificationLast.setText(clone.getModificationDate().toString());
	}
}

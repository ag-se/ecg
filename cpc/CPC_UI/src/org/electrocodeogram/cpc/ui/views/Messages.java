package org.electrocodeogram.cpc.ui.views;


import org.eclipse.osgi.util.NLS;


public class Messages extends NLS
{
	private static final String BUNDLE_NAME = "org.electrocodeogram.cpc.ui.views.messages"; //$NON-NLS-1$
	public static String SimpleCloneView_ActionName_CloneDetails;
	public static String SimpleCloneView_ActionName_RemoveClone;
	public static String SimpleCloneView_ActionToolTip_CloneDetails;
	public static String SimpleCloneView_ActionToolTip_RemoveClone;
	public static String SimpleCloneView_ColumnHead_Creator;
	public static String SimpleCloneView_ColumnHead_Date;
	public static String SimpleCloneView_ColumnHead_Length;
	public static String SimpleCloneView_ColumnHead_Position;
	public static String SimpleCloneView_ColumnHead_Status;
	public static String SimpleCloneView_MessageDialogTitle_SimpleCloneView;
	public static String SimpleCloneView_SuccessMessage_CloneRemoved;
	public static String TreeCloneView_ActionName_CloneDetails;
	public static String TreeCloneView_ActionName_RemoveClone;
	public static String TreeCloneView_ActionToolTip_CloneDetails;
	public static String TreeCloneView_ActionToolTip_RemoveClone;
	public static String TreeCloneView_ColumnHead_Creator;
	public static String TreeCloneView_ColumnHead_File;
	public static String TreeCloneView_ColumnHead_Length;
	public static String TreeCloneView_ColumnHead_Position;
	public static String TreeCloneView_ColumnHead_Project;
	public static String TreeCloneView_ColumnHead_Status;
	public static String TreeCloneView_MessageDialogTitle_TreeCloneView;
	static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages()
	{
	}
}

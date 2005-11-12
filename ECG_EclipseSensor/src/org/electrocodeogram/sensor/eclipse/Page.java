/**
 * 
 */
package org.electrocodeogram.sensor.eclipse;

import org.eclipse.core.internal.runtime.Messages;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferencePageContainer;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
/**
 *
 */
public class Page implements IWorkbenchPreferencePage {

    private Control control;
    private Combo mRebuildIfNeeded;
    private Button mPurgeCacheButton;
    private Button mWarnBeforeLosingFilesets;
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    public void init(IWorkbench workbench) {
    //TODO Auto-generated method stub
        
        this.setTitle("ElectroCodeoGram Sensor");

    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferencePage#computeSize()
     */
    public Point computeSize() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferencePage#isValid()
     */
    public boolean isValid() {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferencePage#okToLeave()
     */
    public boolean okToLeave() {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferencePage#performCancel()
     */
    public boolean performCancel() {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferencePage#performOk()
     */
    public boolean performOk() {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferencePage#setContainer(org.eclipse.jface.preference.IPreferencePageContainer)
     */
    public void setContainer(IPreferencePageContainer preferencePageContainer) {
    // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferencePage#setSize(org.eclipse.swt.graphics.Point)
     */
    public void setSize(Point size) {
    // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public Control createContents(Composite parent) {
    
        Composite parentComposite = new Composite(parent, SWT.NULL);
        
        FormLayout layout = new FormLayout();
        
        parentComposite.setLayout(layout);

        createGeneralContents(parentComposite);

        return parentComposite;

    }
    
    private Composite createGeneralContents(Composite parent)
    {
        //
        // Build the composite for the general settings.
        //
        Group generalComposite = new Group(parent, SWT.NULL);
        generalComposite.setText("foobar");
        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        generalComposite.setLayout(layout);

        //
        // Get the preferences.
        //
        // Preferences prefs = EclipseSensorPlugin.getInstance().getPluginPreferences();

        //
        // Create a combo with the rebuild options
        //
        Composite rebuildComposite = new Composite(generalComposite, SWT.NULL);
        GridLayout layout2 = new GridLayout(3, false);
        layout2.marginHeight = 0;
        layout2.marginWidth = 0;
        rebuildComposite.setLayout(layout2);
        rebuildComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label lblRebuild = new Label(rebuildComposite, SWT.NULL);
        lblRebuild.setText("blabla");

        mRebuildIfNeeded = new Combo(rebuildComposite, SWT.READ_ONLY);
        mRebuildIfNeeded.setItems(new String[] { MessageDialogWithToggle.PROMPT,
            MessageDialogWithToggle.ALWAYS, MessageDialogWithToggle.NEVER });
//        mRebuildIfNeeded.select(mRebuildIfNeeded.indexOf(prefs
//                .getString(CheckstylePlugin.PREF_ASK_BEFORE_REBUILD)));

        //
        // Create button to purge the checker cache
        //

        mPurgeCacheButton = new Button(rebuildComposite, SWT.FLAT);
//        ImageDescriptor descriptor = CheckstylePlugin.imageDescriptorFromPlugin(
//                CheckstylePlugin.PLUGIN_ID, "icons/refresh.gif"); //$NON-NLS-1$
        //mPurgeCacheButton.setImage(descriptor.createImage());
        //mPurgeCacheButton.setToolTipText(Messages.CheckstylePreferencePage_btnRefreshCheckerCache);
        //mPurgeCacheButton.addSelectionListener(mController);
        GridData gd = new GridData();
        gd.horizontalAlignment = GridData.END;
        gd.grabExcessHorizontalSpace = true;
        gd.heightHint = 20;
        gd.widthHint = 20;
        mPurgeCacheButton.setLayoutData(gd);

        //
        // Create the "Fileset warning" check box.
        //
        mWarnBeforeLosingFilesets = new Button(generalComposite, SWT.CHECK);
        mWarnBeforeLosingFilesets.setText("pups!");
//        mWarnBeforeLosingFilesets.setSelection(prefs
//                .getBoolean(CheckstylePlugin.PREF_FILESET_WARNING));
       
        return generalComposite;
    }


    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#dispose()
     */
    public void dispose() {
    // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#getControl()
     */
    public Control getControl() {
        
        return this.control;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#getDescription()
     */
    public String getDescription() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#getErrorMessage()
     */
    public String getErrorMessage() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#getImage()
     */
    public Image getImage() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#getMessage()
     */
    public String getMessage() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#getTitle()
     */
    public String getTitle() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#performHelp()
     */
    public void performHelp() {
    // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#setDescription(java.lang.String)
     */
    public void setDescription(String description) {
    // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#setImageDescriptor(org.eclipse.jface.resource.ImageDescriptor)
     */
    public void setImageDescriptor(ImageDescriptor image) {
    // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#setTitle(java.lang.String)
     */
    public void setTitle(String title) {
    // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#setVisible(boolean)
     */
    public void setVisible(boolean visible) {
    // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent) {
        
        this.control = createContents(parent);
        
    }

}

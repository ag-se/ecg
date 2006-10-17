package org.electrocodeogram.sensor.eclipse.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.hackystat.stdext.sensor.eclipse.EclipseSensorPlugin;

/**
 * This class represents a preference page that
 * is contributed to the Preferences dialog. By 
 * subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows
 * us to create a page that is small and knows how to 
 * save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They
 * are stored in the preference store that belongs to
 * the main plug-in class. That way, preferences can
 * be accessed directly via the preference store.
 */

public class ECGMainPreferencePage
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {

    private BooleanFieldEditor eclipsePropertiesEditor;
    private RadioGroupFieldEditor serverTypeEditor;
    private StringFieldEditor serverAddressEditor;
    private IntegerFieldEditor serverPortEditor;
    private FileFieldEditor serverBatchEditor;
    private RadioGroupFieldEditor logLevelEditor;
    private StringFieldEditor logFileEditor;
    
	public ECGMainPreferencePage() {
		super(GRID);
		setPreferenceStore(EclipseSensorPlugin.getInstance().getPreferenceStore());
		setDescription("You can overwrite the sensor.properties setting specific to this workbench.\n" +
                "Just must restart the workbench for any changes to take effect.");
	}
	
	/**
	 * Creates the field editors. Field editors are abstractions of
	 * the common GUI blocks needed to manipulate various types
	 * of preferences. Each field editor knows how to save and
	 * restore itself.
	 */
	public void createFieldEditors() {
        
        eclipsePropertiesEditor = new BooleanFieldEditor(
                ECGPreferenceConstants.P_ECLIPSE_PROPERTIES,
                "&Use workbench specific settings",
                getFieldEditorParent());
        addField(eclipsePropertiesEditor);

        serverTypeEditor = new RadioGroupFieldEditor(
                ECGPreferenceConstants.P_SERVER_TYPE,
                "&Kind of ECGLab server",
                1,
                new String[][] { 
                        { "&None (disables ECG)", ECGPreferenceConstants.P_SERVER_TYPE_NULL }, 
                        { "&Inline", ECGPreferenceConstants.P_SERVER_TYPE_INLINE }, 
                        { "&Remote", ECGPreferenceConstants.P_SERVER_TYPE_REMOTE }
        }, getFieldEditorParent());
        addField(serverTypeEditor);

        serverBatchEditor = new FileFieldEditor(
                ECGPreferenceConstants.P_SERVER_BATCH, 
                "&Batch file to start ECGLab:", 
                getFieldEditorParent());
        addField(serverBatchEditor);

        serverAddressEditor = new StringFieldEditor(
                ECGPreferenceConstants.P_SERVER_ADDRESS, 
                "Ip &address of the server:", 
                getFieldEditorParent());
        addField(serverAddressEditor);

        serverPortEditor = new IntegerFieldEditor(
                ECGPreferenceConstants.P_SERVER_PORT, 
                "&Port number of the server:", 
                getFieldEditorParent());
        addField(serverPortEditor);

        logLevelEditor = new RadioGroupFieldEditor(
                        ECGPreferenceConstants.P_LOG_LEVEL,
                        "&Level of logging",
                        3,
                        new String[][] {
                                { "&Off", ECGPreferenceConstants.P_LOG_LEVEL_OFF }, 
                                { "&Error", ECGPreferenceConstants.P_LOG_LEVEL_ERROR }, 
                                { "&Warning", ECGPreferenceConstants.P_LOG_LEVEL_WARNING },
                                { "&Info", ECGPreferenceConstants.P_LOG_LEVEL_INFO }, 
                                { "&Verbose", ECGPreferenceConstants.P_LOG_LEVEL_VERBOSE }, 
                                { "&Packet", ECGPreferenceConstants.P_LOG_LEVEL_PACKET },
                                { "&Debug", ECGPreferenceConstants.P_LOG_LEVEL_DEBUG }
                        }, getFieldEditorParent());
        addField(logLevelEditor);

        logFileEditor = new StringFieldEditor(
                        ECGPreferenceConstants.P_LOG_FILE, 
                        "&File name for logging:", 
                        getFieldEditorParent());
        addField(logFileEditor);

        update();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.FieldEditorPreferencePage#performDefaults()
     */
    protected void performDefaults() {
        super.performDefaults();
        update(false);
    }
    
    private void update() {
        update(this.getPreferenceStore().getBoolean(ECGPreferenceConstants.P_ECLIPSE_PROPERTIES),
               this.getPreferenceStore().getString(ECGPreferenceConstants.P_SERVER_TYPE));        
    }
    
    private void update(boolean eclipseProps) {        
        update (eclipseProps,
                this.getPreferenceStore().getString(ECGPreferenceConstants.P_SERVER_TYPE));
    }

    private void update(boolean eclipseProps, String type) {
        boolean inline = type.equals(ECGPreferenceConstants.P_SERVER_TYPE_INLINE);
        boolean nullserver = type.equals(ECGPreferenceConstants.P_SERVER_TYPE_NULL);
        serverTypeEditor.setEnabled(eclipseProps, this.getFieldEditorParent());
        serverAddressEditor.setEnabled(eclipseProps && !nullserver, this.getFieldEditorParent());
        serverPortEditor.setEnabled(eclipseProps && !nullserver, this.getFieldEditorParent());
        serverBatchEditor.setEnabled(eclipseProps && inline, this.getFieldEditorParent());
        logLevelEditor.setEnabled(eclipseProps, this.getFieldEditorParent());
        logFileEditor.setEnabled(eclipseProps, this.getFieldEditorParent());                    
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.FieldEditorPreferencePage#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt) {
        // Check whether the eclipse's properties flag has changed
        if (evt.getSource() == this.eclipsePropertiesEditor) {
            update(((Boolean)evt.getNewValue()).booleanValue());
        }
        else if (evt.getSource() == this.serverTypeEditor) {
            update(true, evt.getNewValue().toString());
        }
        else
            super.propertyChange(evt);
    }
	
}
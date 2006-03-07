/*
 * Class: MSDTFilterIntermediateModule
 * Version: 1.0
 * Date: 16.10.2005
 * By: Frank@Schlesinger.com
 */

package org.electrocodeogram.module.intermediate.implementation;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.electrocodeogram.event.ValidEventPacket;
import org.electrocodeogram.logging.LogHelper;
import org.electrocodeogram.module.UIModule;
import org.electrocodeogram.module.intermediate.IntermediateModule;
import org.electrocodeogram.modulepackage.ModuleProperty;
import org.electrocodeogram.modulepackage.ModulePropertyException;
import org.electrocodeogram.msdt.MicroSensorDataType;
import org.electrocodeogram.system.ModuleSystem;

/**
 * This module is filtering events depending on the event's <em>MicroSensorDataType</em>.
 * It provides a GUI dialog where the user can choose, which <em>MicroSensorDataTypes</em>
 * are to be filtered.
 */
public class MSDTFilterIntermediateModule extends IntermediateModule implements UIModule {

    /**
     * This is the logger.
     */
    private static Logger logger = LogHelper
        .createLogger(MSDTFilterIntermediateModule.class.getName());

	/**
     * Name of the blocker configuration property of the module
     */
	private static final String CONF_PROPERTY = "Blocker";
	
	/**
     * Name of the filter1 configuration property of the module
     */
	private static final String FILTER1_PROPERTY = "Filter1";

	/**
     * Name of the filter1 configuration property of the module
     */
	private static final String FILTER2_PROPERTY = "Filter2";

	/**
     * Name of the filter1 configuration property of the module
     */
	private static final String FILTER3_PROPERTY = "Filter3";

	/**
     * Name of the filter1 configuration property of the module
     */
	private static final String FILTER4_PROPERTY = "Filter4";

	/**
     * Name of the filter1 configuration property of the module
     */
	private static final String FILTER5_PROPERTY = "Filter5";

	/**
     * This is a map of <em>MicroSensorDataTypes</em>, which are filtered.
     */
    private HashMap < MicroSensorDataType, Boolean > msdtFilterMap;

    /**
     * This is a map containing the checkboxes from the user dialog.
     */
    private HashMap < MicroSensorDataType, JCheckBox > chkMsdtSelection;

    /**
     * The panel with the checkboxes.
     */
    private JPanel pnlCheckBoxes;

    /**
     * The main panel of the dialog.
     */
    private JPanel pnlMain;

    /**
     * List of five compiled regular expressions 
     */
	private Pattern[] filters = new Pattern[5];
    
    /**
     * List of five MSDTs (in name representation) which correspond to
     * the filters. See the documentation of the module's properties
     * in the module.properties.xml
     */
	private String[] msdts = new String[5];
	
    /**
     * This creates the module instance. It is not to be
     * called by developers, instead it is called from the <em>ECG
     * ModuleRegistry</em> subsystem, when the user requested a new instance of this
     * module.
     * @param id
     *            This is the unique <code>String</code> id of the module
     * @param name
     *            This is the name which is assigned to the module
     *            instance
     */
    public MSDTFilterIntermediateModule(final String id, final String name) {
        super(id, name);

        logger.entering(this.getClass().getName(),
            "MSDTFilterIntermediateModule", new Object[] {id, name});

        logger.exiting(this.getClass().getName(),
            "MSDTFilterIntermediateModule");

    }

    /**
     * @see org.electrocodeogram.module.intermediate.IntermediateModule#analyse(org.electrocodeogram.event.ValidEventPacket)
     */
    @Override
    public final ValidEventPacket analyse(final ValidEventPacket packet) {

        logger.entering(this.getClass().getName(),
            "MSDTFilterIntermediateModule", new Object[] {packet});

        if (this.msdtFilterMap.get(packet.getMicroSensorDataType()) == Boolean.FALSE) {

	        logger.log(Level.FINE, "The event type is filtered.");

            logger.exiting(this.getClass().getName(), null);

            return null;
        }
		
		String eventString = packet.toString();
		String eventTypeName = packet.getMicroSensorDataType().getName();
		ValidEventPacket result = packet;
		
        // for each regex filter, filter the non-regex-compliant
		if (msdts[0] != null && eventTypeName.equals(msdts[0]))
			if (!filters[0].matcher(eventString).matches())
				result = null;
		if (msdts[1] != null && eventTypeName.equals(msdts[1]))
			if (!filters[1].matcher(eventString).matches())
				result = null;
		if (msdts[2] != null && eventTypeName.equals(msdts[2]))
			if (!filters[2].matcher(eventString).matches())
				result = null;
		if (msdts[3] != null && eventTypeName.equals(msdts[3]))
			if (!filters[3].matcher(eventString).matches())
				result = null;
		if (msdts[4] != null && eventTypeName.equals(msdts[4]))
			if (!filters[4].matcher(eventString).matches())
				result = null;
			
		if (result != null)			
			logger.log(Level.FINE, "The event fulfills one of the filters.");
		else
			logger.log(Level.FINE, "The event is filtered.");

		logger.exiting(this.getClass().getName(), null);

        return result;

    }

    /**
     * @see org.electrocodeogram.module.Module#propertyChanged(org.electrocodeogram.modulepackage.ModuleProperty)
     */
    @Override
    public final void propertyChanged(final ModuleProperty moduleProperty)
    throws ModulePropertyException {

        logger.entering(this.getClass().getName(), "propertyChanged",
            new Object[] {moduleProperty});

        logger.log(Level.INFO, "Request to set the property: "
                + moduleProperty.getName());

		String value = moduleProperty.getValue();
		
        if (moduleProperty.getName().equals(CONF_PROPERTY)) {

	        logger.log(Level.INFO, "Request to set the property: "
	                + moduleProperty.getName());

			configureFilter(value);
			
        } else if (moduleProperty.getName().equals(FILTER1_PROPERTY)) {
            logger.log(Level.INFO, "Request to set the property: "
                                   + moduleProperty.getName());
			extractFilterExpression(value, 0);

        } else if (moduleProperty.getName().equals(FILTER2_PROPERTY)) {
            logger.log(Level.INFO, "Request to set the property: "
                                   + moduleProperty.getName());
			extractFilterExpression(value, 1);

        } else if (moduleProperty.getName().equals(FILTER3_PROPERTY)) {
            logger.log(Level.INFO, "Request to set the property: "
                                   + moduleProperty.getName());
			extractFilterExpression(value, 2);

        } else if (moduleProperty.getName().equals(FILTER4_PROPERTY)) {
            logger.log(Level.INFO, "Request to set the property: "
                                   + moduleProperty.getName());
			extractFilterExpression(value, 3);

        } else if (moduleProperty.getName().equals(FILTER5_PROPERTY)) {
            logger.log(Level.INFO, "Request to set the property: "
                                   + moduleProperty.getName());
			extractFilterExpression(value, 4);

		} else {
	        
			logger.log(Level.WARNING,
	            "The module does not support a property with the given name: "
	                            + moduleProperty.getName());
	
	        logger.exiting(this.getClass().getName(), "propertyChanged");
	
	        throw new ModulePropertyException(
	            "The module does not support this property.", this.getName(),
	            this.getId(), moduleProperty.getName(), moduleProperty
	                .getValue());
	
	    }

        logger.exiting(this.getClass().getName(), "propertyChanged");
    }

	
	/**
     * Parses the expression {MSDTTYPE}={REGEX} filter properties
     * 
	 * @param value
	 * @param index
	 */
	private void extractFilterExpression(String value, int index) {
		
		if (value != null && value.length() > 0) {
			int sep = value.indexOf('=');
			if (sep > 0) {
				msdts[index] = new String(value.substring(0, sep));
				String pattern = value.substring(sep+1);
				filters[index] = Pattern.compile(pattern);
				return;
			}
		}
		msdts[index] = null;
		filters[index] = null;
	}

	/**
     * Updates the list of MSDTs
     * 
     * @see org.electrocodeogram.module.Module#update()
     */
    @Override
    public final void update() {

        logger.entering(this.getClass().getName(), "update");

        setFilterMap();

        logger.exiting(this.getClass().getName(), "update");

    }


    /**
     * This method fills the {@link #msdtFilterMap}
     * with the currently registered <em>MicroSensorDataTypes</em>.
     */
    private void setFilterMap() {

        logger.entering(this.getClass().getName(), "setFilterMap");

        this.msdtFilterMap = new HashMap < MicroSensorDataType, Boolean >();

        MicroSensorDataType[] msdts = ModuleSystem.getInstance()
            .getMicroSensorDataTypes();

		// First, include all avaiable MSDTs as keys in the HashMap
		for (MicroSensorDataType msdt : msdts) {
            this.msdtFilterMap.put(msdt, Boolean.TRUE);
        }

		// Now (re-)set the correct values of the filter configuration
		try {
			configureFilter(this.getModuleProperty(CONF_PROPERTY).getValue());
		} catch (ModulePropertyException e) {
			logger.log(Level.WARNING, "The filter module is supposed to support the property " +  CONF_PROPERTY);
		}
		
        logger.exiting(this.getClass().getName(), "setFilterMap");
    }

    /**
     * @see org.electrocodeogram.module.intermediate.IntermediateModule#initialize()
     */
    @Override
    public final void initialize() {

        logger.entering(this.getClass().getName(), "initialize");

        setFilterMap();

        this.setProcessingMode(ProcessingMode.FILTER);

        logger.exiting(this.getClass().getName(), "initialize");
    }

    /**
     * This method is used to configure this filter module.
     * It creates and displays a user dialog to set the filter rules.
     */
    public final void configureFilter(String filterExpression) {

        logger.entering(this.getClass().getName(), "configureFilter");

		if (filterExpression != null) {
			
			for (MicroSensorDataType msdt : this.msdtFilterMap.keySet()) {
	
				// Use regular expression ".*msdt.TYPE.xsd.*" to be found in 
				// the configuration property
				if (filterExpression.matches(".*" + msdt.getName() + ".*")) {
	
					this.msdtFilterMap.put(msdt, Boolean.FALSE);
	
				} else {
	
					this.msdtFilterMap.put(msdt, Boolean.TRUE);
	
				}
	        }
		}

        logger.exiting(this.getClass().getName(), "configureFilter");
    }

    /**
     * This method updates the filter rules according to the current user selection.
     *
     */
    final void updateMsdtFilterMap() {

        logger.entering(this.getClass().getName(), "updateMsdtFilterMap");

        String filterExpression = "";
		
		for (MicroSensorDataType msdt : this.chkMsdtSelection.keySet()) {
            JCheckBox chkBox = this.chkMsdtSelection.get(msdt);

            this.msdtFilterMap.remove(msdt);

            if (chkBox.isSelected()) {

				this.msdtFilterMap.put(msdt, Boolean.FALSE);
				// Compile new filter property string
				filterExpression += (filterExpression.length() == 0 ? "" : ",") + msdt.getName();

			} else {

				this.msdtFilterMap.put(msdt, Boolean.TRUE);

			}
        }
		
		// set the new value of the filter property
		try {
			this.getModuleProperty(CONF_PROPERTY).setValue(filterExpression);
		} catch (ModulePropertyException e) {
			logger.log(Level.WARNING, "The filter module is supposed to support the property " +  CONF_PROPERTY);
		}
			

        logger.exiting(this.getClass().getName(), "updateMsdtFilterMap");
    }

    /**
     * Creates and displays a checkbox for every <em>MicroSensorDataType</em>.
     *
     */
    final void initializeCheckBoxes() {

        logger.entering(this.getClass().getName(), "initializeCheckBoxes");

        this.chkMsdtSelection = new HashMap < MicroSensorDataType, JCheckBox >();

        for (MicroSensorDataType msdt : this.msdtFilterMap.keySet()) {
            if (this.msdtFilterMap.get(msdt) == Boolean.TRUE) {
				this.chkMsdtSelection.put(msdt, new JCheckBox(msdt.getName(),
                    false));
            } else {
                this.chkMsdtSelection.put(msdt, new JCheckBox(msdt.getName(),
                    true));
            }

        }

        logger.exiting(this.getClass().getName(), "initializeCheckBoxes");
    }

    /**
     * Resets the selections of the checkboxes to the current filter rules.
     *
     */
    final void refreshCheckBoxes() {

        logger.entering(this.getClass().getName(), "refreshCheckBoxes");

        for (MicroSensorDataType msdt : this.msdtFilterMap.keySet()) {
            if (this.msdtFilterMap.get(msdt) == Boolean.TRUE) {
                this.chkMsdtSelection.get(msdt).setSelected(false);
            } else {
                this.chkMsdtSelection.get(msdt).setSelected(true);
            }

        }

        logger.exiting(this.getClass().getName(), "refreshCheckBoxes");
    }

    /**
     * Creates and returns the panel with the checkboxes.
     * @return The panel with the checkboxes
     */
    final JPanel createCheckBoxPanel() {

        logger.entering(this.getClass().getName(), "createCheckBoxPanel");
        initializeCheckBoxes();
        this.pnlCheckBoxes = new JPanel();

        JPanel pnlLeft = new JPanel();

        pnlLeft.setLayout(new BoxLayout(pnlLeft, BoxLayout.Y_AXIS));

        JPanel pnlRight = new JPanel();

        pnlRight.setLayout(new BoxLayout(pnlRight, BoxLayout.Y_AXIS));

        boolean left = true;

        for (JCheckBox chkMsdt : this.chkMsdtSelection.values()) {
            if (left) {
                pnlLeft.add(chkMsdt);

                left = false;
            } else {
                pnlRight.add(chkMsdt);

                left = true;
            }

        }

        this.pnlCheckBoxes.add(pnlLeft);

        this.pnlCheckBoxes.add(pnlRight);

        logger.exiting(this.getClass().getName(), "createCheckBoxPanel",
            this.pnlCheckBoxes);

        return this.pnlCheckBoxes;
    }

    /**
     * Creates and returns the main panel of the dialog.
     * @return The main panel of the dialog
     */
    final JPanel createMainPanel() {

        logger.entering(this.getClass().getName(), "createMainPanel");

        this.pnlMain = new JPanel();

        this.pnlMain.setLayout(new BorderLayout());

        this.pnlMain.add(new JLabel(
            "Select the MicroSensorDataTypes that shall be filtered out"),
            BorderLayout.NORTH);

        this.pnlMain.add(createCheckBoxPanel(), BorderLayout.CENTER);

        this.pnlMain.add(getButtonPanel(), BorderLayout.SOUTH);

        logger.exiting(this.getClass().getName(), "createMainPanel",
            this.pnlMain);

        return this.pnlMain;
    }

    /**
     * Creates and returns the panel with the buttons.
     * @return The panel with the buttons
     */
    private Component getButtonPanel() {

        logger.entering(this.getClass().getName(), "getButtonPanel");

        JButton btnOK = new JButton("OK");

        btnOK.addActionListener(new ActionListener() {

            public void actionPerformed(@SuppressWarnings("unused")
            final ActionEvent e) {
                updateMsdtFilterMap();
				closePanel();
            }
        });

        JButton btnApply = new JButton("Apply");

        btnApply.addActionListener(new ActionListener() {

            public void actionPerformed(@SuppressWarnings("unused")
            final ActionEvent e) {
                updateMsdtFilterMap();
            }
        });

        JButton btnCancel = new JButton("Cancel");

        btnCancel.addActionListener(new ActionListener() {

            public void actionPerformed(@SuppressWarnings("unused")
            final ActionEvent e) {
				closePanel();
            }
        });

        JButton btnClearAll = new JButton("Clear all");

        btnClearAll.addActionListener(new ActionListener() {

            @SuppressWarnings( {"synthetic-access", "unqualified-field-access"})
            public void actionPerformed(@SuppressWarnings("unused")
            final ActionEvent e) {
                for (JCheckBox chkMsdt : chkMsdtSelection.values()) {
                    chkMsdt.setSelected(false);
                }

            }
        });

        JButton btnRestore = new JButton("Restore");

        btnRestore.addActionListener(new ActionListener() {

            public void actionPerformed(@SuppressWarnings("unused")
            final ActionEvent e) {
                refreshCheckBoxes();

            }

        });

        JPanel pnlButtons = new JPanel();

        pnlButtons.add(btnRestore);

        pnlButtons.add(btnClearAll);

        pnlButtons.add(btnCancel);

        pnlButtons.add(btnApply);

		pnlButtons.add(btnOK);

        logger.exiting(this.getClass().getName(), "getButtonPanel", pnlButtons);

        return pnlButtons;
    }

    /**
     * @see org.electrocodeogram.module.UIModule#getPanelName()
     */
    public final String getPanelName() {

        return "Configure Filter";
    }

    /**
     * @see org.electrocodeogram.module.UIModule#getPanel()
     */
    public final JPanel getPanel() {

        this.pnlMain = createMainPanel(); 
        
        return this.pnlMain;
    }
	
	/**
	 * Closes the Modules Configuration panel frame
	 * Note: This is probably quite a hack
	 */
	private final void closePanel() {

		this.pnlMain.getRootPane().getParent().setVisible(false);
		
	}

}

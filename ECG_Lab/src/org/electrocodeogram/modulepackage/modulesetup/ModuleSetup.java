/*
 * Class: ModuleSetup
 * Version: 1.0
 * Date: 18.10.2005
 * By: Frank@Schlesinger.com
 */

package org.electrocodeogram.modulepackage.modulesetup;

import java.util.ArrayList;
import java.util.logging.Logger;

import org.electrocodeogram.logging.LogHelper;

/**
 * A <em>ModuleSetup</em> is a list of {@link ModuleConfiguration} elements, each
 * containing the confiiguration of one module instance from the ECG Lab.
 * The <em>ModuleSetup</em> therefore is an image of the configuration of modules
 * in the ECG Lab and can be stored into a file. Of course a <em>ModuleSetup</em>
 * can also be loaded from a file into the ECG Lab.
 */
public class ModuleSetup {

    /**
     * This is the logger.
     */
    private static Logger logger = LogHelper.createLogger(ModuleSetup.class
        .getName());

    /**
     * This is the list containing the {@link ModuleConfiguration} elements.
     */
    private ArrayList<ModuleConfiguration> moduleSetupList;

    /**
     * Creates a <em>ModuleSetup</em>.
     *
     */
    public ModuleSetup() {

        logger.entering(this.getClass().getName(), "ModuleSetup");

        this.moduleSetupList = new ArrayList<ModuleConfiguration>();

        logger.exiting(this.getClass().getName(), "ModuleSetup");
    }

    /**
     * Adds a <em>ModuleConfiguration</em> for a module instance to this <em>ModuleSetup</em>.
     * @param moduleConfiguration Is the new <em>ModuleConfiguration</em> to add
     */
    public final void addModuleConfiguration(
        final ModuleConfiguration moduleConfiguration) {

        logger.entering(this.getClass().getName(), "addModuleConfiguration",
            new Object[] {moduleConfiguration});

        this.moduleSetupList.add(moduleConfiguration);

        logger.exiting(this.getClass().getName(), "addModuleConfiguration");
    }

    /**
     * Returns all <em>ModuleConfigurations</em> of this <em>ModuleSetup</em>.
     * @return All <em>ModuleConfigurations</em> of this <em>ModuleSetup</em>
     */
    public final ModuleConfiguration[] getModuleConfigurations() {

        logger.entering(this.getClass().getName(), "getModuleConfigurations");

        logger.exiting(this.getClass().getName(), "getModuleConfigurations",
            this.moduleSetupList
                .toArray(new ModuleConfiguration[this.moduleSetupList.size()]));

        return this.moduleSetupList
            .toArray(new ModuleConfiguration[this.moduleSetupList.size()]);
    }
}

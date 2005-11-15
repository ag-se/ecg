package org.electrocodeogram.modulepackage;

/**
 * Lists the three different types of
 * modules.
 */
public enum ModuleType {
    /**
     * A module of this type is not able to
     * be connected to other modules, but other modules are able
     * to be connected to it. Its purpose is to read in events
     * from locations external to the <em>ECG Lab</em>.
     */
    SOURCE_MODULE,
    /**
     * This is a module type where the module is connectable to
     * other <em>SOURCE_MODULES</em> and
     * <em>INTERMEDIATE_MODULES</em>. Its purpose is to analyse
     * incoming events and pass events to other modules.
     */
    INTERMEDIATE_MODULE,
    /**
     * A module of this type is only able to
     * be connected to other <em>SOURCE_MODULES</em> and
     * <em>INTERMEDIATE_MODULES</em>. Its purpose is to write
     * events into locations external to the <em>ECG Lab</em>.
     */
    TARGET_MODULE
}

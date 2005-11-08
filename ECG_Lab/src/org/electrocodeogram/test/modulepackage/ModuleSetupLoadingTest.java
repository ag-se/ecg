/**
 * 
 */
package org.electrocodeogram.test.modulepackage;

import java.io.File;

import org.electrocodeogram.module.registry.IModuleRegistry;
import org.electrocodeogram.module.registry.ModuleSetupLoadException;
import org.electrocodeogram.modulepackage.classloader.ModuleClassLoaderInitializationException;
import org.electrocodeogram.system.ISystem;
import org.electrocodeogram.system.Core;

import junit.framework.TestCase;

/**
 * 
 */
public class ModuleSetupLoadingTest extends TestCase {

    private IModuleRegistry _moduleRegistry;

    private static ISystem _systemRoot = org.electrocodeogram.system.System
        .getInstance();

    protected void setUp() {
        this._moduleRegistry = _systemRoot.getModuleRegistry();
    }

    public void testIfInvalidModuleSetupCausesExceptionWithDuplicateModuleId() {
        try {
            this._moduleRegistry.setModuleDirectory(new File("modules"));

            this._moduleRegistry.loadModuleSetup(new File(
                "testmodulesetups\\duplicateModuleId\\module.setup"));

            assertTrue(false);
        } catch (ModuleClassLoaderInitializationException e) {
            assertTrue(false);
        } catch (ModuleSetupLoadException e) {
            assertTrue(true);

        }

    }

    public void testIfNotExistingModuleSetupCausesException() {
        try {
            this._moduleRegistry.setModuleDirectory(new File("modules"));

            this._moduleRegistry.loadModuleSetup(new File(
                "testmodulesetups\\emptyModuleSetup"));

            assertTrue(false);
        } catch (ModuleClassLoaderInitializationException e) {
            assertTrue(false);
        } catch (ModuleSetupLoadException e) {

            assertTrue(true);

        }

    }

    public void testIfInvalidModuleSetupCausesExceptionWithInvalidRootNode() {
        try {
            this._moduleRegistry.setModuleDirectory(new File("modules"));

            this._moduleRegistry.loadModuleSetup(new File(
                "testmodulesetups\\invalidRootNode\\module.setup"));

            assertTrue(false);
        } catch (ModuleClassLoaderInitializationException e) {
            assertTrue(false);
        } catch (ModuleSetupLoadException e) {

            assertTrue(true);

        }

    }

    public void testIfInvalidModuleSetupCausesExceptionWithInvalidModuleNode() {
        try {
            this._moduleRegistry.setModuleDirectory(new File("modules"));

            this._moduleRegistry.loadModuleSetup(new File(
                "testmodulesetups\\invalidModuleNode\\module.setup"));

            assertTrue(false);
        } catch (ModuleClassLoaderInitializationException e) {
            assertTrue(false);
        } catch (ModuleSetupLoadException e) {

            assertTrue(true);

        }

    }

    public void testIfInvalidModuleSetupCausesExceptionWithIllegalActiveAttribute() {
        try {
            this._moduleRegistry.setModuleDirectory(new File("modules"));

            this._moduleRegistry.loadModuleSetup(new File(
                "testmodulesetups\\illegalActiveAttribute\\module.setup"));

            assertTrue(false);
        } catch (ModuleClassLoaderInitializationException e) {
            assertTrue(false);
        } catch (ModuleSetupLoadException e) {

            assertTrue(true);

        }

    }

    public void testIfInvalidModuleSetupCausesExceptionWithEmptyModuleName() {
        try {
            this._moduleRegistry.setModuleDirectory(new File("modules"));

            this._moduleRegistry.loadModuleSetup(new File(
                "testmodulesetups\\emptyModuleName\\module.setup"));

            assertTrue(false);
        } catch (ModuleClassLoaderInitializationException e) {
            assertTrue(false);
        } catch (ModuleSetupLoadException e) {

            assertTrue(true);

        }

    }

    public void testIfInvalidModuleSetupCausesExceptionWithIllegalPropertyType() {
        try {
            this._moduleRegistry.setModuleDirectory(new File("modules"));

            this._moduleRegistry.loadModuleSetup(new File(
                "testmodulesetups\\illegalPropertyType\\module.setup"));

            assertTrue(false);
        } catch (ModuleClassLoaderInitializationException e) {
            assertTrue(false);
        } catch (ModuleSetupLoadException e) {

            assertTrue(true);

        }

    }

    public void testIfInvalidModuleSetupCausesExceptionWithUnknownModuleClass() {
        try {
            this._moduleRegistry.setModuleDirectory(new File("modules"));

            this._moduleRegistry.loadModuleSetup(new File(
                "testmodulesetups\\unknownModuleClass\\module.setup"));

            assertTrue(false);
        } catch (ModuleClassLoaderInitializationException e) {
            assertTrue(false);
        } catch (ModuleSetupLoadException e) {

            assertTrue(true);

        }

    }

    public void testIfInvalidModuleSetupCausesExceptionWithConnectedToUnknownModule() {
        try {
            this._moduleRegistry.setModuleDirectory(new File("modules"));

            this._moduleRegistry.loadModuleSetup(new File(
                "testmodulesetups\\connectedToUnknownModule\\module.setup"));

            assertTrue(false);
        } catch (ModuleClassLoaderInitializationException e) {
            assertTrue(false);
        } catch (ModuleSetupLoadException e) {

            assertTrue(true);

        }

    }

    public void testIfValidModuleSetupCausesNoException() {

        try {
            this._moduleRegistry.setModuleDirectory(new File("modules"));

            this._moduleRegistry.loadModuleSetup(new File(
                "testmodulesetups\\validModuleSetup\\module.setup"));

            assertTrue(true);
        } catch (ModuleClassLoaderInitializationException e) {
            assertTrue(false);
        } catch (ModuleSetupLoadException e) {

            assertTrue(false);

        }

    }

}

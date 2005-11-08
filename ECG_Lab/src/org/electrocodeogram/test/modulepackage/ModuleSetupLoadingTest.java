package org.electrocodeogram.test.modulepackage;

import java.io.File;

import org.electrocodeogram.module.registry.IModuleRegistry;
import org.electrocodeogram.module.registry.ModuleSetupLoadException;
import org.electrocodeogram.modulepackage.classloader.ModuleClassLoaderInitializationException;

import junit.framework.TestCase;

/**
 * Collection of testcases that check the loading of
 * <em>ModuleSetups</em> from "./ECG_Lab/testmodulesetups/". Both
 * valid and invalid/not wellformed <em>ModuleSetup</em> files are
 * tested.
 */
public class ModuleSetupLoadingTest extends TestCase {

    /**
     * A reference to the object wich is loading the
     * <em>ModuleSetups</em>.
     */
    private IModuleRegistry moduleRegistry;

    /**
     * @see junit.framework.TestCase#setUp() Gets the
     *      {@link #moduleRegistry}.
     */
    @Override
    protected final void setUp() {
        this.moduleRegistry = org.electrocodeogram.system.System.getInstance()
            .getModuleRegistry();
    }

    /**
     * Testcase MS01 according to the document TESTPLAN Version 2.1 or
     * higher. This testcase is trying to load the
     * <em>ModuleSetup</em> from
     * "testmodulesetups/duplicateModuleId". This <em>ModuleSetup</em>
     * contains two identical moodule ids. The expected result is that
     * the test is throwing a {@link ModuleSetupLoadException}.
     */
    public final void testIfInvalidModuleSetupCausesExceptionForDuplicateModuleId() {
        try {
            this.moduleRegistry.setModuleDirectory(new File("modules"));

            this.moduleRegistry.loadModuleSetup(new File("testmodulesetups"
                                                         + File.separator
                                                         + "duplicateModuleId"
                                                         + File.separator
                                                         + "module.setup"));

            assertTrue(false);
        } catch (ModuleClassLoaderInitializationException e) {
            assertTrue(false);
        } catch (ModuleSetupLoadException e) {
            assertTrue(true);

        }

    }

    /**
     * Testcase MS02 according to the document TESTPLAN Version 2.1 or
     * higher. This testcase is trying to load the
     * <em>ModuleSetup</em> from
     * "testmodulesetups/emptyModuleSetup". This <em>ModuleSetup</em>
     * is empty. The expected result is that the test is throwing a
     * {@link ModuleSetupLoadException}.
     */
    public final void testIfEmptyModuleSetupCausesException() {
        try {
            this.moduleRegistry.setModuleDirectory(new File("modules"));

            this.moduleRegistry.loadModuleSetup(new File("testmodulesetups"
                                                         + File.separator
                                                         + "emptyModuleSetup"));

            assertTrue(false);
        } catch (ModuleClassLoaderInitializationException e) {
            assertTrue(false);
        } catch (ModuleSetupLoadException e) {

            assertTrue(true);

        }

    }

    /**
     * Testcase MS03 according to the document TESTPLAN Version 2.1 or
     * higher. This testcase is trying to load the
     * <em>ModuleSetup</em> from "testmodulesetups/invalidRootNode".
     * This <em>ModuleSetup's</em> root node is not
     * &lt;modulesetup&gt;. The expected result is that the test is
     * throwing a {@link ModuleSetupLoadException}.
     */
    public final void testIfInvalidModuleSetupCausesExceptionForInvalidRootNode() {
        try {
            this.moduleRegistry.setModuleDirectory(new File("modules"));

            this.moduleRegistry.loadModuleSetup(new File("testmodulesetups"
                                                         + File.separator
                                                         + "invalidRootNode"
                                                         + File.separator
                                                         + "module.setup"));

            assertTrue(false);
        } catch (ModuleClassLoaderInitializationException e) {
            assertTrue(false);
        } catch (ModuleSetupLoadException e) {

            assertTrue(true);

        }

    }

    /**
     * Testcase MS04 according to the document TESTPLAN Version 2.1 or
     * higher. This testcase is trying to load the
     * <em>ModuleSetup</em> from
     * "testmodulesetups/invalidModuleNode". This
     * <em>ModuleSetup's</em> root node is not &lt;modulesetup&gt;.
     * The expected result is that the test is throwing a
     * {@link ModuleSetupLoadException}.
     */
    public final void testIfInvalidModuleSetupCausesExceptionForInvalidModuleNode() {
        try {
            this.moduleRegistry.setModuleDirectory(new File("modules"));

            this.moduleRegistry.loadModuleSetup(new File("testmodulesetups"
                                                         + File.separator
                                                         + "invalidModuleNode"
                                                         + File.separator
                                                         + "module.setup"));

            assertTrue(false);
        } catch (ModuleClassLoaderInitializationException e) {
            assertTrue(false);
        } catch (ModuleSetupLoadException e) {

            assertTrue(true);

        }

    }

    /**
     * Testcase MS05 according to the document TESTPLAN Version 2.1 or
     * higher. This testcase is trying to load the
     * <em>ModuleSetup</em> from
     * "testmodulesetups/illegalActiveAttribute". An "active"
     * attribute is neither "true" nor "false" here. The expected
     * result is that the test is throwing a
     * {@link ModuleSetupLoadException}.
     */
    public final void testIfInvalidModuleSetupCausesExceptionForIllegalActiveAttribute() {
        try {
            this.moduleRegistry.setModuleDirectory(new File("modules"));

            this.moduleRegistry.loadModuleSetup(new File(
                "testmodulesetups" + File.separator + "illegalActiveAttribute"
                                + File.separator + "module.setup"));

            assertTrue(false);
        } catch (ModuleClassLoaderInitializationException e) {
            assertTrue(false);
        } catch (ModuleSetupLoadException e) {

            assertTrue(true);

        }

    }

    /**
     * Testcase MS06 according to the document TESTPLAN Version 2.1 or
     * higher. This testcase is trying to load the
     * <em>ModuleSetup</em> from
     * "testmodulesetups/illegalActiveAttribute". An "name" attribute
     * is empty. The expected result is that the test is throwing a
     * {@link ModuleSetupLoadException}.
     */
    public final void testIfInvalidModuleSetupCausesExceptionForEmptyModuleName() {
        try {
            this.moduleRegistry.setModuleDirectory(new File("modules"));

            this.moduleRegistry.loadModuleSetup(new File("testmodulesetups"
                                                         + File.separator
                                                         + "emptyModuleName"
                                                         + File.separator
                                                         + "module.setup"));

            assertTrue(false);
        } catch (ModuleClassLoaderInitializationException e) {
            assertTrue(false);
        } catch (ModuleSetupLoadException e) {

            assertTrue(true);

        }

    }

    /**
     * Testcase MS07 according to the document TESTPLAN Version 2.1 or
     * higher. This testcase is trying to load the
     * <em>ModuleSetup</em> from
     * "testmodulesetups/illegalPropertyType". A &lt;propertyType&gt;
     * value is not a known java class name. The expected result is
     * that the test is throwing a {@link ModuleSetupLoadException}.
     */
    public final void testIfInvalidModuleSetupCausesExceptionForIllegalPropertyType() {
        try {
            this.moduleRegistry.setModuleDirectory(new File("modules"));

            this.moduleRegistry.loadModuleSetup(new File(
                "testmodulesetups" + File.separator + "illegalPropertyType"
                                + File.separator + "module.setup"));

            assertTrue(false);
        } catch (ModuleClassLoaderInitializationException e) {
            assertTrue(false);
        } catch (ModuleSetupLoadException e) {

            assertTrue(true);

        }

    }

    /**
     * Testcase MS08 according to the document TESTPLAN Version 2.1 or
     * higher. This testcase is trying to load the
     * <em>ModuleSetup</em> from
     * "testmodulesetups/unknownModuleClass". The module class is
     * unknown. The expected result is that the test is throwing a
     * {@link ModuleSetupLoadException}.
     */
    public final void testIfInvalidModuleSetupCausesExceptionForUnknownModuleClass() {
        try {
            this.moduleRegistry.setModuleDirectory(new File("modules"));

            this.moduleRegistry.loadModuleSetup(new File("testmodulesetups"
                                                         + File.separator
                                                         + "unknownModuleClass"
                                                         + File.separator
                                                         + "module.setup"));

            assertTrue(false);
        } catch (ModuleClassLoaderInitializationException e) {
            assertTrue(false);
        } catch (ModuleSetupLoadException e) {

            assertTrue(true);

        }

    }

    /**
     * Testcase MS09 according to the document TESTPLAN Version 2.1 or
     * higher. This testcase is trying to load the
     * <em>ModuleSetup</em> from
     * "testmodulesetups/connectedToUnknownModule". A module is
     * connected to an unknown module. The expected result is that the
     * test is throwing a {@link ModuleSetupLoadException}.
     */
    public final void testIfInvalidModuleSetupCausesExceptionForConnectedToUnknownModule() {
        try {
            this.moduleRegistry.setModuleDirectory(new File("modules"));

            this.moduleRegistry.loadModuleSetup(new File(
                "testmodulesetups" + File.separator
                                + "connectedToUnknownModule" + File.separator
                                + "module.setup"));

            assertTrue(false);
        } catch (ModuleClassLoaderInitializationException e) {
            assertTrue(false);
        } catch (ModuleSetupLoadException e) {

            assertTrue(true);

        }

    }

    /**
     * Testcase MS10 according to the document TESTPLAN Version 2.1 or
     * higher. This testcase is trying to load the
     * <em>ModuleSetup</em> from
     * "testmodulesetups/validModuleSetup". This <em>ModuleSetup</em> is valid.
     * The expected result is that the
     * <em>ModuleSetup</em> id loaded and the test does not cause any exceptions.
     */
    public final void testIfValidModuleSetupCausesNoException() {

        try {
            this.moduleRegistry.setModuleDirectory(new File("modules"));

            this.moduleRegistry.loadModuleSetup(new File("testmodulesetups"
                                                         + File.separator
                                                         + "validModuleSetup"
                                                         + File.separator
                                                         + "module.setup"));

            assertTrue(true);
        } catch (ModuleClassLoaderInitializationException e) {
            assertTrue(false);
        } catch (ModuleSetupLoadException e) {

            assertTrue(false);

        }

    }

}

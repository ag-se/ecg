package org.electrocodeogram.test.modulepackage;

import java.io.File;
import java.util.Observable;
import java.util.Observer;

import junit.framework.TestCase;

import org.electrocodeogram.module.registry.ModuleRegistry;
import org.electrocodeogram.modulepackage.ModuleDescriptor;
import org.electrocodeogram.modulepackage.classloader.ModuleClassLoaderInitializationException;

import utmj.threaded.RetriedAssert;

/**
 * Collects testcases to check the loading of <em>ModulePackages</em>.
 * Both valid and invalid <em>ModulePackages</em> are loaded from
 * the "./ECG_Lab/testmodules" directory.
 */
public class ModulePackageLoadingTest extends TestCase implements Observer {

    /**
     * Is the delay for the {@link RetriedAssert}.
     */
    private static final int ASSERT_DELAY = 100;

    /**
     * Is the whole time to wait for the {@link RetriedAssert}.
     */
    private static final int TIME_TO_WAIT = 5000;

    /**
     * A reference to the object that is loading the
     * <em>ModulePackages</em>.
     */
    private ModuleRegistry moduleRegistry;

    /**
     * This is true after a <em>ModulePackage</em> has been loaded
     * successfully.
     */
    private boolean result = false;

    /**
     * @see junit.framework.TestCase#setUp() Releases the
     *      {@link #moduleRegistry}.
     */
    @Override
    protected final void setUp() {

        this.moduleRegistry = null;
    }

    /**
     * Testcase MP01 according to the document TESTPLAN Version 2.1 or
     * higher. This testcase is trying to load the
     * <em>ModulePackage</em> from
     * "testmodules/notExistingDirectory". This <em>ModulePackage</em>
     * is not existing. The expected result is that the test is 
     * throwing a {@link ModuleClassLoaderInitializationException}.
     */
    public final void testIfNotExistingModuleDirectoryCausesException() {
        this.moduleRegistry = new ModuleRegistry();

        this.moduleRegistry.addObserver(this);

        try {
            this.moduleRegistry.setModuleDirectory(new File(
                "testmodules" + File.separator + "notExistingDirectory"));

            assertTrue(false);
        } catch (ModuleClassLoaderInitializationException e) {
            assertTrue(true);
        }

    }

    /**
     * Testcase MP02 according to the document TESTPLAN Version 2.1 or
     * higher. This testcase is trying to load the
     * <em>ModulePackage</em> from "testmodules/emptyDirectory".
     * This <em>ModulePackage</em> is empty. The expected result is
     * that the <em>ModulePackage</em> is simply ignored.
     */
    public final void testIfEmptyModuleDirectoryIsIgnored() {
        this.moduleRegistry = new ModuleRegistry();

        this.moduleRegistry.addObserver(this);

        try {
            this.moduleRegistry.setModuleDirectory(new File(
                "testmodules" + File.separator + "emptyDirectory"));

            String[] modulePackage = this.moduleRegistry
                .geModulePackageIds();

            assertEquals(modulePackage.length, 0);

        } catch (ModuleClassLoaderInitializationException e) {
            assertTrue(false);
        }

    }

    /**
     * Testcase MP03 according to the document TESTPLAN Version 2.1 or
     * higher. This testcase is trying to load the two
     * <em>ModulePackage</em> from "testmodules/Duplicate
     * ModulePackageId". These <em>ModulePackages</em> are having
     * the same id. The expected result is that the first
     * <em>ModulePackage</em> is loaded and the second is simply
     * ignored.
     */
    public final void testIfDuplicateModulePackageIdIsIgnored() {
        this.moduleRegistry = new ModuleRegistry();

        this.moduleRegistry.addObserver(this);

        try {
            this.moduleRegistry.setModuleDirectory(new File(
                "testmodules" + File.separator + "Duplicate ModulePackageId"));

            String[] modulePackage = this.moduleRegistry
                .geModulePackageIds();

            assertEquals(modulePackage.length, 1);

        } catch (ModuleClassLoaderInitializationException e) {
            assertTrue(false);
        }

    }

    /**
     * Testcase MP04 according to the document TESTPLAN Version 2.1 or
     * higher. This testcase is trying to load the
     * <em>ModulePackage</em> from
     * "testmodules/noModulePropertyFile". This <em>ModulePackage</em>
     * is missing a "module.properties.xml" file. The expected result
     * is that the test is throwing a
     * {@link ModuleClassLoaderInitializationException}.
     */
    public final void testIfNoModulePropertyFileCausesException() {
        this.moduleRegistry = new ModuleRegistry();

        this.moduleRegistry.addObserver(this);

        try {
            this.moduleRegistry.setModuleDirectory(new File(
                "testmodules" + File.separator +"noModulePropertyFile"));

            String[] modulePackage = this.moduleRegistry
                .geModulePackageIds();

            assertEquals(modulePackage.length, 0);

        } catch (ModuleClassLoaderInitializationException e) {
            assertTrue(false);
        }

    }

    /**
     * Testcase MP05 according to the document TESTPLAN Version 2.1 or
     * higher. This testcase is trying to load the
     * <em>ModulePackage</em> from
     * "testmodules/emptyModulePropertyFile". This
     * <em>ModulePackage</em> has an empty "module.properties.xml"
     * file. The expected result is that the <em>ModulePackage</em>
     * is simply ignored.
     */
    public final void testIfEmptyModulePropertyFileIsIgnored() {
        this.moduleRegistry = new ModuleRegistry();

        this.moduleRegistry.addObserver(this);

        try {
            this.moduleRegistry.setModuleDirectory(new File(
                "testmodules" + File.separator + "emptyModulePropertyFile"));

            String[] modulePackage = this.moduleRegistry
                .geModulePackageIds();

            assertEquals(modulePackage.length, 0);

        } catch (ModuleClassLoaderInitializationException e) {
            assertTrue(false);
        }

    }

    /**
     * Testcase MP06 according to the document TESTPLAN Version 2.1 or
     * higher. This testcase is trying to load the
     * <em>ModulePackage</em> from "testmodules/missingClassFile".
     * This <em>ModulePackage</em> has a missing module class file.
     * The expected result is that the <em>ModulePackage</em> is
     * simply ignored.
     */
    public final void testIfMissingClassFileIsIgnored() {
        this.moduleRegistry = new ModuleRegistry();

        this.moduleRegistry.addObserver(this);

        try {
            this.moduleRegistry.setModuleDirectory(new File(
                "testmodules" + File.separator + "missingClassFile"));

            String[] modulePackage = this.moduleRegistry
                .geModulePackageIds();

            assertEquals(modulePackage.length, 0);

        } catch (ModuleClassLoaderInitializationException e) {
            assertTrue(false);
        }

    }

    /**
     * Testcase MP07 according to the document TESTPLAN Version 2.1 or
     * higher. This testcase is trying to load the
     * <em>ModulePackage</em> from "testmodules/malformedA". This
     * <em>ModulePackage</em> has a malformed
     * "module.properties.xml" file. The &lt;/description&gt; tag is
     * missing. The expected result is that the <em>ModulePackage</em>
     * is simply ignored.
     */
    public final void testIfMalformedModulePropertyIsIgnoredA() {
        this.moduleRegistry = new ModuleRegistry();

        this.moduleRegistry.addObserver(this);

        try {
            this.moduleRegistry.setModuleDirectory(new File(
                "testmodules" + File.separator + "malformedA"));

            String[] modulePackage = this.moduleRegistry
                .geModulePackageIds();

            assertEquals(modulePackage.length, 0);

        } catch (ModuleClassLoaderInitializationException e) {
            assertTrue(false);
        }

    }

    /**
     * Testcase MP07 according to the document TESTPLAN Version 2.1 or
     * higher. This testcase is trying to load the
     * <em>ModulePackage</em> from "testmodules/malformedB". This
     * <em>ModulePackage</em> has a malformed
     * "module.properties.xml" file. The &lt;/propertyType&gt; tag is
     * expected but &lt;propertyType&gt; is found. The expected result
     * is that the <em>ModulePackage</em> is simply ignored.
     */
    public final void testIfMalformedModulePropertyIsIgnoredB() {
        this.moduleRegistry = new ModuleRegistry();

        this.moduleRegistry.addObserver(this);

        try {
            this.moduleRegistry.setModuleDirectory(new File(
                "testmodules" + File.separator + "malformedB"));

            String[] modulePackage = this.moduleRegistry
                .geModulePackageIds();

            assertEquals(modulePackage.length, 0);

        } catch (ModuleClassLoaderInitializationException e) {
            assertTrue(false);
        }

    }

    /**
     * Testcase MP08 according to the document TESTPLAN Version 2.1 or
     * higher. This testcase is trying to load the
     * <em>ModulePackage</em> from "testmodules/malformedC". This
     * <em>ModulePackage</em> has a malformed
     * "module.properties.xml" file. &lt;&gt; is found inside an
     * element. The expected result is that the <em>ModulePackage</em>
     * is simply ignored.
     */
    public final void testIfMalformedModulePropertyIsNotLoadedC() {
        this.moduleRegistry = new ModuleRegistry();

        this.moduleRegistry.addObserver(this);

        try {
            this.moduleRegistry.setModuleDirectory(new File(
                "testmodules" + File.separator + "malformedC"));

            String[] modulePackage = this.moduleRegistry
                .geModulePackageIds();

            assertEquals(modulePackage.length, 0);

        } catch (ModuleClassLoaderInitializationException e) {
            assertTrue(false);
        }

    }

    /**
     * Testcase MP09 according to the document TESTPLAN Version 2.1 or
     * higher. This testcase is trying to load the
     * <em>ModulePackage</em> from "testmodules/invalidA". This
     * <em>ModulePackage</em> has an invalid "module.properties.xml"
     * file. The expected &lt;id&gt; element is named &lt;invalid&gt;
     * The expected result is that the <em>ModulePackage</em> is
     * simply ignored.
     */
    public final void testIfWellformedButInvalidModulePropertyIsNotLoadedA() {
        this.moduleRegistry = new ModuleRegistry();

        this.moduleRegistry.addObserver(this);

        try {
            this.moduleRegistry.setModuleDirectory(new File(
                "testmodules" + File.separator + "invalidA"));

            String[] modulePackage = this.moduleRegistry
                .geModulePackageIds();

            assertEquals(modulePackage.length, 0);

        } catch (ModuleClassLoaderInitializationException e) {
            assertTrue(false);
        }

    }

    /**
     * Testcase MP10 according to the document TESTPLAN Version 2.1 or
     * higher. This testcase is trying to load the
     * <em>ModulePackage</em> from "testmodules/invalidB". This
     * <em>ModulePackage</em> has an invalid "module.properties.xml"
     * file. The "module.properties.xml" is missing the
     * &lt;description&gt; element. The expected result is that the
     * <em>ModulePackage</em> is simply ignored.
     */
    public final void testIfWellformedButInvalidModulePropertyIsNotLoadedB() {
        this.moduleRegistry = new ModuleRegistry();

        this.moduleRegistry.addObserver(this);

        try {
            this.moduleRegistry.setModuleDirectory(new File(
                "testmodules\\invalidB"));

            String[] modulePackage = this.moduleRegistry
                .geModulePackageIds();

            assertEquals(modulePackage.length, 0);

        } catch (ModuleClassLoaderInitializationException e) {
            assertTrue(false);
        }

    }

    /**
     * Testcase MP11 according to the document TESTPLAN Version 2.1 or
     * higher. This testcase is trying to load the
     * <em>ModulePackage</em> from "testmodules/invalidC". This
     * <em>ModulePackage</em> has an invalid "module.properties.xml"
     * file. The "module.properties.xml" is missing the
     * &lt;propertyType&gt; and &lt;propertyValue&gt; element for a
     * <em>ModuleProperty</em>. The expected result is that the
     * <em>ModulePackage</em> is simply ignored.
     */
    public final void testIfWellformedButInvalidModulePropertyIsNotLoadedC() {
        this.moduleRegistry = new ModuleRegistry();

        this.moduleRegistry.addObserver(this);

        try {
            this.moduleRegistry.setModuleDirectory(new File(
                "testmodules" + File.separator + "invalidC"));

            String[] modulePackage = this.moduleRegistry
                .geModulePackageIds();

            assertEquals(modulePackage.length, 0);

        } catch (ModuleClassLoaderInitializationException e) {
            assertTrue(false);
        }

    }

    /**
     * Testcase MP12 according to the document TESTPLAN Version 2.1 or
     * higher. This testcase is trying to load the
     * <em>ModulePackage</em> from "testmodules/validModule". This
     * <em>ModulePackage</em> has is valid. The expected result is
     * that the <em>ModulePackage</em> is loaded in not more than 5
     * seconds.
     * @throws Exception
     *             If the {@link RetriedAssert} is throwing it
     */
    public final void testIfValidModuleIsLoaded() throws Exception {
        this.moduleRegistry = new ModuleRegistry();

        this.moduleRegistry.addObserver(this);

        this.moduleRegistry.setModuleDirectory(new File(
            "testmodules" + File.separator + "validModule"));

        RetriedAssert ra = new RetriedAssert(TIME_TO_WAIT, ASSERT_DELAY) {

            @SuppressWarnings("synthetic-access")
            @Override
            public void run() throws Exception {

                assertTrue(ModulePackageLoadingTest.this.result);

            }
        };

        ra.start();

        String[] modulePackage = this.moduleRegistry.geModulePackageIds();

        assertEquals(modulePackage.length, 1);
    }

    /**
     * Testcase MP13 according to the document TESTPLAN Version 2.1 or
     * higher. This testcase is trying to load a
     * <em>ModulePackage</em> from "testmodules/Duplicated
     * validModule"". This <em>ModulePackage</em> is valid but is
     * containing an identically named module class file as the
     * previous loaded <em>validModule</em> from{@link #testIfValidModuleIsLoaded()}.
     * The expected result is that the <em>ModulePackage</em> is
     * simply ignored.
     */
    public final void testIfDuplicateModuleClassIsIgnored() {
        this.moduleRegistry = new ModuleRegistry();

        this.moduleRegistry.addObserver(this);

        try {
            this.moduleRegistry.setModuleDirectory(new File(
                "testmodules\\Duplicated validModule"));

            String[] modulePackage = this.moduleRegistry
                .geModulePackageIds();

            assertEquals(modulePackage.length, 0);

        } catch (ModuleClassLoaderInitializationException e) {
            assertTrue(false);
        }

    }

    /**
     * @see java.util.Observer#update(java.util.Observable,
     *      java.lang.Object)
     */
    public final void update(@SuppressWarnings("unused")
    final Observable o, final Object arg) {
        if (arg instanceof ModuleDescriptor) {
            this.result = true;
        }

    }

}

/*
 * Class: IModuleRegistry
 * Version: 1.0
 * Date: 18.10.2005
 * By: Frank@Schlesinger.com
 */

package org.electrocodeogram.module.registry;

import java.io.File;

import org.electrocodeogram.module.Module;
import org.electrocodeogram.modulepackage.ModuleDescriptor;
import org.electrocodeogram.modulepackage.classloader.ModuleClassLoaderInitializationException;

/**
 * This <code>Interface</code> contains methods that are used to
 * access information about available <em>ModulePackagaes</em> and
 * running {@link org.electrocodeogram.module.Module} instances. It is
 * also used to create new running module instances from
 * <em>ModulePackages</em>.
 */
public interface IModuleRegistry {

    /**
     * This method returns the unique <code>String</code> ids of all currently
     * known <em>ModulePackages</em>.
     * @return The ids of all <em>ModulePackages</em>
     */
    String[] geModulePackageIds();

    /**
     * This method returns the module instance with the given uniique int id.
     * @param id
     *            Is the unique int id of the module instance to return
     * @return The desired module instance
     * @throws ModuleInstanceNotFoundException
     *             If the given unique int id is illegal (id < 0) or if a
     *             module instance with the given id can not be found
     */
    Module getModule(int id) throws ModuleInstanceNotFoundException;

    /**
     * This method takes the unique <code>String</code> id of a <em>ModulePackage</em>
     * and returns a new module instance from it. It also assignes  the given name to
     * the module instance.
     * @param id
     *            Is the unique <code>String</code> id of <em>ModulePackage</em>
     * @param name
     *            Is the name that should be given to the new module instance
     * @return The unique int id that is assigned to the module during creation
     * @throws ModuleInstantiationException
     *             If an <code>Exception</code> occures during the instanciation
     *             of the module
     * @throws ModulePackageNotFoundException
     *             If the given <code>String</code> id is empty or if an <em>ModulePackage</em>
     *             with the given id can not be found
     */
    int createModule(String id, String name)
        throws ModuleInstantiationException, ModulePackageNotFoundException;

    /**
     * The method returns the {@link ModuleDescriptor} of a <em>ModulePackage</em>.
     * The <em>ModuleDescriptor</em> contains the information that has been
     * provided with the module in its <em>"module.properties.xml"</em> file.
     * @param id
     *            Is the unique <code>String</code> id <em>ModulePackage</em>
     * @return The <em>ModuleDescriptor</em> of the <em>ModulePackage</em>
     * @throws ModulePackageNotFoundException
     *             If the given <code>String</code> id is empty or if a
     *             <em>ModulePackage</em> with the given id can not be found
     */
    ModuleDescriptor getModuleDescriptor(String id)
        throws ModulePackageNotFoundException;

    /**
     * This method stores the current <em>ModuleSetup</em> as configured in
     * the ECG Lab into the given <code>File</code>.
     * @param file
     *            Is the <code>File</code> to store the <em>ModuleSetup</em> in
     * @throws ModuleSetupStoreException
     *             If an <code>Exception</code> occures during the storing
     */
    void storeModuleSetup(File file) throws ModuleSetupStoreException;

    /**
     * This method loads at <em>ModuleSetup</em> from the given <code>File</code>
     * into the ECG Lab.
     * @param file
     *            Is the <code>File</code> to load the <em>ModuleSetup</em> from
     * @throws ModuleSetupLoadException
     *             If an <code>Exception</code> occures during the loading
     */
    void loadModuleSetup(File file) throws ModuleSetupLoadException;

    /**
     * This method tells the <em>ModuleRegistry</em> where to look for
     * <em>ModulePackages</em>.
     * @param moduleDirectory Is the directory to look for <em>ModulePackages</em>
     * @throws ModuleClassLoaderInitializationException If an <code>Exception</code> occures while initializing the {@link org.electrocodeogram.modulepackage.classloader.ModuleClassLoader}
     */
    void setModuleDirectory(File moduleDirectory)
        throws ModuleClassLoaderInitializationException;
}

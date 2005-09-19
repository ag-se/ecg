/**
 * 
 */
package org.electrocodeogram.test.module.registry;

import java.io.File;
import java.util.Observable;
import java.util.Observer;

import org.electrocodeogram.module.ModuleDescriptor;
import org.electrocodeogram.module.classloader.ModuleClassLoaderInitializationException;

import utmj.threaded.RetriedAssert;

import junit.framework.TestCase;

/**
 *
 */
public class ModuleRegistryTest extends TestCase implements Observer
{

	private MockModuleRegistry mockModuleRegistry;

	private boolean result = false;

	protected void setUp()
	{
		this.mockModuleRegistry = null;

		this.result = false;
	}

	public void testIfNotExistingModuleDirectoryCausesException()
	{
		this.mockModuleRegistry = new MockModuleRegistry();

		this.mockModuleRegistry.addObserver(this);

		try
		{
			this.mockModuleRegistry.setFile(new File(
					"bin\\org\\electrocodeogram\\test\\module\\registry\\notExistingDirectory"));

			assertTrue(false);
		}
		catch (ModuleClassLoaderInitializationException e)
		{
			assertTrue(true);
		}

	}

	public void testIfEmptyModuleDirectoryCausesException()
	{
		this.mockModuleRegistry = new MockModuleRegistry();

		this.mockModuleRegistry.addObserver(this);

		try
		{
			this.mockModuleRegistry.setFile(new File(
					"bin\\org\\electrocodeogram\\test\\module\\registry\\emptyDirectory"));

			assertTrue(false);
		}
		catch (ModuleClassLoaderInitializationException e)
		{
			assertTrue(true);
		}

	}

	public void testIfNoModulePropertyFileCausesException() throws ModuleClassLoaderInitializationException, InterruptedException
	{
		this.mockModuleRegistry = new MockModuleRegistry();

		this.mockModuleRegistry.addObserver(this);

		this.mockModuleRegistry.setFile(new File(
				"testmodules\\noModulePropertyFile"));
		
		Thread.sleep(1000);

		assertFalse(this.result);

	}

	public void testIfEmptyModulePropertyFileIsNotLoaded() throws ModuleClassLoaderInitializationException, InterruptedException
	{
		this.mockModuleRegistry = new MockModuleRegistry();

		this.mockModuleRegistry.addObserver(this);

		this.mockModuleRegistry.setFile(new File(
				"testmodules\\emptyModulePropertyFile"));

		Thread.sleep(1000);

		assertFalse(this.result);

	}
	
	public void testIfMissingClassFileIsNotLoaded() throws ModuleClassLoaderInitializationException, InterruptedException
	{
		this.mockModuleRegistry = new MockModuleRegistry();

		this.mockModuleRegistry.addObserver(this);

		this.mockModuleRegistry.setFile(new File(
				"testmodules\\missingClassFile"));

		Thread.sleep(1000);

		assertFalse(this.result);

	}
	
	public void testIfMalformedModulePropertyIsNotLoadedA() throws ModuleClassLoaderInitializationException, InterruptedException
	{
		this.mockModuleRegistry = new MockModuleRegistry();

		this.mockModuleRegistry.addObserver(this);

		this.mockModuleRegistry.setFile(new File(
				"testmodules\\malformedA"));

		Thread.sleep(1000);

		assertFalse(this.result);

	}

	public void testIfMalformedModulePropertyIsNotLoadedB() throws ModuleClassLoaderInitializationException, InterruptedException
	{
		this.mockModuleRegistry = new MockModuleRegistry();

		this.mockModuleRegistry.addObserver(this);

		this.mockModuleRegistry.setFile(new File(
				"testmodules\\malformedB"));

		Thread.sleep(1000);

		assertFalse(this.result);

	}
	
	public void testIfMalformedModulePropertyIsNotLoadedC() throws ModuleClassLoaderInitializationException, InterruptedException
	{
		this.mockModuleRegistry = new MockModuleRegistry();

		this.mockModuleRegistry.addObserver(this);

		this.mockModuleRegistry.setFile(new File(
				"testmodules\\malformedC"));

		Thread.sleep(1000);

		assertFalse(this.result);

	}
	
	public void testIfWellformedButInvalidModulePropertyIsNotLoadedA() throws ModuleClassLoaderInitializationException, InterruptedException
	{
		this.mockModuleRegistry = new MockModuleRegistry();

		this.mockModuleRegistry.addObserver(this);

		this.mockModuleRegistry.setFile(new File(
				"testmodules\\invalidA"));

		Thread.sleep(1000);

		assertFalse(this.result);

	}
	
	public void testIfWellformedButInvalidModulePropertyIsNotLoadedB() throws ModuleClassLoaderInitializationException, InterruptedException
	{
		this.mockModuleRegistry = new MockModuleRegistry();

		this.mockModuleRegistry.addObserver(this);

		this.mockModuleRegistry.setFile(new File(
				"testmodules\\invalidB"));

		Thread.sleep(1000);

		assertFalse(this.result);

	}
	
	public void testIfWellformedButInvalidModulePropertyIsNotLoadedC() throws ModuleClassLoaderInitializationException, InterruptedException
	{
		this.mockModuleRegistry = new MockModuleRegistry();

		this.mockModuleRegistry.addObserver(this);

		this.mockModuleRegistry.setFile(new File(
				"testmodules\\invalidC"));

		Thread.sleep(1000);

		assertFalse(this.result);

	}
	
	public void testIfValidModuleIsLoaded() throws Exception
	{
		this.mockModuleRegistry = new MockModuleRegistry();

		this.mockModuleRegistry.addObserver(this);

		this.mockModuleRegistry.setFile(new File(
				"testmodules\\validModule"));

		new RetriedAssert(5000, 100) {
            @Override
            public void run() throws Exception
            {
                assertTrue(result);
            }
        }.start();
	}

	/**
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	public void update(Observable o, Object arg)
	{
		if (arg instanceof ModuleDescriptor)
		{
			this.result = true;
		}

	}

}

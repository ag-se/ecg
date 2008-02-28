package org.electrocodeogram.cpc.test.tests;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.electrocodeogram.cpc.core.CPCCorePlugin;
import org.electrocodeogram.cpc.core.api.data.CloneDiff;
import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.data.ICloneFile;
import org.electrocodeogram.cpc.core.api.data.ICloneGroup;
import org.electrocodeogram.cpc.core.api.data.ICloneObjectExtension;
import org.electrocodeogram.cpc.core.api.data.extension.ICloneModificationHistoryExtension;
import org.electrocodeogram.cpc.core.api.data.extension.ICloneNonWsPositionExtension;
import org.electrocodeogram.cpc.core.api.data.special.ICloneObjectExtensionLazyMultiStatefulObject;
import org.electrocodeogram.cpc.core.api.data.special.IStoreCloneObject;
import org.electrocodeogram.cpc.core.api.provider.data.ICloneFactoryProvider;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider;
import org.electrocodeogram.cpc.core.api.provider.store.StoreLockingException;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider.LockMode;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider.UpdateMode;
import org.electrocodeogram.cpc.test.data.ISomeMultiStatefulCloneObjectExtension;
import org.electrocodeogram.cpc.test.data.ISomeStatefulCloneObjectExtension;
import org.electrocodeogram.cpc.test.data.SomeDataElement;
import org.electrocodeogram.cpc.test.utils.TestCloneUtils;
import org.electrocodeogram.cpc.test.utils.TestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class StoreTest
{
	private static Log log = LogFactory.getLog(StoreTest.class);

	private ICloneFactoryProvider cloneFactoryProvider;
	private IStoreProvider storeProvider;

	private static final String PROJECT_NAME = "StoreTest";

	/**
	 * Perform pre-test initialisation.
	 */
	@Before
	public void setUp() throws Exception
	{
		TestUtils.defaultSetUp(PROJECT_NAME, "project1", true, false);
		storeProvider = TestUtils.getStoreProvider();
		cloneFactoryProvider = (ICloneFactoryProvider) CPCCorePlugin.getProviderRegistry().lookupProvider(
				ICloneFactoryProvider.class);
		log.debug("Providers - Store: " + storeProvider + ", CloneFactory: " + cloneFactoryProvider);
	}

	/**
	 * Perform post-test cleanup.
	 */
	@After
	public void tearDown() throws Exception
	{
		TestUtils.defaultTearDown(PROJECT_NAME);
	}

	/**
	 * Add and remove a couple of clones. They are not persisted, all actions are done in cache.
	 */
	@Test
	public void testCloneAddAndRemoveCache() throws StoreLockingException
	{
		log.debug("testCloneAddAndRemoveCache()");

		subTestCloneAddAndRemove(false);
	}

	/**
	 * Add and remove a couple of clones. They are persisted.
	 */
	@Test
	public void testCloneAddAndRemovePersist() throws StoreLockingException
	{
		log.debug("testCloneAddAndRemovePersist()");

		subTestCloneAddAndRemove(true);
	}

	private void subTestCloneAddAndRemove(boolean persist) throws StoreLockingException
	{
		log.debug("subTestCloneAddAndRemove() - persist: " + persist);

		try
		{
			storeProvider.acquireWriteLock(LockMode.DEFAULT);

			log.trace("subTestCloneAddAndRemove() - adding clones");
			ICloneFile file = storeProvider.lookupCloneFileByPath(PROJECT_NAME, "src/test/Class1.java", true, true);
			assertNotNull(file);

			ICloneGroup group1 = (ICloneGroup) cloneFactoryProvider.getInstance(ICloneGroup.class);
			assertNotNull(group1);

			IClone clone1 = TestCloneUtils.createTestClone(cloneFactoryProvider, file, group1, 0, 20, null);
			storeProvider.addClone(clone1);

			IClone clone2 = TestCloneUtils.createTestClone(cloneFactoryProvider, file, group1, 60, 73, null);
			storeProvider.addClone(clone2);

			//persist the data
			if (persist)
				storeProvider.persistData(file);

			//now check if they were correctly added
			log.trace("subTestCloneAddAndRemove() - checking lookup");
			ICloneFile fileX = storeProvider.lookupCloneFile(file.getUuid());
			log.trace("File by fileid: " + fileX);
			assertEquals(file, fileX);
			IClone clone1X = storeProvider.lookupClone(clone1.getUuid());
			log.trace("Clone1 by cloneid: " + clone1X);
			assertEquals(clone1, clone1X);
			IClone clone2X = storeProvider.lookupClone(clone2.getUuid());
			log.trace("Clone2 by cloneid: " + clone2X);
			assertEquals(clone2, clone2X);

			List<IClone> clones = storeProvider.getClonesByFile(file.getUuid());
			log.trace("Clones by fileid: " + clones);
			assertTrue(clones.size() == 2 && clones.get(0).equals(clone1) && clones.get(1).equals(clone2));

			clones = storeProvider.getClonesByGroup(group1.getUuid());
			log.trace("Clones by groupid: " + clones);
			Collections.sort(clones);
			assertTrue(clones.size() == 2 && clones.get(0).equals(clone1) && clones.get(1).equals(clone2));

			//remove clones
			log.trace("subTestCloneAddAndRemove() - removing clones");
			storeProvider.removeClone(clone1);
			storeProvider.removeClone(clone2);

			//persist again
			if (persist)
				storeProvider.persistData(file);

			//now check again
			log.trace("subTestCloneAddAndRemove() - check removal");
			ICloneFile fileY = storeProvider.lookupCloneFile(file.getUuid());
			assertEquals(file, fileY);
			IClone clone1Y = storeProvider.lookupClone(clone1.getUuid());
			assertNull(clone1Y);
			IClone clone2Y = storeProvider.lookupClone(clone2.getUuid());
			assertNull(clone2Y);

			clones = storeProvider.getClonesByFile(file.getUuid());
			assertTrue(clones.isEmpty());

			clones = storeProvider.getClonesByGroup(group1.getUuid());
			assertTrue(clones.isEmpty());

		}
		finally
		{
			storeProvider.releaseWriteLock();
		}
	}

	/**
	 * Tests adding and removing of non-stateful {@link ICloneObjectExtension}s. 
	 */
	@Test
	public void testCloneExtensionAddAndRemoveNonStatefulNoPersistNoClear() throws StoreLockingException
	{
		log.debug("testCloneExtensionAddAndRemoveNonStatefulNoPersistNoClear()");

		subTestCloneExtensionAddAndRemoveNonStateful(false, false);
	}

	@Test
	public void testCloneExtensionAddAndRemoveNonStatefulPersistNoClear() throws StoreLockingException
	{
		log.debug("testCloneExtensionAddAndRemoveNonStatefulPersistNoClear()");

		subTestCloneExtensionAddAndRemoveNonStateful(true, false);
	}

	@Test
	public void testCloneExtensionAddAndRemoveNonStatefulPersistClear() throws StoreLockingException
	{
		log.debug("testCloneExtensionAddAndRemoveNonStatefulPersistClear()");

		subTestCloneExtensionAddAndRemoveNonStateful(true, true);
	}

	private void subTestCloneExtensionAddAndRemoveNonStateful(boolean persist, boolean clearCache)
			throws StoreLockingException
	{
		log.debug("subTestCloneExtensionAddAndRemoveNonStateful() - persist: " + persist + ", clearCache: "
				+ clearCache);

		/*
		 * Create two base IClone entries
		 */

		log.trace("testCloneExtensionAddAndRemoveNonStateful() - adding clones");

		ICloneFile file;
		IClone clone1;
		IClone clone2;
		try
		{
			storeProvider.acquireWriteLock(LockMode.DEFAULT);

			file = storeProvider.lookupCloneFileByPath(PROJECT_NAME, "src/test/Class1.java", true, true);
			assertNotNull(file);

			ICloneGroup group1 = (ICloneGroup) cloneFactoryProvider.getInstance(ICloneGroup.class);
			assertNotNull(group1);

			clone1 = TestCloneUtils.createTestClone(cloneFactoryProvider, file, group1, 0, 20, null);
			storeProvider.addClone(clone1);

			clone2 = TestCloneUtils.createTestClone(cloneFactoryProvider, file, group1, 60, 73, null);
			storeProvider.addClone(clone2);

			if (persist)
				storeProvider.persistData(file);
		}
		finally
		{
			storeProvider.releaseWriteLock();
		}

		/*
		 * Clear cache.
		 */
		if (clearCache)
			clearCache(file);

		/*
		 * Lookup clone1 and add a non-stateful extension object.
		 */

		log.trace("testCloneExtensionAddAndRemoveNonStateful() - adding non-stateful extension");

		try
		{
			storeProvider.acquireWriteLock(LockMode.DEFAULT);

			IClone newClone1 = storeProvider.lookupClone(clone1.getUuid());

			//create a new non-stateful extension
			ICloneNonWsPositionExtension extension1 = (ICloneNonWsPositionExtension) cloneFactoryProvider
					.getInstance(ICloneNonWsPositionExtension.class);
			assert (extension1 != null);
			extension1.setStartNonWsOffset(10);
			extension1.setEndNonWsOffset(20);
			newClone1.addExtension(extension1);

			storeProvider.updateClone(newClone1, UpdateMode.MOVED);

			if (persist)
				storeProvider.persistData(file);
		}
		finally
		{
			storeProvider.releaseWriteLock();
		}

		/*
		 * Clear cache.
		 */
		if (clearCache)
			clearCache(file);

		/*
		 * Lookup clone1 again and ensure that no extension is registered.
		 */

		log
				.trace("testCloneExtensionAddAndRemoveNonStateful() - checking that non-stateful extension was not persisted");

		IClone newClone2 = storeProvider.lookupClone(clone1.getUuid());
		assertTrue("hasExtensions does not match real extension status - hasExtensions: " + newClone2.hasExtensions()
				+ ", extensions: " + newClone2.getExtensions(), newClone2.hasExtensions() == !newClone2.getExtensions()
				.isEmpty());
		assertTrue("non-stateful extension was persisted", !newClone2.hasExtensions());

		/*
		 * Clear cache.
		 */
		if (clearCache)
			clearCache(file);

		/*
		 * Make sure that clone2 wasn't affected in any way.
		 */

		log.trace("testCloneExtensionAddAndRemoveNonStateful() - making sure that clone2 was not affected.");

		IClone staticClone = storeProvider.lookupClone(clone2.getUuid());

		//before comparing both, we need to make sure that the dirty flag is set equally
		((IStoreCloneObject) clone2).setDirty(false);
		((IStoreCloneObject) staticClone).setDirty(false);

		assertTrue("clone2 was somehow affected, not equal", clone2.equalsAll(staticClone));
		assertTrue("clone2 was somehow affected, has extensions", !staticClone.hasExtensions());
	}

	/**
	 * Tests adding and removing of stateful {@link ICloneObjectExtension}s. 
	 */
	@Test
	public void testCloneExtensionAddAndRemoveStatefulNoPersistNoClear() throws StoreLockingException
	{
		log.debug("testCloneExtensionAddAndRemoveStatefulNoPersistNoClear()");

		subTestCloneExtensionAddAndRemoveStateful(false, false);
	}

	@Test
	public void testCloneExtensionAddAndRemoveStatefulPersistNoClear() throws StoreLockingException
	{
		log.debug("testCloneExtensionAddAndRemoveStatefulPersistNoClear()");

		subTestCloneExtensionAddAndRemoveStateful(true, false);
	}

	@Test
	public void testCloneExtensionAddAndRemoveStatefulPersistClear() throws StoreLockingException
	{
		log.debug("testCloneExtensionAddAndRemoveStatefulPersistClear()");

		subTestCloneExtensionAddAndRemoveStateful(true, true);
	}

	private void subTestCloneExtensionAddAndRemoveStateful(boolean persist, boolean clearCache)
			throws StoreLockingException
	{
		log.debug("subTestCloneExtensionAddAndRemoveStateful() - persist: " + persist + ", clearCache: " + clearCache);

		/*
		 * Create two base IClone entries
		 */

		log.trace("testCloneExtensionAddAndRemoveStateful() - adding clones");

		ICloneFile file;
		ICloneGroup group1;
		IClone clone1;
		IClone clone2;
		try
		{
			storeProvider.acquireWriteLock(LockMode.DEFAULT);

			file = storeProvider.lookupCloneFileByPath(PROJECT_NAME, "src/test/Class1.java", true, true);
			assertNotNull(file);

			group1 = (ICloneGroup) cloneFactoryProvider.getInstance(ICloneGroup.class);
			assertNotNull(group1);

			clone1 = TestCloneUtils.createTestClone(cloneFactoryProvider, file, group1, 0, 20, null);
			storeProvider.addClone(clone1);

			clone2 = TestCloneUtils.createTestClone(cloneFactoryProvider, file, group1, 60, 73, null);
			storeProvider.addClone(clone2);

			if (persist)
				storeProvider.persistData(file);
		}
		finally
		{
			storeProvider.releaseWriteLock();
		}

		/*
		 * Clear cache.
		 */
		if (clearCache)
			clearCache(file);

		/*
		 * Lookup clone1 and add a stateful non-multi extension.
		 */

		log.trace("testCloneExtensionAddAndRemoveStateful() - adding and persisting stateful non-multi extension");

		ISomeStatefulCloneObjectExtension extension2;
		try
		{
			storeProvider.acquireWriteLock(LockMode.DEFAULT);

			IClone newClone2b = storeProvider.lookupClone(clone1.getUuid());

			//create a new non-stateful extension
			extension2 = (ISomeStatefulCloneObjectExtension) cloneFactoryProvider
					.getInstance(ISomeStatefulCloneObjectExtension.class);
			assert (extension2 != null);

			extension2.setData("test123");

			log.trace("EXTENSIONS (pre-add): " + newClone2b.getExtensions());
			newClone2b.addExtension(extension2);
			log.trace("EXTENSIONS (post-add): " + newClone2b.getExtensions());

			storeProvider.updateClone(newClone2b, UpdateMode.MOVED);

			if (persist)
				storeProvider.persistData(file);
		}
		finally
		{
			storeProvider.releaseWriteLock();
		}

		/*
		 * Clear cache.
		 */
		if (clearCache)
			clearCache(file);

		/*
		 * Lookup clone1 again and ensure that the extension was persisted.
		 */

		log
				.trace("testCloneExtensionAddAndRemoveStateful() - checking that the stateful non-multi extension was persisted");

		IClone newClone2c = storeProvider.lookupClone(clone1.getUuid());
		log.trace("EXTENSIONS (post-add, re-lookup): " + newClone2c.getExtensions());

		assertTrue("hasExtensions does not match real extension status - hasExtensions: " + newClone2c.hasExtensions()
				+ ", extensions: " + newClone2c.getExtensions(), newClone2c.hasExtensions() == !newClone2c
				.getExtensions().isEmpty());
		assertTrue("stateful non-multi extension was not persisted", newClone2c.hasExtensions());
		assertTrue("wrong number of extensions - got: " + newClone2c.getExtensions().size() + ", expected: 1 - "
				+ newClone2c.getExtensions(), newClone2c.getExtensions().size() == 1);

		ISomeStatefulCloneObjectExtension newExtension2 = (ISomeStatefulCloneObjectExtension) newClone2c
				.getExtension(ISomeStatefulCloneObjectExtension.class);
		assertTrue("extension not registered under correct interface type", newExtension2 != null);
		assertTrue("extension was not correctly persisted, expected data: " + extension2.getData() + ", got: "
				+ newExtension2.getData(), extension2.getData().equals(newExtension2.getData()));

		/*
		 * Clear cache.
		 */
		if (clearCache)
			clearCache(file);

		/*
		 * Test the removal of the stateful extension.
		 */
		log.trace("testCloneExtensionAddAndRemoveStateful() - removing stateful extension");

		try
		{
			storeProvider.acquireWriteLock(LockMode.DEFAULT);

			IClone newClone2d = storeProvider.lookupClone(clone1.getUuid());
			log.trace("EXTENSIONS (pre-del): " + newClone2d.getExtensions());
			newClone2d.removeExtension(ISomeStatefulCloneObjectExtension.class);
			log.trace("EXTENSIONS (post-del): " + newClone2d.getExtensions());

			storeProvider.updateClone(newClone2d, UpdateMode.MOVED);

			if (persist)
				storeProvider.persistData(file);
		}
		finally
		{
			storeProvider.releaseWriteLock();
		}

		/*
		 * Clear cache.
		 */
		if (clearCache)
			clearCache(file);

		/*
		 * Lookup clone1 again and ensure that no extension is registered.
		 */

		log
				.trace("testCloneExtensionAddAndRemoveStateful() - checking that the stateful extension was correctly removed");

		IClone newClone2e = storeProvider.lookupClone(clone1.getUuid());
		log.trace("EXTENSIONS (post-del re-lookup): " + newClone2e.getExtensions());

		assertTrue("hasExtensions does not match real extension status - hasExtensions: " + newClone2e.hasExtensions()
				+ ", extensions: " + newClone2e.getExtensions(), newClone2e.hasExtensions() == !newClone2e
				.getExtensions().isEmpty());
		assertTrue("removal of stateful extension failed", !newClone2e.hasExtensions());

		/*
		 * Clear cache.
		 */
		if (clearCache)
			clearCache(file);

		/*
		 * Make sure that clone2 wasn't affected in any way.
		 */

		log.trace("testCloneExtensionAddAndRemoveStateful() - making sure that clone2 was not affected.");

		IClone staticClone = storeProvider.lookupClone(clone2.getUuid());

		//before comparing both, we need to make sure that the dirty flag is set equally
		((IStoreCloneObject) clone2).setDirty(false);
		((IStoreCloneObject) staticClone).setDirty(false);

		assertTrue("clone2 was somehow affected, not equal", clone2.equalsAll(staticClone));
		assertTrue("clone2 was somehow affected, has extensions", !staticClone.hasExtensions());

	}

	/**
	 * Tests adding and removing of {@link ISomeMultiStatefulCloneObjectExtension}s. 
	 */
	@Test
	public void testCloneExtensionAddAndRemoveMultiStatefulNoPersistNoClear() throws StoreLockingException
	{
		log.debug("testCloneExtensionAddAndRemoveMultiStatefulNoPersistNoClear()");

		subTestCloneExtensionAddAndRemoveMultiStateful(false, false);
	}

	@Test
	public void testCloneExtensionAddAndRemoveMultiStatefulPersistNoClear() throws StoreLockingException
	{
		log.debug("testCloneExtensionAddAndRemoveMultiStatefulPersistNoClear()");

		subTestCloneExtensionAddAndRemoveMultiStateful(true, false);
	}

	@Test
	public void testCloneExtensionAddAndRemoveMultiStatefulPersistClear() throws StoreLockingException
	{
		log.debug("testCloneExtensionAddAndRemoveMultiStatefulPersistClear()");

		subTestCloneExtensionAddAndRemoveMultiStateful(true, true);
	}

	private void subTestCloneExtensionAddAndRemoveMultiStateful(boolean persist, boolean clearCache)
			throws StoreLockingException
	{
		log.debug("subTestCloneExtensionAddAndRemoveMultiStateful() - persist: " + persist + ", clearCache: "
				+ clearCache);

		/*
		 * Create two base IClone entries
		 */

		log.trace("testCloneExtensionAddAndRemoveMultiStateful() - adding clones");

		ICloneFile file;
		IClone clone1;
		IClone clone2;
		try
		{
			storeProvider.acquireWriteLock(LockMode.DEFAULT);

			file = storeProvider.lookupCloneFileByPath(PROJECT_NAME, "src/test/Class1.java", true, true);
			assertNotNull(file);

			ICloneGroup group1 = (ICloneGroup) cloneFactoryProvider.getInstance(ICloneGroup.class);
			assertNotNull(group1);

			clone1 = TestCloneUtils.createTestClone(cloneFactoryProvider, file, group1, 0, 20, null);
			storeProvider.addClone(clone1);

			clone2 = TestCloneUtils.createTestClone(cloneFactoryProvider, file, group1, 60, 73, null);
			storeProvider.addClone(clone2);

			if (persist)
				storeProvider.persistData(file);
		}
		finally
		{
			storeProvider.releaseWriteLock();
		}

		/*
		 * Clear cache.
		 */
		if (clearCache)
			clearCache(file);

		/*
		 * Add a multi stateful extension.
		 */

		log.trace("testCloneExtensionAddAndRemoveMultiStateful() - adding multi stateful extension");

		SomeDataElement elem1;
		SomeDataElement elem2;
		SomeDataElement elem3;
		SomeDataElement elem4;
		try
		{
			storeProvider.acquireWriteLock(LockMode.DEFAULT);

			IClone newClone1 = storeProvider.lookupClone(clone1.getUuid());

			//create a new non-stateful extension
			ISomeMultiStatefulCloneObjectExtension extension1 = (ISomeMultiStatefulCloneObjectExtension) cloneFactoryProvider
					.getInstance(ISomeMultiStatefulCloneObjectExtension.class);

			extension1.setData("ext1 data");
			elem1 = new SomeDataElement(1, "elem1");
			extension1.addDataElement(elem1);
			elem3 = new SomeDataElement(3, "elem3");
			extension1.addDataElement(elem3);
			elem2 = new SomeDataElement(2, "elem2");
			extension1.addDataElement(elem2);
			elem4 = new SomeDataElement(4, "elem4");
			extension1.addDataElement(elem4);

			newClone1.addExtension(extension1);

			storeProvider.updateClone(newClone1, UpdateMode.MODIFIED);

			if (persist)
				storeProvider.persistData(file);
		}
		finally
		{
			storeProvider.releaseWriteLock();
		}

		/*
		 * Clear cache.
		 */
		if (clearCache)
			clearCache(file);

		/*
		 * Lookup clone1 again and ensure that the multi stateful extension is still registered.
		 */

		log
				.trace("testCloneExtensionAddAndRemoveMultiStateful() - checking that multi stateful extension was persisted.");

		IClone newClone1b = storeProvider.lookupClone(clone1.getUuid());
		assertTrue("hasExtensions does not match real extension status - hasExtensions: " + newClone1b.hasExtensions()
				+ ", extensions: " + newClone1b.getExtensions(), newClone1b.hasExtensions() == !newClone1b
				.getExtensions().isEmpty());
		assertTrue("multi stateful extension was not persisted", newClone1b.hasExtensions());
		ISomeMultiStatefulCloneObjectExtension newExtension1 = (ISomeMultiStatefulCloneObjectExtension) newClone1b
				.getExtension(ISomeMultiStatefulCloneObjectExtension.class);
		assertTrue("unable to retrieve multi extension from clone", newExtension1 != null);
		assertTrue("multi extension does not have correct number of sub entries", newExtension1.getDataElements()
				.size() == 4);

		assertTrue("elem1 id missmatch", newExtension1.getDataElements().get(0).equals(elem1));
		assertTrue("elem1 data missmatch", newExtension1.getDataElements().get(0).getData().equals(elem1.getData()));

		assertTrue("elem2 missmatch", newExtension1.getDataElements().get(1).equals(elem2));
		assertTrue("elem2 data missmatch", newExtension1.getDataElements().get(1).getData().equals(elem2.getData()));

		assertTrue("elem3 missmatch", newExtension1.getDataElements().get(2).equals(elem3));
		assertTrue("elem3 data missmatch", newExtension1.getDataElements().get(2).getData().equals(elem3.getData()));

		assertTrue("elem4 missmatch", newExtension1.getDataElements().get(3).equals(elem4));
		assertTrue("elem4 data missmatch", newExtension1.getDataElements().get(3).getData().equals(elem4.getData()));

		/*
		 * Clear cache.
		 */
		if (clearCache)
			clearCache(file);

		/*
		 * Modify sub-element data.
		 */

		log.trace("testCloneExtensionAddAndRemoveMultiStateful() - modifying sub-element data");

		SomeDataElement elem3b;
		SomeDataElement elem5;
		try
		{
			storeProvider.acquireWriteLock(LockMode.DEFAULT);

			IClone newClone1c = storeProvider.lookupClone(clone1.getUuid());
			ISomeMultiStatefulCloneObjectExtension newExtension1b = (ISomeMultiStatefulCloneObjectExtension) newClone1c
					.getExtension(ISomeMultiStatefulCloneObjectExtension.class);

			//update elem3
			elem3b = new SomeDataElement(3, "elem3b");
			newExtension1b.addDataElement(elem3b);

			//delete elem2
			newExtension1b.removeDataElement(elem2);

			//add elem5
			elem5 = new SomeDataElement(5, "elem5");
			newExtension1b.addDataElement(elem5);

			storeProvider.updateClone(newClone1c, UpdateMode.MODIFIED);

			if (persist)
				storeProvider.persistData(file);
		}
		finally
		{
			storeProvider.releaseWriteLock();
		}

		/*
		 * Clear cache.
		 */
		if (clearCache)
			clearCache(file);

		/*
		 * Lookup clone1 again and ensure that the multi stateful extension was correctly modified.
		 */

		log.trace("testCloneExtensionAddAndRemoveMultiStateful() - checking modification of sub-element data.");

		IClone newClone1d = storeProvider.lookupClone(clone1.getUuid());
		assertTrue("hasExtensions does not match real extension status - hasExtensions: " + newClone1d.hasExtensions()
				+ ", extensions: " + newClone1d.getExtensions(), newClone1d.hasExtensions() == !newClone1d
				.getExtensions().isEmpty());
		assertTrue("multi stateful extension was not persisted", newClone1d.hasExtensions());
		ISomeMultiStatefulCloneObjectExtension newExtension1c = (ISomeMultiStatefulCloneObjectExtension) newClone1d
				.getExtension(ISomeMultiStatefulCloneObjectExtension.class);
		assertTrue("unable to retrieve multi extension from clone", newExtension1c != null);
		assertTrue("multi extension does not have correct number of sub entries - got: "
				+ newExtension1c.getDataElements().size() + ", expected: 4 - " + newExtension1c.getDataElements(),
				newExtension1c.getDataElements().size() == 4);

		assertTrue("elem1 id missmatch", newExtension1c.getDataElements().get(0).equals(elem1));
		assertTrue("elem1 data missmatch", newExtension1c.getDataElements().get(0).getData().equals(elem1.getData()));

		assertTrue("elem3b missmatch", newExtension1c.getDataElements().get(1).equals(elem3b));
		assertTrue("elem3b data missmatch", newExtension1c.getDataElements().get(1).getData().equals(elem3b.getData()));

		assertTrue("elem4 missmatch", newExtension1c.getDataElements().get(2).equals(elem4));
		assertTrue("elem4 data missmatch", newExtension1c.getDataElements().get(2).getData().equals(elem4.getData()));

		assertTrue("elem5 missmatch", newExtension1c.getDataElements().get(3).equals(elem5));
		assertTrue("elem5 data missmatch", newExtension1c.getDataElements().get(3).getData().equals(elem5.getData()));

		/*
		 * Clear cache.
		 */
		if (clearCache)
			clearCache(file);

		/*
		 * Make sure that clone2 wasn't affected in any way.
		 */

		log.trace("testCloneExtensionAddAndRemoveMultiStateful() - making sure that clone2 was not affected.");

		IClone staticClone = storeProvider.lookupClone(clone2.getUuid());

		//before comparing both, we need to make sure that the dirty flag is set equally
		((IStoreCloneObject) clone2).setDirty(false);
		((IStoreCloneObject) staticClone).setDirty(false);

		assertTrue("clone2 was somehow affected, not equal", clone2.equalsAll(staticClone));
		assertTrue("clone2 was somehow affected, has extensions", !staticClone.hasExtensions());

		//TODO: test removal of extension sub-elements, of extensions and of clone objects.

	}

	/**
	 * Tests adding and removing of {@link ICloneModificationHistoryExtension}s. 
	 */
	@Test
	public void testCloneExtensionAddAndRemoveCloneHistoryNoPersistNoClear() throws StoreLockingException
	{
		log.debug("testCloneExtensionAddAndRemoveCloneHistoryNoPersistNoClear()");

		subTestCloneExtensionAddAndRemoveCloneHistory(false, false);
	}

	@Test
	public void testCloneExtensionAddAndRemoveCloneHistoryPersistNoClear() throws StoreLockingException
	{
		log.debug("testCloneExtensionAddAndRemoveCloneHistoryPersistNoClear()");

		subTestCloneExtensionAddAndRemoveCloneHistory(true, false);
	}

	@Test
	public void testCloneExtensionAddAndRemoveCloneHistoryPersistClear() throws StoreLockingException
	{
		log.debug("testCloneExtensionAddAndRemoveCloneHistoryPersistClear()");

		subTestCloneExtensionAddAndRemoveCloneHistory(true, true);
	}

	private void subTestCloneExtensionAddAndRemoveCloneHistory(boolean persist, boolean clearCache)
			throws StoreLockingException
	{
		log.debug("subTestCloneExtensionAddAndRemoveCloneHistory() - persist: " + persist + ", clearCache: "
				+ clearCache);

		/*
		 * Create two base IClone entries
		 */

		log.trace("testCloneExtensionAddAndRemoveCloneHistory() - adding clones");

		ICloneFile file;
		IClone clone1;
		IClone clone2;
		try
		{
			storeProvider.acquireWriteLock(LockMode.DEFAULT);

			file = storeProvider.lookupCloneFileByPath(PROJECT_NAME, "src/test/Class1.java", true, true);
			assertNotNull(file);

			ICloneGroup group1 = (ICloneGroup) cloneFactoryProvider.getInstance(ICloneGroup.class);
			assertNotNull(group1);

			clone1 = TestCloneUtils.createTestClone(cloneFactoryProvider, file, group1, 0, 20, null);
			storeProvider.addClone(clone1);

			clone2 = TestCloneUtils.createTestClone(cloneFactoryProvider, file, group1, 60, 73, null);
			storeProvider.addClone(clone2);

			if (persist)
				storeProvider.persistData(file);
		}
		finally
		{
			storeProvider.releaseWriteLock();
		}

		/*
		 * Clear cache.
		 */
		if (clearCache)
			clearCache(file);

		/*
		 * Add a multi stateful extension.
		 */

		log.trace("testCloneExtensionAddAndRemoveCloneHistory() - adding multi stateful extension");

		CloneDiff diff1;
		CloneDiff diff2;
		try
		{
			storeProvider.acquireWriteLock(LockMode.DEFAULT);

			IClone newClone3 = storeProvider.lookupClone(clone1.getUuid());

			//create a new non-stateful extension
			ICloneModificationHistoryExtension extension3 = (ICloneModificationHistoryExtension) cloneFactoryProvider
					.getInstance(ICloneModificationHistoryExtension.class);

			diff1 = new CloneDiff("creator1", new Date(System.currentTimeMillis()), false, 0, 10, "some text1");
			diff2 = new CloneDiff("creator2", new Date(System.currentTimeMillis() + 1000), false, 15, 20, "some text2");
			extension3.addCloneDiff(diff1);
			extension3.addCloneDiff(diff2);

			newClone3.addExtension(extension3);

			storeProvider.updateClone(newClone3, UpdateMode.MOVED_MODIFIED);

			if (persist)
				storeProvider.persistData(file);
		}
		finally
		{
			storeProvider.releaseWriteLock();
		}

		/*
		 * Clear cache.
		 */
		if (clearCache)
			clearCache(file);

		/*
		 * Lookup clone1 again and ensure that the multi stateful extension is still registered.
		 */

		log
				.trace("testCloneExtensionAddAndRemoveCloneHistory() - checking that multi stateful extension was persisted.");

		IClone newClone4 = storeProvider.lookupClone(clone1.getUuid());
		assertTrue("hasExtensions does not match real extension status - hasExtensions: " + newClone4.hasExtensions()
				+ ", extensions: " + newClone4.getExtensions(), newClone4.hasExtensions() == !newClone4.getExtensions()
				.isEmpty());
		assertTrue("multi stateful extension was not persisted", newClone4.hasExtensions());
		ICloneModificationHistoryExtension newExtension = (ICloneModificationHistoryExtension) newClone4
				.getExtension(ICloneModificationHistoryExtension.class);
		assertTrue("unable to retrieve multi extension from clone", newExtension != null);

		//now fetch sub-elements for the extension
		ICloneModificationHistoryExtension newExtension2 = (ICloneModificationHistoryExtension) storeProvider
				.getFullCloneObjectExtension(newClone4, (ICloneObjectExtensionLazyMultiStatefulObject) newExtension);

		assertTrue("unable to retrieve multi extension from clone", newExtension2 != null);
		assertTrue("multi extension does not have correct number of sub entries - got: "
				+ newExtension.getCloneDiffs().size() + ", expected: 2 - " + newExtension2.getCloneDiffs(),
				newExtension2.getCloneDiffs().size() == 2);
		assertTrue("diff1 missmatch", newExtension2.getCloneDiffs().get(0).equals(diff1));
		assertTrue("diff2 missmatch", newExtension2.getCloneDiffs().get(1).equals(diff2));

		/*
		 * Clear cache.
		 */
		if (clearCache)
			clearCache(file);

		/*
		 * Make sure that clone2 wasn't affected in any way.
		 */

		log.trace("testCloneExtensionAddAndRemoveCloneHistory() - making sure that clone2 was not affected.");

		IClone staticClone = storeProvider.lookupClone(clone2.getUuid());

		//before comparing both, we need to make sure that the dirty flag is set equally
		((IStoreCloneObject) clone2).setDirty(false);
		((IStoreCloneObject) staticClone).setDirty(false);

		assertTrue("clone2 was somehow affected, not equal", clone2.equalsAll(staticClone));
		assertTrue("clone2 was somehow affected, has extensions", !staticClone.hasExtensions());

	}

	/**
	 * Tests thread safety by generating a large number of threads which add random clone data
	 * to the store provider.
	 */
	/*
	@Test
	public void testStressTest() throws SQLException
	{
		int runnerCount = 15;
		int numberOfFiles = 100;
		int runtime = 60;

		List<TestRunner> runners = new LinkedList<TestRunner>();

		//create a couple of test runners
		for (int i = 0; i < runnerCount; ++i)
		{
			TestRunner r = new TestRunner(cloneFactoryProvider, storeProvider, numberOfFiles);
			r.start();
			runners.add(r);
		}

		//now wait for a bit
		try
		{
			Thread.sleep(1000 * runtime);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}

		log.debug("telling all test runners to stop");
		//send all a shutdown signal
		for (TestRunner runner : runners)
		{
			runner.shutdown();
		}

		//wait for all to finish
		for (TestRunner runner : runners)
		{
			try
			{
				runner.join();
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}

		//check integrity
		if (storeProvider instanceof AbstractStoreProvider)
		{
			log.info(((AbstractStoreProvider) storeProvider).getCacheStats());
			assertTrue(((AbstractStoreProvider) storeProvider).checkCacheIntegrity());
		}

	}

	private class TestRunner extends Thread
	{
		private ICloneFactoryProvider cloneFactory;
		private IStoreProvider storeProvider;
		private List<String> fileNames;
		private Random rnd;
		private boolean shutdown = false;

		public TestRunner(ICloneFactoryProvider cloneFactory, IStoreProvider storeProvider, int numberOfFiles)
		{
			this.rnd = new Random();
			this.cloneFactory = cloneFactory;
			this.storeProvider = storeProvider;

			fileNames = new ArrayList<String>(numberOfFiles);
			for (int i = 0; i < numberOfFiles; ++i)
			{
				fileNames.add("file-" + i);
			}
		}

		@Override
		public void run()
		{
			while (!shutdown)
			{
				//random sleep
				try
				{
					Thread.sleep((int) (1000 * Math.random()));
				}
				catch (InterruptedException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				try
				{
					doTestRun();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}

		public void shutdown()
		{
			shutdown = true;
		}

		private void doTestRun() throws Exception
		{
			/*
			 * lookup a file
			 * /
			String filePath = randomFile();
			log.debug("file path: " + filePath);
			ICloneFile file = storeProvider.lookupCloneFileByPath("test", filePath);
			assertNotNull(file);
			log.debug("got file: " + file);

			/*
			 * get clones for file 
			 * /
			List<IClone> clones = storeProvider.getClonesByFile(file.getUuid());
			assertNotNull(clones);
			log.debug("got clones: " + clones);

			/*
			 * pick random clone and get clone group data
			 * /
			IClone clone = null;
			if (!clones.isEmpty())
			{
				clone = clones.get(randomIndex(clones.size()));
				log.debug("random clone: " + clone);
				if (clone.getGroupUuid() != null)
				{
					List<IClone> groupClones = storeProvider.getClonesByGroup(clone.getGroupUuid());
					log.debug("got group clones: " + groupClones);
				}
			}

			/*
			 * add a new clone to the file, either using the group from the earlier clone
			 * or two new clones at once and a new group for them
			 * /
			if (clone != null && clone.getGroupUuid() != null && (Math.random() > 0.7))
			{
				//add a new clone for this group
				IClone newClone = randomClone(file.getUuid(), clone.getGroupUuid());

				storeProvider.acquireWriteLock();
				storeProvider.addClone(newClone);
				storeProvider.releaseWriteLock();
			}
			else
			{
				//add a new group and two clones
				ICloneGroup newGroup = cloneFactory.getCloneGroupInstance();
				IClone newClone1 = randomClone(file.getUuid(), newGroup.getUuid());
				IClone newClone2 = randomClone(file.getUuid(), newGroup.getUuid());

				storeProvider.acquireWriteLock();
				storeProvider.addCloneGroup(newGroup);
				storeProvider.addClone(newClone1);
				storeProvider.addClone(newClone2);
				storeProvider.releaseWriteLock();
			}

			//decide whether we should persist, revert or keep going
			double rand = Math.random();
			rand = 0.9;
			if (rand > 0.8)
			{
				log.debug("persisting data for: " + file);
				storeProvider.acquireWriteLock();
				storeProvider.persistData(file);
				storeProvider.releaseWriteLock();
			}
			else if (rand > 0.7)
			{
				log.debug("reverting data for: " + file);
				storeProvider.acquireWriteLock();
				storeProvider.revertData(file);
				storeProvider.releaseWriteLock();
			}
			//else, just keep going

			//dump some stats
			if (storeProvider instanceof SQLStoreProvider)
				log.debug(((SQLStoreProvider) storeProvider).getCacheStats());
		}

		private IClone randomClone(String fileUuid, String groupUuid)
		{
			IClone newClone = cloneFactory.getCloneInstance();
			newClone.setCreationDate(new Date());
			newClone.setCreator("exp");
			newClone.setFileUuid(fileUuid);
			newClone.setGroupUuid(groupUuid);
			newClone.setTransient(false);

			IClonePosition pos = cloneFactory.getClonePositionInstance();

			int startAbsoluteOffset = randomRange(0, 5000);
			int startOffset = startAbsoluteOffset + randomRange(0, 500);
			pos.setStart(startOffset, startAbsoluteOffset);

			int len = randomRange(1, 500);
			int endOffset = startOffset + len + randomRange(0, 50);
			int endAbsoluteOffset = startAbsoluteOffset + len;
			pos.setEnd(endOffset, endAbsoluteOffset);

			newClone.setPosition(pos);

			return newClone;
		}

		private String randomFile()
		{
			return fileNames.get(randomIndex(fileNames.size()));
		}

		private int randomIndex(int lowerThan)
		{
			if (lowerThan <= 0)
				return 0;

			return rnd.nextInt(lowerThan);
		}

		private int randomRange(int lowerBound, int upperBound)
		{
			return randomIndex(upperBound - lowerBound) + lowerBound;
		}
	}
	*/

	private void clearCache(ICloneFile file) throws StoreLockingException
	{
		try
		{
			storeProvider.acquireWriteLock(LockMode.DEFAULT);
			//make sure we don't get errors due to the file not being cached
			storeProvider.getClonesByFile(file.getUuid());
			storeProvider.persistData(file);
			storeProvider.hintPurgeCache(file);
		}
		finally
		{
			storeProvider.releaseWriteLock();
		}
	}
}

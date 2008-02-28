package org.electrocodeogram.cpc.test.tests;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.electrocodeogram.cpc.core.api.provider.store.StoreLockingException;
import org.electrocodeogram.cpc.test.utils.TestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class EmptyTest
{
	private static Log log = LogFactory.getLog(EmptyTest.class);

	private static final String PROJECT_NAME = "EmptyTest";

	/**
	 * Perform pre-test initialisation.
	 */
	@Before
	public void setUp() throws Exception
	{
		TestUtils.defaultSetUp(PROJECT_NAME, "project1", false, false);
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
	public void testEmpty() throws StoreLockingException
	{
		log.debug("testEmpty()");
	}
}

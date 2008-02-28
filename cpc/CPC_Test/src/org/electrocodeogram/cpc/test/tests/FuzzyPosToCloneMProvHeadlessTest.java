package org.electrocodeogram.cpc.test.tests;


import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.electrocodeogram.cpc.core.CPCCorePlugin;
import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.data.ICloneFile;
import org.electrocodeogram.cpc.core.api.provider.data.ICloneFactoryProvider;
import org.electrocodeogram.cpc.core.api.provider.store.StoreLockingException;
import org.electrocodeogram.cpc.core.api.provider.track.IFuzzyPositionToCloneMatchingProvider;
import org.electrocodeogram.cpc.test.utils.TestCloneUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


/**
 * A simple test suite for the {@link IFuzzyPositionToCloneMatchingProvider}.
 * 
 * @author vw
 */
public class FuzzyPosToCloneMProvHeadlessTest
{
	private static Log log = LogFactory.getLog(FuzzyPosToCloneMProvHeadlessTest.class);

	private ICloneFactoryProvider cloneFactoryProvider;
	private IFuzzyPositionToCloneMatchingProvider fuzzyPositionToCloneMatchingProvider;

	public FuzzyPosToCloneMProvHeadlessTest()
	{
		cloneFactoryProvider = (ICloneFactoryProvider) CPCCorePlugin.getProviderRegistry().lookupProvider(
				ICloneFactoryProvider.class);
		assertNotNull("failed to obtain clone factory provider", cloneFactoryProvider);

		fuzzyPositionToCloneMatchingProvider = (IFuzzyPositionToCloneMatchingProvider) CPCCorePlugin
				.getProviderRegistry().lookupProvider(IFuzzyPositionToCloneMatchingProvider.class);
		assertNotNull("failed to obtain fuzzy position to clone matching provider",
				fuzzyPositionToCloneMatchingProvider);
	}

	/**
	 * Perform pre-test initialisation.
	 */
	@Before
	public void setUp() throws Exception
	{
	}

	/**
	 * Perform post-test cleanup.
	 */
	@After
	public void tearDown() throws Exception
	{
	}

	/**
	 * Test the matching provider in case of exact offset/length matches.
	 */
	@Test
	public void testExactMatch() throws StoreLockingException
	{
		log.debug("testExactMatch()");

		/*
		 * Prepare data.
		 */
		String fileContent = "ABC\nDEF\nGEH\nIJK\nLMN\nOPQ\nRST\nUVW\nXYZ";
		IDocument document = new Document(fileContent);
		ICloneFile cloneFile = (ICloneFile) cloneFactoryProvider.getInstance(ICloneFile.class);
		List<IClone> clones = new LinkedList<IClone>();

		/*
		 * Create clone instances.
		 */

		//ABC
		IClone clone1 = TestCloneUtils.createTestClone(cloneFactoryProvider, cloneFile, null, 0, 2, document);
		clones.add(clone1);

		//GEH
		IClone clone2 = TestCloneUtils.createTestClone(cloneFactoryProvider, cloneFile, null, 8, 10, document);
		clones.add(clone2);

		//OPQ\nRST\nUVW\n
		IClone clone3 = TestCloneUtils.createTestClone(cloneFactoryProvider, cloneFile, null, 20, 31, document);
		clones.add(clone3);

		/*
		 * Do the testing.
		 */

		//ABC
		IClone clone1m = fuzzyPositionToCloneMatchingProvider.findClone(cloneFile, clones, 0, 3, fileContent);
		assertTrue("Matching of clone failed - expected: " + clone1 + ", got: " + clone1m, clone1.equals(clone1m));

		//Nothing (DEF)
		IClone clone2m = fuzzyPositionToCloneMatchingProvider.findClone(cloneFile, clones, 4, 3, fileContent);
		assertTrue("Matching of clone failed - expected: null, got: " + clone2m, clone2m == null);

		//OPQ\nRST\nUVW\n
		IClone clone3m = fuzzyPositionToCloneMatchingProvider.findClone(cloneFile, clones, 20, 12, fileContent);
		assertTrue("Matching of clone failed - expected: " + clone3 + ", got: " + clone3m, clone3.equals(clone3m));

		log.debug("testExactMatch() - done.");
	}

	/**
	 * Test non-exact matches with matching non-whitespace positions.
	 */
	@Test
	public void testNonWsMatch() throws StoreLockingException
	{
		log.debug("testNonWsMatch()");

		/*
		 * Prepare data.
		 */
		String fileContent = "ABC\nDEF\nGEH\nIJK\nLMN\nOPQ\nRST\nUVW\nXYZ";
		IDocument document = new Document(fileContent);
		ICloneFile cloneFile = (ICloneFile) cloneFactoryProvider.getInstance(ICloneFile.class);
		List<IClone> clones = new LinkedList<IClone>();

		/*
		 * Create clone instances.
		 */

		//ABC
		IClone clone1 = TestCloneUtils.createTestClone(cloneFactoryProvider, cloneFile, null, 0, 2, document);
		clones.add(clone1);

		//GEH
		IClone clone2 = TestCloneUtils.createTestClone(cloneFactoryProvider, cloneFile, null, 8, 10, document);
		clones.add(clone2);

		//OPQ\nRST\nUVW\n
		IClone clone3 = TestCloneUtils.createTestClone(cloneFactoryProvider, cloneFile, null, 20, 31, document);
		clones.add(clone3);

		/*
		 * Do the testing.
		 */

		//ABC\n
		IClone clone1m = fuzzyPositionToCloneMatchingProvider.findClone(cloneFile, clones, 0, 4, fileContent);
		assertTrue("Matching of clone failed - expected: " + clone1 + ", got: " + clone1m, clone1.equals(clone1m));

		//Nothing (\nDEF\n)
		IClone clone2m = fuzzyPositionToCloneMatchingProvider.findClone(cloneFile, clones, 3, 5, fileContent);
		assertTrue("Matching of clone failed - expected: null, got: " + clone2m, clone2m == null);

		//\nOPQ\nRST\nUVW
		IClone clone3m = fuzzyPositionToCloneMatchingProvider.findClone(cloneFile, clones, 19, 12, fileContent);
		assertTrue("Matching of clone failed - expected: " + clone3 + ", got: " + clone3m, clone3.equals(clone3m));

		log.debug("testNonWsMatch() - done.");
	}

	/**
	 * Test non-exact matches based on start and end line.
	 */
	@Test
	public void testLineMatch() throws StoreLockingException
	{
		log.debug("testLineMatch()");

		/*
		 * Prepare data.
		 */
		String fileContent = "ABC\nDEF\nGEH\nIJK\nLMN\nOPQ\nRST\nUVW\nXYZ";
		IDocument document = new Document(fileContent);
		ICloneFile cloneFile = (ICloneFile) cloneFactoryProvider.getInstance(ICloneFile.class);
		List<IClone> clones = new LinkedList<IClone>();

		/*
		 * Create clone instances.
		 */

		//ABC
		IClone clone1 = TestCloneUtils.createTestClone(cloneFactoryProvider, cloneFile, null, 0, 2, document);
		clones.add(clone1);

		//GEH
		IClone clone2 = TestCloneUtils.createTestClone(cloneFactoryProvider, cloneFile, null, 8, 10, document);
		clones.add(clone2);

		//OPQ\nRST\nUVW\n
		IClone clone3 = TestCloneUtils.createTestClone(cloneFactoryProvider, cloneFile, null, 20, 31, document);
		clones.add(clone3);

		/*
		 * Do the testing.
		 */

		//Nothing (IJK\nLMN\nOPQ\n)
		IClone clone2m = fuzzyPositionToCloneMatchingProvider.findClone(cloneFile, clones, 11, 12, fileContent);
		assertTrue("Matching of clone failed - expected: null, got: " + clone2m, clone2m == null);

		//PQ\nRST\nUV
		IClone clone3m = fuzzyPositionToCloneMatchingProvider.findClone(cloneFile, clones, 21, 9, fileContent);
		assertTrue("Matching of clone failed - expected: " + clone3 + ", got: " + clone3m, clone3.equals(clone3m));

		log.debug("testLineMatch() - done.");
	}

}

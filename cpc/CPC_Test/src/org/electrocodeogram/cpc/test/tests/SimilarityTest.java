package org.electrocodeogram.cpc.test.tests;


import static org.junit.Assert.assertTrue;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.electrocodeogram.cpc.core.CPCCorePlugin;
import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.data.ICloneFile;
import org.electrocodeogram.cpc.core.api.data.ICloneGroup;
import org.electrocodeogram.cpc.core.api.provider.data.ICloneFactoryProvider;
import org.electrocodeogram.cpc.core.api.provider.similarity.ISimilarityProvider;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider.LockMode;
import org.electrocodeogram.cpc.test.CPCTestPlugin;
import org.electrocodeogram.cpc.test.utils.TestCloneUtils;
import org.electrocodeogram.cpc.test.utils.TestEditorUtils;
import org.electrocodeogram.cpc.test.utils.TestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class SimilarityTest
{
	private static Log log = LogFactory.getLog(SimilarityTest.class);

	private static final String PROJECT_NAME = "SimilarityTest";

	/**
	 * Perform pre-test initialisation.
	 */
	@Before
	public void setUp() throws Exception
	{
		TestUtils.defaultSetUp(PROJECT_NAME, "project1", false, true);
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
	public void testSimilarityProvider() throws Exception
	{
		log.debug("testSimilarityProvider()");

		final String FILE = "src/test/Similarity.java";

		/*
		 * Open file in editor.
		 */
		IFile file = TestUtils.getFile(PROJECT_NAME, FILE);
		IEditorPart editor = TestEditorUtils.openEditor(file);

		assertTrue("text editor expected", editor instanceof ITextEditor);

		ITextEditor textEditor = (ITextEditor) editor;
		IDocumentProvider provider = textEditor.getDocumentProvider();
		IDocument document = provider.getDocument(textEditor.getEditorInput());

		TestUtils.delay(CPCTestPlugin.TEST_PRECHECK_DELAY);

		/*
		 * Get provider instances.
		 */

		ISimilarityProvider similarityProvider = (ISimilarityProvider) CPCCorePlugin.getProviderRegistry()
				.lookupProvider(ISimilarityProvider.class);
		assertTrue("failed to obtain similarity provider instance", similarityProvider != null);

		IStoreProvider storeProvider = (IStoreProvider) CPCCorePlugin.getProviderRegistry().lookupProvider(
				IStoreProvider.class);
		assertTrue("failed to obtain store provider instance", storeProvider != null);

		ICloneFactoryProvider cloneFactoryProvider = (ICloneFactoryProvider) CPCCorePlugin.getProviderRegistry()
				.lookupProvider(ICloneFactoryProvider.class);
		assertTrue("failed to obtain clone factory provider instance", cloneFactoryProvider != null);

		/*
		 * Create fake clone data.
		 */

		ICloneFile cloneFile = storeProvider.lookupCloneFileByPath(PROJECT_NAME, FILE, true, true);
		assertTrue("failed to obtain clone file instance", cloneFile != null);
		List<ICloneGroup> cloneGroups = new LinkedList<ICloneGroup>();
		List<IClone> clones = new LinkedList<IClone>();

		ICloneGroup cloneGroup1 = (ICloneGroup) cloneFactoryProvider.getInstance(ICloneGroup.class);
		cloneGroups.add(cloneGroup1);

		//first main method
		IClone clone1 = TestCloneUtils
				.createTestClone(cloneFactoryProvider, cloneFile, cloneGroup1, 195, 278, document);
		clones.add(clone1);

		//second main method (one line)
		IClone clone2 = TestCloneUtils
				.createTestClone(cloneFactoryProvider, cloneFile, cloneGroup1, 281, 357, document);
		clones.add(clone2);

		//3rd main method (string modified)
		IClone clone3 = TestCloneUtils
				.createTestClone(cloneFactoryProvider, cloneFile, cloneGroup1, 359, 441, document);
		clones.add(clone3);

		//4th main method (method and param renamed)
		IClone clone4 = TestCloneUtils
				.createTestClone(cloneFactoryProvider, cloneFile, cloneGroup1, 444, 539, document);
		clones.add(clone4);

		ICloneGroup cloneGroup2 = (ICloneGroup) cloneFactoryProvider.getInstance(ICloneGroup.class);
		cloneGroups.add(cloneGroup2);

		//1th main method with comment
		IClone clone1b = TestCloneUtils.createTestClone(cloneFactoryProvider, cloneFile, cloneGroup1, 542, 848,
				document);
		clones.add(clone1b);

		//2th main method with comment
		IClone clone2b = TestCloneUtils.createTestClone(cloneFactoryProvider, cloneFile, cloneGroup1, 850, 1166,
				document);
		clones.add(clone2b);

		ICloneGroup cloneGroup3 = (ICloneGroup) cloneFactoryProvider.getInstance(ICloneGroup.class);
		cloneGroups.add(cloneGroup3);

		//standalone code
		IClone clone1c = TestCloneUtils.createTestClone(cloneFactoryProvider, cloneFile, cloneGroup1, 1168, 1276,
				document);
		clones.add(clone1c);

		ICloneGroup cloneGroup4 = (ICloneGroup) cloneFactoryProvider.getInstance(ICloneGroup.class);
		cloneGroups.add(cloneGroup4);

		//big method - original
		IClone clone1d = TestCloneUtils.createTestClone(cloneFactoryProvider, cloneFile, cloneGroup1, 1279, 2370,
				document);
		clones.add(clone1d);

		//big method - renamed
		IClone clone2d = TestCloneUtils.createTestClone(cloneFactoryProvider, cloneFile, cloneGroup1, 2372, 3527,
				document);
		clones.add(clone2d);

		/*
		 * Now store the clone data.
		 */

		storeProvider.acquireWriteLock(LockMode.DEFAULT);
		for (ICloneGroup group : cloneGroups)
			storeProvider.addCloneGroup(group);
		storeProvider.addClones(clones);
		storeProvider.releaseWriteLock();

		/*
		 * Display to user.
		 */

		TestUtils.delay(1000);

		/*
		 * Check similarities.
		 */

		checkSimilarity(storeProvider, similarityProvider, clone1, clone2, 100, 100);

		checkSimilarity(storeProvider, similarityProvider, clone1, clone3, 80, 100);

		checkSimilarity(storeProvider, similarityProvider, clone1, clone4, 60, 100);

		checkSimilarity(storeProvider, similarityProvider, clone1b, clone2b, 100, 100);

		checkSimilarity(storeProvider, similarityProvider, clone1, clone1c, 0, 49);

		checkSimilarity(storeProvider, similarityProvider, clone1d, clone2d, 60, 100);

	}

	private void checkSimilarity(IStoreProvider storeProvider, ISimilarityProvider similarityProvider, IClone clone1,
			IClone clone2, int minSimilarity, int maxSimilarity)
	{
		int similarity = similarityProvider.calculateSimilarity(ISimilarityProvider.LANGUAGE_JAVA, clone1, clone2,
				false);

		if (log.isTraceEnabled())
			log.trace("checkSimilarity() - got: " + similarity + " - expecting: " + minSimilarity + "-" + maxSimilarity
					+ ", clone1: " + clone1 + ", clone2: " + clone2);

		if (similarity < minSimilarity || similarity > maxSimilarity)
		{
			log.info("CLONE1: " + clone1);
			log.info("  CONTENT: " + clone1.getContent());
			log.info("CLONE2: " + clone2);
			log.info("  CONTENT: " + clone2.getContent());
			assertTrue("similarity check failed - got: " + similarity + ", expected: " + minSimilarity + "-"
					+ maxSimilarity, false);
		}
	}
}

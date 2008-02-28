package org.electrocodeogram.cpc.test.tests;


import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.data.ICloneFile;
import org.electrocodeogram.cpc.core.api.provider.classification.IClassificationProvider;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider;
import org.electrocodeogram.cpc.test.CPCTestPlugin;
import org.electrocodeogram.cpc.test.utils.TestEditorUtils;
import org.electrocodeogram.cpc.test.utils.TestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


/**
 * Test cases for the {@link IClassificationProvider} implementation.
 * 
 * @author vw
 */
public class ClassificationTest
{
	private static Log log = LogFactory.getLog(ClassificationTest.class);

	private static final String PROJECT_NAME = "ClassificationTest";

	private IDocument document;
	private TestEditorUtils edUtils;
	private IStoreProvider storeProvider;
	private ICloneFile cloneFile;

	/**
	 * Perform pre-test initialisation.
	 */
	@Before
	public void setUp() throws Exception
	{
		TestUtils.defaultSetUp(PROJECT_NAME, "project1", true, true);
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
	 * Copy a class and ensure that it is correctly classified as CLASS.
	 */
	@Test
	public void testClassClassification() throws Exception
	{
		log.debug("testClassClassification()");

		final String FILE = "src/test/Similarity.java";

		prepare(FILE);

		/*
		 * Copy the entire class body.
		 * We don't actually paste it, the transient clone is enough for our test.
		 */
		edUtils.copyAction(0, document.getLength() - 1);

		TestUtils.delay(CPCTestPlugin.TEST_PRECHECK_DELAY);

		/*
		 * Now ensure that the new transient clone was correctly classified
		 * as CLASS.
		 */

		List<IClone> clones = storeProvider.getClonesByFile(cloneFile.getUuid());
		assertTrue("unexpected clone count - clones: " + clones, clones.size() == 1);

		assertTrue("clone does not have CLASS classification - classifications: " + clones.get(0).getClassifications(),
				clones.get(0).hasClassification(IClassificationProvider.CLASSIFICATION_CLASS));
	}

	/**
	 * Copy a method and ensure that it is correctly classified as METHOD.
	 */
	@Test
	public void testMethodClassification() throws Exception
	{
		log.debug("testMethodClassification()");

		final String FILE = "src/test/Similarity.java";

		prepare(FILE);

		/*
		 * Copy one method.
		 * We don't actually paste it, the transient clone is enough for our test.
		 */
		edUtils.copyAction(195, 84);

		TestUtils.delay(CPCTestPlugin.TEST_PRECHECK_DELAY);

		/*
		 * Now ensure that the new transient clone was correctly classified
		 * as CLASS.
		 */

		List<IClone> clones = storeProvider.getClonesByFile(cloneFile.getUuid());
		assertTrue("unexpected clone count - clones: " + clones, clones.size() == 1);

		assertTrue(
				"clone does not have METHOD classification - classifications: " + clones.get(0).getClassifications(),
				clones.get(0).hasClassification(IClassificationProvider.CLASSIFICATION_METHOD));
	}

	/**
	 * Copy a loop and ensure that it is correctly classified as LOOP.
	 */
	@Test
	public void testLoopClassification() throws Exception
	{
		log.debug("testLoopClassification()");

		final String FILE = "src/test/Similarity.java";

		prepare(FILE);

		/*
		 * Copy one condition.
		 * We don't actually paste it, the transient clone is enough for our test.
		 */
		edUtils.copyAction(2740, 236);

		TestUtils.delay(CPCTestPlugin.TEST_PRECHECK_DELAY);

		/*
		 * Now ensure that the new transient clone was correctly classified
		 * as CLASS.
		 */

		List<IClone> clones = storeProvider.getClonesByFile(cloneFile.getUuid());
		assertTrue("unexpected clone count - clones: " + clones, clones.size() == 1);

		assertTrue("clone does not have LOOP classification - classifications: " + clones.get(0).getClassifications(),
				clones.get(0).hasClassification(IClassificationProvider.CLASSIFICATION_LOOP));
	}

	/**
	 * Copy a condition and ensure that it is correctly classified as CONDITION.
	 */
	@Test
	public void testConditionClassification() throws Exception
	{
		log.debug("testConditionClassification()");

		final String FILE = "src/test/Similarity.java";

		prepare(FILE);

		/*
		 * Copy one condition.
		 * We don't actually paste it, the transient clone is enough for our test.
		 */
		edUtils.copyAction(3067, 147);

		TestUtils.delay(CPCTestPlugin.TEST_PRECHECK_DELAY);

		/*
		 * Now ensure that the new transient clone was correctly classified
		 * as CLASS.
		 */

		List<IClone> clones = storeProvider.getClonesByFile(cloneFile.getUuid());
		assertTrue("unexpected clone count - clones: " + clones, clones.size() == 1);

		assertTrue("clone does not have CONDITION classification - classifications: "
				+ clones.get(0).getClassifications(), clones.get(0).hasClassification(
				IClassificationProvider.CLASSIFICATION_CONDITION));
	}

	/**
	 * Copy a couple of complex methods and ensure that the clone is correctly classified as COMPLEX.
	 */
	@Test
	public void testComplexClassification() throws Exception
	{
		log.debug("testComplexClassification()");

		final String FILE = "src/test/Similarity.java";

		prepare(FILE);

		/*
		 * Copy one method.
		 * We don't actually paste it, the transient clone is enough for our test.
		 */
		edUtils.copyAction(1279, 2249);

		TestUtils.delay(CPCTestPlugin.TEST_PRECHECK_DELAY);

		/*
		 * Now ensure that the new transient clone was correctly classified
		 * as CLASS.
		 */

		List<IClone> clones = storeProvider.getClonesByFile(cloneFile.getUuid());
		assertTrue("unexpected clone count - clones: " + clones, clones.size() == 1);

		assertTrue("clone does not have COMPLEX classification - classifications: "
				+ clones.get(0).getClassifications(), clones.get(0).hasClassification(
				IClassificationProvider.CLASSIFICATION_COMPLEX));
	}

	private void prepare(String filePath) throws Exception
	{
		/*
		 * Open file in editor.
		 */
		IFile file = TestUtils.getFile(PROJECT_NAME, filePath);
		IEditorPart editor = TestEditorUtils.openEditor(file);

		assertTrue("text editor expected", editor instanceof ITextEditor);

		ITextEditor textEditor = (ITextEditor) editor;
		IDocumentProvider provider = textEditor.getDocumentProvider();
		document = provider.getDocument(textEditor.getEditorInput());
		edUtils = new TestEditorUtils(textEditor);

		TestUtils.delay(CPCTestPlugin.TEST_PRECHECK_DELAY);

		/*
		 * Get provider instance.
		 */
		storeProvider = TestUtils.getStoreProvider();

		cloneFile = storeProvider.lookupCloneFileByPath(PROJECT_NAME, filePath, true, false);
		assertTrue("unable to obtain clone file", cloneFile != null);
	}
}

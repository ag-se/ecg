package org.electrocodeogram.cpc.test.tests;


import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.electrocodeogram.cpc.test.CPCTestPlugin;
import org.electrocodeogram.cpc.test.utils.TestEditorUtils;
import org.electrocodeogram.cpc.test.utils.TestUtils;
import org.junit.After;
import org.junit.Before;


public class EclipseBugTest
{
	private static Log log = LogFactory.getLog(EclipseBugTest.class);

	private static final String PROJECT_NAME = "EmptyTest";

	/**
	 * Perform pre-test initialization.
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
	//@Test
	public void testTextFileEdit() throws Exception
	{
		log.info("testTextFileEdit()");

		final String FILE = "src/test/BigTextFile.txt";

		//open file in editor
		IFile file = TestUtils.getFile(PROJECT_NAME, FILE);
		IEditorPart editor = TestEditorUtils.openEditorNonJavaFile(file);

		assertTrue("text editor expected", editor instanceof ITextEditor);

		ITextEditor textEditor = (ITextEditor) editor;
		IDocumentProvider provider = textEditor.getDocumentProvider();
		IDocument document = provider.getDocument(textEditor.getEditorInput());

		TestUtils.delay(CPCTestPlugin.TEST_PRECHECK_DELAY);

		Random rand = new Random();

		while (true)
		{
			int offset = rand.nextInt(500);
			int length = rand.nextInt(10);
			String text = "1234567890";
			document.replace(offset, length, text);

			int wait = rand.nextInt(2000);
			TestUtils.delay(wait);
		}
	}
}

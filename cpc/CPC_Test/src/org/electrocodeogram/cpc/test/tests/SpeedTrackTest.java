package org.electrocodeogram.cpc.test.tests;


import static org.junit.Assert.assertTrue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.electrocodeogram.cpc.core.api.provider.store.StoreLockingException;
import org.electrocodeogram.cpc.test.CPCTestPlugin;
import org.electrocodeogram.cpc.test.utils.TestEditorUtils;
import org.electrocodeogram.cpc.test.utils.TestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class SpeedTrackTest
{
	private static Log log = LogFactory.getLog(SpeedTrackTest.class);

	private static final String PROJECT_NAME = "SpeedTrackTest";

	/**
	 * Perform pre-test initialization.
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
	 * Simulates a couple of simple copy&paste actions to create clones and then
	 * modifies the text and verifies correct tracking.
	 */
	@Test
	public void testTrackingSpeed() throws StoreLockingException, PartInitException, JavaModelException,
			BadLocationException
	{
		log.info("testTrackingSpeed()");

		final String FILE = "src/test/BigClass.java";

		//open file in editor
		IFile file = TestUtils.getFile(PROJECT_NAME, FILE);
		IEditorPart editor = TestEditorUtils.openEditor(file);

		assertTrue("text editor expected", editor instanceof ITextEditor);

		ITextEditor textEditor = (ITextEditor) editor;
		IDocumentProvider provider = textEditor.getDocumentProvider();
		IDocument document = provider.getDocument(textEditor.getEditorInput());
		TestEditorUtils edUtils = new TestEditorUtils(textEditor);

		TestUtils.delay(CPCTestPlugin.TEST_PRECHECK_DELAY);

		//keep overall time
		long allStart = System.currentTimeMillis();

		/*
		 * Create some clones.
		 */
		log.trace("testTrackingSpeed() - creating some clones");
		long creationStart = System.currentTimeMillis();

		//for (int i = 5000; i >= 100; i -= 100)
		for (int i = 1000; i <= 8000; i += 100)
		{
			edUtils.copyAction(i, 25);
			edUtils.pasteAction(i + 10, 0);
		}

		long creationTime = System.currentTimeMillis() - creationStart;
		log.debug("testTrackingSpeed() - clone creation time: " + creationTime + " ms (" + (creationTime / 1000)
				+ " s)");

		TestUtils.delay(CPCTestPlugin.TEST_PRECHECK_DELAY);

		/*
		 * Now we do some modifications to the document
		 */

		log.trace("testTrackingSpeed() - doing some modifications which do not affect the contents of any clone");
		long mod1Start = System.currentTimeMillis();

		for (int i = 500; i >= 0; --i)
		{
			document.replace(i, 0, "12345");
			document.replace(i, 8, "321");
		}

		long mod1Time = System.currentTimeMillis() - mod1Start;
		log.debug("testTrackingSpeed() - clone modification time1: " + mod1Time + " ms (" + (mod1Time / 1000) + " s)");

		//now do some modifications which affect clone content
		log.trace("testTrackingSpeed() - doing some modifications which affect the contents of clones");
		long mod2Start = System.currentTimeMillis();

		for (int i = 1000; i <= 8000; i += 100)
		{
			document.replace(i + 1, 5, "12345");
			document.replace(i + 2, 5, "12345");
			document.replace(i + 3, 5, "12345");
		}

		long mod2Time = System.currentTimeMillis() - mod2Start;
		log.debug("testTrackingSpeed() - clone modification time2: " + mod2Time + " ms (" + (mod2Time / 1000) + " s)");

		/*
		 * Display a summary
		 */
		long allTime = System.currentTimeMillis() - allStart;
		log.info("testTrackingSpeed() - timing results for this test:");
		log.info("  clone creation time      : " + creationTime + " ms (" + (creationTime / 1000) + " s)");
		log.info("  clone modification time 1: " + mod1Time + " ms (" + (mod1Time / 1000) + " s)");
		log.info("  clone modification time 2: " + mod2Time + " ms (" + (mod2Time / 1000) + " s)");
		log.info("  total time               : " + allTime + " ms (" + (allTime / 1000) + " s)");

		/*
		 * some results: (with INFO level logging for all modules, TRACE for test module)
		 * (HW: AMD Athlon64 X2 Dual Core 4200+, 2GB RAM, Eclipse 3.3, Linux 2.6.22.6, KDE 3.5.7, X 7.3)
		 * 2007-09-29 09:39:28 INFO  [main] SpeedTrackTest:134 -   clone creation time      : 5085 ms (5 s)
		 * 2007-09-29 09:39:28 INFO  [main] SpeedTrackTest:135 -   clone modification time 1: 959 ms (0 s)
		 * 2007-09-29 09:39:28 INFO  [main] SpeedTrackTest:136 -   clone modification time 2: 496 ms (0 s)
		 * 
		 * 2007-09-29 09:41:14 INFO  [main] SpeedTrackTest:134 -   clone creation time      : 4403 ms (4 s)
		 * 2007-09-29 09:41:14 INFO  [main] SpeedTrackTest:135 -   clone modification time 1: 928 ms (0 s)
		 * 2007-09-29 09:41:14 INFO  [main] SpeedTrackTest:136 -   clone modification time 2: 510 ms (0 s)
		 * 
		 * 2007-09-29 09:43:03 INFO  [main] SpeedTrackTest:134 -   clone creation time      : 4733 ms (4 s)
		 * 2007-09-29 09:43:03 INFO  [main] SpeedTrackTest:135 -   clone modification time 1: 967 ms (0 s)
		 * 2007-09-29 09:43:03 INFO  [main] SpeedTrackTest:136 -   clone modification time 2: 540 ms (0 s)
		 */
		/*
		 * Ok, we're done, wait a bit to let the user see the result in the clone view.
		 */
		TestUtils.delay(2000);

	}

}

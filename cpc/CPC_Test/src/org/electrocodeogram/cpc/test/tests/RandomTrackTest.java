package org.electrocodeogram.cpc.test.tests;


import static org.junit.Assert.assertTrue;

import java.util.Random;

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
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider;
import org.electrocodeogram.cpc.core.api.provider.store.StoreLockingException;
import org.electrocodeogram.cpc.test.utils.TestEditorUtils;
import org.electrocodeogram.cpc.test.utils.TestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class RandomTrackTest
{
	private static Log log = LogFactory.getLog(RandomTrackTest.class);

	private static final String PROJECT_NAME = "RandomTrackTest";

	private IStoreProvider storeProvider;

	private Random rnd;

	public RandomTrackTest()
	{
		this.rnd = new Random();
	}

	/**
	 * Perform pre-test initialization.
	 */
	@Before
	public void setUp() throws Exception
	{
		TestUtils.defaultSetUp(PROJECT_NAME, "project1", true, true);
		storeProvider = TestUtils.getStoreProvider();
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
	public void testCPCloneCreationAndTracking() throws StoreLockingException, PartInitException, JavaModelException,
			BadLocationException
	{
		log.info("testCPCloneCreationAndTracking()");

		final int RANDOM_CCPS = 100;
		final int RANDOM_EDITS = 3;
		final String FILE = "src/test/BigClass.java";

		//open file in editor
		IFile file = TestUtils.getFile(PROJECT_NAME, FILE);
		IEditorPart editor = TestEditorUtils.openEditor(file);

		assertTrue("text editor expected", editor instanceof ITextEditor);

		ITextEditor textEditor = (ITextEditor) editor;
		IDocumentProvider provider = textEditor.getDocumentProvider();
		IDocument document = provider.getDocument(textEditor.getEditorInput());
		TestEditorUtils edUtils = new TestEditorUtils(textEditor);

		TestUtils.delay(1000);

		/*
		 * Now do some random actions on the file
		 */

		edUtils.copyAction(0, 10);

		for (int i = 0; i < RANDOM_CCPS; ++i)
		{
			//get a random range which lies fully inside the current document.
			Range r = randomRange(0, 500, document);
			log.debug("CCP: " + r + " - CCPS RUN: " + (i + 1) + "/" + RANDOM_CCPS);

			//do either a copy, cut or paste action
			double rand = Math.random();
			if (rand < 0.5)
				edUtils.pasteAction(r.startOffset, r.length);
			else if (rand < 0.9)
				edUtils.copyAction(r.startOffset, r.length); //12345
			else
				edUtils.cutAction(r.startOffset, r.length); //12345

			TestUtils.delay(150);

			//now do some shuffeling inside the file
			for (int j = 0; j < RANDOM_EDITS; ++j)
			{
				Range r2 = randomRange(0, 50, document);
				log.debug("EDIT: " + r2 + " - EDIT RUN: " + (j + 1) + "/" + RANDOM_EDITS + " in CCPS RUN: " + (i + 1)
						+ "/" + RANDOM_CCPS);
				try
				{
					document.replace(r2.startOffset, r2.length, "1234567890\n");
				}
				catch (BadLocationException e)
				{
					log.error("BadLocationException on EDIT for range: " + r2 + ", document length: "
							+ document.getLength(), new Throwable());
				}

				//TestUtils.delay(2000);
			}

			TestUtils.delay(150);
		}

		/*
		 * Ok, we're done, wait a bit to let the user see the result in the clone view.
		 */
		TestUtils.delay(10000);

	}

	private Range randomRange(int minLength, int maxLength, IDocument document)
	{
		Range r = new Range();

		r.startOffset = randomRange(0, document.getLength() - 1);
		r.length = randomLength(minLength, maxLength, r.startOffset, document);

		return r;
	}

	private int randomLength(int min, int max, int startOffset, IDocument doc)
	{
		return Math.min(randomRange(min, max), (doc.getLength() - startOffset) - 1);
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

	private class Range
	{
		int startOffset;
		int length;

		@Override
		public String toString()
		{
			return "Range[startOffset: " + startOffset + ", length: " + length + "]";
		}
	}
}

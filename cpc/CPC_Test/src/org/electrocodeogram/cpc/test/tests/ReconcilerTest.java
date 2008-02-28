package org.electrocodeogram.cpc.test.tests;


import static org.junit.Assert.assertNotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.electrocodeogram.cpc.core.api.data.ICloneFile;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider;
import org.electrocodeogram.cpc.core.api.provider.store.StoreLockingException;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider.LockMode;
import org.electrocodeogram.cpc.core.utils.CoreUtils;
import org.electrocodeogram.cpc.test.CPCTestPlugin;
import org.electrocodeogram.cpc.test.utils.TestCloneUtils;
import org.electrocodeogram.cpc.test.utils.TestEditorUtils;
import org.electrocodeogram.cpc.test.utils.TestJavaUtils;
import org.electrocodeogram.cpc.test.utils.TestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class ReconcilerTest
{
	private static Log log = LogFactory.getLog(ReconcilerTest.class);

	private static final String PROJECT_NAME = "ReconcilerTest";

	private IStoreProvider storeProvider;

	/**
	 * Perform pre-test initialisation.
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
	 * Reformats the source code on a file which is not currently visible in an editor.<br/>
	 * First opens the file to create some clones, then saves and closes it before the 
	 * reformat is triggered.<br/>
	 * <br/>
	 * Test for: WhitespaceOnlyChangeStrategy<br/>
	 * <br/>
	 * This test is almost equal to: {@link SimpleTrackTest#testSourceReformatOnClosedFile()} 
	 */
	@Test
	public void testDirectSourceReformatOnClosedFile() throws Exception
	{
		log.debug("testDirectSourceReformatOnClosedFile()");

		/*
		 * Open Editor
		 */
		log.debug("testDirectSourceReformatOnClosedFile() - open editor");

		final String FILE = "src/test/SourceReformat.java";

		//open file in editor
		IFile file = TestUtils.getFile(PROJECT_NAME, FILE);
		IEditorPart editor = TestEditorUtils.openEditor(file);

		TestEditorUtils.checkEditor(editor);

		ITextEditor textEditor = (ITextEditor) editor;
		IDocumentProvider provider = textEditor.getDocumentProvider();
		IDocument document = provider.getDocument(textEditor.getEditorInput());
		TestEditorUtils edUtils = new TestEditorUtils(textEditor);

		TestUtils.delay(CPCTestPlugin.TEST_PRECHECK_DELAY);

		/*
		 * Create some clones
		 */
		log.debug("testDirectSourceReformatOnClosedFile() - create clones");

		edUtils.copyAction(49, 119); //entire main method
		edUtils.pasteAction(169, 0); //below main method, within class

		document.replace(72, 0, "2"); //rename first main method to "main2"

		edUtils.copyAction(113, 50); //"System .out   .println(       "Hello World!"    );"
		edUtils.pasteAction(164, 0); //start of next line

		TestUtils.delay(1000);

		/*
		 * now check the generated clone data 
		 */
		log.debug("testDirectSourceReformatOnClosedFile() - evaluate - PRE");

		ICloneFile cloneFile = storeProvider.lookupCloneFileByPath(PROJECT_NAME, FILE, true, true);
		assertNotNull("clone file not found", cloneFile);

		//expected positions
		int[] startPos = new int[] { 49, 113, 164, 222 };
		int[] lengths = new int[] { 172, 50, 52, 119 };
		TestCloneUtils.clonePosCheck(storeProvider, cloneFile, startPos, lengths);

		/*
		 * Save file and close editor
		 */
		log.debug("testDirectSourceReformatOnClosedFile() - save file and close editor");

		//save file
		textEditor.doSave(null);

		//close editor
		textEditor.close(false);

		TestUtils.delay(1000);

		/*
		 * Clean the cache.
		 * 
		 * PROBLEM:
		 * - external modification detection does not work if the file is still in cache.
		 * => a modification while eclipse is running might not be detected.
		 * TODO: look into better ways of detecting external modifications.
		 */
		clearCache(cloneFile);

		/*
		 * Source Reformat
		 */
		log.debug("testDirectSourceReformatOnClosedFile() - start source reformat");

		//read content from disk
		String source = CoreUtils.readFileContent(file);
		//log.trace("CONTENT ON DISK: " + source);

		//create temporary document and reformat it
		IDocument newDocument = new Document(source);
		TestJavaUtils.reformatSource(newDocument);

		//log.trace("REFORMAT RESULT: " + newDocument.get());

		//write reformatted data back to disk
		CoreUtils.writeFileContent(file, newDocument.get());

		TestUtils.delay(3000);

		//String newSource = CoreUtils.readFileContent(file);
		//log.trace("NEW CONTENT ON DISK: " + newSource);

		/*
		 * Reopen editor.
		 */
		editor = TestEditorUtils.openEditor(file);
		TestEditorUtils.checkEditor(editor);

		TestUtils.delay(2000);

		/*
		 * now check the generated clone data 
		 */
		log.debug("testDirectSourceReformatOnClosedFile() - evaluate - POST");

		//expected positions
		startPos = new int[] { 47, 93, 131, 172 };
		lengths = new int[] { 122, 35, 35, 84 };
		TestCloneUtils.clonePosCheck(storeProvider, cloneFile, startPos, lengths);

		/*
		 * Ok, we're done, wait a bit to let the user see the result in the clone view.
		 */
		TestUtils.delay(2000);
	}

	/**
	 * Reformats the source code on a file which is not currently visible in an editor.<br/>
	 * First opens the file to create some clones, then saves and closes it before the 
	 * reformat is triggered.<br/>
	 * <br/>
	 * Test for: DefaultDiffStrategy<br/>
	 * <br/>
	 * Initial clone creation is equal to: {@link SimpleTrackTest#testSourceReformatOnClosedFile()}<br/>
	 * <br/>
	 * TODO: this is not a good test case
	 */
	@Test
	public void testDirectModificationOnClosedFile() throws Exception
	{
		log.debug("testDirectModificationOnClosedFile()");

		/*
		 * Open Editor
		 */
		log.debug("testDirectModificationOnClosedFile() - open editor");

		final String FILE = "src/test/SourceReformat.java";

		//open file in editor
		IFile file = TestUtils.getFile(PROJECT_NAME, FILE);
		IEditorPart editor = TestEditorUtils.openEditor(file);

		TestEditorUtils.checkEditor(editor);

		ITextEditor textEditor = (ITextEditor) editor;
		IDocumentProvider provider = textEditor.getDocumentProvider();
		IDocument document = provider.getDocument(textEditor.getEditorInput());
		TestEditorUtils edUtils = new TestEditorUtils(textEditor);

		TestUtils.delay(CPCTestPlugin.TEST_PRECHECK_DELAY);

		/*
		 * Create some clones
		 */
		log.debug("testDirectModificationOnClosedFile() - create clones");

		edUtils.copyAction(49, 119); //entire main method
		edUtils.pasteAction(169, 0); //below main method, within class

		document.replace(72, 0, "2"); //rename first main method to "main2"

		edUtils.copyAction(113, 50); //"System .out   .println(       "Hello World!"    );"
		edUtils.pasteAction(164, 0); //start of next line

		TestUtils.delay(1000);

		/*
		 * now check the generated clone data 
		 */
		log.debug("testDirectModificationOnClosedFile() - evaluate - PRE");

		ICloneFile cloneFile = storeProvider.lookupCloneFileByPath(PROJECT_NAME, FILE, true, true);
		assertNotNull("clone file not found", cloneFile);

		//expected positions
		int[] startPos = new int[] { 49, 113, 164, 222 };
		int[] lengths = new int[] { 172, 50, 52, 119 };
		TestCloneUtils.clonePosCheck(storeProvider, cloneFile, startPos, lengths);

		/*
		 * Save file and close editor
		 */
		log.debug("testDirectModificationOnClosedFile() - save file and close editor");

		//save file
		textEditor.doSave(null);

		//close editor
		textEditor.close(false);

		TestUtils.delay(1000);

		/*
		 * Clean the cache.
		 * 
		 * PROBLEM:
		 * - external modification detection does not work if the file is still in cache.
		 * => a modification while eclipse is running might not be detected.
		 * TODO: look into better ways of detecting external modifications.
		 */
		clearCache(cloneFile);

		/*
		 * Source Reformat
		 */
		log.debug("testDirectModificationOnClosedFile() - start source reformat");

		//read content from disk
		String source = CoreUtils.readFileContent(file);
		//log.trace("CONTENT ON DISK: " + source);

		//create temporary document and reformat it
		IDocument newDocument = new Document(source);
		TestJavaUtils.reformatSource(newDocument);

		//the content was now reformatted, and should look like this:
		/*
		package test;

		public class SourceReformat {

		public static void main2(String[] args)

		{
		System.out.println("Hello World!");
		System.out.println("Hello World!");
		}

		public static void main(String[] args)

		{
		System.out.println("Hello World!");

		}

		}
		 */

		//now add some new content
		newDocument.replace(46, 0,
				"\tpublic static void main3(String[] args)\n\t{\n\t\tSystem.out.println(\"Bla!\");\n\t}\n");

		//write reformatted data back to disk
		CoreUtils.writeFileContent(file, newDocument.get());

		TestUtils.delay(3000);

		//String newSource = CoreUtils.readFileContent(file);
		//log.trace("NEW CONTENT ON DISK: " + newSource);

		/*
		 * Reopen editor.
		 */
		log.debug("testDirectModificationOnClosedFile() - re-open editor");
		editor = TestEditorUtils.openEditor(file);
		TestEditorUtils.checkEditor(editor);

		TestUtils.delay(2000);

		/*
		 * now check the generated clone data 
		 */
		log.debug("testDirectModificationOnClosedFile() - evaluate - POST");

		//expected positions
		//FIXME: These are not the optimal positions, but rather the ones which result from
		//		 the strange diff events which are generated by the diff utils for this modification.
		//		 This test case may thus well fail with other diff implementations and it would not
		//		 be a bad sign. This test case would then need manual checking.
		startPos = new int[] { 47, 170, 206, 249 };
		lengths = new int[] { 200, 35, 37, 84 };
		TestCloneUtils.clonePosCheck(storeProvider, cloneFile, startPos, lengths);

		/*
		 * Ok, we're done, wait a bit to let the user see the result in the clone view.
		 */
		TestUtils.delay(2000);
	}

	/**
	 * Does some direct modifications on a closed file which also include clones
	 * which are not delimited by whitespace characters. 
	 */
	@Test
	public void testDirectModificationOnClosedFile2() throws Exception
	{
		log.debug("testDirectModificationOnClosedFile2()");

		/*
		 * Open Editor
		 */
		log.debug("testDirectModificationOnClosedFile2() - open editor");

		final String FILE = "src/test/Class2.java";

		//open file in editor
		IFile file = TestUtils.getFile(PROJECT_NAME, FILE);
		IEditorPart editor = TestEditorUtils.openEditor(file);

		TestEditorUtils.checkEditor(editor);

		ITextEditor textEditor = (ITextEditor) editor;
		IDocumentProvider provider = textEditor.getDocumentProvider();
		IDocument document = provider.getDocument(textEditor.getEditorInput());
		TestEditorUtils edUtils = new TestEditorUtils(textEditor);

		TestUtils.delay(CPCTestPlugin.TEST_PRECHECK_DELAY);

		/*
		 * Create some clones
		 * 
		 * Content:
		 * 12345
		 * 67890
		 * 12345
		 */
		log.debug("testDirectModificationOnClosedFile2() - create clones");

		edUtils.copyAction(0, 5); //first line "12345"
		edUtils.pasteAction(5, 0); //right beside itself

		edUtils.copyAction(12, 3); //"789"
		edUtils.pasteAction(19, 0); //middle of last line

		TestUtils.delay(1000);

		/*
		 * now check the generated clone data
		 * 
		 * Content:
		 * 1234512345
		 * 67890
		 * 12789345
		 */
		log.debug("testDirectModificationOnClosedFile2() - evaluate - PRE");

		ICloneFile cloneFile = storeProvider.lookupCloneFileByPath(PROJECT_NAME, FILE, true, true);
		assertNotNull("clone file not found", cloneFile);

		//expected positions
		int[] startPos = new int[] { 0, 5, 12, 19 };
		int[] lengths = new int[] { 5, 5, 3, 3 };
		TestCloneUtils.clonePosCheck(storeProvider, cloneFile, startPos, lengths);

		/*
		 * Save file and close editor
		 */
		log.debug("testDirectModificationOnClosedFile() - save file and close editor");

		//save file
		textEditor.doSave(null);

		//close editor
		textEditor.close(false);

		TestUtils.delay(1000);

		/*
		 * Clean the cache.
		 * 
		 * PROBLEM:
		 * - external modification detection does not work if the file is still in cache.
		 * => a modification while eclipse is running might not be detected.
		 * TODO: look into better ways of detecting external modifications.
		 */
		clearCache(cloneFile);

		/*
		 * Source Reformat
		 */
		log.debug("testDirectModificationOnClosedFile2() - start source reformat");

		//read content from disk
		String source = CoreUtils.readFileContent(file);
		//log.trace("CONTENT ON DISK: " + source);

		//create temporary document and reformat it
		IDocument newDocument = new Document(source);

		//now add some new content to the very beginning of the file
		newDocument.replace(0, 0, "   	\n\n");
		//6 whitespace characters

		//write reformatted data back to disk
		CoreUtils.writeFileContent(file, newDocument.get());

		TestUtils.delay(3000);

		//String newSource = CoreUtils.readFileContent(file);
		//log.trace("NEW CONTENT ON DISK: " + newSource);

		/*
		 * Reopen editor.
		 */
		log.debug("testDirectModificationOnClosedFile2() - re-open editor");
		editor = TestEditorUtils.openEditor(file);
		TestEditorUtils.checkEditor(editor);

		TestUtils.delay(2000);

		/*
		 * now check the generated clone data 
		 */
		log.debug("testDirectModificationOnClosedFile2() - evaluate - POST");

		//expected positions
		//FIXME: These are not the optimal positions, but rather the ones which result from
		//		 the strange diff events which are generated by the diff utils for this modification.
		//		 This test case may thus well fail with other diff implementations and it would not
		//		 be a bad sign. This test case would then need manual checking.
		startPos = new int[] { 0 + 6, 5 + 6, 12 + 6, 19 + 6 };
		lengths = new int[] { 5, 5, 3, 3 };
		TestCloneUtils.clonePosCheck(storeProvider, cloneFile, startPos, lengths);

		/*
		 * Ok, we're done, wait a bit to let the user see the result in the clone view.
		 */
		TestUtils.delay(2000);
	}

	private void clearCache(ICloneFile file) throws StoreLockingException
	{
		try
		{
			storeProvider.acquireWriteLock(LockMode.DEFAULT);

			//make store provider happy, otherwise it might complain that there is nothing to persist
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

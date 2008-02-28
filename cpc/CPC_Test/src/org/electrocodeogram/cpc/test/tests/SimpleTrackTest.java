package org.electrocodeogram.cpc.test.tests;


import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.data.ICloneFile;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider;
import org.electrocodeogram.cpc.core.api.provider.store.StoreLockingException;
import org.electrocodeogram.cpc.test.CPCTestPlugin;
import org.electrocodeogram.cpc.test.utils.TestCloneUtils;
import org.electrocodeogram.cpc.test.utils.TestEditorUtils;
import org.electrocodeogram.cpc.test.utils.TestJavaUtils;
import org.electrocodeogram.cpc.test.utils.TestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


/**
 * Does some simple copy and paste tracking tests.
 * 
 * NOTE: these tests will fail unless the CPC Classifier is configured to
 * 		 accept clones of _all_ sizes!
 * 		 Please modify <em>CPC_Classifier/preferences.ini</em> accordingly,
 * 		 before running these tests.
 * 
 * @author vw
 */
public class SimpleTrackTest
{
	private static Log log = LogFactory.getLog(SimpleTrackTest.class);

	private static final String PROJECT_NAME = "SimpleTrackTest";

	private IStoreProvider storeProvider;

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

		final String FILE = "src/test/Class2.java";

		//open file in editor
		IFile file = TestUtils.getFile(PROJECT_NAME, FILE);
		IEditorPart editor = TestEditorUtils.openEditor(file);

		assertTrue("text editor expected", editor instanceof ITextEditor);

		ITextEditor textEditor = (ITextEditor) editor;
		IDocumentProvider provider = textEditor.getDocumentProvider();
		IDocument document = provider.getDocument(textEditor.getEditorInput());
		TestEditorUtils edUtils = new TestEditorUtils(textEditor);

		TestUtils.delay(CPCTestPlugin.TEST_PRECHECK_DELAY);

		/*
		 * Initial file content:
		 * 
		 * 12345
		 * 67890
		 * 12345
		 */

		/*
		 * create two clone groups with two clones each
		 */
		edUtils.copyAction(0, 5); //12345
		edUtils.pasteAction(18, 0); //end of file

		edUtils.copyAction(6, 5); //67890
		edUtils.pasteAction(17, 0); //end of 3rd line

		TestUtils.delay(CPCTestPlugin.TEST_PRECHECK_DELAY);

		/*
		 * Resulting file content:
		 * 
		 * 12345
		 * 67890
		 * 1234567890
		 * 12345
		 */

		/*
		 * now check the generated clone data 
		 */
		ICloneFile cloneFile = storeProvider.lookupCloneFileByPath(PROJECT_NAME, FILE, true, true);
		assertNotNull("clone file not found", cloneFile);

		//expected positions
		int[] startPos = new int[] { 0, 6, 17, 23 };
		int[] lengths = new int[] { 5, 5, 5, 5 };
		TestCloneUtils.clonePosCheck(storeProvider, cloneFile, startPos, lengths);

		/*
		 * Now we do some modifications to the document
		 */

		//move everything down one line
		document.replace(0, 0, "abcde\n");

		/*
		 * abcde
		 * 12345
		 * 67890
		 * 1234567890
		 * 12345
		 */

		TestUtils.delay(CPCTestPlugin.TEST_PRECHECK_DELAY);

		startPos = new int[] { 6, 12, 23, 29 };
		lengths = new int[] { 5, 5, 5, 5 };
		TestCloneUtils.clonePosCheck(storeProvider, cloneFile, startPos, lengths);

		//insert something within first clone
		document.replace(7, 0, "22");

		/*
		 * abcde
		 * 1222345
		 * 67890
		 * 1234567890
		 * 12345
		 */

		TestUtils.delay(CPCTestPlugin.TEST_PRECHECK_DELAY);

		startPos = new int[] { 6, 14, 25, 31 };
		lengths = new int[] { 7, 5, 5, 5 };
		TestCloneUtils.clonePosCheck(storeProvider, cloneFile, startPos, lengths);

		//replace something within first clone
		document.replace(10, 2, "433");

		/*
		 * abcde
		 * 12224335
		 * 67890
		 * 1234567890
		 * 12345
		 */

		TestUtils.delay(CPCTestPlugin.TEST_PRECHECK_DELAY);

		startPos = new int[] { 6, 15, 26, 32 };
		lengths = new int[] { 8, 5, 5, 5 };
		TestCloneUtils.clonePosCheck(storeProvider, cloneFile, startPos, lengths);

		//completely delete 2nd clone
		document.replace(13, 9, "");

		/*
		 * abcde
		 * 1222433234567890
		 * 12345
		 */

		TestUtils.delay(CPCTestPlugin.TEST_PRECHECK_DELAY);

		startPos = new int[] { 6, 17, 23 };
		lengths = new int[] { 7, 5, 5 };
		TestCloneUtils.clonePosCheck(storeProvider, cloneFile, startPos, lengths);

		//add something to the end
		document.replace(28, 0, "\nxyz");

		/*
		 * abcde
		 * 1222433234567890
		 * 12345
		 * xyz
		 */

		TestUtils.delay(CPCTestPlugin.TEST_PRECHECK_DELAY);

		startPos = new int[] { 6, 17, 23 };
		lengths = new int[] { 7, 5, 5 };
		TestCloneUtils.clonePosCheck(storeProvider, cloneFile, startPos, lengths);

		//delete something from 2nd and 3rd clone
		document.replace(20, 5, "");

		/*
		 * abcde
		 * 12224332345678345
		 * xyz
		 */

		TestUtils.delay(CPCTestPlugin.TEST_PRECHECK_DELAY);

		startPos = new int[] { 6, 17, 20 };
		lengths = new int[] { 7, 3, 3 };
		TestCloneUtils.clonePosCheck(storeProvider, cloneFile, startPos, lengths);

		//make a copy of the entire data
		edUtils.copyAction(0, 27);
		//add a newline at the end
		document.replace(27, 0, "\n");
		//paste the data
		edUtils.pasteAction(28, 0);

		/*
		 * abcde
		 * 12224332345678345
		 * xyz
		 * abcde
		 * 12224332345678345
		 * xyz
		 */

		TestUtils.delay(CPCTestPlugin.TEST_PRECHECK_DELAY);

		startPos = new int[] { 0, 6, 17, 20, 28 };
		lengths = new int[] { 27, 7, 3, 3, 27 };
		TestCloneUtils.clonePosCheck(storeProvider, cloneFile, startPos, lengths);

		/*
		 * Ok, we're done, wait a bit to let the user see the result in the clone view.
		 */
		TestUtils.delay(2000);

	}

	/**
	 * Simulates a couple of copy&paste actions to create overlapping clones and then
	 * modifies the text and verifies correct tracking.
	 */
	@Test
	public void testCPOverlappingCloneCreationAndTracking() throws StoreLockingException, PartInitException,
			JavaModelException, BadLocationException
	{
		log.info("testCPOverlappingCloneCreationAndTracking()");

		final String FILE = "src/test/Class3.java";

		//open file in editor
		IFile file = TestUtils.getFile(PROJECT_NAME, FILE);
		IEditorPart editor = TestEditorUtils.openEditor(file);

		assertTrue("text editor expected", editor instanceof ITextEditor);

		ITextEditor textEditor = (ITextEditor) editor;
		IDocumentProvider provider = textEditor.getDocumentProvider();
		IDocument document = provider.getDocument(textEditor.getEditorInput());
		TestEditorUtils edUtils = new TestEditorUtils(textEditor);

		TestUtils.delay(CPCTestPlugin.TEST_PRECHECK_DELAY);

		/*
		 * Initial file content:
		 * 
		 * 12345
		 */

		/*
		 * create two copies of the data
		 */
		edUtils.copyAction(0, 6); //12345
		edUtils.pasteAction(6, 0); //end of file
		edUtils.pasteAction(12, 0); //end of file

		//copy the entire file once
		edUtils.copyAction(0, 18); //12345\n12345\n12345\n
		edUtils.pasteAction(18, 0); //end of file

		/*
		 * Resulting file content:
		 * 
		 * 12345
		 * 12345
		 * 12345
		 * 12345
		 * 12345
		 * 12345
		 */

		TestUtils.delay(CPCTestPlugin.TEST_PRECHECK_DELAY);

		/*
		 * now check the generated clone data 
		 */
		ICloneFile cloneFile = storeProvider.lookupCloneFileByPath(PROJECT_NAME, FILE, true, true);
		assertNotNull("clone file not found", cloneFile);

		//expected positions
		int[] startPos = new int[] { 0, 0, 6, 12, 18 };
		int[] lengths = new int[] { 6, 18, 6, 6, 18 };
		TestCloneUtils.clonePosCheck(storeProvider, cloneFile, startPos, lengths);

		/*
		 * Now we do some modifications to the document
		 */

		//delete the 2nd line
		document.replace(6, 6, "");

		/*
		 * 12345
		 * 12345
		 * 12345
		 * 12345
		 * 12345
		 */

		TestUtils.delay(CPCTestPlugin.TEST_PRECHECK_DELAY);

		startPos = new int[] { 0, 0, 6, 12 };
		lengths = new int[] { 6, 12, 6, 18 };
		TestCloneUtils.clonePosCheck(storeProvider, cloneFile, startPos, lengths);

		/*
		 * Ok, we're done, wait a bit to let the user see the result in the clone view.
		 */
		TestUtils.delay(2000);

	}

	/**
	 * Simulates a couple of copy&paste actions to create self replacing/modifying clones and then
	 * modifies the text and verifies correct tracking.
	 */
	@Test
	public void testCPSelfReplacingCloneCreationAndTracking() throws StoreLockingException, PartInitException,
			JavaModelException, BadLocationException
	{
		log.info("testCPSelfReplacingCloneCreationAndTracking()");

		final String FILE = "src/test/Class3.java";

		//open file in editor
		IFile file = TestUtils.getFile(PROJECT_NAME, FILE);
		IEditorPart editor = TestEditorUtils.openEditor(file);

		assertTrue("text editor expected", editor instanceof ITextEditor);

		ITextEditor textEditor = (ITextEditor) editor;
		IDocumentProvider provider = textEditor.getDocumentProvider();
		IDocument document = provider.getDocument(textEditor.getEditorInput());
		TestEditorUtils edUtils = new TestEditorUtils(textEditor);

		TestUtils.delay(CPCTestPlugin.TEST_PRECHECK_DELAY);

		/*
		 * Initial file content:
		 * 
		 * 12345
		 */

		// create a copy of the data and replace the source
		edUtils.copyAction(1, 3); //234
		edUtils.pasteAction(1, 3); //replaces 234 with 234

		/*
		 * 12345
		 */

		TestUtils.delay(CPCTestPlugin.TEST_PRECHECK_DELAY);

		ICloneFile cloneFile = storeProvider.lookupCloneFileByPath(PROJECT_NAME, FILE, true, true);
		assertNotNull("clone file not found", cloneFile);

		//there should be no real clones and exactly one transient clone
		List<IClone> clones = storeProvider.getClonesByFile(cloneFile.getUuid());
		assertTrue("clone list should contain exactly one transient clone and no real clones", clones.size() == 1
				&& clones.get(0).isTransient());

		// copy again and replace only part of the beginning source
		edUtils.copyAction(1, 3); //234
		edUtils.pasteAction(0, 2); //replaces 12 with 234

		/*
		 * 234345
		 */

		TestUtils.delay(CPCTestPlugin.TEST_PRECHECK_DELAY);

		//there should be no real clones and exactly one transient clone
		clones = storeProvider.getClonesByFile(cloneFile.getUuid());
		//this check is now outdated
		//		assertTrue("clone list should contain exactly one transient clone and no real clones - clones: " + clones,
		//				clones.size() == 1 && clones.get(0).isTransient());

		// copy again and replace only part of the middle source
		edUtils.copyAction(1, 4); //3434
		edUtils.pasteAction(3, 0); //insert after 234

		/*
		 * 2343434345
		 */

		TestUtils.delay(CPCTestPlugin.TEST_PRECHECK_DELAY);

		//there should be no real clones and exactly one transient clone
		clones = storeProvider.getClonesByFile(cloneFile.getUuid());
		//this check is now outdated
		//		assertTrue("clone list should contain exactly one transient clone and no real clones", clones.size() == 1
		//				&& clones.get(0).isTransient());

		//copy again and repalce only part of the end of the source
		edUtils.copyAction(1, 8); //34343434
		edUtils.pasteAction(7, 3); //replace last 345

		/*
		 * 234343434343434
		 */

		TestUtils.delay(CPCTestPlugin.TEST_PRECHECK_DELAY);

		//there should be no real clones and exactly one transient clone
		clones = storeProvider.getClonesByFile(cloneFile.getUuid());
		//this check is now outdated
		//		assertTrue("clone list should contain exactly one transient clone and no real clones", clones.size() == 1
		//				&& clones.get(0).isTransient());

		//now delete all and check if the position of a transient clone is correctly updated
		document.set("abc\nABC\n123\n");
		edUtils.copyAction(4, 4); //ABC\n
		document.replace(0, 0, "987\n"); //add at beginning of file
		edUtils.pasteAction(16, 0); //after the 123\n

		/*
		 * 987
		 * abc
		 * ABC
		 * 123
		 * ABC
		 */

		/*
		 * now check the generated clone data 
		 */

		//expected positions
		int[] startPos = new int[] { 8, 16 };
		int[] lengths = new int[] { 4, 4 };
		TestCloneUtils.clonePosCheck(storeProvider, cloneFile, startPos, lengths);

		/*
		 * Ok, we're done, wait a bit to let the user see the result in the clone view.
		 */
		TestUtils.delay(2000);

	}

	/**
	 * Simulates a copy action, then removes the source then simulates a paste action. (former bug)
	 */
	@Test
	public void testCPCloneCreationWithSourceDeletion() throws StoreLockingException, PartInitException,
			JavaModelException, BadLocationException
	{
		log.info("testCPCloneCreationWithSourceDeletion()");

		final String FILE = "src/test/Class3.java";

		//open file in editor
		IFile file = TestUtils.getFile(PROJECT_NAME, FILE);
		IEditorPart editor = TestEditorUtils.openEditor(file);

		assertTrue("text editor expected", editor instanceof ITextEditor);

		ITextEditor textEditor = (ITextEditor) editor;
		IDocumentProvider provider = textEditor.getDocumentProvider();
		IDocument document = provider.getDocument(textEditor.getEditorInput());
		TestEditorUtils edUtils = new TestEditorUtils(textEditor);

		TestUtils.delay(CPCTestPlugin.TEST_PRECHECK_DELAY);

		/*
		 * Initial file content:
		 * 
		 * 12345
		 */

		// create a copy
		edUtils.copyAction(0, 3); //123

		// delete the source
		document.replace(0, 4, "ABCD"); //12345 -> ABCD5

		//paste at end of file
		edUtils.pasteAction(5, 0);

		/*
		 * ABCD5123
		 */

		TestUtils.delay(CPCTestPlugin.TEST_PRECHECK_DELAY);

		/*
		 * now check the generated clone data 
		 */

		ICloneFile cloneFile = storeProvider.lookupCloneFileByPath(PROJECT_NAME, FILE, true, true);
		assertNotNull("clone file not found", cloneFile);

		//expected positions
		int[] startPos = new int[] { 5 };
		int[] lengths = new int[] { 3 };
		TestCloneUtils.clonePosCheck(storeProvider, cloneFile, startPos, lengths);

		/*
		 * Ok, we're done, wait a bit to let the user see the result in the clone view.
		 */
		TestUtils.delay(2000);

	}

	/**
	 * Simulates a copy action and paste action which lead to a source-reformat-on-paste situation. (former bug)
	 */
	@Test
	public void testCPCloneCreationWithSourceReformatOnPaste() throws StoreLockingException, PartInitException,
			JavaModelException, BadLocationException
	{
		log.info("testCPCloneCreationWithSourceReformatOnPaste()");

		final String FILE = "src/test/Class1.java";

		//open file in editor
		IFile file = TestUtils.getFile(PROJECT_NAME, FILE);
		IEditorPart editor = TestEditorUtils.openEditor(file);

		assertTrue("text editor expected", editor instanceof ITextEditor);

		ITextEditor textEditor = (ITextEditor) editor;
		//IDocumentProvider provider = textEditor.getDocumentProvider();
		//IDocument document = provider.getDocument(textEditor.getEditorInput());
		TestEditorUtils edUtils = new TestEditorUtils(textEditor);

		TestUtils.delay(CPCTestPlugin.TEST_PRECHECK_DELAY);

		/*
		 * Initial file content:
		 * 
		 * package test;
		 * 
		 * public class Class1
		 * {
		 * 		public static void main (String[] args)
		 *		{
		 *			System.out.println("Hello World!");
		 *		}
		 * }
		 */

		// create a copy
		edUtils.copyAction(83, 36); //System.out.println("Hello World!");\n

		//paste at the beginning of the next line
		edUtils.pasteAction(119, 0);

		/*
		 * package test;
		 * 
		 * public class Class1
		 * {
		 * 		public static void main (String[] args)
		 *		{
		 *			System.out.println("Hello World!");
		 *System.out.println("Hello World!");
		 *		}
		 * }
		 * 
		 * BUT WITH AUTO REFORMAT:
		 * package test;
		 * 
		 * public class Class1
		 * {
		 * 		public static void main (String[] args)
		 *		{
		 *			System.out.println("Hello World!");
		 *			System.out.println("Hello World!");
		 *		}
		 * }
		 * 
		 */

		TestUtils.delay(CPCTestPlugin.TEST_PRECHECK_DELAY);

		/*
		 * now check the generated clone data 
		 */

		ICloneFile cloneFile = storeProvider.lookupCloneFileByPath(PROJECT_NAME, FILE, true, true);
		assertNotNull("clone file not found", cloneFile);

		//expected positions
		int[] startPos = new int[] { 83, 119 };
		int[] lengths = new int[] { 36, 38 };
		TestCloneUtils.clonePosCheck(storeProvider, cloneFile, startPos, lengths);

		/*
		 * Ok, we're done, wait a bit to let the user see the result in the clone view.
		 */
		TestUtils.delay(2000);

	}

	/**
	 * Reformats the source code in an open editor.
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testCodeReformatInOpenEditor() throws Exception
	{
		log.debug("testCodeReformatInOpenEditor()");

		/*
		 * Open Editor
		 */
		log.debug("testCodeReformatInOpenEditor() - open editor");

		final String FILE = "src/test/SourceReformat.java";

		//open file in editor
		IFile file = TestUtils.getFile(PROJECT_NAME, FILE);
		IEditorPart editor = TestEditorUtils.openEditor(file);

		assertTrue("text editor expected", editor instanceof ITextEditor);

		ITextEditor textEditor = (ITextEditor) editor;
		IDocumentProvider provider = textEditor.getDocumentProvider();
		IDocument document = provider.getDocument(textEditor.getEditorInput());
		TestEditorUtils edUtils = new TestEditorUtils(textEditor);

		TestUtils.delay(CPCTestPlugin.TEST_PRECHECK_DELAY);

		/*
		 * Create some clones
		 */
		log.debug("testCodeReformatInOpenEditor() - create clones");

		edUtils.copyAction(49, 119); //entire main method
		edUtils.pasteAction(169, 0); //below main method, within class

		document.replace(72, 0, "2"); //rename first main method to "main2"

		edUtils.copyAction(113, 50); //"System .out   .println(       "Hello World!"    );"
		edUtils.pasteAction(164, 0); //start of next line

		TestUtils.delay(CPCTestPlugin.TEST_PRECHECK_DELAY);

		/*
		 * now check the generated clone data 
		 */
		log.debug("testCodeReformatInOpenEditor() - evaluate - PRE");

		ICloneFile cloneFile = storeProvider.lookupCloneFileByPath(PROJECT_NAME, FILE, true, true);
		assertNotNull("clone file not found", cloneFile);

		//expected positions
		int[] startPos = new int[] { 49, 113, 164, 222 };
		int[] lengths = new int[] { 172, 50, 52, 119 };
		TestCloneUtils.clonePosCheck(storeProvider, cloneFile, startPos, lengths);

		/*
		 * Source Reformat
		 */
		log.debug("testCodeReformatInOpenEditor() - start source reformat");

		TestJavaUtils.reformatSource(document);

		TestUtils.delay(CPCTestPlugin.TEST_PRECHECK_DELAY);

		/*
		 * now check the generated clone data 
		 */
		log.debug("testCodeReformatInOpenEditor() - evaluate - POST");

		//expected positions
		startPos = new int[] { 47, 93, 129, 172 };
		lengths = new int[] { 123, 35, 37, 84 };
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
	 * This test is almost equal to: {@link SimpleTrackTest#testCodeReformatInOpenEditor()} 
	 */
	@Test
	public void testSourceReformatOnClosedFile() throws Exception
	{
		log.debug("testSourceReformatOnClosedFile()");

		/*
		 * Open Editor
		 */
		log.debug("testSourceReformatOnClosedFile() - open editor");

		final String FILE = "src/test/SourceReformat.java";

		//open file in editor
		IFile file = TestUtils.getFile(PROJECT_NAME, FILE);
		IEditorPart editor = TestEditorUtils.openEditor(file);

		assertTrue("text editor expected", editor instanceof ITextEditor);

		ITextEditor textEditor = (ITextEditor) editor;
		IDocumentProvider provider = textEditor.getDocumentProvider();
		IDocument document = provider.getDocument(textEditor.getEditorInput());
		TestEditorUtils edUtils = new TestEditorUtils(textEditor);

		TestUtils.delay(CPCTestPlugin.TEST_PRECHECK_DELAY);

		/*
		 * Create some clones
		 */
		log.debug("testSourceReformatOnClosedFile() - create clones");

		edUtils.copyAction(49, 119); //entire main method
		edUtils.pasteAction(169, 0); //below main method, within class

		document.replace(72, 0, "2"); //rename first main method to "main2"

		edUtils.copyAction(113, 50); //"System .out   .println(       "Hello World!"    );"
		edUtils.pasteAction(164, 0); //start of next line

		TestUtils.delay(1000);

		/*
		 * now check the generated clone data 
		 */
		log.debug("testSourceReformatOnClosedFile() - evaluate - PRE");

		ICloneFile cloneFile = storeProvider.lookupCloneFileByPath(PROJECT_NAME, FILE, true, true);
		assertNotNull("clone file not found", cloneFile);

		//expected positions
		int[] startPos = new int[] { 49, 113, 164, 222 };
		int[] lengths = new int[] { 172, 50, 52, 119 };
		TestCloneUtils.clonePosCheck(storeProvider, cloneFile, startPos, lengths);

		/*
		 * Save file and close editor
		 */
		log.debug("testSourceReformatOnClosedFile() - save file and close editor");

		//save file
		textEditor.doSave(null);

		//close editor
		textEditor.close(false);

		TestUtils.delay(1000);

		/*
		 * Source Reformat
		 */
		log.debug("testSourceReformatOnClosedFile() - start source reformat");

		// get the buffer manager
		IPath filePath = file.getFullPath();
		ITextFileBufferManager bufferManager = FileBuffers.getTextFileBufferManager();
		try
		{
			bufferManager.connect(filePath, LocationKind.IFILE, null);
			// retrieve the buffer
			ITextFileBuffer textFileBuffer = bufferManager.getTextFileBuffer(filePath, LocationKind.IFILE);

			//get the document
			IDocument newDocument = textFileBuffer.getDocument();

			//now reformat the source
			TestJavaUtils.reformatSource(newDocument);

			log.debug("testSourceReformatOnClosedFile() - committing source reformat");

			// commit changes to underlying file
			textFileBuffer.commit(null /* ProgressMonitor */, false /* Overwrite */);

		}
		finally
		{
			log.debug("testSourceReformatOnClosedFile() - disconnecting buffer");
			bufferManager.disconnect(filePath, LocationKind.IFILE, null);
		}

		log.debug("testSourceReformatOnClosedFile() - source reformat done");

		TestUtils.delay(3000);

		//String newSource = CoreUtils.readFileContent(file);
		//log.trace("NEW CONTENT ON DISK: " + newSource);

		/*
		 * Reopen editor.
		 */
		editor = TestEditorUtils.openEditor(file);
		assertTrue("text editor expected", editor instanceof ITextEditor);

		TestUtils.delay(1000);

		/*
		 * now check the generated clone data 
		 */
		log.debug("testCodeReformatInOpenEditor() - evaluate - POST");

		//expected positions
		//		//old ext. reconciler whitespace-only-modification positions
		//		startPos = new int[] { 47, 93, 131, 172 };
		//		lengths = new int[] { 122, 34, 34, 84 };
		//new Position based Tracking positions
		startPos = new int[] { 47, 93, 129, 172 };
		lengths = new int[] { 123, 35, 37, 84 };
		TestCloneUtils.clonePosCheck(storeProvider, cloneFile, startPos, lengths);

		/*
		 * Ok, we're done, wait a bit to let the user see the result in the clone view.
		 */
		TestUtils.delay(2000);
	}

	/**
	 * Refactors the source code in an open editor.
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testRefactorInOpenEditor() throws Exception
	{
		log.debug("testRefactorInOpenEditor()");

		/*
		 * Open Editor
		 */
		log.debug("testRefactorInOpenEditor() - open editor");

		final String PACKAGE = "test";
		final String CLASS = "Refactor";
		final String FILE = "src/test/" + CLASS + ".java";
		final String OLD_FIELD = "xyz";
		final String NEW_FIELD = "x_y_z";

		//open file in editor
		IFile file = TestUtils.getFile(PROJECT_NAME, FILE);
		IEditorPart editor = TestEditorUtils.openEditor(file);

		assertTrue("text editor expected", editor instanceof ITextEditor);

		ITextEditor textEditor = (ITextEditor) editor;
		IDocumentProvider provider = textEditor.getDocumentProvider();
		IDocument document = provider.getDocument(textEditor.getEditorInput());
		TestEditorUtils edUtils = new TestEditorUtils(textEditor);

		TestUtils.delay(CPCTestPlugin.TEST_PRECHECK_DELAY);

		/*
		 * Create some clones
		 */
		log.debug("testCodeReformatInOpenEditor() - create clones");

		edUtils.copyAction(154, 68); //entire bla() method
		edUtils.pasteAction(274, 0); //below blubb() method, within class

		document.replace(178, 0, "2"); //rename first bla() method to bla2()

		edUtils.copyAction(187, 33); //System.out.println("xyz: "+xyz); in first bla (now bla2)
		edUtils.pasteAction(220, 0); //begin of next line

		TestUtils.delay(CPCTestPlugin.TEST_PRECHECK_DELAY);

		/*
		 * Check the generated clone data 
		 */
		log.debug("testCodeReformatInOpenEditor() - evaluate - PRE");

		ICloneFile cloneFile = storeProvider.lookupCloneFileByPath(PROJECT_NAME, FILE, true, true);
		assertNotNull("clone file not found", cloneFile);

		//expected positions
		int[] startPos = new int[] { 154, 187, 220, 310 };
		int[] lengths = new int[] { 104, 33, 35, 68 };
		TestCloneUtils.clonePosCheck(storeProvider, cloneFile, startPos, lengths);

		/*
		 * Refactor
		 */
		log.debug("testCodeReformatInOpenEditor() - start source reformat");

		TestJavaUtils.refactorRenameField(PROJECT_NAME, PACKAGE, CLASS, OLD_FIELD, NEW_FIELD);

		TestUtils.delay(CPCTestPlugin.TEST_PRECHECK_DELAY);

		/*
		 * Check the generated clone data 
		 */
		log.debug("testCodeReformatInOpenEditor() - evaluate - POST");

		//expected positions
		startPos = new int[] { 156, 189, 224, 318 };
		lengths = new int[] { 108, 35, 37, 70 };
		TestCloneUtils.clonePosCheck(storeProvider, cloneFile, startPos, lengths);

		/*
		 * Ok, we're done, wait a bit to let the user see the result in the clone view.
		 */
		TestUtils.delay(2000);

	}
}

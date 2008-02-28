package org.electrocodeogram.cpc.test.tests;


import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
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
import org.electrocodeogram.cpc.core.api.data.CloneDiff;
import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.data.ICloneFile;
import org.electrocodeogram.cpc.core.api.data.extension.ICloneModificationHistoryExtension;
import org.electrocodeogram.cpc.core.api.data.special.ICloneObjectExtensionMultiStatefulObject;
import org.electrocodeogram.cpc.core.api.provider.data.ICloneFactoryProvider;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider;
import org.electrocodeogram.cpc.core.api.provider.store.StoreLockingException;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider.LockMode;
import org.electrocodeogram.cpc.core.utils.CoreHistoryUtils;
import org.electrocodeogram.cpc.core.utils.CoreUtils;
import org.electrocodeogram.cpc.test.CPCTestPlugin;
import org.electrocodeogram.cpc.test.utils.TestCloneUtils;
import org.electrocodeogram.cpc.test.utils.TestEditorUtils;
import org.electrocodeogram.cpc.test.utils.TestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class UtilTest
{
	private static Log log = LogFactory.getLog(UtilTest.class);

	private ICloneFactoryProvider cloneFactoryProvider;
	private IStoreProvider storeProvider;

	private static final String PROJECT_NAME = "UtilTest";

	/**
	 * Perform pre-test initialisation.
	 */
	@Before
	public void setUp() throws Exception
	{
		TestUtils.defaultSetUp(PROJECT_NAME, "project1", true, true);
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
	public void testCoreUtilsExtensionMerge() throws StoreLockingException
	{
		log.debug("testCoreUtilsExtensionMerge()");

		ICloneFactoryProvider cloneFactoryProvider = (ICloneFactoryProvider) CPCCorePlugin.getProviderRegistry()
				.lookupProvider(ICloneFactoryProvider.class);
		assert (cloneFactoryProvider != null);

		/*
		 * Create some extensions.
		 */
		ICloneModificationHistoryExtension baseExtension = (ICloneModificationHistoryExtension) cloneFactoryProvider
				.getInstance(ICloneModificationHistoryExtension.class);
		ICloneModificationHistoryExtension incrementalExtension = (ICloneModificationHistoryExtension) cloneFactoryProvider
				.getInstance(ICloneModificationHistoryExtension.class);

		/*
		 * Add some clone diff data.
		 */
		long creationTime = System.currentTimeMillis();

		CloneDiff diff1 = new CloneDiff("creator1", new Date(creationTime), false, 10, 10, "bla1");
		baseExtension.addCloneDiff(diff1);
		CloneDiff diff2 = new CloneDiff("creator2", new Date(creationTime + 60000), false, 20, 10, "bla2");
		baseExtension.addCloneDiff(diff2);
		CloneDiff diff3 = new CloneDiff("creator3", new Date(creationTime + 120000), false, 30, 10, "bla3");
		baseExtension.addCloneDiff(diff3);
		CloneDiff diff4 = new CloneDiff("creator4", new Date(creationTime + 180000), false, 40, 10, "bla4");
		baseExtension.addCloneDiff(diff4);

		//insert test
		CloneDiff diff3b = new CloneDiff("creator3b", new Date(creationTime + 150000), false, 35, 10, "bla3b");
		incrementalExtension.addCloneDiff(diff3b);

		//replace test
		CloneDiff diff4new = new CloneDiff("creator4new", new Date(creationTime + 180000), false, 40, 5, "bla4new");
		incrementalExtension.addCloneDiff(diff4new);

		//addition test
		CloneDiff diff5 = new CloneDiff("creator5", new Date(creationTime + 200000), false, 50, 15, "bla5");
		incrementalExtension.addCloneDiff(diff5);

		//the incremental extension needs to be part of an ICloneObject for the merge function to work
		//as expected (this is also how the method will be used in AbstractStoreProvider).
		IClone clone = (IClone) cloneFactoryProvider.getInstance(IClone.class);
		clone.addExtension(incrementalExtension);

		/*
		 * Now merge the extensions.
		 */
		ICloneObjectExtensionMultiStatefulObject result = CoreUtils.mergeMultiExtensions(cloneFactoryProvider,
				(ICloneObjectExtensionMultiStatefulObject) baseExtension,
				(ICloneObjectExtensionMultiStatefulObject) incrementalExtension);
		assert (result != null && result instanceof ICloneModificationHistoryExtension);

		/*
		 * Check the result.
		 */
		ICloneModificationHistoryExtension resultHist = (ICloneModificationHistoryExtension) result;

		assertTrue("wrong size of merged extension - got: " + resultHist.getCloneDiffs().size() + ", expected: 6",
				resultHist.getCloneDiffs().size() == 6);

		//make sure all diffs are correctly added
		assertTrue("diff1 missmatch", resultHist.getCloneDiffs().get(0).equals(diff1));
		assertTrue("diff2 missmatch", resultHist.getCloneDiffs().get(1).equals(diff2));
		assertTrue("diff3 missmatch", resultHist.getCloneDiffs().get(2).equals(diff3));

		assertTrue("diff3b missmatch", resultHist.getCloneDiffs().get(3).equals(diff3b));
		assertTrue("diff4new missmatch", resultHist.getCloneDiffs().get(4).equals(diff4new));
		assertTrue("diff4new not updated", resultHist.getCloneDiffs().get(4).getCreator().equals("creator4new"));
		assertTrue("diff5 missmatch", resultHist.getCloneDiffs().get(5).equals(diff5));

	}

	/**
	 * Checks whether the clone modification history is working correctly.
	 * Creates some clones, does some modifications and then evaluates the old clone contents
	 * returned by the clone modification history. 
	 */
	@Test
	public void testCloneModificationHistoryContentByDate() throws Exception
	{
		/*
		 * This test case is very similar to:
		 * org.electrocodeogram.cpc.test.tests.SimpleTrackTest.testCPCloneCreationAndTracking()
		 */
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

		ICloneFile cloneFile = storeProvider.lookupCloneFileByPath(PROJECT_NAME, FILE, true, true);
		assertNotNull("clone file not found", cloneFile);

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

		TestUtils.delay(CPCTestPlugin.TEST_PRECHECK_DELAY);

		/*
		 * Resulting file content:
		 * 
		 * 12345
		 * 67890
		 * 12345
		 * 12345
		 */

		/*
		 * We'll need to be able to address the two clones later, get their UUIDs
		 */
		storeProvider.acquireWriteLock(LockMode.DEFAULT);
		List<IClone> clones = storeProvider.getClonesByFile(cloneFile.getUuid());
		storeProvider.releaseWriteLock();
		assertTrue("invalid clone list for file: " + clones, clones != null && clones.size() == 2);

		IClone clone1 = clones.get(0);
		//		IClone clone2 = clones.get(1);

		/*
		 * Remember content of all clones
		 */
		List<String> clone1Contents = new LinkedList<String>();
		String content = TestCloneUtils.getCloneContent(storeProvider, clone1.getUuid());
		assertTrue("invalid content for clone1: " + content, content != null && content.equals("12345"));

		//		List<String> clone2Contents = new LinkedList<String>();
		//		content = TestCloneUtils.getCloneContent(storeProvider, clone2.getUuid());
		//		assertTrue("invalid content for clone2: " + content, content != null && content.equals("12345"));

		/*
		 * Now we do some modifications to the document
		 */

		//insert something within first clone
		document.replace(1, 0, "22");

		TestUtils.delay(CPCTestPlugin.TEST_PRECHECK_DELAY);

		/*
		 * 1222345
		 * 67890
		 * 12345
		 * 12345
		 */

		content = TestCloneUtils.getCloneContent(storeProvider, clone1.getUuid());
		assertTrue("invalid content for clone1: " + content, content != null && content.equals("1222345"));
		clone1Contents.add(content);

		//replace something within first clone
		document.replace(4, 2, "433");

		TestUtils.delay(CPCTestPlugin.TEST_PRECHECK_DELAY);

		/*
		 * 12224335
		 * 67890
		 * 1234567890
		 * 12345
		 */

		content = TestCloneUtils.getCloneContent(storeProvider, clone1.getUuid());
		assertTrue("invalid content for clone1: " + content, content != null && content.equals("12224335"));
		clone1Contents.add(content);

		//modification which overlaps with end of clone1
		document.replace(6, 5, "abc");

		TestUtils.delay(CPCTestPlugin.TEST_PRECHECK_DELAY);

		/*
		 * 122243abc890
		 * 1234567890
		 * 12345
		 */

		content = TestCloneUtils.getCloneContent(storeProvider, clone1.getUuid());
		assertTrue("invalid content for clone1: " + content, content != null && content.equals("122243"));
		clone1Contents.add(content);

		//move everything down a bit
		document.replace(0, 0, "abcde\n");

		TestUtils.delay(CPCTestPlugin.TEST_PRECHECK_DELAY);

		/*
		 * abcde
		 * 122243abc890
		 * 1234567890
		 * 12345
		 */

		content = TestCloneUtils.getCloneContent(storeProvider, clone1.getUuid());
		assertTrue("invalid content for clone1: " + content, content != null && content.equals("122243"));
		//clone1Contents.add(content);

		//modification which overlaps with the begining of clone1
		document.replace(3, 7, "xyz");

		TestUtils.delay(CPCTestPlugin.TEST_PRECHECK_DELAY);

		/*
		 * abcxyz43abc890
		 * 1234567890
		 * 12345
		 */

		content = TestCloneUtils.getCloneContent(storeProvider, clone1.getUuid());
		assertTrue("invalid content for clone1: " + content, content != null && content.equals("43"));
		clone1Contents.add(content);

		/*
		 * No check history of clone1.
		 */
		storeProvider.acquireWriteLock(LockMode.DEFAULT);

		IClone clone1new = storeProvider.lookupClone(clone1.getUuid());
		ICloneModificationHistoryExtension history = (ICloneModificationHistoryExtension) storeProvider
				.getFullCloneObjectExtension(clone1new, ICloneModificationHistoryExtension.class);
		assertTrue("wrong size of history: " + history, history != null
				&& history.getCloneDiffs().size() == clone1Contents.size());

		//ensure that we receive the original clone content if date is older than creation date of the clone
		content = CoreHistoryUtils.getCloneContentForDate(storeProvider, clone1new, new Date(clone1new
				.getCreationDate().getTime() - 1), true);
		assertTrue("original content not returned for old date (1)", clone1new.getOriginalContent().equals(content));

		//ensure that we receive the original clone content if date is older than oldest diff
		CloneDiff firstDiff = history.getCloneDiffs().get(0);
		content = CoreHistoryUtils.getCloneContentForDate(storeProvider, clone1new, new Date(firstDiff
				.getCreationDate().getTime() - 1), true);
		assertTrue("original content not returned for old date (2)", clone1new.getOriginalContent().equals(content));

		//now check the content for each diff
		int i = 0;
		for (CloneDiff diff : history.getCloneDiffs())
		{
			log.trace("DIFF: " + diff);
			//get content after this diff
			content = CoreHistoryUtils.getCloneContentForDate(storeProvider, clone1new, diff.getCreationDate(), true);
			assertTrue("history content does not match actual content - diff: " + diff + ", history: " + content
					+ ", expected: " + clone1Contents.get(i), clone1Contents.get(i).equals(content));
			++i;
		}

		storeProvider.releaseWriteLock();
	}
}

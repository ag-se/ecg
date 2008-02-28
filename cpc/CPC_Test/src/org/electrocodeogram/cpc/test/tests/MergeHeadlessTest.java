package org.electrocodeogram.cpc.test.tests;


import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.electrocodeogram.cpc.core.CPCCorePlugin;
import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.data.ICloneFile;
import org.electrocodeogram.cpc.core.api.data.ICloneGroup;
import org.electrocodeogram.cpc.core.api.data.special.ICreatorClone;
import org.electrocodeogram.cpc.core.api.data.special.ICreatorCloneFile;
import org.electrocodeogram.cpc.core.api.provider.data.ICloneFactoryProvider;
import org.electrocodeogram.cpc.core.api.provider.merge.IMergeProvider;
import org.electrocodeogram.cpc.core.api.provider.merge.IMergeResult;
import org.electrocodeogram.cpc.core.api.provider.merge.IMergeResultPerspective;
import org.electrocodeogram.cpc.core.api.provider.merge.IMergeTask;
import org.electrocodeogram.cpc.core.api.provider.merge.MergeException;
import org.electrocodeogram.cpc.core.api.provider.store.StoreLockingException;
import org.electrocodeogram.cpc.core.utils.CoreClonePositionUtils;
import org.electrocodeogram.cpc.core.utils.CoreUtils;
import org.electrocodeogram.cpc.test.utils.TestCloneUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class MergeHeadlessTest
{
	private static Log log = LogFactory.getLog(MergeHeadlessTest.class);

	private ICloneFactoryProvider cloneFactoryProvider;
	private IMergeProvider mergeProvider;

	public MergeHeadlessTest()
	{
		cloneFactoryProvider = (ICloneFactoryProvider) CPCCorePlugin.getProviderRegistry().lookupProvider(
				ICloneFactoryProvider.class);
		assertNotNull("failed to obtain clone factory provider", cloneFactoryProvider);

		mergeProvider = (IMergeProvider) CPCCorePlugin.getProviderRegistry().lookupProvider(IMergeProvider.class);
		assertNotNull("failed to obtain merge provider", mergeProvider);
		assertTrue(true);
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
	 * Simple update without conflicts.
	 */
	@Test
	public void testSimpleUpdate() throws StoreLockingException
	{
		log.debug("testSimpleUpdate() - creating clones.");

		String baseSource = "123\n ABC\n123\nABC\n";
		String localSource = "123\n ABC\n123\nABC\n";
		String remoteSource = "123\nABC\n124\n123\n";
		String mergedSource = "123\nABC\n124\n123\n";

		IDocument baseDoc = new Document(baseSource);
		//IDocument localDoc = new Document(localSource);
		IDocument remoteDoc = new Document(remoteSource);
		//IDocument mergedDoc = new Document(mergedSource);

		ICreatorCloneFile file = (ICreatorCloneFile) cloneFactoryProvider.getInstance(ICloneFile.class);
		file.setProject("project");
		file.setPath("path");

		ICloneGroup groupA = (ICloneGroup) cloneFactoryProvider.getInstance(ICloneGroup.class);
		ICloneGroup groupB = (ICloneGroup) cloneFactoryProvider.getInstance(ICloneGroup.class);

		/*
		 * Create base/local clones.
		 */
		IClone clone123a = TestCloneUtils.createTestClone(cloneFactoryProvider, file, groupA, 0, 2, baseDoc); //123 (1st)
		IClone clone123b = TestCloneUtils.createTestClone(cloneFactoryProvider, file, groupA, 9, 11, baseDoc); //123 (2nd)

		IClone cloneABCa = TestCloneUtils.createTestClone(cloneFactoryProvider, file, groupB, 5, 7, baseDoc); //ABC (1st)
		IClone cloneABCb = TestCloneUtils.createTestClone(cloneFactoryProvider, file, groupB, 13, 15, baseDoc); //ABC (2nd)

		List<IClone> baseClones = CoreUtils.cloneCloneList(Arrays.asList(clone123a, cloneABCa, clone123b, cloneABCb),
				false);
		List<IClone> localClones = CoreUtils.cloneCloneList(baseClones, false);

		/*
		 * Create remote clones.
		 */

		//move clone ABCa
		cloneABCa.setOffset(4);
		//modify clone 123b
		((ICreatorClone) clone123b).setContent("124");
		//add clone 123c
		IClone clone123c = TestCloneUtils.createTestClone(cloneFactoryProvider, file, groupA, 12, 14, remoteDoc); //123 (3nd)
		//ABCb was removed
		List<IClone> remoteClones = Arrays.asList(clone123a, cloneABCa, clone123b, clone123c);

		/*
		 * cloneABCb is removed @ remote
		 * clone123b is modified @ remote
		 * clone123c is added @ remote
		 */

		/*
		 * Now execute merge.
		 */
		log.debug("testSimpleUpdate() - executing merge.");

		IMergeTask mergeTask = mergeProvider.createTask();

		mergeTask.setBaseCloneFile(file);
		mergeTask.setBaseClones(baseClones);
		mergeTask.setBaseSourceFileContent(baseSource);
		mergeTask.setLocalCloneFile(file);
		mergeTask.setLocalClones(localClones);
		mergeTask.setLocalSourceFileContent(localSource);
		mergeTask.setRemoteCloneFile(file);
		mergeTask.setRemoteClones(remoteClones);
		mergeTask.setRemoteSourceFileContent(remoteSource);
		mergeTask.setMergedSourceFileContent(mergedSource);

		assertTrue("failed to create valid merge task", mergeTask.isValid());

		IMergeResult mergeResult = null;
		try
		{
			mergeResult = mergeProvider.merge(mergeTask);
		}
		catch (IllegalArgumentException e)
		{
			assertTrue(false);
		}
		catch (MergeException e)
		{
			e.printStackTrace();
			assertTrue("merge failed - " + e, false);
		}

		/*
		 * Now verify the result.
		 */
		log.debug("testSimpleUpdate() - checking merge result.");

		//check overall merge result
		List<IClone> mergedClones = new ArrayList<IClone>(mergeResult.getMergedClones());
		Collections.sort(mergedClones);
		assertTrue("merge result does not equal remote clone data - remoteClones: " + remoteClones + ", mergedClones: "
				+ mergedClones, CoreClonePositionUtils.cloneListsEqual(remoteClones, mergedClones));

		//check remote perspective
		assertTrue("added clones list of remote perspective is not empty", mergeResult.getRemotePerspective()
				.getAddedClones().isEmpty());
		assertTrue("lost clones list of remote perspective is not empty", mergeResult.getRemotePerspective()
				.getLostClones().isEmpty());
		assertTrue("removed clones list of remote perspective is not empty", mergeResult.getRemotePerspective()
				.getRemovedClones().isEmpty());
		assertTrue("modified clones list of remote perspective is not empty", mergeResult.getRemotePerspective()
				.getModifiedClones().isEmpty());
		assertTrue("moved clones list of remote perspective is not empty", mergeResult.getRemotePerspective()
				.getMovedClones().isEmpty());
		List<IClone> remoteUnchangedClones = new ArrayList<IClone>(mergeResult.getRemotePerspective()
				.getUnchangedClones());
		Collections.sort(remoteUnchangedClones);
		assertTrue("unchanged clones list of remote perspective does not equal merged clone list - unchanged clones: "
				+ remoteUnchangedClones + ", mergedClones: " + mergedClones, CoreClonePositionUtils.cloneListsEqual(
				remoteUnchangedClones, mergedClones));

		//check local perspective
		IMergeResultPerspective localPers = mergeResult.getLocalPerspective();
		assertTrue("added clones list of local perspective is incorrect", localPers.getAddedClones().size() == 1
				&& localPers.getAddedClones().contains(clone123c));
		assertTrue("removed clones list of local perspective is incorrect", localPers.getRemovedClones().size() == 1
				&& localPers.getRemovedClones().contains(cloneABCb));
		assertTrue("lost clones list of local perspective is not empty", localPers.getLostClones().isEmpty());
		assertTrue("modified clones list of local perspective is incorrect", localPers.getModifiedClones().size() == 1
				&& localPers.getModifiedClones().contains(clone123b));
		//TODO: we could be stricter here some day, for now we allow the modified clone to appear in the moved
		//		clones list too.
		//		assertTrue("moved clones list of local perspective is incorrect", localPers.getMovedClones().size() == 1
		//				&& localPers.getMovedClones().contains(cloneABCa));
		assertTrue(
				"moved clones list of local perspective is incorrect",
				(localPers.getMovedClones().size() == 1 && localPers.getMovedClones().contains(cloneABCa))
						|| (localPers.getMovedClones().size() == 2 && localPers.getMovedClones().contains(cloneABCa) && localPers
								.getMovedClones().contains(clone123b)));
		assertTrue("unchanged clones list of local perspective is incorrect",
				localPers.getUnchangedClones().size() == 1 && localPers.getUnchangedClones().contains(clone123a));

		log.debug("testSimpleUpdate() - done.");
	}

	/**
	 * Simple update with local and remote deletions.
	 */
	@Test
	public void testSimpleDeletions() throws StoreLockingException
	{
		log.debug("testSimpleDeletions() - creating clones.");

		String baseSource = "123\n ABC\n123\nABC\n";
		String localSource = "     ABC\n123\nABC\n";
		String remoteSource = "123\n ABC\n123\n";
		String mergedSource = "     ABC\n123\n";

		IDocument baseDoc = new Document(baseSource);
		//IDocument localDoc = new Document(localSource);
		//IDocument remoteDoc = new Document(remoteSource);
		//IDocument mergedDoc = new Document(mergedSource);

		ICreatorCloneFile file = (ICreatorCloneFile) cloneFactoryProvider.getInstance(ICloneFile.class);
		file.setProject("project");
		file.setPath("path");

		ICloneGroup groupA = (ICloneGroup) cloneFactoryProvider.getInstance(ICloneGroup.class);
		ICloneGroup groupB = (ICloneGroup) cloneFactoryProvider.getInstance(ICloneGroup.class);

		/*
		 * Create base clones.
		 */
		IClone clone123a = TestCloneUtils.createTestClone(cloneFactoryProvider, file, groupA, 0, 2, baseDoc); //123 (1st)
		IClone clone123b = TestCloneUtils.createTestClone(cloneFactoryProvider, file, groupA, 9, 11, baseDoc); //123 (2nd)

		IClone cloneABCa = TestCloneUtils.createTestClone(cloneFactoryProvider, file, groupB, 5, 7, baseDoc); //ABC (1st)
		IClone cloneABCb = TestCloneUtils.createTestClone(cloneFactoryProvider, file, groupB, 13, 15, baseDoc); //ABC (2nd)

		List<IClone> baseClones = CoreUtils.cloneCloneList(Arrays.asList(clone123a, cloneABCa, clone123b, cloneABCb),
				false);

		/*
		 * Create local clones.
		 */

		List<IClone> localClones = CoreUtils.cloneCloneList(Arrays.asList(cloneABCa, clone123b, cloneABCb), false);
		//		IClone cloneABCaLoc = localClones.get(0);
		//		cloneABCaLoc.setOffset(0);
		//		IClone clone123bLoc = localClones.get(1);
		//		clone123bLoc.setOffset(4);
		//		IClone cloneABCbLoc = localClones.get(2);
		//		cloneABCbLoc.setOffset(8);

		/*
		 * Create remote clones.
		 */

		List<IClone> remoteClones = Arrays.asList(clone123a, cloneABCa, clone123b);

		/*
		 * cloneABCb is removed @ remote
		 * clone123b is modified @ remote
		 * clone123c is added @ remote
		 */

		/*
		 * Now execute merge.
		 */
		log.debug("testSimpleDeletions() - executing merge.");

		IMergeTask mergeTask = mergeProvider.createTask();

		mergeTask.setBaseCloneFile(file);
		mergeTask.setBaseClones(baseClones);
		mergeTask.setBaseSourceFileContent(baseSource);
		mergeTask.setLocalCloneFile(file);
		mergeTask.setLocalClones(localClones);
		mergeTask.setLocalSourceFileContent(localSource);
		mergeTask.setRemoteCloneFile(file);
		mergeTask.setRemoteClones(remoteClones);
		mergeTask.setRemoteSourceFileContent(remoteSource);
		mergeTask.setMergedSourceFileContent(mergedSource);

		assertTrue("failed to create valid merge task", mergeTask.isValid());

		IMergeResult mergeResult = null;
		try
		{
			mergeResult = mergeProvider.merge(mergeTask);
		}
		catch (IllegalArgumentException e)
		{
			assertTrue(false);
		}
		catch (MergeException e)
		{
			e.printStackTrace();
			assertTrue("merge failed - " + e, false);
		}

		/*
		 * Now verify the result.
		 */
		log.debug("testSimpleDeletions() - checking merge result.");

		//check overall merge result
		List<IClone> expectedResult = Arrays.asList(cloneABCa, clone123b);
		List<IClone> mergedClones = new ArrayList<IClone>(mergeResult.getMergedClones());
		Collections.sort(mergedClones);
		assertTrue("merge result does not equal expected data - expectedResult: " + expectedResult + ", mergedClones: "
				+ mergedClones, CoreClonePositionUtils.cloneListsEqual(expectedResult, mergedClones));

		//check remote perspective
		IMergeResultPerspective remotePers = mergeResult.getRemotePerspective();
		assertTrue("added clones list of remote perspective is not empty", remotePers.getAddedClones().isEmpty());
		assertTrue("lost clones list of remote perspective is not empty", remotePers.getLostClones().isEmpty());
		assertTrue("removed clones list of remote perspective is incorrect", remotePers.getRemovedClones().size() == 1
				&& remotePers.getRemovedClones().contains(clone123a));
		assertTrue("modified clones list of remote perspective is not empty", remotePers.getModifiedClones().isEmpty());
		assertTrue("moved clones list of remote perspective is not empty", remotePers.getMovedClones().isEmpty());
		assertTrue("unchanged clones list of remote perspective is incorrect",
				remotePers.getUnchangedClones().size() == 2 && remotePers.getUnchangedClones().contains(cloneABCa)
						&& remotePers.getUnchangedClones().contains(clone123b));

		//check local perspective
		IMergeResultPerspective localPers = mergeResult.getLocalPerspective();
		assertTrue("added clones list of local perspective is not empty", localPers.getAddedClones().isEmpty());
		assertTrue("removed clones list of local perspective is incorrect", localPers.getRemovedClones().size() == 1
				&& localPers.getRemovedClones().contains(cloneABCb));
		assertTrue("lost clones list of local perspective is not empty", localPers.getLostClones().isEmpty());
		assertTrue("modified clones list of local perspective is not empty", localPers.getModifiedClones().isEmpty());
		assertTrue("moved clones list of local perspective is not empty", localPers.getMovedClones().isEmpty());
		assertTrue("unchanged clones list of local perspective is incorrect",
				localPers.getUnchangedClones().size() == 2 && localPers.getUnchangedClones().contains(cloneABCa)
						&& localPers.getUnchangedClones().contains(clone123b));

		log.debug("testSimpleDeletions() - done.");
	}

	/**
	 * Simple update with local and remote additions.
	 */
	@Test
	public void testSimpleAdditions() throws StoreLockingException
	{
		log.debug("testSimpleDeletions() - creating clones.");

		String baseSource = "123\naaaa\nABC\nbbbb\n123\ncccc\nABC\nxxxx\n";
		String localSource = "123\naaaa\nABC\nbbbb\n123\ncccc\nABC\n123\nxxxx\n";
		String remoteSource = "123\naaaa\nABC\nbbbb\n123\ncccc\nABC\nxxxx\nABC\n";
		String mergedSource = "123\naaaa\nABC\nbbbb\n123\ncccc\nABC\n123\nxxxx\nABC\n";

		IDocument baseDoc = new Document(baseSource);
		IDocument localDoc = new Document(localSource);
		IDocument remoteDoc = new Document(remoteSource);
		//IDocument mergedDoc = new Document(mergedSource);

		ICreatorCloneFile file = (ICreatorCloneFile) cloneFactoryProvider.getInstance(ICloneFile.class);
		file.setProject("project");
		file.setPath("path");

		ICloneGroup groupA = (ICloneGroup) cloneFactoryProvider.getInstance(ICloneGroup.class);
		ICloneGroup groupB = (ICloneGroup) cloneFactoryProvider.getInstance(ICloneGroup.class);

		/*
		 * Create base clones.
		 */
		IClone clone123a = TestCloneUtils.createTestClone(cloneFactoryProvider, file, groupA, 0, 2, baseDoc); //123 (1st)
		IClone clone123b = TestCloneUtils.createTestClone(cloneFactoryProvider, file, groupA, 18, 20, baseDoc); //123 (2nd)

		IClone cloneABCa = TestCloneUtils.createTestClone(cloneFactoryProvider, file, groupB, 9, 11, baseDoc); //ABC (1st)
		IClone cloneABCb = TestCloneUtils.createTestClone(cloneFactoryProvider, file, groupB, 27, 29, baseDoc); //ABC (2nd)

		List<IClone> baseClones = CoreUtils.cloneCloneList(Arrays.asList(clone123a, cloneABCa, clone123b, cloneABCb),
				false);

		/*
		 * Create local clones.
		 */

		List<IClone> localClones = CoreUtils.cloneCloneList(baseClones, false);
		IClone clone123cLoc = TestCloneUtils.createTestClone(cloneFactoryProvider, file, groupA, 31, 33, localDoc); //123 (3rd)
		localClones.add(clone123cLoc);

		/*
		 * Create remote clones.
		 */

		List<IClone> remoteClones = CoreUtils.cloneCloneList(baseClones, false);
		IClone cloneABCcRem = TestCloneUtils.createTestClone(cloneFactoryProvider, file, groupA, 36, 38, remoteDoc); //ABC (3rd)
		remoteClones.add(cloneABCcRem);

		/*
		 * cloneABCb is removed @ remote
		 * clone123b is modified @ remote
		 * clone123c is added @ remote
		 */

		/*
		 * Now execute merge.
		 */
		log.debug("testSimpleDeletions() - executing merge.");

		IMergeTask mergeTask = mergeProvider.createTask();

		mergeTask.setBaseCloneFile(file);
		mergeTask.setBaseClones(baseClones);
		mergeTask.setBaseSourceFileContent(baseSource);
		mergeTask.setLocalCloneFile(file);
		mergeTask.setLocalClones(localClones);
		mergeTask.setLocalSourceFileContent(localSource);
		mergeTask.setRemoteCloneFile(file);
		mergeTask.setRemoteClones(remoteClones);
		mergeTask.setRemoteSourceFileContent(remoteSource);
		mergeTask.setMergedSourceFileContent(mergedSource);

		assertTrue("failed to create valid merge task", mergeTask.isValid());

		IMergeResult mergeResult = null;
		try
		{
			mergeResult = mergeProvider.merge(mergeTask);
		}
		catch (IllegalArgumentException e)
		{
			assertTrue(false);
		}
		catch (MergeException e)
		{
			e.printStackTrace();
			assertTrue("merge failed - " + e, false);
		}

		/*
		 * Now verify the result.
		 */
		log.debug("testSimpleDeletions() - checking merge result.");

		//check overall merge result
		List<IClone> expectedResult = new ArrayList<IClone>(baseClones.size() + 2);
		expectedResult.addAll(baseClones);
		expectedResult.add(clone123cLoc);
		cloneABCcRem.setOffset(40);
		expectedResult.add(cloneABCcRem);

		List<IClone> mergedClones = new ArrayList<IClone>(mergeResult.getMergedClones());
		Collections.sort(mergedClones);
		assertTrue("merge result does not equal expected data - expectedResult: " + expectedResult + ", mergedClones: "
				+ mergedClones, CoreClonePositionUtils.cloneListsEqual(expectedResult, mergedClones));

		//check remote perspective
		IMergeResultPerspective remotePers = mergeResult.getRemotePerspective();
		assertTrue("added clones list of remote perspective is incorrect", remotePers.getAddedClones().size() == 1
				&& remotePers.getAddedClones().contains(clone123cLoc));
		assertTrue("lost clones list of remote perspective is not empty", remotePers.getLostClones().isEmpty());
		assertTrue("removed clones list of remote perspective is not empty", remotePers.getRemovedClones().isEmpty());
		assertTrue("modified clones list of remote perspective is not empty", remotePers.getModifiedClones().isEmpty());
		assertTrue("moved clones list of remote perspective is invalid", remotePers.getMovedClones().size() == 1
				&& remotePers.getMovedClones().contains(cloneABCcRem));
		List<IClone> sortedUnchangedRem = new ArrayList<IClone>(remotePers.getUnchangedClones());
		Collections.sort(sortedUnchangedRem);
		assertTrue("unchanged clones list of remote perspective is incorrect", CoreClonePositionUtils.cloneListsEqual(
				sortedUnchangedRem, baseClones));

		//check local perspective
		IMergeResultPerspective localPers = mergeResult.getLocalPerspective();
		assertTrue("added clones list of local perspective is incorrect", localPers.getAddedClones().size() == 1
				&& localPers.getAddedClones().contains(cloneABCcRem));
		assertTrue("lost clones list of local perspective is not empty", localPers.getLostClones().isEmpty());
		assertTrue("removed clones list of local perspective is not empty", localPers.getRemovedClones().isEmpty());
		assertTrue("modified clones list of local perspective is not empty", localPers.getModifiedClones().isEmpty());
		assertTrue("moved clones list of local perspective is not empty", localPers.getMovedClones().isEmpty());
		List<IClone> sortedUnchangedLoc = new ArrayList<IClone>(localPers.getUnchangedClones());
		Collections.sort(sortedUnchangedLoc);
		List<IClone> unchangedExpectedLoc = new ArrayList<IClone>(baseClones.size());
		unchangedExpectedLoc.addAll(baseClones);
		unchangedExpectedLoc.add(clone123cLoc);
		assertTrue("unchanged clones list of local perspective is incorrect", CoreClonePositionUtils.cloneListsEqual(
				sortedUnchangedLoc, unchangedExpectedLoc));

		log.debug("testSimpleDeletions() - done.");
	}
}

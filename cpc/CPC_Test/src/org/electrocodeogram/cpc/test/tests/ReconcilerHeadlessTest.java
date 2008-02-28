package org.electrocodeogram.cpc.test.tests;


import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.electrocodeogram.cpc.core.CPCCorePlugin;
import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.data.ICloneFile;
import org.electrocodeogram.cpc.core.api.data.special.ICreatorClone;
import org.electrocodeogram.cpc.core.api.data.special.ICreatorCloneFile;
import org.electrocodeogram.cpc.core.api.provider.data.ICloneFactoryProvider;
import org.electrocodeogram.cpc.core.api.provider.reconciler.IReconcilerProvider;
import org.electrocodeogram.cpc.core.api.provider.reconciler.IReconciliationResult;
import org.electrocodeogram.cpc.core.api.provider.reconciler.IReconciliationResult.Status;
import org.electrocodeogram.cpc.core.utils.CoreStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class ReconcilerHeadlessTest
{
	private static Log log = LogFactory.getLog(ReconcilerHeadlessTest.class);

	private static final byte REC_MOVED = 1;
	private static final byte REC_MODIFIED = 2;
	private static final byte REC_REMOVED = 3;
	private static final byte REC_LOST = 4;
	private static final byte REC_NOCHANGE = 5;

	private ICloneFactoryProvider cloneFactoryProvider;

	public ReconcilerHeadlessTest()
	{
		cloneFactoryProvider = (ICloneFactoryProvider) CPCCorePlugin.getProviderRegistry().lookupProvider(
				ICloneFactoryProvider.class);
		assertNotNull("failed to obtain clone factory provider", cloneFactoryProvider);
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
	 * Makes direct use of an {@link IReconcilerProvider} to reconcile
	 * changes between some artificial documents. 
	 */
	@Test
	public void testHeadlessReconciliationWsOnly1() throws Exception
	{
		log.debug("testHeadlessReconciliationWsOnly1()");

		/*
		 * Setup fake data.
		 */
		String persistedFileContent = "\tABC\n\tDEF\n   GEH\n\tIJH\n";
		String newFileContent = "   \tABC\n\tDEF\n   GEH\n\tIJH\n";

		subTestStandaloneReconciliation(persistedFileContent, newFileContent, 13, 3, 16, 3, Status.FULL_RECONCILIATION,
				REC_MOVED);

		log.debug("testHeadlessReconciliationWsOnly1() - done.");
	}

	@Test
	public void testHeadlessReconciliationWsOnly2() throws Exception
	{
		log.debug("testHeadlessReconciliationWsOnly2()");

		/*
		 * Setup fake data.
		 */
		String persistedFileContent = "\tABC\n\tDEF\n   GEH\n\tIJH\n";
		String newFileContent = "   \tABC\n\tDEF\n   GEH\n\tIJH\n";

		subTestStandaloneReconciliation(persistedFileContent, newFileContent, 13, 4, 16, 3, Status.FULL_RECONCILIATION,
				REC_MODIFIED);

		log.debug("testHeadlessReconciliationWsOnly2() - done.");
	}

	@Test
	public void testHeadlessReconciliationWsOnly3() throws Exception
	{
		log.debug("testHeadlessReconciliationWsOnly3()");

		/*
		 * Setup fake data.
		 */
		String persistedFileContent = "\tABC\n\tDEF\n   GEH\n\tIJH\n";
		String newFileContent = "   \tABC\n\tDEF\n   GEH\n\tIJH\n";

		subTestStandaloneReconciliation(persistedFileContent, newFileContent, 12, 4, 16, 3, Status.FULL_RECONCILIATION,
				REC_MODIFIED);

		log.debug("testHeadlessReconciliationWsOnly3() - done.");
	}

	@Test
	public void testHeadlessReconciliationWsOnly4() throws Exception
	{
		log.debug("testHeadlessReconciliationWsOnly4()");

		/*
		 * Setup fake data.
		 */
		String persistedFileContent = "\tABC\n\tDEF\n   GEH\n\tIJH\n";
		String newFileContent = "   \tABC\n\tDEF\n   GEH\n\tIJH\n";

		subTestStandaloneReconciliation(persistedFileContent, newFileContent, 12, 5, 16, 3, Status.FULL_RECONCILIATION,
				REC_MODIFIED);

		log.debug("testHeadlessReconciliationWsOnly4() - done.");
	}

	@Test
	public void testHeadlessReconciliationWsOnly5() throws Exception
	{
		log.debug("testHeadlessReconciliationWsOnly5()");

		/*
		 * Setup fake data.
		 */
		String persistedFileContent = "\tABC\n\tDEF\n   GEH\n\tIJH\n";
		String newFileContent = "\tABC\n\tDEF\n   G E H\n\tIJH\n";

		//clone: "   GEH\n"
		subTestStandaloneReconciliation(persistedFileContent, newFileContent, 10, 7, 13, 5, Status.FULL_RECONCILIATION,
				REC_MODIFIED);

		log.debug("testHeadlessReconciliationWsOnly5() - done.");
	}

	@Test
	public void testHeadlessReconciliationWsOnly6() throws Exception
	{
		log.debug("testHeadlessReconciliationWsOnly6()");

		/*
		 * Setup fake data.
		 */
		String persistedFileContent = "\tABC\n\tDEF\n   G E H\n\tIJH\n";
		String newFileContent = "\tABC\n\tDEF\n   GEH\n\tIJH\n";

		//clone: "   GEH\n"
		subTestStandaloneReconciliation(persistedFileContent, newFileContent, 13, 5, 13, 3, Status.FULL_RECONCILIATION,
				REC_MODIFIED);

		log.debug("testHeadlessReconciliationWsOnly6() - done.");
	}

	@Test
	public void testHeadlessReconciliationNotAffected1() throws Exception
	{
		log.debug("testHeadlessReconciliationNotAffected1()");

		/*
		 * Setup fake data.
		 */
		String persistedFileContent = "\tABC\n\tDEF\n   GEH\n\tIJH\n";
		String newFileContent = "\tABC\n\tDEF\n   GEH\n\tIJH   \n";

		//clone: "GEH"
		subTestStandaloneReconciliation(persistedFileContent, newFileContent, 13, 3, 13, 3, Status.FULL_RECONCILIATION,
				REC_NOCHANGE);

		log.debug("testHeadlessReconciliationNotAffected1() - done.");
	}

	@Test
	public void testHeadlessReconciliationNotAffected2() throws Exception
	{
		log.debug("testHeadlessReconciliationNotAffected2()");

		/*
		 * Setup fake data.
		 */
		String persistedFileContent = "\tABC\n\tDEF\n   GEH\n\tIJH\n";
		String newFileContent = "\tABC\n\tDEF\n   GEH\n\tIJH sf asdf  \n";

		//clone: "GEH"
		subTestStandaloneReconciliation(persistedFileContent, newFileContent, 13, 3, 13, 3, Status.FULL_RECONCILIATION,
				REC_NOCHANGE);

		log.debug("testHeadlessReconciliationNotAffected2() - done.");
	}

	@Test
	public void testHeadlessReconciliationDiff1() throws Exception
	{
		log.debug("testHeadlessReconciliationDiff1()");

		/*
		 * Setup fake data.
		 */
		String persistedFileContent = "\tABC\n\tDEF\n   GEH\n\tIJH\n";
		String newFileContent = "\tABC\n\tDEF\n\tIJH\n";

		//clone: "GEH"
		subTestStandaloneReconciliation(persistedFileContent, newFileContent, 13, 3, 0, 0, Status.FULL_RECONCILIATION,
				REC_REMOVED);

		log.debug("testHeadlessReconciliationDiff1() - done.");
	}

	@Test
	public void testHeadlessReconciliationDiff2() throws Exception
	{
		log.debug("testHeadlessReconciliationDiff2()");

		/*
		 * Setup fake data.
		 */
		String persistedFileContent = "\tABC\n\tDEF\n   GEH\n\tIJH\n";
		String newFileContent = "\tABC\n\tDEF\n   GEEEH\n\tIJH\n";

		//clone: "GEH"
		subTestStandaloneReconciliation(persistedFileContent, newFileContent, 13, 3, 13, 5, Status.FULL_RECONCILIATION,
				REC_MODIFIED);

		log.debug("testHeadlessReconciliationDiff2() - done.");
	}

	@Test
	public void testHeadlessReconciliationDiff3() throws Exception
	{
		log.debug("testHeadlessReconciliationDiff3()");

		/*
		 * Setup fake data.
		 */
		String persistedFileContent = "\tABC\n\tDEF\n   GEH\n\tIJH\n";
		String newFileContent = "\tABC\n\tDEF\n  EH\n\tIJH\n";

		//clone: "GEH"
		subTestStandaloneReconciliation(persistedFileContent, newFileContent, 13, 3, 12, 2, Status.FULL_RECONCILIATION,
				REC_MODIFIED);

		log.debug("testHeadlessReconciliationDiff3() - done.");
	}

	@Test
	public void testHeadlessReconciliationDiff4() throws Exception
	{
		log.debug("testHeadlessReconciliationDiff4()");

		/*
		 * Setup fake data.
		 */
		String persistedFileContent = "\tABC\n\tDEF\n   GEH\n\tIJH\n";
		String newFileContent = "\tABC\n\tDEF\n   GE\tIJH\n";

		//clone: "GEH"
		subTestStandaloneReconciliation(persistedFileContent, newFileContent, 13, 3, 13, 2, Status.FULL_RECONCILIATION,
				REC_MODIFIED);

		log.debug("testHeadlessReconciliationDiff4() - done.");
	}

	private void subTestStandaloneReconciliation(String persistedFileContent, String newFileContent, int oldOffset,
			int oldLen, int newOffset, int newLen, Status recStatus, byte cloneStatus)
	{
		IReconcilerProvider recProv = (IReconcilerProvider) CPCCorePlugin
				.getProviderRegistry().lookupProvider(IReconcilerProvider.class);
		assertTrue("failed to obtain reconciliation provider", recProv != null);

		ICloneFile cloneFile = (ICloneFile) cloneFactoryProvider.getInstance(ICloneFile.class);
		((ICreatorCloneFile) cloneFile).setProject("Someproject");
		((ICreatorCloneFile) cloneFile).setPath("some/path");

		List<IClone> persistedClones = new ArrayList<IClone>(1);

		//Clone: "GEH\n"
		IClone clone = (IClone) cloneFactoryProvider.getInstance(IClone.class);
		((ICreatorClone) clone).setFileUuid(cloneFile.getUuid());
		((ICreatorClone) clone).setCreationDate(new Date());
		((ICreatorClone) clone).setCreator("TEST");
		((ICreatorClone) clone).setContent(persistedFileContent.substring(oldOffset, oldOffset + oldLen));
		clone.setOffset(oldOffset);
		clone.setLength(oldLen);
		persistedClones.add(clone);

		IReconciliationResult result = recProv.reconcile(cloneFile, persistedClones, persistedFileContent,
				newFileContent, false);

		assertTrue("reconciliation status incorrect - got: " + result.getStatus() + ", expected: " + recStatus, result
				.getStatus().equals(recStatus));

		IClone newClone = null;
		if (cloneStatus != REC_LOST)
			assertTrue("lost clones list not empty: " + result.getLostClones(), result.getLostClones().isEmpty());
		else
		{
			assertTrue("lost clones list is empty", !result.getLostClones().isEmpty());
			assertTrue("lost clones list has wrong size: " + result.getLostClones(), result.getLostClones().size() == 1);
			newClone = result.getLostClones().get(0);
		}

		if (cloneStatus != REC_MODIFIED)
			assertTrue("modified clones list not empty: " + result.getModifiedClones(), result.getModifiedClones()
					.isEmpty());
		else
		{
			assertTrue("modified clones list is empty", !result.getModifiedClones().isEmpty());
			assertTrue("modified clones list has wrong size: " + result.getModifiedClones(), result.getModifiedClones()
					.size() == 1);
			newClone = result.getModifiedClones().get(0);
		}

		if (cloneStatus != REC_REMOVED)
			assertTrue("removed clones list not empty: " + result.getRemovedClones(), result.getRemovedClones()
					.isEmpty());
		else
		{
			assertTrue("removed clones list is empty", !result.getRemovedClones().isEmpty());
			assertTrue("removed clones list has wrong size: " + result.getRemovedClones(), result.getRemovedClones()
					.size() == 1);
			newClone = result.getRemovedClones().get(0);
		}

		if (cloneStatus != REC_MOVED && cloneStatus != REC_MODIFIED)
			assertTrue("moved clones list not empty: " + result.getMovedClones(), result.getMovedClones().isEmpty());
		else if (cloneStatus == REC_MOVED)
		{
			assertTrue("moved clones list is empty", !result.getMovedClones().isEmpty());
			assertTrue("moved clones list has wrong size: " + result.getMovedClones(),
					result.getMovedClones().size() == 1);
			newClone = result.getMovedClones().get(0);
		}

		if (cloneStatus == REC_NOCHANGE)
		{
			assertTrue("got new clone instance, even though no change was expected", newClone == null);
			newClone = clone;
		}

		assertTrue("unable to locate new clone instance", newClone != null);

		if (cloneStatus != REC_REMOVED && cloneStatus != REC_LOST)
		{
			assertTrue("reconciled clone has wrong offset - got: " + newClone.getOffset() + ", expected: " + newOffset
					+ ", content: " + CoreStringUtils.quoteString(newClone.getContent()),
					newClone.getOffset() == newOffset);
			assertTrue("reconciled clone has wrong length - got: " + newClone.getLength() + ", expected: " + newLen
					+ ", content: " + CoreStringUtils.quoteString(newClone.getContent()),
					newClone.getLength() == newLen);

			assertTrue("clone content not updated correctly - got: "
					+ CoreStringUtils.quoteString(newClone.getContent()) + ", expected: "
					+ CoreStringUtils.quoteString(newFileContent.substring(newOffset, newOffset + newLen)),
					newFileContent.substring(newOffset, newOffset + newLen).equals(newClone.getContent()));
		}
	}

}

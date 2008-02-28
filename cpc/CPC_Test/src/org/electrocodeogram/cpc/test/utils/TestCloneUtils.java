package org.electrocodeogram.cpc.test.utils;


import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.data.ICloneFile;
import org.electrocodeogram.cpc.core.api.data.ICloneGroup;
import org.electrocodeogram.cpc.core.api.data.special.ICreatorClone;
import org.electrocodeogram.cpc.core.api.provider.data.ICloneFactoryProvider;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider;
import org.electrocodeogram.cpc.core.api.provider.store.StoreLockingException;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider.LockMode;
import org.electrocodeogram.cpc.core.utils.CoreStringUtils;
import org.electrocodeogram.cpc.core.utils.CoreUtils;


public class TestCloneUtils
{
	private static Log log = LogFactory.getLog(TestCloneUtils.class);

	public static IClone createTestClone(ICloneFactoryProvider cloneFactoryProvider, ICloneFile file,
			ICloneGroup group, int start, int end, IDocument document)
	{
		IClone clone = (IClone) cloneFactoryProvider.getInstance(IClone.class);
		assertNotNull(clone);
		((ICreatorClone) clone).setCreationDate(new Date());
		((ICreatorClone) clone).setCreator("test");
		((ICreatorClone) clone).setFileUuid(file.getUuid());
		clone.setTransient(false);
		if (group != null)
			clone.setGroupUuid(group.getUuid());

		clone.setOffset(start);
		clone.setLength(end - start + 1);

		if (document != null)
		{
			try
			{
				((ICreatorClone) clone).setContent(document.get(clone.getOffset(), clone.getLength()));
			}
			catch (BadLocationException e)
			{
				e.printStackTrace();
				log.info("CLONE: " + clone);
				log.info("OFFSET: " + clone.getOffset() + ", LENGTH: " + clone.getLength() + ", ENDOFFSET: "
						+ clone.getEndOffset());
				log.info("DOCUMENT-LENGTH: " + document.getLength());
				log.info("DOCUMENT-CONTENT: " + document.get());
				assertTrue("extraction of clone content failed - " + e, false);
			}
		}
		else
		{
			//fake content
			((ICreatorClone) clone).setContent("FAKE CONTENT");
		}

		return clone;
	}

	public static void clonePosCheck(IStoreProvider storeProvider, ICloneFile cloneFile, int[] startPos, int[] lengths)
	{
		assertTrue("error in test case, array sizes must match", startPos.length == lengths.length);

		//Make sure that all CPC Track caches are being written back before we check the positions
		//Acquiring a write lock even though we only want to get a list of clones achieves this.
		List<IClone> clones = null;
		try
		{
			storeProvider.acquireWriteLock(LockMode.DEFAULT);
			clones = storeProvider.getClonesByFile(cloneFile.getUuid());
		}
		catch (StoreLockingException e)
		{
			log.error("clonePosCheck() - locking error - " + e, e);
		}
		finally
		{
			storeProvider.releaseWriteLock();
		}

		if (log.isTraceEnabled())
		{
			log.trace("clonePosCheck() - clones: " + clones);
			log.trace("clonePosCheck() - current pos  - " + extractClonePositionShortString(clones));
			log.trace("clonePosCheck() - expected pos - offsets: " + CoreUtils.arrayToString(startPos) + " - lens: "
					+ CoreUtils.arrayToString(lengths));
		}

		assertTrue("invalid clone count - got: " + clones.size() + ", expected: " + startPos.length,
				clones.size() == startPos.length);

		for (int i = 0; i < startPos.length; ++i)
		{
			if (clones.get(i).getOffset() != startPos[i] || clones.get(i).getLength() != lengths[i])
			{
				log.warn("clonePosCheck() - position check failed for clone - clone: " + clones.get(i));
				log.warn("clonePosCheck() - expected position: " + startPos[i] + ":" + lengths[i]);
				log.warn("clonePosCheck() - current position : " + clones.get(i).getOffset() + ":"
						+ clones.get(i).getLength());
				log.warn("clonePosCheck() - current content: "
						+ CoreStringUtils.quoteString(clones.get(i).getContent()));
			}
			assertTrue("invalid clone position for clone " + i + " - got: " + clones.get(i).getOffset() + ":"
					+ clones.get(i).getLength() + ", expected: " + startPos[i] + ":" + lengths[i], clones.get(i)
					.getOffset() == startPos[i]
					&& clones.get(i).getLength() == lengths[i]);
		}
	}

	private static String extractClonePositionShortString(List<IClone> clones)
	{
		List<Integer> pos = new ArrayList<Integer>(clones.size());
		List<Integer> len = new ArrayList<Integer>(clones.size());

		for (IClone clone : clones)
		{
			pos.add(clone.getOffset());
			len.add(clone.getLength());
		}

		return "offsets: " + CoreUtils.arrayToString(pos.toArray(new Integer[clones.size()])) + " - lens: "
				+ CoreUtils.arrayToString(len.toArray(new Integer[clones.size()]));
	}

	/**
	 * Retrieves the current clone content for a given clone UUID.
	 *  
	 * @param storeProvider
	 * @param cloneUuid
	 * @return may be NULL
	 */
	public static String getCloneContent(IStoreProvider storeProvider, String cloneUuid)
	{
		assert (cloneUuid != null);

		IClone clone = null;
		try
		{
			storeProvider.acquireWriteLock(LockMode.DEFAULT);
			clone = storeProvider.lookupClone(cloneUuid);
		}
		catch (StoreLockingException e)
		{
			log.error("getCloneContent() - locking error - " + e, e);
			return null;
		}
		finally
		{
			storeProvider.releaseWriteLock();
		}

		if (clone == null)
			return null;

		return clone.getContent();
	}

}

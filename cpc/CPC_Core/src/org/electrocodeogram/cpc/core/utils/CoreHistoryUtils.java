package org.electrocodeogram.cpc.core.utils;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.electrocodeogram.cpc.core.api.data.CloneDiff;
import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.data.extension.ICloneModificationHistoryExtension;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider;


public class CoreHistoryUtils
{
	private static final Log log = LogFactory.getLog(CoreHistoryUtils.class);

	/**
	 * Takes a clone and a date and returns the content of the clone at the given point in time.<br/>
	 * If a {@link ICloneModificationHistoryExtension} {@link CloneDiff} entry matches the date exactly,
	 * it will still be taken into account.
	 * 
	 * @param storeProvider a valid store provider reference, never null.
	 * @param clone the clone to get the content for, never null.
	 * @param historyDate the point in time for which the content should be retrieved, never null.
	 * 		If the date is smaller than the creation date of the clone, the original clone content is returned.
	 * @param enableOptimisations if true, certain optimisations will be performed to reduce the performance impact
	 * 		of this method. I.e. directly returning the original content if the <em>historyDate</em> lies after
	 * 		all {@link CloneDiff} elements. Usually you'll want to set this value to true. Setting this
	 * 		value to false only has a meaning for debugging situations.
	 * @return content of the clone at the given point in time, may be NULL if the content could not
	 * 		be computed.
	 * 
	 * @see ICloneModificationHistoryExtension
	 * @see CloneDiff
	 */
	public static String getCloneContentForDate(IStoreProvider storeProvider, IClone clone, Date historyDate,
			boolean enableOptimisations)
	{
		if (log.isDebugEnabled())
			log.debug("getCloneContentForDate() - clone: " + clone + ", historyDate: " + historyDate + " ["
					+ historyDate.getTime() + "]");
		assert (storeProvider != null && clone != null && historyDate != null);

		//check if the original clone content is all we need
		if (historyDate.before(clone.getCreationDate()))
		{
			log
					.debug("getCloneContentForDate() - history date way younger than clone creation date, returning original clone content.");
			return clone.getOriginalContent();
		}

		//get the full history for this clone
		ICloneModificationHistoryExtension history = (ICloneModificationHistoryExtension) clone
				.getExtension(ICloneModificationHistoryExtension.class);
		if (history == null || history.isPartial())
		{
			log.debug("getCloneContentForDate() - retrieving full history from store provider.");
			history = (ICloneModificationHistoryExtension) storeProvider.getFullCloneObjectExtension(clone,
					ICloneModificationHistoryExtension.class);
		}

		//check if we now have a full history
		if (history == null || history.getCloneDiffs().isEmpty())
		{
			//there seems to be no history for this clone, return the original content
			log.debug("getCloneContentForDate() - clone has no history, returning original clone content.");

			//this clone should not have been modified, ever
			if (!clone.getOriginalContent().equals(clone.getContent()))
			{
				log
						.error(
								"getCloneContentForDate() - clone has no history, but original clone content and current clone content don't match - clone: "
										+ clone
										+ ", history: "
										+ history
										+ ", original content: "
										+ CoreStringUtils.truncateString(clone.getOriginalContent())
										+ ", current content: " + CoreStringUtils.truncateString(clone.getContent()),
								new Throwable());
				return null;
			}

			return clone.getOriginalContent();
		}

		assert (!history.isPartial());

		/*
		 * Check if there are any diffs after the given point in time.
		 * If there aren't, we can just return the current content.
		 */
		List<CloneDiff> diffs = history.getCloneDiffs();
		CloneDiff lastDiff = diffs.get(diffs.size() - 1);
		if (enableOptimisations && historyDate.after(lastDiff.getCreationDate()))
		{
			//there seems to be no history for this clone, return the original content
			log
					.debug("getCloneContentForDate() - clone was not modifiy since the given point in time, returning current clone content.");
			return clone.getContent();
		}

		/*
		 * Ok, now we have the original content and the full history.
		 * Replay each diff event till we reach the given point in time.
		 */
		String content = clone.getOriginalContent();
		assert (content != null);
		IDocument document = new Document(content);

		if (log.isDebugEnabled())
			log.debug("ORIGINAL-CONTENT: " + CoreStringUtils.quoteString(content));

		for (CloneDiff diff : diffs)
		{
			if (log.isTraceEnabled())
				log.trace("DIFF: " + diff);

			if (diff.getCreationDate().after(historyDate))
			{
				log.trace("getCloneContentForDate() - this diff is too old, aborting diff replay - diff date: "
						+ diff.getCreationDate().getTime() + ", historyDate: " + historyDate.getTime() + ", delta: "
						+ (diff.getCreationDate().getTime() - historyDate.getTime()));
				break;
			}

			//apply diff to document
			try
			{
				document.replace(diff.getOffset(), diff.getLength(), (diff.getText() != null ? diff.getText() : ""));
			}
			catch (BadLocationException e)
			{
				log.error("getCloneContentForDate() - unable to apply diff - diff: " + diff + ", content: "
						+ CoreStringUtils.quoteString(document.get()) + " - " + e, e);
				return null;
			}

			if (log.isTraceEnabled())
				log.trace("POST-CONTENT: " + document.get());
		}

		if (log.isDebugEnabled())
			log.debug("getCloneContentForDate() - result: " + document.get());

		return document.get();
	}

	/**
	 * Reconstructs <b>all</b> content states for the given clone.
	 * 
	 * @param storeProvider a valid {@link IStoreProvider} reference, never null.
	 * @param clone the clone to reconstruct the past contents for, never null.
	 * @param history the {@link ICloneModificationHistoryExtension} to obtain the history data from.
	 * 		The extension must be <b>fully loaded</b>. Never null.
	 * @return a list with one entry per {@link CloneDiff} element found in the given {@link ICloneModificationHistoryExtension}.
	 * 		The order is the same as in the extension (meaning oldest first). The string represents the clone content
	 * 		<b>after</b> the corresponding {@link CloneDiff} has been applied. The content of the last entry should
	 * 		match the current content of the clone. The original clone content is not part of this list. Never null.
	 */
	public static List<HistoryEntry> getCloneAllContents(IStoreProvider storeProvider, IClone clone,
			ICloneModificationHistoryExtension history)
	{
		if (log.isDebugEnabled())
			log.debug("getCloneAllContents() - clone: " + clone + ", history: " + history);
		assert (storeProvider != null && clone != null && history != null && !history.isPartial());

		List<HistoryEntry> result = new ArrayList<HistoryEntry>(history.getCloneDiffs().size());

		/*
		 * We have the original content and the full history.
		 * Replay each diff event till we the end of the history.
		 * Store all intermediate results.
		 */
		String content = clone.getOriginalContent();
		assert (content != null);
		IDocument document = new Document(content);

		if (log.isDebugEnabled())
			log.debug("ORIGINAL-CONTENT: " + content);

		for (CloneDiff diff : history.getCloneDiffs())
		{
			if (log.isTraceEnabled())
				log.trace("DIFF: " + diff);

			//apply diff to document
			try
			{
				document.replace(diff.getOffset(), diff.getLength(), (diff.getText() != null ? diff.getText() : ""));
			}
			catch (BadLocationException e)
			{
				log.error("getCloneContentForDate() - unable to apply diff - diff: " + diff + ", content: "
						+ document.get() + " - " + e, e);
				return result;
			}

			if (log.isTraceEnabled())
				log.trace("POST-CONTENT: " + document.get());

			//add to result list
			result.add(new HistoryEntry(diff, document.get()));
		}

		if (log.isDebugEnabled())
			log.debug("getCloneAllContents() - result: " + result);

		return result;
	}
}

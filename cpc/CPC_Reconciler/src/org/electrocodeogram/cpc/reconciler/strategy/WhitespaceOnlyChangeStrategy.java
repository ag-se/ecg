package org.electrocodeogram.cpc.reconciler.strategy;


import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.electrocodeogram.cpc.core.CPCCorePlugin;
import org.electrocodeogram.cpc.core.api.CPCConstants;
import org.electrocodeogram.cpc.core.api.data.CloneDiff;
import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.data.ICloneFile;
import org.electrocodeogram.cpc.core.api.data.extension.ICloneModificationHistoryExtension;
import org.electrocodeogram.cpc.core.api.data.extension.ICloneNonWsPositionExtension;
import org.electrocodeogram.cpc.core.api.data.special.ICreatorClone;
import org.electrocodeogram.cpc.core.api.provider.data.ICloneFactoryProvider;
import org.electrocodeogram.cpc.core.api.provider.reconciler.IDiffResult;
import org.electrocodeogram.cpc.core.api.provider.reconciler.IReconciliationResult;
import org.electrocodeogram.cpc.core.utils.CoreStringUtils;
import org.electrocodeogram.cpc.reconciler.api.strategy.IReconcilerStrategy;


/**
 * This strategy checks whether all modifications made to a file are purely whitespace changes.<br/>
 * This typically happens if automated source-reformat utilities are used.<br/>
 * <br/>
 * If this situation is detected, clone positions are updated by falling back to the corresponding
 * nonWsOffsets.
 * 
 * @author vw
 */
public class WhitespaceOnlyChangeStrategy implements IReconcilerStrategy
{
	private static Log log = LogFactory.getLog(WhitespaceOnlyChangeStrategy.class);

	private ICloneFactoryProvider cloneFactoryProvider;

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.reconciler.strategy.IExternalModificationReconcilerStrategy#reconcile(org.electrocodeogram.cpc.core.api.data.ICloneFile, java.util.List, java.lang.String, java.lang.String, java.util.List, java.util.List, org.electrocodeogram.cpc.core.api.provider.reconciler.ReconciliationResult)
	 */
	@Override
	public Status reconcile(ICloneFile cloneFile, List<IClone> persistedClones, String persistedFileContent,
			String newFileContent, List<IDiffResult> differences, LinkedList<IClone> pendingClones,
			IReconciliationResult result)
	{
		if (log.isTraceEnabled())
			log.trace("reconcile() - ..., pendingClones: " + pendingClones + ", result: " + result);

		/*
		 * This strategy does not re-evaluate any clones which have already been handled by
		 * another strategy. This means that there is nothing to do if pendingClones is empty.
		 */
		if (pendingClones.isEmpty())
		{
			log.trace("reconcile() - pendingClones is empty, SKIPPING");
			return Status.SKIPPED;
		}

		//now check if there are any non-whitespace changes
		for (IDiffResult diff : differences)
		{
			if (CoreStringUtils.containsNonWhitespace(diff.getText()))
			{
				//nope, this diff also contains some non-whitespace changes
				//we can't handle this
				log.trace("reconcile() - modification made non-whitespace changes, SKIPPING - i.e. \"" + diff.getText()
						+ "\"");

				return Status.SKIPPED;
			}
		}

		/*
		 * Ok, all changes are whitespace only, we can obtain the updated clone positions
		 * by using the nonWsOffsets.
		 */
		log.trace("reconcile() - modification contains only whitespace changes");

		List<IClone> handledClones = new LinkedList<IClone>();
		cloneFactoryProvider = (ICloneFactoryProvider) CPCCorePlugin.getProviderRegistry().lookupProvider(
				ICloneFactoryProvider.class);
		assert (cloneFactoryProvider != null);

		boolean fullyReconciled = true;
		for (IClone clone : pendingClones)
		{
			if (log.isTraceEnabled())
				log.trace("CLONE: " + clone);

			byte state = updateAbsoluteOffsetsByNonWsOffsets(clone, newFileContent);
			if (state != POS_FAILED)
			{
				//the position of this clone was successfully reconciled, we'll need to remember it
				//and have to remove it from the pendingClones list later.
				handledClones.add(clone);

				//the clone may have been moved
				if (state == POS_MOVED)
					result.getMovedClones().add(clone);

				//or modified
				else if (state == POS_MODIFIED)
				{
					result.getModifiedClones().add(clone);

					//in most cases a modified clone will also have moved
					result.getMovedClones().add(clone);
				}
			}
			else
			{
				fullyReconciled = false;
			}
		}

		//now remove all clones which were handled from the pending clones list
		if (fullyReconciled)
			//as an optimisation we clear the entire list, if all clones were handled
			pendingClones.clear();
		else
			//otherwise we delete only the ones which we updated earlier
			pendingClones.removeAll(handledClones);

		if (fullyReconciled)
			return Status.FULL;
		else
			return Status.PARTIAL;
	}

	/**
	 * The clone was moved within the file.
	 * The length and content remain unchanged.
	 */
	private static final byte POS_MOVED = 1;

	/**
	 * The clone's length or content have changed.
	 * It's position may also have changed.
	 */
	private static final byte POS_MODIFIED = 2;

	/**
	 * The clone's position was not modified.
	 * It is still in sync with the new file content.
	 */
	private static final byte POS_UNCHANGED = 3;

	/**
	 * The clone's new position could not be computed.
	 */
	private static final byte POS_FAILED = 4;

	/**
	 * Takes the non-whitespace offsets of the clone and the new file content and
	 * calculates corresponding absolute offsets. The new offsets are then
	 * written to the clone position.
	 * 
	 * @param clone the clone to update, never null.
	 * @param fileContent the new file content, never null.
	 * @return the result state of the update. {@link WhitespaceOnlyChangeStrategy#POS_FAILED},
	 * 		{@link WhitespaceOnlyChangeStrategy#POS_MOVED}, {@link WhitespaceOnlyChangeStrategy#POS_MODIFIED}
	 * 		or {@link WhitespaceOnlyChangeStrategy#POS_UNCHANGED}
	 */
	private byte updateAbsoluteOffsetsByNonWsOffsets(IClone clone, String fileContent)
	{
		assert (clone != null && fileContent != null);

		ICloneNonWsPositionExtension nonWsPosExt = (ICloneNonWsPositionExtension) clone
				.getExtension(ICloneNonWsPositionExtension.class);
		if (nonWsPosExt == null)
		{
			log
					.warn("updateAbsoluteOffsetsByNonWsOffsets() - non-whitespace position data is not present, ignoring clone: "
							+ clone);
			return POS_FAILED;
		}
		int nonWsStart = nonWsPosExt.getStartNonWsOffset();
		int nonWsEnd = nonWsPosExt.getEndNonWsOffset();

		int newStart = -1;
		int newEnd = -1;

		int absPos = 0;
		int nonWsPos = 0;
		for (char chr : fileContent.toCharArray())
		{
			if (log.isTraceEnabled() && ((Math.abs(nonWsPos - nonWsStart) < 5) || (Math.abs(nonWsPos - nonWsEnd) < 5)))
				log.trace("absPos: " + absPos + ", nonWsPos: " + nonWsPos + ", char: " + chr);

			if (nonWsPos == nonWsStart)
			{
				/*
				 * This may happen multiple times in a row if there is more than one whitespace
				 * in the border area. In such a case the last update of newStart will prevail.
				 * It will point to the first non-whitespace character after the border area.
				 */
				if (log.isTraceEnabled())
					log.trace("updateAbsoluteOffsetsByNonWsOffsets() - found start pos - nonWsPos: " + nonWsPos
							+ ", absPos: " + absPos);

				newStart = absPos;
			}

			if (nonWsPos == nonWsEnd /*&& newEnd == -1*/)
			{
				/*
				 * For the end position take the very first match.
				 * It should correspond to the first whitespace after the clone content.
				 * We then substract one, in order to obtain the last non-whitespace character. 
				 */
				if (log.isTraceEnabled())
					log.trace("updateAbsoluteOffsetsByNonWsOffsets() - found end pos - nonWsPos: " + nonWsPos
							+ ", absPos: " + absPos);

				newEnd = absPos;
			}

			//update positions for next loop
			if (chr != ' ' && chr != '\t' && chr != '\n' && chr != '\r')
				++nonWsPos;
			++absPos;
		}

		if (newStart == -1 || newEnd == -1)
		{
			log
					.warn("updateAbsoluteOffsetsByNonWsOffsets() - unable to compute absolute offsets from nonWsOffsets - newStart: "
							+ newStart + ", newEnd: " + newEnd + ", clone: " + clone);

			return POS_FAILED;
		}
		else
		{
			byte result = POS_UNCHANGED;

			int oldLength = clone.getLength();

			//check if we modified the positions
			if (clone.getOffset() != newStart || clone.getEndOffset() != newEnd)
			{

				//updated
				clone.setOffset(newStart);
				clone.setLength(newEnd - newStart + 1);

				if (log.isTraceEnabled())
					log.trace("updateAbsoluteOffsetsByNonWsOffsets() - new position - offset: " + clone.getOffset()
							+ ", len: " + clone.getLength() + ", oldLen: " + oldLength);

				result = POS_MOVED;
			}
			else
			{
				//unchanged				
				if (log.isTraceEnabled())
					log.trace("updateAbsoluteOffsetsByNonWsOffsets() - position remained unchanged - offset: "
							+ clone.getOffset() + ", len: " + clone.getLength());
			}

			//check if we modified the clone's content
			String oldContent = clone.getContent();
			String newContent = fileContent.substring(clone.getOffset(), clone.getOffset() + clone.getLength());
			if (!oldContent.equals(newContent))
			{
				if (log.isTraceEnabled())
					log.trace("updateAbsoluteOffsetsByNonWsOffsets() - clone content modified - old content: "
							+ CoreStringUtils.quoteString(oldContent) + ", new content: "
							+ CoreStringUtils.quoteString(newContent));

				((ICreatorClone) clone).setContent(newContent);

				/*
				 * We need to add some fake CloneDiffs here in order to ensure
				 * that the clone diff data is correct.
				 * For now we just replace the entire clone content. 
				 */
				ICloneModificationHistoryExtension history = (ICloneModificationHistoryExtension) clone
						.getExtension(ICloneModificationHistoryExtension.class);
				if (history == null)
				{
					history = (ICloneModificationHistoryExtension) cloneFactoryProvider
							.getInstance(ICloneModificationHistoryExtension.class);
					clone.addExtension(history);
				}
				history.addCloneDiff(new CloneDiff(CPCConstants.CLONE_CREATOR_AUTOMATED_RECONCILIATION, new Date(),
						true, 0, oldLength, newContent));

				result = POS_MODIFIED;
			}

			return result;
		}
	}
}

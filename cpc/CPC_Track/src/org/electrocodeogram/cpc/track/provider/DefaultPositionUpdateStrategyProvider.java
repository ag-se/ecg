package org.electrocodeogram.cpc.track.provider;


import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.electrocodeogram.cpc.core.CPCCorePlugin;
import org.electrocodeogram.cpc.core.api.data.CloneDiff;
import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.data.extension.ICloneModificationHistoryExtension;
import org.electrocodeogram.cpc.core.api.data.special.ICreatorClone;
import org.electrocodeogram.cpc.core.api.provider.IManagableProvider;
import org.electrocodeogram.cpc.core.api.provider.data.ICloneFactoryProvider;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider;
import org.electrocodeogram.cpc.core.api.provider.track.CPCDocumentEvent;
import org.electrocodeogram.cpc.core.api.provider.track.CPCPosition;
import org.electrocodeogram.cpc.core.api.provider.track.IPositionUpdateStrategyProvider;
import org.electrocodeogram.cpc.core.utils.CoreHistoryUtils;
import org.electrocodeogram.cpc.core.utils.CoreStringUtils;
import org.electrocodeogram.cpc.core.utils.CoreUtils;


/**
 * Default {@link IPositionUpdateStrategyProvider} implementation.
 * 
 * @author vw
 */
public class DefaultPositionUpdateStrategyProvider implements IPositionUpdateStrategyProvider, IManagableProvider
{
	private static Log log = LogFactory.getLog(DefaultPositionUpdateStrategyProvider.class);

	private ICloneFactoryProvider cloneFactoryProvider;
	private IStoreProvider storeProvider;

	public DefaultPositionUpdateStrategyProvider()
	{
		if (log.isTraceEnabled())
			log.trace("CPCPositionUpdateStrategy()");

		cloneFactoryProvider = (ICloneFactoryProvider) CPCCorePlugin.getProviderRegistry().lookupProvider(
				ICloneFactoryProvider.class);
		assert (cloneFactoryProvider != null);

		storeProvider = (IStoreProvider) CPCCorePlugin.getProviderRegistry().lookupProvider(IStoreProvider.class);
		assert (storeProvider != null);
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.track.IPositionUpdateStrategyProvider#updatePositions(org.eclipse.jface.text.DocumentEvent, org.eclipse.jface.text.Position[])
	 */
	@Override
	public boolean updatePositions(DocumentEvent event, Position[] positions)
	{
		if (log.isTraceEnabled())
			log.trace("updatePositions() - event: " + event + ", positions: " + positions + " ["
					+ (positions != null ? positions.length : '-') + "]");
		assert (event != null && positions != null);
		if (log.isTraceEnabled())
			log.trace("updatePositions() - EVENT - offset: " + event.getOffset() + ", length: " + event.getLength()
					+ ", text: " + CoreStringUtils.quoteString(event.getText()) + ", mod: "
					+ event.getModificationStamp());

		if (positions.length == 0)
			//nothing to do
			return false;

		//keep track of whether we updated any positions
		boolean positionsUpdated = false;

		//cache some values
		IDocument document = event.getDocument();

		//cache some offsets
		int length = event.getLength();
		int addLength = (event.getText() != null ? event.getText().length() : 0);
		int startOffset = event.getOffset();
		int endOffset = startOffset + event.getLength() - 1;
		int addEndOffset = startOffset + addLength - 1;

		//calculate some position diffs for our different measures
		int charCountDiff = addLength - length;

		if (log.isTraceEnabled())
			log.trace("updatePositions() - resulting diff - charCountDiff: " + charCountDiff + ", length: " + length
					+ ", addLength: " + addLength + ", startOffset: " + startOffset + ", endOffset: " + endOffset
					+ ", addEndOffset: " + addEndOffset);

		/*
		 * Clones can be affected by a diff in serveral ways:
		 *  a) deprecated
		 * 
		 * 	b) they are located entirely above this diff event's location
		 * 		- no need to modify these at all, they are not returned by the store provider query
		 * 
		 * 	c) they are located entirely below the diff event's location
		 * 		- they need to be shifted down accordingly but their contents are not being modified
		 * 
		 * 	d) they are directly affected by the diff event, as it is located inside of those clones
		 * 		- these events can potentially trigger CPC warnings and the clone data will need
		 * 			to be updated in various ways.
		 * 
		 * NOTE: on diff ranges
		 * 		The start and end offset only really differ, if data was deleted. For diffs which only add data
		 * 		endOffset==startOffset-1. (note that endOffset is NOT equal to startOffset in this case)
		 * 		addEndOffset marks the offset at which the diffs resulting output ends. In some cases
		 * 		you'll actually want to check against addEndOffset and not endOffset. Keep that in mind. 
		 * 
		 * TODO: what about cases where the diff directly touches, but does not enter a clone?
		 * 		 should we increase the size of the clone to encompass any new data introduced
		 * 		 by the diff? 
		 */

		CPCPosition cpcPos;
		int preEventOffset;
		for (Position position : positions)
		{
			if (log.isTraceEnabled())
				log.trace("POS: " + position);

			cpcPos = (CPCPosition) position;
			preEventOffset = cpcPos.getOffset();

			//ignore deleted positions
			if (cpcPos.isDeleted)
			{
				if (log.isTraceEnabled())
					log.trace("updatePositions() - ignoring deleted position - " + cpcPos);

				continue;
			}

			//b) entire clone located above the diff
			//else 
			if (cpcPos.getEndOffset() < startOffset)
			{
				if (log.isTraceEnabled())
					log.trace("updatePositions() - skipping clone ABOVE diff - cpcPos: " + cpcPos);

				//clone content should be unchanged
				debugValidateClone(cpcPos, document, null);
			}

			//c) entire clone located below the diff
			else if (endOffset < cpcPos.offset)
			{
				if (log.isTraceEnabled())
					log.trace("updatePositions() - moving clone BELOW diff - cpcPos: " + cpcPos);

				//make sure the position really changed
				if (charCountDiff != 0)
				{
					//we'll need to move the entire clone down or up according to the diff counts
					cpcPos.offset += charCountDiff;
					positionsUpdated = true;

					String content = null;
					if (log.isTraceEnabled())
					{
						log.trace("updatePositions() - new position: " + cpcPos);
						content = getCloneContent(document, cpcPos);
						log.trace("  CONTENT: " + CoreStringUtils.quoteString(content));
					}

					//clone content should match
					debugValidateClone(cpcPos, document, content);
				}
				else
				{
					if (log.isTraceEnabled())
						log.trace("updatePositions() - position remained unchanged by diff");
				}

			}

			//d) diff is at least partly located within the clone 
			else
			{
				if (log.isTraceEnabled())
					log.trace("updatePositions() - CLONE MODIFIED - cpcPos: " + cpcPos);

				/*
				 * possible cases:
				 *  1) the diff matches exactly with the clone offsets
				 *      -> the clone was removed
				 * 	2) the diff is located completely within the clone
				 * 		-> we just need to move the end marker
				 * 	3) the diff exceeds start AND end position (the diff is bigger than the clone)
				 * 		-> the entire clone was deleted
				 * 	4) the diff exceeds only the start position OR only the end position 
				 * 		-> at least part of the clone was deleted -> shrink the clone accordingly
				 * 		-> then maybe extend it again using any newly added stuff?
				 */

				//1) the diff matches exactly with the clone offsets
				if ((cpcPos.offset == startOffset) && (cpcPos.getEndOffset() == endOffset))
				{
					log.trace("processCodeDiffEvent() - DIFF REPLACED CLONE - deleting");

					/*
					 *  TODO: we might still want to keep the clone if the similarity of
					 *  	the replaced clone content and the diff content is high enough.
					 *  	It's not clear whether that would be useful though. Because complete replacement
					 *  	does imply removal of all earlier copy and pasted source.
					 */

					//the clone was deleted
					cpcPos.delete();
					positionsUpdated = true;

					log.trace("processCodeDiffEvent() - clone DELETED");

				}

				//2) diff located completely within the clone
				else if ((cpcPos.offset <= startOffset) && (endOffset <= cpcPos.getEndOffset()))
				{
					log.trace("processCodeDiffEvent() - diff WITHIN CLONE");

					//we only need to move the end position, if the clone has grown or shrunk
					//due to the update

					String content = null;

					cpcPos.length += charCountDiff;
					positionsUpdated = true;

					if (log.isTraceEnabled())
					{
						log.trace("processCodeDiffEvent() - new position: " + cpcPos);
						content = getCloneContent(document, cpcPos);
						log.trace("  CONTENT: " + CoreStringUtils.quoteString(content));
					}

					cpcPos.setContentModified(true);
					//the DocumentEvent is completely located within the clone. We can therefore just use the values
					//from the DocumentEvent for the CPCDocumentEvent. We just need to convert the offset to a relative
					//offset.
					cpcPos.addContentModifyingDocumentEvent(new CPCDocumentEvent(startOffset - preEventOffset, length,
							event.getText()));

					//store new clone content for future checks
					storeModifiedClone(cpcPos, document, content);
				}

				//3) the diff exceeds start AND end position (the diff is bigger than the clone)
				else if (startOffset <= cpcPos.offset && (cpcPos.getEndOffset() <= endOffset))
				{
					log.trace("processCodeDiffEvent() - diff OVER CLONE");

					//the clone was deleted
					cpcPos.delete();
					positionsUpdated = true;

					log.trace("processCodeDiffEvent() - clone DELETED");
				}

				//4) the diff exceeds only the start position OR only the end position
				else
				{
					log.trace("processCodeDiffEvent() - diff TOUCHES CLONE");
					if (log.isTraceEnabled())
						log.trace("processCodeDiffEvent() - offsets - dsoff: " + startOffset + ", deoff: " + endOffset
								+ ", daoff: " + addEndOffset + ", csoff: " + cpcPos.offset + ", ceoff: "
								+ cpcPos.getEndOffset());

					String content = null;

					/*
					 * TODO: it is not clear what should be done here, we have multiple options
					 * 
					 * 	a) shrink the clone accordingly, if the diff deleted any data, treat any
					 * 		newly added data as not belonging to the clone
					 * 		i.e.
					 * 			a b <diff> c <clone> d </diff> e f </clone> g h
					 * 			with diff inserting "1 2 3"
					 * 		=>
					 * 			a b 1 2 3 <clone> e f </clone> g h
					 * 	b) first shrink the clone according to the removed data then expand it
					 * 		according to the added data
					 * 		i.e.
					 * 			a b <diff> c <clone> d </diff> e f </clone> g h
					 * 			with diff inserting "1 2 3"
					 * 		=>
					 * 			a b <clone> 1 2 3 e f </clone> g h
					 * 	c) if data is removed and added, then only take the part into account which
					 * 		was overlapping with the clone.
					 * 		i.e.
					 * 			a b <diff> c <clone> d </diff> e f </clone> g h
					 * 			with diff inserting "1 2 3"
					 * 		=>
					 * 			a b 1 2 <clone> 3 e f </clone> g h
					 * 
					 * The choice could also be made dynamically, i.e. do b) if the clone would grow by
					 * less than X characters that way, otherwise use a) or c). If b) is deemed to be an
					 * interesting option, this approach may be required to prevent i.e. large code paste
					 * actions from bloating up any clone which they may only have slightly touched. 
					 */

					//TODO: for simplicity reasons we implement only a) for now
					//start of clone was touched
					if ((startOffset <= cpcPos.offset) && (cpcPos.offset <= endOffset))
					{
						log.trace("processCodeDiffEvent() - diff partly touches clone HEAD");

						//shrink clone by the number of characters removed, which are within the clones offset range
						int charsRemoved = endOffset - cpcPos.offset + 1;
						//TODO: check if this ^ is ok

						if (log.isTraceEnabled())
							log.trace("processCodeDiffEvent() - charsRemoved: " + charsRemoved);

						//move the start position accordingly.
						//the clones is moved as with any other move, however, at the same time it shrinks by
						//charsRemoved characters
						cpcPos.offset += charCountDiff + charsRemoved;

						//the end position also moves, but only as with any other move
						//pos.setEndOffset(pos.getEndOffset() + charCountDiff);
						//the length was reduced by charsRemoved chars
						cpcPos.length -= charsRemoved;
						positionsUpdated = true;

						//Create a simple removal CPCDocumentEvent, we just need to remove the first
						//charsRemoved characters from the clone.
						cpcPos.addContentModifyingDocumentEvent(new CPCDocumentEvent(0, charsRemoved, null));

						if (log.isTraceEnabled())
						{
							log.trace("processCodeDiffEvent() - new position: " + cpcPos);
							content = getCloneContent(document, cpcPos);
							log.trace("  CONTENT: " + CoreStringUtils.quoteString(content));
						}
					}
					//end of clone was touched
					else if ((startOffset <= cpcPos.getEndOffset()) && (cpcPos.getEndOffset() <= endOffset))
					{
						log.trace("processCodeDiffEvent() - diff partly touches clone TAIL");

						//shrink clone by the number of characters removed, which are within the clones offset range
						int charsRemoved = cpcPos.getEndOffset() - startOffset + 1;

						if (log.isTraceEnabled())
							log.trace("processCodeDiffEvent() - charsRemoved: " + charsRemoved);

						//move the end position accordingly.
						//pos.setEndOffset(pos.getEndOffset() - charsRemoved);

						//the length was reduced by charsRemoved chars
						cpcPos.length -= charsRemoved;
						positionsUpdated = true;

						//Create a simple removal CPCDocumentEvent, we just need to remove the last
						//charsRemoved characters from the clone.
						cpcPos.addContentModifyingDocumentEvent(new CPCDocumentEvent(startOffset - preEventOffset,
								charsRemoved, null));

						if (log.isTraceEnabled())
						{
							log.trace("processCodeDiffEvent() - new position: " + cpcPos);
							content = getCloneContent(document, cpcPos);
							log.trace("  CONTENT: " + CoreStringUtils.quoteString(content));
						}
					}
					else
					{
						//this shouldn't happen
						log
								.fatal("INTERNAL ERROR: processCodeDiffEvent() - unable to identify diff position in relation to clone data - dsoff: "
										+ startOffset
										+ ", deoff: "
										+ endOffset
										+ ", daoff: "
										+ addEndOffset
										+ ", csoff: "
										+ cpcPos.offset
										+ ", ceoff: "
										+ cpcPos.getEndOffset()
										+ ", cpcPos: " + cpcPos + ", diffEvent: " + event);
					}

					cpcPos.setContentModified(true);

					//store new clone content for debug checks
					storeModifiedClone(cpcPos, document, content);
				}

			}
		}

		return positionsUpdated;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.track.IPositionUpdateStrategyProvider#extractCloneData(org.eclipse.jface.text.Position[], java.util.List, java.util.List, java.util.List)
	 */
	@Override
	public void extractCloneData(Position[] positions, List<IClone> movedClones, List<IClone> modifiedClones,
			List<IClone> removedClones, IDocument document)
	{
		if (log.isTraceEnabled())
			log.trace("extractCloneData() - " + CoreUtils.arrayToString(positions) + ", movedClones: " + movedClones
					+ ", modifiedClones: " + modifiedClones + ", removedClones: " + removedClones + ", document: "
					+ document);
		assert (positions != null && movedClones != null && movedClones.isEmpty() && modifiedClones != null
				&& modifiedClones.isEmpty() && removedClones != null && removedClones.isEmpty());

		CPCPosition cpcPos;
		IClone clone;
		for (Position pos : positions)
		{

			if (pos instanceof CPCPosition)
			{
				cpcPos = (CPCPosition) pos;
				clone = cpcPos.getClone();

				if (pos.isDeleted())
				{
					//the clone was removed by one of the document change events
					if (log.isTraceEnabled())
						log.trace("extractCloneData() - clone removed - " + clone);

					removedClones.add(clone);

					//now remove the position from the document
					if (document != null)
					{
						try
						{
							document.removePosition(CPCPosition.CPC_POSITION_CATEGORY, pos);
						}
						catch (BadPositionCategoryException e)
						{
							log.error("extractCloneData() - failed to remove position - " + e + " - position: " + pos
									+ ", cat: " + CPCPosition.CPC_POSITION_CATEGORY, e);
						}
					}
				}
				else
				{
					//check if the clone moved
					if (clone.getOffset() != cpcPos.getOffset() || clone.getLength() != cpcPos.getLength())
					{
						if (log.isTraceEnabled())
							log.trace("extractCloneData() - clone moved - " + clone);

						//the clones position was modified
						clone.setOffset(cpcPos.getOffset());
						clone.setLength(cpcPos.getLength());

						movedClones.add(clone);
					}
					else
					{
						if (log.isTraceEnabled())
							log.trace("extractCloneData() - clone position and length unchanged - " + clone);
					}

					//check if the content was modified
					if (cpcPos.isContentModified())
					{
						if (log.isTraceEnabled())
							log.trace("extractCloneData() - clone content modified - " + clone);

						//yep, the clone content has changed

						if (CPCCorePlugin.isDebugChecking())
						{
							/*
							 * Lets do some extra debug checking here.
							 * If the clone's modification history doesn't contain any clone diff elements, then
							 * the current content (before the changes made on this position object are applied)
							 * and the original content for this clone should match.
							 */
							ICloneModificationHistoryExtension tmpHistory = (ICloneModificationHistoryExtension) storeProvider
									.getFullCloneObjectExtension(clone, ICloneModificationHistoryExtension.class);
							assert (tmpHistory == null || !tmpHistory.isPartial());

							if (tmpHistory == null || tmpHistory.getCloneDiffs().isEmpty())
							{
								//ok there is no history or it is empty, contents should match
								if (!clone.getOriginalContent().equals(clone.getContent()))
								{
									log.warn("Origin. Content: "
											+ CoreStringUtils.quoteString(clone.getOriginalContent()));
									log.warn("Current Content: " + CoreStringUtils.quoteString(clone.getContent()));
									log.error(
											"extractCloneDiffData() - clone has no modification history and yet orignal and current content do not match - clone: "
													+ clone + ", tmpHistory:" + tmpHistory, new Throwable());
								}
							}
						}

						//set the new content
						((ICreatorClone) clone).setContent(cpcPos.getContent());

						modifiedClones.add(clone);

						//extract the clone diff data from the position
						extractCloneDiffData(cpcPos, clone);
					}
					else
					{
						if (log.isTraceEnabled())
							log.trace("extractCloneData() - clone content unchanged - " + clone);

						//in this case the CPCPosition and IClone content should be equal
						if (!clone.getContent().equals(cpcPos.getContent()))
							log.error(
									"extractCloneData() - content differs even though not  marked as modified - pos content: "
											+ cpcPos.getContent() + ", clone content: " + clone.getContent()
											+ ", clone: " + clone, new Throwable());
					}
				}
				//else the clone remained unchanged

			}
			else
			{
				log.error("extractCloneData() - position of unexpected type in cpc category - " + pos, new Throwable());
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.IProvider#getProviderName()
	 */
	@Override
	public String getProviderName()
	{
		return "CPC Track - CPCPositionUpdateStrategyProvider";
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.IProvider#onLoad()
	 */
	@Override
	public void onLoad()
	{
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.IProvider#onUnload()
	 */
	@Override
	public void onUnload()
	{
	}

	/*
	 * Private Methods.
	 */

	private String getCloneContent(IDocument document, CPCPosition cpcPos)
	{
		try
		{
			return document.get(cpcPos.offset, cpcPos.length);
		}
		catch (BadLocationException e)
		{
			log.error("getCloneConten() - unable to extract clone content from document - " + cpcPos + " - " + e, e);
			return null;
		}
	}

	private void debugValidateClone(CPCPosition cpcPos, IDocument document, String newContent)
	{
		assert (cpcPos != null);

		if (!CPCCorePlugin.isDebugChecking())
			return;

		if (log.isTraceEnabled())
			log.trace("debugValidateClone() - cpcPos: " + cpcPos);

		if (newContent == null)
			try
			{
				newContent = document.get(cpcPos.offset, cpcPos.length);
			}
			catch (BadLocationException e)
			{
				log.error("debugValidateClone() - unable to obtain new clone content - " + e + " - cpcPos: " + cpcPos,
						e);
				return;
			}

		if (newContent == null)
		{
			log.error("debugValidateClone() - unable to obtain new clone content - cpcPos: " + cpcPos, new Throwable());
			return;
		}

		String oldContent = cpcPos.getContent();

		if (oldContent == null)
		{
			log.error("debugValidateClone() - unable to retrieve old clone content - cpcPos: " + cpcPos,
					new Throwable());
			return;
		}

		//ok, we have old and new content, make sure that they are equal
		if (oldContent.equals(newContent))
			//we're done
			return;

		/*
		 * The old and new clone contents do not match.
		 * 
		 * !!! THIS SHOULD NEVER HAPPEN !!!
		 * 
		 * We have some serious problem somewhere.
		 */

		log.warn("debugValidateClone() -  current content: " + CoreStringUtils.quoteString(newContent));
		log.warn("debugValidateClone() - expected content: " + CoreStringUtils.quoteString(oldContent));
		log.fatal("debugValidateClone() - VALIDATION FAILED - cpcPos: " + cpcPos, new Throwable());
	}

	private void storeModifiedClone(CPCPosition cpcPos, IDocument document, String newContent)
	{
		assert (cpcPos != null && document != null);

		if (log.isTraceEnabled())
			log.trace("storeModifiedClone() - cpcPos: " + cpcPos + ", document: " + document + ", newContent: "
					+ CoreStringUtils.quoteString(newContent));

		if (newContent == null)
		{
			try
			{
				newContent = document.get(cpcPos.offset, cpcPos.length);
				if (log.isTraceEnabled())
					log
							.trace("storeModifiedClone() - extracted newContent: "
									+ CoreStringUtils.quoteString(newContent));
			}
			catch (BadLocationException e)
			{
				log.error("storeModifiedClone() - unable to obtain new clone content - " + e + " - cpcPos: " + cpcPos,
						e);
				return;
			}
		}

		if (newContent == null)
		{
			log.error("storeModifiedClone() - unable to obtain new clone content - cpcPos: " + cpcPos, new Throwable());
			return;
		}

		//set the new clone content
		cpcPos.setContent(newContent);
	}

	/**
	 * Takes a {@link CPCPosition} and extracts {@link DocumentEvent}s from it which are then
	 * converted into {@link CloneDiff} objects. These are then added to the clone diff result map. 
	 */
	private void extractCloneDiffData(CPCPosition cpcPos, IClone clone)
	{
		if (log.isTraceEnabled())
			log.trace("extractCloneDiffData() - cpcPos: " + cpcPos + ", clone: " + clone);

		if (cpcPos.contentModifyingDocumentEvents == null && cpcPos.contentModifyingDocumentEvents.isEmpty())
		{
			//nothing to do
			log.trace("extractCloneDiffData() - no stored document events for this position, nothing to do.");
			return;
		}

		//we'll be needing the username of the current user
		String creator = CoreUtils.getUsername();

		//get the clones modification history extension or add it, if it doesn't exist yet.
		ICloneModificationHistoryExtension history = (ICloneModificationHistoryExtension) clone
				.getExtension(ICloneModificationHistoryExtension.class);
		if (history == null)
		{
			log
					.trace("extractCloneDiffData() - clone did not yet contain a modification history extension, creating/adding a new one.");

			//if debug checking is enabled
			if (CPCCorePlugin.isDebugChecking())
			{
				//double check, that we really can't get a history extension for this clone
				ICloneModificationHistoryExtension tmpHistory = (ICloneModificationHistoryExtension) storeProvider
						.getFullCloneObjectExtension(clone, ICloneModificationHistoryExtension.class);

				if (tmpHistory != null)
				{
					//this shouldn't happen. The clone should have had that extension to begin with.
					log
							.error(
									"extractCloneDiffData() - clone has no modification history and yet we were able to retrieve one from the store provider - clone: "
											+ clone
											+ ", extensions: "
											+ clone.getExtensions()
											+ ", tmpHistory: "
											+ tmpHistory, new Throwable());
				}
			}

			history = (ICloneModificationHistoryExtension) cloneFactoryProvider
					.getInstance(ICloneModificationHistoryExtension.class);
			clone.addExtension(history);
		}
		else
		{
			if (log.isTraceEnabled())
				log.trace("extractCloneDiffData() - clone already has a modification history extension - history: "
						+ history);
		}

		//optional debug checking
		ICloneModificationHistoryExtension debugHistory = history;
		if (log.isTraceEnabled() && CPCCorePlugin.isDebugChecking())
		{
			/*
			 * We're going to double check our generated diff events later.
			 * However, in order to do that we need to make sure that we have all history data loaded.
			 */
			if (debugHistory.isPartial())
			{
				debugHistory = (ICloneModificationHistoryExtension) storeProvider.getFullCloneObjectExtension(clone,
						ICloneModificationHistoryExtension.class);

				log.trace("extractCloneDiffData() - retrieved full debug history - debugHistory: " + debugHistory);
			}
		}

		//convert each document event
		CPCDocumentEvent lastEvent = null;
		int len, lastLen;
		CloneDiff newDiff;

		for (CPCDocumentEvent event : cpcPos.contentModifyingDocumentEvents)
		{
			if (log.isTraceEnabled())
				log.trace("extractCloneDiffData() - event - off: " + event.fOffset + ", len: " + event.fLength
						+ ", text: " + CoreStringUtils.quoteString(event.fText));

			//We always need to look at two adjacent events at the same time
			//because we might need to merge some of the events.
			if (lastEvent == null)
			{
				lastEvent = event;
				continue;
			}

			len = (event.fText == null ? 0 : event.fText.length());
			lastLen = (lastEvent.fText == null ? 0 : lastEvent.fText.length());

			//lets compare event and lastEvent and decide whether they can be merged
			//first look at additions
			if (len > 0 && event.fLength == 0 && lastLen > 0 && lastEvent.fLength == 0
					&& lastEvent.fOffset + lastLen == event.fOffset)
			{
				//the two events are located side by side and can be merged
				//update lastEvent to represent a merged version

				if (log.isTraceEnabled())
					log.trace("extractCloneDiffData() - merging ADD events - last off: " + lastEvent.fOffset
							+ ", len: " + lastEvent.fLength + ", text: " + CoreStringUtils.quoteString(lastEvent.fText)
							+ " - curr off: " + event.fOffset + ", len: " + event.fLength + ", text: "
							+ CoreStringUtils.quoteString(event.fText));

				lastEvent.fText = lastEvent.fText + event.fText;

				if (log.isTraceEnabled())
					log.trace("extractCloneDiffData() - new lastEvent - off: " + lastEvent.fOffset + ", len: "
							+ lastEvent.fLength + ", text: " + CoreStringUtils.quoteString(lastEvent.fText));

				//continue with the next event, we might need to merge a lot of them
				continue;
			}
			//otherwise look at removals
			else if (len == 0 && event.fLength > 0 && lastLen == 0 && lastEvent.fLength > 0
					&& (lastEvent.fOffset == event.fOffset + event.fLength || lastEvent.fOffset == event.fOffset))
			{
				//the two removal events are side by side and can be merged

				if (log.isTraceEnabled())
					log.trace("extractCloneDiffData() - merging DEL events - last off: " + lastEvent.fOffset
							+ ", len: " + lastEvent.fLength + ", text: " + CoreStringUtils.quoteString(lastEvent.fText)
							+ " - curr off: " + event.fOffset + ", len: " + event.fLength + ", text: "
							+ CoreStringUtils.quoteString(event.fText));

				lastEvent.fOffset = Math.min(event.fOffset, lastEvent.fOffset);
				lastEvent.fLength += event.fLength;

				if (log.isTraceEnabled())
					log.trace("extractCloneDiffData() - new lastEvent - off: " + lastEvent.fOffset + ", len: "
							+ lastEvent.fLength + ", text: " + CoreStringUtils.quoteString(lastEvent.fText));

				//continue with the next event, we might need to merge a lot of them
				continue;
			}
			//otherwise we can't merge them
			else
			{
				//it seems as if we can't merge these two events.
				//in that case we convert the lastEvent into a CloneDiff and take the event as our new lastEvent.

				/*
				 * TODO: add support for detection of automated changes here.
				 * Right now we're setting the automaticChange value to false, in all cases.
				 * This will probably need support in CPCPosition & co.
				 */

				newDiff = new CloneDiff(creator, history.getValidCreationDate(), false, lastEvent.fOffset,
						lastEvent.fLength, lastEvent.fText);
				history.addCloneDiff(newDiff);
				if (log.isTraceEnabled() && CPCCorePlugin.isDebugChecking())
				{
					if (history != debugHistory)
						//We only need to do this if debugHistory was initialised with a value
						//that differs from history.
						debugHistory.addCloneDiff(newDiff);
				}

				if (log.isTraceEnabled())
					log.trace("extractCloneDiffData() - new CloneDiff: " + newDiff);

				//the current event is the new lastEvent
				lastEvent = event;
			}

		}

		if (lastEvent != null)
		{
			//the last clone also  needs to be converted
			newDiff = new CloneDiff(creator, history.getValidCreationDate(), false, lastEvent.fOffset,
					lastEvent.fLength, lastEvent.fText);
			history.addCloneDiff(newDiff);
			if (log.isTraceEnabled() && CPCCorePlugin.isDebugChecking())
			{
				if (history != debugHistory)
					//We only need to do this if debugHistory was initialised with a value
					//that differs from history.
					debugHistory.addCloneDiff(newDiff);
			}

			if (log.isTraceEnabled())
				log.trace("extractCloneDiffData() - new CloneDiff: " + newDiff);
		}

		//remove the DocumentEvents from the position
		cpcPos.contentModifyingDocumentEvents.clear();

		//we're done
		if (log.isTraceEnabled())
			log.trace("extractCloneDiffData() - now " + history.getCloneDiffs().size() + " CloneDiffs in history - "
					+ history);

		//optional debug checking
		if (log.isTraceEnabled() && CPCCorePlugin.isDebugChecking())
		{
			/*
			 * At this point a complete replay of all modification history entries should
			 * result in a content which matches the current content of the clone instance.
			 */
			try
			{
				IClone debugClone = (IClone) clone.clone();
				debugClone.addExtension(debugHistory);
				String replayContent = CoreHistoryUtils.getCloneContentForDate(storeProvider, debugClone, history
						.getValidCreationDate(), false);

				//now compare the contents
				if (replayContent == null || !replayContent.equals(clone.getContent()))
				{
					//oh oh x_X
					log.warn("Current Content: " + CoreStringUtils.quoteString(clone.getContent()));
					log.warn("Diff Content   : " + CoreStringUtils.quoteString(replayContent));
					log.warn("History (Full/Debug): " + debugHistory);
					log.warn("History (Part/Real) : " + history);
					log.fatal("extractCloneDiffData() - diff history does not match current clone content - clone: "
							+ clone, new Throwable());
				}
				else
					log.trace("extractCloneDiffData() - diff debug validation successful.");
			}
			catch (CloneNotSupportedException e)
			{
				log.error("extractCloneDiffData() - unable to clone clone instance: " + clone + " - " + e, e);
			}
		}
	}
}

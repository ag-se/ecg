package org.electrocodeogram.cpc.track.listener;


/**
 * NOT IN USE
 * 
 * @author vw
 *
 * @deprecated
 */
@Deprecated
public class CodeDiffListener //implements IEventHubListener
{
	//	private static Log log = LogFactory.getLog(CodeDiffListener.class);
	//
	//	private CloneRepository cloneRepository = null;
	//	private IStoreProvider storeProvider = null;
	//
	//	/**
	//	 * The last paste clone for which we've already seen the diff event.
	//	 * 
	//	 * @see CloneRepository#getLastPasteClone()
	//	 */
	//	private IClone lastHandledPasteClone = null;
	//
	//	public CodeDiffListener()
	//	{
	//		if (log.isTraceEnabled())
	//			log.trace("CodeDiffListener()");
	//
	//		cloneRepository = CPCTrackPlugin.getCloneRepository();
	//		storeProvider = cloneRepository.getStoreProvider();
	//	}
	//
	//	/*
	//	 * (non-Javadoc)
	//	 * @see org.electrocodeogram.cpc.core.api.hub.registry.IEventHubListener#processEvent(org.electrocodeogram.cpc.core.api.hub.event.CPCEvent)
	//	 */
	//	@Override
	//	public void processEvent(CPCEvent event)
	//	{
	//		if (event instanceof EclipseCodeDiffEvent)
	//		{
	//			processCodeDiffEvent((EclipseCodeDiffEvent) event);
	//		}
	//		else
	//		{
	//			log.error("processEvent() - got event of wrong type: " + event);
	//		}
	//	}
	//
	//	protected void processCodeDiffEvent(EclipseCodeDiffEvent event)
	//	{
	//		/*
	//		 * TODO: we'd need UNDO support here.
	//		 * one simple way to implement this would be to keep track of the last X events and check
	//		 * for each new event if it is equal to the inverse of the latest event on such an "undo-queue".
	//		 * If it is, clone data could be restored to the state from the undo-queue and the event could
	//		 * be ignored.
	//		 * 
	//		 * Problems:
	//		 * 	1) the undo event might not be an exact inverse of the original event
	//		 *  2) it is unclear how many events should be stored in the "undo-queue", memory usage?
	//		 *  3) we'd need to make sure that no other plugin has modified the clone data inbetween
	//		 *  	(this is a general problem which every undo support would have though)
	//		 *  4) ...
	//		 *  
	//		 * and handling of refactorings/code reformats & co should be clear first 
	//		 */
	//		if (log.isTraceEnabled())
	//			log.trace("processCodeDiffEvent(): " + event);
	//
	//		//make sure that this is a java file, we're not interested in any other file types
	//		if (!"java".equals((new Path(event.getFilePath())).getFileExtension()))
	//		{
	//			log.trace("processCodeDiffEvent() - ignoring non JAVA file");
	//			return;
	//		}
	//
	//		//get the clone file handle
	//		ICloneFile cloneFile = storeProvider.lookupCloneFileByPath(event.getProject(), event.getFilePath());
	//		if (cloneFile == null)
	//		{
	//			log.fatal("processCodeDiffEvent() - unable to retrieve clone file: " + cloneFile + ", project: "
	//					+ event.getProject() + ", path: " + event.getFilePath());
	//			return;
	//		}
	//
	//		if (log.isTraceEnabled())
	//			log.trace("processCodeDiffEvent() - clone file: " + cloneFile);
	//
	//		//we should always have an editor content
	//		if (event.getEditorContent() == null)
	//		{
	//			log.error("processCodeDiffEvent() - event is  missing the editor content - " + event);
	//
	//			//we can still go on, extracting the content from the current editor buffer or filesystem will work
	//			//in most cases.
	//		}
	//
	//		try
	//		{
	//			//get an exclusive lock on the clone data, we're very likely to modify it
	//			storeProvider.acquireWriteLock();
	//
	//			//get all the clones which might be affected by this change (all clones at or after the line of the change)
	//			List<IClone> clones = storeProvider.getClonesByFile(cloneFile.getUuid(), event.getOffset(), -1);
	//
	//			/*
	//			 * There are 3 types of diff events. Those that only add data, those that only remove data and those
	//			 * that add and remove data at the same time.
	//			 * The added and the removed data can be processed separately.
	//			 */
	//
	//			//we need the start end the end line for the area affected by the diff
	//			int startOffset = event.getOffset();
	//			int endOffset = startOffset + event.getReplacedText().length() - 1;
	//			int addEndOffset = startOffset + event.getAddedText().length() - 1;
	//			//FIXME: test this ^
	//
	//			//calculate some position diffs for our different measures
	//			int charCountDiff = 0;
	//			int nonWsCountDiff = 0;
	//
	//			//data removed
	//			if (event.getReplacedText().length() > 0)
	//			{
	//				//this diff event removed data
	//
	//				//get the corresponding changes in character counts
	//				int nonWsDelCnt = CoreClonePositionUtils.calculateCharCounts(event.getReplacedText());
	//
	//				if (log.isTraceEnabled())
	//					log.trace("processCodeDiffEvent() - diff REMOVED data - text: " + event.getReplacedText()
	//							+ ", chars: " + event.getReplacedText().length() + ", non-ws chars: " + nonWsDelCnt);
	//
	//				//all clones will need to be moved up by the corresponding values
	//				charCountDiff -= event.getReplacedText().length();
	//				nonWsCountDiff -= nonWsDelCnt;
	//			}
	//
	//			//data added
	//			if (event.getAddedText().length() > 0)
	//			{
	//				//this diff event added data
	//
	//				//get the corresponding changes in character counts
	//				int nonWsAddCnt = CoreClonePositionUtils.calculateCharCounts(event.getAddedText());
	//
	//				if (log.isTraceEnabled())
	//					log.trace("processCodeDiffEvent() - diff ADDED data - text: " + event.getAddedText() + ", chars: "
	//							+ event.getAddedText().length() + ", non-ws chars: " + nonWsAddCnt);
	//
	//				//all clones will need to be moved up by the corresponding values
	//				charCountDiff += event.getAddedText().length();
	//				nonWsCountDiff += nonWsAddCnt;
	//			}
	//
	//			if (log.isTraceEnabled())
	//				log.trace("processCodeDiffEvent() - resulting diff - charCountDiff: " + charCountDiff
	//						+ ", nonWsCountDiff: " + nonWsCountDiff);
	//
	//			/*
	//			 * Clones can be affected by a diff in serveral ways:
	//			 * 
	//			 *  a) clones which should be ignored because their position data is already up to date
	//			 * 		NOTE: this is a clone which was just added by a PASTE action. We will still
	//			 * 			  see a code diff event for such a clone. By the time this code is running,
	//			 * 			  the processing of the PASTE event should be finished and the clone data
	//			 * 			  for the new clone should therefore already be part of the clone list we just
	//			 * 			  retrieved. It is important to omit such a clone from any modifications.
	//			 * 		TODO: maybe we should add some explicit checks which throw if we ever get into a situation,
	//			 * 			  where the code diff event is handled before the corresponding PASTE event.
	//			 * 
	//			 * 	b) they are located entirely above this diff event's location
	//			 * 		- no need to modify these at all, they are not returned by the store provider query
	//			 * 
	//			 * 	c) they are located entirely below the diff event's location
	//			 * 		- they need to be shifted down accordingly but their contents are not being modified
	//			 * 
	//			 * 	d) they are directly affected by the diff event, as it is located inside of those clones
	//			 * 		- these events can potentially trigger CPC warnings and the clone data will need
	//			 * 			to be updated in various ways.
	//			 * 
	//			 * NOTE: on diff ranges
	//			 * 		The start and end offset only really differ, if data was deleted. For diffs which only add data
	//			 * 		endOffset==startOffset-1. (note that endOffset is NOT equal to startOffset in this case)
	//			 * 		addEndOffset marks the offset at which the diffs resulting output ends. In some cases
	//			 * 		you'll actually want to check against addEndOffset and not endOffset. Keep that in mind. 
	//			 * 
	//			 * TODO: what about cases where the diff directly touches, but does not enter a clone?
	//			 * 		 should we increase the size of the clone to encompass any new data introduced
	//			 * 		 by the diff? 
	//			 */
	//
	//			//clones which were deleted due to this diff
	//			List<IClone> deletedClones = new LinkedList<IClone>();
	//
	//			//clones for which the position was updated due to this diff,
	//			//also contains modified clones, if their modification lead to any changes in their position
	//			//deleted clones are not included
	//			List<IClone> movedClones = new LinkedList<IClone>();
	//
	//			//clones for which the actual content was modified by the diff
	//			//deleted clones are not included
	//			List<IClone> modifiedClones = new LinkedList<IClone>();
	//
	//			//get the last paste clone from the repository
	//			IClone lastPasteClone = cloneRepository.getLastPasteClone();
	//			if (lastPasteClone != null && lastPasteClone.equals(lastHandledPasteClone))
	//				//we've already handled this clone
	//				lastPasteClone = null;
	//
	//			if (log.isTraceEnabled())
	//				log.trace("processCodeDiffEvent() - lastPasteClone: " + lastPasteClone + ", lastHandledPasteClone: "
	//						+ lastHandledPasteClone);
	//
	//			for (IClone clone : clones)
	//			{
	//				if (log.isTraceEnabled())
	//					log.trace("CLONE: " + clone);
	//
	//				//a) check whether this diff might be the one which let to the creation of this clone
	//				//in that case we have to ignore the diff for this clone.
	//				if (lastPasteClone != null && lastPasteClone.equals(clone))
	//				/*
	//				//start offset matches
	//				(clone.getPosition().getStartOffset() == startOffset)
	//				//end offset matches
	//				&& (clone.getPosition().getEndOffset() == addEndOffset)
	//				//clone is younger than 15 seconds
	//				&& ((now.getTime() - clone.getCreationDate().getTime()) < 15000)
	//				//content matches
	//				&& (event.getAddedText().equals(CloneParseUtils.getTextForClone(cloneFile, clone, event
	//						.getEditorContent())))*/
	//				/*
	//				 * OLD:
	//				 * Start offset and end offsets matched (note: addEndOffset!) and the contents of
	//				 * the diff event and the clone are equal. We therefore assume that this diff event
	//				 * is the one which belongs to the creation of this clone.
	//				 * 
	//				 * NOTE: this approach might break in certain cases. I.e.
	//				 * 	- this diff represents a manual change which was made directly in front of
	//				 * 	  an existing clone but coincidentally the added text equals the content of the clone.
	//				 * However, together with the time constraint the probability for such an event seems to be
	//				 * too small to warrant any additional countermeasures. 
	//				 */
	//				{
	//					/*
	//					 * Ok, this clone equals the last clone that was created by a paste action.
	//					 * This means that the current diff event is the one which created the clone.
	//					 * Thus the clone position itself is already more or less up to date.
	//					 * 
	//					 * The problem here is that there may have been some code reformatting going on
	//					 * between the paste action and the diff event. The offset and length of the clone
	//					 * may therefore have changed!
	//					 */
	//
	//					if (log.isTraceEnabled())
	//						log.trace("processCodeDiffEvent() - DIFF EQUALS CLONE - ignoring");
	//
	//					/*
	//					 * We assume that this diff was exclusively generated by the paste action
	//					 * which created the clone. As such the offset and length of the inserted
	//					 * data should represent the offset and length of the clone.
	//					 * If there is any discrepancy, it should only be caused by some special,
	//					 * automated modifications of the pasted clone. I.e. a source reformat.
	//					 * 
	//					 * The diff position therefore overrides the clone position, if they differ.
	//					 * 
	//					 * NOTE: this has some consequences, i.e. the resulting modifications to the
	//					 * 		clone may change its similarity to other members of the clone group.
	//					 * 		Especially if eclipse source reformats are applied in their entirety
	//					 * 		on paste actions (see note below). 
	//					 */
	//					IClonePosition pos = clone.getPosition();
	//
	//					if (startOffset != pos.getStartOffset() || addEndOffset != pos.getEndOffset())
	//					{
	//						if (log.isDebugEnabled())
	//							log
	//									.debug("processCodeDiffEvent() - clone and diff POSITIONS DIFFER - using diff position - diff pos: "
	//											+ startOffset
	//											+ "-"
	//											+ addEndOffset
	//											+ ", clone pos: "
	//											+ pos.getStartOffset()
	//											+ "-" + pos.getEndOffset());
	//
	//						String content = null;
	//
	//						//now update the position
	//						IClonePosition newPos = CoreClonePositionUtils.extractPosition(CPCTrackPlugin
	//								.getCloneRepository().getCloneFactory(), startOffset, addEndOffset, event
	//								.getEditorContent());
	//						clone.setPosition(newPos);
	//
	//						movedClones.add(clone);
	//
	//						if (log.isTraceEnabled())
	//						{
	//							log.trace("processCodeDiffEvent() - new position: " + newPos);
	//							content = CloneParseUtils.getTextForClone(cloneFile, clone, event.getEditorContent());
	//							log.trace("  CONTENT: " + content);
	//						}
	//
	//						//we do not add this clone to the modified clones list, as this modification
	//						//is not a "real" modification.
	//
	//						//TODO: verify here that the difference between the old clone content and the new
	//						//		clone content is only a difference caused by a source reformat.
	//						//		This maybe next to impossible to implement though, considering the large
	//						//		amount of code reformattings, which eclipse supports.
	//						//		Though those might only be applied on manual reformat or by the reformat Save Action.
	//						//		Reformat on paste might be limited to indentation changes, TODO: verify this
	//
	//						//store new clone content for future checks
	//						storeModifiedClone(cloneFile, clone, event.getEditorContent(), content);
	//
	//					}
	//					else
	//					{
	//						//clone content should match
	//						debugValidateClone(cloneFile, clone, event.getEditorContent(), null);
	//					}
	//
	//					//this paste clone has now been "handled" (we've processed it's diff event)
	//					lastHandledPasteClone = clone;
	//
	//					continue;
	//				}
	//
	//				//b) entire clone located above the diff
	//				else if (clone.getPosition().getEndOffset() < startOffset)
	//				{
	//					if (log.isTraceEnabled())
	//						log.trace("processCodeDiffEvent() - skipping clone ABOVE diff - clone: " + clone);
	//
	//					//clone content should be unchanged
	//					debugValidateClone(cloneFile, clone, event.getEditorContent(), null);
	//				}
	//
	//				//c) entire clone located below the diff
	//				else if (endOffset < clone.getPosition().getStartOffset())
	//				{
	//					if (log.isTraceEnabled())
	//						log.trace("processCodeDiffEvent() - moving clone BELOW diff - clone: " + clone);
	//
	//					//make sure the position really changed
	//					if ((charCountDiff != 0) || (nonWsCountDiff != 0))
	//					{
	//						//we'll need to move the entire clone down or up according to the diff counts
	//						IClonePosition pos = clone.getPosition();
	//
	//						pos.setStartOffset(pos.getStartOffset() + charCountDiff);
	//						pos.setEndOffset(pos.getEndOffset() + charCountDiff);
	//
	//						pos.setStartNonWsOffset(pos.getStartNonWsOffset() + nonWsCountDiff);
	//						pos.setEndNonWsOffset(pos.getEndNonWsOffset() + nonWsCountDiff);
	//
	//						movedClones.add(clone);
	//
	//						String content = null;
	//						if (log.isTraceEnabled())
	//						{
	//							log.trace("processCodeDiffEvent() - new position: " + pos);
	//							content = CloneParseUtils.getTextForClone(cloneFile, clone, event.getEditorContent());
	//							log.trace("  CONTENT: " + content);
	//						}
	//
	//						//clone content should match
	//						debugValidateClone(cloneFile, clone, event.getEditorContent(), content);
	//					}
	//					else
	//					{
	//						if (log.isTraceEnabled())
	//							log.trace("processCodeDiffEvent() - position remained unchanged by diff");
	//					}
	//
	//				}
	//
	//				//d) diff is at least partly located within the clone 
	//				else
	//				{
	//					if (log.isTraceEnabled())
	//						log.trace("processCodeDiffEvent() - CLONE MODIFIED - clone: " + clone);
	//
	//					/*
	//					 * possible cases:
	//					 *  1) the diff matches exactly with the clone offsets
	//					 *      -> the clone was removed
	//					 * 	2) the diff is located completely within the clone
	//					 * 		-> we just need to move the end marker
	//					 * 	3) the diff exceeds start AND end position (the diff is bigger than the clone)
	//					 * 		-> the entire clone was deleted
	//					 * 	4) the diff exceeds only the start position OR only the end position 
	//					 * 		-> at least part of the clone was deleted -> shrink the clone accordingly
	//					 * 		-> then maybe extend it again using any newly added stuff?
	//					 */
	//
	//					//1) the diff matches exactly with the clone offsets
	//					if ((clone.getPosition().getStartOffset() == startOffset)
	//							&& (clone.getPosition().getEndOffset() == endOffset))
	//					{
	//						log.trace("processCodeDiffEvent() - DIFF REPLACED CLONE - deleting");
	//
	//						/*
	//						 *  TODO: we might still want to keep the clone if the similarity of
	//						 *  	the replaced clone content and the diff content is high enough.
	//						 *  	It's not clear whether that would be useful though. Because complete replacement
	//						 *  	does imply removal of all earlier copy and pasted source.
	//						 */
	//
	//						//the clone was deleted
	//						deletedClones.add(clone);
	//
	//						log.trace("processCodeDiffEvent() - clone DELETED");
	//
	//					}
	//
	//					//2) diff located completely within the clone
	//					else if ((clone.getPosition().getStartOffset() <= startOffset)
	//							&& (endOffset <= clone.getPosition().getEndOffset()))
	//					{
	//						log.trace("processCodeDiffEvent() - diff WITHIN CLONE");
	//
	//						//we only need to move the end position, if the clone has grown or shrunk
	//						//due to the update
	//
	//						String content = null;
	//
	//						//make sure the position really changed
	//						if ((charCountDiff != 0) || (nonWsCountDiff != 0))
	//						{
	//							IClonePosition pos = clone.getPosition();
	//							pos.setEndOffset(pos.getEndOffset() + charCountDiff);
	//							pos.setEndNonWsOffset(pos.getEndNonWsOffset() + nonWsCountDiff);
	//
	//							movedClones.add(clone);
	//
	//							if (log.isTraceEnabled())
	//							{
	//								log.trace("processCodeDiffEvent() - new position: " + pos);
	//								content = CloneParseUtils.getTextForClone(cloneFile, clone, event.getEditorContent());
	//								log.trace("  CONTENT: " + content);
	//							}
	//						}
	//
	//						//but it was definitely modified -> a CPC warning may be triggered
	//						modifiedClones.add(clone);
	//
	//						//store new clone content for future checks
	//						storeModifiedClone(cloneFile, clone, event.getEditorContent(), content);
	//					}
	//
	//					//3) the diff exceeds start AND end position (the diff is bigger than the clone)
	//					else if (startOffset <= (clone.getPosition().getStartOffset())
	//							&& (clone.getPosition().getEndOffset() <= endOffset))
	//					{
	//						log.trace("processCodeDiffEvent() - diff OVER CLONE");
	//
	//						//the clone was deleted
	//						deletedClones.add(clone);
	//
	//						log.trace("processCodeDiffEvent() - clone DELETED");
	//					}
	//
	//					//4) the diff exceeds only the start position OR only the end position
	//					else
	//					{
	//						log.trace("processCodeDiffEvent() - diff TOUCHES CLONE");
	//						if (log.isTraceEnabled())
	//							log.trace("processCodeDiffEvent() - offsets - dsoff: " + startOffset + ", deoff: "
	//									+ endOffset + ", daoff: " + addEndOffset + ", csoff: "
	//									+ clone.getPosition().getStartOffset() + ", ceoff: "
	//									+ clone.getPosition().getEndOffset());
	//
	//						String content = null;
	//
	//						/*
	//						 * TODO: it is not clear what should be done here, we have multiple options
	//						 * 
	//						 * 	a) shrink the clone accordingly, if the diff deleted any data, treat any
	//						 * 		newly added data as not belonging to the clone
	//						 * 		i.e.
	//						 * 			a b <diff> c <clone> d </diff> e f </clone> g h
	//						 * 			with diff inserting "1 2 3"
	//						 * 		=>
	//						 * 			a b 1 2 3 <clone> e f </clone> g h
	//						 * 	b) first shrink the clone according to the removed data then expand it
	//						 * 		according to the added data
	//						 * 		i.e.
	//						 * 			a b <diff> c <clone> d </diff> e f </clone> g h
	//						 * 			with diff inserting "1 2 3"
	//						 * 		=>
	//						 * 			a b <clone> 1 2 3 e f </clone> g h
	//						 * 	c) if data is removed and added, then only take the part into account which
	//						 * 		was overlapping with the clone.
	//						 * 		i.e.
	//						 * 			a b <diff> c <clone> d </diff> e f </clone> g h
	//						 * 			with diff inserting "1 2 3"
	//						 * 		=>
	//						 * 			a b 1 2 <clone> 3 e f </clone> g h
	//						 * 
	//						 * The choice could also be made dynamically, i.e. do b) if the clone would grow by
	//						 * less than X characters that way, otherwise use a) or c). If b) is deemed to be an
	//						 * interesting option, this approach may be required to prevent i.e. large code paste
	//						 * actions from bloating up any clone which they may only have slightly touched. 
	//						 */
	//
	//						//TODO: for simplicity reasons we implement only a) for now
	//						//start of clone was touched
	//						if ((startOffset <= clone.getPosition().getStartOffset())
	//								&& (clone.getPosition().getStartOffset() <= endOffset))
	//						{
	//							log.trace("processCodeDiffEvent() - diff partly touches clone HEAD");
	//
	//							//shrink clone by the number of characters removed, which are within the clones offset range
	//							int charsRemoved = endOffset - clone.getPosition().getStartOffset() + 1;
	//							//TODO: check if this ^ is ok
	//
	//							//get the number of nonWs chars in that range
	//							String stringRemoved = event.getReplacedText().substring(
	//									event.getReplacedText().length() - charsRemoved);
	//							int nonWsRemoved = CoreClonePositionUtils.calculateCharCounts(stringRemoved);
	//
	//							if (log.isTraceEnabled())
	//								log.trace("processCodeDiffEvent() - stringRemoved: " + stringRemoved
	//										+ ", charsRemoved: " + charsRemoved + ", nonWsRemoved: " + nonWsRemoved);
	//
	//							//move the start position accordingly.
	//							//the clones is moved as with any other move, however, at the same time it shrinks by
	//							//charsRemoved characters
	//							IClonePosition pos = clone.getPosition();
	//							pos.setStartOffset(pos.getStartOffset() + charCountDiff + charsRemoved);
	//							pos.setStartNonWsOffset(pos.getStartNonWsOffset() + nonWsCountDiff + nonWsRemoved);
	//							//TODO: test this ^
	//
	//							//the end position also moves, but only as with any other move
	//							pos.setEndOffset(pos.getEndOffset() + charCountDiff);
	//							pos.setEndNonWsOffset(pos.getEndNonWsOffset() + nonWsCountDiff);
	//
	//							movedClones.add(clone);
	//
	//							if (log.isTraceEnabled())
	//							{
	//								log.trace("processCodeDiffEvent() - new position: " + pos);
	//								content = CloneParseUtils.getTextForClone(cloneFile, clone, event.getEditorContent());
	//								log.trace("  CONTENT: " + content);
	//							}
	//						}
	//						//end of clone was touched
	//						else if ((startOffset <= clone.getPosition().getEndOffset())
	//								&& (clone.getPosition().getEndOffset() <= endOffset))
	//						{
	//							log.trace("processCodeDiffEvent() - diff partly touches clone TAIL");
	//
	//							//shrink clone by the number of characters removed, which are within the clones offset range
	//							int charsRemoved = clone.getPosition().getEndOffset() - startOffset + 1;
	//							//TODO: check if this ^ is ok?
	//
	//							//get the number of nonWs chars in that range
	//							String stringRemoved = event.getReplacedText().substring(0, charsRemoved);
	//							int nonWsRemoved = CoreClonePositionUtils.calculateCharCounts(stringRemoved);
	//
	//							if (log.isTraceEnabled())
	//								log.trace("processCodeDiffEvent() - stringRemoved: " + stringRemoved
	//										+ ", charsRemoved: " + charsRemoved + ", nonWsRemoved: " + nonWsRemoved);
	//
	//							//move the end position accordingly.
	//							IClonePosition pos = clone.getPosition();
	//							pos.setEndOffset(pos.getEndOffset() - charsRemoved);
	//							pos.setEndNonWsOffset(pos.getEndNonWsOffset() - nonWsRemoved);
	//
	//							movedClones.add(clone);
	//
	//							if (log.isTraceEnabled())
	//							{
	//								log.trace("processCodeDiffEvent() - new position: " + pos);
	//								content = CloneParseUtils.getTextForClone(cloneFile, clone, event.getEditorContent());
	//								log.trace("  CONTENT: " + content);
	//							}
	//						}
	//						else
	//						{
	//							//this shouldn't happen
	//							log
	//									.fatal("INTERNAL ERROR: processCodeDiffEvent() - unable to identify diff position in relation to clone data - dsoff: "
	//											+ startOffset
	//											+ ", deoff: "
	//											+ endOffset
	//											+ ", daoff: "
	//											+ addEndOffset
	//											+ ", csoff: "
	//											+ clone.getPosition().getStartOffset()
	//											+ ", ceoff: "
	//											+ clone.getPosition().getEndOffset()
	//											+ ", clone: "
	//											+ clone
	//											+ ", diffEvent: " + event);
	//						}
	//
	//						//clone was definitely modified
	//						modifiedClones.add(clone);
	//
	//						//store new clone content for debug checks
	//						storeModifiedClone(cloneFile, clone, event.getEditorContent(), content);
	//					}
	//
	//				}
	//			}
	//
	//			//check if we actually need to update anything, maybe this diff didn't lead to any changes
	//			if ((!movedClones.isEmpty()) || (!modifiedClones.isEmpty()) || (!deletedClones.isEmpty()))
	//			{
	//				//now update the stored clone data
	//				log.trace("processCodeDiffEvent() - updating stored clone data");
	//				if (!deletedClones.isEmpty())
	//					storeProvider.removeClones(deletedClones);
	//				if (!movedClones.isEmpty())
	//					storeProvider.updateClones(movedClones);
	//				if (!modifiedClones.isEmpty())
	//					storeProvider.updateClones(modifiedClones);
	//
	//				//and notify any interested parties about deleted/moved/modified clones
	//				log.trace("processCodeDiffEvent() - notifying other parties about clone changes");
	//				CloneModificationEvent newEvent = new CloneModificationEvent(cloneFile);
	//
	//				if (!movedClones.isEmpty())
	//					newEvent.setMovedClones(movedClones);
	//				if (!modifiedClones.isEmpty())
	//					newEvent.setModifiedClones(modifiedClones);
	//				if (!deletedClones.isEmpty())
	//					newEvent.setRemovedClones(deletedClones);
	//
	//				CPCCorePlugin.getEventHubRegistry().dispatch(newEvent);
	//			}
	//			else
	//			{
	//				log
	//						.trace("processCodeDiffEvent() - no changes were made - NOT updating stored data or sending out notifications");
	//			}
	//		}
	//		catch (StoreLockingException e)
	//		{
	//			log.error("ERROR: processCodeDiffEvent() - unexpected store locking exception - event: " + event + " - "
	//					+ e);
	//		}
	//		finally
	//		{
	//			//make sure we release the exclusive lock again
	//			storeProvider.releaseWriteLock();
	//		}
	//	}
	//
	//	/**
	//	 * Compares the current content of a clone with the last known old content.
	//	 * This method should only be called if a clone was simply moved and not if the clone contents have
	//	 * been modified.
	//	 * 
	//	 * @param cloneFile the file the clone is located in, never null
	//	 * @param clone the clone in question, never null
	//	 * @param newContent the new content of the clone, may be NULL, in which case the data is extracted from the file
	//	 */
	//	private void debugValidateClone(ICloneFile cloneFile, IClone clone, String editorContent, String newContent)
	//			throws StoreLockingException
	//	{
	//		assert (cloneFile != null && clone != null);
	//
	//		if (!CPCTrackPlugin.getDefault().isDebugChecking())
	//			return;
	//
	//		if (log.isTraceEnabled())
	//			log.trace("debugValidateClone() - cloneFile: " + cloneFile + ", clone: " + clone + ", newContent: "
	//					+ newContent);
	//
	//		if (newContent == null)
	//			newContent = CloneParseUtils.getTextForClone(cloneFile, clone, editorContent);
	//
	//		if (newContent == null)
	//		{
	//			log.error("debugValidateClone() - unable to obtain new clone content - cloneFile: " + cloneFile
	//					+ ", clone: " + clone);
	//			return;
	//		}
	//
	//		String oldContent = clone.getContent();
	//
	//		if (oldContent == null)
	//		{
	//			log.error("debugValidateClone() - unable to retrieve old clone content - clone: " + clone);
	//			return;
	//		}
	//
	//		//ok, we have old and new content, make sure that they are equal
	//		if (oldContent.equals(newContent))
	//			//we're done
	//			return;
	//
	//		/*
	//		 * The old and new clone contents do not match.
	//		 * 
	//		 * !!! THIS SHOULD NEVER HAPPEN !!!
	//		 * 
	//		 * We have some serious problem somewhere.
	//		 */
	//
	//		log.warn("debugValidateClone() -  current content: \"" + newContent + "\"");
	//		log.warn("debugValidateClone() - expected content: \"" + oldContent + "\"");
	//		log.fatal("debugValidateClone() - VALIDATION FAILED - clone: " + clone);
	//	}
	//
	//	/**
	//	 * Registers the new content of a clone after a modification with the store provider.
	//	 * 
	//	 * @param cloneFile the file the clone is located in, never null
	//	 * @param clone the clone in question, never null
	//	 * @param newContent the new content of the clone, may be NULL, in which case the data is extracted from the file
	//	 */
	//	private void storeModifiedClone(ICloneFile cloneFile, IClone clone, String editorContent, String newContent)
	//			throws StoreLockingException
	//	{
	//		assert (cloneFile != null && clone != null);
	//
	//		if (log.isTraceEnabled())
	//			log.trace("storeModifiedClone() - cloneFile: " + cloneFile + ", clone: " + clone + ", newContent: "
	//					+ newContent);
	//
	//		if (newContent == null)
	//			newContent = CloneParseUtils.getTextForClone(cloneFile, clone, editorContent);
	//
	//		if (newContent == null)
	//		{
	//			log.error("storeModifiedClone() - unable to obtain new clone content - cloneFile: " + cloneFile
	//					+ ", clone: " + clone);
	//			return;
	//		}
	//
	//		//set the new clone content
	//		((ICreatorClone) clone).setContent(newContent);
	//	}
}

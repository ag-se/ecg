package org.electrocodeogram.cpc.track.listener;


import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.DocumentEvent;
import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.data.ICloneFile;
import org.electrocodeogram.cpc.core.api.data.ICloneGroup;
import org.electrocodeogram.cpc.core.api.data.special.ICreatorClone;
import org.electrocodeogram.cpc.core.api.hub.event.CPCEvent;
import org.electrocodeogram.cpc.core.api.hub.event.CloneModificationEvent;
import org.electrocodeogram.cpc.core.api.hub.event.EclipseCutCopyPasteEvent;
import org.electrocodeogram.cpc.core.api.hub.registry.IEventHubListener;
import org.electrocodeogram.cpc.core.api.provider.classification.IClassificationProvider;
import org.electrocodeogram.cpc.core.api.provider.similarity.ISimilarityProvider;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider;
import org.electrocodeogram.cpc.core.api.provider.store.StoreLockingException;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider.LockMode;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider.UpdateMode;
import org.electrocodeogram.cpc.core.utils.CoreStringUtils;
import org.electrocodeogram.cpc.core.utils.CoreUtils;
import org.electrocodeogram.cpc.track.CPCTrackPlugin;
import org.electrocodeogram.cpc.track.repository.CloneRepository;


/**
 * Listens for {@link EclipseCutCopyPasteEvent}s and creates new {@link IClone} entries in
 * situations where a paste action is likely to have introduced a new clone.
 * 
 * @author vw
 */
public class CutCopyPasteListener implements IEventHubListener
{
	private static Log log = LogFactory.getLog(CutCopyPasteListener.class);

	/**
	 * If the last text action we've seen was a CUT or a COPY, we keep track of
	 * the origin of the current clipboard content here.<br/>
	 * The instance referenced here may not be a real clone yet as it may be the only
	 * member of a clone group. I.e. we've only seen a COPY but never a PASTE.<br/>
	 * If the source of the clipboard content was already a clone then the clone group
	 * membership is set accordingly for this reference.<br/>
	 * If the selection during a COPY/CUT action EXACTLY matched a clone, than this is actually a
	 * reference to that clone instance. If there are any derivations, a new clone instance is
	 * used. 
	 */
	private IClone originOfClipboard = null;

	/**
	 * The content of the clipboard which corresponds to the <em>originOfClipboard</em> source.<br/>
	 * If the content changes without a copy or cut event the clipboard has most likely been modified
	 * by actions in external applications.
	 */
	private String clipboardContent = null;

	/**
	 * This is true, if source was CUT from it's original location. In this case <em>originOfClipboard</em>
	 * will be NULL.
	 */
	private boolean existsOnlyInClipboard = false;

	private CloneRepository cloneRepository = null;
	private IStoreProvider storeProvider = null;
	private IClassificationProvider classificationProvider = null;
	private ISimilarityProvider similarityProvider = null;

	public CutCopyPasteListener()
	{
		log.trace("CutCopyPasteListener()");

		cloneRepository = CPCTrackPlugin.getCloneRepository();
		storeProvider = cloneRepository.getStoreProvider();
		classificationProvider = cloneRepository.getClassificationProvider();
		similarityProvider = cloneRepository.getSimiliarityProvider();
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.hub.registry.IEventHubListener#processEvent(org.electrocodeogram.cpc.core.api.hub.event.CPCEvent)
	 */
	@Override
	public void processEvent(CPCEvent event)
	{
		if (event instanceof EclipseCutCopyPasteEvent)
		{
			processEclipseCutCopyPasteEvent((EclipseCutCopyPasteEvent) event);
		}
		else if (event instanceof CloneModificationEvent)
		{
			processCloneModificationEvent((CloneModificationEvent) event);
		}
		else
		{
			log.error("processEvent() - got event of wrong type: " + event, new Throwable());
		}
	}

	/**
	 * Checks each <em>CloneModificationEvent</em> to see whether our originOfClipboard
	 * might have been removed. 
	 */
	protected void processCloneModificationEvent(CloneModificationEvent event)
	{
		if (log.isTraceEnabled())
			log.trace("processCloneModificationEvent(): " + event);

		//if we don't have any originOfClipboard, we ignore all such events
		if (originOfClipboard == null)
		{
			log.trace("processCloneModificationEvent() - currently no originOfClipboard, ignoring event.");
			return;
		}

		//check if this is a general clear-all-event for a specific file or for all files
		if (event.isFullModification())
		{
			log
					.trace("processCloneModificationEvent() - event is a full modification event, updating originOfClipboard.");

			//Ok, our origin of clipboard might have been removed or modified.
			//Obtain a new copy from the store provider (might be null)
			IClone newOriginOfClipboard = storeProvider.lookupClone(originOfClipboard.getUuid());

			if (log.isTraceEnabled())
				log.trace("processCloneModificationEvent() - old originOfClipboard: " + originOfClipboard
						+ ", new originOfClipboard: " + newOriginOfClipboard);

			originOfClipboard = newOriginOfClipboard;
		}
		else
		{
			//we're only interested in events with removed clone payload
			if (event.getRemovedClones() == null)
			{
				log.trace("processCloneModificationEvent() - event has no removed clones payload, ignoring event.");
				return;
			}

			for (IClone clone : event.getRemovedClones())
			{
				//check if this clone matches our originOfClipboard
				if (originOfClipboard.equals(clone))
				{
					//ok, our origin of clipboard was indeed removed!				
					if (log.isDebugEnabled())
						log
								.debug("processCloneModificationEvent() - originOfClipboard was removed by event - originOfClipboard: "
										+ originOfClipboard + ", event: " + event);

					//reset originOfClipboard
					originOfClipboard = null;

					//the clipboard content now only exists inside the clipboard
					existsOnlyInClipboard = true;

					//we're done now
					return;
				}
			}
		}

		log.trace("processCloneModificationEvent() - originOfClipboard was not affected by event");
	}

	/**
	 * Processes each Cut/Copy/Paste event and creates new clone entries as needed.
	 */
	protected void processEclipseCutCopyPasteEvent(EclipseCutCopyPasteEvent event)
	{
		if (log.isTraceEnabled())
			log.trace("processEclipseCutCopyPasteEvent(): " + event);

		//make sure that this is a java file, we're not interested in any other file types
		if (!"java".equals((new Path(event.getFilePath())).getFileExtension()))
		{
			log.trace("processEclipseCutCopyPasteEvent() - ignoring non JAVA file");
			return;
		}

		//we ignore empty COPY and CUT events
		if ((event.getType().equals(EclipseCutCopyPasteEvent.Type.COPY) || event.getType().equals(
				EclipseCutCopyPasteEvent.Type.CUT))
				&& event.getSelection().length() == 0)
		{
			log.warn("processEclipseCutCopyPasteEvent() - empty selection in copy/cut event, ignoring - event: "
					+ event);
			return;
		}

		//get the clone file handle
		ICloneFile cloneFile = storeProvider.lookupCloneFileByPath(event.getProject(), event.getFilePath(), true, true);

		/*
		 * Our origin of clipboard might have been modified in the mean time.
		 * To ensure that any such modification is written back to the store provider
		 * and that any pending notifications are sent, we acquire a temporary write lock here
		 */
		try
		{
			storeProvider.acquireWriteLock(LockMode.DEFAULT);
		}
		catch (StoreLockingException e)
		{
			log.error("processEclipseCutCopyPasteEvent() - locking error - " + e, e);
		}
		finally
		{
			storeProvider.releaseWriteLock();
		}

		//check if we need to update our stored clipboard data or whether a clone was created
		if (EclipseCutCopyPasteEvent.Type.COPY.equals(event.getType()))
		{
			//COPY event, new data has been written to the clipboard

			if (log.isTraceEnabled())
				log.trace("processEclipseCutCopyPasteEvent(): COPY detected");

			//clear any old transient clone data which we might have created during an earlier action
			clearTransientClone(originOfClipboard);

			//extract the position information
			PositionData positionData = extractPosition(event, true);
			if (positionData == null)
			{
				log.fatal("processEclipseCutCopyPasteEvent() - failed to obtain clone position (copy) - " + event,
						new Throwable());

				return;
			}

			//remember what is currently in the clipboard (to detect external modifications)
			clipboardContent = event.getSelection();

			//debug check, the content of the clone should match the clipboard content
			if ((clipboardContent == null)
					|| (!CoreUtils.equalIgnoringLineBreak(clipboardContent, positionData.cloneContent)))
			{
				log.error(
						"processEclipseCutCopyPasteEvent() - COPY - clipboard content does not match newly created clone content - clipboard: "
								+ clipboardContent + ", clone: " + positionData.cloneContent, new Throwable());
				return;
			}

			//check if we've made a copy of an existing clone
			originOfClipboard = cloneRepository.findClone(cloneFile, positionData.offset, positionData.length, event
					.getEditorContent());
			if (originOfClipboard == null)
			{
				//nope, we need to create a new clone object
				boolean cloneCreated = createTransientClone(cloneFile, positionData, event.getUser(), event
						.getEditorContent(), false);
				if (!cloneCreated)
					//the classification provider rejected this clone, ignore the COPY action
					return;
			}

			//this clone has a file based source (as opposed to a CUT action)
			existsOnlyInClipboard = false;
		}
		else if (EclipseCutCopyPasteEvent.Type.CUT.equals(event.getType()))
		{
			//CUT event, new data has been written to the clipboard
			//however there is no source for this clipboard content

			if (log.isTraceEnabled())
				log.trace("processEclipseCutCopyPasteEvent(): CUT detected");

			//clear any old transient clone data which we might have created during an earlier action
			clearTransientClone(originOfClipboard);

			//a cut is by default not associated with any existing element
			originOfClipboard = null;

			//remember what is currently in the clipboard (to detect external modifications)
			clipboardContent = event.getSelection();

			//this clone does not have a file based source (as opposed to a COPY action)
			existsOnlyInClipboard = true;

			/*
			 * However, there is a special case.
			 * If we're cutting out an existing clone AND if that clone belongs to a
			 * clone group with other clones, then we should preserve the link to that group.
			 * We achieve this by specifying one of the other group members of the clone
			 * as origin of clipboard.
			 * 
			 * First check if this is the case.
			 * 
			 * TODO: supporting this is probably a good idea, however, this approach doesn't work.
			 * By the time a cut event is handled, the underlying clone will already have been
			 * deleted. In order for this to work we'd need to keep some kind of clone deletion queue
			 * or some other way which would allow us to restore (or prevent the deletion in the first place)
			 * the event.
			 */
			//extract the position information
			//			PositionData positionData = extractPosition(event, true);
			//			if (positionData == null)
			//			{
			//				log.fatal("processEclipseCutCopyPasteEvent() - failed to obtain clone position (cut) - " + event,
			//						new Throwable());
			//
			//				return;
			//			}
			//
			//			IClone existingClone = cloneRepository.findClone(cloneFile, positionData.offset, positionData.length);
			//			if (existingClone != null && existingClone.getGroupUuid() != null)
			//			{
			//				//check if it has group members
			//				List<IClone> groupClones = storeProvider.getClonesByGroup(existingClone.getUuid());
			//				if (groupClones.size() > 1)
			//				{
			//					if (log.isTraceEnabled())
			//						log
			//								.trace("processEclipseCutCopyPasteEvent() - cut clone has other group members, trying to extract origin of clipboard: "
			//										+ groupClones);
			//
			//					//ok, we should use one of the other group members as origin of clipboard
			//					for (IClone groupClone : groupClones)
			//					{
			//						if (groupClone.equals(existingClone))
			//							continue;
			//
			//						/*
			//						 * TODO: maybe there is one specific clone which is especially interesting for us?
			//						 * I.e. the origin clone? Or the clone with the highest similarity?
			//						 */
			//						originOfClipboard = groupClone;
			//						existsOnlyInClipboard = false;
			//						break;
			//					}
			//				}
			//			}
		}
		else if (EclipseCutCopyPasteEvent.Type.PASTE.equals(event.getType()))
		{
			//PASTE event, data has been inserted from the clipboard

			if (log.isTraceEnabled())
				log.trace("processEclipseCutCopyPasteEvent(): PASTE detected");

			//first we need to check whether the clipboard data might have been overwritten
			//by some external application
			if ((clipboardContent == null)
					|| (!CoreUtils.equalIgnoringLineBreak(clipboardContent, event.getClipboard())))
			{
				//we either never saw a COPY/CUT event before this paste (null) or the current
				//clipboard content does not match our expectations
				//either way we can assume that the clipboard data comes from an external source
				log
						.info("processEclipseCutCopyPasteEvent(): clipboard data was modified by external application, handling like COPY event.");
				if (log.isDebugEnabled())
				{
					log.debug("processEclipseCutCopyPasteEvent() - expected clipboard content: "
							+ CoreStringUtils.quoteString(clipboardContent));
					log.debug("processEclipseCutCopyPasteEvent() - current clipboard content : "
							+ CoreStringUtils.quoteString(event.getClipboard()));
					if (clipboardContent != null && event.getClipboard() != null)
					{
						log.debug("processEclipseCutCopyPasteEvent() - expected clipboard content [B]: "
								+ CoreUtils.arrayToString(clipboardContent.getBytes()) + "");
						log.debug("processEclipseCutCopyPasteEvent() - current clipboard content  [B]: "
								+ CoreUtils.arrayToString(event.getClipboard().getBytes()) + "");
					}
				}

				//clear any old transient clone data which we might have created during an earlier action
				clearTransientClone(originOfClipboard);

				//starting with the next paste option, we do know this content
				clipboardContent = event.getClipboard();

				//extract the position information
				PositionData positionData = extractPosition(event, false);
				if (positionData == null)
				{
					log.fatal("processEclipseCutCopyPasteEvent() - failed to obtain clone position - " + event,
							new Throwable());

					return;
				}

				//OLD - as we're now updating the document first and then handle the paste event,
				//the content may differ due to source-reformat-on-paste actions by eclipse.
				//				//debug check, the content of the clone should match the clipboard content
				//				if ((clipboardContent == null) || (!clipboardContent.equals(positionData.cloneContent)))
				//				{
				//					log
				//							.error("processEvent() - PASTE - clipboard content does not match newly created clone content - clipboard: "
				//									+ clipboardContent + ", clone: " + positionData.cloneContent);
				//				}

				//create a new clone object
				boolean cloneCreated = createTransientClone(cloneFile, positionData, event.getUser(), event
						.getEditorContent(), true);
				if (!cloneCreated)
					//classification provider rejected the clone, ignore it
					return;

				//this paste location is now a file based source
				existsOnlyInClipboard = false;
			}
			else
			{
				//ok, the clipboard data was unchanged
				//however it might be resulting from a CUT operation, in which case we don't have a clone yet
				if (existsOnlyInClipboard)
				{
					//not a clone yet, we need to update the position here and from now on
					//it is no longer only clipboard

					if (log.isTraceEnabled())
						log.trace("processEclipseCutCopyPasteEvent(): PASTE after CUT, no clone yet, position updated");

					//extract the position information
					PositionData positionData = extractPosition(event, false);
					if (positionData == null)
					{
						log.fatal("processEclipseCutCopyPasteEvent() - failed to obtain clone position - " + event,
								new Throwable());

						return;
					}

					//OLD - as we're now updating the document first and then handle the paste event,
					//the content may differ due to source-reformat-on-paste actions by eclipse.
					//					//debug check, the content of the clone should match the clipboard content
					//					if ((clipboardContent == null) || (!clipboardContent.equals(positionData.cloneContent)))
					//					{
					//						log
					//								.error("processEvent() - PASTE2 - clipboard content does not match newly created clone content - clipboard: "
					//										+ clipboardContent + ", clone: " + positionData.cloneContent);
					//					}

					//create a new transient clone which can act as source for future paste events
					boolean cloneCreated = createTransientClone(cloneFile, positionData, event.getUser(), event
							.getEditorContent(), true);
					if (!cloneCreated)
						//the classification provider rejected this clone
						return;
				}
				else
				{
					/*
					 * !!! CLONE !!!
					 */

					if (log.isDebugEnabled())
						log.debug("processEclipseCutCopyPasteEvent(): PASTE clone detected");

					//keep track of all clones added (maybe be 1 or 2), depending on the transient state
					//of the source clone entry
					List<IClone> addedClones = new LinkedList<IClone>();

					//whether we need to update the origin clone @ store provider
					boolean originModified = false;

					/*
					 * At this point originOfClipboard will point to a Clone instance which describes
					 * the source of this clone. That instance can either be a real clone (we copied from
					 * an existing clone or we pasted multiple times) or a transient clone.
					 * 
					 * NOTE: originOfClipboard is already stored in the clone repository (although maybe as transient clone) 
					 */

					//just checking to make sure, everything is in order
					if (originOfClipboard == null)
					{
						//this should never be the case
						log.error("processEclipseCutCopyPasteEvent(): originOfClipboard is null in internal PASTE - "
								+ event, new Throwable());
						return;
					}

					//the position of our source might have been updated by other events in the mean time
					//we'll need to get a fresh copy of it
					originOfClipboard = storeProvider.lookupClone(originOfClipboard.getUuid());
					if (originOfClipboard == null)
					{
						//this should never happen
						log
								.error(
										"processEclipseCutCopyPasteEvent(): unable to get fresh copy of originOfClipboard in internal PASTE - originOfClipboard: "
												+ originOfClipboard + ", event: " + event, new Throwable());
						return;
					}

					//now create a new clone entry for the pasted part
					//extract the position information
					PositionData positionData = extractPosition(event, false);
					if (positionData == null)
					{
						log.fatal("processEclipseCutCopyPasteEvent() - failed to obtain clone position - " + event,
								new Throwable());

						return;
					}

					//OLD - as we're now updating the document first and then handle the paste event,
					//the content may differ due to source-reformat-on-paste actions by eclipse.
					//					//debug check, the content of the clone should match the clipboard content
					//					if ((clipboardContent == null)
					//							|| (!CoreUtils.equalIgnoringLineBreak(clipboardContent, positionData.cloneContent)))
					//					{
					//						log
					//								.error("processEvent() - PASTE3 - clipboard content does not match newly created clone content - clipboard: "
					//										+ clipboardContent + ", clone: " + positionData.cloneContent);
					//					}

					//OLD - by now the origin will already be updated, so we can never intersect with it!
					//					/*
					//					 * The user might have pasted the clipboard content within the range of it's source.
					//					 * I.e. replacing the source itself partly or completely.
					//					 * In such a case we ignore the source and don't treat the action as cloneing.
					//					 * It is handled like a cut event
					//					 */
					//					if (originOfClipboard.getPosition().intersects(positionData.position))
					//					{
					//						log
					//								.debug("processEvent(): paste location intersects with source location, NOT treating as a clone.");
					//
					//						//create a new transient clone which can act as source for future paste events
					//						createTransientClone(cloneFile, positionData, event.getUser(), true);
					//
					//						return;
					//					}

					/*
					 * TODO: Our origin may well have been modified considerably since the current clipboard content
					 * 		 was copied from it. I.e. our own paste action might overwrite parts of the source or the
					 * 		 source may have been modified by any intermediate user actions.
					 * 
					 * 		 We should therefore reevaluate the similarity between the current origin content and
					 * 		 clipboard content at this point and treat this like a CUT/PASTE action, if the similarity
					 * 		 is too low.
					 */
					int similarity = similarityProvider.calculateSimilarity(ISimilarityProvider.LANGUAGE_JAVA,
							originOfClipboard.getContent(), positionData.cloneContent);
					//FIXME: don't use hard coded 50% here?
					if (!positionData.reformatted && similarity < 50
					/*&& !CoreUtils.equalIgnoringLineBreak(originOfClipboard.getContent(),
							positionData.cloneContent)*/)
					{
						if (log.isDebugEnabled())
							log
									.debug("processEclipseCutCopyPasteEvent(): origin of clone does no longer equal clipboard, NOT treating as a clone - similarity: "
											+ similarity + "%");

						//create a new transient clone which can act as source for future paste events
						boolean cloneCreated = createTransientClone(cloneFile, positionData, event.getUser(), event
								.getEditorContent(), true);
						if (!cloneCreated)
							//rejected by classification provider
							return;

						return;
					}

					//if our origin is transient, we need to change that, it is now a real clone
					boolean originWasTransient = false;
					if (originOfClipboard.isTransient())
					{
						originOfClipboard.setTransient(false);
						addedClones.add(originOfClipboard);
						originModified = true;
						originWasTransient = true;

						log.trace("processEclipseCutCopyPasteEvent(): transient origin clone now non-transient");
					}

					//if our origin doesn't belong to any clone group yet, we need to create a new group
					ICloneGroup newCloneGroup = null;
					boolean originHadNoGroup = false;
					if (originOfClipboard.getGroupUuid() == null)
					{
						//ICloneGroup cloneGroup = new CloneGroup();
						newCloneGroup = (ICloneGroup) cloneRepository.getCloneFactory().getInstance(ICloneGroup.class);
						originOfClipboard.setGroupUuid(newCloneGroup.getUuid());
						originModified = true;
						originHadNoGroup = true;

						if (log.isTraceEnabled())
							log.trace("processEclipseCutCopyPasteEvent() - new clone group: " + newCloneGroup);
					}

					//create a new clone object
					//Clone newClone = new Clone(cloneFile, position, event.getUserId());
					IClone newClone = (IClone) cloneRepository.getCloneFactory().getInstance(IClone.class);
					((ICreatorClone) newClone).setFileUuid(cloneFile.getUuid());
					((ICreatorClone) newClone).setCreator(event.getUser());
					((ICreatorClone) newClone).setCreationDate(new Date());
					newClone.setOffset(positionData.offset);
					newClone.setLength(positionData.length);
					//remember the content
					((ICreatorClone) newClone).setContent(positionData.cloneContent);

					//this clone belongs to the same group as the source
					newClone.setGroupUuid(originOfClipboard.getGroupUuid());
					//let the origin uuid for this clone point to its source, the originOfClipboard 
					newClone.setOriginUuid(originOfClipboard.getUuid());

					//classify the new clone (the origin is already classified at this point) 
					IClassificationProvider.Result cpresult = classificationProvider.classify(
							IClassificationProvider.Type.INITIAL, cloneFile, newClone, event.getEditorContent(),
							originOfClipboard);
					if (IClassificationProvider.Result.REJECTED.equals(cpresult))
					{
						//the classification provider has rejected this clone.
						//we should therefore ignore it here
						if (log.isDebugEnabled())
							log
									.debug("processEclipseCutCopyPasteEvent() - classification provider rejected new clone - ignoring clone - clone: "
											+ newClone);

						//we need to undo any modifications which we may have made to our origin
						if (originHadNoGroup)
							originOfClipboard.setGroupUuid(null);
						if (originWasTransient)
							originOfClipboard.setTransient(true);

						return;
					}

					addedClones.add(newClone);

					try
					{
						storeProvider.acquireWriteLock(LockMode.DEFAULT);

						//if a new clone group was added, it need to be stored now
						if (newCloneGroup != null)
							storeProvider.addCloneGroup(newCloneGroup);

						//add the clone to the repository
						storeProvider.addClone(newClone);

						//						//also update lastPasteClone
						//						cloneRepository.setLastPasteClone(newClone);

						//we have may have modified the source clone data (transient/group),
						//mark it as dirty, if needed
						if (originModified)
							storeProvider.updateClone(originOfClipboard, UpdateMode.MOVED);
					}
					catch (StoreLockingException e)
					{
						log.fatal("processEclipseCutCopyPasteEvent() - locking failure - event: " + event + " - " + e,
								e);
					}
					finally
					{
						//make sure we release the exclusive lock again
						storeProvider.releaseWriteLock();
					}
				}
			}
		}
		else
		{
			log.warn("processEclipseCutCopyPasteEvent(): unknown event type: " + event.getType());
		}

		if (log.isTraceEnabled())
			log.trace("processEclipseCutCopyPasteEvent() - local state - originOfClipboard: " + originOfClipboard
					+ ", existsOnlyInClipboard: " + existsOnlyInClipboard + ", clipboardContent: " + clipboardContent);

	}

	/**
	 * Creates a new transient clone which usually represents the current clipboard content.
	 * 
	 * @param cloneFile the corresponding clone file, never null
	 * @param positionData the position data of the clone (position & content), never null
	 * @param user the user who created the clone, never null
	 * @return true if the clone was created, false if the classification provider prevented the creation.
	 */
	protected boolean createTransientClone(ICloneFile cloneFile, PositionData positionData, String user,
			String editorContent, boolean wasPasteAction)
	{
		if (log.isTraceEnabled())
			log.trace("createTransientClone(): cloneFile: " + cloneFile + ", positionData: " + positionData
					+ ", user: " + user + ", editorContent: "
					+ (editorContent == null ? "null" : "[" + editorContent.length() + "]") + ", wasPasteAction: "
					+ wasPasteAction);

		//there may be some old transient clone still lying around
		clearTransientClone(originOfClipboard);

		//create a new clone object
		originOfClipboard = (IClone) cloneRepository.getCloneFactory().getInstance(IClone.class);
		((ICreatorClone) originOfClipboard).setFileUuid(cloneFile.getUuid());
		((ICreatorClone) originOfClipboard).setCreator(user);
		((ICreatorClone) originOfClipboard).setCreationDate(new Date());
		originOfClipboard.setOffset(positionData.offset);
		originOfClipboard.setLength(positionData.length);
		//also remember clone content
		((ICreatorClone) originOfClipboard).setContent(positionData.cloneContent);
		//such a clone will initially never belong to any clone group

		//this is not a real clone yet and should therefore not be persisted
		originOfClipboard.setTransient(true);
		//by default a new transient clone is an orphan
		originOfClipboard.setCloneState(IClone.State.ORPHAN, 0, null);

		//classify the new clone
		IClassificationProvider.Result cpresult = classificationProvider.classify(IClassificationProvider.Type.INITIAL,
				cloneFile, originOfClipboard, editorContent, null);
		if (IClassificationProvider.Result.REJECTED.equals(cpresult))
		{
			if (log.isDebugEnabled())
				log
						.debug("createTransientClone() - classification provider rejected transient clone, ignoring clone - "
								+ originOfClipboard);
			originOfClipboard = null;
			existsOnlyInClipboard = true;
			return false;
		}

		//tell the clone repository to track the position of this clone
		//the position may well be changed due to edits by the user before the
		//clipboard content is pasted somewhere
		try
		{
			storeProvider.acquireWriteLock(LockMode.DEFAULT);
			storeProvider.addClone(originOfClipboard);

			//			if (wasPasteAction)
			//			{
			//				//this is a special case of a transient clone, contrary to a copy/cut clone it does have a
			//				//corresponding diff event and we therefore need to set the lastPasteClone value
			//				//of the clone registry accordingly
			//				cloneRepository.setLastPasteClone(originOfClipboard);
			//			}
		}
		catch (StoreLockingException e)
		{
			log.fatal("createTransientClone() - locking failure - cloneFile: " + cloneFile + ", positionData: "
					+ positionData + ", user: " + user + " - " + e, e);
		}
		finally
		{
			//make sure we release the exclusive lock again
			storeProvider.releaseWriteLock();
		}

		//this paste location is now a file based source
		existsOnlyInClipboard = false;

		return true;
	}

	/**
	 * Removes a clone from the repository if it is not null and transient. 
	 */
	protected void clearTransientClone(IClone clone)
	{
		if ((clone != null) && (clone.isTransient()))
		{
			try
			{
				storeProvider.acquireWriteLock(LockMode.DEFAULT);
				storeProvider.removeClone(clone);
			}
			catch (StoreLockingException e)
			{
				log.fatal("clearTransientClone() - locking failure - clone: " + clone + " - " + e, e);
			}
			finally
			{
				//make sure we release the exclusive lock again
				storeProvider.releaseWriteLock();
			}
		}
	}

	/**
	 * Extracts a <em>ClonePosition</em> from the cut/copy/paste event.
	 * 
	 * @param event the cut/copy/paste event to parse, never null.
	 * @param isReadAction true if this is a COPY or CUT action, false if this is a PASTE action
	 * @return clone position data or null if calculation failed for some reason.
	 */
	protected PositionData extractPosition(EclipseCutCopyPasteEvent event, boolean isReadAction)
	{
		assert (event != null);

		if (log.isTraceEnabled())
			log.trace("extractPosition(): " + event);

		String editorContent = event.getEditorContent();
		if (editorContent == null)
		{
			//this should never happen
			log.error("extractPosition() - editorContent is null in event: " + event, new Throwable());
			return null;
		}

		//for copy/cut actions the selected text is what counts, for paste actions it's the clipboard content
		int sourceLength = (isReadAction ? event.getSelection().length() : event.getClipboard().length());
		int startCharPos = event.getOffset();
		int endCharPos = startCharPos + sourceLength - 1;
		boolean reformatted = false;

		//some special handling for positions in paste actions
		if (EclipseCutCopyPasteEvent.Type.PASTE.equals(event.getType()))
		{
			/*
			 * Eclipse may reformat source code on paste. This means that our offsets would no longer
			 * match the final position and size of the clone.
			 * We therefore need to take a look at the last document change event. As that should be
			 * the event which corresponds to this paste action. If the position and size of that
			 * event differ from the position and size of this paste event, a source reformat has
			 * taken place and we need to modify the paste offsets accordingly.
			 */

			//get last document event
			DocumentEvent lastDocumentEvent = cloneRepository.getLastDocumentEvent();

			if (lastDocumentEvent != null)
			{
				if (log.isTraceEnabled())
					log.trace("extractPosition() - document event for PASTE event found - offset: "
							+ lastDocumentEvent.getOffset() + ", len: " + lastDocumentEvent.getLength() + ", text: "
							+ lastDocumentEvent.getText());

				//this document change event should always be the one which corresponds to this paste action
				if (lastDocumentEvent.getText() == null)
				{
					//this shouldn't happen
					log.error("extractPosition() - document event for PASTE event has no text value - paste event: "
							+ event + ", lastDocumentEvent: " + lastDocumentEvent, new Throwable());
				}
				else
				{
					//check if the position or length differs
					if ((lastDocumentEvent.getOffset() != startCharPos)
							|| ((lastDocumentEvent.getOffset() + lastDocumentEvent.getText().length() - 1) != endCharPos))
					{
						if (log.isDebugEnabled())
						{
							log
									.debug("extractPosition() - document event position DIFFERS from paste event position - USING document event positions.");
							log.debug("extractPosition() - paste event positions -    offset: " + startCharPos
									+ ", len: " + (endCharPos - startCharPos + 1));
							log.debug("extractPosition() - document event positions - offset: "
									+ lastDocumentEvent.getOffset() + ", len: " + lastDocumentEvent.getText().length());
						}

						startCharPos = lastDocumentEvent.getOffset();
						endCharPos = lastDocumentEvent.getOffset() + lastDocumentEvent.getText().length() - 1;
						reformatted = true;
					}
				}

				//this document event has now been handled, clear it
				cloneRepository.setLastDocumentEvent(null);
			}
		}

		if (log.isTraceEnabled())
			log.trace("extractPosition() - char pos offsets from event - start: " + startCharPos + ", end: "
					+ endCharPos);

		//this does no longer apply, editorContent is now the content after the paste
		/*
		if (!isReadAction)
		{
			//in case of a PASTE action the editorContent represents the source state BEFORE the paste event
			//which means we'll have to insert the pasted text here in order to get a meaningful position

			//we always get the substring till startCharPos
			editorContent = editorContent.substring(0, startCharPos)
			//then we insert the clipboard content
					+ event.getClipboard()
					//and then we add the remaining content. However, if anything was selected,
					//than that many chars need to be dropped, we therefore use the length
					//of the selection as an additional offset
					+ editorContent.substring(startCharPos + event.getSelection().length());

			if (log.isTraceEnabled())
				log.trace("extractPosition() - generated after PASTE body: " + editorContent);
		}*/

		String positionContent = null;
		if (EclipseCutCopyPasteEvent.Type.CUT.equals(event.getType()))
			//for a cut event we should take the selection
			positionContent = event.getSelection();
		else
			//for a copy or paste action we can just take the content from the document
			positionContent = editorContent.substring(startCharPos, endCharPos + 1);

		int length = endCharPos - startCharPos + 1;
		return new PositionData(startCharPos, length, positionContent, reformatted);
	}

	/**
	 * Simple data collection class for parameter passing. 
	 */
	protected class PositionData
	{
		protected int offset = -1;
		protected int length = -1;
		protected String cloneContent = null;
		protected boolean reformatted = false;

		private PositionData(int offset, int length, String cloneContent, boolean reformatted)
		{
			this.offset = offset;
			this.length = length;
			this.cloneContent = cloneContent;
			this.reformatted = reformatted;
		}

		@Override
		public String toString()
		{
			return "PositionData[" + offset + ":" + length + " ; " + cloneContent + "]";
		}
	}
}

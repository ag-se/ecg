package org.electrocodeogram.cpc.notification.utils;


import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.data.ICloneFile;
import org.electrocodeogram.cpc.core.api.data.IClone.State;
import org.electrocodeogram.cpc.core.api.hub.event.CloneNotificationEvent;
import org.electrocodeogram.cpc.core.api.provider.notification.IEvaluationResult;
import org.electrocodeogram.cpc.core.api.provider.notification.INotificationEvaluationProvider;
import org.electrocodeogram.cpc.core.api.provider.notification.IEvaluationResult.Action;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider;
import org.electrocodeogram.cpc.core.api.provider.store.StoreLockingException;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider.LockMode;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider.UpdateMode;


/**
 * Contains a set of static utility functions which are used by different classes
 * within the <em>CPC Notification</em> module.
 * 
 * @author vw
 */
public class NotificationUtils
{
	private static Log log = LogFactory.getLog(NotificationUtils.class);

	/**
	 * Processes the {@link IEvaluationResult} of an {@link INotificationEvaluationProvider}
	 * and returns a list of generated {@link CloneNotificationEvent}s.
	 * <p>
	 * <b>IMPORTANT:</b> This method <u>may modify clone data</u> and will thus acquire an <u>exclusive IStoreProvider
	 * 		write lock</u> if needed. This may lead to potential deadlocks if this method is called from within
	 * 		a synchronized block. It is therefore recommended to never call this method from any location
	 * 		which is holding any kind of lock.
	 * 
	 * @param storeProvider a valid {@link IStoreProvider} reference, never null.
	 * @param cloneFile the corresponding {@link IClone}, never null.
	 * @param modifiedClone the modified {@link IClone}, never null.
	 * @param result the {@link IEvaluationResult} to process, never null.
	 * @param delayedNotification
	 * @return a list of new {@link CloneNotificationEvent}s.
	 */
	public static List<CloneNotificationEvent> processEvaluationResult(IStoreProvider storeProvider,
			ICloneFile cloneFile, IClone modifiedClone, IEvaluationResult result, boolean delayedNotification)
	{
		if (log.isTraceEnabled())
			log.trace("processEvaluationResult() - storeProvider: " + storeProvider + ", cloneFile: " + cloneFile
					+ ", modifiedClone: " + modifiedClone + ", result: " + result + ", delayedNotification: "
					+ delayedNotification);
		assert (storeProvider != null && cloneFile != null && modifiedClone != null && result != null);

		List<CloneNotificationEvent> eventQueue = new LinkedList<CloneNotificationEvent>();

		/*
		 * IGNORE
		 * do nothing
		 */
		if (IEvaluationResult.Action.IGNORE.equals(result.getAction()))
		{
			log.trace("processEvaluationResult() - ignoring clone modification");
		}

		/*
		 * INSYNC
		 * mark this clone and all its clone group members as state insync
		 * unless they already are in state ignored.
		 */
		else if (IEvaluationResult.Action.INSYNC.equals(result.getAction()))
		{
			log.trace("processEvaluationResult() - setting state of clone and its group members to INSYNC.");

			//update state directly
			NotificationUtils.setStateByActionForGroup(storeProvider, modifiedClone, Action.INSYNC, result);
		}

		/*
		 * INSYNC_CUSTOMISED
		 * mark this clone and all its clone group members as state insync customised
		 * unless they already are in state ignored.
		 */
		else if (IEvaluationResult.Action.INSYNC_CUSTOMISED.equals(result.getAction()))
		{
			log.trace("processEvaluationResult() - setting state of clone and its group members to INSYNC_CUSTOMISED.");

			//update state directly
			NotificationUtils.setStateByActionForGroup(storeProvider, modifiedClone, Action.INSYNC_CUSTOMISED, result);
		}

		/*
		 * MODIFIED
		 * mark this clone and all its clone group members as state modified
		 * unless they already have a higher state.
		 */
		else if (IEvaluationResult.Action.MODIFIED.equals(result.getAction()))
		{
			log.trace("processEvaluationResult() - setting state of clone and its group members to MODIFIED.");

			//update state directly
			NotificationUtils.setStateByActionForGroup(storeProvider, modifiedClone, Action.MODIFIED, result);
		}

		/*
		 * NOTIFY or WARN (also instant)
		 * create new CloneNotificationEvent
		 * And mark all clone group members as modified.
		 * The modified clone itself is set to modified for NOTIFY and WARN and to notify or warn
		 * for INSTANT_NOTIFY or INSTANT_WARN
		 */
		else if (IEvaluationResult.Action.NOTIFY.equals(result.getAction())
				|| IEvaluationResult.Action.WARN.equals(result.getAction())
				|| IEvaluationResult.Action.INSTANT_NOTIFY.equals(result.getAction())
				|| IEvaluationResult.Action.INSTANT_WARN.equals(result.getAction()))
		{
			log.trace("processEvaluationResult() - creating notification event");
			CloneNotificationEvent newEvent = new CloneNotificationEvent(cloneFile);

			newEvent.setModifiedClone(modifiedClone);
			newEvent.setWeight(result.getWeight());
			newEvent.setMessage(result.getMessage());

			//set the type according to the result's action field
			if (IEvaluationResult.Action.NOTIFY.equals(result.getAction()))
			{
				if (delayedNotification)
					newEvent.setType(CloneNotificationEvent.Type.DELAY_NOTIFY);
				else
					newEvent.setType(CloneNotificationEvent.Type.NOTIFY);
			}
			else if (IEvaluationResult.Action.WARN.equals(result.getAction()))
			{
				if (delayedNotification)
					newEvent.setType(CloneNotificationEvent.Type.DELAY_WARN);
				else
					newEvent.setType(CloneNotificationEvent.Type.WARN);
			}
			else if (IEvaluationResult.Action.INSTANT_NOTIFY.equals(result.getAction()))
			{
				newEvent.setType(CloneNotificationEvent.Type.NOTIFY);
			}
			else if (IEvaluationResult.Action.INSTANT_WARN.equals(result.getAction()))
			{
				newEvent.setType(CloneNotificationEvent.Type.WARN);
			}

			eventQueue.add(newEvent);

			/*
			 * Now update the clone state.
			 */

			//if the event was not delayed, set the new state directly
			if (CloneNotificationEvent.Type.NOTIFY.equals(newEvent.getType()))
			{
				NotificationUtils.setStateByActionForGroup(storeProvider, modifiedClone, Action.NOTIFY, result);
			}
			else if (CloneNotificationEvent.Type.WARN.equals(newEvent.getType()))
			{
				NotificationUtils.setStateByActionForGroup(storeProvider, modifiedClone, Action.WARN, result);
			}
			//if the event was delayed, set the state temporarily to MODIFIED, but only, if the
			//current state is DEFAULT (insync)
			else if ((CloneNotificationEvent.Type.DELAY_NOTIFY.equals(newEvent.getType()))
					|| (CloneNotificationEvent.Type.DELAY_WARN.equals(newEvent.getType())))
			{
				if (IClone.State.DEFAULT.equals(modifiedClone.getCloneState()))
					NotificationUtils.setStateByActionForGroup(storeProvider, modifiedClone, Action.MODIFIED, result);
			}
		}

		/*
		 * LEAVE_GROUP
		 * remove the clone from its group
		 */
		else if (IEvaluationResult.Action.LEAVE_GROUP.equals(result.getAction()))
		{
			log.trace("processEvaluationResult() - removing clone from group");

			NotificationUtils.removeCloneFromGroup(storeProvider, modifiedClone);

			/*
			 * TODO: we should probably reevaluate the clone group now. It might be in sync after this event.
			 */
		}

		//???
		else
		{
			log.warn("processEvaluationResult() - unknown action type: " + result.getAction() + " - " + result + " - "
					+ modifiedClone);
		}

		if (log.isTraceEnabled())
			log.trace("processEvaluationResult() - result (eventQueue): " + eventQueue);

		return eventQueue;
	}

	/**
	 * Sets the state for all members of the given clone's clone group according to the given {@link IEvaluationResult.Action}.<br/>
	 * <br/>
	 * The concrete semantics depend on the action given.<br/>
	 * <ul>
	 * 	<li>for {@link IEvaluationResult.Action#INSYNC}:<br/>
	 * 		The clone and all it's group members will be set to {@link IClone.State#DEFAULT}, unless they are
	 * 		currently set to {@link IClone.State#IGNORE}.
	 * 	</li>
	 * 	<li>for {@link IEvaluationResult.Action#MODIFIED}:<br/>
	 * 		The clone's state and all its clone members' state will be set to {@link IClone.State#MODIFIED}.<br/>
	 * 		However, group members will only be updated if their current state is {@link IClone.State#DEFAULT} or
	 * 		{@link IClone.State#MODIFIED}.<br/>
	 *		The given clone itself will always be updated, unless it's state is	currently set to {@link IClone.State#IGNORE}.
	 * 	</li>
	 * 	<li>for {@link IEvaluationResult.Action#NOTIFY} and {@link IEvaluationResult.Action#WARN}:<br/>
	 * 		Group members are updated as in the {@link IEvaluationResult.Action#MODIFIED} case.<br/>
	 *		The given clone itself will always be updated, unless it's state is	currently set to {@link IClone.State#IGNORE}.
	 * 	</li>
	 * 	<li>
	 * 		Any other action value is not supported.
	 * 	</li>
	 * </ul>
	 * The optional {@link IEvaluationResult} object are only used to update the clone data (weight, message).<br/>
	 * The {@link IEvaluationResult#getAction()} value will <b>not</b> be used. 
	 * 
	 * @param storeProvider valid store provider reference, never null.
	 * @param modifiedClone the clone for who's group to set the state, never null.
	 * @param action the new action to derive the state from, never null.
	 * @param result the old {@link IEvaluationResult}, may be NULL.
	 */
	public static void setStateByActionForGroup(IStoreProvider storeProvider, IClone modifiedClone,
			IEvaluationResult.Action action, IEvaluationResult result)
	{
		if (log.isTraceEnabled())
			log.trace("setStateByActionForGroup() - modifiedClone: " + modifiedClone + ", action: " + action
					+ ", result: " + result + ", storeProvider: " + storeProvider);
		assert (storeProvider != null && modifiedClone != null && action != null);

		if (modifiedClone.getGroupUuid() == null)
		{
			//this shouldn't happen
			log.error("setStateByActionForGroup() - update group state for groupless clone: " + modifiedClone,
					new Throwable());
			return;
		}

		try
		{
			//get an exclusive lock
			storeProvider.acquireWriteLock(LockMode.DEFAULT);

			//get a fresh copy of the modified clone
			IClone latestModifiedClone = storeProvider.lookupClone(modifiedClone.getUuid());
			if (latestModifiedClone == null)
			{
				//clone was deleted in the mean time?
				log.trace("setStateByActionForGroup() - clone was concurrently deleted, ignoring.");
				return;
			}
			else if (latestModifiedClone.getGroupUuid() == null)
			{
				//I don't think we'll see this often.
				log.trace("setStateByActionForGroup() - clone was concurrently removed from its group, ignoring.");
				return;
			}

			//get the clone group for the clone
			List<IClone> groupClones = storeProvider.getClonesByGroup(latestModifiedClone.getGroupUuid());
			if (groupClones.isEmpty())
			{
				//this shouldn't happen. The clone group should at least have the clone itself in it.
				log
						.error("setStateByActionForGroup() - clone was not present in its own clone group?",
								new Throwable());
				return;
			}

			double weight = ((result != null) ? result.getWeight() : 0);
			String message = ((result != null) ? result.getMessage() : null);

			IClone modifiedCloneInGroupList = null;
			//set the new state for all group members (includes the modified clone itself)
			for (IClone groupClone : groupClones)
			{
				if (IEvaluationResult.Action.INSYNC.equals(action))
				{
					//we're only updating clones which are not of state IGNORE
					if (!State.IGNORE.equals(groupClone.getCloneState()))
						groupClone.setCloneState(State.DEFAULT, weight, message);
				}
				else if (IEvaluationResult.Action.MODIFIED.equals(action)
						|| IEvaluationResult.Action.NOTIFY.equals(action)
						|| IEvaluationResult.Action.WARN.equals(action))
				{
					//in all these cases we first need to set all group members to state MODIFIED
					//we're only updating clones in states DEFAULT and MODIFIED (to update date)
					if (State.DEFAULT.equals(groupClone.getCloneState())
							|| State.MODIFIED.equals(groupClone.getCloneState()))
						groupClone.setCloneState(State.MODIFIED, weight, message);
				}
				else
				{
					log.warn("setStateByActionForGroup() - action not supported: " + action);
				}

				if (latestModifiedClone.equals(groupClone))
					modifiedCloneInGroupList = groupClone;
			}

			if (modifiedCloneInGroupList == null)
			{
				//this shouldn't happen
				log.error("setStateByActionForGroup() - clone was not part of its own clone group: "
						+ latestModifiedClone, new Throwable());
				return;
			}

			if (IEvaluationResult.Action.NOTIFY.equals(action))
			{
				//we're only updating the main clone if it is in state DEFAULT, MODIFIED or NOTIFY (to update date)
				if (State.DEFAULT.equals(modifiedCloneInGroupList.getCloneState())
						|| State.MODIFIED.equals(modifiedCloneInGroupList.getCloneState())
						|| State.NOTIFY.equals(modifiedCloneInGroupList.getCloneState()))
					modifiedCloneInGroupList.setCloneState(State.NOTIFY, weight, message);
			}
			else if (IEvaluationResult.Action.WARN.equals(action))
			{
				//we're only updating the main clone if it is in state DEFAULT, MODIFIED, NOTIFY or WARN (to update date)
				if (State.DEFAULT.equals(modifiedCloneInGroupList.getCloneState())
						|| State.MODIFIED.equals(modifiedCloneInGroupList.getCloneState())
						|| State.NOTIFY.equals(modifiedCloneInGroupList.getCloneState())
						|| State.WARN.equals(modifiedCloneInGroupList.getCloneState()))
					modifiedCloneInGroupList.setCloneState(State.WARN, weight, message);
			}

			//now send the updated clones to the store provider
			storeProvider.updateClones(groupClones, UpdateMode.MOVED);
		}
		catch (StoreLockingException e)
		{
			log.error("procesCloneModificationEvent() - lock error - " + e, e);
		}
		finally
		{
			storeProvider.releaseWriteLock();
		}
	}

	/**
	 * Removes the given clone from its clone group and updates the store provider accordingly.
	 * 
	 * @param storeProvider valid store provider reference, never null.
	 * @param modifiedClone the clone to drop from its group, never null.
	 */
	public static void removeCloneFromGroup(IStoreProvider storeProvider, IClone modifiedClone)
	{
		if (log.isTraceEnabled())
			log.trace("removeCloneFromGroup() - modifiedClone: " + modifiedClone + ", storeProvider: " + storeProvider);
		assert (storeProvider != null && modifiedClone != null);

		if (modifiedClone.getGroupUuid() == null)
		{
			//this shouldn't happen
			log.error("removeCloneFromGroup() - can't remove groupless clone from group: " + modifiedClone,
					new Throwable());
			return;
		}

		try
		{
			storeProvider.acquireWriteLock(LockMode.DEFAULT);

			//get the latest version of the modifiedClone
			IClone latestModifiedClone = storeProvider.lookupClone(modifiedClone.getUuid());
			if (latestModifiedClone == null)
			{
				//clone was deleted in the mean time?
				log.trace("removeCloneFromGroup() - clone was concurrently deleted, ignoring.");
				return;
			}

			//reset group info
			latestModifiedClone.setGroupUuid(null);

			//store updated clone
			storeProvider.updateClone(latestModifiedClone, UpdateMode.MOVED);
		}
		catch (StoreLockingException e)
		{
			log.error("removeCloneFromGroup() - lock error - " + e, e);
		}
		finally
		{
			storeProvider.releaseWriteLock();
		}
	}
}

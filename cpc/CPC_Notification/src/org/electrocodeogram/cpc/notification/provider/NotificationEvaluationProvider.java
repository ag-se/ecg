package org.electrocodeogram.cpc.notification.provider;


import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.electrocodeogram.cpc.core.CPCCorePlugin;
import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.provider.IManagableProvider;
import org.electrocodeogram.cpc.core.api.provider.notification.IEvaluationResult;
import org.electrocodeogram.cpc.core.api.provider.notification.INotificationEvaluationProvider;
import org.electrocodeogram.cpc.core.api.provider.notification.IEvaluationResult.Action;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider;
import org.electrocodeogram.cpc.notification.api.strategy.INotificationEvaluationStrategy;
import org.electrocodeogram.cpc.notification.api.strategy.INotificationEvaluationStrategyResult;
import org.electrocodeogram.cpc.notification.listener.CloneModificationListener;


/**
 * Default {@link INotificationEvaluationProvider} implementation.
 * 
 * @author vw
 * 
 * @see INotificationEvaluationProvider
 * @see CloneModificationListener
 */
public class NotificationEvaluationProvider implements INotificationEvaluationProvider, IManagableProvider
{
	private static final Log log = LogFactory.getLog(NotificationEvaluationProvider.class);

	public static final String EXTENSION_POINT_STRATEGIES = "org.electrocodeogram.cpc.notification.notificationEvaluationStategies";

	private List<NotificationEvaluationStrategyDescriptor> registeredStrategies;
	private IStoreProvider storeProvider;

	public NotificationEvaluationProvider()
	{
		log.trace("NotificationEvaluationProvider()");

		storeProvider = (IStoreProvider) CPCCorePlugin.getProviderRegistry().lookupProvider(IStoreProvider.class);
		assert (storeProvider != null);

		initialiseStrategies();
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.notification.INotificationEvaluationProvider#evaluateModification(org.electrocodeogram.cpc.core.api.data.IClone, java.util.List)
	 */
	@Override
	public IEvaluationResult evaluateModification(IClone modifiedClone, List<IClone> groupMembers,
			boolean initialEvaluation)
	{
		if (log.isTraceEnabled())
			log.trace("evaluateModification() - modifiedClone: " + modifiedClone + ", groupMembers: " + groupMembers
					+ ", initialEvaluation: " + initialEvaluation);
		assert (modifiedClone != null);

		//we're obviously only interested in clones which are a member of a clone group
		if (modifiedClone.getGroupUuid() == null)
		{
			log.trace("evaluateModification() - ignoring standalone clone (not part of any group).");
			return new EvaluationResult(Action.INSYNC);
		}

		//if we haven't been provided with this data,
		//get all group members for the clone, if it has a group
		if (groupMembers == null)
			groupMembers = storeProvider.getClonesByGroup(modifiedClone.getGroupUuid());

		//we're not interested in clones which are the sole member of their group
		if (groupMembers.size() == 1)
		{
			log.trace("evaluateModification() - ignoring standalone clone (the only member of its group).");

			// We're already done here, this is just a sanity check, that one group member should be our clone.
			if (!groupMembers.contains(modifiedClone))
			{
				//This shouldn't happen. One possible situation where this is to be expected is a concurrent
				//deletion of the clone in question. We double check that here to make the errors less spurtious.
				if (storeProvider.lookupClone(modifiedClone.getUuid()) == null)
				{
					log.warn("evaluateModification() - clone was concurrently deleted, ignoring - clone: "
							+ modifiedClone, new Throwable());
				}
				else
				{
					log.error(
							"evaluateModification() - the clone's group has one member, but it does not match the clone - groupMembers: "
									+ groupMembers + ", clone: " + modifiedClone, new Throwable());
				}
			}

			return new EvaluationResult(Action.INSYNC);
		}
		else if (groupMembers.isEmpty())
		{
			//This shouldn't happen. One possible situation where this is to be expected is a concurrent
			//deletion of the clone in question. We double check that here to make the errors less spurtious.
			if (storeProvider.lookupClone(modifiedClone.getUuid()) == null)
			{
				log.warn("evaluateModification() - clone was concurrently deleted, ignoring(2) - clone: "
						+ modifiedClone, new Throwable());
			}
			else
			{
				log.error(
						"evaluateModification() - the clone has a group, but the group is empty (doesn't even contain the clone itself): "
								+ modifiedClone, new Throwable());
			}
			return new EvaluationResult(Action.INSYNC);
		}

		//create an empty result wrapper object
		NotificationEvaluationStrategyResult results = new NotificationEvaluationStrategyResult();

		//execute all strategies (results will be added to result wrapper object)
		callStrategies(modifiedClone, groupMembers, initialEvaluation, results);

		/*
		 * At this point all applying strategies have been executed and their results can
		 * be found in the result wrapper object.
		 * Note: A strategy may also add multiple results or none at all. The number of entries in the
		 * result wrapper is therefore not related to the number of registered or applying strategies.
		 * 
		 * We now have to aggregate the results into a single final result.
		 * Each strategy has assigned an action and a weight to its result(s).
		 * For now we chose the result with the highest weight as final result.
		 * If the maximal weight is shared by multiple results, the first one (created by the strategy with
		 * the higher priority) is used. 
		 */
		IEvaluationResult maxWeightResult = null;
		for (IEvaluationResult subResult : results.getResults())
		{
			if (maxWeightResult == null || maxWeightResult.getWeight() < subResult.getWeight())
			{
				if (log.isTraceEnabled())
					log
							.trace("evaluateModification() - result: " + subResult + " overrides result: "
									+ maxWeightResult);
				maxWeightResult = subResult;
			}
		}

		if (maxWeightResult == null)
		{
			//fall back to IGNORE
			log.trace("evaluateModification() - evaluation strategies returned no results, falling back to IGNORE.");
			maxWeightResult = new EvaluationResult(Action.IGNORE);
		}

		if (log.isTraceEnabled())
			log.trace("evaluateModification() - result: " + maxWeightResult);

		return maxWeightResult;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.IProvider#getProviderName()
	 */
	@Override
	public String getProviderName()
	{
		return "CPC Notification - Default Notification Evaluation Provider";
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.IProvider#onLoad()
	 */
	@Override
	public void onLoad()
	{
		//nothing to do		
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.IProvider#onUnload()
	 */
	@Override
	public void onUnload()
	{
		//nothing to do
	}

	/**
	 * Retrieves a list of registered {@link INotificationEvaluationStrategy}s.
	 * 
	 * @return list of strategies, may be NULL.
	 */
	public List<NotificationEvaluationStrategyDescriptor> getStrategies()
	{
		return registeredStrategies;
	}

	/*
	 * Private methods.
	 */

	/**
	 * Executes all registered strategies in order of their priority till the first strategy
	 * returns {@link INotificationEvaluationStrategy.Status#BREAK} or all strategies
	 * have been run.
	 */
	private void callStrategies(IClone modifiedClone, List<IClone> groupMembers, boolean initialEvaluation,
			INotificationEvaluationStrategyResult results)
	{
		//now run each strategy in turn
		for (NotificationEvaluationStrategyDescriptor strategyDescr : registeredStrategies)
		{
			if (log.isTraceEnabled())
				log.trace("callStrategies() - strategy: " + strategyDescr);

			if (!strategyDescr.isActivated())
			{
				log.trace("callStrategies() - skipping deactivated strategy.");
				continue;
			}

			//make sure a misbehaving strategy doesn't prevent other strategies from working
			try
			{
				INotificationEvaluationStrategy.Status status = strategyDescr.getInstance().evaluateModification(
						modifiedClone, groupMembers, initialEvaluation, results);

				if (log.isTraceEnabled())
					log.trace("callStrategies() - status: " + status);

				if (INotificationEvaluationStrategy.Status.BREAK.equals(status))
				{
					if (log.isDebugEnabled())
						log
								.debug("callStrategies() - aborting further execution of strategies by request of strategy: "
										+ strategyDescr);
					break;
				}
			}
			catch (Exception e)
			{
				log.error("callStrategies() - error during the execution of strategy: " + strategyDescr + " - " + e, e);
			}

		}

		if (log.isTraceEnabled())
			log.trace("callStrategies() - results: " + results);
	}

	/**
	 * Retrieves all registered {@link INotificationEvaluationStrategy} extensions from the
	 * corresponding extension point and adds them to the <em>registeredStrategies</em> list,
	 * ordered by descending priority.
	 */
	private void initialiseStrategies()
	{
		log.trace("initialiseStrategies(): building strategy registry from extension data");

		registeredStrategies = new LinkedList<NotificationEvaluationStrategyDescriptor>();

		IConfigurationElement[] extensions = Platform.getExtensionRegistry().getConfigurationElementsFor(
				EXTENSION_POINT_STRATEGIES);
		for (IConfigurationElement element : extensions)
		{
			try
			{
				NotificationEvaluationStrategyDescriptor descriptor = new NotificationEvaluationStrategyDescriptor(
						element);

				registeredStrategies.add(descriptor);
			}
			catch (Exception e)
			{
				log.error("initialiseStrategies(): registration of strategy failed: " + element.getAttribute("class")
						+ ", elem: " + element + " - " + e, e);
			}
		}

		//make sure all strategies are ordered by priority
		Collections.sort(registeredStrategies);

		if (log.isTraceEnabled())
			log.trace("initialiseStrategies() - registered strategies: " + registeredStrategies);
	}
}

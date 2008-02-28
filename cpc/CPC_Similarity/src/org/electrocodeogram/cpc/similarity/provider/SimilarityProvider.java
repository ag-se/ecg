package org.electrocodeogram.cpc.similarity.provider;


import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.electrocodeogram.cpc.core.CPCCorePlugin;
import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.data.special.ICreatorClone;
import org.electrocodeogram.cpc.core.api.provider.IManagableProvider;
import org.electrocodeogram.cpc.core.api.provider.data.ICloneFactoryProvider;
import org.electrocodeogram.cpc.core.api.provider.similarity.ISimilarityProvider;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider;
import org.electrocodeogram.cpc.core.utils.CoreStringUtils;
import org.electrocodeogram.cpc.similarity.api.strategy.ISimilarityStrategy;
import org.electrocodeogram.cpc.similarity.api.strategy.ISimilarityStrategy.Status;


/**
 * Default implementation of the {@link ISimilarityProvider} API.
 * 
 * @author vw
 */
public class SimilarityProvider implements ISimilarityProvider, IManagableProvider
{
	private static final Log log = LogFactory.getLog(SimilarityProvider.class);

	public static final String EXTENSION_POINT_STRATEGIES = "org.electrocodeogram.cpc.similarity.similarityStategies";

	private List<SimilarityStrategyDescriptor> registeredStrategies;

	public SimilarityProvider()
	{
		log.trace("SimilarityProvider()");

		initialiseStrategies();
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.similarity.ISimilarityProvider#calculateSimilarity(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public int calculateSimilarity(String language, String content1, String content2)
	{
		if (log.isTraceEnabled())
			log.trace("calculateSimilarity() - language: " + language + ", content1: "
					+ CoreStringUtils.truncateString(content1) + ", content2: "
					+ CoreStringUtils.truncateString(content2));
		assert (language != null && content1 != null && content2 != null);

		ICloneFactoryProvider cloneFactoryProvider = (ICloneFactoryProvider) CPCCorePlugin.getProviderRegistry()
				.lookupProvider(ICloneFactoryProvider.class);
		assert (cloneFactoryProvider != null);

		IClone clone1 = (IClone) cloneFactoryProvider.getInstance(IClone.class, "TEMP1");
		((ICreatorClone) clone1).setContent(content1);
		clone1.setLength(content1.length());

		IClone clone2 = (IClone) cloneFactoryProvider.getInstance(IClone.class, "TEMP2");
		((ICreatorClone) clone2).setContent(content2);
		clone2.setLength(content2.length());

		return calculateSimilarity(language, clone1, clone2, true);
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.similarity.ISimilarityProvider#calculateSimilarity(java.lang.String, org.electrocodeogram.cpc.core.api.data.IClone, org.electrocodeogram.cpc.core.api.data.IClone, boolean)
	 */
	@Override
	public int calculateSimilarity(String language, IClone clone1, IClone clone2, boolean transientCheck)
	{
		if (log.isTraceEnabled())
			log.trace("calculateSimilarity() - language: " + language + ", clone1: " + clone1 + ", clone2: " + clone2
					+ ", transientCheck: " + transientCheck);
		assert (language != null && clone1 != null && clone2 != null);

		//make sure we have the content of both clones
		if (clone1.getContent() == null || clone2.getContent() == null)
		{
			//we don't have the content, but we need it!
			log.error("calculateSimilarity() - clone content is missing, returning 0% similarity - clone1: " + clone1
					+ ", clone2: " + clone2, new Throwable());
			return 0;
		}

		//clones with equal content have 100% similarity
		if (clone1.getContent().equals(clone2.getContent()))
		{
			log.trace("calculateSimilarity() - clone content matches - result: 100% similarity");
			return 100;
		}

		//check if we're allowed to access the store provider
		IStoreProvider storeProvider = null;
		if (!transientCheck)
		{
			//ok, we are. Acquire a reference once here, in order to prevent each strategy from having to acquire its
			//own copy.
			storeProvider = (IStoreProvider) CPCCorePlugin.getProviderRegistry().lookupProvider(IStoreProvider.class);
		}

		int result = callStrategies(storeProvider, language, clone1, clone2);

		if (log.isTraceEnabled())
			log.trace("calculateSimilarity() - result: " + result);

		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.IProvider#getProviderName()
	 */
	@Override
	public String getProviderName()
	{
		return "CPC Similarity - Default Similarity Provider";
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
	 * Retrieves a list of the currently registered {@link ISimilarityStrategy}s.
	 * 
	 * @return may be NULL.
	 */
	public List<SimilarityStrategyDescriptor> getStrategies()
	{
		return registeredStrategies;
	}

	/*
	 * Private methods.
	 */

	/**
	 * Executes all registered strategies.
	 */
	private int callStrategies(IStoreProvider storeProvider, String language, IClone clone1, IClone clone2)
	{
		SimilarityStrategyTask task = new SimilarityStrategyTask(language, clone1, clone2);

		//now run each strategy in turn
		for (SimilarityStrategyDescriptor strategyDescr : registeredStrategies)
		{
			if (log.isTraceEnabled())
				log.trace("callStrategies() - strategy: " + strategyDescr);

			if (!strategyDescr.isActivated())
			{
				log.trace("callStrategies() - skipping deactivated strategy.");
				continue;
			}

			//skip strategies with weight 0
			if (strategyDescr.getWeight() == 0)
			{
				log.trace("callStrategies() - skipping strategy, weight is 0.");
				continue;
			}

			//make sure a misbehaving strategy doesn't prevent other strategies from working
			try
			{
				//set the primary weight for the processing of this strategy
				task.setPrimaryWeight(strategyDescr.getWeight());
				Status status = strategyDescr.getInstance().calculateSimilarity(storeProvider, task);

				if (log.isTraceEnabled())
					log.trace("callStrategies() - result - status: " + status + ", task: " + task);

				if (Status.BREAK.equals(status))
				{
					log.trace("callStrategies() - strategy returned status BREAK, skipping all remaining strategies.");
					break;
				}
			}
			catch (Exception e)
			{
				log.error("callStrategies() - error during the execution of strategy: " + strategyDescr + " - " + e, e);
			}

		}

		if (log.isTraceEnabled())
			log.trace("callStrategies() - final result: " + task.getAverageScore() + " - task: " + task);

		return task.getAverageScore();
	}

	/**
	 * Retrieves all registered {@link ISimilarityStrategy} extensions from the
	 * corresponding extension point and adds them to the <em>registeredStrategies</em> registry,
	 * ordered by descending priority.
	 */
	private void initialiseStrategies()
	{
		log.trace("initialiseStrategies()");

		registeredStrategies = new LinkedList<SimilarityStrategyDescriptor>();

		IConfigurationElement[] extensions = Platform.getExtensionRegistry().getConfigurationElementsFor(
				EXTENSION_POINT_STRATEGIES);
		for (IConfigurationElement element : extensions)
		{
			try
			{
				SimilarityStrategyDescriptor descriptor = new SimilarityStrategyDescriptor(element, 1.0f);

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

		//		registeredStrategies.add(new SimilarityStrategyDescriptor(new JavaCodeNormalisingStrategy(), 1.0));
		//		registeredStrategies.add(new SimilarityStrategyDescriptor(new GenericCodeNormalisingStrategy(), 1.0));
		//		registeredStrategies.add(new SimilarityStrategyDescriptor(new LevenshteinDistanceStrategy(), 1.0));
	}
}

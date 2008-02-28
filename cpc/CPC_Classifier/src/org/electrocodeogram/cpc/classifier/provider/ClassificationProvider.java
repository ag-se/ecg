package org.electrocodeogram.cpc.classifier.provider;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.electrocodeogram.cpc.classifier.api.strategy.IClassificationStrategy;
import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.data.ICloneFile;
import org.electrocodeogram.cpc.core.api.provider.IManagableProvider;
import org.electrocodeogram.cpc.core.api.provider.classification.IClassificationProvider;
import org.electrocodeogram.cpc.core.utils.CoreFileUtils;
import org.electrocodeogram.cpc.core.utils.CoreUtils;


/**
 * Default {@link IClassificationProvider} implementation.
 * <p>
 * Additional {@link IClassificationStrategy}s can be registered via the "<em>classificationStategies</em>" extension
 * point of the <em>CPC Classifier</em> module.
 * 
 * @author vw
 *
 * @see IClassificationProvider
 * @see IClassificationStrategy
 */
public class ClassificationProvider implements IClassificationProvider, IManagableProvider
{
	private static final Log log = LogFactory.getLog(ClassificationProvider.class);

	public static final String EXTENSION_POINT_STRATEGIES = "org.electrocodeogram.cpc.classifier.classificationStategies";

	private List<ClassificationStrategyDescriptor> registeredStrategies;

	public ClassificationProvider()
	{
		log.trace("ClassificationProvider()");

		initialiseStrategies();
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.classification.IClassificationProvider#classify(org.electrocodeogram.cpc.core.api.provider.classification.IClassificationProvider.Type, org.electrocodeogram.cpc.core.api.data.ICloneFile, org.electrocodeogram.cpc.core.api.data.IClone, java.lang.String, org.electrocodeogram.cpc.core.api.data.IClone)
	 */
	@Override
	public Result classify(Type type, ICloneFile cloneFile, IClone clone, String fileContent, IClone originClone)
	{
		if (log.isTraceEnabled())
			log.trace("classify() - type: " + type + ", cloneFile: " + cloneFile + ", clone: " + clone
					+ ", fileContent: " + fileContent + ", originClone: " + originClone);
		assert (type != null && cloneFile != null && clone != null);

		/*
		 * INIT
		 */

		//check for file content availability
		if (fileContent == null)
		{
			//We weren't provided with the content of the file, however, some strategies
			//will need it. Try to obtain it now.

			//get a file handle
			IFile fileHandle = CoreFileUtils.getFileForCloneFile(cloneFile);
			if (fileHandle == null)
			{
				log.error("classify() - unable to obtain file handle for clone file: " + cloneFile, new Throwable());
				return Result.ERROR;
			}

			//now get the content
			fileContent = CoreUtils.getFileContentFromEditorOrFile(fileHandle);
			if (fileContent == null)
			{
				log.error("classify() - unable to obtain file content for clone file: " + cloneFile, new Throwable());
				return Result.ERROR;
			}
		}

		//initialise our result data structure
		Map<String, Double> resultMap = new HashMap<String, Double>(10);
		resultMap.put(IClassificationStrategy.CLASSIFICATION_REJECT, new Double(0.0));

		//clear any existing classifications from the clone
		//if a complete reclassification was requested
		if (Type.RECLASSIFY.equals(type))
		{
			if (log.isTraceEnabled())
				log.trace("classify() - dropping all existing classifications from clone: "
						+ clone.getClassifications());

			for (String classification : new ArrayList<String>(clone.getClassifications()))
				clone.removeClassification(classification);
		}
		//otherwise import the existing classification data
		else if (Type.INCREMENTAL.equals(type))
		{
			if (log.isTraceEnabled())
				log.trace("classify() - reimporting all existing classifications from clone: "
						+ clone.getClassifications());

			for (String classification : clone.getClassifications())
				resultMap.put(classification, new Double(1));
		}
		else if (Type.INITIAL.equals(type))
		{
			//integrity check, there should be no existing classifications
			if (!clone.getClassifications().isEmpty())
			{
				log.warn("classify() - clone with non-empty classification list with Type INITIAL - clone: " + clone
						+ ", classifications: " + clone.getClassifications() + ", cloneFile: " + cloneFile
						+ ", originClone: " + originClone, new Throwable());

				//drop all of them
				for (String classification : new ArrayList<String>(clone.getClassifications()))
					clone.removeClassification(classification);
			}
		}
		else
		{
			//huh? unsupported type?
			log.error("classify() - unsupported classification type: " + type, new Throwable());
			return Result.ERROR;
		}

		/*
		 * EXEC
		 */

		//now execute all strategies
		callStrategies(type, cloneFile, clone, fileContent, originClone, resultMap);

		/*
		 * RESULT
		 */

		//transfer all classifications with weight >0 to the clone object
		for (Map.Entry<String, Double> entry : resultMap.entrySet())
		{
			//ignore special keys
			if (entry.getKey().equals(IClassificationStrategy.CLASSIFICATION_REJECT))
				continue;

			//check if this classification was accepted
			if (entry.getValue() > 0)
			{
				//it was, add it to the clone

				if (log.isTraceEnabled())
					log.trace("classify() - adding classification to clone - key: " + entry.getKey() + ", weight: "
							+ entry.getValue());

				clone.addClassification(entry.getKey());
			}
			else
			{
				if (log.isTraceEnabled())
					log.trace("classify() - IGNORING classification - key: " + entry.getKey() + ", weight: "
							+ entry.getValue());
			}

		}

		//check whether we're going to accept or reject the clone
		Result result;
		double rejectResult = resultMap.get(IClassificationStrategy.CLASSIFICATION_REJECT);
		if (rejectResult > 0)
			//ok, the clone was rejected
			result = Result.REJECTED;
		else
			result = Result.ACCEPTED;

		if (log.isTraceEnabled())
			log.trace("classify() - result: " + result + " - added " + clone.getClassifications().size()
					+ " classifications.");

		//we're done
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.IProvider#getProviderName()
	 */
	@Override
	public String getProviderName()
	{
		return "CPC Classifier - Default Classification Provider";
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

	/**
	 * Retrieves a list of the currently registered {@link IClassificationStrategy}s.
	 * 
	 * @return currently registered strategies, may be NULL.
	 */
	public List<ClassificationStrategyDescriptor> getStrategies()
	{
		return registeredStrategies;
	}

	/*
	 * Private methods.
	 */

	/**
	 * Executes all registered strategies in order of their priority till the first strategy
	 * returns {@link IClassificationStrategy.Status#BREAK} or all strategies
	 * have been run.
	 */
	private void callStrategies(Type type, ICloneFile cloneFile, IClone clone, String fileContent, IClone originClone,
			Map<String, Double> result)
	{
		//now run each strategy in turn
		for (ClassificationStrategyDescriptor strategyDescr : registeredStrategies)
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
				IClassificationStrategy.Status status = strategyDescr.getInstance().classify(type, cloneFile, clone,
						fileContent, originClone, result);

				if (log.isTraceEnabled())
					log.trace("callStrategies() - status: " + status);

				if (IClassificationStrategy.Status.BREAK.equals(status))
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
			log.trace("callStrategies() - result: " + result);
	}

	/**
	 * Retrieves all registered {@link IClassificationStrategy} extensions from the
	 * corresponding extension point and adds them to the <em>registeredStrategies</em> list,
	 * ordered by descending priority.
	 */
	private void initialiseStrategies()
	{
		log.trace("initialiseStrategies()");

		registeredStrategies = new LinkedList<ClassificationStrategyDescriptor>();

		IConfigurationElement[] extensions = Platform.getExtensionRegistry().getConfigurationElementsFor(
				EXTENSION_POINT_STRATEGIES);
		for (IConfigurationElement element : extensions)
		{
			try
			{
				ClassificationStrategyDescriptor descriptor = new ClassificationStrategyDescriptor(element);

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

		//		registeredStrategies.add(new MinLengthStrategy());
		//		registeredStrategies.add(new CopyOriginClassificationStrategy());
		//		registeredStrategies.add(new JDTASTStrategy());
	}

}

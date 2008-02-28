package org.electrocodeogram.cpc.classifier.strategy;


import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.electrocodeogram.cpc.classifier.api.strategy.IClassificationStrategy;
import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.data.ICloneFile;
import org.electrocodeogram.cpc.core.api.provider.classification.IClassificationProvider.Type;
import org.electrocodeogram.cpc.core.utils.CoreUtils;


/**
 * A simple strategy which copies the classification of the origin clone if the initial
 * classification of a new clone is requested, the origin is specified and the contents
 * of the new clone and the origin match.<br/>
 * In such a scenario, the classification of the new clone should obviously equal those
 * of the origin clone.<br/>
 * <br/>
 * As a special precaution an additional check ensures that the origin actually has some
 * classifications. Otherwise classification might have failed or was not executed for
 * some reason. In which case we do nothing.<br/>
 * <br/>
 * Returns {@link IClassificationStrategy.Status#BREAK} to prevent all remaining
 * classification strategies from needlessly reclassifying the clone.
 * 
 * @author vw
 */
public class CopyOriginClassificationStrategy implements IClassificationStrategy
{
	private static final Log log = LogFactory.getLog(CopyOriginClassificationStrategy.class);

	public CopyOriginClassificationStrategy()
	{
		log.trace("CopyOriginClassificationStrategy()");
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.classifier.api.strategy.IClassificationStrategy#classify(org.electrocodeogram.cpc.core.api.provider.classification.IClassificationProvider.Type, org.electrocodeogram.cpc.core.api.data.ICloneFile, org.electrocodeogram.cpc.core.api.data.IClone, java.lang.String, org.electrocodeogram.cpc.core.api.data.IClone, java.util.Map)
	 */
	@Override
	public Status classify(Type type, ICloneFile cloneFile, IClone clone, String fileContent, IClone originClone,
			Map<String, Double> result)
	{
		if (log.isTraceEnabled())
			log.trace("classify() - type: " + type + ", cloneFile: " + cloneFile + ", clone: " + clone
					+ ", fileContent: " + CoreUtils.objectToLength(fileContent) + ", originClone: " + originClone
					+ ", result: " + result);
		assert (type != null && cloneFile != null && clone != null && fileContent != null && result != null);

		/*
		 * Check if our preconditions are met.
		 */
		if (!Type.INITIAL.equals(type))
		{
			log.trace("classify() - ignoring non-initial classification request.");
			return Status.SKIPPED;
		}
		else if (originClone == null)
		{
			log.trace("classify() - ignoring classification request without origin data.");
			return Status.SKIPPED;
		}
		else if (!clone.getContent().equals(originClone.getContent()))
		{
			log.trace("classify() - ignoring classification request for clone which no longer matches its origin.");
			return Status.SKIPPED;
		}
		else if (originClone.getClassifications().isEmpty())
		{
			log
					.trace("classify() - ignoring classification request for clone for which the origin has no classifications.");
			return Status.SKIPPED;
		}

		/*
		 * Ok, we can copy over all the classifications from the origin to the new clone.
		 */
		if (log.isTraceEnabled())
			log.trace("classify() - copying classifications from origin: " + originClone.getClassifications());

		for (String classification : originClone.getClassifications())
			result.put(classification, new Double(1));

		/*
		 * We're done and no other strategy should need to modify these classifications
		 * any further.
		 */
		return Status.BREAK;
	}

}

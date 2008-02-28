package org.electrocodeogram.cpc.classifier.strategy;


import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.electrocodeogram.cpc.classifier.CPCClassifierPlugin;
import org.electrocodeogram.cpc.classifier.api.strategy.IClassificationStrategy;
import org.electrocodeogram.cpc.classifier.preferences.CPCPreferenceConstants;
import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.data.ICloneFile;
import org.electrocodeogram.cpc.core.api.provider.classification.IClassificationProvider;
import org.electrocodeogram.cpc.core.utils.CoreUtils;


/**
 * Simple strategy which rejects clones which do not have a specific minimal size. 
 * 
 * @author vw
 */
public class MinLengthStrategy implements IClassificationStrategy
{
	private static final Log log = LogFactory.getLog(MinLengthStrategy.class);

	/**
	 * Weight of the penalty given if any of the limits is violated. 
	 */
	private static final double STRATEGY_WEIGHT = 50.0;

	public MinLengthStrategy()
	{
		log.trace("MinLengthStrategy()");
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.classifier.api.strategy.IClassificationStrategy#classify(org.electrocodeogram.cpc.core.api.provider.classification.IClassificationProvider.Type, org.electrocodeogram.cpc.core.api.data.ICloneFile, org.electrocodeogram.cpc.core.api.data.IClone, java.lang.String, org.electrocodeogram.cpc.core.api.data.IClone, java.util.Map)
	 */
	@Override
	public Status classify(IClassificationProvider.Type type, ICloneFile cloneFile, IClone clone, String fileContent,
			IClone originClone, Map<String, Double> result)
	{
		if (log.isTraceEnabled())
			log.trace("classify() - type: " + type + ", cloneFile: " + cloneFile + ", clone: " + clone
					+ ", fileContent: " + CoreUtils.objectToLength(fileContent) + ", originClone: " + originClone
					+ ", result: " + result);
		assert (type != null && cloneFile != null && clone != null && fileContent != null && result != null);

		boolean modified = false;

		/*
		 * Check for character length
		 */
		int minCharLen = CPCClassifierPlugin.getDefault().getPluginPreferences().getInt(
				CPCPreferenceConstants.PREF_STRATEGY_MINLENGTH_CHARLEN);
		if (clone.getLength() < minCharLen)
		{
			if (log.isDebugEnabled())
				log.debug("classify() - rejecting clone due to char length limit - limit: " + minCharLen + ", clone: "
						+ clone.getLength() + " - " + clone);

			reject(result);
			modified = true;
		}

		/*
		 * Check for token length
		 */
		int minTokenLen = CPCClassifierPlugin.getDefault().getPluginPreferences().getInt(
				CPCPreferenceConstants.PREF_STRATEGY_MINLENGTH_TOKENLEN);
		//TODO: use a more sophisticated java code tokenizer here
		StringTokenizer tokenizer = new StringTokenizer(clone.getContent());
		int tokenCount = tokenizer.countTokens();
		if (tokenCount < minTokenLen)
		{
			if (log.isDebugEnabled())
				log.debug("classify() - rejecting clone due to token length limit - limit: " + minTokenLen
						+ ", clone: " + tokenCount + " - " + clone);

			reject(result);
			modified = true;
		}

		/*
		 * Check for line count
		 */
		int minLineCount = CPCClassifierPlugin.getDefault().getPluginPreferences().getInt(
				CPCPreferenceConstants.PREF_STRATEGY_MINLENGTH_LINECOUNT);
		StringTokenizer lineTokenizer = new StringTokenizer(clone.getContent(), "\n");
		int lineCount = lineTokenizer.countTokens();
		if (lineCount < minLineCount)
		{
			if (log.isDebugEnabled())
				log.debug("classify() - rejecting clone due to line count limit - limit: " + minLineCount + ", clone: "
						+ lineCount + " - " + clone);

			reject(result);
			modified = true;
		}

		if (modified)
			return Status.MODIFIED;
		else
			return Status.SKIPPED;

	}

	private void reject(Map<String, Double> result)
	{
		Double oldWeight = result.get(CLASSIFICATION_REJECT);
		Double newWeight = Math.min(oldWeight + STRATEGY_WEIGHT, Double.MAX_VALUE);
		result.put(CLASSIFICATION_REJECT, newWeight);

		if (log.isTraceEnabled())
			log.trace("reject() - old weight: " + oldWeight + ",  new weight: " + newWeight);
	}
}

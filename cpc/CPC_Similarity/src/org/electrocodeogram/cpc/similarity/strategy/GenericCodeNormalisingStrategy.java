package org.electrocodeogram.cpc.similarity.strategy;


import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.electrocodeogram.cpc.core.api.provider.similarity.ISimilarityProvider;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider;
import org.electrocodeogram.cpc.core.utils.CoreStringUtils;
import org.electrocodeogram.cpc.similarity.api.strategy.ISimilarityStrategy;
import org.electrocodeogram.cpc.similarity.api.strategy.ISimilarityStrategyTask;


/**
 * Simple {@link ISimilarityStrategy} which normalises whitespaces within
 * a non "TEXT" content.
 * 
 * @author vw
 */
public class GenericCodeNormalisingStrategy implements ISimilarityStrategy
{
	private static final Log log = LogFactory.getLog(GenericCodeNormalisingStrategy.class);

	public GenericCodeNormalisingStrategy()
	{
		log.trace("GenericCodeNormalisingStrategy()");
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.similarity.api.strategy.ISimilarityStrategy#calculateSimilarity(org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider, org.electrocodeogram.cpc.similarity.api.strategy.ISimilarityStrategyTask)
	 */
	@Override
	public Status calculateSimilarity(IStoreProvider storeProvider, ISimilarityStrategyTask task)
	{
		if (log.isTraceEnabled())
			log.trace("calculateSimilarity() - task: " + task);
		assert (task != null);

		//make sure we're _not_ looking at a text file
		if (ISimilarityProvider.LANGUAGE_TEXT.equals(task.getLanguage()))
		{
			log.trace("calculateSimilarity() - ignoring text file.");
			return Status.SKIPPED;
		}

		//make sure the file hasn't already been reformatted
		int processingStatus = task.getProcessingStatus();
		if ((processingStatus & ISimilarityStrategyTask.PROCESSING_STATUS_NORMALISED_WHITESPACE) != 0)
		{
			log.trace("calculateSimilarity() - ignoring already normalised file.");
			return Status.SKIPPED;
		}

		//ok, now lets try to normalise the content strings.
		int i = 0;
		for (String content : new String[] { task.getProcessedContent1(), task.getProcessedContent2() })
		{
			StringTokenizer tokeniser = new StringTokenizer(content);
			StringBuilder sb = new StringBuilder(content.length());

			while (tokeniser.hasMoreTokens())
			{
				sb.append(tokeniser.nextToken());
				sb.append(' ');
			}

			String result = sb.toString();
			if (log.isTraceEnabled())
			{
				log.trace("calculateSimilarity() - old content: " + CoreStringUtils.truncateString(content));
				log.trace("calculateSimilarity() - new content: " + CoreStringUtils.truncateString(result));
			}

			if (i == 0)
				task.setProcessedContent1(result, ISimilarityStrategyTask.PROCESSING_STATUS_NORMALISED_WHITESPACE);
			else
				task.setProcessedContent2(result, ISimilarityStrategyTask.PROCESSING_STATUS_NORMALISED_WHITESPACE);
			++i;
		}

		return Status.CONTINUE;
	}

}

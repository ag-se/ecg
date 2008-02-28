package org.electrocodeogram.cpc.similarity.provider;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.utils.CoreStringUtils;
import org.electrocodeogram.cpc.similarity.api.strategy.ISimilarityStrategyTask;


/**
 * Default implementation for {@link ISimilarityStrategyTask}.
 * 
 * @author vw
 */
public class SimilarityStrategyTask implements ISimilarityStrategyTask
{
	private static final Log log = LogFactory.getLog(SimilarityStrategyTask.class);

	private String language;
	private IClone clone1;
	private IClone clone2;

	private int processingStatus = 0;
	private String processedContent1;
	private String processedContent2;

	private double totalScore = 0;
	private double totalWeight = 0;
	private double primaryWeight = -1;
	private boolean forceNonEqual = false;

	/**
	 * Creates a new {@link SimilarityStrategyTask}. Also initialises {@link SimilarityStrategyTask#getProcessedContent1()} & co.
	 * 
	 * @param language the language of the given clone contents, never null.
	 * @param clone1 the first clone, never null.
	 * @param clone2 the second clone, never null.
	 */
	public SimilarityStrategyTask(String language, IClone clone1, IClone clone2)
	{
		if (log.isTraceEnabled())
			log.trace("SimilarityStrategyTask() - language: " + language + ", clone1: " + clone1 + ", clone2: "
					+ clone2);
		assert (clone1 != null && clone2 != null);

		this.language = language;

		this.clone1 = clone1;
		this.processedContent1 = clone1.getContent();

		this.clone2 = clone2;
		this.processedContent2 = clone2.getContent();
	}

	/**
	 * Sets the primary weight for the next {@link SimilarityStrategyTask#addScore(double, double)} call.<br/>
	 * Must not be set by the strategy itself.
	 * 
	 * @param primaryWeight always &gt;=0
	 */
	void setPrimaryWeight(double primaryWeight)
	{
		if (log.isTraceEnabled())
			log.trace("setPrimaryWeight() - primaryWeight: " + primaryWeight);
		assert (primaryWeight >= 0);

		this.primaryWeight = primaryWeight;
	}

	/* (non-Javadoc)
	 * @see org.electrocodeogram.cpc.similarity.provider.ISimilarityStrategyTask#addScore(double, double)
	 */
	@Override
	public void addScore(double score, double weight)
	{
		if (log.isTraceEnabled())
			log.trace("addScore() - score: " + score + ", weight: " + weight + ", primaryWeight: " + primaryWeight);
		assert (score >= 0 && score <= 100 && weight >= 0 && primaryWeight >= 0);

		//ignore all entries with zero weight
		if (weight == 0 || primaryWeight == 0)
		{
			log.trace("addScore() - ignored.");
			return;
		}

		double finalWeight = weight * primaryWeight;
		totalScore += score * finalWeight;
		totalWeight += finalWeight;

		primaryWeight = -1;

		if (log.isTraceEnabled())
			log.trace("addScore() - new totals - totalScore: " + totalScore + ", totalWeight: " + totalWeight);
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.similarity.api.strategy.ISimilarityStrategyTask#isForceNonEqual()
	 */
	@Override
	public boolean isForceNonEqual()
	{
		return forceNonEqual;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.similarity.api.strategy.ISimilarityStrategyTask#markForceNonEqual()
	 */
	@Override
	public void markForceNonEqual()
	{
		log.trace("markForceNonEqual()");

		forceNonEqual = true;
	}

	/* (non-Javadoc)
	 * @see org.electrocodeogram.cpc.similarity.provider.ISimilarityStrategyTask#getAverageScore()
	 */
	@Override
	public int getAverageScore()
	{
		int result = (int) Math.round(totalScore / totalWeight);

		if (forceNonEqual && result == 100)
			--result;

		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.similarity.api.strategy.ISimilarityStrategyTask#getLanguage()
	 */
	@Override
	public String getLanguage()
	{
		return language;
	}

	/* (non-Javadoc)
	 * @see org.electrocodeogram.cpc.similarity.provider.ISimilarityStrategyTask#getClone1()
	 */
	@Override
	public IClone getClone1()
	{
		return clone1;
	}

	/* (non-Javadoc)
	 * @see org.electrocodeogram.cpc.similarity.provider.ISimilarityStrategyTask#getClone2()
	 */
	@Override
	public IClone getClone2()
	{
		return clone2;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.similarity.api.strategy.ISimilarityStrategyTask#getProcessingStatus()
	 */
	@Override
	public int getProcessingStatus()
	{
		return processingStatus;
	}

	/* (non-Javadoc)
	 * @see org.electrocodeogram.cpc.similarity.provider.ISimilarityStrategyTask#getProcessedContent1()
	 */
	@Override
	public String getProcessedContent1()
	{
		return processedContent1;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.similarity.api.strategy.ISimilarityStrategyTask#setProcessedContent1(java.lang.String, int)
	 */
	@Override
	public void setProcessedContent1(String processedContent1, int processingStatus)
	{
		if (log.isTraceEnabled())
			log.trace("setProcessedContent1() - processedContent1: "
					+ CoreStringUtils.truncateString(processedContent1) + ", processingStatus: " + processingStatus);
		assert (processedContent1 != null && processingStatus >= 0);

		this.processedContent1 = processedContent1;
		this.processingStatus |= processingStatus;
	}

	/* (non-Javadoc)
	 * @see org.electrocodeogram.cpc.similarity.provider.ISimilarityStrategyTask#getProcessedContent2()
	 */
	@Override
	public String getProcessedContent2()
	{
		return processedContent2;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.similarity.api.strategy.ISimilarityStrategyTask#setProcessedContent2(java.lang.String, int)
	 */
	@Override
	public void setProcessedContent2(String processedContent2, int processingStatus)
	{
		if (log.isTraceEnabled())
			log.trace("setProcessedContent2() - processedContent2: "
					+ CoreStringUtils.truncateString(processedContent2) + ", processingStatus: " + processingStatus);
		assert (processedContent2 != null && processingStatus >= 0);

		this.processedContent2 = processedContent2;
		this.processingStatus |= processingStatus;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "SimilarityStrategyTask[averageScore: " + getAverageScore() + ", totalScore: " + totalScore
				+ ", totalWeight: " + totalWeight + ", clone1: " + clone1 + ", clone2: " + clone2 + "]";
	}
}

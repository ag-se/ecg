package org.electrocodeogram.cpc.similarity.api.strategy;


import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.provider.similarity.ISimilarityProvider;
import org.electrocodeogram.cpc.similarity.provider.SimilarityStrategyTask;


/**
 * Parameter value for {@link ISimilarityStrategy#calculateSimilarity(org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider, ISimilarityStrategyTask)}.
 * 
 * @author vw
 */
public interface ISimilarityStrategyTask
{
	/**
	 * Set once an {@link ISimilarityStrategy} has filtered out parts of the
	 * the processed contents. I.e. comments.
	 */
	public static final int PROCESSING_STATUS_FILTERED = 1;

	/**
	 * Set once an {@link ISimilarityStrategy} has normalised white spaces of
	 * the processed contents.
	 */
	public static final int PROCESSING_STATUS_NORMALISED_WHITESPACE = 2;

	/**
	 * Set once an {@link ISimilarityStrategy} has normalised identifiers of
	 * the processed contents.
	 */
	public static final int PROCESSING_STATUS_NORMALISED_IDENTIFIERS = 4;

	/**
	 * This method may be called only once per strategy.
	 * 
	 * @param score
	 * @param weight
	 */
	public void addScore(double score, double weight);

	/**
	 * Whether the final similarity between the two clones needs to be capped at 99%.
	 * <br>
	 * This is required to adhere to the {@link ISimilarityProvider} API specification which
	 * states that a similarity of 100% must only be returned if the two code sections
	 * are guaranteed to be semantically equivalent.
	 * <br>
	 * If this value is true, some strategy detected a violation of that semantic equivalence.
	 * 
	 * @return true if this task must never return 100, false otherwise.
	 */
	public boolean isForceNonEqual();

	/**
	 * A strategy <b>has to</b> call this method if it detects any semantic difference
	 * between the two code fragments.
	 * <br>
	 * Once set, this flag can't be unset.
	 * 
	 * @see ISimilarityStrategyTask#isForceNonEqual()
	 */
	public void markForceNonEqual();

	/**
	 * Retrieves the current average weighted score of all strategies which have been
	 * executed for this task.
	 * <br>
	 * Guaranteed to never return 100 if {@link ISimilarityStrategyTask#isForceNonEqual()} is true.
	 * 
	 * @return weighted, average score as integer between 0 and 100.
	 */
	public int getAverageScore();

	/**
	 * Retrieves the language for the given clone fragments.
	 * 
	 * @return the language for the given clone fragments, as specified by {@link ISimilarityProvider}, never null.
	 */
	public String getLanguage();

	/**
	 * Retrieves the first clone for this comparison.
	 * <br>
	 * The returned clone object may not be modified in any way.
	 * 
	 * @return first clone, never null.
	 */
	public IClone getClone1();

	/**
	 * Retrieves the second clone for this comparison.
	 * <br>
	 * The returned clone object may not be modified in any way.
	 * 
	 * @return second clone, never null.
	 */
	public IClone getClone2();

	/**
	 * Retrieves the processing status of the two processed content strings.
	 * <br>
	 * The value is a bit mask, comprised of <em>PROCESSING_STATUS_*</em> values.  
	 * 
	 * @return current processing status bit mask for both content strings.
	 * 
	 * @see ISimilarityStrategyTask#setProcessedContent1(String, int)
	 */
	public int getProcessingStatus();

	/**
	 * Retrieves the processed/normalised content for clone1.
	 * <br>
	 * This value may be updated by each strategy in turn.
	 * 
	 * @return the current processed content for clone1, never null.
	 */
	public String getProcessedContent1();

	/**
	 * Updates the processed/normalised content for clone1.
	 * <br>
	 * Strategies may only update this value. The value of {@link IClone#getContent()} may not be modified.
	 * 
	 * @param processedContent1 the new content for clone1, never null.
	 * @param processingStatus bit mask which indicates the kinds of modifications made to the content,
	 * 		data is additive. There is no need to pass through bits set earlier. Bits can't be unset.
	 * 		There is one bit mask for both contents.
	 */
	public void setProcessedContent1(String processedContent1, int processingStatus);

	/**
	 * Retrieves the processed/normalised content for clone2.
	 * 
	 * @see SimilarityStrategyTask#getProcessedContent1()
	 */
	public String getProcessedContent2();

	/**
	 * Updates the processed/normalised content for clone2.
	 * 
	 * @see #setProcessedContent1(String, int)
	 */
	public void setProcessedContent2(String processedContent2, int processingStatus);

}

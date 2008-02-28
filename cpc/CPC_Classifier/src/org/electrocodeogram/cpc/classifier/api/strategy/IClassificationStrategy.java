package org.electrocodeogram.cpc.classifier.api.strategy;


import java.util.Map;

import org.electrocodeogram.cpc.classifier.provider.ClassificationProvider;
import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.data.ICloneFile;
import org.electrocodeogram.cpc.core.api.provider.classification.IClassificationProvider;


/**
 * API interface for new strategies which want to plug into the <em>CPC Classifier<em>'s
 * {@link IClassificationProvider} implementation.
 * 
 * @author vw
 *
 * @see IClassificationProvider
 * @see ClassificationProvider
 */
public interface IClassificationStrategy
{
	/**
	 * A special classification which indicates whether the clone should be rejected.
	 * 
	 * @see #classify(IClassificationProvider.Type, ICloneFile, IClone, String, IClone, Map)
	 */
	public static final String CLASSIFICATION_REJECT = "cpc.reject";

	/**
	 * Return status indicator for {@link #classify(IClassificationProvider.Type, ICloneFile, IClone, String, IClone, Map)}.
	 */
	public enum Status
	{
		/**
		 * Indicates that the strategy does not apply to the given clone and did not make any modifications.
		 */
		SKIPPED,

		/**
		 * Indicates that the strategy made some modifications to the classification of the clone.
		 */
		MODIFIED,

		/**
		 * Indicates that this event should not be passed on to any more strategies and that
		 * the clone's classification is in it's final stage.
		 * <p>
		 * A strategy will typically return this value if it detected a special situation which may
		 * confuse other strategies or if it needs to make sure that no other strategy will
		 * override its decision. 
		 */
		BREAK,
	}

	/**
	 * Takes a clone object and the content of the file which contains the clone and
	 * tries to find good classifications for the clone.
	 * <p>
	 * A strategy will add its results to the given result map.
	 * <br>
	 * The clone object itself is <b>not</b> modified in any way.
	 * <br>
	 * Keys of the result map are classification strings as defined in {@link IClassificationProvider} or custom
	 * strings defined by 3rd party plugins.
	 * <br>
	 * The values specify the weight of the corresponding classification if an incremental <em>type</em>
	 * is selected, all old classifications are initially added with weight <em>1.0</em> to the result map.
	 * <br>
	 * Each strategy may increase or decrease the values for any classification, according to its own judgement of
	 * the clone.
	 * <br>
	 * After all strategies have been applied, the {@link IClassificationProvider} will keep all classifications with
	 * a weight &gt;0.
	 * <p>
	 * The key {@link #CLASSIFICATION_REJECT} ("<em>cpc.reject</em>") is a special case. Its
	 * value determines whether the clone will be accepted or rejected. The default value is 0.
	 * <br>
	 * If the value is &gt;0 after all strategies have been executed, the clone is rejected.
	 *
	 * @param type {@link IClassificationProvider.Type} classification type, never null.
	 * @param cloneFile the clone file which contains the clone, must not be modified, never null.
	 * @param clone the clone to classify, must not be modified, never null.
	 * @param fileContent the content of the corresponding file, never null.
	 * @param originClone optional origin clone, may be NULL.
	 * @param result a result map with all classifications and their weight,
	 * 			a strategy writes its results to this map, never null.
	 * @return the {@link Status} of the strategy execution, never null.
	 */
	public Status classify(IClassificationProvider.Type type, ICloneFile cloneFile, IClone clone, String fileContent,
			IClone originClone, Map<String, Double> result);
}

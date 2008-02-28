package org.electrocodeogram.cpc.core.api.provider.classification;


import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.data.ICloneFile;
import org.electrocodeogram.cpc.core.api.provider.IProvider;


/**
 * The CPC API for clone classification providers.<br>A classification provider takes a clone objects,
 * analyses it and attaches a number of classifications to it.
 * <br>
 * Classifications are Strings which usually correspond to the <em>CLASSIFICATION_*</em> constants of this class.
 * <p> 
 * 3rd parties may add their own classification strings. Such strings need to have a globally
 * unique prefix to prevent collisions with other classifications.
 * <br>
 * The prefix "<em>cpc.</em>" is reserved for the default CPC classifiers.
 * <br>
 * Classification strings may only contain letters, numbers and dots. Classification strings are case sensitive.
 * 
 * @author vw
 * 
 * @see IClone#hasClassification(String)
 * @see IClone#getClassifications()
 * @see IClone#addClassification(String)
 * @see IClone#removeClassification(String)
 */
public interface IClassificationProvider extends IProvider
{
	/*
	 * Default classifications constants.
	 */

	/**
	 * The clone contains at least one complete java class. 
	 */
	public static final String CLASSIFICATION_CLASS = "cpc.class";

	/**
	 * The clone contains at least one complete java method. 
	 */
	public static final String CLASSIFICATION_METHOD = "cpc.method";

	/**
	 * The clone contains at least one complete loop construct.
	 */
	public static final String CLASSIFICATION_LOOP = "cpc.loop";

	/**
	 * The clone contains at least one complete java condition block.
	 * <br>
	 * I.e. a complete "if () { ... } else { ... }" construct. 
	 */
	public static final String CLASSIFICATION_CONDITION = "cpc.condition";

	/**
	 * The clone contains a complete identifier and nothing else.
	 * Whitespaces and comments are ignored.
	 */
	public static final String CLASSIFICATION_IDENTIFIER = "cpc.identifier";

	/**
	 * The clone contains only comments and whitespaces or a part of a comment.
	 */
	public static final String CLASSIFICATION_COMMENT = "cpc.comment";

	/**
	 * The clone contains potentially complex code.
	 * <br>
	 * A clone should be tagged with this classification if it seems likely
	 * that the clone is non-trivial and that any update anomalies inside
	 * such a clone are potentially interesting candidates for CPC Warnings. 
	 */
	public static final String CLASSIFICATION_COMPLEX = "cpc.complex";

	/**
	 * The clone contains is probably a template code fragment.
	 * <br>
	 * Clones of this kind can be similar to each other, but the underlying
	 * semantics are usually not related.
	 * <br>
	 * This classification should only be set, if there is a high probability
	 * that the decision is correct. Other modules may base their decisions
	 * on this fact. I.e. CPC Notify may decide to ignore a clone modification
	 * if this classification is set.
	 * <br>
	 * However, if it is absolutely clear that there is no point in tracking
	 * this clone at all. The classification provider should return
	 * a {@link Result#REJECTED} result. 
	 */
	public static final String CLASSIFICATION_TEMPLATE = "cpc.template";

	/*
	 * ... more to come
	 */

	/**
	 * Possible results of the {@link #classify(Type, ICloneFile, IClone, String, IClone)} method.
	 */
	public enum Result
	{
		/**
		 * The classifier classified this clone as being
		 * suitable for tracking.
		 */
		ACCEPTED,

		/**
		 * The classifier classified this clone as NOT being
		 * suitable for tracking.
		 * <br>
		 * This clone should simply be ignored.
		 */
		REJECTED,

		/**
		 * Returned if any non-recoverable error occurs during clone classification.
		 * <br>
		 * This indicates that no classification data was added to the clone.
		 * <br>
		 * Users of this API will usually not have to check for this condition, it can
		 * be handled similarly to {@link Result#ACCEPTED} in most situations. 
		 */
		ERROR
	}

	/**
	 * Specifies the type of classification to be performed. 
	 */
	public enum Type
	{
		/**
		 * First classification of a newly created clone.
		 */
		INITIAL,

		/**
		 * Incremental update of classifications of an existing clone which may or may
		 * not have been classified before.
		 * <br>
		 * In this mode old classifications may be preserved if this reduces the processing
		 * effort required for the classification of the clone or if some of the classifications
		 * are not intended to be recalculated after clone creation.
		 * <p>
		 * It is up to the {@link IClassificationProvider} whether this state is actually
		 * handled differently from {@link Type#RECLASSIFY}.
		 */
		INCREMENTAL,

		/**
		 * Complete reclassification of the clone.
		 * <br>
		 * All existing classifications are removed.
		 */
		RECLASSIFY
	}

	/**
	 * Takes a clone object and passes it to all registered classification strategies to decide on
	 * the correct classifications.
	 * <p>
	 * The new classifications are directly added to the clones classifications data structure
	 * (the clone object is updated in place).
	 * <br>
	 * It is up to the specified <em>type</em> and the implementation how existing classification
	 * are handled.
	 * <p>
	 * Providing the file content is optional, however, if it is already present for some reason,
	 * it should be provided to reduce load.
	 * <p>
	 * A classification provider may try to obtain additional information for the corresponding file
	 * from the Eclipse environment, if it is running. I.e. a classification provider may try to obtain
	 * the AST for a Java class.
	 * <br>
	 * It is up to the classification provider implementation whether to make use of any such additional
	 * information or not. 
	 * 
	 * @param type the type of classification to be performed, never null.
	 * @param cloneFile the clone file which contains the given clone, never null.
	 * @param clone the clone to classify, never null.
	 * @param fileContent current content of the file this clone is located in, may be NULL in which case
	 * 		the content will be retrieved from an open editor or the filesystem, when needed.
	 * @param originClone optional reference to the {@link IClone} which the given <em>clone</em> was copied
	 * 		from. This will usually only be available for {@link Type#INITIAL} calls and even then it is
	 * 		optional. May be NULL.
	 * @return the general classification result {@link Result}, never null.
	 */
	public Result classify(Type type, ICloneFile cloneFile, IClone clone, String fileContent, IClone originClone);
}

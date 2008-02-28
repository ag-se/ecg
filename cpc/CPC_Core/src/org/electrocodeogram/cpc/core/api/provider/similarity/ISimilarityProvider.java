package org.electrocodeogram.cpc.core.api.provider.similarity;


import org.electrocodeogram.cpc.core.api.data.CloneDiff;
import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.provider.IProvider;


/**
 * A similarity provider can be used to determine the percentage of similarity between two given {@link IClone} instances.
 * <br>
 * The {@link ISimilarityProvider} interface is implemented by all similarity provider implementations.
 * <p>
 * An implementation will typically offer its own extension API to allow addition, modification or removal of the
 * strategies used to determine the similarity value.  
 * 
 * @author vw
 */
public interface ISimilarityProvider extends IProvider
{
	/**
	 * Possible value for the <em>language</em> parameters of this interface.
	 * <br>
	 * Indicates to the similarity provider that the given clone contents are
	 * <b>potentially</b> valid java source fragments.
	 * <br>
	 * This is only a hint, the source fragments may have invalid syntax or may
	 * not actually be java sources.
	 * <br>
	 * The similarity provider will fall back to {@link ISimilarityProvider#LANGUAGE_TEXT}
	 * if it can't parse the given sources. 
	 */
	public static final String LANGUAGE_JAVA = "java";

	/**
	 * Possible value for the <em>language</em> parameters of this interface.
	 * <br>
	 * Indicates to the similarity provider that the given clone contents are
	 * <b>potentially</b> source fragments in an unknown language.
	 * <br>
	 * The similarity provider may try to normalise white spaces for such cases.
	 */
	public static final String LANGUAGE_OTHER = "other";

	/**
	 * Possible value for the <em>language</em> parameters of this interface.
	 * <br>
	 * Indicates to the similarity provider that the given clone contents are
	 * not sources in any particular programming language and that they should
	 * be handled as plain text.
	 */
	public static final String LANGUAGE_TEXT = "text";

	/**
	 * For future extensions.
	 * 
	 * @see #LANGUAGE_JAVA
	 */
	public static final String LANGUAGE_C_PLUS_PLUS = "cpp";

	/**
	 * For future extensions.
	 * 
	 * @see #LANGUAGE_JAVA
	 */
	public static final String LANGUAGE_C = "c";

	/**
	 * For future extensions.
	 * 
	 * @see #LANGUAGE_JAVA
	 */
	public static final String LANGUAGE_PERL = "perl";

	/**
	 * For future extensions.
	 * 
	 * @see #LANGUAGE_JAVA
	 */
	public static final String LANGUAGE_PHP = "php";

	/**
	 * For future extensions.
	 * 
	 * @see #LANGUAGE_JAVA
	 */
	public static final String LANGUAGE_PYTHON = "python";

	/**
	 * For future extensions.
	 * 
	 * @see #LANGUAGE_JAVA
	 */
	public static final String LANGUAGE_RUBY = "ruby";

	/**
	 * For future extensions.
	 * 
	 * @see #LANGUAGE_JAVA
	 */
	public static final String LANGUAGE_JAVASCRIPT = "js";

	/*
	 * A similarity strategy implementer may support additional/other language strings. 
	 */

	/**
	 * Takes two clones and calculates the similarity of the two clones to each other.
	 * <br>
	 * The similarity is returned as a percent value.
	 * <p>
	 * Similarity is based on the contents of the given clones. The clone uuids are not taken
	 * into account. It is therefore possible to calculate the similarity between two instances
	 * of the same clone.
	 * <p>
	 * A similarity provider may internally acquire a store provider to
	 * obtain additional data for the clones in question, if transientCheck is false.
	 * <br>
	 * I.e. the detailed {@link CloneDiff}s.
	 * <p>
	 * <b>NOTE:</b> A similarity of 100 may <b>only</b> be returned if it can be guaranteed that the two code
	 * fragments are semantically equal. Thus clients of this API can distinguish two classes of
	 * matches, =100 and &lt;100.
	 * 
	 * @param language indication of the potential programming language of the given source fragments, never null.
	 * @param clone1 the first clone to compare, never null.
	 * @param clone2 the second clone to compare, never null.
	 * @param transientCheck true if the given clones might not be in sync with the store provider,
	 * 		in this case any implementation of this interface is forbidden to query the store provider
	 * 		for any additional info about the clones.
	 * @return similarity between the two clones, range: 0-100, 0 = no similarity, 100 = clones are semantically equal.
	 */
	public int calculateSimilarity(String language, IClone clone1, IClone clone2, boolean transientCheck);

	/**
	 * Simple interface for similarity calculation between two strings.
	 * 
	 * @param language indication of the potential programming language of the given source fragments, never null.
	 * @param content1 content of the first clone, never null.
	 * @param content2 content of the second clone, never null.
	 * @return similarity between the two clones, range: 0-100, 0 = no similarity, 100 = clones are semantically equal.
	 * 
	 * @see ISimilarityProvider#calculateSimilarity(String, IClone, IClone, boolean)
	 */
	public int calculateSimilarity(String language, String content1, String content2);
}

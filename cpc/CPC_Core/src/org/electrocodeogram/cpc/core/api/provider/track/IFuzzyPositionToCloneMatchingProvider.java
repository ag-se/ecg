package org.electrocodeogram.cpc.core.api.provider.track;


import java.util.List;

import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.data.ICloneFile;
import org.electrocodeogram.cpc.core.api.provider.IProvider;


/**
 * API specification for fuzzy position to clone matching providers.&nbsp;
 * Such a provider is used by the <em>CPC Track</em> module to check whether any
 * existing clone matches a given position an length.
 * <p>
 * Implementations of this interface are used to find an existing clone during
 * a copy/cut operation. 
 * 
 * @author vw
 */
public interface IFuzzyPositionToCloneMatchingProvider extends IProvider
{
	/**
	 * Checks the specified area in the given file for existing clones and returns
	 * an existing clone if it matches the specified area relatively well.
	 * <p>
	 * A typical difference in area which might not be relevant to the clone itself
	 * are leading and trailing whitespaces.
	 * 
	 * @param cloneFile the {@link ICloneFile} in question, never null.
	 * @param clones a list of all clones within the file, never null.
	 * @param offset the start offset of the area in question, always &gt;=0.
	 * @param length the length of the area in question, always &gt;=0.
	 * @param fileContent the current content of the file, never null.
	 * @return an {@link IClone} instance which matches (fuzzy) the given area or NULL if no such
	 * 		clone was found.
	 */
	public IClone findClone(ICloneFile cloneFile, List<IClone> clones, int offset, int length, String fileContent);
}

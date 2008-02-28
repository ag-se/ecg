package org.electrocodeogram.cpc.core.api.provider.reconciler;


import java.util.List;

import org.electrocodeogram.cpc.core.api.provider.IProvider;


/**
 * A {@link IDiffProvider} provides character based diff services to other components.
 * <p>
 * A character based diff provides a hint at how the differences between two given
 * text fragments may have occurred. There is no guarantee that the returned
 * {@link IDiffResult}s correspond to the real modifications made.
 * <p>
 * The actual diff algorithm used is not specified and it is up to the implementation
 * how diffs are generated.
 * <p>
 * The most prominent user of this provider is the <em>CPC Reconciler</em> module.
 * 
 * @author vw
 * 
 * @see IDiffResult
 */
public interface IDiffProvider extends IProvider
{
	/**
	 * Computes a character based diff between the two given strings.
	 * 
	 * @param oldText the old text, never null.
	 * @param newText the new text, never null.
	 * @return a list of differences between the two strings, never null.
	 */
	public List<IDiffResult> charDiff(String oldText, String newText);

	/*
	 * TODO: consider whether line based diffs are also needed by some
	 * CPC components. If that is the case, it might be a good idea
	 * to add a line based diff method here too.
	 */
}

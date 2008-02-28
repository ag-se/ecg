package org.electrocodeogram.cpc.reconciler.provider;


import java.io.BufferedReader;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.electrocodeogram.cpc.core.api.provider.IManagableProvider;
import org.electrocodeogram.cpc.core.api.provider.reconciler.IDiffProvider;
import org.electrocodeogram.cpc.core.api.provider.reconciler.IDiffResult;
import org.electrocodeogram.cpc.reconciler.utils.diff.LineDiffResult;
import org.electrocodeogram.cpc.reconciler.utils.diff.diffmatchpatch.DMPDiff;
import org.electrocodeogram.cpc.reconciler.utils.diff.diffmatchpatch.DiffMatchPatch;
import org.incava.util.diff.Diff;
import org.incava.util.diff.Difference;


/**
 * Default {@link IDiffProvider} implementation.
 * <p>
 * This class acts as a wrapper for the actual diff implementations.
 * <br>
 * Currently the following implementations are offered.
 * <ul>	
 * 	<li>java-diff library - http://www.incava.org/projects/java/java-diff/</li>
 * 	<li>google diff_match_patch - http://code.google.com/p/google-diff-match-patch/</li>
 * </ul>
 * 
 * @author vw
 * 
 * @see IDiffProvider
 * @see IDiffResult
 */
public class DefaultDiffProvider implements IDiffProvider, IManagableProvider
{
	private static Log log = LogFactory.getLog(DefaultDiffProvider.class);

	/**
	 * Computes a line based diff between the two given strings.
	 * 
	 * @param oldText the old text as a list of lines, never null.
	 * @param newText the new text as a list of lines, never null.
	 * @return a list of differences, never null.
	 * 
	 * @deprecated no longer in use
	 */
	@Deprecated
	@SuppressWarnings("unchecked")
	public static List<LineDiffResult> lineDiff(List<String> oldText, List<String> newText)
	{
		assert (oldText != null && newText != null);

		if (log.isTraceEnabled())
			log.trace("lineDiff() - oldText: " + oldText + ", newText: " + newText);

		List<LineDiffResult> result = new LinkedList<LineDiffResult>();

		Diff diff = new Diff(oldText, newText);
		List differences = diff.diff();

		for (Object difference : differences)
		{
			result.add(new LineDiffResult(((Difference) difference)));
		}

		if (log.isTraceEnabled())
			log.trace("lineDiff() - result: " + result);

		return result;
	}

	/**
	 * Convenience method. Splits the inputs into lines and then delegates the call to
	 * {@link DefaultDiffProvider#lineDiff(List, List)}
	 * 
	 * @param oldText the old text, never null.
	 * @param newText the new text, never null.
	 * @return a list of differences, never null.
	 * 
	 * @deprecated no longer in use
	 */
	@Deprecated
	public static List<LineDiffResult> lineDiff(String oldText, String newText)
	{
		assert (oldText != null && newText != null);

		return lineDiff(splitIntoLines(oldText), splitIntoLines(newText));
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.reconciler.IDiffProvider#charDiff(java.lang.String, java.lang.String)
	 */
	public List<IDiffResult> charDiff(String oldText, String newText)
	{
		assert (oldText != null && newText != null);

		if (log.isTraceEnabled())
			log.trace("newCharDiff() - oldText: " + oldText + ", newText: " + newText);

		List<IDiffResult> result = new LinkedList<IDiffResult>();

		DiffMatchPatch dmp = new DiffMatchPatch();
		LinkedList<DMPDiff> diffs = dmp.diff_main(oldText, newText, false);
		int offset = 0;

		for (DMPDiff diff : diffs)
		{
			if (!DiffMatchPatch.Operation.EQUAL.equals(diff.operation))
				//we're only interested in DELETE and INSERT events
				result.add(new CharDiffResult(offset, diff));

			//keep track of the current offset in the old text
			if (!DiffMatchPatch.Operation.INSERT.equals(diff.operation))
				offset += diff.text.length();
		}

		if (log.isTraceEnabled())
			log.trace("lineDiff() - result: " + result);

		return result;
	}

	/**
	 * Breaks a string into a list of lines. Use the value of the
	 * <code>line.separator</code> system property as the linebreak character.
	 *
	 * @param text the text to convert.
	 * 
	 * @deprecated no longer in use
	 */
	@Deprecated
	public static List<String> splitIntoLines(String text)
	{
		assert (text != null);

		BufferedReader reader = new BufferedReader(new StringReader(text));
		List<String> result = new LinkedList<String>();
		String s;

		try
		{
			while ((s = reader.readLine()) != null)
			{
				result.add(s);
			}
		}
		catch (java.io.IOException e)
		{
			log.error("splitIntoLines() - unable to split text into lines - text: " + text, new Throwable());
		}

		return result;
	}

	/**
	 * Splits a given text string into a {@link Character} array.<br/>
	 * NOTE: this may be SLOW!
	 * 
	 * @param text the text to convert, never null.
	 * @return a Character array containing the entire text, never null.
	 * 
	 * @deprecated no longer in use
	 */
	@Deprecated
	public static Character[] splitIntoCharacters(String text)
	{
		assert (text != null);

		Character[] result = new Character[text.length()];
		int pos = 0;
		for (char c : text.toCharArray())
			result[pos++] = c;

		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.IProvider#getProviderName()
	 */
	@Override
	public String getProviderName()
	{
		return "CPC Reconciler - Default Diff Provider";
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.IManagableProvider#onLoad()
	 */
	@Override
	public void onLoad()
	{
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.IManagableProvider#onUnload()
	 */
	@Override
	public void onUnload()
	{
	}

}

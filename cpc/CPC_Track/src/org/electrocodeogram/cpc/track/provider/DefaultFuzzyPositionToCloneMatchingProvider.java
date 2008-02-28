package org.electrocodeogram.cpc.track.provider;


import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.electrocodeogram.cpc.core.CPCCorePlugin;
import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.data.ICloneFile;
import org.electrocodeogram.cpc.core.api.data.extension.ICloneNonWsPositionExtension;
import org.electrocodeogram.cpc.core.api.data.special.ICreatorClone;
import org.electrocodeogram.cpc.core.api.provider.IManagableProvider;
import org.electrocodeogram.cpc.core.api.provider.data.ICloneFactoryProvider;
import org.electrocodeogram.cpc.core.api.provider.track.IFuzzyPositionToCloneMatchingProvider;
import org.electrocodeogram.cpc.core.utils.CoreClonePositionUtils;
import org.electrocodeogram.cpc.core.utils.CoreStringUtils;


/**
 * Default {@link IFuzzyPositionToCloneMatchingProvider} implementation.
 * <p>
 * This implementation tries to find an existing clone for the given offset and
 * length by applying the following approaches in order. Once a clone instance
 * is found, the processing is aborted. If all approaches fail to find a clone,
 * null is returned. 
 * <ol>
 * 	<li>exact match by offset and length</li>
 *  <li>match by non-whitespace position</li>
 *  <li>loose match by start and end line</li>
 * </ol>
 * 
 * @author vw
 */
public class DefaultFuzzyPositionToCloneMatchingProvider implements IFuzzyPositionToCloneMatchingProvider,
		IManagableProvider
{
	private static final Log log = LogFactory.getLog(DefaultFuzzyPositionToCloneMatchingProvider.class);

	public DefaultFuzzyPositionToCloneMatchingProvider()
	{
		log.trace("DefaultFuzzyPositionToCloneMatchingProvider()");
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.track.IFuzzyPositionToCloneMatchingProvider#findClone(org.electrocodeogram.cpc.core.api.data.ICloneFile, java.util.List, int, int)
	 */
	@Override
	public IClone findClone(ICloneFile cloneFile, List<IClone> clones, int offset, int length, String fileContent)
	{
		if (log.isTraceEnabled())
			log.trace("findClone() - cloneFile: " + cloneFile + ", clones: " + clones + ", offset: " + offset
					+ ", length: " + length + ", fileContent: " + CoreStringUtils.truncateString(fileContent));
		assert (cloneFile != null && clones != null && offset >= 0 && length >= 0 && fileContent != null);
		assert ((offset + length - 1) < fileContent.length());

		/*
		 * If there are no existing clones, we don't need to check anything.
		 */
		if (clones.isEmpty())
		{
			log.trace("findClone() - document contains no clones, returning null.");
			return null;
		}

		//TODO: what should be our criteria for deciding whether a selected text on a copy operation
		//		matches an existing clone in that file?
		//		some possibilities:
		//			a) exact match - start line & start offset and end line & end offset need to match exactly
		//			b) loose by non-whitespace positions
		//			c) loose by line - start line and end line must match
		//			d) loose by similarity - the "similarity" between the clone in question and the selection need to be high
		//			...
		//
		//		for now we go with a)-c)

		IClone result = null;

		/*
		 * a) exact match check
		 */
		log.trace("findClone() - looking for exact match.");
		result = exact_match(clones, offset, length);

		/*
		 * b) loose by non-whitespace positions
		 */
		if (result == null)
		{
			log.trace("findClone() - looking for loose no-whitespace match.");
			result = loose_nows_match(clones, offset, length, fileContent);
		}

		/*
		 * c) loose by line
		 */
		if (result == null)
		{
			log.trace("findClone() - looking for loose same-line match.");
			result = loose_sameline_match(clones, offset, length, fileContent);
		}

		/*
		 * d) loose by similarity
		 * 
		 * TODO: not yet implemented
		 */

		if (log.isTraceEnabled())
			log.trace("findClone() - result: " + result);

		return result;
	}

	/**
	 * Checks whether any clone provides an exact match for the given offset and length.
	 */
	private IClone exact_match(List<IClone> clones, int offset, int length)
	{
		for (IClone clone : clones)
		{
			//TODO: we're taking the first match here, but there might actually be multiple clones
			//		with the same start and end offsets, so another clone might also be a match and
			//		might belong to a larger group.
			if ((clone.getOffset() == offset) && (clone.getLength() == length))
			{
				if (log.isTraceEnabled())
					log.trace("exact_match() - found exact match: " + clone);

				return clone;
			}
		}

		//no exact match found
		return null;
	}

	/**
	 * Checks whether any clone provides an exact match for the given offset and length
	 * once we ignore all whitespaces. 
	 */
	private IClone loose_nows_match(List<IClone> clones, int offset, int length, String fileContent)
	{
		/*
		 * We need the non-whitespace positions of all clones.
		 * Calculate them first.
		 */
		ICloneFactoryProvider cloneFactoryProvider = (ICloneFactoryProvider) CPCCorePlugin.getProviderRegistry()
				.lookupProvider(ICloneFactoryProvider.class);
		assert (cloneFactoryProvider != null);
		CoreClonePositionUtils.extractPositions(cloneFactoryProvider, clones, fileContent);

		/*
		 * Obtain the non-whitespace position for the new "clone".
		 */
		ICreatorClone tmpClone = (ICreatorClone) cloneFactoryProvider.getInstance(IClone.class);
		assert (tmpClone != null);
		tmpClone.setOffset(offset);
		tmpClone.setLength(length);
		tmpClone.setContent(fileContent.substring(offset, offset + length));
		CoreClonePositionUtils.extractPositions(cloneFactoryProvider, Arrays.asList(new IClone[] { tmpClone }),
				fileContent);
		ICloneNonWsPositionExtension tmpNonWsPos = (ICloneNonWsPositionExtension) tmpClone
				.getExtension(ICloneNonWsPositionExtension.class);
		if (tmpNonWsPos == null)
		{
			log.warn("loose_nows_match() - temp. clone has no non-ws position extension, aborting - clone: " + tmpClone
					+ ", extensions: " + tmpClone.getExtensions() + ", clones: " + clones + ", offset: " + offset
					+ ", length: " + length + ", fileContent: " + CoreStringUtils.quoteString(fileContent),
					new Throwable());
			return null;
		}

		/*
		 * Now check each clone.
		 */
		for (IClone clone : clones)
		{
			ICloneNonWsPositionExtension nonWsPos = (ICloneNonWsPositionExtension) clone
					.getExtension(ICloneNonWsPositionExtension.class);
			if (nonWsPos == null)
			{
				log.warn("loose_nows_match() - clone has no non-ws position extension, skipping - clone: " + clone
						+ ", extensions: " + clone.getExtensions() + ", clones: " + clones + ", offset: " + offset
						+ ", length: " + length + ", fileContent: " + CoreStringUtils.quoteString(fileContent),
						new Throwable());
				continue;
			}

			//TODO: we're taking the first match here, but there might actually be multiple clones
			//		with the same non-whitespace start and end offsets, so another clone might also be a match and
			//		might belong to a larger group.
			if ((nonWsPos.getStartNonWsOffset() == tmpNonWsPos.getStartNonWsOffset())
					&& (nonWsPos.getEndNonWsOffset() == tmpNonWsPos.getEndNonWsOffset()))
			{
				if (log.isTraceEnabled())
					log.trace("loose_nows_match() - found non-whitespace position match: " + clone);

				return clone;
			}
		}

		//no exact match found
		return null;
	}

	/**
	 * Loose same line matching of clone positions. Clones are treated as matches if their start and
	 * end lines match.
	 */
	private IClone loose_sameline_match(List<IClone> clones, int offset, int length, String fileContent)
	{
		/*
		 * We build a temporary Document object in order to allow offset->line number lookups.
		 */
		IDocument doc = new Document(fileContent);

		int tmpStartLine = -1;
		int tmpEndLine = -1;

		try
		{
			tmpStartLine = doc.getLineOfOffset(offset);
			tmpEndLine = doc.getLineOfOffset(offset + length - 1);
		}
		catch (BadLocationException e)
		{
			log.error(
					"loose_sameline_match() - failed to extract start and end line for given position, aborting - offset: "
							+ offset + ", length: " + length + ", tmpStartLine: " + tmpStartLine + ", tmpEndLine: "
							+ tmpEndLine + ", fileContent: " + CoreStringUtils.quoteString(fileContent), e);
			return null;
		}

		for (IClone clone : clones)
		{
			/*
			 * get start and endline for thos clone.
			 */
			int startLine = -1;
			int endLine = -1;
			try
			{
				startLine = doc.getLineOfOffset(clone.getOffset());
				endLine = doc.getLineOfOffset(clone.getEndOffset());
			}
			catch (BadLocationException e)
			{
				log.error("loose_sameline_match() - failed to extract start and end line for clone, skipping - clone: "
						+ clone + ", startLine: " + startLine + ", endLine: " + endLine + ", fileContent: "
						+ CoreStringUtils.quoteString(fileContent), e);
				continue;
			}

			if ((startLine == tmpStartLine) && (endLine == tmpEndLine))
			{
				/*
				 * TODO: Once again there may be multiple matches for the given start and end line.
				 * For now we simply take the first match. However, another match might actually be
				 * even better!
				 */
				if (log.isTraceEnabled())
					log.trace("loose_sameline_match() - found same-line match: " + clone);

				return clone;
			}

		}

		//no exact match found
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.core.api.provider.IProvider#getProviderName()
	 */
	@Override
	public String getProviderName()
	{
		return "CPC Track - Default Fuzzy Prosition to Clone Matching Provider";
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

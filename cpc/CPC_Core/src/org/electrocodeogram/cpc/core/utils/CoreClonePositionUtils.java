package org.electrocodeogram.cpc.core.utils;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.data.ICloneObject;
import org.electrocodeogram.cpc.core.api.data.ICloneObjectExtension;
import org.electrocodeogram.cpc.core.api.data.extension.ICloneNonWsPositionExtension;
import org.electrocodeogram.cpc.core.api.data.special.ICloneObjectExtensionMultiStatefulObject;
import org.electrocodeogram.cpc.core.api.data.special.ICloneObjectExtensionStatefulObject;
import org.electrocodeogram.cpc.core.api.data.special.IStatefulObject;
import org.electrocodeogram.cpc.core.api.provider.data.ICloneFactoryProvider;


/**
 * A collection of utility methods which are related to the extraction of clone
 * positions (esp. non-whitespace positions) from file or editor contents.<br/>
 * <br/>
 * This class is not meant to be instantiated. 
 * 
 * @author vw
 * 
 * TODO: rename to CoreCloneUtils
 */
public class CoreClonePositionUtils
{
	private static Log log = LogFactory.getLog(CoreClonePositionUtils.class);

	/**
	 * This class is not meant to be instantiated.
	 */
	private CoreClonePositionUtils()
	{

	}

	//	/**
	//	 * Extracts a <em>ClonePosition</em> from a text given a start and end offset.
	//	 * 
	//	 * @param startOffset The character offset from the beginning of the file at which the
	//	 * 		clone begins. The first character is 0. Whitespaces are counted.<br/>
	//	 * 		<em>content.charAt(startCharPos)</em> should be the first character of the clone.<br/>
	//	 * 		Must be &gt;=0 and &lt;= <em>endCharPos</em> and &lt;= <em>content.length()</em>.
	//	 * @param endOffset The character offset from the beginning of the file at which the
	//	 * 		clone ends. The first character is 0. Whitespaces are counted.<br/>
	//	 * 		<em>content.charAt(endCharPos)</em> should be the last character of the clone.<br/>
	//	 * 		Must be &gt;=0 and &gt;= <em>startCharPos</em> and &lt;= <em>content.length()</em>.
	//	 * @param content The content to extract the position from. Never null.
	//	 * @return the extracted <em>ClonePosition</em> object, never null.
	//	 */
	//	public static IClonePosition extractPosition(ICloneFactoryProvider cloneFactoryProvider, int startOffset,
	//			int endOffset, String content)
	//	{
	//		assert (content != null && startOffset >= 0 && endOffset >= startOffset && endOffset <= content.length());
	//
	//		if (log.isTraceEnabled())
	//			log.trace("extractPosition() - startOffset: " + startOffset + ", endOffset: " + endOffset + ", content: "
	//					+ content);
	//
	//		IClonePosition position = (IClonePosition) cloneFactoryProvider.getInstance(IClonePosition.class);
	//		position.setStartOffset(startOffset);
	//		position.setStartNonWsOffset(-1);
	//		position.setEndOffset(endOffset);
	//		position.setEndNonWsOffset(-1);
	//
	//		int nonWhitespaceCharCount = 0;
	//		char chr;
	//		for (int pos = 0; pos < content.length(); ++pos)
	//		{
	//			chr = content.charAt(pos);
	//
	//			//check if we've reached the start of the clone
	//			if (pos == startOffset)
	//			{
	//				if (log.isTraceEnabled())
	//					log.trace("extractPosition() - found start of position - char: " + chr + ", pos: " + pos
	//							+ nonWhitespaceCharCount);
	//
	//				position.setStartNonWsOffset(nonWhitespaceCharCount);
	//			}
	//
	//			//check if we've reached the end of the clone
	//			if (pos == endOffset)
	//			{
	//				if (log.isTraceEnabled())
	//					log.trace("extractPosition() - found end of position - char: " + chr + ", pos: " + pos
	//							+ nonWhitespaceCharCount);
	//
	//				position.setEndNonWsOffset(nonWhitespaceCharCount);
	//
	//				//ok, we're done now
	//				break;
	//			}
	//
	//			if ((chr != ' ') && (chr != '\t') && (chr != '\n') && (chr != '\r'))
	//			{
	//				//we're looking at a non-whitespace char (no " ",\t,\n,\r)
	//				++nonWhitespaceCharCount;
	//			}
	//		}
	//
	//		//make sure we found the end of the range
	//		if (position.getEndNonWsOffset() == -1)
	//		{
	//			log.error("extractPosition() - unable to find end offset - startOffset: " + startOffset + ", endOffset: "
	//					+ endOffset + ", content: " + content + ", position: " + position);
	//		}
	//
	//		//make sure the result is valid
	//		if (position.getStartNonWsOffset() > position.getEndNonWsOffset())
	//		{
	//			log.error("extractPosition() - startNonWsOffset > endNonWsOffset - startOffset: " + startOffset
	//					+ ", endOffset: " + endOffset + ", content: " + content + ", position: " + position);
	//		}
	//
	//		if (log.isTraceEnabled())
	//			log.trace("extractPosition() - result: " + position);
	//
	//		return position;
	//
	//	}

	/**
	 * Takes a list of {@link IClone} objects which have only the absolute position set and sets
	 * their non-whitespace positions accordingly.<br/>
	 * <br/>
	 * Instead of using this method, extractPosition(ICloneFactoryProvider, int, int, String)
	 * could simply be used multiple times. The end result would be the same but this method
	 * scales better for large clone lists.
	 * 
	 * @param cloneFactoryProvider a valid clone factory, never null.
	 * @param clones a list of clones from one file which should be updated, never null.
	 * @param content the content of the file/editor in which all given clones are located, never null.
	 */
	public static void extractPositions(ICloneFactoryProvider cloneFactoryProvider, List<IClone> clones, String content)
	{
		assert (cloneFactoryProvider != null && clones != null && content != null);

		if (log.isTraceEnabled())
			log.trace("extractPositions() - cloneFactoryProvider: " + cloneFactoryProvider + ", clones: " + clones
					+ ", content: " + CoreStringUtils.truncateString(content));

		//first cache the start and end offsets of all clones
		int startOffset, endOffset;

		Map<Integer, Integer> startPosMap = new HashMap<Integer, Integer>(clones.size());
		Map<Integer, Integer> endPosMap = new HashMap<Integer, Integer>(clones.size());

		for (IClone clone : clones)
		{
			startOffset = clone.getOffset();
			endOffset = clone.getEndOffset();

			//some sanity checking
			if (startOffset < 0 || endOffset < 0 || endOffset < startOffset || startOffset >= content.length()
					|| endOffset >= content.length())
			{
				log.error("extractPositions() - invalid position input - clone: " + clone + ", content length: "
						+ content.length(), new Throwable());
				/*
				 * This shouldn't happen.
				 * If it does we just ignore this clone and continue with the next one.
				 */
				continue;
			}
			else if (!CoreStringUtils.containsNonWhitespace(clone.getContent()))
			{
				log.warn(
						"extractPositions() - clone contains only whitespace characters, this is not supported - clone: "
								+ clone + ", clone content: " + CoreStringUtils.quoteString(clone.getContent()),
						new Throwable());
				continue;
			}

			startPosMap.put(startOffset, -1);
			endPosMap.put(endOffset, -1);
		}

		//now go through the content _once_ and collect all non-whistespace locations in the posMap
		int nonWhitespaceCharCount = 0;
		char chr;
		for (int pos = 0; pos < content.length(); ++pos)
		{
			chr = content.charAt(pos);

			//check if we're interested in this position
			//TODO: check if this is actually faster than just using extractPosition() once for each
			//		clone. Doing one hashmap lookup per character might actually be slower!
			if (startPosMap.containsKey(pos) || endPosMap.containsKey(pos))
			{
				if (log.isTraceEnabled())
					log.trace("extractPositions() - found position - char: " + chr + ", pos: " + pos
							+ ", nonWhitespaceCharCount: " + nonWhitespaceCharCount);

				/*
				 * The normal way of counting nonWhitespaceCharCount works well for start positions.
				 * If the position is located within a whitespace segment this will yield the
				 * non-whitespace position of the next non-whitespace character.
				 */
				if (startPosMap.containsKey(pos))
					startPosMap.put(pos, nonWhitespaceCharCount);

				/*
				 * For end positions we need to do some special handling for the case where the
				 * position is located in a whitespace segment.
				 * The correct position in that case is the nonwhitespace location of the last
				 * nonwhitespace character. Which means decreasing the nonWhitespaceCharCount
				 * by one.
				 */
				if (endPosMap.containsKey(pos))
				{
					if ((chr != ' ') && (chr != '\t') && (chr != '\n') && (chr != '\r'))
					{
						endPosMap.put(pos, nonWhitespaceCharCount);
					}
					else
					{
						if (log.isTraceEnabled())
							log
									.trace("extractPositions() - char at pos is whitespace, decreasing nonWhitespaceCharCount by one, result: "
											+ (nonWhitespaceCharCount - 1));
						endPosMap.put(pos, nonWhitespaceCharCount - 1);
					}
				}
			}

			if ((chr != ' ') && (chr != '\t') && (chr != '\n') && (chr != '\r'))
			{
				//we're looking at a non-whitespace char (no " ",\t,\n,\r)
				++nonWhitespaceCharCount;
			}
		}

		//now we have all non-whitespace locations, lets update the clone entries
		for (IClone clone : clones)
		{
			Integer nonWsStart = startPosMap.get(clone.getOffset());
			Integer nonWsEnd = endPosMap.get(clone.getEndOffset());

			if (nonWsStart == null || nonWsEnd == null || nonWsStart < 0 || nonWsEnd < 0 || nonWsEnd < nonWsStart)
			{
				log.error("extractPositions() - failed to extract non-whitespace locations for clone - clone: " + clone
						+ ", nonWsStart: " + nonWsStart + ", nonWsEnd: " + nonWsEnd, new Throwable());
				/*
				 * This shouldn't happen.
				 * If it does it is a serious problem, but we can't really fix it here and as most of the CPC code does
				 * not use the non-whitespace location data we might actually be able to continue with the
				 * broken data without a problem.
				 */
				continue;
			}

			//attach a non-whitespace position object or update an existing extension
			ICloneNonWsPositionExtension nonWsPosition = (ICloneNonWsPositionExtension) clone
					.getExtension(ICloneNonWsPositionExtension.class);
			if (nonWsPosition == null)
			{
				//we need to create a new position extension
				nonWsPosition = (ICloneNonWsPositionExtension) cloneFactoryProvider
						.getInstance(ICloneNonWsPositionExtension.class);

				clone.addExtension(nonWsPosition);
			}
			else
			{
				if (log.isTraceEnabled())
					log.trace("extractPositions() - reusing existing ICloneNonWsPositionExtension for clone: " + clone
							+ ", nonWsPosition: " + nonWsPosition);
			}

			//update the position
			nonWsPosition.setStartNonWsOffset(nonWsStart);
			nonWsPosition.setEndNonWsOffset(nonWsEnd);

			if (log.isTraceEnabled())
				log.trace("extractPositions() - new ICloneNonWsPositionExtension for clone - " + clone
						+ ", nonWsPosition: " + nonWsPosition);
		}

		if (log.isTraceEnabled())
			log.trace("extractPositions() - done.");
	}

	/**
	 * Calculates the number of non-whitespace characters in a given string.
	 * 
	 * @param content the string to calculate the counts for, never null
	 * @return non-whitespace char count
	 */
	public static int calculateCharCounts(String content)
	{
		assert (content != null);

		int nonWhitespaceCharCount = 0;

		char chr;
		for (int pos = 0; pos < content.length(); ++pos)
		{
			chr = content.charAt(pos);

			//increase char counter if needed
			if ((chr != ' ') && (chr != '\t') && (chr != '\n') && (chr != '\r'))
			{
				//we're looking at a non-whitespace char (no " ",\t,\n,\r)
				++nonWhitespaceCharCount;
			}
		}

		return nonWhitespaceCharCount;
	}

	/**
	 * Checks if two given lists of {@link IClone} instances are "deeply" equal.<br/>
	 * The lists must have the same length, contain the clones in the same order and
	 * each clone must be "deeply" equal to it's pair.
	 * 
	 * @param clonesA a list of clones, may be NULL.
	 * @param clonesB a list of clones, may be NULL.
	 * @return true if the two lists are deeply equal, false otherwise.
	 * 
	 * @see #statefulObjectsEqual(IStatefulObject, IStatefulObject, boolean)
	 */
	public static boolean cloneListsEqual(List<IClone> clonesA, List<IClone> clonesB)
	{
		if (log.isTraceEnabled())
			log.trace("cloneListsEqual() - clonesA: " + clonesA + ", clonesB: " + clonesB);

		if (clonesA == null && clonesB == null)
		{
			log.trace("cloneListsEqual() - TRUE - both are null");
			return true;
		}

		if (clonesA == null || clonesB == null)
		{
			log.trace("cloneListsEqual() - FALSE - one is null");
			return false;
		}

		if (clonesA.size() != clonesB.size())
		{
			log.trace("cloneListsEqual() - FALSE - size differs");
			return false;
		}

		for (int i = 0; i < clonesA.size(); ++i)
		{
			if (!CoreClonePositionUtils.statefulObjectsEqual((IStatefulObject) clonesA.get(i),
					(IStatefulObject) clonesB.get(i), true))
			{
				log.trace("cloneListsEqual() - FALSE - deep equallity check failed for " + (i + 1)
						+ ". clone - cloneA: " + clonesA.get(i) + ", cloneB: " + clonesB.get(i));
				return false;
			}
		}

		log.trace("cloneListsEqual() - TRUE - full match");

		return true;
	}

	/**
	 * Does a "deep" equality check of two {@link IStatefulObject} instances.
	 * 
	 * @param s1 first instance, may be NULL.
	 * @param s2 second instance, may be NULL.
	 * @param ignoreNonStatefulElements if true, non stateful elements will be skipped during comparison.
	 * @return true if both instances are completely equal, false otherwise.
	 * 		Also true if both values are null.
	 */
	public static boolean statefulObjectsEqual(IStatefulObject s1, IStatefulObject s2, boolean ignoreNonStatefulElements)
	{
		if (log.isTraceEnabled())
			log.trace("statefulObjectsEqual() - s1: " + s1 + ", s2: " + s2 + ", ignoreNonStatefulElements: "
					+ ignoreNonStatefulElements);

		/*
		 * Do null checks.
		 */
		if (s1 == null && s2 == null)
			return true;

		if (s1 == null || s2 == null)
		{
			log.trace("statefulObjectsEqual() - FALSE - one element is null");
			return false;
		}

		/*
		 * Ensure that the class is the same.
		 */
		if (s1.getClass() != s2.getClass())
		{
			log.trace("statefulObjectsEqual() - FALSE - class mismatch");
			return false;
		}

		/*
		 * Make sure that all state values match.
		 */
		Map<String, Comparable<? extends Object>> state1 = s1.getState();
		Map<String, Comparable<? extends Object>> state2 = s2.getState();
		if (ignoreNonStatefulElements)
		{
			//hasExtensions of the two elements doesn't always have to match if
			//we haven't recreated some non-stateful extensions
			state1.remove("hasExtensions");
			state2.remove("hasExtensions");
		}
		if (!mapsEqual(state1, state2))
		{
			log.trace("statefulObjectsEqual() - FALSE - state map mismatch");
			return false;
		}

		/*
		 * If this is a multi stateful extension, check all subelements too.
		 */
		if (s1 instanceof ICloneObjectExtensionMultiStatefulObject)
		{
			if (!((ICloneObjectExtensionMultiStatefulObject) s1).getMultiState().equals(
					((ICloneObjectExtensionMultiStatefulObject) s2).getMultiState()))
			{
				log.trace("statefulObjectsEqual() - FALSE - multi state mismatch");
				log.trace("s1 multi state: " + ((ICloneObjectExtensionMultiStatefulObject) s1).getMultiState());
				log.trace("s2 multi state: " + ((ICloneObjectExtensionMultiStatefulObject) s2).getMultiState());
				return false;
			}
		}

		/*
		 * If this is a clone object, make sure the extensions match too.
		 */
		if (s1 instanceof ICloneObject)
		{
			if (getExtensionCount((ICloneObject) s1, ignoreNonStatefulElements) != getExtensionCount((ICloneObject) s2,
					ignoreNonStatefulElements))
			{
				log.trace("statefulObjectsEqual() - FALSE - extension count mismatch");
				return false;
			}

			List<ICloneObjectExtension> extS1 = ((ICloneObject) s1).getExtensions();
			for (ICloneObjectExtension ext1 : extS1)
			{
				if (!(ext1 instanceof ICloneObjectExtensionStatefulObject))
					continue;

				ICloneObjectExtension ext2 = ((ICloneObject) s2).getExtension(ext1.getExtensionInterfaceClass());
				if (ext2 == null)
				{
					log.trace("statefulObjectsEqual() - FALSE - extension missing: " + ext1);
					return false;
				}

				if (!statefulObjectsEqual((ICloneObjectExtensionStatefulObject) ext1,
						(ICloneObjectExtensionStatefulObject) ext2, ignoreNonStatefulElements))
				{
					log.trace("statefulObjectsEqual() - FALSE - extension mismatch");
					return false;
				}
			}

			List<ICloneObjectExtension> extS2 = ((ICloneObject) s2).getExtensions();
			for (ICloneObjectExtension ext2 : extS2)
			{
				if (!(ext2 instanceof ICloneObjectExtensionStatefulObject))
					continue;

				ICloneObjectExtension ext1 = ((ICloneObject) s1).getExtension(ext2.getExtensionInterfaceClass());
				if (ext1 == null)
				{
					log.trace("statefulObjectsEqual() - FALSE - extension missing: " + ext2);
					return false;
				}

				if (!statefulObjectsEqual((ICloneObjectExtensionStatefulObject) ext1,
						(ICloneObjectExtensionStatefulObject) ext2, ignoreNonStatefulElements))
				{
					log.trace("statefulObjectsEqual() - FALSE - extension mismatch");
					return false;
				}
			}
		}

		log.trace("statefulObjectsEqual() - TRUE - full match");

		return true;
	}

	/**
	 * Checks whether the two given maps contain exactly the same elements.<br/>
	 * The maps are expected to contain only values which can be found in
	 * maps returned by {@link IStatefulObject#getState()}.
	 * 
	 * @param m1 first map, may be NULL.
	 * @param m2 second map, may be NULL.
	 * @return true if both maps contain equal data, false otherwise.
	 * 		Also true if both maps are null.
	 */
	private static boolean mapsEqual(Map<String, Comparable<? extends Object>> m1,
			Map<String, Comparable<? extends Object>> m2)
	{
		//TODO: this method is not needed in our case?
		//the default equals() implementation of Map might be good enough.

		if (m1 == null && m2 == null)
			return true;

		if (m1 != null)
			return m1.equals(m2);

		return false;
	}

	private static int getExtensionCount(ICloneObject cloneObject, boolean ignoreNonStatefulElements)
	{
		if (cloneObject == null)
			return 0;

		int count = 0;
		for (ICloneObjectExtension ext : cloneObject.getExtensions())
		{
			if (!(ext instanceof ICloneObjectExtensionStatefulObject))
				continue;

			++count;
		}

		return count;
	}

}

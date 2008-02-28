package org.electrocodeogram.cpc.merge.utils;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.data.special.IStatefulObject;
import org.electrocodeogram.cpc.core.utils.CoreClonePositionUtils;


public class MergeUtils
{
	private static final Log log = LogFactory.getLog(MergeUtils.class);

	public static final int DIFF_TYPE_UNCHANGED = 0;
	public static final int DIFF_TYPE_MOVED = 1;
	public static final int DIFF_TYPE_MODIFIED = 2;

	private MergeUtils()
	{
		//this class is not meant to be instantiated
	}

	/**
	 * Checks in what way two given {@link IClone} instances differ.<br/>
	 * Returns:
	 * <ul>
	 * 	<li>DIFF_TYPE_UNCHANGED</li>
	 * 	<li>DIFF_TYPE_MOVED</li>
	 * 	<li>DIFF_TYPE_MODIFIED</li>
	 * 	<li>DIFF_TYPE_MOVED + DIFF_TYPE_MODIFIED</li>
	 * </ul>
	 * 
	 * @param cloneA first clone, never null.
	 * @param cloneB second clone, never null.
	 * @return bitmask of DIFF_TYPE_MOVED and DIFF_TYPE_MODIFIED or DIFF_TYPE_UNCHANGED.
	 */
	public static int evaluateDifference(IClone cloneA, IClone cloneB)
	{
		if (log.isTraceEnabled())
			log.trace("evaluateDifference() - cloneA: " + cloneA + ", cloneB: " + cloneB);
		assert (cloneA != null && cloneB != null);

		int result = DIFF_TYPE_UNCHANGED;

		if (cloneA.getContent() != cloneB.getContent())
			//clone content was modified
			result += DIFF_TYPE_MODIFIED;

		/*
		 * The "moved" check is tricky, as it needs to take into account _all_ fields of
		 * the two clone instances as well as their registered extensions.
		 */
		if (!CoreClonePositionUtils.statefulObjectsEqual((IStatefulObject) cloneA, (IStatefulObject) cloneB, true))
		{
			//TODO: this does not check whether the only difference between the two clones is the content.
			//Which means that we'll always return MODIFIED+MOVED and never MODIFIED alone, if the content
			//of a clone was modified.
			result += DIFF_TYPE_MOVED;
		}

		if (log.isTraceEnabled())
			log.trace("evaluateDifference() - result: " + result);

		return result;
	}

}

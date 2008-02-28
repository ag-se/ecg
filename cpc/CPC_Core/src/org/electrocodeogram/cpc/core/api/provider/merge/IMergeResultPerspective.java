package org.electrocodeogram.cpc.core.api.provider.merge;


import java.util.List;

import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.hub.event.CloneModificationEvent;


/**
 * An {@link IMergeResultPerspective} describes the changes made during an {@link IMergeProvider}
 * merge of local and remote clone data from either the local or the remote perspective.
 * <p>
 * It can be thought of as a "diff" which can be applied to the former clone data of the
 * corresponding "side" and which will then yield the new merged clone data.
 * <p>
 * The resulting clone data for both "sides" will always be the same. But depending on the
 * perspective a clone may fall into different "change categories".
 * <p>
 * Some examples:
 * <ul>
 * 	<li>A clone which was created on this side will fall into the "unchanged" (or maybe "moved", depending on merge)
 * 		category on this side and into the "added" category on the other side.</li>
 *  <li>A clone which changed its position due to editing of the document on the other side
 *  	(and for which the position remained unchanged on this side) will fall into the "moved"
 *  	category on this side and into the "unchanged" (or maybe "moved", depending on merge) category on the other side.</li>
 * </ul>
 * 
 * @author vw
 */
public interface IMergeResultPerspective
{
	/**
	 * Retrieves a human readable name for this perspective.
	 * <br>
	 * By default this is either "<em>local</em>" or "<em>remote</em>".
	 * 
	 * @return the name for this perspective, never null.
	 */
	public String getName();

	/**
	 * A list of new clones which were added due to actions on the other "side".
	 * <p>
	 * Due to the uniqueness of clone UUIDs a newly added clone can't be part of both perspectives.
	 * <p>
	 * The order of the {@link IClone} instances in this list is not defined.
	 * 
	 * @return a list of {@link IClone} instances, may be empty, never null.
	 */
	public List<IClone> getAddedClones();

	/**
	 * A list of former clones of this "side" which were moved due to actions on the other "side".
	 * <br>
	 * This also includes clone instances which had any other values (beside the content)
	 * modified, i.e. extension data.
	 * <p>
	 * The order of the {@link IClone} instances in this list is not defined.
	 * 
	 * @return a list of {@link IClone} instances, may be empty, never null.
	 * 
	 * @see CloneModificationEvent#getMovedClones()
	 */
	public List<IClone> getMovedClones();

	/**
	 * A list of former clones of this "side" for which the <b>content</b> was modified due to actions
	 * on the other "side".
	 * <p>
	 * The order of the {@link IClone} instances in this list is not defined.
	 * 
	 * @return a list of {@link IClone} instances, may be empty, never null.
	 * 
	 * @see CloneModificationEvent#getModifiedClones()
	 */
	public List<IClone> getModifiedClones();

	/**
	 * A list of former clones of this "side" which were removed due to user actions on the other "side".
	 * <p>
	 * Clones which were removed on both sides will be part of both perspectives.
	 * <p>
	 * The order of the {@link IClone} instances in this list is not defined.
	 * 
	 * @return a list of {@link IClone} instances, may be empty, never null.
	 */
	public List<IClone> getRemovedClones();

	/**
	 * A list of former clones of this "side" which were dropped due to merge conflicts.
	 * <p>
	 * Lost clones which existed on both sides will be part of both perspectives.
	 * <p>
	 * The order of the {@link IClone} instances in this list is not defined.
	 * 
	 * @return a list of {@link IClone} instances, may be empty, never null.
	 */
	public List<IClone> getLostClones();

	/**
	 * A list of former clones of this "side" which were not affected by this merge.
	 * <p>
	 * These clones are always part of both perspectives.
	 * <p>
	 * The order of the {@link IClone} instances in this list is not defined.
	 * 
	 * @return a list of {@link IClone} instances, may be empty, never null.
	 */
	public List<IClone> getUnchangedClones();

}

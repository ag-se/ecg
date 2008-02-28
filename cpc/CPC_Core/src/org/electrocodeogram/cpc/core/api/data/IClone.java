package org.electrocodeogram.cpc.core.api.data;


import java.util.Collection;
import java.util.Date;

import org.electrocodeogram.cpc.core.api.data.collection.ICloneInterfaces;
import org.electrocodeogram.cpc.core.api.data.special.IStatefulObject;
import org.electrocodeogram.cpc.core.api.provider.classification.IClassificationProvider;
import org.electrocodeogram.cpc.core.api.provider.data.ICloneFactoryProvider;
import org.electrocodeogram.cpc.core.api.provider.notification.IEvaluationResult;


/**
 * Public interface for all clone data objects.
 * <p>
 * This interface lists all methods which are available to all CPC plug-ins and 3rd party
 * contributions.
 * <p>
 * Additional methods are defined by more specific sub-interfaces which
 * belong to individual CPC plugins and are to be considered private.<br/>
 * Any CPC plugin other than the one designated in the sub-interface API must not access
 * such methods.<br/>
 * <p>
 * <b>Any implementation needs to implement {@link ICloneInterfaces}.
 * Implementing only {@link IClone} is not enough!</b>
 * 
 * @author vw
 * 
 * @see ICloneInterfaces
 * @see ICloneFactoryProvider
 */
public interface IClone extends Comparable<IClone>, ICloneObject
{
	/**
	 * {@link IStatefulObject} persistence class identifier, value: "<em>clone</em>"
	 */
	public final String PERSISTENCE_CLASS_IDENTIFIER = "clone";

	/**
	 * Specifies the state of a clone.
	 * <br>
	 * The state influences the way a clone is handled by some CPC modules.
	 * <br>
	 * Additional information may be attached to a state.
	 * 
	 * @see #getCloneState()
	 * @see #getCloneStateChangeDate()
	 * @see #getCloneStateWeight()
	 * @see #getCloneStateMessage()
	 * @see #setCloneState(State, double, String)
	 */
	public enum State
	{
		/**
		 * The clone was not modified or the modifications were judged not to be
		 * of consequence by the CPC Notification module.
		 * <br>
		 * Furthermore the <b>same holds for all members of this clone's clone group</b>.
		 * Meaning that this clone and all it's clone group members are semantically
		 * equal or very nearly so.
		 * <p>
		 * Another way of looking at this would be to consider {@link State#DEFAULT}
		 * as <em>IN SYNC</em> and all other states (besides {@link State#IGNORE}) as
		 * <em>NOT IN SYNC</em>.
		 * <p>
		 * The CPC ruler will display these clones in green.
		 * <br>
		 * No marker is added for clones of this this state.
		 * <br>
		 * This is the initial state for all newly created clone instances.
		 */
		DEFAULT,

		/**
		 * The clone was not modified after the modifications made during its initial
		 * creation. Any changes made right after the clone was created are considered
		 * parametrisations of the clone and do not represent modifications which need
		 * to be propagated to other group members.
		 * <p>
		 * The same state applies to all members of this clone's clone group.
		 * <p> 
		 * No marker is added for clones of this state.
		 */
		CUSTOMISED,

		/**
		 * The clone was modified and the modification was judged to be noteworthy
		 * by the CPC Notification module. However, it was not enough to warrant
		 * a state of {@link State#NOTIFY} or even {@link State#WARN}.
		 * <p>
		 * Another use for this state are clones which had one of their clone
		 * group members modified. Once any member of a clone group changes to state
		 * {@link State#MODIFIED}, {@link State#NOTIFY} or {@link State#WARN}, all
		 * other members of the clone group which are currently in state
		 * {@link State#DEFAULT} should be changed into this state. 
		 * <p>
		 * The CPC ruler will display these clones in blue.
		 * <br>
		 * No marker is added for clones of this this state.
		 */
		MODIFIED,

		/**
		 * A notification of type NOTIFY is pending for this clone.
		 * <p>
		 * The CPC ruler will display these clones in yellow.
		 * <br>
		 * An information marker is added for clones of this this state.
		 */
		NOTIFY,

		/**
		 * A notification of type WARN is pending for this clone.
		 * <p>
		 * The CPC ruler will display these clones in red.
		 * <br>
		 * A warning marker is added for clones of this this state.
		 */
		WARN,

		/**
		 * The user requested that this clone should be ignored from now on.
		 * <br>
		 * Its position will still be tracked and it will still be shown in clone views
		 * but at no point will any notifications or warnings be issued for a clone
		 * of this state.
		 * <p>
		 * If this clone is member of a clone group, notifications will still be generated
		 * for changes within the other members of that group. However, the ignored clone
		 * will not be taken into account for the evaluation of modifications in other
		 * group members. In effect this is very similar to making the ignored clone leave
		 * the group. The only difference is that the clone could be "unignored" later and
		 * would rejoin its clone group as a normal group member.
		 * <p>
		 * The CPC ruler will display these clones in gray.
		 * <br>
		 * No marker is added for clones of this this state.
		 */
		IGNORE,

		/**
		 * The clone is the only member of its group.
		 * <br>
		 * Depending on the CPC configuration such clones will be periodically purged
		 * or are kept forever (research purposes). It is also up to the CPC configuration
		 * whether such "standalone" clones will be displayed to the user or whether
		 * they are hidden.
		 */
		ORPHAN
	}

	/*
	 * Fixed values which do not change over the lifetime of a clone object.
	 */

	/**
	 * Retrieves the creation date of this clone.
	 * 
	 * @return creation date, never null.
	 */
	public Date getCreationDate();

	/**
	 * Retrieves the creator (username) of this clone.
	 * <br>
	 * This value may be null if the creator could not be determined.
	 * 
	 * @return creator of this clone, may be NULL.
	 */
	public String getCreator();

	/**
	 * Retrieves the UUID for the clone file in which this clone is located.
	 * 
	 * @return clone file uuid, never null.
	 */
	public String getFileUuid();

	/*
	 * Position related values.
	 */

	/**
	 * Retrieves the offset of the first character which is part of this clone.
	 * 
	 * @return the character offset to the beginning of the file at which the clone begins,
	 * 			inclusive, first char is 0.
	 */
	public int getOffset();

	/**
	 * Sets the offset of the first character which is part of this clone.
	 * 
	 * @param offset the character offset to the beginning of the file at which the clone begins,
	 * 		inclusive, first char is 0.
	 * 
	 * @see #getOffset()
	 */
	public void setOffset(int offset);

	/**
	 * Retrieves the length of this clone.
	 * <br>
	 * A clone can't have length 0.
	 * 
	 * @return length in characters, never <=0.
	 */
	public int getLength();

	/**
	 * Sets the length of this clone.
	 * 
	 * @param length the length in characters, never <=0.
	 * 
	 * @see #getLength()
	 */
	public void setLength(int length);

	/*
	 * Relation to other clone objects.
	 */

	/**
	 * Retrieves the UUID of the clone group which this clone belongs to.
	 * <br>
	 * Initially a clone belongs to no group, in which case this value is NULL.
	 * <br>
	 * As long as a clone is a member of a clone group, this value is non-NULL.
	 * 
	 * @return clone group for this clone, if any, may be NULL
	 */
	public String getGroupUuid();

	/**
	 * Sets the UUID of the clone group which this clone belongs to.
	 * 
	 * @param groupUuid the clone group for this clone, if any, may be NULL
	 * 
	 * @see #getGroupUuid()
	 */
	public void setGroupUuid(String groupUuid);

	/**
	 * Retrieves the origin clone from which this clone was copied.
	 * <br>
	 * May be null if this clone has no origin (i.e. it is only the source for other clones).
	 * <br>
	 * A clone's origin may also be deleted, in which case this value is reset to NULL.
	 * 
	 * @return uuid of the origin clone of this clone or NULL if no origin exists.
	 */
	public String getOriginUuid();

	/**
	 * Sets the origin clone for this clone.
	 * <br>
	 * This method is usually only used during the creation of the clone but may be used
	 * again at a later point to reset the origin uuid to NULL in case the origin clone
	 * was deleted.
	 * 
	 * @param originUuid uuid of the origin clone, may be NULL.
	 */
	public void setOriginUuid(String originUuid);

	/*
	 * Classification
	 */

	/**
	 * Returns a collection with all classifications of this clone.
	 * <br>
	 * All elements of the collection are unique, there are no NULL elements.
	 * <p>
	 * <b>NOTE:</b> The returned collection may not be modified in any way.
	 * It may or may not be backed by the internal classification data structure.
	 * A client who wants to iterate over the set while the clone might be
	 * concurrently modified, should create its own shallow copy.
	 * <p>
	 * Refer to the <em>CLASSIFICATION_*</em> constants in {@link IClassificationProvider} for more information.
	 * 
	 * @return classification of this clone, collection must not be modified, never null.
	 * 
	 * @see IClassificationProvider
	 */
	public Collection<String> getClassifications();

	/**
	 * Checks whether this clone possesses the given classification.
	 * <p>
	 * Refer to the <em>CLASSIFICATION_*</em> constants of the {@link IClassificationProvider} for more information.
	 * 
	 * @param classification the classification to check for, never null.
	 * @return true if the clone has that classification, false otherwise.
	 * 
	 * @see IClassificationProvider
	 */
	public boolean hasClassification(String classification);

	/**
	 * Adds the given classification string to this clone.
	 * <br>
	 * Multiple additions of the same string have no effect.
	 * <p>
	 * Refer to the <em>CLASSIFICATION_*</em> constants of the {@link IClassificationProvider} for more information.
	 * 
	 * @param classification the classification string to add, never null.
	 * 
	 * @see IClassificationProvider
	 */
	public void addClassification(String classification);

	/**
	 * Removes the given classification from this clone.
	 * <br>
	 * Has no effect if the clone did not possess the classification.
	 * <p>
	 * Refer to the <em>CLASSIFICATION_*</em> constants of the {@link IClassificationProvider} for more information.
	 * 
	 * @param classification the classification string to remove, never null.
	 * 
	 * @see IClassificationProvider
	 */
	public void removeClassification(String classification);

	/*
	 * Clone content.
	 */

	/**
	 * Retrieves the original content of this clone at the time of its creation.
	 * <br>
	 * Calling this method may be expensive as contents may be lazy loaded.
	 * 
	 * @return original content of clone, never null.
	 */
	public String getOriginalContent();

	/**
	 * Retrieves the current content of this clone.
	 * <br>
	 * Calling this method may be expensive as contents may be lazy loaded.
	 * 
	 * @return current content of clone, never null.
	 */
	public String getContent();

	/**
	 * Retrieves the date of the last modification to this clone's content.
	 * <br>
	 * Initially this value matches the {@link IClone#getCreationDate()}.
	 * 
	 * @return modification date, never null.
	 */
	public Date getModificationDate();

	/*
	 * Clone state
	 */

	/**
	 * Retrieves the {@link State} of this clone instance.
	 * 
	 * @return current state of this clone, never null.
	 */
	public State getCloneState();

	/**
	 * Retrieves the date of the last modification to this clone's {@link State}.
	 * <br>
	 * This value is automatically updated to the current time, whenever the clone state is modified.
	 * <br>
	 * Initially this value matches the {@link IClone#getCreationDate()}.
	 * 
	 * @return date of last state change, never null.
	 * 
	 * @see #setCloneState(State, double, String)
	 */
	public Date getCloneStateChangeDate();

	/**
	 * Retrieves the date of the last dismissal of a cpc notification for this clone by the user.
	 * <br>
	 * The date can be used to retrieve the clone content from the history at the point in time where
	 * the notification was dismissed.
	 * <br>
	 * In most cases this value will be NULL.
	 *  
	 * @return date of last dismissal of a cpc notification, may be NULL.
	 */
	public Date getCloneStateDismissalDate();

	/**
	 * Retrieves the weight of the current clone state of this clone.
	 * <br>
	 * This value only has a meaning for the states {@link State#NOTIFY} and {@link State#WARN}.
	 * Otherwise the value is 0.
	 * 
	 * @return the weight of the current clone state, 0 if the state is neither {@link State#NOTIFY}
	 * 		nor {@link State#WARN}.
	 * 
	 * @see IEvaluationResult#getWeight()
	 */
	public double getCloneStateWeight();

	/**
	 * Retrieves an optional message which contains the rationale for the current clone state of this clone.
	 * <br>
	 * This value is only defined for the states {@link State#NOTIFY} and {@link State#WARN}. However, the value is
	 * optional and can be NULL at any time.
	 * <br>
	 * For all other states the value is always NULL.
	 * <p>
	 * This value is displayed to the user and should therefore be human readable and localised.
	 * 
	 * @return human readable reason behind the current clone state, this value may be NULL if no reason was given
	 * 		or if the clone state is neither {@link State#NOTIFY} nor {@link State#WARN}.
	 */
	public String getCloneStateMessage();

	/**
	 * Sets the {@link State} of this clone instance.
	 * <br>
	 * A call to this method will also update {@link #getCloneStateChangeDate()}.
	 * 
	 * @param cloneState new state for this clone, never null.
	 * @param weight weight of the new state. This value only has a meaning for 
	 * 		 states {@link State#NOTIFY} and {@link State#WARN} and should be 0 for all others.
	 * @param message optional human readable reason behind the new clone state. This value should only
	 * 		be defined for states {@link State#NOTIFY} and {@link State#WARN} (even then it may be NULL)
	 * 		and should be NULL for all other states.
	 * 
	 * @see #getCloneState()
	 * @see #getCloneStateChangeDate()
	 * @see #getCloneStateWeight()
	 * @see #getCloneStateMessage()
	 */
	public void setCloneState(State cloneState, double weight, String message);

	/**
	 * Whether this clone instance should be persisted or not.
	 * <br>
	 * {@link IClone} instances may be created for temporary use, i.e. to keep track of the source
	 * for CutCopyPaste actions. Such transient {@link IClone} instances must be tracked like
	 * normal instances (their position can change due to modifications to the file), however they
	 * are not yet part of any clone group and are therefore not real clones. Such transient instances
	 * must not be persisted.
	 * 
	 * @return <em>true</em> if this clone should not yet be persisted, <em>false</em> otherwise.
	 */
	public boolean isTransient();

	/**
	 * Specifies whether this clone instance should be persisted or not.
	 * 
	 * @param _transient <em>true</em> if this clone should not yet be persisted, <em>false</em> otherwise.
	 * 
	 * @see IClone#isTransient()
	 */
	public void setTransient(boolean _transient);

	/*
	 * Convenience methods
	 */

	/**
	 * Returns the offset of the last character which is still part of this clone.
	 * <br>
	 * Use {@link IClone#setOffset(int)} and {@link IClone#setLength(int)} to modify this value.
	 * <br>
	 * Convenience method.
	 * 
	 * @return offset + length - 1
	 */
	public int getEndOffset();

	/**
	 * Checks whether two clone positions intersect.
	 * <br>
	 * Convenience method.
	 * 
	 * @param clone the other clone to compare against, never null.
	 * @return true if the two position ranges have at least one character in common, false otherwise.
	 */
	public boolean intersects(IClone clone);

	/**
	 * Checks whether this clone intersect with the given range.
	 * <br>
	 * Convenience method.
	 * 
	 * @param offset start offset, 0-based character count, always &gt;= 0.
	 * @param length length in characters, always &gt;= 0.
	 * 		A length of 0 is handled like a length of 1 (endOffset=offset).
	 * 		Which means a 0-length range may intersect with another range.
	 * @return true if the two position ranges have at least one character in common, false otherwise.
	 */
	public boolean intersects(int offset, int length);

	/*
	 * Comparable
	 */

	/**
	 * This is a somewhat tricky implementation of compareTo().
	 * <br>
	 * By contract <em>this.compareTo(o) == 0</em> must always yield the same result
	 * as <em>this.equals(o)</em>.
	 * <p>
	 * However, we're ordering by start line, start offset, end offset here.<br/>
	 * Two clones which are not equal may well start at the same line/offset.<br/>
	 * If this happens some extra code tries to resolve the issue by putting one
	 * of the clones first and the other one second.
	 */
	public int compareTo(IClone o);

}

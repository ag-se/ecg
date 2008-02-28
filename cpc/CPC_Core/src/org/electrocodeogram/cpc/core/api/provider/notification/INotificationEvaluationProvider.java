package org.electrocodeogram.cpc.core.api.provider.notification;


import java.util.List;

import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.data.ICloneGroup;
import org.electrocodeogram.cpc.core.api.data.extension.ICloneModificationHistoryExtension;
import org.electrocodeogram.cpc.core.api.hub.event.CloneModificationEvent;
import org.electrocodeogram.cpc.core.api.hub.event.CloneNotificationEvent;
import org.electrocodeogram.cpc.core.api.provider.IProvider;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider;


/**
 * A notification evaluation provider is used to determine whether a specific clone modification should trigger
 * a user notification/warning or whether it should be ignored.
 * <br>
 * The {@link INotificationEvaluationProvider} interface is implemented by all notification evaluation providers.
 * <p>
 * Like all providers, this implementation in itself is passive. Usually the plugin which provides the implementation
 * will also provide some harness code which listens for {@link CloneModificationEvent}s and delegates the
 * evaluation of each modified clone to this provider. The {@link IEvaluationResult} of the provider is then used
 * by the harness code to update the clone data accordingly.
 * 
 * @author vw
 */
public interface INotificationEvaluationProvider extends IProvider
{

	/**
	 * Takes an {@link IClone} instance which was recently modified by the user and
	 * a list of all members of its {@link ICloneGroup} and evaluates how the
	 * modification should be handled.
	 * <p>
	 * A notification evaluation provider may internally acquire additional information
	 * from other sources, if needed. I.e. from the registered store provider. 
	 * 
	 * @param modifiedClone the clone which was modified, never null.
	 * 		Data on the modifications made since the last notification check are
	 * 		attached to the clone as an {@link ICloneModificationHistoryExtension} object,
	 * 		if this is the initial evaluation of the clone. For re-evaluations the
	 * 		modification history is empty.
	 * 		The clone itself is guaranteed to be a member of a non-empty clone group. 
	 * @param groupMembers a list of all members of modifiedClone's clone group,
	 * 		modifiedClone itself is also part of the list, may be NULL.
	 * 		If this is NULL, the implementation will internally acquire the clone group
	 * 		data from the {@link IStoreProvider}.
	 * @param initialEvaluation true if this is the first time this modification is evaluated.
	 * 		Typically this is set to true when the modification is first seen as an
	 * 		{@link CloneModificationEvent} and set to false for later re-evaluations due to
	 * 		(delayed) {@link CloneNotificationEvent}s.
	 * @return the {@link IEvaluationResult} for this modification, never null.
	 * 
	 * @see IEvaluationResult
	 */
	public IEvaluationResult evaluateModification(IClone modifiedClone, List<IClone> groupMembers,
			boolean initialEvaluation);

}

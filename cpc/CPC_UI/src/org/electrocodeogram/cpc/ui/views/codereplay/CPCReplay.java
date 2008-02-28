package org.electrocodeogram.cpc.ui.views.codereplay;


import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.electrocodeogram.cpc.core.CPCCorePlugin;
import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.data.extension.ICloneModificationHistoryExtension;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider;
import org.electrocodeogram.cpc.core.utils.CoreHistoryUtils;
import org.electrocodeogram.cpc.core.utils.HistoryEntry;


public class CPCReplay extends Replay
{
	private static final Log log = LogFactory.getLog(CPCReplay.class);

	private static IStoreProvider storeProvider = (IStoreProvider) CPCCorePlugin.getProviderRegistry().lookupProvider(
			IStoreProvider.class);

	/**
	 * Creates a complete {@link Replay} set for the given clone by reading its
	 * {@link ICloneModificationHistoryExtension} data, if available.
	 *  
	 * @param clone the clone to create a {@link Replay} set for, never null.
	 */
	public CPCReplay(IClone clone)
	{
		if (log.isTraceEnabled())
			log.trace("CPCReplay() - clone: " + clone);
		assert (clone != null);

		/*
		 * First get modification history data for clone.
		 */

		ICloneModificationHistoryExtension history = (ICloneModificationHistoryExtension) clone
				.getExtension(ICloneModificationHistoryExtension.class);
		if (history == null || history.isPartial())
		{
			//get full version of history
			history = (ICloneModificationHistoryExtension) storeProvider.getFullCloneObjectExtension(clone,
					ICloneModificationHistoryExtension.class);
		}

		if (log.isTraceEnabled())
			log.trace("CPCReplay() - history: " + history);

		/*
		 * Set original content
		 */
		int i = 0;
		ReplayElement element = new ReplayElement(clone.getCreationDate(), i, new String[0], clone.getCreator(), "",
				clone.getUuid(), clone.getOriginalContent());
		this.addReplayElement(element);

		if (history == null)
		{
			log.trace("CPCReplay() - clone has no history data, creating empty Replay.");
			return;
		}

		assert (!history.isPartial());

		/*
		 * Now create a ReplayElement for each modification.
		 */

		//reconstruct the contents for each entry in our history
		List<HistoryEntry> historyEntries = CoreHistoryUtils.getCloneAllContents(storeProvider, clone, history);

		for (HistoryEntry entry : historyEntries)
		{
			if (log.isTraceEnabled())
				log.trace("DIFF: " + entry.getDiff());

			element = new ReplayElement(entry.getDiff().getCreationDate(), ++i, new String[0], entry.getDiff()
					.getCreator(), "x", clone.getUuid(), entry.getContentAfterDiff());
			Diff elemDiff = new Diff(entry.getDiff().getOffset(), entry.getDiff().getOffset()
					+ entry.getDiff().getLength(), entry.getDiff().getText());
			element.setDiff(elemDiff);

			if (log.isTraceEnabled())
				log.trace("ReplayElement: " + element);

			this.addReplayElement(element);
		}
	}

	@Override
	public String toString()
	{
		return "CPCReplay[current: " + getCurrentElement() + ", set: " + getElements() + "]";
	}
}

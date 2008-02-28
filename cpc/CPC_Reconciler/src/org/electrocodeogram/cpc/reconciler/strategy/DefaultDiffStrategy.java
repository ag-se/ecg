package org.electrocodeogram.cpc.reconciler.strategy;


import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;
import org.electrocodeogram.cpc.core.CPCCorePlugin;
import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.data.ICloneFile;
import org.electrocodeogram.cpc.core.api.provider.reconciler.IDiffResult;
import org.electrocodeogram.cpc.core.api.provider.reconciler.IReconciliationResult;
import org.electrocodeogram.cpc.core.api.provider.track.CPCPosition;
import org.electrocodeogram.cpc.core.api.provider.track.IPositionUpdateStrategyProvider;
import org.electrocodeogram.cpc.reconciler.api.strategy.IReconcilerStrategy;


/**
 * This strategy uses the provided diff and tries to apply it to each clone in turn.<br/>
 * <br/>
 * Unfortunately this is not as safe as it may sound at first. It is important to keep in
 * mind that the generated diffs are by no means guaranteed to correspond to the actual
 * modifications of the file. As such this strategy may modify or remove
 * clones which were not actually modified by the external edit.
 * 
 * @author vw
 */
public class DefaultDiffStrategy implements IReconcilerStrategy
{
	private static Log log = LogFactory.getLog(DefaultDiffStrategy.class);

	/*
	 * (non-Javadoc)
	 * @see org.electrocodeogram.cpc.reconciler.strategy.IExternalModificationReconcilerStrategy#reconcile(org.electrocodeogram.cpc.core.api.data.ICloneFile, java.util.List, java.lang.String, java.lang.String, java.util.List, java.util.LinkedList, org.electrocodeogram.cpc.core.api.provider.reconciler.IReconciliationResult)
	 */
	@Override
	public Status reconcile(ICloneFile cloneFile, List<IClone> persistedClones, String persistedFileContent,
			String newFileContent, List<IDiffResult> differences, LinkedList<IClone> pendingClones,
			IReconciliationResult result)
	{
		if (log.isTraceEnabled())
			log.trace("reconcile() - ..., pendingClones: " + pendingClones + ", result: " + result);

		/*
		 * This strategy does not re-evaluate any clones which have already been handled by
		 * another strategy. This means that there is nothing to do if pendingClones is empty.
		 */
		if (pendingClones.isEmpty())
		{
			log.trace("reconcile() - pendingClones is empty, SKIPPING");
			return Status.SKIPPED;
		}

		/*
		 * Prepare data
		 */

		//get a position update strategy provider
		IPositionUpdateStrategyProvider posUpdateStrategyProvider = (IPositionUpdateStrategyProvider) CPCCorePlugin
				.getProviderRegistry().lookupProvider(IPositionUpdateStrategyProvider.class);
		assert (posUpdateStrategyProvider != null);

		//create a fake document for this modification process
		Document document = new Document(persistedFileContent);

		//create a fake position array for all clones
		CPCPosition[] positions = new CPCPosition[pendingClones.size()];
		int i = 0;
		for (IClone clone : pendingClones)
			positions[i++] = new CPCPosition(clone);

		/*
		 * Process diffs
		 */

		DocumentEvent event;
		int offset;
		int length;
		String text;
		//for each extracted file modification
		//diffs are handled in reversed order as each diff would otherwise invalidate the offsets of
		//the other diffs when applied
		for (int j = differences.size() - 1; j >= 0; --j)
		{
			IDiffResult diff = differences.get(j);

			//extract some data from the diff
			offset = diff.getOffset();
			length = (diff.isDelete() ? diff.getLength() : 0);
			text = (diff.isInsert() ? diff.getText() : "");

			if (log.isTraceEnabled())
				log.trace("DIFF: " + diff + ", offset: " + offset + ", length: " + length + ", text: " + text);

			//apply the diff to the document
			try
			{
				if (log.isTraceEnabled())
					log.trace("  replacing: \"" + document.get(offset, length) + "\" with \"" + text + "\"");

				document.replace(offset, length, text);
			}
			catch (BadLocationException e)
			{
				log.error("reconcile() - unable to apply diff to document - diff: " + diff + ", document: " + document,
						e);
			}

			//create a fake document event
			event = new DocumentEvent(document, offset, length, text);

			if (log.isTraceEnabled())
				for (CPCPosition pos : positions)
					log.trace("PRE-POS: " + pos);

			//update the position of all clones
			posUpdateStrategyProvider.updatePositions(event, positions);

			if (log.isTraceEnabled())
				for (CPCPosition pos : positions)
					log.trace("POST-POS: " + pos);
		}

		/*
		 * Extracting clone data from positions
		 */

		posUpdateStrategyProvider.extractCloneData(positions, result.getMovedClones(), result.getModifiedClones(),
				result.getRemovedClones(), null);

		//now update the clone offsets with the data from the positions array
		/*
		for (CPCPosition position : positions)
		{
			IClone clone = position.getClone();

			//check whether the clone was deleted
			if (position.isDeleted())
			{
				if (log.isTraceEnabled())
					log.trace("reconcile() - clone deleted: " + clone);

				result.getRemovedClones().add(clone);

				continue;
			}

			//check whether the clone was moved
			if (clone.getOffset() != position.getOffset() || clone.getLength() != position.getLength())
			{
				//the clone was moved
				if (log.isTraceEnabled())
					log.trace("reconcile() - clone moved: " + clone);

				result.getMovedClones().add(clone);
			}

			//update clone object
			clone.setOffset(position.getOffset());
			clone.setLength(position.getLength());

			if (position.isContentModified())
			{
				//the clone content was modified
				if (log.isTraceEnabled())
					log.trace("reconcile() - clone content modified: " + clone);

				((ICreatorClone) clone).setContent(position.getContent());
				result.getModifiedClones().add(clone);
			}
		}
		*/

		//now all former pending clones should be handled
		log.trace("reconcile() - marking all pending clones as handled");
		pendingClones.clear();

		//we're done
		return Status.FULL;
	}

}

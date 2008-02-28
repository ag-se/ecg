package org.electrocodeogram.cpc.core.api.provider.track;


import java.util.List;

import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.electrocodeogram.cpc.core.api.data.IClone;
import org.electrocodeogram.cpc.core.api.data.extension.ICloneModificationHistoryExtension;
import org.electrocodeogram.cpc.core.api.provider.IProvider;
import org.electrocodeogram.cpc.core.api.provider.store.IStoreProvider;


/**
 * An {@link IPositionUpdateStrategyProvider} specifies how clone entries should be affected by modifications to 
 * a file. I.e. whether to consider text typed next to a clone to be part of the clone or not.
 * <p>
 * The <em>CPC Track</em> module uses the main {@link IPositionUpdateStrategyProvider} for all clone position
 * modifications.
 * 
 * @author vw
 * 
 * @see CPCPosition
 * @see CPCDocumentEvent
 */
public interface IPositionUpdateStrategyProvider extends IProvider
{
	/**
	 * Updates the given positions in-place according to the given event.
	 * <br>
	 * The {@link IClone} elements inside of any affected {@link CPCPosition} object are <b>not</b> updated.
	 * <p>
	 * <b>NOTE:</b> The signature of this interface was dictated by performance considerations.
	 * To still allow reuse in other contexts an implementation must <b>not</b> make use of
	 * any other methods of the encapsulated document object than {@link IDocument#get()} and
	 * {@link IDocument#get(int, int)}.
	 * <br>
	 * A caller may provide a custom {@link IDocument} implementation which supports only those two
	 * methods.
	 * <p>
	 * <b>NOTE:</b> An implementation is <b>not</b> allowed to access an {@link IStoreProvider}.
	 * See comments in <em>CPCPositionUpdater</em> for more details.
	 * 
	 * @param event the event to process, never null. Make sure you understand the limitations of the
	 * 		underlying {@link IDocument} object if you implement this interface.
	 * @param positions an array with positions which should be updated, never null.
	 * 			Positions are updated in place. All positions are guaranteed to be {@link CPCPosition} objects.
	 * @return true if at least one position was modified, false otherwise.
	 * 
	 * @see CPCPosition
	 */
	public boolean updatePositions(DocumentEvent event, Position[] positions);

	/**
	 * Takes an array of {@link CPCPosition}s and extracts any clone data modifications from it.
	 * <br>
	 * Detailed descriptions of each modification are added to each clone's {@link ICloneModificationHistoryExtension}.
	 * <br>
	 * Clones may be part of the <em>movedClones</em> and <em>modifiedClones</em> lists at the same time.
	 * <br>
	 * The <em>removedClones</em> list must always be disjunct from the <em>movedClones</em> and <em>modifiedClones</em> lists.
	 * 
	 * @param positions an array of {@link CPCPosition}s to extract data from, never null.
	 * @param movedClones an empty result list in which moved clones should be stored, never null.
	 * @param modifiedClones an empty result list in which modified clones should be stored, never null.
	 * @param removedClones an empty result list in which removed clones should be stored, never null.
	 * @param document optional parameter, if present any removed clone will also have its position removed from the document, may be NULL.
	 */
	public void extractCloneData(Position[] positions, List<IClone> movedClones, List<IClone> modifiedClones,
			List<IClone> removedClones, IDocument document);
}

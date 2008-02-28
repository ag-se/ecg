package org.electrocodeogram.cpc.imports.api.data;


import org.electrocodeogram.cpc.core.api.data.ICloneObjectExtension;
import org.electrocodeogram.cpc.imports.api.imports.adapter.IImportToolAdapter;


/**
 * Optional clone object extension which <b>may</b> be used by {@link IImportToolAdapter}s to
 * provide some per-clone confidence data.
 * <p>
 * An {@link IImportToolAdapter} is not required to make use of this feature.
 * 
 * @author vw
 */
public interface IImportCloneObjectExtension extends ICloneObjectExtension
{
	/**
	 * Provides the import implementations confidence in the detection accuracy of this clone. 
	 * 
	 * @return confidence in percent (range: 0-100) or -1 if not set.
	 */
	public byte getConfidence();

}

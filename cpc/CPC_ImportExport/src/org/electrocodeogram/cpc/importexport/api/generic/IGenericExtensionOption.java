package org.electrocodeogram.cpc.importexport.api.generic;


/**
 * A generic option wrapper which represents a single configuration option of some
 * registered extension element.<br/>
 * <br/>
 * The <em>CPC Imports</em> and <em>CPC Exports</em> modules define their own sub-interfaces.
 * 
 * @author vw
 */
public interface IGenericExtensionOption
{
	/**
	 * Retrieves the id/key under which any user provided values for this option are to
	 * be stored in the an option map.
	 * 
	 * @return a unique identifier for this option, never null.
	 */
	public String getId();

	/**
	 * Retrieves the name/description of the option. 
	 * 
	 * @return a human readable name for this option, never null. 
	 */
	public String getName();

	/**
	 * Retrieves the default value of the option, if a default value was specified.
	 * 
	 * @return a default value for this option, may be NULL.
	 */
	public String getDefaultValue();

	/**
	 * Retrieves the currently set value.<br/>
	 * Automatically falls back to default value, if value is null and default value is non-null.
	 * 
	 * @return currently set value, may be NULL.
	 */
	public String getValue();

	/**
	 * Sets the value for this option.
	 * 
	 * @param value the new value, may be NULL.
	 */
	public void setValue(String value);

}

package org.electrocodeogram.codereplay.dataProvider;

/**
 * Class Causes provides a mapping between the exact causes that led to the creation
 * of a specific event and the basic causes that are needed by the logic.
 * While the logic only needs to know if an event for example indicates a creation or
 * modification of an element, from the viewpoint of the creator of the event the cause
 * could be much more specific.
 * 
 * New causes have to be associated to one of the three basic causes by adding them to the
 * array.
 * 
 * basic causes are ADD, DEL(ETE), CHANGE and ID_CHANGED. 
 *
 * IMPORTANT: Since there is now the possibility to add new Readers(see {@link AbstractReader}),
 * the functionality of this class should be moved in there in order to have only one location
 * for all new data that has to be added when new doctypes are made available.
 * 
 * @author marco kranz
 */
public final class Causes {

	private static String[] ADDED = {"ADDED", "MERGED_ADD_AT_START", "MERGED_ADD_AT_END"};
	
	private static String[] CHANGED = {"CHANGED", "EXTENDED_AT_START", "EXTENDED_IN_BETWEEN", "EXTENDED_AT_END", "SPLIT_DEL_AT_START", "SPLIT_DEL_AT_END", "SPLIT_ADD_AT_END", "SPLIT_ADD_AT_START", "SHORTENED_AT_START", "SHORTENED_IN_BETWEEN", "SHORTENED_AT_END"};
	
	private static String[] DELETED = {"DELETED", "MERGED_DEL_AT_START", "MERGED_DEL_AT_END", "SHORTENED_AT_ALL"};
	
	/**
	 * basic cause in case the identifier changed
	 */
	public static String ID_CHANGED = "IDENTIFIER_CHANGED";
	/**
	 * basic cause in case an element has been added
	 */
	public static String ADD = "ADDED";
	/**
	 * basic cause in case an element was changed
	 */
	public static String CHANGE = "CHANGED";
	/**
	 * basic cause in case an element was deleted
	 */
	public static String DEL = "DELETED";
	/**
	 * indicates that a detailed cause is unknown(no mapping provided)
	 */
	public static String UNKNOWN = "UNKNOWN";
	
	
	private Causes(){}
	
	
	/**
	 * This Method receives the detailed cause(as String) and mapps it 
	 * to one of the basic causes.
	 * 
	 * @param cause the detailed cause for the creation of the event described by a {@link ReplayElement}
	 * @return the basic cause or UNKNOWN in case no mapping is provided
	 */
	public static String getBasicCause(String cause){
		for(int i = 0; i < ADDED.length; i++){
			if(cause.equals(ADDED[i]))
				return ADD;
		}
		for(int i = 0; i < CHANGED.length; i++){
			if(cause.equals(CHANGED[i]))
				return CHANGE;
		}
		for(int i = 0; i < DELETED.length; i++){
			if(cause.equals(DELETED[i]))
				return DEL;
		}
		if(cause.equals(ID_CHANGED))
			return ID_CHANGED;
		else
			return UNKNOWN;
	}
}

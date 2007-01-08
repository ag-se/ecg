package org.electrocodeogram.codereplay.eventIO;

import java.io.File;

/**
 * Abstract superclass for all new readers.
 * To implement a new reader extend this class. A new Reader should be created to be able to read new filetypes.
 * 
 * @author marco kranz
 */
public abstract class AbstractReader{
	
	
	/**
	 * New readers should be implemented as singletons. overwrite this method to 
	 * return the singleton instance of your reader.
	 * 
	 * @return null (basic implementation)
	 */
	public static AbstractReader getInstance(){
		return null;
	}
	
	/**
	 * This method will be called when a new file should be opened.
	 * 
	 * @param file the file to be opened.
	 */
	public abstract void openFile(File file);
	
	/**
	 * should return the name of your reader.
	 * 
	 * @return name of reader 
	 */
	public abstract String getName();
}

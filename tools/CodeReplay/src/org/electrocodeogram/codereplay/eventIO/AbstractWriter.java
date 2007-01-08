package org.electrocodeogram.codereplay.eventIO;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;

/**
 * Abstract superclass for all new writers.
 * To implement a new writer extend this class. A new Writer should be created to be able to write new filetypes.
 * 
 * @author marco kranz
 */
public abstract class AbstractWriter {

	/**
	 * New writers should be implemented as singletons. overwrite this method to 
	 * return the singleton instance of your writer.
	 * 
	 * @return null (basic implementation)
	 */
	public static AbstractWriter getInstance(){
		return null;
	}
	
	/**
	 * This method should write all of the Replays in the enumeration to the specified file.
	 * 
	 * @param file the file to write to
	 * @param replays enumeration of replays that should be written to the file
	 * @throws IOException
	 */
	public abstract void write(File file, Enumeration replays) throws IOException;
	
	/**
	 * should return the name of your writer.
	 * 
	 * @return name of writer 
	 */
	public abstract String getName();
}
